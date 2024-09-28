package com.nguyenminh.microservices.zwallet.service;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service

public class CloudinaryService {
@Autowired
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String upload(MultipartFile file)  {
        try{
            Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
            return data.get("url").toString();
        }catch (IOException io){
            throw new RuntimeException("Image upload fail");
        }
    }
}
