package com.mrdotxin.icloudcanvas.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.icloudcanvas.common.ErrorCode;
import com.mrdotxin.icloudcanvas.exception.BusinessException;
import com.mrdotxin.icloudcanvas.exception.ThrowUtils;
import com.mrdotxin.icloudcanvas.model.dto.space.SpaceAddRequest;
import com.mrdotxin.icloudcanvas.model.entity.Space;
import com.mrdotxin.icloudcanvas.model.entity.User;
import com.mrdotxin.icloudcanvas.model.enums.SpaceLevelEnum;
import com.mrdotxin.icloudcanvas.service.SpaceService;
import com.mrdotxin.icloudcanvas.mapper.spaceMapper;
import com.mrdotxin.icloudcanvas.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author Administrator
 * @description 针对表【Space(空间)】的数据库操作Service实现
 * @createDate 2025-05-31 20:51:30
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<spaceMapper, Space>
        implements SpaceService {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private UserService userService;

    @Override
    public void validateSpace(Space space, boolean isAdding) {
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.PARAMS_ERROR);

        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        if (isAdding) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名不能为空");
            ThrowUtils.throwIf(ObjectUtil.isNull(spaceLevel), ErrorCode.PARAMS_ERROR, "空间级别不能为空D");
        }

        if (ObjectUtil.isNotNull(spaceLevel) && ObjectUtil.isNull(spaceLevelEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }

        if (StrUtil.isNotEmpty(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长了");
        }

    }

    @Override
    public void fillSpaceByLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            Long maxSize = spaceLevelEnum.getMaxSize();
            Long maxCount = spaceLevelEnum.getMaxCount();

            if (ObjectUtil.isNull(space.getMaxSize())) {
                space.setMaxSize(maxSize);
            }

            if (ObjectUtil.isNull(space.getMaxCount())) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public boolean isSpaceExist(Long id) {

        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);

        return this.baseMapper.exists(queryWrapper);
    }

    @Override
    public Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);

        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }

        if (ObjectUtil.isNull(spaceAddRequest.getSpaceLevel())) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }

        if (!SpaceLevelEnum.COMMON.getValue().equals(space.getSpaceType()) &&
                !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非管理员不能随意修改空间等级");
        }

        validateSpace(space, true);

        fillSpaceByLevel(space);

        Long userId = loginUser.getId();
        String lock = String.valueOf(userId).intern();
        // 使用锁来对用户的并发操作严格把控
        synchronized (lock) {
            // 使用transactionTemplate精细化事务管理
            Long newSpaceId = transactionTemplate.execute(status -> {
                boolean exits = this.lambdaQuery().eq(Space::getId, userId).exists();
                ThrowUtils.throwIf(exits, ErrorCode.FORBIDDEN_ERROR, "每个用户仅能拥有一个空间");

                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public Space getByUserId(Long userId) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);

        Space space = this.baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.PARAMS_ERROR, "该用户没有空间");

        return space;
    }
}




