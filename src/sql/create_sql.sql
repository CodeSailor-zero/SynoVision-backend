create database synovision;

use synovision;

-- 用户表
create table synovision.user
(
    id           bigint primary key auto_increment comment 'id',
    userAccount  varchar(256)  not null comment '用户账号',
    userPassword varchar(512)  not null comment '用户密码',
    userName     varchar(256)  not null comment '用户昵称',
    userAvatar   varchar(1024) null comment '用户头像',
    userProfile  varchar(512)  null comment '用户简介',
    userRole     varchar(256)  not null default 'user' comment '用户角色：user/admin',
    editTime     datetime      not null default current_timestamp comment '编辑时间',
    createTime   datetime      not null default current_timestamp comment '创建时间',
    updateTime   datetime      not null default current_timestamp on update current_timestamp comment '更新时间',
    isDelete     tinyint       not null default 0 comment '是否删除',
    unique key uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 图片表
create table synovision.picture
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
    url          VARCHAR(512) NOT NULL COMMENT '图片地址',
    name         VARCHAR(128) NOT NULL COMMENT '图片名称',
    introduction VARCHAR(512) NULL COMMENT '图片简介',
    category     VARCHAR(64)  NULL COMMENT '图片分类',
    tags         VARCHAR(512) NULL COMMENT '图片标签(JSON数组)',
    picSize      BIGINT       NULL COMMENT '图片体积',
    picWidth     INT          NULL COMMENT '图片宽度',
    picHeight    INT          NULL COMMENT '图片高度',
    picFormat    VARCHAR(32)  NULL COMMENT '图片格式',
    userId       BIGINT       NOT NULL COMMENT '用户id',
    createTime   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    editTime     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    updateTime   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX idx_name (name),
    INDEX idx_introduction (introduction),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_userId (userId)
) COMMENT '图片表' COLLATE = utf8mb4_unicode_ci;

-- tag表
CREATE TABLE synovision.tag
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
    tagName    VARCHAR(64) NOT NULL UNIQUE COMMENT '标签名称',
    tagCount   INT         NOT NULL DEFAULT 0 COMMENT '标签出现次数',
    createTime DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    editTime   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    updateTime DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT     NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX idx_tagName_tagCount (tagName, tagCount)
) COMMENT 'tag表' COLLATE = utf8mb4_unicode_ci;

-- 在图片表添加审核字段
ALTER TABLE synovision.picture
    ADD COLUMN reviewStatus  INT      NOT NULL DEFAULT 0 COMMENT '是否审核，0 - 待审核，1 - 通过，2 - 未通过',
    ADD COLUMN reviewMessage DATETIME NULL COMMENT '审核信息',
    ADD COLUMN reviewId      BIGINT   NULL COMMENT '审核人id',
    ADD COLUMN reviewTime    DATETIME NULL COMMENT '审核时间';

CREATE INDEX idx_reviewStatus ON synovision.picture (reviewStatus);

-- 添加缩略图地址
ALTER TABLE synovision.picture
    ADD COLUMN thumbnailUrl VARCHAR(512) NULL COMMENT '缩略图 url';

CREATE TABLE synovision.space
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
    spaceName  VARCHAR(256) DEFAULT '默认空间名' NOT NULL COMMENT '空间名称',
    spaceLevel INT          DEFAULT 0            NOT NULL COMMENT '空间等级 0-普通级，1-专业版，2-旗舰版',
    maxSize    BIGINT       DEFAULT 0            NOT NULL COMMENT '空间图片最大容量',
    maxCount   BIGINT       DEFAULT 0            NOT NULL COMMENT '空间图片最大数量',
    totalSize  BIGINT       DEFAULT 0            NOT NULL COMMENT '空间图片总容量',
    totalCount BIGINT       DEFAULT 0            NOT NULL COMMENT '当前空间下图片总数量',
    userId     BIGINT                            not null comment '创建用户id',
    createTime DATETIME                          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    editTime   DATETIME                          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    updateTime DATETIME                          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    isDelete   TINYINT                           NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX idx_userId (userId),
    INDEX idx_spaceName (spaceName),
    INDEX idx_spaceLevel (spaceLevel)
) COMMENT '空间表' COLLATE = utf8mb4_unicode_ci;

-- 在图片表添加空间id
ALTER TABLE synovision.picture
    ADD COLUMN spaceId BIGINT NULL COMMENT '空间id(为null，则代表图片在public区)';
CREATE INDEX idx_spaceId ON synovision.picture (spaceId);