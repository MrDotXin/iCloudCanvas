package com.mrdotxin.icloudcanvas.model.dto.space;

import com.mrdotxin.icloudcanvas.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceQueryRequest extends PageRequest implements Serializable {

    private Long spaceId;

    private Long userId;

    private String spaceName;

    private String spaceType;

    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;
}
