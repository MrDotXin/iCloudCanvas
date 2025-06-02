package com.mrdotxin.icloudcanvas.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.exception.BusinessException;
import com.mrdotxin.icloudcanvas.exception.ThrowUtils;
import com.mrdotxin.icloudcanvas.model.dto.file.UploadFileResult;
import com.mrdotxin.icloudcanvas.model.dto.picture.PictureQueryRequest;
import com.mrdotxin.icloudcanvas.model.dto.picture.PictureReviewRequest;
import com.mrdotxin.icloudcanvas.model.dto.picture.PictureUploadBatchRequest;
import com.mrdotxin.icloudcanvas.model.dto.picture.PictureUploadRequest;
import com.mrdotxin.icloudcanvas.model.dto.space.SpaceCheckInfo;
import com.mrdotxin.icloudcanvas.model.entity.Picture;
import com.mrdotxin.icloudcanvas.model.entity.Space;
import com.mrdotxin.icloudcanvas.model.entity.User;
import com.mrdotxin.icloudcanvas.model.enums.FileUploadTypeEnum;
import com.mrdotxin.icloudcanvas.model.enums.PictureReviewStatusEnum;
import com.mrdotxin.icloudcanvas.model.enums.SpaceTypeEnum;
import com.mrdotxin.icloudcanvas.model.vo.PictureVO;
import com.mrdotxin.icloudcanvas.model.vo.UserVO;
import com.mrdotxin.icloudcanvas.service.SpaceService;
import com.mrdotxin.icloudcanvas.upload.FileManager;
import com.mrdotxin.icloudcanvas.service.PictureService;
import com.mrdotxin.icloudcanvas.mapper.PictureMapper;
import com.mrdotxin.icloudcanvas.service.UserService;
import com.mrdotxin.icloudcanvas.utils.PathUtils;
import com.mrdotxin.icloudcanvas.utils.ScrapyUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Administrator
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        Long userId = pictureQueryRequest.getUserId();
        String searchText = pictureQueryRequest.getSearchText();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();


        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();

        // 搜索框可以输入图片名或者包含在简介里面的字段
        log.info(pictureQueryRequest.getSearchText());
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like(StrUtil.isNotBlank(searchText), "name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }

        log.info("reviewStatus {}", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(Objects.nonNull(reviewStatus) && reviewStatus < 3, "reviewStatus", reviewStatus);
        queryWrapper.eq(Objects.nonNull(reviewerId), "reviewId", reviewerId);
        queryWrapper.eq(Objects.nonNull(id) && id > 0, "id", id);
        queryWrapper.eq(Objects.nonNull(userId) && userId > 0, "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth) && picWidth > 0, "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight) && picHeight > 0, "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale) && picScale > 0.0, "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize) && picSize > 0, "picSize", picSize);

        if (ObjectUtil.isNotNull(spaceId)) {
            queryWrapper.eq("spaceId", spaceId);
        } else {
            queryWrapper.eq("spaceId", null);
        }

        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    /**
     * 更新/删除, 上传/撤销文件, 更新额度, 回滚
     *
     * @param multipartFile 要上传的文件类型
     * @param pictureUploadRequest 附带的参数
     * @param loginUser 用户
     * @return 一个上传成功的VO
     */
    @Override
    public PictureVO uploadPicture(Object multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(Objects.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);

        Long pictureId = pictureUploadRequest.getId();
        Picture oldPicture = null;
        boolean isSave = Objects.nonNull(pictureId) && pictureId > 0;
        if (isSave) {
            oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(Objects.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(pictureId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "禁止操作!");
            }
        }

        // 得到撤销当前已存在的影响后的相对额度
        SpaceCheckInfo spaceCheckInfo = getRevertedSpaceQuota(pictureUploadRequest, loginUser, oldPicture);

        ThrowUtils.throwIf(spaceCheckInfo.getLeftCount() == 0, ErrorCode.OPERATION_ERROR, "空间超限");

        String uploadPathPrefix = ObjectUtil.isNull(pictureUploadRequest.getSpaceId()) ?
                String.format("public/%s", loginUser.getId()) :
                String.format("space/%s", pictureUploadRequest.getSpaceId());

        UploadFileResult uploadFileResult = fileManager.uploadFile(pictureUploadRequest.getUploadType(), multipartFile, uploadPathPrefix, spaceCheckInfo.getLeftSize());
        final Picture [] picture_f = { null };
        try {
            transactionTemplate.execute(status -> {
                try {
                    // 更新空间状态
                    if (spaceCheckInfo.getSpaceId() >= 0) {
                        spaceCheckInfo.setLeftSize(spaceCheckInfo.getLeftCount() + uploadFileResult.getPicSize());
                        spaceCheckInfo.setLeftCount(spaceCheckInfo.getLeftCount() + 1);
                        resetPictureSpace(spaceCheckInfo);
                    }

                    // 保存新信息
                    picture_f[0] = buildPictureWithDTO(loginUser, uploadFileResult, pictureId);
                    fillPictureReviewStatus(picture_f[0], loginUser);
                    if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
                        picture_f[0].setName(pictureUploadRequest.getPicName());
                    }
                    boolean result = isSave ? this.save(picture_f[0]) : this.updateById(picture_f[0]);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "上传文件失败");

                    return 0L;
                } catch (BusinessException e) {
                    status.setRollbackOnly(); // 回滚
                    throw e;
                }
            });
        } catch(BusinessException e) {
            if (ObjectUtil.isNotNull(picture_f[0])) {
                FreePictureResource(picture_f[0]);
            }
            throw e;
        }

        // 删除旧信息，这样如果前面的操作失败了, 就可以回滚, 相当于没有删
        FreePictureResource(oldPicture);

        return PictureVO.objToVo(picture_f[0]);
    }

    @Override
    public List<PictureVO> uploadPictureBatch(PictureUploadBatchRequest pictureUploadBatchRequest, User loginUser) {
        String searchText = pictureUploadBatchRequest.getSearchText();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR);

        Integer count = pictureUploadBatchRequest.getCount();
        ThrowUtils.throwIf(count == null || count > 30, ErrorCode.PARAMS_ERROR, "数量太多了, 最多30条");

        Integer pageStart = pictureUploadBatchRequest.getStart();
        List<String> resultUrls = ScrapyUtils.searchPictureUrlFromBing(searchText, count, pageStart);
        List<PictureVO> results = new ArrayList<>();
        int index = 0;
        for (String resultUrl : resultUrls) {
            index ++;
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setUploadType(FileUploadTypeEnum.URL.getValue());
            pictureUploadRequest.setPicName(pictureUploadBatchRequest.getPicPrefix() + index);
            results.add(this.uploadPicture(resultUrl, pictureUploadRequest, loginUser));
        }

        return results;
    }

    @Override
    public void validatePicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
        Page<PictureVO> page = new Page<>();
        BeanUtils.copyProperties(picturePage, page);

        List<Picture> pictureList = picturePage.getRecords();
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());

        page.setRecords(pictureVOList);
        return page;
    }

    @Override
    public PictureVO getPictureVO(Picture picture) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = pictureVO.getUserId();

        User user = userService.getById(userId);
        UserVO userVO = userService.getUserVO(user);

        pictureVO.setUser(userVO);

        return pictureVO;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (Objects.isNull(id) || Objects.isNull(statusEnum) || PictureReviewStatusEnum.REVIEWING.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Picture picture = this.getById(id);
        if (Objects.isNull(picture)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        if (picture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复审核!");
        }

        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        ThrowUtils.throwIf(!this.updateById(updatePicture), ErrorCode.SYSTEM_ERROR);
    }

    @Override
    public void fillPictureReviewStatus(Picture picture, User loginUser) {
        picture.setReviewTime(new Date());
        picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
    }

    @Override
    public void FreePictureResource(Picture picture) {
        if (ObjectUtil.isNull(picture)) return;

        String url = picture.getUrl();
        String thumbNail = picture.getThumbnailUrl();
        String rawUrl = PathUtils.wipeSuffix(url) + "." + picture.getRawFormat();

        fileManager.removeObjectByUrlIfExists(url);
        fileManager.removeObjectByUrlIfExists(thumbNail);
        fileManager.removeObjectByUrlIfExists(rawUrl);
    }

    @Override
    public void revertSpaceQuota(Picture picture) {

    }

    @Override
    public SpaceCheckInfo getRevertedSpaceQuota(PictureUploadRequest pictureUploadRequest, User loginUser, Picture oldPicture) {
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCode.NO_AUTH_ERROR);

        Long oldPictureSpaceId = ObjectUtil.isNull(oldPicture) ? null : oldPicture.getSpaceId();
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (ObjectUtil.isNotNull(oldPictureSpaceId)) {
            if (ObjectUtil.isNull(spaceId)) {
                pictureUploadRequest.setSpaceId(oldPictureSpaceId);
            } else if (!spaceId.equals(oldPictureSpaceId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "不能将已有空间的图片转移到另一个空间");
            }
        }

        SpaceCheckInfo spaceCheckInfo = new SpaceCheckInfo(-1L, -1L, -1L);
        if (ObjectUtil.isNotNull(spaceId)) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "空间不存在");

            if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "仅管理员或本人可以上传空间");
            }

            spaceCheckInfo.setLeftCount(space.getMaxCount() - space.getTotalCount());
            spaceCheckInfo.setLeftSize(space.getMaxSize() - space.getTotalSize());
            spaceCheckInfo.setSpaceId(space.getId());
            if (ObjectUtil.isNotNull(oldPicture)) {
                spaceCheckInfo.setLeftCount(spaceCheckInfo.getLeftCount() - 1);
                spaceCheckInfo.setLeftSize(spaceCheckInfo.getLeftSize() - oldPicture.getPicSize());
            }
        }

        return spaceCheckInfo;
    }

    @Override
    public void checkPictureAuth(Picture picture, User loginUser) {
        Long spaceId = picture.getSpaceId();
        if (ObjectUtil.isNull(spaceId)) {
            if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(picture.getUserId())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "仅本人或者管理员可以操作");
            }
        } else {
            Space space = spaceService.getById(spaceId);
            if (!SpaceTypeEnum.isPublic(space.getSpaceType())) {
                if (!space.getUserId().equals(loginUser.getId())) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "仅本人可以操作");
                }
            }
        }
    }

    @Override
    public void resetPictureSpace(SpaceCheckInfo spaceCheckInfo) {
        if (ObjectUtil.isNull(spaceCheckInfo) || spaceCheckInfo.getSpaceId() <= 0) {
            return;
        }

        UpdateWrapper<Space> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("id", spaceCheckInfo.getSpaceId());
        queryWrapper.set("totalCount", spaceCheckInfo.getLeftCount());
        queryWrapper.set("totalSize", spaceCheckInfo.getLeftSize());

        spaceService.update(queryWrapper);
    }


    private static @NotNull Picture buildPictureWithDTO(User loginUser, Object pictureProperties, Long pictureId) {
        Picture picture = new Picture();

        BeanUtils.copyProperties(pictureProperties, picture);
        picture.setUserId(loginUser.getId());

        if (Objects.nonNull(pictureId) && pictureId > 0) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        return picture;
    }
}




