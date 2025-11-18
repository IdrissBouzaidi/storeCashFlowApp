package com.idApps.minIOApi.help.utilClasses;

import org.springframework.web.multipart.MultipartFile;

public interface FileHelper {

    static String getExtensionFromFile(MultipartFile file) {
        String fileOriginalName = file.getOriginalFilename();
        return fileOriginalName.substring(fileOriginalName.indexOf('.'));
    }
}
