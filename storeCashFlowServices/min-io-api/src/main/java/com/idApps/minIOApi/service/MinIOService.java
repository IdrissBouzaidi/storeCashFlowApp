package com.idApps.minIOApi.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class MinIOService {

    private final MinioClient remoteMinioClient;

    private final String minIOBucketName;

    public MinIOService(
            @Qualifier("remoteMinioClient") MinioClient remoteMinioClient,
            @Value("${min-io.bucket-name}") String minIOBucketName
    ) {
        this.remoteMinioClient = remoteMinioClient;
        this.minIOBucketName = minIOBucketName;
    }

    public String getFilePresignedUrl(String fileName, String bucketName) throws Exception {
        return this.remoteMinioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName == null? this.minIOBucketName: bucketName)
                    .object(fileName)
                    .expiry(60 * 60) // valable 1 heure
                    .build()
        );
    }
}
