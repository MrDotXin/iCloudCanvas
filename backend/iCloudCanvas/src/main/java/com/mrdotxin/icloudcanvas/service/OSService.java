package com.mrdotxin.icloudcanvas.service;

import com.mrdotxin.icloudcanvas.model.dto.file.UploadFileResult;

import java.io.File;

public interface OSService {

    String upload(String fileKey, File file);

    UploadFileResult uploadWithInfo(String fileKey, File file);

    byte[] getObjectContentByKey(String filePath);

    void removeObjectByUrl(String url);
}
