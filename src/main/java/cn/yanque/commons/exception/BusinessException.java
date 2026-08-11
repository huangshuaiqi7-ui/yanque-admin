package cn.yanque.commons.exception;

import cn.yanque.commons.apires.IErrorCode;
import lombok.Data;

/**
 * @ClassName BusinessException
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description 自定义异常类.
 */
@Data
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Integer code;

    // message , 从父类当中继承过来的.


    public  BusinessException(IErrorCode errorCode){//errorCode: code,message
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public  BusinessException(Integer code,String message){
        super(message);
        this.code = code;
    }


    public  BusinessException(IErrorCode errorCode,String message){
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 提供工具类方法, 便于直接快速的获取BusinessException
     * @param errorCode
     *
     */
    public static BusinessException of(IErrorCode errorCode){
        return new BusinessException(errorCode);
    }

    public static BusinessException of(Integer code,String message){
        return new BusinessException(code,message);
    }

    public static BusinessException of(IErrorCode errorCode,String message){
        return new BusinessException(errorCode,message);
    }


}
