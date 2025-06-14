package com.mrdotxin.icloudcanvas.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * SQL 工具
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }

    public static <T> boolean checkFieldExist(IService<T> service, String fieldName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(fieldName, value);
        return service.count(queryWrapper) > 0;
    }

    public static <T> T getFieldByFieldName(IService<T> service, String fieldName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(fieldName, value);
        return service.getOne(queryWrapper);
    }

    public static <T> List<T> listFieldByFieldName(IService<T> service, String fieldName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(fieldName, value);
        return service.list(queryWrapper);
    }
}
