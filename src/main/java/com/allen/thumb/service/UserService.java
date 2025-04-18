package com.allen.thumb.service;

import com.allen.thumb.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author KiveAllen
 * @description 针对表【user】的数据库操作Service
 * @createDate 2025-04-18 14:59:02
 */
public interface UserService extends IService<User> {

    User getLoginUser(HttpServletRequest request);

}
