package com.example.demo.service;

import com.example.demo.constant.MessageConstant;
import com.example.demo.dto.ImageRequest;
import com.example.demo.entity.Image;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.ImageRepository;
import com.example.demo.repository.UserAccountRepository;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserAccountRepository userAccountRepository;
    public static final String DIRECTORY = System.getProperty("user.home") + "/uploads/";

    private String uploadFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        Path fileStorage = get(DIRECTORY, fileName).toAbsolutePath().normalize();
        copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
        return fileStorage.toString();
    }

    public void createImage(ImageRequest imageRequest) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserAccount> user = userAccountRepository.findByEmail(authentication.getName());

        Assert.isTrue(user.isPresent(), MessageConstant.INVALID_TOKEN_CREDENTIALS);

        Image image = Image.builder()
                .title(imageRequest.getTitle())
                .description(imageRequest.getDescription())
                .image_path(uploadFile(imageRequest.getImage()))
                .user_id(user.get().getId())
                .sum_likes(0L)
                .date_time(new Timestamp(new Date().getTime()))
                .build();

        imageRepository.save(image);
    }

    public void deleteImage(Long imageId) {
        Image image = imageRepository.getReferenceById(imageId);
        imageRepository.delete(image);
    }

    public Resource getImagePath(String name) throws MalformedURLException {
        Path imagePath = get(DIRECTORY).toAbsolutePath().normalize().resolve(name);
        return new UrlResource(imagePath.toUri());
    }
}