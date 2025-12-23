package com.yyds.hrcscommon.utils;

import cn.hutool.core.io.FileUtil;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yyds.hrcscommon.exception.BusinessException;
import com.yyds.hrcscommon.result.UploadResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "aliyun.oss")  // 直接在类上配置
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private Long maxFileSize; // 最大文件大小
    private String cdnDomain; // CDN域名（可选）

    /**
     * 上传文件（完整版）
     * @param file MultipartFile文件
     * @param dir 存储目录（如：notice/）
     * @return 上传结果对象
     */
    public UploadResult uploadFile(MultipartFile file, String dir) throws BusinessException {
        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小不能超过" + (maxFileSize / 1024 / 1024) + "MB");
        }

        // 2. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String ext = FileUtil.getSuffix(originalFilename).toLowerCase();
        String objectName = dir + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8) + "." + ext;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 3. 上传文件
        try  {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            ossClient.putObject(bucketName, objectName, file.getInputStream(), metadata);

            String url = generateUrl(objectName);
            log.info("文件上传成功: {}, 大小: {}KB", objectName, file.getSize() / 1024);

            return UploadResult.builder()
                    .url(url)
                    .objectName(objectName)
                    .fileSize(file.getSize())
                    .originalFilename(originalFilename)
                    .build();

        } catch (OSSException e) {
            log.error("OSS上传失败 - ErrorCode:{}, Message:{}", e.getErrorCode(), e.getErrorMessage());
            throw new BusinessException("文件上传失败：" + e.getErrorMessage());
        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new BusinessException("上传异常：" + e.getMessage());
        }
    }

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
     */
    public void delete(String objectName) throws BusinessException {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try  {
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            throw new BusinessException("文件删除失败");
        }
    }

    /**
     * 生成访问URL（支持CDN）
     */
    private String generateUrl(String objectName) {
        String baseUrl = StringUtils.isNotBlank(cdnDomain) ? cdnDomain : bucketName + "." + endpoint;
        return "https://" + baseUrl + "/" + objectName;
    }

    
}