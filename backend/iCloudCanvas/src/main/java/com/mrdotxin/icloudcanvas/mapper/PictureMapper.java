package com.mrdotxin.icloudcanvas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrdotxin.icloudcanvas.model.dto.picture.inner.PictureDeleteParams;
import com.mrdotxin.icloudcanvas.model.entity.Picture;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
*/
public interface PictureMapper extends BaseMapper<Picture> {

    Integer deleteFromDatabase(@Param("id") String id);

    List<PictureDeleteParams> selectKPriorDeletedPicture(@Param("limits") Integer limits);

    Integer deletePicturesByBatch(@Param("ids") List<Long> ids);
}




