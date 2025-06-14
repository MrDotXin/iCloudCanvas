package com.mrdotxin.icloudcanvas.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.exception.BusinessException;
import com.mrdotxin.icloudcanvas.exception.ThrowUtils;
import com.mrdotxin.icloudcanvas.model.dto.file.UploadFileResult;
import com.mrdotxin.icloudcanvas.service.OSService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Slf4j
public abstract class FileUploaderTemplate {

    @Resource
    private OSService osService;

    public final UploadFileResult uploadFile(Object inputSource, String uploadFilePath, Long limitSize) {
        String suffix = validPicture(inputSource);
        String uuid = RandomUtil.randomString(16);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadPath = String.format("%s/%s", uploadFilePath, uploadFilename);
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            transferToTempFile(inputSource, file);

            if (limitSize >= 0) {
                ThrowUtils.throwIf(FileUtil.size(file) > limitSize, ErrorCode.OPERATION_ERROR, "空间超限! 无法保存个人空间");
            }

            return osService.uploadWithInfo(uploadPath, file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            deleteTempFile(file);
        }
    }

    protected abstract String validPicture(Object multipartFile);

    protected abstract void transferToTempFile(Object inputSource, File file) throws IOException;

    public final void deleteTempFile(File file) {
         if (Objects.nonNull(file)) {
             boolean result = file.delete();
             if (!result) {
                 log.error("file delete error, filePath = {}", file.getAbsolutePath());
             }
         }
    }
}


