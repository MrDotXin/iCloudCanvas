package com.mrdotxin.icloudcanvas.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceLevelInfo {

    private String levelName;

    private String levelDescription;

    private Integer value;

    private Long levelMaxCount;

    private Long levelMaxSize;
}
