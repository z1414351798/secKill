CREATE DATABASE `shop` ;

use shop;
-- shop.address definition

CREATE TABLE `address` (
  `id` bigint NOT NULL COMMENT '地址ID',
  `receiver_name` varchar(64) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `province` varchar(32) DEFAULT NULL,
  `city` varchar(32) DEFAULT NULL,
  `district` varchar(32) DEFAULT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';


-- shop.inventory definition

CREATE TABLE `inventory` (
  `sku_id` varchar(64) NOT NULL,
  `total` int NOT NULL,
  `available` int NOT NULL,
  `locked` int NOT NULL,
  `version` int NOT NULL,
  PRIMARY KEY (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- shop.inventory_deduct_log definition

CREATE TABLE `inventory_deduct_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` varchar(64) DEFAULT NULL,
  `sku_id` varchar(64) DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sku` (`order_id`,`sku_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6800 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- shop.order_outbox definition

CREATE TABLE `order_outbox` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `aggregate_id` varchar(64) DEFAULT NULL,
  `event_type` varchar(50) DEFAULT NULL,
  `payload` text,
  `status` varchar(20) DEFAULT NULL,
  `retry_count` int DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- shop.orders definition

CREATE TABLE `orders` (
  `order_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `sku_id` varchar(64) DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `paying_at` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `receiver_name` varchar(64) DEFAULT NULL,
  `receiver_phone` varchar(20) DEFAULT NULL,
  `address_snapshot` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `idx_orders_status_create_time` (`status`,`create_time`),
  KEY `idx_orders_status_paying_at` (`status`,`paying_at`),
  KEY `idx_orders_id_status` (`order_id`,`status`),
  KEY `inventory_order_sku` (`order_id`,`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- shop.payment_order definition

CREATE TABLE `payment` (
  `payment_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `sku_id` varchar(64) DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `uk_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13531 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- shop.`user` definition

CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(64) NOT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1-正常 0-禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `password` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';


-- shop.user_address definition

CREATE TABLE `user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `address_id` bigint NOT NULL,
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认地址',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_address` (`user_id`,`address_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户地址关系表';


-- shop.users definition

CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(64) DEFAULT NULL,
  `password` varchar(64) DEFAULT NULL,
  `email` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;