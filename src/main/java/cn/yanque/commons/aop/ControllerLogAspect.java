package cn.yanque.commons.aop;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class ControllerLogAspect {

    /*
    切点表达式:   访问权限修饰 [public]  返回值类型  包名.类名.方法名(参数列表)
     cn.yanque..    .. 下所有的子包.
     controller..   controller包下所有的子包.
     *.*(..)  第一个*  所有的类.
              第二个*  所有的方法.
              (..) 代表任意方法的参数.

     */
    @Around("execution(* cn.yanque..controller..*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "#" + signature.getName();
        HttpServletRequest request = getCurrentRequest();

        log.info("接口开始: uri={}, httpMethod={}, controller={}, args={}",
                request == null ? "-" : request.getRequestURI(),
                request == null ? "-" : request.getMethod(),
                methodName,
                JSON.toJSONString(formatArgs(joinPoint.getArgs())));

        //手动调用目标方法. 目标方法的返回值,result
        Object result = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;
        log.info("接口结束: uri={}, controller={}, cost={}ms, result={}",
                request == null ? "-" : request.getRequestURI(),
                methodName,
                cost,
                JSON.toJSONString(result));
        return result;
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest();
    }

    /**
     * 日志参数中不能直接序列化 MultipartFile，否则序列化器会尝试把文件资源解析成 URL，
     * 从而触发“MultipartFile resource cannot be resolved to URL”异常。
     */
    private Object[] formatArgs(Object[] args) {
        return Arrays.stream(args)
                .map(this::formatArg)
                .toArray(Object[]::new);
    }

    private Object formatArg(Object arg) {
        if (arg instanceof MultipartFile file) {
            Map<String, Object> fileInfo = new LinkedHashMap<>();
            fileInfo.put("fieldName", file.getName());
            fileInfo.put("originalFilename", file.getOriginalFilename());
            fileInfo.put("contentType", file.getContentType());
            fileInfo.put("size", file.getSize());
            return fileInfo;
        }
        if (arg instanceof MultipartFile[] files) {
            return Arrays.stream(files)
                    .map(this::formatArg)
                    .toArray(Object[]::new);
        }
        if (arg instanceof ServletRequest) {
            return "ServletRequest";
        }
        if (arg instanceof ServletResponse) {
            return "ServletResponse";
        }
        return arg;
    }
}
