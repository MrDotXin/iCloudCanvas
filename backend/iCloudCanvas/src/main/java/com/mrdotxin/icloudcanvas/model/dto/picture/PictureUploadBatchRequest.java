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
public class PictureUploadBatchRequest implements Serializable {

    /**
     * searchText 搜索的内容
     */
    private String searchText;

    /**
     * 统一命名前缀
     */
    private String picPrefix;

    /**
     * count 需要一次抓取的数量
     */
    private Integer count;

    private static final long serialVersionUID = 1L;
}