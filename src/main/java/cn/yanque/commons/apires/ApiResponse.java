package cn.yanque.commons.apires;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;

/**
 * @ClassName ApiResponse
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description 统一响应实体对象.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> implements Serializable {



    /*
       给当前的类创建一个序列化版本号.
       目的: 就是为了保证序列化和反序列化的时候, 不会出现问题.
     */
    private static final long serialVersionUID = 1L;

    /*
     * 状态码.
     */
    private Integer code;

    /*
     * 响应信息.
     */
    private String message;

    /*
     * 响应数据.
     * Object 类型,试可以实现功能, 但是不建议. 因为使用 Object 会导致类型转换. 就有转换的风险.
     */
    private T data;

    public ApiResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }



    /**
     * 创建一个成功响应对象.并且带有数据.
     * @param data 响应数据. 分页查询.
     * @return ApiResponse
     */

    public static <T> ApiResponse<T> success(T data) {
        // 创建一个成功响应对象.
        ApiResponse<T> apiResponse = new ApiResponse<>();
        // 设置状态码.
        apiResponse.setCode(CommonErrorCode.SUCCESS.getCode());
        // 响应信息.
        apiResponse.setMessage(CommonErrorCode.SUCCESS.getMessage());
        // 响应数据.
        apiResponse.setData(data);
        return apiResponse;
    }


    /**
     * 创建一个成功响应对象.   增删改.
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(CommonErrorCode.SUCCESS.getCode(),
                CommonErrorCode.SUCCESS.getMessage());
    }

    /**
     * 创建一个失败响应对象.
     * @param errorCode 错误码.
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> fail(IErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 创建一个失败响应对象.
     * @param errorCode 错误码.
     * @param message 错误信息. 自己手动传入状态码的错误信息i.
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> fail(IErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), message);
    }

    /**
     * 创建一个失败响应对象.
     * @param code 错误码.
     * @param message 错误信息.
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<>(code, message);
    }


}
