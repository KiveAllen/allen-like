package com.allen.thumb.common;

import java.util.function.Supplier;

/**
 * 返回工具类
 *
 * @author KiveAllen
 */
public class ResultUtils {

    /**
     * 成功
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }


//    /**
//     * 失败
//     */
//    public static BaseResponse<?> error(ErrorCode errorCode) {
//        return new BaseResponse<>(errorCode);
//    }

    /**
     * 失败
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    /**
     * 封装处理消息的函数(post)
     *
     * @param action 请求体
     * @param <T>    请求体类型
     * @return RestBean<T>
     */
    public static <T> BaseResponse<T> messageHandleSuccess(Supplier<T> action) {
        return success(action.get());
    }

}
