package com.meiseguo.api;

import com.meiseguo.api.pojo.Config;
import com.meiseguo.api.pojo.Token;
import com.meiseguo.api.utils.CryptoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ObjectUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 过滤器
 * filter的三种典型应用：
 *1、可以在filter中根据条件决定是否调用chain.doFilter(request, response)方法，即是否让目标资源执行
 *2、在让目标资源执行之前，可以对request\response作预处理，再让目标资源执行
 *3、在目标资源执行之后，可以捕获目标资源的执行结果，从而实现一些特殊的功能
 */
@WebFilter(filterName = "TheFilter",urlPatterns = "/internal/*")
@Configuration
public class InternalFilter implements Filter {
    Logger logger = LogManager.getLogger(this.getClass().getName());

    @Autowired
    private MongoTemplate mongoTemplate;
    private ConcurrentHashMap<String, Config> cache = new ConcurrentHashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("TheFilter start at {}", LocalDateTime.now());
        init("filter.debug");
        init("filter.ignore");
    }

    private Config get(String key) {
        Config config = cache.get(key);
        if(config == null) return null;
        if(config.getCreatetime().plusMinutes(30).isBefore(LocalDateTime.now())) {
            config = init(key);
        }
        return config;
    }

    private Config init(String key) {
        Config one = mongoTemplate.findOne(new Query(Criteria.where("key").is(key)), Config.class);
        if(one == null) {
            logger.error("config not found: {}", key);
            return null;
        }
        one.setCreatetime(LocalDateTime.now());
        cache.put(key, one);
        return one;
    }

    private boolean reject(String uri, HttpServletRequest request, ServletResponse servletResponse) {
        Config one = get("filter.debug");
        if(ObjectUtils.isEmpty(one)) {
            logger.error("debug not config");
            return false;
        }
        if("true".equals(one.getValue())) {
            logger.info("debug is on");
            return false;
        }
        logger.info("debug is off({}), needs to verify this request {}", one.getValue(), uri);
        Config ignore = get("filter.ignore");
        if(!ObjectUtils.isEmpty(ignore)) {
            //check ignore and maybe pass early
            String value = ignore.getValue();
            String[] values = value.split(",");
            for(String v: values) {
                if(uri.contains(v)) {
                    logger.info("ignore this uri {}, {}", uri, v);
                    return false;
                }
            }
        }
        logger.info("header without token, reject {}", uri);
        try {
            servletResponse.setContentType("application/json;charset=utf-8");
            servletResponse.getWriter().print("{\"msg\":\"403 forbid\",\"state\":403}");
            return true;
        } catch (Exception e) {
            logger.error("fail to write 401", e);
            return false;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = ((HttpServletRequest) servletRequest);
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Methods","*");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        String requestURI = request.getRequestURI();
        String token = request.getHeader("token");
        String access = request.getHeader("access");
        logger.debug("{} token {} access {}",requestURI, token, access);
        if(requestURI.startsWith("/internal")) {
            if (ObjectUtils.isEmpty(token)) {
                if (reject(requestURI, request, servletResponse)) {
                    return;
                }
            } else {
                Token one = mongoTemplate.findOne(new Query(Criteria.where("token").is(token)), Token.class);
                if (ObjectUtils.isEmpty(one)) {
                    if (reject(requestURI, request, servletResponse)) {
                        return;
                    }
                } else {
                    //check App status?
                    //check token expire?
                    if (CryptoUtil.expired(one.getExpireAt())) {
                        if (reject(requestURI, request, servletResponse)) {
                            return;
                        }
                    }
                }
            }
        }
        //调用该方法后，表示过滤器经过原来的url请求处理方法
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {
        logger.info("TheFilter destroy at {}", LocalDateTime.now());
    }
}

