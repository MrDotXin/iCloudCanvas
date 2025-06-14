package com.mrdotxin.icloudcanvas.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceTypeModifyRequest implements Serializable {
    private Long spaceId;

    private Integer spaceType;

}
