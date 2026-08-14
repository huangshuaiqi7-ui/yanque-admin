package cn.yanque;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName YanQueApplication
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description yanque项目的主启动类
 */
@SpringBootApplication
@MapperScan({
        "cn.yanque.modules.users.mapper",
        "cn.yanque.modules.roles.mapper",
        "cn.yanque.modules.permissions.mapper",
        "cn.yanque.modules.campuses.mapper",
        "cn.yanque.modules.courses.mapper",
        "cn.yanque.modules.classes.mapper",
        "cn.yanque.modules.duties.mapper",
        "cn.yanque.modules.configs.mapper",
        "cn.yanque.modules.students.mapper",
        "cn.yanque.modules.homeworks.mapper",
        "cn.yanque.modules.examquestions.mapper",
        "cn.yanque.modules.exampapers.mapper",
        "cn.yanque.modules.exams.mapper",
        "cn.yanque.modules.studentexams.mapper",
        "cn.yanque.modules.products.mapper",
        "cn.yanque.modules.prepayorders.mapper",
        "cn.yanque.modules.payments.mapper",
        // AI 问答模块 Mapper，不加这里 Spring 启动时找不到 AiChatMapper Bean。
        "cn.yanque.modules.aichat.mapper",
        "cn.yanque.modules.aiknowledge.mapper"
})
public class YanQueApplication {

    public static void main(String[] args) {
        SpringApplication.run(YanQueApplication.class, args);
    }
}
