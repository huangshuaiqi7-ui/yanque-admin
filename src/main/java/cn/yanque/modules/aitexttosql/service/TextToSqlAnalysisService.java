package cn.yanque.modules.aitexttosql.service;

import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.context.UserContext;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlAnalyzeReq;
import cn.yanque.modules.roles.mapper.SysRoleMapper;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Text-to-SQL 数据分析页业务服务。
 */
@Service
public class TextToSqlAnalysisService {
    private final SysRoleMapper roleMapper;
    private final TextToSqlPythonClient pythonClient;
    private final TextToSqlRunService runService;

    public TextToSqlAnalysisService(SysRoleMapper roleMapper, TextToSqlPythonClient pythonClient,
                                    TextToSqlRunService runService) {
        this.roleMapper = roleMapper;
        this.pythonClient = pythonClient;
        this.runService = runService;
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
        String conversationId = req.getConversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
            req.setConversationId(conversationId);
        }
        Long runId = runService.createRunning(conversationId, req.getQuestion(), userId, "USER");
        long start = System.currentTimeMillis();
        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(userId);
        try {
            JSONObject response = pythonClient.analyze(req, userId, roleCodes);
            long durationMs = System.currentTimeMillis() - start;
            runService.saveResult(conversationId, response, durationMs);
            response.put("runRecordId", runId);
            response.put("run_record_id", runId);
            response.put("conversationId", conversationId);
            response.put("conversation_id", conversationId);
            return response;
        } catch (RuntimeException exception) {
            runService.saveFailure(conversationId, exception.getMessage(), System.currentTimeMillis() - start);
            throw exception;
        }
    }
}
