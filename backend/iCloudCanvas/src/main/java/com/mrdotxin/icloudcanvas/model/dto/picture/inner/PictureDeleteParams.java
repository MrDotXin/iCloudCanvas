package com.mrdotxin.icloudcanvas.model.dto.picture.inner;

import lombok.Data;

@Data
public class PictureDeleteParams {
    /**
     * id
     */
    private Long id;

    /**
     * 图片url
     */
    private String url;

    /**
     * 缩略图url
     */
    private String thumbnailUrl;
    /**
     * 原图像的类型
     */
    private String rawFormat;
}
