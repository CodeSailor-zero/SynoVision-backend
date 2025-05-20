create database synovision;

use synovision;

-- 用户表
create table synovision.user(
    id bigint primary key auto_increment comment 'id',
    userAccount varchar(256) not null comment '用户账号',
    userPassword varchar(512) not null comment '用户密码',
    userName varchar(256) not null comment '用户昵称',
    userAvatar varchar(1024) null  comment '用户头像',
    userProfile varchar(512) null comment '用户简介',
    userRole varchar(256) not null default 'user' comment '用户角色：user/admin',
    editTime datetime not null default current_timestamp comment '编辑时间',
    createTime datetime not null default current_timestamp comment '创建时间',
    updateTime datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    isDelete tinyint not null default 0 comment '是否删除',
    unique key uk_userAccount(userAccount),
    INDEX idx_userName(userName)
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 图片表
create table synovision.picture(
    id bigint primary key auto_increment comment 'id',
    url varchar(512) not null comment '图片地址',
    name varchar(128) not null comment '图片名称',
    introduction varchar(512) null comment '图片简介',
    category varchar(64) null comment '图片分类',
    tags varchar(512) null comment '图片标签(JSON数组)',
    picSize bigint  null comment '图片体积',
    picWidth int   null comment '图片宽度',
    picHeight int  null comment '图片高度',
    picFormat varchar(32)  null comment '图片格式',
    userId bigint not null comment '用户id',
    createTime datetime not null default current_timestamp comment '创建时间',
    editTime datetime not null default current_timestamp comment '编辑时间',
    updateTime datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    isDelete tinyint not null default 0 comment '是否删除',
    INDEX idx_name (name),
    INDEX idx_introduction (introduction),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_userId (userId)
) comment '图片表' collate = utf8mb4_unicode_ci;

