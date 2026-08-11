package cn.yanque.modules.products.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class ProductPageReq {
    private String keyword;
    @Min(1) private Integer pageNum=1;
    @Min(1) @Max(1000) private Integer pageSize=10;
}
