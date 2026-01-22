package com.nara.aivleTK.controller;

import com.nara.aivleTK.domain.Attachment.Attachment;
import com.nara.aivleTK.dto.ApiResponse;
import com.nara.aivleTK.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/board/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("files")MultipartFile[] files) {
        try {
            List<Attachment> uploadedFiles = attachmentService.uploadFiles(files);

            return ResponseEntity.ok().body(new ApiResponse<>("success", "업로드 성공", uploadedFiles));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("업로드 실패: " + e.getMessage());
        }
    }
}
