package com.unicom.post.config.filter;

import com.unicom.post.common.utils.JwtUtils;
import com.unicom.post.modules.auth.domain.UserPrincipal;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            Claims claims = jwtUtils.parseToken(token);
            if (claims != null) {
                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();
                String realName = claims.get("realName", String.class);
                String phone = claims.get("phone", String.class);
                List<String> roles = (List<String>) claims.get("roles");
                Integer status = claims.get("status", Integer.class);

                // 构建 UserPrincipal（密码为 null 或空，因为已认证）
                UserPrincipal userPrincipal = new UserPrincipal(
                        userId,
                        username,
                        null,
                        realName,
                        phone,
                        roles,
                        status != null ? status : 1
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}