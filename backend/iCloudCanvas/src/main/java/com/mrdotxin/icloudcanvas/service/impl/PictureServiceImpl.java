package com.mrdotxin.icloudcanvas.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.mrdotxin.icloudcanvas.model.entity.Picture;
import com.mrdotxin.icloudcanvas.model.entity.User;
import com.mrdotxin.icloudcanvas.model.enums.FileUploadTypeEnum;
import com.mrdotxin.icloudcanvas.model.enums.PictureReviewStatusEnum;
import com.mrdotxin.icloudcanvas.model.vo.PictureVO;
import com.mrdotxin.icloudcanvas.model.vo.UserVO;
import com.mrdotxin.icloudcanvas.upload.FileManager;
import com.mrdotxin.icloudcanvas.service.PictureService;
import com.mrdotxin.icloudcanvas.mapper.PictureMapper;
import com.mrdotxin.icloudcanvas.service.UserService;
import com.mrdotxin.icloudcanvas.utils.ScrapyUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
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

        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    @Override
    public PictureVO uploadPicture(Object multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(Objects.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);

        boolean isSave = true;
        Long pictureId = pictureUploadRequest.getId();
        if (Objects.nonNull(pictureId) && pictureId > 0) {
            Picture oldPicture = this.getById(pictureId);
            boolean exists = Objects.nonNull(oldPicture);
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

            if (!userService.isAdmin(loginUser) && loginUser.getId().equals(pictureId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "禁止操作!");
            }

            String url = oldPicture.getUrl();
            fileManager.removeObjectByUrlIfExists(url);

            isSave = false;
        }

        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadFileResult uploadFileResult = fileManager.uploadFile(pictureUploadRequest.getUploadType(), multipartFile, uploadPathPrefix);

        Picture picture = getPicture(loginUser, uploadFileResult, pictureId);

        if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picture.setName(pictureUploadRequest.getPicName());
        }

        fillPictureReviewStatus(picture, loginUser);

        boolean result;
        if (isSave) {
            result = this.save(picture);
        } else {
            result = this.updateById(picture);
        }

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "上传文件失败");

        return PictureVO.objToVo(picture);
    }

    @Override
    public List<PictureVO> uploadPictureBatch(PictureUploadBatchRequest pictureUploadBatchRequest, User loginUser) {
        String searchText = pictureUploadBatchRequest.getSearchText();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR);

        Integer count = pictureUploadBatchRequest.getCount();
        ThrowUtils.throwIf(count == null || count > 30, ErrorCode.PARAMS_ERROR, "数量太多了, 最多30条");

        List<String> resultUrls = ScrapyUtils.searchPictureUrlFromBing(searchText, count);
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

    private static @NotNull Picture getPicture(User loginUser, UploadFileResult uploadFileResult, Long pictureId) {
        Picture picture = new Picture();
        picture.setUrl(uploadFileResult.getUrl());
        picture.setName(uploadFileResult.getName());
        picture.setPicSize(uploadFileResult.getPicSize());
        picture.setPicWidth(uploadFileResult.getPicWidth());
        picture.setPicHeight(uploadFileResult.getPicHeight());
        picture.setPicScale(uploadFileResult.getPicScale());
        picture.setPicFormat(uploadFileResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        if (Objects.nonNull(pictureId) && pictureId > 0) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        return picture;
    }
}




