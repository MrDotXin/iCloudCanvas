package com.mrdotxin.icloudcanvas.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceCheckInfo {

    Long size;

    Long count;

    Long spaceId;

    Long maxSize;

    Long maxCount;
}
