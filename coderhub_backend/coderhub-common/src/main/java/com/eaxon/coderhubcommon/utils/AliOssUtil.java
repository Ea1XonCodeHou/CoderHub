package com.eaxon.coderhubcommon.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }

    /**
     * 删除文件
     *
     * @param objectName 文件名（如：article/2024/10/21/xxx.md）
     */
    public void delete(String objectName) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 删除文件
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件删除成功：{}", objectName);
        } catch (OSSException oe) {
            log.error("OSS删除文件失败：{}", oe.getErrorMessage());
            throw new RuntimeException("文件删除失败：" + oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("OSS客户端异常：{}", ce.getMessage());
            throw new RuntimeException("文件删除失败：" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 下载文件并转为字符串
     *
     * @param objectName 文件名（如：article/2024/10/21/xxx.md）
     * @return 文件内容字符串
     */
    public String downloadAsString(String objectName) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 获取文件对象
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            
            // 读取内容
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ossObject.getObjectContent(), StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                log.info("文件下载成功：{}，长度：{}", objectName, content.length());
                return content;
            }
        } catch (OSSException oe) {
            log.error("OSS下载文件失败：{}", oe.getErrorMessage());
            throw new RuntimeException("文件下载失败：" + oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("OSS客户端异常：{}", ce.getMessage());
            throw new RuntimeException("文件下载失败：" + ce.getMessage());
        } catch (Exception e) {
            log.error("读取文件内容异常：{}", e.getMessage());
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
