# 数据库初始化


-- 创建库
create database if not exists icc;

-- 切换库
use icc;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'ROLE_USER'       not null comment '用户角色：USER/ADMIN/BAN',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图片字段
create table if not exists picture
(
    id              bigint  auto_increment comment  'id' primary key,
    url             varchar(256)    not null comment '图片url',
    name            varchar(128)    not null comment '图片名称',
    introduction    varchar(512)     null comment '简介',
    category        varchar(128)     null comment '分类',
    tags            varchar(512)     null comment '标签',
    picSize         bigint           null comment '图片大小',
    picWidth        int              null comment '图片宽度',
    picHeight       int              null comment '图片高度',
    picScale        double           null comment '宽高比',
    picFormat       varchar(32)      null comment '图片格式',
    userId          bigint           not null comment '创建用户id',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime        datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime      datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint      default 0 not null comment '逻辑删除',
    INDEX idx_name(name),
    INDEX idx_introduction(introduction),
    INDEX idx_category(category),
    INDEX idx_tags(tags),
    INDEX idx_userid(userId)
) comment '图片' collate = utf8mb4_unicode_ci;