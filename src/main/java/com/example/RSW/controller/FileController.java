package com.example.RSW.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

@Controller
public class FileController {

    @GetMapping("/gen/file/download")
    public void downloadFile(@RequestParam("path") String path, HttpServletResponse response) throws IOException {
        String basePath = "C:/upload/"; // 파일이 저장된 실제 경로
        String fullPath = basePath + path;

        File file = new File(fullPath);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // ContentType 자동 설정
        String contentType = Files.probeContentType(file.toPath());
        response.setContentType(contentType != null ? contentType : "application/octet-stream");

        // 파일명 인코딩 (다운로드 시 한글 깨짐 방지)
        String encodedName = URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20");

        response.setHeader("Content-Disposition", "inline; filename=\"" + encodedName + "\"");
        response.setContentLengthLong(file.length());

        FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());
    }
}
