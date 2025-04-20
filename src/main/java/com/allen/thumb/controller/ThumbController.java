package com.allen.thumb.controller;

import com.allen.thumb.common.BaseResponse;
import com.allen.thumb.common.ResultUtils;
import com.allen.thumb.model.dto.thumb.DoThumbRequest;
import com.allen.thumb.service.ThumbService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("thumb")
public class ThumbController {
    @Resource
    private ThumbService thumbService;

    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return ResultUtils.messageHandleSuccess(() -> thumbService.doThumb(doThumbRequest, request));
    }

    @PostMapping("/undo")
    public BaseResponse<Boolean> undoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return ResultUtils.messageHandleSuccess(() -> thumbService.undoThumb(doThumbRequest, request));
    }

}
