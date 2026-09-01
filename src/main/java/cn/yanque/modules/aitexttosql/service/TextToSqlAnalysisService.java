package cn.yanque.modules.aitexttosql.service;

import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.context.UserContext;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlAnalyzeReq;
import cn.yanque.modules.roles.mapper.SysRoleMapper;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Text-to-SQL 数据分析页业务服务。
 */
@Service
public class TextToSqlAnalysisService {
    private final SysRoleMapper roleMapper;
    private final TextToSqlPythonClient pythonClient;

    public TextToSqlAnalysisService(SysRoleMapper roleMapper, TextToSqlPythonClient pythonClient) {
        this.roleMapper = roleMapper;
        this.pythonClient = pythonClient;
    }

    /**
     * 当前登录用户发起自然语言分析。
     *
     * 前端只传问题；这里补齐 userId 和角色编码，再交给 Python Text-to-SQL 总流程。
     */
    public JSONObject analyze(TextToSqlAnalyzeReq req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(userId);
        return pythonClient.analyze(req, userId, roleCodes);
    }
}
