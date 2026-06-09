package com.oexil.studentreg.service;

import com.oexil.studentreg.utils.FileUtilizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

@Service
public class ImageUploadService {

    @Value("${archive.path}")
    private String archivePath;

    @Value("${server.file.prefix}")
    private String filePrefix;

    public String[] getResultsOfFileWrite(MultipartFile imageFile) {
        String urlPrefix = filePrefix + "/";
        String fileName = new FileUtilizer().generateFileName(imageFile.getOriginalFilename());

        if (!new FileUtilizer().writeToDisk(imageFile, Paths.get(archivePath), fileName)) {
            return null;
        } else {
            return new String[]{(archivePath + "/" + fileName), (urlPrefix + fileName), (fileName)};
        }
    }
}
