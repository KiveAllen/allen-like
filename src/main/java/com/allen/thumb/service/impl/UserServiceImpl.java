package com.allen.thumb.service.impl;

import com.allen.thumb.constant.UserConstant;
import com.allen.thumb.mapper.UserMapper;
import com.allen.thumb.model.entity.User;
import com.allen.thumb.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * @author KiveAllen
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2025-04-18 14:59:02
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
    }
}




