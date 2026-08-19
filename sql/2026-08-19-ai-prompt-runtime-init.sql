-- AI 运行时提示词初始化脚本。
-- 将 Python 原内置提示词迁移到 prompt_template / prompt_template_version。
-- 已存在同 code 模板或 v1 版本时不覆盖内容；仅在 active_version_id 为空时绑定 v1。

insert into prompt_template(code, name, agent_code, prompt_type, scene_code, status, description, create_time, update_time)
values('student_chat_system', '学生问答系统提示词', 'student_chat_agent', 'SYSTEM', 'CHAT', 'ACTIVE',
       '学生端 AI 问答默认系统提示词。', now(), now())
on duplicate key update code = code;
insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select t.id, 1, '你是燕雀教育的学习助教，服务对象是正在学习编程的学生。

回答要求：
1. 用中文回答，语气平实，不说客套话。
2. 先给结论，再给理由；能举例子就举一个最小的例子。
3. 涉及代码时，写出可直接运行的完整片段，并说明关键行在做什么。
4. 不确定的内容直接说不确定，不要编造 API、参数或版本号。
5. 与学习无关的问题，礼貌说明后引导回学习话题。', null, '初始化 Python 内置提示词', null
from prompt_template t
where t.code = 'student_chat_system'
  and not exists(select 1 from prompt_template_version v where v.template_id = t.id and v.version_no = 1);
update prompt_template t
join prompt_template_version v on v.template_id = t.id and v.version_no = 1
set t.active_version_id = v.id
where t.code = 'student_chat_system' and t.active_version_id is null;

insert into prompt_template(code, name, agent_code, prompt_type, scene_code, status, description, create_time, update_time)
values('student_chat_summary_context', '学生问答历史摘要上下文模板', 'student_chat_agent', 'SYSTEM', 'CHAT', 'ACTIVE',
       '学生端问答把历史摘要拼入 system prompt 的模板。', now(), now())
on duplicate key update code = code;
insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select t.id, 1, '【此前对话摘要】
{summary}

摘要是对更早对话的压缩，细节可能已丢失。若学生追问摘要里的细节，可以请他补充说明。',
       json_object('summary', '历史对话摘要'), '初始化 Python 内置提示词', null
from prompt_template t
where t.code = 'student_chat_summary_context'
  and not exists(select 1 from prompt_template_version v where v.template_id = t.id and v.version_no = 1);
update prompt_template t
join prompt_template_version v on v.template_id = t.id and v.version_no = 1
set t.active_version_id = v.id
where t.code = 'student_chat_summary_context' and t.active_version_id is null;

insert into prompt_template(code, name, agent_code, prompt_type, scene_code, status, description, create_time, update_time)
values('student_chat_rag_user', '学生问答知识库引用用户提示词', 'student_chat_agent', 'USER', 'RAG', 'ACTIVE',
       '学生端问答检索到知识库引用时的用户消息模板。', now(), now())
on duplicate key update code = code;
insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select t.id, 1, '学生问题：{question}

【QA知识库参考资料】
{context}

请优先基于上述参考资料回答。若参考资料不足以支持答案，请明确说明“当前知识库中未找到足够依据”，再给出通用学习建议；不要编造参考资料中不存在的事实、API、参数或版本号。',
       json_object('question', '学生本次问题', 'context', '知识库引用分段'), '初始化 Python 内置提示词', null
from prompt_template t
where t.code = 'student_chat_rag_user'
  and not exists(select 1 from prompt_template_version v where v.template_id = t.id and v.version_no = 1);
update prompt_template t
join prompt_template_version v on v.template_id = t.id and v.version_no = 1
set t.active_version_id = v.id
where t.code = 'student_chat_rag_user' and t.active_version_id is null;

insert into prompt_template(code, name, agent_code, prompt_type, scene_code, status, description, create_time, update_time)
values('student_chat_no_rag_user', '学生问答无知识库引用用户提示词', 'student_chat_agent', 'USER', 'RAG', 'ACTIVE',
       '学生端问答未检索到知识库引用时的用户消息模板。', now(), now())
on duplicate key update code = code;
insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select t.id, 1, '学生问题：{question}

【QA知识库参考资料】
未检索到知识库依据。

请先说明未检索到知识库依据，再基于你的通用学习助教能力给出学习建议；不要声称答案来自知识库。',
       json_object('question', '学生本次问题'), '初始化 Python 内置提示词', null
