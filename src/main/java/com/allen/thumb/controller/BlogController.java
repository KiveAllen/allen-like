package com.allen.thumb.controller;

import com.allen.thumb.common.BaseResponse;
import com.allen.thumb.common.ResultUtils;
import com.allen.thumb.model.entity.Blog;
import com.allen.thumb.model.vo.BlogVO;
import com.allen.thumb.service.BlogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    @GetMapping("/get")
    public BaseResponse<BlogVO> get(long blogId, HttpServletRequest request) {
        return ResultUtils.messageHandleSuccess(() -> blogService.getBlogVOById(blogId, request));
    }

    @GetMapping("/list")
    public BaseResponse<List<BlogVO>> list(HttpServletRequest request) {
        List<Blog> blogList = blogService.list();
        if (blogList == null) {
            return ResultUtils.success(List.of());
        }
        return ResultUtils.messageHandleSuccess(() -> blogService.getBlogVOList(blogList, request));
    }

}
