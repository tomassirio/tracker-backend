package com.tomassirio.wanderer.commons.config;

import com.tomassirio.wanderer.commons.security.CurrentUserIdArgumentResolver;
import com.tomassirio.wanderer.commons.security.JwtUtils;
import com.tomassirio.wanderer.commons.security.ScopeInterceptor;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityWebConfig implements WebMvcConfigurer, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        JwtUtils jwtUtils = applicationContext.getBean(JwtUtils.class);
        resolvers.add(new CurrentUserIdArgumentResolver(jwtUtils));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        JwtUtils jwtUtils = applicationContext.getBean(JwtUtils.class);
        registry.addInterceptor(new ScopeInterceptor(jwtUtils));
    }
}
