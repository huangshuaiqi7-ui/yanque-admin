-- Text-to-SQL SQL 生成提示词补丁：
-- 要求模型生成 SQL 时，所有字段都必须带表名或表别名。

set @template_id = (
    select id
    from prompt_template
    where code = 'text_to_sql_generate_sql_system'
    limit 1
);

set @next_version_no = (
    select coalesce(max(version_no), 0) + 1
    from prompt_template_version
    where template_id = @template_id
);

insert into prompt_template_version(template_id, version_no, content, variables, change_note, create_by)
select
    t.id,
    @next_version_no,
    replace(
        v.content,
        '5. 只能使用 DDL Resource 里确认存在的表和字段。',
        '5. 只能使用 DDL Resource 里确认存在的表和字段。
6. SQL 中所有字段必须带表名或表别名，例如 op.order_no、order_payment.status；不能生成 order_no、status 这种裸字段。
7. 推荐给每张表设置简短别名，并在 SELECT、WHERE、JOIN ON、GROUP BY、ORDER BY、函数参数中统一使用别名字段。'
    ),
    v.variables,
    '要求 Text-to-SQL 生成的字段必须带表名或表别名',
    null
from prompt_template t
inner join prompt_template_version v on v.id = t.active_version_id
where t.code = 'text_to_sql_generate_sql_system'
  and v.content not like '%SQL 中所有字段必须带表名或表别名%';

set @new_version_id = (
    select id
    from prompt_template_version
    where template_id = @template_id
      and content like '%SQL 中所有字段必须带表名或表别名%'
    order by version_no desc
    limit 1
);

update prompt_template
set active_version_id = @new_version_id,
    update_time = now()
where id = @template_id
  and @new_version_id is not null;