from prompt_template t
where t.code = 'student_chat_no_rag_user'
  and not exists(select 1 from prompt_template_version v where v.template_id = t.id and v.version_no = 1);
update prompt_template t
join prompt_template_version v on v.template_id = t.id and v.version_no = 1
set t.active_version_id = v.id
where t.code = 'student_chat_no_rag_user' and t.active_version_id is null;

insert into prompt_template(code, name, agent_code, prompt_type, scene_code, status, description, create_time, update_time)
values('chat_summary_system', '对话摘要系统提示词', 'summary_agent', 'SYSTEM', 'SUMMARY', 'ACTIVE',
       '会话压缩摘要的 system prompt。', now(), now())
on duplicate key update code = code;
insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select t.id, 1, '你是对话摘要助手。

请把旧摘要和新增对话合并成一段新的中文摘要，用于后续 AI 问答上下文。

要求：
1. 保留学生正在学习的主题、关键问题、已经解释过的结论。
2. 保留学生明显不理解或反复追问的点。
3. 不要写客套话，不要加入新知识。
4. 摘要控制在 500 字以内。', null, '初始化 Python 内置提示词', null
from prompt_template t
where t.code = 'chat_summary_system'
  and not exists(select 1 from prompt_template_version v where v.template_id = t.id and v.version_no = 1);
update prompt_template t
join prompt_template_version v on v.template_id = t.id and v.version_no = 1
set t.active_version_id = v.id
where t.code = 'chat_summary_system' and t.active_version_id is null;

insert into prompt_template(code, name, agent_code, prompt_type, scene_code, status, description, create_time, update_time)
values('chat_summary_user', '对话摘要用户提示词', 'summary_agent', 'USER', 'SUMMARY', 'ACTIVE',
       '会话压缩摘要的 user prompt。', now(), now())
on duplicate key update code = code;
insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select t.id, 1, '【旧摘要】
{summary}

【新增对话】
{messages}',
       json_object('summary', '旧摘要，没有时为无', 'messages', '本次要压缩的新增对话'), '初始化 Python 内置提示词', null
from prompt_template t
where t.code = 'chat_summary_user'
  and not exists(select 1 from prompt_template_version v where v.template_id = t.id and v.version_no = 1);
update prompt_template t
join prompt_template_version v on v.template_id = t.id and v.version_no = 1
set t.active_version_id = v.id
where t.code = 'chat_summary_user' and t.active_version_id is null;

insert into prompt_template(code, name, agent_code, prompt_type, scene_code, status, description, create_time, update_time)
values('knowledge_qa_system', '知识库问答测试系统提示词', 'knowledge_qa_agent', 'SYSTEM', 'RAG', 'ACTIVE',
       '管理端知识库问答测试 system prompt。', now(), now())
on duplicate key update code = code;
insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select t.id, 1, '你是燕雀管理后台的知识库问答测试助手。只能基于用户提供的引用分段回答；如果引用分段不足以支持答案，请明确说明“知识库中未找到足够依据”。回答要简洁，面向教务和课程业务场景。',
       null, '初始化 Python 内置提示词', null
from prompt_template t
where t.code = 'knowledge_qa_system'
  and not exists(select 1 from prompt_template_version v where v.template_id = t.id and v.version_no = 1);
update prompt_template t
join prompt_template_version v on v.template_id = t.id and v.version_no = 1
set t.active_version_id = v.id
where t.code = 'knowledge_qa_system' and t.active_version_id is null;

insert into prompt_template(code, name, agent_code, prompt_type, scene_code, status, description, create_time, update_time)
values('knowledge_qa_user', '知识库问答测试用户提示词', 'knowledge_qa_agent', 'USER', 'RAG', 'ACTIVE',
       '管理端知识库问答测试 user prompt。', now(), now())
on duplicate key update code = code;
insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select t.id, 1, '问题：{question}

引用分段：
{context}

请基于引用分段给出答案。',
       json_object('question', '测试问题', 'context', '知识库引用分段'), '初始化 Python 内置提示词', null
from prompt_template t
where t.code = 'knowledge_qa_user'
  and not exists(select 1 from prompt_template_version v where v.template_id = t.id and v.version_no = 1);
update prompt_template t
join prompt_template_version v on v.template_id = t.id and v.version_no = 1
set t.active_version_id = v.id
where t.code = 'knowledge_qa_user' and t.active_version_id is null;
