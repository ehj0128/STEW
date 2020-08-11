package com.ssafy.study.controller;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.study.common.exception.FileUploadException;
import com.ssafy.study.common.model.BasicResponse;
import com.ssafy.study.common.util.FileUtils;
import com.ssafy.study.user.model.User;
import com.ssafy.study.user.model.UserDto;
import com.ssafy.study.user.model.UserModify;
import com.ssafy.study.user.model.UserPrincipal;
import com.ssafy.study.user.model.UserSignUp;
import com.ssafy.study.user.service.UserService;
import com.ssafy.study.util.JwtProperties;
import com.ssafy.study.util.JwtUtil;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/user")
@ApiResponses(value = { @ApiResponse(code = 401, message = "Unauthorized", response = BasicResponse.class),
		@ApiResponse(code = 403, message = "Forbidden", response = BasicResponse.class),
		@ApiResponse(code = 404, message = "Not Found", response = BasicResponse.class),
		@ApiResponse(code = 500, message = "Failure", response = BasicResponse.class) })
public class UserController {
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private UserService userService;
	@Autowired
	private FileUtils fileUtil;
	
	// private final String fileBaseUrl = "/home/ubuntu/app/img/user";
	private final String fileBaseUrl = "C:\\Users\\multicampus\\Desktop\\img\\user";
	
	@PostMapping("/signup")
	@ApiOperation("회원가입")
	public ResponseEntity<BasicResponse> signUp(UserSignUp signUpInfo){
		
		User user = signUpInfo.toEntity();
		
		user.setUserPw(new BCryptPasswordEncoder().encode(user.getUserPw()));
		
		if(signUpInfo.getUserImg() != null) {
			try {
				user.setUserImg(fileUtil.uploadFile(signUpInfo.getUserImg(), fileBaseUrl));
			} catch (IOException e) {
				e.printStackTrace();
				throw new FileUploadException();
			}
		}
		userService.save(user);
		
		BasicResponse result = new BasicResponse();
		
		result.status = true;
		result.msg = "success";
		result.object = user;
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
//	비밀번호 맞는지 아닌지
//	유저 업데이트할때 비밀번호 포함 안하고싶다
	
	@PostMapping("/checkpw")
	@ApiOperation("비밀번호 확인")
	public ResponseEntity<BasicResponse> checkPw(@AuthenticationPrincipal UserPrincipal principal, String userPw){
		
		BasicResponse result = new BasicResponse();
		
		result.status = true;
		result.msg = "success";
		result.object = false;
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		if(encoder.matches(userPw, principal.getPassword())) {
			result.object = true;
		}
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	
	@GetMapping
	@ApiOperation("회원 정보")
	public ResponseEntity<BasicResponse> userDetail(@AuthenticationPrincipal UserPrincipal userPrincipal){
		
		User user = userService.loadUserByUserId(userPrincipal.getUserId());
		
		BasicResponse result = new BasicResponse();
		
		result.status = true;
		result.msg = "success";
		result.object = user;
		
		return new ResponseEntity<>(result, HttpStatus.OK);
		
	}
	
	@DeleteMapping("/{userId}")
	@ApiOperation("회원 탈퇴")
	public ResponseEntity<BasicResponse> signOut(@PathVariable long userId, HttpServletRequest request) throws ServletException{
		
		userService.deleteById(userId);
		
		BasicResponse result = new BasicResponse();
		
		String accessToken = request.getHeader(JwtProperties.HEADER_STRING).replace(JwtProperties.TOKEN_PREFIX, "");
		
		long remains = JwtUtil.getExpiringTime(accessToken) - System.currentTimeMillis();
		
//		BlackListing
		redisTemplate.opsForValue().set(accessToken, "logout");
		redisTemplate.expire(accessToken, remains, TimeUnit.MILLISECONDS);
		
//		Delete RefreshToken
		redisTemplate.delete(JwtUtil.getUsernameFromToken(accessToken));
		
		result.status = true;
		result.msg = "success";
		result.object = true;
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@PutMapping
	@ApiOperation("회원 수정")
	public ResponseEntity<BasicResponse> modify(UserModify userModify, @AuthenticationPrincipal UserPrincipal principal){
		
		BasicResponse result = new BasicResponse();
		
		User origin = userService.loadUserByUserId(principal.getUserId());
		
		origin.update(userModify);
		
		if(userModify.isUpdateImg()) {
			if(userModify.getUserImg() != null) {
				try {
					origin.setUserImg(fileUtil.uploadFile(userModify.getUserImg(), fileBaseUrl));
				} catch (IOException e) {
					e.printStackTrace();
					throw new FileUploadException();
				}
			}else {
				origin.setUserImg("여기에 기본 이미지");
			}
		}
		
		User modifiedUser = userService.save(origin);
		
		result.status = true;
		result.msg = "success";
		result.object = modifiedUser;
		
		return new ResponseEntity<>(result, HttpStatus.OK);
		
	}
	
	@GetMapping("/check")
	@ApiOperation("중복 확인")
	public ResponseEntity<BasicResponse> checkDuplicated(String userEmail){
		
		BasicResponse result = new BasicResponse();
		
		result.status = true;
		result.msg = "success";
		result.object = userService.findByUserEmailAndType(userEmail, "stew") == null ? true : false;
		
		return new ResponseEntity<>(result, HttpStatus.OK);
		
	}
	
	@GetMapping("/refresh")
	@ApiOperation("토큰 갱신")
	public ResponseEntity<BasicResponse> refreshToken(HttpServletRequest request, HttpServletResponse response){
		
		BasicResponse result = new BasicResponse();
		
		String refreshToken = request.getHeader("refreshToken").replace(JwtProperties.TOKEN_PREFIX, "");
		
		if(refreshToken == null || !JwtUtil.verify(refreshToken))
			result.msg = "fail";

		String accessToken = request.getHeader(JwtProperties.HEADER_STRING).replace(JwtProperties.TOKEN_PREFIX, "");
		
		if(accessToken == null) {
			result.msg = "accessToken not found";
		}
		
		UserDto user = JwtUtil.getUserFromToken(accessToken);
		
		if(refreshToken.equals(redisTemplate.opsForValue().get(user.getUserEmail() + "#" + user.getType()))){
			UserPrincipal userPrincipal = new UserPrincipal(userService.loadUserByUserId(user.getUserId()));
			
			accessToken = JwtProperties.TOKEN_PREFIX + JwtUtil.generateAccessToken(userPrincipal);
			
			response.addHeader("accessToken", accessToken);
			
			result.status = true;
			result.msg = "success";
		}else result.msg = "Invalid RefreshToken";
		
		return new ResponseEntity<>(result, result.status ? HttpStatus.OK : HttpStatus.FORBIDDEN);
	}
	
}
