package cn.yanque.modules.aiprompt.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aiprompt.mapper.PromptTemplateMapper;
import cn.yanque.modules.aiprompt.mapper.PromptTemplateVersionMapper;
import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateEntity;
import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateVersionEntity;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTestReq;
import cn.yanque.modules.aiprompt.service.PromptTestPythonClient;
import cn.yanque.modules.aiprompt.service.PromptTestService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 提示词测试业务实现。
 */
@Service
public class PromptTestServiceImpl implements PromptTestService {
    private final PromptTemplateMapper templateMapper;
    private final PromptTemplateVersionMapper versionMapper;
    private final PromptTestPythonClient pythonClient;

    /**
     * 创建提示词测试服务。
     *
     * @param templateMapper 提示词模板数据访问对象
     * @param versionMapper  提示词版本数据访问对象
     * @param pythonClient   Python 提示词测试客户端
     */
    public PromptTestServiceImpl(PromptTemplateMapper templateMapper,
                                 PromptTemplateVersionMapper versionMapper,
                                 PromptTestPythonClient pythonClient) {
        this.templateMapper = templateMapper;
        this.versionMapper = versionMapper;
        this.pythonClient = pythonClient;
    }

    /**
     * 执行提示词流式测试。
     *
     * @param req 测试请求参数
     * @return 流式测试 SSE
     */
    @Override
    public SseEmitter stream(PromptTestReq req) {
        PromptTemplateVersionEntity systemVersion = requireVersion(req.getSystemTemplateId(),
                req.getSystemVersionId(), "SYSTEM");
        PromptTemplateVersionEntity userVersion = requireVersion(req.getUserTemplateId(),
                req.getUserVersionId(), "USER");
        req.setModel(StrUtil.trim(req.getModel()));
        if (StrUtil.isBlank(req.getModel())) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEST_INVALID);
        }

        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> streamTest(emitter, systemVersion, userVersion, req)).start();
        return emitter;
    }

    /** 调用 Python 提示词测试接口，并把 SSE 事件原样转发给前端。 */
    private void streamTest(SseEmitter emitter,
                            PromptTemplateVersionEntity systemVersion,
                            PromptTemplateVersionEntity userVersion,
                            PromptTestReq req) {
        AtomicBoolean finished = new AtomicBoolean(false);
        try {
            pythonClient.streamPromptTest(systemVersion.getContent(), userVersion.getContent(), req, event -> {
                JSONObject data = event.data();
                sendEvent(emitter, event.event(), data);
                if ("done".equals(event.event()) || "error".equals(event.event())) {
                    finished.set(true);
                    emitter.complete();
                }
            });
            if (!finished.get()) {
                sendEvent(emitter, "error", Map.of("message", "AI提示词测试响应异常，请稍后重试"));
                emitter.complete();
            }
        } catch (Exception exception) {
            sendEvent(emitter, "error", Map.of("message", "AI提示词测试暂时不可用，请稍后重试"));
            emitter.complete();
        }
    }

    /** 校验模板、版本归属和提示词类型。 */
    private PromptTemplateVersionEntity requireVersion(Long templateId, Long versionId, String promptType) {
        PromptTemplateEntity template = templateMapper.selectById(templateId);
        if (template == null || !promptType.equals(template.getPromptType())) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEST_INVALID);
        }
        PromptTemplateVersionEntity version = versionMapper.selectById(versionId);
        if (version == null || !Objects.equals(version.getTemplateId(), templateId)
                || StrUtil.isBlank(version.getContent())) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEST_INVALID);
        }
        return version;
    }

    /** 发送一条 SSE 事件给前端。 */
    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(JSON.toJSONString(data == null ? new LinkedHashMap<>() : data)));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
