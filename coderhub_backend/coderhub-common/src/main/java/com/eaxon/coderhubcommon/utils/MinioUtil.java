package com.eaxon.coderhubcommon.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import io.minio.*;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO文件上传工具类
 * 用于大文件（视频、大型文档）的存储
 */
@Data
@AllArgsConstructor
@Slf4j
public class MinioUtil {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    /**
     * 获取MinIO客户端
     */
    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 上传文件（字节数组）
     *
     * @param bytes       文件字节数组
     * @param objectName  对象名称（如：video/2024/11/07/uuid.mp4）
     * @param contentType 文件类型（如：video/mp4）
     * @return 文件访问URL
     */
    public String upload(byte[] bytes, String objectName, String contentType) {
        MinioClient minioClient = getMinioClient();

        try {
            // 检查bucket是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                // 创建bucket
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("创建bucket成功：{}", bucketName);
            }

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType(contentType)
                            .build()
            );

            // 构建访问URL
            String url = endpoint + "/" + bucketName + "/" + objectName;
            log.info("文件上传成功：{}", url);
            return url;

        } catch (Exception e) {
            log.error("MinIO上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件（InputStream）
     *
     * @param inputStream 输入流
     * @param objectName  对象名称
     * @param contentType 文件类型
     * @param size        文件大小
     * @return 文件访问URL
     */
    public String upload(InputStream inputStream, String objectName, String contentType, long size) {
        MinioClient minioClient = getMinioClient();

        try {
            // 检查bucket是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                // 创建bucket
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("创建bucket成功：{}", bucketName);
            }

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            // 构建访问URL
            String url = endpoint + "/" + bucketName + "/" + objectName;
            log.info("文件上传成功：{}", url);
            return url;

        } catch (Exception e) {
            log.error("MinIO上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void delete(String objectName) {
        MinioClient minioClient = getMinioClient();

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功：{}", objectName);
        } catch (Exception e) {
            log.error("MinIO删除失败", e);
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取预签名URL（临时访问URL，用于私有文件）
     *
     * @param objectName 对象名称
     * @param expires    过期时间（秒）
     * @return 预签名URL
     */
    public String getPresignedUrl(String objectName, int expires) {
        MinioClient minioClient = getMinioClient();

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expires)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预签名URL失败", e);
            throw new RuntimeException("获取预签名URL失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件到指定bucket（InputStream）
     *
     * @param inputStream 输入流
     * @param objectName  对象名称
     * @param contentType 文件类型
     * @param size        文件大小
     * @param targetBucket 目标bucket名称
     * @return 文件访问URL
     */
    public String uploadToBucket(InputStream inputStream, String objectName, String contentType, long size, String targetBucket) {
        MinioClient minioClient = getMinioClient();

        try {
            // 检查bucket是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(targetBucket).build()
            );

            if (!exists) {
                // 创建bucket
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(targetBucket).build()
                );
                log.info("创建bucket成功：{}", targetBucket);
                
                // 设置bucket为公开读取
                String policy = "{\n" +
                        "    \"Version\": \"2012-10-17\",\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": \"*\",\n" +
                        "            \"Action\": [\"s3:GetObject\"],\n" +
                        "            \"Resource\": [\"arn:aws:s3:::" + targetBucket + "/*\"]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(targetBucket)
                                .config(policy)
                                .build()
                );
                log.info("设置bucket {} 公开读取策略成功", targetBucket);
            }

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            // 构建访问URL
            String url = endpoint + "/" + targetBucket + "/" + objectName;
            log.info("文件上传成功：{}", url);
            return url;

        } catch (Exception e) {
            log.error("MinIO上传到{}失败", targetBucket, e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }
}
