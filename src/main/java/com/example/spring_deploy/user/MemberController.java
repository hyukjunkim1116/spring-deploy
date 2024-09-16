package com.example.spring_deploy.user;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final AmazonS3 amazonS3;

    private final MemberRepository memberRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }
    @GetMapping("/db/{username}")
    public String test(@PathVariable String username) {
        Member byUsername = memberRepository.findByUsername(username);
        return byUsername.getUsername();
    }

    @PostMapping("/db/{username}")
    public String db(@PathVariable String username) {
        Member member = new Member(username);
        Member save = memberRepository.save(member);
        return save.getUsername();
    }
    @Transactional
    @PostMapping("/s3/{username}")
    public String s3ImageUploadTest(@RequestBody MultipartFile image,@PathVariable String username) throws IOException {
        Member byUsername = memberRepository.findByUsername(username);
        String imageUrl = saveUserImage(image);
        byUsername.changeImage(imageUrl);
        return imageUrl;
    }
    @GetMapping("/s3/{username}")
    public String s3ImageLoadTest(@PathVariable String username) throws IOException {
        Member byUsername = memberRepository.findByUsername(username);
        return byUsername.getImage();
    }

    private String saveUserImage(MultipartFile multipartFile) throws IOException {
        String uuid = String.valueOf(UUID.randomUUID());
        String originalFilename = multipartFile.getOriginalFilename();
        String fileUrl = originalFilename + uuid;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        amazonS3.putObject(bucket, fileUrl, multipartFile.getInputStream(), metadata);
        return amazonS3.getUrl(bucket, fileUrl).toString();
    }


}
