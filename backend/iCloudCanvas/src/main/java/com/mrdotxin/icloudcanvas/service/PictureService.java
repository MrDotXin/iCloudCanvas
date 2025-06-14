package com.mrdotxin.icloudcanvas.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mrdotxin.icloudcanvas.common.DeleteRequest;
import com.mrdotxin.icloudcanvas.model.dto.picture.PictureQueryRequest;
import com.mrdotxin.icloudcanvas.model.dto.picture.PictureReviewRequest;
import com.mrdotxin.icloudcanvas.model.dto.picture.PictureUploadBatchRequest;
import com.mrdotxin.icloudcanvas.model.dto.picture.PictureUploadRequest;
import com.mrdotxin.icloudcanvas.model.dto.space.SpaceCheckInfo;
import com.mrdotxin.icloudcanvas.model.entity.Picture;
import com.mrdotxin.icloudcanvas.model.entity.User;
import com.mrdotxin.icloudcanvas.model.vo.PictureVO;

import java.util.List;

/**
* @author Administrator
*/
public interface PictureService extends IService<Picture> {

    /**
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     *
     * @param file
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object file, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     *
     * @param pictureUploadBatchRequest
     * @param loginUser
     * @return
     */
    List<PictureVO> uploadPictureBatch(PictureUploadBatchRequest pictureUploadBatchRequest, User loginUser);

    /**
     *
     * @param picture
     */
    void validatePicture(Picture picture);

    /**
     *
     * @param picturePage
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);

    /**
     *
     * @param picture
     * @return
     */
    PictureVO getPictureVO(Picture picture);

    /**
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     *
     * @param picture
     * @param loginUser
     */
    void fillPictureReviewStatus(Picture picture, User loginUser);

    /**
     * @param picture 释放图片对象对应的资源
     */
    void freePictureResource(Picture picture);

    /**
     * 如果图片被删除, 则返还剩余额度
     * @param picture 删除的图片对象
     */
    void revertSpaceQuota(Picture picture);

    /**
     * 会尝试根据传入的图片信息来返回如果原图片删除带来的对额度影响
     * 如果不存在空间则返回负值
     * @param pictureUploadRequest
     * @param loginUser
     */
    SpaceCheckInfo getRevertedSpaceQuota(PictureUploadRequest pictureUploadRequest, User loginUser, Picture oldPictureSpaceId);

    /**
     *
     * @param deleteRequest id
     * @param loginUser 登录用户
     */
    void deletePicture(DeleteRequest deleteRequest, User loginUser);

    /**
     * @param picture   照片
     * @param loginUser 当前操作用户
     */
    void checkPictureAuth(Picture picture, User loginUser);

    /**
     * 根据当前额度更新
     */
    void resetPictureSpace(SpaceCheckInfo spaceCheckInfo);
}
