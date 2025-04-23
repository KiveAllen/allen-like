package com.allen.thumb.controller;

import com.allen.thumb.common.BaseResponse;
import com.allen.thumb.common.ResultUtils;
import com.allen.thumb.model.dto.thumb.DoThumbRequest;
import com.allen.thumb.service.ThumbService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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

    private final Counter successCounter;
    private final Counter failureCounter;

    public ThumbController(MeterRegistry registry) {
        this.successCounter = Counter.builder("thumb.success.count")
                .description("Total successful thumb")
                .register(registry);
        this.failureCounter = Counter.builder("thumb.failure.count")
                .description("Total failed thumb")
                .register(registry);
    }


    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean success;
        try {
            success = thumbService.doThumb(doThumbRequest, request);
            if (success) {
                successCounter.increment();
            } else {
                failureCounter.increment();
            }
        } catch (Exception e) {
            failureCounter.increment();
            throw e;
        }
        return ResultUtils.success(success);
    }


    @PostMapping("/undo")
    public BaseResponse<Boolean> undoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return ResultUtils.messageHandleSuccess(() -> thumbService.undoThumb(doThumbRequest, request));
    }

}
