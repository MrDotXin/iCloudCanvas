package com.mrdotxin.icloudcanvas.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.config.CosConfig;
import com.mrdotxin.icloudcanvas.exception.BusinessException;
import com.mrdotxin.icloudcanvas.model.dto.file.UploadFileResult;
import com.mrdotxin.icloudcanvas.service.OSService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.transfer.Upload;
import com.qcloud.cos.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Service
public class CosClientService implements OSService {

    @Resource
    private CosConfig cosConfig;

    @Resource
    private COSClient cosClient;
    @Autowired
    private ConfigurationPropertiesAutoConfiguration configurationPropertiesAutoConfiguration;

    @Override
    public String upload(String fileKey, File file) {
        try {
            cosClient.putObject(cosConfig.getBucketName(), fileKey, file);

            return cosConfig.getHost() + fileKey;

        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public UploadFileResult uploadWithInfo(String fileKey, File file) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosConfig.getBucketName(),
                    fileKey,
                    file
            );

            PicOperations picOperations = new PicOperations();
            picOperations.setIsPicInfo(1);

            putObjectRequest.setPicOperations(picOperations);

            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            UploadFileResult uploadFileResult = new UploadFileResult();
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadFileResult.setPicWidth(picWidth);
            uploadFileResult.setPicHeight(picHeight);
            uploadFileResult.setPicScale(picScale);
            uploadFileResult.setPicFormat(imageInfo.getFormat());
            uploadFileResult.setPicSize(FileUtil.size(file));
            uploadFileResult.setUrl(cosConfig.getHost() + fileKey);

            return uploadFileResult;
        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public byte[] getObjectContentByKey(String filePath) {
        try {
            COSObject cosObject = cosClient.getObject(cosConfig.getBucketName(), filePath);
            COSObjectInputStream objectContent = cosObject.getObjectContent();

            return IOUtils.toByteArray(objectContent);
        } catch (CosClientException | IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void removeObjectByUrl(String url) {
        try {
            String key = url.substring(cosConfig.getHost().length());
            cosClient.deleteObject(cosConfig.getBucketName(), key);
        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }
}
