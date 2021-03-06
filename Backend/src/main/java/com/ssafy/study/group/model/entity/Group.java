package com.ssafy.study.group.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.study.common.model.TimeEntity;
import com.ssafy.study.group.model.dto.ModifyGroupDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "gp_tb")
public class Group extends TimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long gpNo; // 스터디 아이디

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gp_cat_no")
	private GroupCategory gpCat;
//	private int gpCatNo;// 타입 아이디

	@Column(length = 128)
	private String gpNm;// 스터디 이름
	private long gpMgrId;// 팀장아이디
	@Column(length = 1000)
	private String gpIntro;// 소개

	@Column(length = 500)
	private String gpTag;// 태그

	private boolean gpPublic;// 공개여부
	@Column(length = 255)
	private String gpImg;// 썸네일

	@Column(columnDefinition = "integer default 5")
	private int gpMaxNum;// 최대인원
	@Column(columnDefinition = "integer default 1")
	private int gpCurNum;// 현재인원

	public void update(ModifyGroupDto group) {
		this.gpCat = new GroupCategory(group.getGpCatNo());

		if (!isEmptyString(group.getGpNm()))
			this.gpNm = group.getGpNm();

		if (!isEmptyString(group.getGpIntro()))
			this.gpIntro = group.getGpIntro();

		this.gpPublic = group.isGpPublic();

		if (group.isUpdateGpImg())
			this.gpImg = group.getGpImgPath();

		this.gpTag = (group.getGpTag() != null ? String.join(",", group.getGpTag()) : null);

	}

	public boolean isEmptyString(String str) {
		if (str == null || str.equals(""))
			return true;
		return false;
	}

	public Group(long gpNo) {
		this.gpNo = gpNo;
	}
}
