package com.ensam.projet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private String error;
    private Integer status;
    private LocalDateTime timestamp;
    private Object details;

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null, LocalDateTime.now(), null);
    }

    public static <T> ApiResponse<T> error(String error, int status) {
        return new ApiResponse<>(false, null, null, error, status, LocalDateTime.now(), null);
    }

    public static <T> ApiResponse<T> error(String error, int status, Object details) {
        return new ApiResponse<>(false, null, null, error, status, LocalDateTime.now(), details);
    }
}
