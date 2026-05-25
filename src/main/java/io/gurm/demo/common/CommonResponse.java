package io.gurm.demo.common;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonResponse<T> {
    private final Meta meta;
    private final T data;

    public CommonResponse(boolean success, String code, String message, T data) {
        this.meta = new Meta(success, code, message);
        this.data = data;
    }

    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(true, "SUCCESS", message, data);
    }

    public static CommonResponse<Object> error(String errorCode, String errorMessage) {
        return new CommonResponse<>(false, errorCode, errorMessage, null);
    }

    @Getter
    public static class Meta {
        private final boolean success;
        private final String code;
        private final String message;
        private final LocalDateTime timestamp;

        public Meta(boolean success, String code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }
}
