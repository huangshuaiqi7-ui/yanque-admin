package cn.yanque.commons.filters;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @ClassName LogGuidFilter
 * @Author mrzhang
 * @Date 2026/7/18
 * @Description 日志记录的过滤器.
 */
@Component //纳入Spring容器管理
@Order(Integer.MIN_VALUE) // 保证过滤器优先执行.
public class LogGuidFilter extends OncePerRequestFilter {

    public static final String REQUEST_GUID_KEY = "guid";
    public static final String REQUEST_GUID_ATTR = "requestGuid";
    public static final String REQUEST_GUID_HEADER = "X-Request-Guid";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            //1: 获取日志guid
            String guid = revolveGuid(request, response, filterChain);

            // MDC 底层就是封装了ThreadLocal
            MDC.put(REQUEST_GUID_KEY, guid);
            // 3: 添加到请求属性中. 后续异步请求, ThreadLocal当中的会丢失.  request 不会丢失.  controller service (异步请求.ThreadLocal)
            request.setAttribute(REQUEST_GUID_ATTR, guid);
            // 4: 添加到响应头中
            response.addHeader(REQUEST_GUID_HEADER, guid);

            //发行:
            filterChain.doFilter(request, response);

            //空:
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally { 
            // ThreadLocal清理guid
             MDC.clear();
        }
    }

    /*
       该方法获取到一个guid的值: 
       (1)来源于前端传递
       (2)前端没值, 就可以直接使用hutool工具包生成一个. guid
     */
    private  String revolveGuid(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        //1: 先从请求头中获取日志guid.
        String guid = request.getHeader(REQUEST_GUID_HEADER);
        //有值:
        if (!StrUtil.hasBlank(guid)){
            return  guid.trim();
        }
        
        return IdUtil.simpleUUID();
    }
}
