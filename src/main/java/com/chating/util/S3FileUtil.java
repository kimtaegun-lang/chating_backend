package com.chating.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3FileUtil {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 파일 업로드 메서드
    public String upload(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 저장 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String saveFileName = "chat/" + timestamp + "_" + uuid + ext;

        // Content Type 자동 추출
        String contentType = URLConnection.guessContentTypeFromName(originalFilename);
        
        // 한글 파일명 인코딩
        String encodedOriginalName = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                                               .replaceAll("\\+", "%20");

        S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(saveFileName)
                .contentType(contentType != null ? contentType : "application/octet-stream")
                .contentDisposition("attachment; filename*=UTF-8''" + encodedOriginalName)
                .contentDisposition("inline; filename*=UTF-8''" + encodedOriginalName)  // attachment → inline
                .build();

        s3.putObject(request, RequestBody.fromBytes(file.getBytes()));

        // 업로드 된 파일의 URL 반환
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, saveFileName);
    }
    
    	public void delete(String fileUrl) {
    	    try {
    	        URI uri = URI.create(fileUrl);   // URL 파싱
    	        String key = uri.getPath().substring(1);  // "/chat/xxx.png" → "chat/xxx.png"

    	        S3Client s3 = S3Client.builder()
    	                .region(Region.of(region))
    	                .credentialsProvider(
    	                        StaticCredentialsProvider.create(
    	                                AwsBasicCredentials.create(accessKey, secretKey)))
    	                .build();

    	        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
    	                .bucket(bucket)
    	                .key(key)
    	                .build();

    	        s3.deleteObject(deleteObjectRequest);
    	        System.out.println("S3 파일 삭제 완료: " + key);

    	    } catch (Exception e) {
    	        System.out.println("S3 파일 삭제 실패: " + e.getMessage());
    	    }
    	}
}