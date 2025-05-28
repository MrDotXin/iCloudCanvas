package com.mrdotxin.icloudcanvas.upload;

import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.exception.BusinessException;
import com.mrdotxin.icloudcanvas.model.dto.file.UploadFileResult;
import com.mrdotxin.icloudcanvas.model.enums.FileUploadTypeEnum;
import com.mrdotxin.icloudcanvas.service.OSService;
import com.mrdotxin.icloudcanvas.upload.impl.MultipartFileUploader;
import com.mrdotxin.icloudcanvas.upload.impl.UrlFileUploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


// 对upload功能再抽象, 最后合成一个总的服务类
@Slf4j
@Service
public class FileManager {

    @Resource
    private OSService osService;

    @Resource
    private UrlFileUploader urlFileUploader;

    @Resource
    private MultipartFileUploader multipartFileUploader;

    public UploadFileResult uploadFile(String policy, Object inputSource, String uploadFilePath) {
        if (policy.equals(FileUploadTypeEnum.MULTIPART_FILE.getValue())) {
            return multipartFileUploader.uploadFile(inputSource, uploadFilePath);
        }

        if (policy.equals(FileUploadTypeEnum.URL.getValue())) {
            return urlFileUploader.uploadFile(inputSource, uploadFilePath);
        }

        throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误的上传类型!");
    }

    public final void removeObjectByUrlIfExists(String url) {
        try {
            osService.removeObjectByUrl(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
