package com.ssafy.study.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

	String uploadFile(MultipartFile file, String basePath);

}
