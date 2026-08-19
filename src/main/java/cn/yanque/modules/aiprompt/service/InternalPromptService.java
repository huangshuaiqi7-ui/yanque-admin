package cn.yanque.modules.aiprompt.service;

import cn.yanque.modules.aiprompt.pojo.vo.resvo.InternalPromptRes;

/**
 * 内部服务读取提示词业务接口。
 */
public interface InternalPromptService {
    /**
     * 按提示词编码查询当前启用版本。
     *
     * @param code 提示词编码
     * @return 当前启用提示词
     */
    InternalPromptRes getActivePrompt(String code);
}
