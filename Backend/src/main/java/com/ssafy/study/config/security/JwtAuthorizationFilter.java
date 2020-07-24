package com.ssafy.study.config.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.ssafy.study.user.model.UserPrincipal;
import com.ssafy.study.user.model.UserToken;
import com.ssafy.study.user.repository.UserRepository;
import com.ssafy.study.util.JwtProperties;
import com.ssafy.study.util.JwtUtil;

public class JwtAuthorizationFilter extends OncePerRequestFilter{
	
	private RedisTemplate<String, Object> redisTemplate;
	private UserRepository userRepository;
	
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, RedisTemplate redisTemplate) {
		super();
		this.userRepository = userRepository;
		this.redisTemplate = redisTemplate;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		logger.info("Authorization Filter");
		String token = request.getHeader(JwtProperties.HEADER_STRING);
		
		if(token == null || !token.startsWith(JwtProperties.TOKEN_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}
		
		Authentication authentication = getUsernamePasswordAuthentication(response, token);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		chain.doFilter(request, response);
	}
	
	private Authentication getUsernamePasswordAuthentication(HttpServletResponse response, String token) {
		if(token == null || !token.startsWith(JwtProperties.TOKEN_PREFIX)) return null;
		
		String userEmail = JwtUtil.getUsernameFromToken(token.replace(JwtProperties.TOKEN_PREFIX, ""));
		if(userEmail == null) return null;
		
		UserPrincipal userPrincipal = getUserPrincipalByUserEmail(userEmail);
		
		try {
			JwtUtil.verify(token);
		} catch (TokenExpiredException accessExpiredException) {
			logger.error("AccessToken Expired");
			UserToken userToken = (UserToken) redisTemplate.opsForValue().get(userEmail);
			
			try {
				JwtUtil.verify(userToken.getRefreshToken());
				
				response.setHeader(
						JwtProperties.HEADER_STRING,
						JwtProperties.TOKEN_PREFIX + JwtUtil.generateAccessToken(userPrincipal));
				logger.info("AccessToken Regenerated");
				
			} catch (TokenExpiredException refreshExpiredException) {
				logger.error("RefreshToken Expired");
				return null;
			} catch (NullPointerException e) {
				logger.error("RefreshToken Expired");
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		logger.info("Authorities : " + userPrincipal.getAuthorities());
		return new UsernamePasswordAuthenticationToken(userEmail, null, userPrincipal.getAuthorities());
	}
	
	private UserPrincipal getUserPrincipalByUserEmail(String userEmail) {
		return new UserPrincipal(this.userRepository.findByUserEmail(userEmail).get());
	}
}
