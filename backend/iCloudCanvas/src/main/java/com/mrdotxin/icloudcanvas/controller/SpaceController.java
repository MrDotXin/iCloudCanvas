package com.mrdotxin.icloudcanvas.controller;

import cn.hutool.core.util.ObjectUtil;
import com.mrdotxin.icloudcanvas.common.BaseResponse;
import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.common.ResultUtils;
import com.mrdotxin.icloudcanvas.exception.ThrowUtils;
import com.mrdotxin.icloudcanvas.model.dto.space.SpaceAddRequest;
import com.mrdotxin.icloudcanvas.model.dto.space.SpaceUpdateRequest;
import com.mrdotxin.icloudcanvas.model.entity.Space;
import com.mrdotxin.icloudcanvas.model.entity.User;
import com.mrdotxin.icloudcanvas.model.vo.SpaceVO;
import com.mrdotxin.icloudcanvas.service.SpaceService;
import com.mrdotxin.icloudcanvas.service.UserService;
import net.bytebuddy.implementation.bytecode.Throw;
import org.aspectj.weaver.patterns.HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    @GetMapping("/add")
    public BaseResponse<SpaceVO> getSpaceVOByUserId(@RequestParam("userId") Long userId) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userId), ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getByUserId(userId);

        return ResultUtils.success(SpaceVO.objToVo(space));
    }
}
