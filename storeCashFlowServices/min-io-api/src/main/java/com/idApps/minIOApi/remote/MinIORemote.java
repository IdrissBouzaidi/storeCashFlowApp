package com.idApps.minIOApi.remote;

import com.idApps.minIOApi.model.dto.FileDetailsDto;
import com.idApps.minIOApi.model.dto.RefTableDto;
import com.idApps.minIOApi.model.response.ApiResponse;
import io.minio.errors.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface MinIORemote {
    ResponseEntity<ApiResponse<byte[]>> downloadFile(String fileName, String bucketName);

    void uploadImage(MultipartFile file, String fileName) throws Exception;

    void deleteFile(String fileName, String bucketName) throws Exception;

    ResponseEntity<ApiResponse<List<RefTableDto>>> getFilesUrls(List<String> fileNames, String bucketName);
}
