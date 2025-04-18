package com.allen.thumb.service;

import com.allen.thumb.model.dto.thumb.DoThumbRequest;
import com.allen.thumb.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author KiveAllen
 * @description 针对表【thumb】的数据库操作Service
 * @createDate 2025-04-18 14:59:02
 */
public interface ThumbService extends IService<Thumb> {

    /**
     * 点赞
     *
     * @param doThumbRequest 点赞参数
     * @param request        请求
     * @return {@link Boolean }
     */
    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    /**
     * 取消点赞
     *
     * @param doThumbRequest 取消参数
     * @param request        请求
     * @return {@link Boolean }
     */
    Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    Boolean hasThumb(Long blogId, Long userId);

}
