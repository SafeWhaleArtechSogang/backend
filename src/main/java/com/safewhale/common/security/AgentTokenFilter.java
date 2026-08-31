package com.safewhale.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safewhale.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code /internal/**} 전용 인증. AI 서버가 붙이는 {@code X-Agent-Token} 을 검증한다.
 *
 * <p>이 경로에는 JWT 가 없다. 브라우저가 부를 경로도 아니라서 CORS 도 열지 않는다
 * (SecurityConfig 의 internalChain 참고).
 *
 * <p>비교는 {@link MessageDigest#isEqual}로 한다. {@code String.equals} 는 첫 불일치
 * 문자에서 즉시 빠져나오므로 응답 시간이 토큰 앞부분의 일치 길이를 흘린다.
 */
@Slf4j
@Component
public class AgentTokenFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Agent-Token";
    private static final List<SimpleGrantedAuthority> AUTHORITIES =
            List.of(new SimpleGrantedAuthority("ROLE_AGENT"));

    private final byte[] expected;
    private final ObjectMapper objectMapper;

    public AgentTokenFilter(@Value("${app.agent.token}") String token, ObjectMapper objectMapper) {
        this.expected = token.getBytes(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String presented = request.getHeader(HEADER);
        if (presented == null || !MessageDigest.isEqual(expected, presented.getBytes(StandardCharsets.UTF_8))) {
            // 토큰 값은 로그에 남기지 않는다. 어느 경로가 막혔는지만 남긴다.
            log.warn("내부 API 토큰 불일치 — {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getWriter(),
                    ApiResponse.fail("INVALID_AGENT_TOKEN", "X-Agent-Token 이 유효하지 않습니다."));
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ai-server", null, AUTHORITIES));
        chain.doFilter(request, response);
    }
}
