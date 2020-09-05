package com.ssafy.study.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.study.common.util.FileUtils;

import io.swagger.annotations.ApiOperation;

@RestController
public class CommonController {
	@Autowired
	private FileUtils fileUtil;

	private final String fileBaseUrl = "C:\\Users\\multicampus\\pjt_sub2\\img";

	@GetMapping(value = "/image/{type}/{path}", produces = MediaType.IMAGE_JPEG_VALUE)
	@ApiOperation("이미지 출력 <img src='http://localhost:8399/api/image/(type)/{Img경로}'>")
	public byte[] showThumbnailSinglePath(@PathVariable String type, @PathVariable String path) {
		String filePath = File.separator + type + File.separator + path;
		byte[] img = {};
		try {
			img = fileUtil.downloadFile(fileBaseUrl, filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	@GetMapping(value = "/image/{type}/{year}/{month}/{date}/{path}", produces = MediaType.IMAGE_JPEG_VALUE)
	@ApiOperation("이미지 출력 <img src='http://localhost:8399/api/image/(type)/{Img경로}'>")
	public byte[] showThumbnail(@PathVariable String type, @PathVariable String year, @PathVariable String month,
			@PathVariable String date, @PathVariable String path) {
		String filePath = File.separator + type + File.separator + year + File.separator + month + File.separator + date
				+ File.separator + path;
		byte[] img = {};
		try {
			img = fileUtil.downloadFile(fileBaseUrl, filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

}
