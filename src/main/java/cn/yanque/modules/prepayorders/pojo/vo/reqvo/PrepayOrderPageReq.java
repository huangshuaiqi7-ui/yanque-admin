package cn.yanque.modules.prepayorders.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class PrepayOrderPageReq {
    private String keyword; private String orderStatus;
    @Min(1) private Integer pageNum=1; @Min(1) @Max(1000) private Integer pageSize=10;
}
