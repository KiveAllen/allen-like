package com.allen.thumb.service.impl;

import com.allen.thumb.constant.RedisLuaScriptConstant;
import com.allen.thumb.listener.thumb.msg.ThumbEvent;
import com.allen.thumb.mapper.ThumbMapper;
import com.allen.thumb.model.dto.thumb.DoThumbRequest;
import com.allen.thumb.model.entity.Thumb;
import com.allen.thumb.model.entity.User;
import com.allen.thumb.model.enums.LuaStatusEnum;
import com.allen.thumb.service.ThumbService;
import com.allen.thumb.service.UserService;
import com.allen.thumb.util.RedisKeyUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service("thumbService")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceMQImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final PulsarTemplate<ThumbEvent> pulsarTemplate;

    /**
 * 处理点赞请求
 *
 * 此方法接收一个点赞请求对象和HTTP请求对象，验证请求参数的有效性，
 * 然后执行点赞逻辑，包括在Redis中记录用户的点赞操作，并发送点赞事件到消息队列
 *
 * @param doThumbRequest 点赞请求对象，包含需要点赞的博客ID等信息
 * @param request HTTP请求对象，用于获取当前登录用户信息
 * @return 如果点赞成功，返回true；否则抛出异常
 * @throws RuntimeException 如果参数错误或用户已点赞，则抛出运行时异常
 */
@Override
public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
    // 检查请求参数是否为空，如果为空则抛出异常
    if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
        throw new RuntimeException("参数错误");
    }

    // 获取当前登录用户信息
    User loginUser = userService.getLoginUser(request);
    Long loginUserId = loginUser.getId();
    Long blogId = doThumbRequest.getBlogId();
    String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);

    // 执行 Lua 脚本，点赞存入 Redis
    long result = redisTemplate.execute(
            RedisLuaScriptConstant.THUMB_SCRIPT_MQ,
            List.of(userThumbKey),
            blogId
    );

    // 如果点赞失败（用户已点赞），抛出异常
    if (LuaStatusEnum.FAIL.getValue() == result) {
        throw new RuntimeException("用户已点赞");
    }

    // 创建点赞事件对象，准备发送到消息队列
    ThumbEvent thumbEvent = ThumbEvent.builder()
            .blogId(blogId)
            .userId(loginUserId)
            .type(ThumbEvent.EventType.INCR)
            .eventTime(LocalDateTime.now())
            .build();

    // 异步发送点赞事件到消息队列，如果发送失败，则从Redis中删除点赞记录，并记录错误日志
    pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
        redisTemplate.opsForHash().delete(userThumbKey, blogId.toString(), true);
        log.error("点赞事件发送失败: userId={}, blogId={}", loginUserId, blogId, ex);
        return null;
    });

    // 点赞成功，返回true
    return true;
}


    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogId();
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
        // 执行 Lua 脚本，点赞记录从 Redis 删除  
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT_MQ,
                List.of(userThumbKey),
                blogId
        );
        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new RuntimeException("用户未点赞");
        }
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .blogId(blogId)
                .userId(loginUserId)
                .type(ThumbEvent.EventType.DECR)
                .eventTime(LocalDateTime.now())
                .build();
        pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
            redisTemplate.opsForHash().put(userThumbKey, blogId.toString(), true);
            log.error("点赞事件发送失败: userId={}, blogId={}", loginUserId, blogId, ex);
            return null;
        });

        return true;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }

}
