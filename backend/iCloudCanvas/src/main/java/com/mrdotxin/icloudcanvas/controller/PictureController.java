package com.mrdotxin.icloudcanvas.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrdotxin.icloudcanvas.annotation.AuthCheck;
import com.mrdotxin.icloudcanvas.common.BaseResponse;
import com.mrdotxin.icloudcanvas.common.DeleteRequest;
import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.common.ResultUtils;
import com.mrdotxin.icloudcanvas.constant.UserConstant;
import com.mrdotxin.icloudcanvas.exception.BusinessException;
import com.mrdotxin.icloudcanvas.exception.ThrowUtils;
import com.mrdotxin.icloudcanvas.model.dto.picture.*;
import com.mrdotxin.icloudcanvas.model.entity.Picture;
import com.mrdotxin.icloudcanvas.model.entity.Space;
import com.mrdotxin.icloudcanvas.model.entity.User;
import com.mrdotxin.icloudcanvas.model.enums.FileUploadTypeEnum;
import com.mrdotxin.icloudcanvas.model.enums.PictureReviewStatusEnum;
import com.mrdotxin.icloudcanvas.model.vo.PictureVO;
import com.mrdotxin.icloudcanvas.model.vo.UserVO;
import com.mrdotxin.icloudcanvas.service.PictureService;
import com.mrdotxin.icloudcanvas.service.SpaceService;
import com.mrdotxin.icloudcanvas.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestParam("file") MultipartFile file, PictureUploadRequest pictureUploadRequest, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        pictureUploadRequest.setUploadType(FileUploadTypeEnum.MULTIPART_FILE.getValue());
        PictureVO pictureVO = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);

        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestParam("url") String file, PictureUploadRequest pictureUploadRequest, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        pictureUploadRequest.setUploadType(FileUploadTypeEnum.URL.getValue());
        PictureVO pictureVO = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);

        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/url/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<PictureVO>> grabPicturesByPrompt(@RequestBody PictureUploadBatchRequest pictureUploadBatchRequest, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        List<PictureVO> pictureVOList = pictureService.uploadPictureBatch(pictureUploadBatchRequest, loginUser);

        return ResultUtils.success(pictureVOList);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(Objects.isNull(deleteRequest) || deleteRequest.getId() <= 0,
                ErrorCode.OPERATION_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);

        pictureService.deletePicture(deleteRequest, loginUser);

        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest updateRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(Objects.isNull(updateRequest) || updateRequest.getId() <= 0,
            ErrorCode.OPERATION_ERROR);

        // 校验
        Picture picture = new Picture();
        BeanUtils.copyProperties(updateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(updateRequest.getTags()));

        pictureService.validatePicture(picture);

        Picture oldPicture = pictureService.getById(picture.getId());
        ThrowUtils.throwIf(Objects.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "该图片不存在");

        pictureService.fillPictureReviewStatus(picture, userService.getLoginUser(httpServletRequest));
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get/id")
    public BaseResponse<Picture> getPictureById(@RequestParam("id") Long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.OPERATION_ERROR);

        Picture picture = pictureService.getById(id);

        ThrowUtils.throwIf(Objects.isNull(picture), ErrorCode.NOT_FOUND_ERROR);

        pictureService.checkPictureAuth(picture, userService.getLoginUser(httpServletRequest));

        return ResultUtils.success(picture);
    }

    @GetMapping("/getVO/id")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam("id") Long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.OPERATION_ERROR);

        Picture picture = pictureService.getById(id);

        ThrowUtils.throwIf(Objects.isNull(picture), ErrorCode.NOT_FOUND_ERROR);
        if (ObjectUtil.isNotNull(picture.getSpaceId())) {
            pictureService.checkPictureAuth(picture, userService.getLoginUser(httpServletRequest));
        }

        PictureVO pictureVO = PictureVO.objToVo(picture);
        UserVO userVO =  userService.getUserVO(userService.getById(pictureVO.getUserId()));
        pictureVO.setUser(userVO);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest httpServletRequest) {
        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();

        ThrowUtils.throwIf(pageSize >= 20, ErrorCode.FORBIDDEN_ERROR);

        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));

        return ResultUtils.success(picturePage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest httpServletRequest) {
        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();

        ThrowUtils.throwIf(pageSize > 30, ErrorCode.FORBIDDEN_ERROR);

        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (ObjectUtil.isNull(spaceId)) {
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        } else {
            User loginUser = userService.getLoginUser(httpServletRequest);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.OPERATION_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你无法查看当前空间内容");
            }
        }

        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));

        return ResultUtils.success(pictureService.getPictureVOPage(picturePage));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest editRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(Objects.isNull(editRequest) || editRequest.getId() <= 0,
            ErrorCode.OPERATION_ERROR);

        // 校验
        Picture picture = new Picture();
        BeanUtils.copyProperties(editRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(editRequest.getTags()));
        picture.setEditTime(new Date());

        pictureService.validatePicture(picture);

        // 是否存在
        Picture oldPicture = pictureService.getById(picture.getId());
        ThrowUtils.throwIf(Objects.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "该图片不存在");

        // 本人或者管理员
        User loginUser = userService.getLoginUser(httpServletRequest);
        pictureService.checkPictureAuth(oldPicture, loginUser);
        pictureService.fillPictureReviewStatus(picture, loginUser);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/review")
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(Objects.isNull(pictureReviewRequest), ErrorCode.PARAMS_ERROR);
        User user = userService.getLoginUser(httpServletRequest);
        pictureService.doPictureReview(pictureReviewRequest, user);
        return ResultUtils.success(true);
    }

    // 项目规模较小, 直接用枚举来维护, 后续拓展使用固定分类 + 可变标签, 并利用相似度计算合并多个类似标签
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意", "自然", "二次元", "游戏", "科幻");
        List<String> categoryList = Arrays.asList("表情包", "素材", "海报", "风景", "动漫", "插画");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    @GetMapping("/tags/all")
    public BaseResponse<List<String>> listPictureTagAll() {
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意", "自然", "二次元", "游戏", "科幻");
        return ResultUtils.success(tagList);
    }

    @GetMapping("/category/all")
    public BaseResponse<List<String>> listPictureCategoryAll() {
        List<String> categoryList = Arrays.asList("表情包", "素材", "海报", "风景", "动漫", "插画");
        return ResultUtils.success(categoryList);
    }
}
