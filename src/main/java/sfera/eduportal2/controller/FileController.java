package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.response.ResFile;
import sfera.eduportal2.service.FileService;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // ✅ FILE UPLOAD
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("File is empty", HttpStatus.NOT_FOUND, false, null));
        }

        ApiResponse response = fileService.saveFile(file);
        return ResponseEntity.ok(response);
    }

    // ✅ FILE DOWNLOAD
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable long id) {

        ResFile file = fileService.getAttachment(id);

        return ResponseEntity.ok()
                .headers(file.getHeaders())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFillName() + "\"")
                .body(file.getResource());
    }
}