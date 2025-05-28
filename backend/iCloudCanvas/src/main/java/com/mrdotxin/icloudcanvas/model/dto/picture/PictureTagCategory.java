package com.mrdotxin.icloudcanvas.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureTagCategory implements Serializable {

    private List<String> categoryList;

    private List<String> tagList;

    private static final long serialVersionUID = 1L;
}
