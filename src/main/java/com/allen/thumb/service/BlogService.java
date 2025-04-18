package com.allen.thumb.service;

import com.allen.thumb.model.entity.Blog;
import com.allen.thumb.model.vo.BlogVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author KiveAllen
 * @description 针对表【blog】的数据库操作Service
 * @createDate 2025-04-18 14:59:02
 */
public interface BlogService extends IService<Blog> {

    BlogVO getBlogVOById(long blogId, HttpServletRequest request);

    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);


}
