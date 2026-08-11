package cn.yanque.commons.apires;

/**
 * @ClassName IErrorCode
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description  统一返回状态码的接口定义
 */
public interface IErrorCode {


    /**
     * 获取状态码
     * @return
     */
    Integer getCode();


    /**
     * 获取状态码信息
     * @return
     */
    String getMessage();
}
