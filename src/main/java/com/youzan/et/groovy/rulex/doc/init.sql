CREATE TABLE `et_scene` (
  `id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `app_id` varchar(32) NOT NULL DEFAULT '' COMMENT '系统标识',
  `scene_name` varchar(32) NOT NULL COMMENT '场景名称',
  `scene_code` varchar(64) NOT NULL DEFAULT '' COMMENT '场景code',
  `scene_desc` varchar(32) NOT NULL DEFAULT '' COMMENT '场景描述',
  `scene_type` tinyint(2) NOT NULL DEFAULT '0' COMMENT '场景规则类型: 1:skipOnApplied 一个匹配成功则跳过其他 2:skipOnIgnored 一个匹配失败则跳过其他 3:skipOnFailed 一个执行失败 则跳过其他',
  `scene_status` tinyint(2) NOT NULL DEFAULT 0 COMMENT '场景状态: 0 disabled 1 enabled',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_scene_code` (`scene_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='场景'

CREATE TABLE `et_scene_action` (
  `id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `app_id` varchar(32) NOT NULL DEFAULT '' COMMENT '系统标识',
  `scene_id` bigint(32) NOT NULL COMMENT '对应场景id',
  `scene_code` varchar(64) NOT NULL COMMENT '对应场景code',
  `action_code` varchar(64) NOT NULL COMMENT '动作code',
  `action_desc` varchar(256) NOT NULL DEFAULT '' COMMENT '动作描述',
  `action` varchar(1024) NOT NULL DEFAULT '' COMMENT 'groovy 脚本',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_scene_action`(`scene_code`, `action_code`),
  KEY `idx_action_code`(`action_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='场景动作';

CREATE TABLE `et_scene_rule` (
  `id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `app_id` varchar(32) NOT NULL DEFAULT '' COMMENT '系统标识',
  `scene_id` bigint(32) NOT NULL COMMENT '对应场景id',
  `scene_code` varchar(64) NOT NULL COMMENT '对应场景code',
  `rule` varchar(1024) NOT NULL COMMENT '规则',
  `rule_type` smallint(1) NOT NULL DEFAULT '1' COMMENT '规则内容类型:1: rule_expr_id 组合 1 & 2 || 3, 2: groovy 脚本',
  `rule_code` varchar(64) NOT NULL DEFAULT '' COMMENT '规则code',
  `rule_name` varchar(64) NOT NULL DEFAULT '' COMMENT '规则名称',
  `rule_desc` varchar(128) NOT NULL DEFAULT '' COMMENT '规则描述',
  `priority` int(12) NOT NULL DEFAULT '0' COMMENT '优先级:0最低,数字越大越高',
  `actions_code` varchar(128) NOT NULL DEFAULT '' COMMENT '动作code,多个动作逗号分开',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_scene_code_rule_code`(`scene_code`, `rule_code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='场景规则';

CREATE TABLE `et_scene_rule_expr` (
  `id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `scene_rule_id` bigint(32) NOT NULL COMMENT '对应场景规则id',
  `expr_var` bigint(32) NOT NULL COMMENT '场景变量id',
  `expr_op` varchar(16) NOT NULL DEFAULT '' COMMENT '操作符',
  `expr_val` varchar(32) NOT NULL DEFAULT '' COMMENT '值',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='场景规则表达式';

CREATE TABLE `et_scene_var` (
  `id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `app_id` varchar(32) NOT NULL DEFAULT '' COMMENT '系统标识',
  `scene_id` bigint(32) NOT NULL COMMENT '对应场景id',
  `scene_code` varchar(64) NOT NULL DEFAULT '' COMMENT '场景 code',
  `var_name` varchar(32) NOT NULL DEFAULT '' COMMENT '变量名称',
  `var_type` varchar(32) NOT NULL DEFAULT '' COMMENT '变量类型',
  `var_desc` varchar(32) NOT NULL DEFAULT '' COMMENT '变量描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted_at` datetime NOT NULL DEFAULT '1970-01-01 08:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_scene_var`(`scene_code`, `var_name`),
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='场景变量';