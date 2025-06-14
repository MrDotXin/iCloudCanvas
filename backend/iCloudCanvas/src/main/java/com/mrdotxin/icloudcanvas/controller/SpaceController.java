package com.mrdotxin.icloudcanvas.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mrdotxin.icloudcanvas.common.BaseResponse;
import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.common.ResultUtils;
import com.mrdotxin.icloudcanvas.exception.BusinessException;
import com.mrdotxin.icloudcanvas.exception.ThrowUtils;
import com.mrdotxin.icloudcanvas.model.dto.space.*;
import com.mrdotxin.icloudcanvas.model.entity.Space;
import com.mrdotxin.icloudcanvas.model.entity.User;
import com.mrdotxin.icloudcanvas.model.enums.SpaceLevelEnum;
import com.mrdotxin.icloudcanvas.model.enums.SpaceTypeEnum;
import com.mrdotxin.icloudcanvas.model.vo.SpaceVO;
import com.mrdotxin.icloudcanvas.service.SpaceService;
import com.mrdotxin.icloudcanvas.service.UserService;
import com.mrdotxin.icloudcanvas.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @PostMapping("/update")
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceUpdateRequest), ErrorCode.PARAMS_ERROR);

        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);

        boolean isAdding = false;
        if (ObjectUtil.isNotNull(space.getId())) {
            ThrowUtils.throwIf(spaceService.isSpaceExist(space.getId()), ErrorCode.PARAMS_ERROR, "此空间不存在!");
            isAdding = true;
        }
        spaceService.validateSpace(space, isAdding);
        spaceService.fillSpaceByLevel(space);
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "无法更新, 请重试");

        return ResultUtils.success(true);
    }

    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceAddRequest), ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(httpServletRequest);

        Long added = spaceService.addSpace(spaceAddRequest, user);

        return ResultUtils.success(added);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceEditRequest), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isAnyBlank(spaceEditRequest.getSpaceName(), spaceEditRequest.getSpaceType()), ErrorCode.PARAMS_ERROR, "参数不得为空");

        User user = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isNull(SpaceTypeEnum.valueOf(spaceEditRequest.getSpaceType())), ErrorCode.PARAMS_ERROR, "请求参数错误! 空间类型值不合法");

        if (SqlUtils.checkFieldExist(spaceService, "userId", user.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前用户还没有开辟个人空间!");
        }

        UpdateWrapper<Space> wrapper = new UpdateWrapper<Space>()
                .eq("userId", user.getId())
                .set("spaceName", spaceEditRequest.getSpaceName())
                .set("spaceType", spaceEditRequest.getSpaceType());
        boolean result = spaceService.update(wrapper);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "无法保存数据库");
        return ResultUtils.success(true);
    }

    @GetMapping("/get/id/vo")
    public BaseResponse<SpaceVO> getSpaceVOByUserId(@RequestParam("userId") Long userId) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userId), ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getByUserId(userId);

        return ResultUtils.success(SpaceVO.objToVo(space));
    }


    @GetMapping("/list/space/level")
    public BaseResponse<List<SpaceLevelInfo>> listSpaceLevelInfo() {
        List<SpaceLevelInfo> spaceLevelInfos = Arrays.stream(SpaceLevelEnum.values()).map(
                spaceLevelEnum -> new SpaceLevelInfo(
                    spaceLevelEnum.getText(),
                        spaceLevelEnum.getDesc(),
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )
        ).collect(Collectors.toList());
        return ResultUtils.success(spaceLevelInfos);
    }


    @PostMapping("/change/type")
    public BaseResponse<Boolean> changeSpaceType(@RequestBody SpaceTypeModifyRequest spaceTypeModifyRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceTypeModifyRequest), ErrorCode.PARAMS_ERROR, "参数不得为空");

        User user = userService.getLoginUser(httpServletRequest);

        Space space = spaceService.getById(spaceTypeModifyRequest.getSpaceId());
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.PARAMS_ERROR, "空间不存在!");
        userService.validateIsAdminOrOwner(user, space.getUserId());

        boolean result = spaceService.lambdaUpdate()
                .eq(Space::getId, spaceTypeModifyRequest.getSpaceId())
                .set(Space::getSpaceType, spaceTypeModifyRequest.getSpaceType())
                .update();

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "无法更新!");

        return ResultUtils.success(true);
    }
}
