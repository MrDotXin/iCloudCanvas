package com.mrdotxin.icloudcanvas.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class PictureUploadRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * uploadType 上传的方式
     */
    private String uploadType;

    /*
     * 可以手动设置照片名称
     */
    private String picName;

    /**
     * 所在的空间
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}