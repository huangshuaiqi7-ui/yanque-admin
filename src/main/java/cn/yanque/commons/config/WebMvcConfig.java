package cn.yanque.commons.config;

import cn.yanque.commons.constant.JwtConstants;
import cn.yanque.commons.interceptor.SignInterceptor;
import cn.yanque.commons.interceptor.TokenInterceptor;
import cn.yanque.commons.interceptor.PermissionInterceptor;
import cn.yanque.commons.interceptor.PendingPayInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private static final String[] AUTH_EXCLUDE_PATHS = {
            JwtConstants.LOGIN_PATH,
            JwtConstants.STUDENT_LOGIN_PATH,
            "/student/pending/**",
            "/yop-callback/**",
            "/internal/**"
    };

    //使用构造器注入:
    private final TokenInterceptor tokenInterceptor;
    private final SignInterceptor signInterceptor;
    private final PermissionInterceptor permissionInterceptor;
    private final PendingPayInterceptor pendingPayInterceptor;


    public WebMvcConfig(TokenInterceptor tokenInterceptor, SignInterceptor signInterceptor,
                        PermissionInterceptor permissionInterceptor, PendingPayInterceptor pendingPayInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
        this.signInterceptor = signInterceptor;
        this.permissionInterceptor = permissionInterceptor;
        this.pendingPayInterceptor = pendingPayInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(AUTH_EXCLUDE_PATHS)
                .order(0);
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(AUTH_EXCLUDE_PATHS)
                .order(1);
        registry.addInterceptor(signInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(AUTH_EXCLUDE_PATHS)
                .order(2);
        registry.addInterceptor(pendingPayInterceptor)
                .addPathPatterns("/student/pending/**")
                .order(0);
    }
}
