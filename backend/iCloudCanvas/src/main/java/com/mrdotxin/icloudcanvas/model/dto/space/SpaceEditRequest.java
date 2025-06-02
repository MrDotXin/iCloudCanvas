package com.mrdotxin.icloudcanvas.model.dto.space;

import lombok.Data;

import java.io.Serializable;

// 将用户修改(Edit)和管理员修改(update)分离
@Data
public class SpaceEditRequest implements Serializable {

    private String spaceName;

    private String spaceType;

    private static final long serialVersionUID = 1L;
}
