package com.idApps.minIOApi.remote;

import com.idApps.minIOApi.help.utilClasses.FileHelper;
import com.idApps.minIOApi.model.dto.RefTableDto;
import com.idApps.minIOApi.model.response.ApiResponse;
import com.idApps.minIOApi.service.MinIOService;
import com.idApps.minIOApi.service.ThumbnailatorService;
import io.minio.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Data
@Component
public class MinIORemoteImpl implements MinIORemote {

    private final MinioClient internalMinioClient;
    private final MinIOService minIOService;

    private final ThumbnailatorService thumbnailatorService;

    private final String minIOAccessKey;
    private final String minIOSecretKey;
    private final String minIOBucketName;
    private final String minIOThumbnailBucketName;

    public MinIORemoteImpl(
            @Qualifier("internalMinioClient") MinioClient internalMinioClient,
            MinIOService minIOService,
            ThumbnailatorService thumbnailatorService,
            @Value("${min-io.access-key}") String minIOAccessKey,
            @Value("${min-io.secret-key}") String minIOSecretKey,
            @Value("${min-io.bucket-name}") String minIOBucketName,
            @Value("${min-io.thumbnail-bucket-name}") String minIOThumbnailBucketName
    ) {
        this.internalMinioClient = internalMinioClient;
        this.minIOService = minIOService;
        this.thumbnailatorService = thumbnailatorService;
        this.minIOAccessKey = minIOAccessKey;
        this.minIOSecretKey = minIOSecretKey;
        this.minIOBucketName = minIOBucketName;
        this.minIOThumbnailBucketName = minIOThumbnailBucketName;
    }

    @Override
    public ResponseEntity<ApiResponse<byte[]>> downloadFile(String fileName, String bucketName) {
        try {
            InputStream is = this.internalMinioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName == null? this.minIOBucketName: bucketName)
                            .object(fileName)
                            .build());
            byte[] fileBytes = is.readAllBytes(); // lire tout le contenu

            // Définir les headers HTTP pour forcer le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(fileBytes.length);
            headers.setContentDispositionFormData("attachment", fileName);
            return new ResponseEntity<>(new ApiResponse<>(fileBytes), headers, HttpStatus.OK);
        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<RefTableDto>>> getFilesUrls(List<String> fileNames, String bucketName) {
        try {
            String finalBucketName = bucketName == null? this.minIOBucketName: bucketName;
            List<RefTableDto> fileUrls = fileNames.stream().map(fileName -> {
                try {
                    String filePresignedUrl = this.minIOService.getFilePresignedUrl(fileName, finalBucketName);
                    return RefTableDto.builder()
                            .code(fileName)
                            .label(filePresignedUrl)
                            .build();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).toList();

            return ResponseEntity.ok().body(new ApiResponse<>(fileUrls));
        }
        catch(Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse(e));
        }
    }

    @Override
    public void uploadImage(MultipartFile file, String fileName) throws Exception {

        byte[] compressedFileBytes = this.thumbnailatorService.createThumbnail(file.getInputStream());

        this.internalMinioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(this.minIOBucketName)
                        .object(fileName) // nom de l’objet
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        try {

            this.internalMinioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(this.minIOThumbnailBucketName)
                            .object(fileName)
                            .stream(new ByteArrayInputStream(compressedFileBytes), compressedFileBytes.length, -1)
                            .contentType("image/jpeg")
                            .build()
            );
        }
        catch(Exception e) {
            this.deleteFile(fileName, this.minIOBucketName);
            throw e;
        }
    }

    @Override
    public void deleteFile(String fileName, String bucketName) throws Exception {
        this.internalMinioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName == null? this.minIOBucketName: bucketName)
                        .object(fileName)
                        .build()
        );
    }
}
