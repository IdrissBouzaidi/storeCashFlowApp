package com.idApps.minIOApi.configuration;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
public class BeanConfig {

    private final String remoteMinIOUrl;
    private final String internalMinIOUrl;
    private final String minIOAccessKey;
    private final String minIOSecretKey;


    BeanConfig(
        @Value("${min-io.remote-url}") String remoteMinIOUrl,
        @Value("${min-io.internal-url}") String internalMinIOUrl,
        @Value("${min-io.access-key}") String minIOAccessKey,
        @Value("${min-io.secret-key}") String minIOSecretKey
    ) {
        this.remoteMinIOUrl = remoteMinIOUrl;
        this.internalMinIOUrl = internalMinIOUrl;
        this.minIOAccessKey = minIOAccessKey;
        this.minIOSecretKey = minIOSecretKey;
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "remoteMinioClient")
    public MinioClient remoteMinioClient() {
        System.out.println("remoteMinIOUrl: " + this.remoteMinIOUrl);
        return MinioClient.builder()
                .endpoint(this.remoteMinIOUrl)
                .credentials(this.minIOAccessKey, this.minIOSecretKey)
                .build();
    }

    @Bean(name = "internalMinioClient")
    public MinioClient internalMinioClient() {
        System.out.println("internalMinIOUrl: " + this.internalMinIOUrl);
        return MinioClient.builder()
                .endpoint(this.internalMinIOUrl)
                .credentials(this.minIOAccessKey, this.minIOSecretKey)
                .build();
    }
}
