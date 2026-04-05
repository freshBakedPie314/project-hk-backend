package com.enigma.projecthkbackend.filters;

import com.enigma.projecthkbackend.entities.User;
import com.enigma.projecthkbackend.repos.UserRepository;
import com.enigma.projecthkbackend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = jwtUtil.validateAndExtract(token);

            if(jwtUtil.isRefreshToken(claims))
            {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String email = claims.get("email").toString();
            User user = userRepository.findByEmail(email).orElseThrow();

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user,null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        filterChain.doFilter(request,response);
    }
}
