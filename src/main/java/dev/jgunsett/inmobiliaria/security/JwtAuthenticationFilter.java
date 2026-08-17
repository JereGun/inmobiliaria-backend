package dev.jgunsett.inmobiliaria.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String JWT_FAILURE_ATTRIBUTE = "jwtAuthenticationFailure";

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");
		boolean protectedRequest = request.getRequestURI().startsWith("/api/")
				&& !request.getRequestURI().startsWith("/api/v1/auth/");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			if (protectedRequest) {
				log.warn("JWT ausente o con formato inválido: {} {}", request.getMethod(), request.getRequestURI());
				request.setAttribute(JWT_FAILURE_ATTRIBUTE, "No se recibió un token Bearer válido");
			}
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);

		try {
			String username = jwtService.extractUsername(token);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtService.isTokenValid(token, userDetails)) {
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities()
					);
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
					log.info("JWT validado para {}: {} {}", username, request.getMethod(), request.getRequestURI());
				} else {
					log.warn("JWT rechazado para {}: {} {}", username, request.getMethod(), request.getRequestURI());
					request.setAttribute(JWT_FAILURE_ATTRIBUTE, "El token no coincide con el usuario autenticado");
				}
			}
		} catch (JwtException | IllegalArgumentException ex) {
			SecurityContextHolder.clearContext();
			request.setAttribute(JWT_FAILURE_ATTRIBUTE, "JWT inválido o vencido (" + ex.getClass().getSimpleName() + ")");
			log.warn("JWT rechazado en {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getClass().getSimpleName());
		}

		filterChain.doFilter(request, response);
	}
}
