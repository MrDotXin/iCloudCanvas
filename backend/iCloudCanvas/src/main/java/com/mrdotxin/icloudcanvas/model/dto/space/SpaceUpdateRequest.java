package com.mrdotxin.icloudcanvas.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUpdateRequest implements Serializable {

    /**
     * 如果传入了id就说明是修改，否则是添加
     */
    private Long spaceId;

    private String spaceName;

    private String spaceType;

    private Integer spaceLevel;

    private Long maxSize;

    private Long maxCount;

    private static final long serialVersionUID = 1L;
}
