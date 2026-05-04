package com.example.HousePrediction.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("FAILED", "Lỗi hệ thống không xác định!", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_AREA("FAILED", "Diện tích không hợp lệ!", HttpStatus.BAD_REQUEST),
    PYTHON_API_ERROR("FAILED", "Không thể kết nối đến AI Python!", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("FAILED", "Không tìm thấy người dùng!", HttpStatus.NOT_FOUND),
    USER_EXISTED("FAILED", "Tên đăng nhập đã tồn tại!", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH("FAILED", "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST);

    private final String status;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String status, String message, HttpStatus httpStatus) {
        this.status = status;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}