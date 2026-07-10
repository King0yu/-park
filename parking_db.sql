/*
 Navicat Premium Data Transfer

 Source Server         : charging_db
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : parking_db

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 10/07/2026 19:19:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for park_area
-- ----------------------------
DROP TABLE IF EXISTS `park_area`;
CREATE TABLE `park_area`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '区域ID（主键，自增）',
  `area_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域编码（唯一标识）',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域名称',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域详细地址',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区县',
  `total_spaces` int NOT NULL DEFAULT 0 COMMENT '区域停车位总数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-运营中，2-维护中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `area_code`(`area_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '停车区域表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of park_area
-- ----------------------------
INSERT INTO `park_area` VALUES (1, 'AREA001', 'A区停车场', 'XX小区A栋南侧', 'XX省', 'XX市', 'XX区', 50, 1, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_area` VALUES (2, 'AREA002', 'B区停车场', 'XX小区B栋西侧', 'XX省', 'XX市', 'XX区', 50, 1, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_area` VALUES (3, 'AREA003', 'C区停车场', 'XX小区C区地下', 'XX省', 'XX市', 'XX区', 50, 1, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_area` VALUES (4, 'AREA004', 'D区停车场', 'XX小区D栋东侧', 'XX省', 'XX市', 'XX区', 50, 1, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_area` VALUES (5, 'AREA005', 'E区停车场', 'XX小区E区地下', 'XX省', 'XX市', 'XX区', 50, 1, '2026-05-30 16:03:12', '2026-06-17 13:59:00');

-- ----------------------------
-- Table structure for park_config
-- ----------------------------
DROP TABLE IF EXISTS `park_config`;
CREATE TABLE `park_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统名称',
  `copyright` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '版权标语',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of park_config
-- ----------------------------
INSERT INTO `park_config` VALUES (1, '小区停车场管理系统', '© 2024 小区停车场管理系统 版权所有', '2026-06-20 18:47:04', '2026-06-20 18:47:04');

-- ----------------------------
-- Table structure for park_record
-- ----------------------------
DROP TABLE IF EXISTS `park_record`;
CREATE TABLE `park_record`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID（主键，自增）',
  `record_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '记录编号（唯一标识）',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID（关联sys_user表）',
  `area_id` bigint UNSIGNED NOT NULL COMMENT '区域ID（关联park_area表）',
  `space_id` bigint UNSIGNED NOT NULL COMMENT '停车位ID（关联park_space表）',
  `car_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车牌号',
  `start_time` datetime NOT NULL COMMENT '停车开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '停车结束时间',
  `parking_duration` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '停车时长（小时）',
  `unit_price` decimal(6, 4) NOT NULL COMMENT '单价（元/小时）',
  `total_cost` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总费用',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '记录状态：0-待停车，1-停车中，2-已完成，3-已取消，4-异常',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `record_no`(`record_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_area_id`(`area_id` ASC) USING BTREE,
  INDEX `idx_space_id`(`space_id` ASC) USING BTREE,
  CONSTRAINT `fk_record_area` FOREIGN KEY (`area_id`) REFERENCES `park_area` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_record_space` FOREIGN KEY (`space_id`) REFERENCES `park_space` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '停车记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of park_record
-- ----------------------------
INSERT INTO `park_record` VALUES (1, 'PRK202401010001', 3, 1, 1, '京A12345', '2024-01-01 08:00:00', '2024-01-01 09:30:00', 1.50, 5.0000, 7.50, 2, 1, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_record` VALUES (2, 'PRK202401010002', 4, 2, 51, '京B88888', '2024-01-01 10:00:00', '2024-01-01 11:45:00', 1.75, 5.0000, 8.75, 2, 1, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_record` VALUES (3, 'PRK202401020001', 3, 1, 2, '京A54321', '2024-01-02 14:00:00', '2026-07-10 18:40:09', 22085.00, 5.0000, 110425.00, 2, 1, NULL, 0, '2026-05-30 16:03:12', '2026-07-10 18:40:09');
INSERT INTO `park_record` VALUES (29, 'PRK202606170001', 13, 3, 103, '桂A11111', '2026-06-17 13:53:39', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-06-17 13:53:39', '2026-06-17 13:53:39');
INSERT INTO `park_record` VALUES (30, 'PRK202606170002', 13, 2, 54, 'a\'a\'a', '2026-06-17 13:56:15', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 1, '2026-06-17 13:56:15', '2026-06-17 13:56:26');
INSERT INTO `park_record` VALUES (31, 'PRK202606170003', 13, 5, 201, 'AAAAA', '2026-06-17 13:58:19', '2026-06-20 18:08:39', 77.00, 5.0000, 385.00, 2, 1, NULL, 0, '2026-06-17 13:58:19', '2026-06-20 18:08:39');
INSERT INTO `park_record` VALUES (32, 'PRK202606200001', 1, 5, 203, '京A22222', '2026-06-20 18:08:22', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-06-20 18:08:22', '2026-06-20 18:08:22');
INSERT INTO `park_record` VALUES (33, 'PRK202606200002', 1, 1, 5, '桂B123456', '2026-06-20 18:09:30', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-06-20 18:09:30', '2026-06-20 18:09:30');
INSERT INTO `park_record` VALUES (34, 'PRK202606200003', 1, 1, 13, '粤A00000', '2026-06-20 18:26:35', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-06-20 18:26:35', '2026-06-20 18:26:35');
INSERT INTO `park_record` VALUES (35, 'PRK202607010001', 14, 1, 36, '桂A12345', '2026-07-01 23:32:06', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-07-01 23:32:06', '2026-07-01 23:32:06');
INSERT INTO `park_record` VALUES (36, 'PRK202607010002', 14, 5, 219, '京A111111', '2026-07-01 23:32:44', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-07-01 23:32:44', '2026-07-01 23:32:44');
INSERT INTO `park_record` VALUES (37, 'PRK202607010003', 15, 4, 194, '桂A33333', '2026-07-01 23:37:31', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-07-01 23:37:31', '2026-07-01 23:37:31');
INSERT INTO `park_record` VALUES (38, 'PRK202607010004', 15, 5, 250, '京A12312', '2026-07-01 23:38:03', '2026-07-01 23:38:38', 1.00, 5.0000, 5.00, 2, 1, NULL, 0, '2026-07-01 23:38:03', '2026-07-01 23:38:38');
INSERT INTO `park_record` VALUES (39, 'PRK202607010005', 16, 1, 1, '粤A11111', '2026-07-01 23:41:31', '2026-07-03 19:29:03', 44.00, 5.0000, 220.00, 2, 1, NULL, 0, '2026-07-01 23:41:31', '2026-07-03 19:29:03');
INSERT INTO `park_record` VALUES (40, 'PRK202607010006', 16, 5, 247, '桂A11111', '2026-07-01 23:42:01', '2026-07-01 23:42:10', 1.00, 5.0000, 5.00, 2, 1, NULL, 0, '2026-07-01 23:42:01', '2026-07-01 23:42:10');
INSERT INTO `park_record` VALUES (41, 'PRK202607030001', 1, 1, 3, 'A', '2026-07-03 19:29:34', '2026-07-03 19:29:47', 1.00, 5.0000, 5.00, 2, 1, NULL, 0, '2026-07-03 19:29:34', '2026-07-03 19:29:47');
INSERT INTO `park_record` VALUES (42, 'PRK202607030002', 1, 1, 4, '111', '2026-07-03 19:30:51', '2026-07-03 19:30:55', 1.00, 5.0000, 5.00, 2, 1, NULL, 0, '2026-07-03 19:30:51', '2026-07-03 19:30:55');
INSERT INTO `park_record` VALUES (43, 'PRK202607030003', 1, 1, 4, '222', '2026-07-03 19:31:31', '2026-07-10 19:08:58', 168.00, 5.0000, 840.00, 2, 1, NULL, 0, '2026-07-03 19:31:31', '2026-07-10 19:08:58');
INSERT INTO `park_record` VALUES (44, 'PRK202607100001', 3, 1, 1, 'A', '2026-07-10 18:40:17', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-07-10 18:40:17', '2026-07-10 18:40:17');
INSERT INTO `park_record` VALUES (45, 'PRK202607100002', 3, 1, 2, 'B', '2026-07-10 18:42:51', '2026-07-10 18:54:07', 1.00, 5.0000, 5.00, 2, 1, NULL, 0, '2026-07-10 18:42:51', '2026-07-10 18:54:07');
INSERT INTO `park_record` VALUES (46, 'PRK202607100003', 1, 1, 3, 'c\'c\'c', '2026-07-10 18:43:28', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-07-10 18:43:28', '2026-07-10 18:43:28');
INSERT INTO `park_record` VALUES (47, 'PRK202607100004', 1, 1, 6, '123', '2026-07-10 18:43:37', '2026-07-10 18:52:52', 1.00, 5.0000, 5.00, 2, 1, NULL, 0, '2026-07-10 18:43:37', '2026-07-10 18:52:52');
INSERT INTO `park_record` VALUES (48, 'PRK202607100005', 3, 1, 2, 'BBB', '2026-07-10 18:54:35', '2026-07-10 19:08:43', 1.00, 5.0000, 5.00, 2, 1, NULL, 0, '2026-07-10 18:54:35', '2026-07-10 19:08:43');
INSERT INTO `park_record` VALUES (49, 'PRK202607100006', 1, 1, 2, 'A', '2026-07-10 19:18:14', NULL, 0.00, 5.0000, 0.00, 1, 0, NULL, 0, '2026-07-10 19:18:14', '2026-07-10 19:18:14');

-- ----------------------------
-- Table structure for park_space
-- ----------------------------
DROP TABLE IF EXISTS `park_space`;
CREATE TABLE `park_space`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '停车位ID（主键，自增）',
  `area_id` bigint UNSIGNED NOT NULL COMMENT '所属区域ID（关联park_area表）',
  `space_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '停车位编码（唯一标识）',
  `space_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '停车位名称/位置描述',
  `space_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '停车位类型（地面/地下/机械/无障碍）',
  `area_size` decimal(5, 2) NOT NULL DEFAULT 12.50 COMMENT '停车位面积',
  `car_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '绑定车牌号（业主车位专用）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-空闲，1-已占用，2-故障，3-维护',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `space_code`(`space_code` ASC) USING BTREE,
  INDEX `idx_area_id`(`area_id` ASC) USING BTREE,
  CONSTRAINT `fk_space_area` FOREIGN KEY (`area_id`) REFERENCES `park_area` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 251 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '停车位表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of park_space
-- ----------------------------
INSERT INTO `park_space` VALUES (1, 1, 'P001', 'A区-1号车位', '普通', 12.50, 'A', 1, '2026-05-30 16:03:12', '2026-07-10 18:40:17');
INSERT INTO `park_space` VALUES (2, 1, 'P002', 'A区-2号车位', '普通', 12.50, 'A', 1, '2026-05-30 16:03:12', '2026-07-10 19:18:14');
INSERT INTO `park_space` VALUES (3, 1, 'P003', 'A区-3号车位', '地下', 15.00, 'c\'c\'c', 1, '2026-05-30 16:03:12', '2026-07-10 18:43:28');
INSERT INTO `park_space` VALUES (4, 1, 'P004', 'A区-4号车位', '无障碍', 18.00, '222', 0, '2026-05-30 16:03:12', '2026-07-10 19:08:58');
INSERT INTO `park_space` VALUES (5, 1, 'P005', 'A区-5号车位', '普通', 12.50, '桂B123456', 1, '2026-05-30 16:03:12', '2026-06-20 18:09:30');
INSERT INTO `park_space` VALUES (6, 1, 'P006', 'A区-6号车位', '普通', 12.50, '123', 0, '2026-05-30 16:03:12', '2026-07-10 18:52:52');
INSERT INTO `park_space` VALUES (7, 1, 'P007', 'A区-7号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (8, 1, 'P008', 'A区-8号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (9, 1, 'P009', 'A区-9号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (10, 1, 'P010', 'A区-10号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (11, 1, 'P011', 'A区-11号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (12, 1, 'P012', 'A区-12号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (13, 1, 'P013', 'A区-13号车位', '普通', 12.50, '粤A00000', 1, '2026-05-30 16:03:12', '2026-06-20 18:26:35');
INSERT INTO `park_space` VALUES (14, 1, 'P014', 'A区-14号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (15, 1, 'P015', 'A区-15号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (16, 1, 'P016', 'A区-16号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (17, 1, 'P017', 'A区-17号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (18, 1, 'P018', 'A区-18号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (19, 1, 'P019', 'A区-19号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (20, 1, 'P020', 'A区-20号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (21, 1, 'P021', 'A区-21号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (22, 1, 'P022', 'A区-22号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (23, 1, 'P023', 'A区-23号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (24, 1, 'P024', 'A区-24号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (25, 1, 'P025', 'A区-25号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (26, 1, 'P026', 'A区-26号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (27, 1, 'P027', 'A区-27号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (28, 1, 'P028', 'A区-28号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (29, 1, 'P029', 'A区-29号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (30, 1, 'P030', 'A区-30号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (31, 1, 'P031', 'A区-31号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (32, 1, 'P032', 'A区-32号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (33, 1, 'P033', 'A区-33号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (34, 1, 'P034', 'A区-34号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (35, 1, 'P035', 'A区-35号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (36, 1, 'P036', 'A区-36号车位', '普通', 12.50, '桂A12345', 1, '2026-05-30 16:03:12', '2026-07-01 23:32:06');
INSERT INTO `park_space` VALUES (37, 1, 'P037', 'A区-37号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (38, 1, 'P038', 'A区-38号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (39, 1, 'P039', 'A区-39号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (40, 1, 'P040', 'A区-40号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (41, 1, 'P041', 'A区-41号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (42, 1, 'P042', 'A区-42号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (43, 1, 'P043', 'A区-43号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (44, 1, 'P044', 'A区-44号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (45, 1, 'P045', 'A区-45号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (46, 1, 'P046', 'A区-46号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (47, 1, 'P047', 'A区-47号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (48, 1, 'P048', 'A区-48号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (49, 1, 'P049', 'A区-49号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (50, 1, 'P050', 'A区-50号车位', '普通', 12.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (51, 2, 'P051', 'B区-1号车位', '地面', 13.50, '京B88888', 1, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (52, 2, 'P052', 'B区-2号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (53, 2, 'P053', 'B区-3号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (54, 2, 'P054', 'B区-4号车位', '地面', 13.50, 'a\'a\'a', 1, '2026-05-30 16:03:12', '2026-06-17 13:56:15');
INSERT INTO `park_space` VALUES (55, 2, 'P055', 'B区-5号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (56, 2, 'P056', 'B区-6号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (57, 2, 'P057', 'B区-7号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (58, 2, 'P058', 'B区-8号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (59, 2, 'P059', 'B区-9号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (60, 2, 'P060', 'B区-10号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (61, 2, 'P061', 'B区-11号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (62, 2, 'P062', 'B区-12号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (63, 2, 'P063', 'B区-13号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (64, 2, 'P064', 'B区-14号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (65, 2, 'P065', 'B区-15号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (66, 2, 'P066', 'B区-16号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (67, 2, 'P067', 'B区-17号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (68, 2, 'P068', 'B区-18号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (69, 2, 'P069', 'B区-19号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (70, 2, 'P070', 'B区-20号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (71, 2, 'P071', 'B区-21号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (72, 2, 'P072', 'B区-22号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (73, 2, 'P073', 'B区-23号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (74, 2, 'P074', 'B区-24号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (75, 2, 'P075', 'B区-25号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (76, 2, 'P076', 'B区-26号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (77, 2, 'P077', 'B区-27号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (78, 2, 'P078', 'B区-28号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (79, 2, 'P079', 'B区-29号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (80, 2, 'P080', 'B区-30号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (81, 2, 'P081', 'B区-31号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (82, 2, 'P082', 'B区-32号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (83, 2, 'P083', 'B区-33号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (84, 2, 'P084', 'B区-34号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (85, 2, 'P085', 'B区-35号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (86, 2, 'P086', 'B区-36号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (87, 2, 'P087', 'B区-37号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (88, 2, 'P088', 'B区-38号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (89, 2, 'P089', 'B区-39号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (90, 2, 'P090', 'B区-40号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (91, 2, 'P091', 'B区-41号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (92, 2, 'P092', 'B区-42号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (93, 2, 'P093', 'B区-43号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (94, 2, 'P094', 'B区-44号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (95, 2, 'P095', 'B区-45号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (96, 2, 'P096', 'B区-46号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (97, 2, 'P097', 'B区-47号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (98, 2, 'P098', 'B区-48号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (99, 2, 'P099', 'B区-49号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (100, 2, 'P100', 'B区-50号车位', '地面', 13.50, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (101, 3, 'P101', 'C区-1号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (102, 3, 'P102', 'C区-2号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (103, 3, 'P103', 'C区-3号车位', '地下', 20.00, '桂A11111', 1, '2026-05-30 16:03:12', '2026-06-17 13:56:05');
INSERT INTO `park_space` VALUES (104, 3, 'P104', 'C区-4号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (105, 3, 'P105', 'C区-5号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (106, 3, 'P106', 'C区-6号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (107, 3, 'P107', 'C区-7号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (108, 3, 'P108', 'C区-8号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (109, 3, 'P109', 'C区-9号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (110, 3, 'P110', 'C区-10号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (111, 3, 'P111', 'C区-11号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (112, 3, 'P112', 'C区-12号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (113, 3, 'P113', 'C区-13号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (114, 3, 'P114', 'C区-14号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (115, 3, 'P115', 'C区-15号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (116, 3, 'P116', 'C区-16号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (117, 3, 'P117', 'C区-17号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (118, 3, 'P118', 'C区-18号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (119, 3, 'P119', 'C区-19号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (120, 3, 'P120', 'C区-20号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (121, 3, 'P121', 'C区-21号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (122, 3, 'P122', 'C区-22号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (123, 3, 'P123', 'C区-23号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (124, 3, 'P124', 'C区-24号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (125, 3, 'P125', 'C区-25号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (126, 3, 'P126', 'C区-26号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (127, 3, 'P127', 'C区-27号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (128, 3, 'P128', 'C区-28号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (129, 3, 'P129', 'C区-29号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (130, 3, 'P130', 'C区-30号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (131, 3, 'P131', 'C区-31号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (132, 3, 'P132', 'C区-32号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (133, 3, 'P133', 'C区-33号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (134, 3, 'P134', 'C区-34号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (135, 3, 'P135', 'C区-35号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (136, 3, 'P136', 'C区-36号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (137, 3, 'P137', 'C区-37号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (138, 3, 'P138', 'C区-38号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (139, 3, 'P139', 'C区-39号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (140, 3, 'P140', 'C区-40号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (141, 3, 'P141', 'C区-41号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (142, 3, 'P142', 'C区-42号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (143, 3, 'P143', 'C区-43号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (144, 3, 'P144', 'C区-44号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (145, 3, 'P145', 'C区-45号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (146, 3, 'P146', 'C区-46号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (147, 3, 'P147', 'C区-47号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (148, 3, 'P148', 'C区-48号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (149, 3, 'P149', 'C区-49号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (150, 3, 'P150', 'C区-50号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (151, 4, 'P151', 'D区-1号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (152, 4, 'P152', 'D区-2号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (153, 4, 'P153', 'D区-3号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (154, 4, 'P154', 'D区-4号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (155, 4, 'P155', 'D区-5号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (156, 4, 'P156', 'D区-6号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (157, 4, 'P157', 'D区-7号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (158, 4, 'P158', 'D区-8号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (159, 4, 'P159', 'D区-9号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (160, 4, 'P160', 'D区-10号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (161, 4, 'P161', 'D区-11号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (162, 4, 'P162', 'D区-12号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (163, 4, 'P163', 'D区-13号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (164, 4, 'P164', 'D区-14号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (165, 4, 'P165', 'D区-15号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (166, 4, 'P166', 'D区-16号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (167, 4, 'P167', 'D区-17号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (168, 4, 'P168', 'D区-18号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (169, 4, 'P169', 'D区-19号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (170, 4, 'P170', 'D区-20号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (171, 4, 'P171', 'D区-21号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (172, 4, 'P172', 'D区-22号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (173, 4, 'P173', 'D区-23号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (174, 4, 'P174', 'D区-24号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (175, 4, 'P175', 'D区-25号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (176, 4, 'P176', 'D区-26号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (177, 4, 'P177', 'D区-27号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (178, 4, 'P178', 'D区-28号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (179, 4, 'P179', 'D区-29号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (180, 4, 'P180', 'D区-30号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (181, 4, 'P181', 'D区-31号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (182, 4, 'P182', 'D区-32号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (183, 4, 'P183', 'D区-33号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (184, 4, 'P184', 'D区-34号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (185, 4, 'P185', 'D区-35号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (186, 4, 'P186', 'D区-36号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (187, 4, 'P187', 'D区-37号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (188, 4, 'P188', 'D区-38号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (189, 4, 'P189', 'D区-39号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (190, 4, 'P190', 'D区-40号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (191, 4, 'P191', 'D区-41号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (192, 4, 'P192', 'D区-42号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (193, 4, 'P193', 'D区-43号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (194, 4, 'P194', 'D区-44号车位', '地下', 15.00, '桂A33333', 1, '2026-05-30 16:03:12', '2026-07-01 23:37:31');
INSERT INTO `park_space` VALUES (195, 4, 'P195', 'D区-45号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (196, 4, 'P196', 'D区-46号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (197, 4, 'P197', 'D区-47号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (198, 4, 'P198', 'D区-48号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (199, 4, 'P199', 'D区-49号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (200, 4, 'P200', 'D区-50号车位', '地下', 15.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (201, 5, 'P201', 'E区-1号车位', '机械', 20.00, 'AAAAA', 1, '2026-05-30 16:03:12', '2026-06-17 13:58:19');
INSERT INTO `park_space` VALUES (202, 5, 'P202', 'E区-2号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (203, 5, 'P203', 'E区-3号车位', '机械', 20.00, '京A22222', 1, '2026-05-30 16:03:12', '2026-06-20 18:08:22');
INSERT INTO `park_space` VALUES (204, 5, 'P204', 'E区-4号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (205, 5, 'P205', 'E区-5号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (206, 5, 'P206', 'E区-6号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (207, 5, 'P207', 'E区-7号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (208, 5, 'P208', 'E区-8号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (209, 5, 'P209', 'E区-9号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (210, 5, 'P210', 'E区-10号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (211, 5, 'P211', 'E区-11号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (212, 5, 'P212', 'E区-12号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (213, 5, 'P213', 'E区-13号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (214, 5, 'P214', 'E区-14号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (215, 5, 'P215', 'E区-15号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (216, 5, 'P216', 'E区-16号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (217, 5, 'P217', 'E区-17号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (218, 5, 'P218', 'E区-18号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (219, 5, 'P219', 'E区-19号车位', '机械', 20.00, '京A111111', 1, '2026-05-30 16:03:12', '2026-07-01 23:32:44');
INSERT INTO `park_space` VALUES (220, 5, 'P220', 'E区-20号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (221, 5, 'P221', 'E区-21号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (222, 5, 'P222', 'E区-22号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (223, 5, 'P223', 'E区-23号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (224, 5, 'P224', 'E区-24号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (225, 5, 'P225', 'E区-25号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (226, 5, 'P226', 'E区-26号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (227, 5, 'P227', 'E区-27号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (228, 5, 'P228', 'E区-28号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (229, 5, 'P229', 'E区-29号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (230, 5, 'P230', 'E区-30号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (231, 5, 'P231', 'E区-31号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (232, 5, 'P232', 'E区-32号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (233, 5, 'P233', 'E区-33号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (234, 5, 'P234', 'E区-34号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (235, 5, 'P235', 'E区-35号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (236, 5, 'P236', 'E区-36号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (237, 5, 'P237', 'E区-37号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (238, 5, 'P238', 'E区-38号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (239, 5, 'P239', 'E区-39号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (240, 5, 'P240', 'E区-40号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (241, 5, 'P241', 'E区-41号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (242, 5, 'P242', 'E区-42号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (243, 5, 'P243', 'E区-43号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (244, 5, 'P244', 'E区-44号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (245, 5, 'P245', 'E区-45号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (246, 5, 'P246', 'E区-46号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (247, 5, 'P247', 'E区-47号车位', '机械', 20.00, '桂A11111', 1, '2026-05-30 16:03:12', '2026-07-01 23:42:01');
INSERT INTO `park_space` VALUES (248, 5, 'P248', 'E区-48号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (249, 5, 'P249', 'E区-49号车位', '机械', 20.00, NULL, 0, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `park_space` VALUES (250, 5, 'P250', 'E区-50号车位', '机械', 20.00, '京A12312', 1, '2026-05-30 16:03:12', '2026-07-01 23:38:03');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键，自增）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名（登录账号，唯一）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（MD5加密）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户姓名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱地址',
  `role` tinyint NOT NULL DEFAULT 2 COMMENT '角色：0-超级管理员，1-管理员，2-普通用户',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', '13800138000', NULL, 0, 1, '2026-05-30 16:03:12', '2026-06-20 18:50:10');
INSERT INTO `sys_user` VALUES (2, 'manager', 'e10adc3949ba59abbe56e057f20f883e', '管理员张三', '13800138001', NULL, 1, 1, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `sys_user` VALUES (3, 'user001', 'e10adc3949ba59abbe56e057f20f883e', '用户李四', '13800138002', NULL, 2, 1, '2026-05-30 16:03:12', '2026-07-10 19:09:45');
INSERT INTO `sys_user` VALUES (4, 'user002', 'e10adc3949ba59abbe56e057f20f883e', '用户王五', '13800138003', NULL, 2, 1, '2026-05-30 16:03:12', '2026-05-30 16:03:12');
INSERT INTO `sys_user` VALUES (13, 'wuye', 'e10adc3949ba59abbe56e057f20f883e', '王哥', '19111111111', NULL, 1, 1, '2026-06-17 13:51:53', '2026-06-17 13:51:53');
INSERT INTO `sys_user` VALUES (14, 'user', 'e10adc3949ba59abbe56e057f20f883e', 'user', '19122334455', NULL, 2, 1, '2026-07-01 23:31:15', '2026-07-01 23:31:15');
INSERT INTO `sys_user` VALUES (15, 'user111', 'e10adc3949ba59abbe56e057f20f883e', 'user111', '19100000000', NULL, 2, 1, '2026-07-01 23:36:45', '2026-07-01 23:36:45');
INSERT INTO `sys_user` VALUES (16, 'AAAAA', 'e10adc3949ba59abbe56e057f20f883e', 'AAAAA', '19199999999', NULL, 2, 1, '2026-07-01 23:41:02', '2026-07-01 23:41:02');

SET FOREIGN_KEY_CHECKS = 1;
