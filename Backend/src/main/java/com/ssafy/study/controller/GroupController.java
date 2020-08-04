package com.ssafy.study.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.study.common.exception.FileUploadException;
import com.ssafy.study.common.model.BasicResponse;
import com.ssafy.study.common.util.FileUtils;
import com.ssafy.study.group.model.dto.GroupDto;
import com.ssafy.study.group.model.dto.ModifyGroupDto;
import com.ssafy.study.group.model.dto.RegistGroupDto;
import com.ssafy.study.group.model.entity.Group;
import com.ssafy.study.group.model.exception.GroupFullException;
import com.ssafy.study.group.model.exception.GroupNotExistException;
import com.ssafy.study.group.model.exception.GroupNotJoinedExcpetion;
import com.ssafy.study.group.model.exception.GroupUnAuthException;
import com.ssafy.study.group.service.GroupService;
import com.ssafy.study.user.model.UserPrincipal;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/study/user")
public class GroupController {

	@Autowired
	private GroupService groupService;
	@Autowired
	private FileUtils fileUtil;

	private final String fileBaseUrl = "C:\\Users\\multicampus\\Desktop\\group_thumb";

	@GetMapping("/my")
	@ApiOperation("로그인한 회원의 스터디 목록 조회")
	public ResponseEntity findMyStudyList(@AuthenticationPrincipal UserPrincipal principal) {
		long userId = principal.getUserId();
		System.out.println(userId);
		BasicResponse result = new BasicResponse();
		result.object = groupService.findMyGroups(userId);
		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/")
	@ApiOperation(value = "스터디 생성", produces = "multipart/form-data")
	public ResponseEntity createStudy(RegistGroupDto group, @AuthenticationPrincipal UserPrincipal principal) {
		Group saveGroup = group.toEntity();
		saveGroup.setGpMgrId(principal.getUserId());

		if (group.getGpImg() != null) {
			try {
				saveGroup.setGpImg(fileUtil.uploadFile(group.getGpImg(), fileBaseUrl));
			} catch (IOException e) {
				e.printStackTrace();
				throw new FileUploadException();
			}
		}

		GroupDto responseGroup = groupService.saveGroup(saveGroup);

		groupService.joinGroup(principal.getUserId(), responseGroup.getGpNo());

		BasicResponse result = new BasicResponse();
		result.object = responseGroup;
		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/{no}")
	@ApiOperation("스터디 상세 조회")
	public ResponseEntity selectStudyNo(@PathVariable long no, @AuthenticationPrincipal UserPrincipal principal) {
		BasicResponse result = new BasicResponse();
		if (!groupService.ckGroupExist(no))
			throw new GroupNotExistException();

		if (!groupService.ckGroupJoin(no, principal.getUserId()))
			throw new GroupNotJoinedExcpetion();

		GroupDto group = groupService.selectGroup(no);

		result.object = group;
		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/{no}")
	@ApiOperation(value = "스터디 수정", produces = "multipart/form-data")
	public ResponseEntity modifytudy(@PathVariable long no, ModifyGroupDto modifyGroup,
			@AuthenticationPrincipal UserPrincipal principal) {
		BasicResponse result = new BasicResponse();

		long userId = principal.getUserId();
		ckGroupAuth(userId, no);

		modifyGroup.setGpNo(no);

		if (modifyGroup.isUpdateGpImg() && modifyGroup.getGpImg() != null) {
			try {
				modifyGroup.setGpImgPath(fileUtil.uploadFile(modifyGroup.getGpImg(), fileBaseUrl));
			} catch (IOException e) {
				e.printStackTrace();
				throw new FileUploadException();
			}
		}

		GroupDto group = groupService.updateGroup(modifyGroup);

		result.object = group;
		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/req")
	@ApiOperation("스터디에 가입요청")
	public ResponseEntity reqJoinGroup(int gpNo, @AuthenticationPrincipal UserPrincipal principal) {
		long userId = principal.getUserId();
		BasicResponse result = new BasicResponse();

		if (groupService.isGroupFull(gpNo))
			throw new GroupFullException();

		if (groupService.ckGroupJoin(gpNo, userId)) {
			result.msg = "duplicate";
			result.status = false;

			return new ResponseEntity<>(result, HttpStatus.CONFLICT);
		}
		groupService.requestJoinGroup(userId, gpNo);

		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/accept")
	@ApiOperation("스터디 가입 승인")
	public ResponseEntity acceptJoinGroup(long reqNo, long gpNo, @AuthenticationPrincipal UserPrincipal principal) {
		long userId = principal.getUserId();

		ckGroupAuth(userId, gpNo);

		BasicResponse result = new BasicResponse();
		if (groupService.isGroupFull(gpNo))
			throw new GroupFullException();

		groupService.acceptJoinGroup(reqNo);

		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/reject")
	@ApiOperation("스터디 가입 거절")
	public ResponseEntity rejectJoinGroup(long reqNo, long gpNo, @AuthenticationPrincipal UserPrincipal principal) {
		long userId = principal.getUserId();

		ckGroupAuth(userId, gpNo);
		groupService.rejectJoinGroup(reqNo);

		BasicResponse result = new BasicResponse();
		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/remove")
	@ApiOperation("스터디 퇴출")
	public ResponseEntity removeGroupMember(long joinNo, long gpNo, @AuthenticationPrincipal UserPrincipal principal) {
		long userId = principal.getUserId();

		ckGroupAuth(userId, gpNo);
		groupService.removeGroupMember(joinNo);

		BasicResponse result = new BasicResponse();
		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/exit")
	@ApiOperation("그룹 나가기")
	public ResponseEntity exitGroup(long gpNo, @AuthenticationPrincipal UserPrincipal principal) {
		long userId = principal.getUserId();
		BasicResponse result = new BasicResponse();

		if (!groupService.ckGroupJoin(gpNo, userId))
			throw new GroupNotJoinedExcpetion();

		if (isGroupMgr(userId, gpNo)) {
			if (groupService.selectGroup(gpNo).getGpCurNum() <= 1) {
				groupService.deleteGroup(gpNo);
			} else {
				result.msg = "매니저 탈퇴 불가";
				result.status = false;

				return new ResponseEntity<>(result, HttpStatus.OK);
			}
		}

		groupService.exitGroup(gpNo, userId);
		result.msg = "success";
		result.status = true;

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// Group Function******************************************

	public void ckGroupAuth(long userId, long gpNo) {
		if (!isGroupMgr(userId, gpNo))
			throw new GroupUnAuthException();
	}

	public boolean isGroupMgr(long userId, long gpNo) {
		long mgrId = groupService.selectGroup(gpNo).getGpMgrId();

		return userId == mgrId ? true : false;
	}

}
