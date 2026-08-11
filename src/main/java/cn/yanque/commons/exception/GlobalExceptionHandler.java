package cn.yanque.commons.exception;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.CommonErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @ClassName GlobalExceptionHandler
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description 全局异常异常处理器.
 */

@RestControllerAdvice
@Slf4j // 日志
public class GlobalExceptionHandler {

    /*
    * 处理业务异常, 捕获的是自定义异常信息.
     */

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e){
        log.error("业务异常捕获:code= {} , message = {}",e.getCode(),e.getMessage(),e);
        HttpStatus status = resolveHttpStatus(e.getCode());
        return ResponseEntity.status(status).body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    private HttpStatus resolveHttpStatus(Integer code) {
        if (isUnauthorized(code)) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (CommonErrorCode.FORBIDDEN.getCode().equals(code)) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.OK;
    }

    /**
     * Token 与请求签名错误使用独立业务码，但 HTTP 状态统一保持为 401。
     */
    private boolean isUnauthorized(Integer code) {
        return CommonErrorCode.UNAUTHORIZED.getCode().equals(code)
                || (code >= 11001 && code <= 11003)
                || (code >= 12001 && code <= 12006);
    }

    /**
     * 处理 JSON 请求体的 Bean Validation 校验异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        return validationFailed(getValidationMessage(exception));
    }

    /**
     * 处理查询参数或表单参数绑定时的校验异常。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return validationFailed(getValidationMessage(exception));
    }

    /**
     * 处理方法参数上的约束校验异常。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .orElse(CommonErrorCode.PARAM_VALID_FAILED.getMessage());
        return validationFailed(message);
    }

    private String getValidationMessage(BindException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        if (fieldError == null) {
            return CommonErrorCode.PARAM_VALID_FAILED.getMessage();
        }
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ApiResponse<Void>> validationFailed(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(CommonErrorCode.PARAM_VALID_FAILED, message));
    }

    /*
    * 处理系统异常, 捕获的是系统异常信息.
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e){
        log.error("系统异常捕获 , message = {}:",e.getMessage(), e);
        return ApiResponse.fail(CommonErrorCode.FAILED.getCode(),CommonErrorCode.FAILED.getMessage());
    }





}
