package com.mrdotxin.icloudcanvas.job;

import cn.hutool.core.util.StrUtil;
import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.exception.BusinessException;
import com.mrdotxin.icloudcanvas.mapper.PictureMapper;
import com.mrdotxin.icloudcanvas.model.dto.picture.inner.PictureDeleteParams;
import com.mrdotxin.icloudcanvas.service.PictureService;
import com.mrdotxin.icloudcanvas.upload.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class doPictureClean {
    @Resource
    private FileManager fileManager;

    @Resource
    private PictureMapper pictureMapper;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void doClean() {
        int batchSize = 50;
        List<PictureDeleteParams> pictureDeleteParams = pictureMapper.selectKPriorDeletedPicture(batchSize);
        if (!pictureDeleteParams.isEmpty()) {
            List<String> deleteKeys = new ArrayList<>();
            List<Long> ids = new ArrayList<>();
            for (PictureDeleteParams pictureDeleteParam : pictureDeleteParams) {
                Long id = pictureDeleteParam.getId();
                String url = pictureDeleteParam.getUrl();
                String rawFormat = pictureDeleteParam.getRawFormat();

                ids.add(id);
                deleteKeys.add(url);
                if (StrUtil.isNotBlank(rawFormat)) {
                    deleteKeys.add(url.substring(0, url.lastIndexOf(".")) + "." + rawFormat);
                }
            }

            try {
                //批量删除图片
                fileManager.removeObjectBatchByUrl(deleteKeys);
                // 批量删除数据库记录
                Integer integer = pictureMapper.deletePicturesByBatch(ids);

                log.info("{} 记录被成功删除了", integer);
            } catch (Exception e) {
                log.error("{} 存在无法删除的图片, 请重试!", e.getMessage());
            }
        }
    }
}
