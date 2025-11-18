package com.idApps.minIOApi.controller;

import com.idApps.minIOApi.model.dto.FileDetailsDto;
import com.idApps.minIOApi.model.dto.RefTableDto;
import com.idApps.minIOApi.model.response.ApiResponse;
import com.idApps.minIOApi.remote.MinIORemote;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/minIoApi")
public class MinIOController {

    @Autowired
    private MinIORemote minIORemote;

    @GetMapping("/{fileName}")
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<byte[]>> downloadFile(@PathVariable String fileName, @RequestParam(required = false) String bucketName) {
        return this.minIORemote.downloadFile(fileName, bucketName);
    }

    @GetMapping(value = "/filesUrls")
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<List<RefTableDto>>> getFilesUrls(@RequestParam List<String> fileNames, @RequestParam(required = false) String bucketName) {
        return this.minIORemote.getFilesUrls(fileNames, bucketName);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam String fileName) {
        System.out.println("MinIO Controller: updating image");
        try {
            this.minIORemote.uploadImage(file, fileName);
            return ResponseEntity.ok(new ApiResponse<>("Image has been added"));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e));
        }
    }

    @SecurityRequirement(name = "keycloak")
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteFile(@RequestParam String fileName) throws Exception {
        try {
            this.minIORemote.deleteFile(fileName, null);
            return ResponseEntity.ok(new ApiResponse<>("File has been deleted successifly"));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e));
        }
    }
}
