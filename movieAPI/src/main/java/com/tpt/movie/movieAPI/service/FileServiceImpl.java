package com.tpt.movie.movieAPI.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException {
        // Get Name Of The File
        String fileName = file.getOriginalFilename();

        // Get the file path to the file
        String filePath = path + File.separator + fileName;

        // Create a file object based on file path
        File newFile = new File(filePath);
        if (!newFile.exists()) {
            newFile.mkdir();
        }

        // Copy the file or upload file to the path
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }

    @Override
    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {
        String filePath = path + File.separator + fileName;
        return new FileInputStream(filePath);
    }
}
