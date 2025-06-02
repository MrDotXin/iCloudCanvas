package com.mrdotxin.icloudcanvas.service;

import com.mrdotxin.icloudcanvas.model.dto.space.SpaceAddRequest;
import com.mrdotxin.icloudcanvas.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mrdotxin.icloudcanvas.model.entity.User;

/**
 * @author Administrator
 */
public interface SpaceService extends IService<Space> {


    /**
     * @param space 对空间对象进行初步校验
     */
    void validateSpace(Space space, boolean isAdding);

    /**
     * @param space 根据空间等级自动填充限额
     */
    void fillSpaceByLevel(Space space);

    /**
     * @param id 查看空间是否存在
     * @return 空间是否存在
     */
    boolean isSpaceExist(Long id);

    /**
     * @param spaceAddRequest DTO
     * @param LoginUser       当前用户
     * @return spaceId
     */
    Long addSpace(SpaceAddRequest spaceAddRequest, User LoginUser);

    /**
     *
     * @param id 获取用户空间
     * @return
     *
     */
    Space getByUserId(Long id);
}
