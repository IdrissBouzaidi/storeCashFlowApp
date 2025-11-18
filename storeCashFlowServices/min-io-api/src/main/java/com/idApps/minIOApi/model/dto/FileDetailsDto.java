package com.idApps.minIOApi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDetailsDto {
    private byte[] file;
    private String fileName;
}
