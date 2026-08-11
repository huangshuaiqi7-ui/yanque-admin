package cn.yanque.modules.payments.mapper;

import cn.yanque.modules.students.pojo.entity.StudentEntity;
import cn.yanque.modules.payments.pojo.PaymentOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface PaymentOrderMapper {
    List<PaymentOrderEntity> selectPage(@Param("orderNo") String orderNo,
                                        @Param("studentName") String studentName,
                                        @Param("studentPhone") String studentPhone,
                                        @Param("prepayOrderNo") String prepayOrderNo,
                                        @Param("status") String status);

    PaymentOrderEntity selectByOrderNo(@Param("orderNo") String orderNo);

    int countOrderNo(@Param("orderNo") String orderNo);

    int insert(PaymentOrderEntity entity);

    int updateProcessing(@Param("orderNo") String orderNo);

    int updateSuccess(@Param("orderNo") String orderNo,
                      @Param("uniqueOrderNo") String uniqueOrderNo,
                      @Param("paySuccessTime") Date paySuccessTime);

    int updateFail(@Param("orderNo") String orderNo);

    int updatePrepayPaid(@Param("orderNo") String prepayOrderNo);

    StudentEntity selectStudentByPhone(@Param("phone") String phone);

    int insertStudent(StudentEntity entity);

    int updateStudentProfile(StudentEntity entity);

    int countStudentNo(@Param("studentNo") String studentNo);

    int countStudentProduct(@Param("studentId") Long studentId,
                            @Param("productId") String productId,
                            @Param("sourceOrderNo") String sourceOrderNo);

    int insertStudentProduct(@Param("studentId") Long studentId,
                             @Param("productId") String productId,
                             @Param("sourceOrderNo") String sourceOrderNo);
}
