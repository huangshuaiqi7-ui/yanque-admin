package cn.yanque.modules.homeworks.pojo.vo.resvo;
import cn.yanque.modules.prepayorders.pojo.vo.resvo.PendingPayOrderRes;
import lombok.*;
@Data public class StudentLoginRes {
    private boolean needPay=false; private boolean needCompleteProfile=false;
    private String token; private String signSecret;
    private String pendingPayToken; private String pendingPaySignSecret;
    private PendingPayOrderRes pendingOrder; private StudentInfo student;
    @Data @AllArgsConstructor public static class StudentInfo { private Long id; private String name; private String phone; }
}
