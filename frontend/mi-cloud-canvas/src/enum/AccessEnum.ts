/**
 * 权限定义
 */
export const ACCESS_ENUM = {
    NOT_LOGIN: "notLogin",
    USER: "ROLE_USER",
    ADMIN: "ROLE_ADMIN",
};

export const ACCESS_ENUM_VALUE : { [key: string]: {value : string} } = {
    "notLogin": {
        value: "未登录"
    },
    "ROLE_USER": {
        value: "用户"
    },
    "ROLE_ADMIN": {
        value: "管理员"
    },
};

