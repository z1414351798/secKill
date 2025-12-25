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
) ENGINE=InnoDB AUTO_INCREMENT=8040 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- shop.orders definition

CREATE TABLE `orders` (
  `order_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `sku_id` varchar(64) DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `paying_at` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `idx_orders_status_create_time` (`status`,`create_time`),
  KEY `idx_orders_status_paying_at` (`status`,`paying_at`),
  KEY `idx_orders_id_status` (`order_id`,`status`),
  KEY `inventory_order_sku` (`order_id`,`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- shop.payment_order definition

CREATE TABLE `payment_order` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `sku_id` varchar(64) DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12733 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
