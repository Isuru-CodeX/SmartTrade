-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: smarttrade
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `smarttrade`
--

/*!40000 DROP DATABASE IF EXISTS `smarttrade`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `smarttrade` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `smarttrade`;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` int NOT NULL AUTO_INCREMENT,
  `line_one` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `line_two` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `postal_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_primary` bit(1) NOT NULL,
  `city_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_address_city` (`city_id`),
  KEY `fk_address_user` (`user_id`),
  CONSTRAINT `fk_address_city` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` (`id`, `line_one`, `line_two`, `postal_code`, `mobile`, `is_primary`, `city_id`, `user_id`) VALUES (1,'12 Main Street','Town Centre','11000','0771234567',_binary '',1,1);
INSERT INTO `address` (`id`, `line_one`, `line_two`, `postal_code`, `mobile`, `is_primary`, `city_id`, `user_id`) VALUES (2,'102/1, Ihala Bomiriya, Kaduwela.','','10640','0727938613',_binary '',1,7);
INSERT INTO `address` (`id`, `line_one`, `line_two`, `postal_code`, `mobile`, `is_primary`, `city_id`, `user_id`) VALUES (3,'102/1, Ihala Bomiriya, Kaduwela.','','10640','0727938613',_binary '',1,8);
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `brand`
--

DROP TABLE IF EXISTS `brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brand` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brand`
--

LOCK TABLES `brand` WRITE;
/*!40000 ALTER TABLE `brand` DISABLE KEYS */;
INSERT INTO `brand` (`id`, `name`) VALUES (1,'Apple');
INSERT INTO `brand` (`id`, `name`) VALUES (2,'Samsung');
INSERT INTO `brand` (`id`, `name`) VALUES (3,'Xiaomi');
INSERT INTO `brand` (`id`, `name`) VALUES (4,'Huawei');
INSERT INTO `brand` (`id`, `name`) VALUES (5,'Google');
INSERT INTO `brand` (`id`, `name`) VALUES (6,'OnePlus');
INSERT INTO `brand` (`id`, `name`) VALUES (7,'Oppo');
INSERT INTO `brand` (`id`, `name`) VALUES (8,'Vivo');
INSERT INTO `brand` (`id`, `name`) VALUES (9,'Nothing');
INSERT INTO `brand` (`id`, `name`) VALUES (10,'UMIDIGI');
INSERT INTO `brand` (`id`, `name`) VALUES (11,'Motorola');
INSERT INTO `brand` (`id`, `name`) VALUES (12,'Honor');
/*!40000 ALTER TABLE `brand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` int NOT NULL AUTO_INCREMENT,
  `qty` int NOT NULL,
  `user_id` int DEFAULT NULL,
  `stock_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cart_user` (`user_id`),
  KEY `fk_cart_stock` (`stock_id`),
  CONSTRAINT `fk_cart_stock` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` (`id`, `qty`, `user_id`, `stock_id`) VALUES (5,2,1,20);
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `city`
--

DROP TABLE IF EXISTS `city`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `city` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `city`
--

LOCK TABLES `city` WRITE;
/*!40000 ALTER TABLE `city` DISABLE KEYS */;
INSERT INTO `city` (`id`, `name`) VALUES (1,'Colombo');
INSERT INTO `city` (`id`, `name`) VALUES (2,'Kandy');
INSERT INTO `city` (`id`, `name`) VALUES (3,'Galle');
INSERT INTO `city` (`id`, `name`) VALUES (4,'Jaffna');
INSERT INTO `city` (`id`, `name`) VALUES (5,'Kegalle');
INSERT INTO `city` (`id`, `name`) VALUES (6,'Matara');
INSERT INTO `city` (`id`, `name`) VALUES (7,'Negombo');
INSERT INTO `city` (`id`, `name`) VALUES (8,'Gampaha');
INSERT INTO `city` (`id`, `name`) VALUES (9,'Kalutara');
INSERT INTO `city` (`id`, `name`) VALUES (10,'Kurunegala');
INSERT INTO `city` (`id`, `name`) VALUES (11,'Anuradhapura');
INSERT INTO `city` (`id`, `name`) VALUES (12,'Ratnapura');
INSERT INTO `city` (`id`, `name`) VALUES (13,'Badulla');
INSERT INTO `city` (`id`, `name`) VALUES (14,'Nuwara Eliya');
INSERT INTO `city` (`id`, `name`) VALUES (15,'Matale');
INSERT INTO `city` (`id`, `name`) VALUES (16,'Trincomalee');
INSERT INTO `city` (`id`, `name`) VALUES (17,'Batticaloa');
INSERT INTO `city` (`id`, `name`) VALUES (18,'Hambantota');
INSERT INTO `city` (`id`, `name`) VALUES (19,'Polonnaruwa');
INSERT INTO `city` (`id`, `name`) VALUES (20,'Vavuniya');
/*!40000 ALTER TABLE `city` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `color`
--

DROP TABLE IF EXISTS `color`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `color` (
  `id` int NOT NULL AUTO_INCREMENT,
  `value` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `color`
--

LOCK TABLES `color` WRITE;
/*!40000 ALTER TABLE `color` DISABLE KEYS */;
INSERT INTO `color` (`id`, `value`) VALUES (1,'Black');
INSERT INTO `color` (`id`, `value`) VALUES (2,'White');
INSERT INTO `color` (`id`, `value`) VALUES (3,'Silver');
INSERT INTO `color` (`id`, `value`) VALUES (4,'Gray');
INSERT INTO `color` (`id`, `value`) VALUES (5,'Blue');
INSERT INTO `color` (`id`, `value`) VALUES (6,'Red');
INSERT INTO `color` (`id`, `value`) VALUES (7,'Green');
INSERT INTO `color` (`id`, `value`) VALUES (8,'Gold');
INSERT INTO `color` (`id`, `value`) VALUES (9,'Purple');
INSERT INTO `color` (`id`, `value`) VALUES (10,'Pink');
INSERT INTO `color` (`id`, `value`) VALUES (11,'Orange');
INSERT INTO `color` (`id`, `value`) VALUES (12,'Lavender');
INSERT INTO `color` (`id`, `value`) VALUES (13,'Moonstone');
INSERT INTO `color` (`id`, `value`) VALUES (14,'Porcelain');
INSERT INTO `color` (`id`, `value`) VALUES (15,'Mist Purple');
INSERT INTO `color` (`id`, `value`) VALUES (16,'Zephyr (Rose)');
/*!40000 ALTER TABLE `color` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_types`
--

DROP TABLE IF EXISTS `delivery_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `delivery_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_types`
--

LOCK TABLES `delivery_types` WRITE;
/*!40000 ALTER TABLE `delivery_types` DISABLE KEYS */;
INSERT INTO `delivery_types` (`id`, `name`, `price`) VALUES (1,'Standard Delivery',1000);
INSERT INTO `delivery_types` (`id`, `name`, `price`) VALUES (2,'Express Delivery',2500);
/*!40000 ALTER TABLE `delivery_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `discount`
--

DROP TABLE IF EXISTS `discount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discount` (
  `id` int NOT NULL AUTO_INCREMENT,
  `coupon_code` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` double NOT NULL,
  `started_at` datetime(6) NOT NULL,
  `expired_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_discount_coupon` (`coupon_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discount`
--

LOCK TABLES `discount` WRITE;
/*!40000 ALTER TABLE `discount` DISABLE KEYS */;
INSERT INTO `discount` (`id`, `coupon_code`, `value`, `started_at`, `expired_at`) VALUES (1,'DEFAULT',0,'2025-01-01 00:00:00.000000','2099-12-31 23:59:59.000000');
/*!40000 ALTER TABLE `discount` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `model`
--

DROP TABLE IF EXISTS `model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `model` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `brand_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_model_brand` (`brand_id`),
  CONSTRAINT `fk_model_brand` FOREIGN KEY (`brand_id`) REFERENCES `brand` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `model`
--

LOCK TABLES `model` WRITE;
/*!40000 ALTER TABLE `model` DISABLE KEYS */;
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (1,'iPhone 12',1);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (2,'iPhone 13',1);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (3,'iPhone 14',1);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (4,'iPhone 15',1);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (5,'iPhone 15 Pro',1);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (6,'iPhone 15 Pro Max',1);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (7,'Galaxy A07',2);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (8,'Galaxy A15',2);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (9,'Galaxy A55',2);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (10,'Galaxy S23',2);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (11,'Galaxy S24',2);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (12,'Galaxy Note 20',2);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (13,'Redmi Note 12',3);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (14,'Redmi Note 13',3);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (15,'Poco X6',3);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (16,'Xiaomi 13T',3);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (17,'Nova 11',4);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (18,'P60 Pro',4);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (19,'Y9a',4);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (20,'Pixel 7',5);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (21,'Pixel 8',5);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (22,'Pixel 8 Pro',5);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (23,'OnePlus 11',6);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (24,'OnePlus 12',6);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (25,'Nord CE 3',6);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (26,'Reno 10',7);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (27,'Reno 11',7);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (28,'A78',7);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (29,'V29',8);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (30,'Y36',8);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (31,'X100',8);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (32,'Phone(4a) PRO',9);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (33,'Poco M8S 5G Ai',3);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (34,'Poco X8 PRO MAX',3);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (35,'G9 5G',10);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (36,'Galaxy A37',2);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (37,'Galaxy A16',2);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (38,'Pixel 10 Pro',5);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (39,'Pixel 9 Pro',5);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (40,'G5A MTK Helio A22',10);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (41,'Redmi Note 15',3);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (42,'Moto Edge 60 Fusion 5G',11);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (43,'Magic V6',12);
INSERT INTO `model` (`id`, `name`, `brand_id`) VALUES (44,'Phone (3a) Pro',9);
/*!40000 ALTER TABLE `model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `qty` int NOT NULL,
  `rating` int NOT NULL,
  `orders_id` int DEFAULT NULL,
  `stock_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_items_orders` (`orders_id`),
  KEY `fk_order_items_stock` (`stock_id`),
  CONSTRAINT `fk_order_items_orders` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_order_items_stock` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` (`id`, `qty`, `rating`, `orders_id`, `stock_id`) VALUES (1,1,0,1,20);
INSERT INTO `order_items` (`id`, `qty`, `rating`, `orders_id`, `stock_id`) VALUES (2,1,0,2,19);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `delivery_types_id` int DEFAULT NULL,
  `status_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `customer_email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customer_mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customer_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipping_address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stripe_payment_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_orders_delivery` (`delivery_types_id`),
  KEY `fk_orders_status` (`status_id`),
  KEY `fk_orders_user` (`user_id`),
  CONSTRAINT `fk_orders_delivery` FOREIGN KEY (`delivery_types_id`) REFERENCES `delivery_types` (`id`),
  CONSTRAINT `fk_orders_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` (`id`, `created_at`, `updated_at`, `delivery_types_id`, `status_id`, `user_id`, `customer_email`, `customer_mobile`, `customer_name`, `shipping_address`, `stripe_payment_id`, `total_amount`) VALUES (1,'2026-10-11 18:50:08.046520','2026-10-11 18:57:14.690135',1,5,9,'test@example.com','0771234567','John Doe','No 123 Main Street, Colombo 10100','pi_3UPMfS1lqfeP35Un1hek59nt',120000);
INSERT INTO `orders` (`id`, `created_at`, `updated_at`, `delivery_types_id`, `status_id`, `user_id`, `customer_email`, `customer_mobile`, `customer_name`, `shipping_address`, `stripe_payment_id`, `total_amount`) VALUES (2,'2026-10-11 18:53:58.542511','2026-10-11 18:55:42.531689',2,6,7,'isururathnayaka999@gmail.com','0727938613','Isuru Rathnayaka','102/1, Ihala Bomiriya, Kaduwela., Colombo 10640','pi_3UPMjI1lqfeP35Un1oe1jrD9',647500);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `model_id` int DEFAULT NULL,
  `color_id` int DEFAULT NULL,
  `quality_id` int DEFAULT NULL,
  `storage_id` int DEFAULT NULL,
  `seller_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_product_model` (`model_id`),
  KEY `fk_product_color` (`color_id`),
  KEY `fk_product_quality` (`quality_id`),
  KEY `fk_product_storage` (`storage_id`),
  KEY `fk_product_seller` (`seller_id`),
  CONSTRAINT `fk_product_color` FOREIGN KEY (`color_id`) REFERENCES `color` (`id`),
  CONSTRAINT `fk_product_model` FOREIGN KEY (`model_id`) REFERENCES `model` (`id`),
  CONSTRAINT `fk_product_quality` FOREIGN KEY (`quality_id`) REFERENCES `quality` (`id`),
  CONSTRAINT `fk_product_seller` FOREIGN KEY (`seller_id`) REFERENCES `seller` (`id`),
  CONSTRAINT `fk_product_storage` FOREIGN KEY (`storage_id`) REFERENCES `storage` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (4,'2026-10-11 13:50:01.327435','2026-10-11 13:50:01.327435','Samsung Galaxy A07 128GB Brand New','<p>Samsung Galaxy A07 - slim body, bright display and all-day battery. Sealed box with 1 year warranty.</p>',7,1,1,3,1);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (5,'2026-10-11 13:50:01.336256','2026-10-11 13:50:01.336256','Samsung Galaxy A07 64GB Like New','<p>Samsung Galaxy A07 in perfect condition. Includes charger and a protective case.</p>',7,3,2,2,1);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (6,'2026-10-11 14:27:18.836380','2026-10-11 14:27:18.836380','Nothing Phone (4a) Pro Cell Phone 2026 New, Unlocked Android 16 Smartphone, Triple-Lens Camera, AI Photo Edits, 6.83\" 144Hz AMOLED Display, 5080mAh Battery, Dual-SIM Glyph Matrix (Black, 12+256GB)','<div><br></div><div>Brand :- Nothing</div><div><ul><li>Operating System :- Android 16</li><li>Ram Memory Installed Size :- 12 GB</li><li>CPU Model :- Snapdragon</li><li>CPU Speed :- 2.8 GHz</li><li>Memory Storage Capacity :- 12 GB</li><li>Screen Size :- 6.83 inches</li><li>Resolution&nbsp; :- 1260 x 2800</li><li>Refresh Rate :- 144</li><li>Model Name :- Nothing Phone 4a Pro</li></ul></div><div><br></div><div>About this item :-<br><br></div><div><ul><li>Pro-Grade Camera with AI Edits: Capture every detail from a distance with the advanced triple camera system. Nothing Phone 4a Pro Features a 50MP Sony main sensor with OIS, a 50MP periscope telephoto lens supporting 3.5x optical up to 140x ultra zoom, and an 8MP ultra-wide lens. Whether it’s expansive landscapes or distant cityscapes, this unlocked cell phone with AI-enhanced imaging ensures uncompromising clarity in every shot</li><li>6.83\" 144Hz AMOLED &amp; 5,000-Nit Peak Brightness: Immerse yourself in a stunning 6.83-inch 1.5K AMOLED display. Delivering a buttery-smooth 144Hz adaptive refresh rate and a record-breaking 5,000 nits of peak brightness, this unlocked Nothing smartphone ensures your content remains crystal clear and vibrant even under direct sunlight. Plus, it is protected by Gorilla Glass 7i for superior scratch and drop resistance</li><li>All-Day 5080mAh Battery &amp; Android 16 Nothing OS 4.1: Stay powered throughout your busiest days with a high-capacity 5,080mAh battery and 50W fast charging (recharge to 60% in just 30 minutes). Experience the clean, bloatware-free Nothing OS 4.1 based on Android 16 and advanced AI tools for a truly intuitive user Interaction</li><li>Snapdragon 7 Gen 4 &amp; 5G Performance: Experience blazing-fast speeds and seamless multitasking powered by the Qualcomm Snapdragon 7 Gen 4 processor. Unlocked android phones pair with up to 12GB of LPDDR5X RAM and UFS 3.1 storage, this device is optimized for smooth 4K video editing and reliable 5G connectivity for the ultimate mobile Journey</li><li>Iconic Glyph Matrix &amp; Metal Unibody: Stand out from the crowd with the all-new Glyph Matrix, featuring 137 individually controllable mini-LEDs that act as a secondary display for notifications, timers, and battery status. Crafted with a premium, ultra-slim 7.9mm aluminum unibody and a transparent camera module, the Nothing Phone 4a Pro merges futuristic aesthetics with an IP65-rated dust and water resistance</li><li>Connectivity: AT&amp;T: 3G/4G/5G supported (not all 5G). voLTE and voNR is supported. LTE: 2/4/5/12/48/66 5G: n2/n5/n48/n66/n77 T-Mobile: 3G/4G/5G supported (not all 5G). LTE: 2/4/12/17/25/41/66/71, 5G: n2/n12/n25/n41/n66/n75</li><li>Verizon Network: Not recommended (No CDMA). Limited 3G/4G/5G bands. You MUST contact Verizon customer service to add the device IMEI to their database before use. LTE: 2/4/5/48/66. 5G: n2/n5/n48/n66/n81</li></ul></div>',32,1,1,3,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (8,'2026-10-11 14:34:20.905771','2026-10-11 14:34:20.905771','Nothing Phone (4a) Pro Cell Phone 2026 New, Unlocked Android 16 Smartphone, Triple-Lens Camera, AI Photo Edits, 6.83\" 144Hz AMOLED Display, 5080mAh Battery, Dual-SIM Glyph Matrix (Sliver, 12+256GB)','About this item :-<br><br><div><ul><li>Pro-Grade Camera with AI Edits: Capture every detail from a distance with the advanced triple camera system. Nothing Phone 4a Pro Features a 50MP Sony main sensor with OIS, a 50MP periscope telephoto lens supporting 3.5x optical up to 140x ultra zoom, and an 8MP ultra-wide lens. Whether it’s expansive landscapes or distant cityscapes, this unlocked cell phone with AI-enhanced imaging ensures uncompromising clarity in every shot</li><li>6.83\" 144Hz AMOLED &amp; 5,000-Nit Peak Brightness: Immerse yourself in a stunning 6.83-inch 1.5K AMOLED display. Delivering a buttery-smooth 144Hz adaptive refresh rate and a record-breaking 5,000 nits of peak brightness, this unlocked Nothing smartphone ensures your content remains crystal clear and vibrant even under direct sunlight. Plus, it is protected by Gorilla Glass 7i for superior scratch and drop resistance</li><li>All-Day 5080mAh Battery &amp; Android 16 Nothing OS 4.1: Stay powered throughout your busiest days with a high-capacity 5,080mAh battery and 50W fast charging (recharge to 60% in just 30 minutes). Experience the clean, bloatware-free Nothing OS 4.1 based on Android 16 and advanced AI tools for a truly intuitive user Interaction</li><li>Snapdragon 7 Gen 4 &amp; 5G Performance: Experience blazing-fast speeds and seamless multitasking powered by the Qualcomm Snapdragon 7 Gen 4 processor. Unlocked android phones pair with up to 12GB of LPDDR5X RAM and UFS 3.1 storage, this device is optimized for smooth 4K video editing and reliable 5G connectivity for the ultimate mobile Journey</li><li>Iconic Glyph Matrix &amp; Metal Unibody: Stand out from the crowd with the all-new Glyph Matrix, featuring 137 individually controllable mini-LEDs that act as a secondary display for notifications, timers, and battery status. Crafted with a premium, ultra-slim 7.9mm aluminum unibody and a transparent camera module, the Nothing Phone 4a Pro merges futuristic aesthetics with an IP65-rated dust and water resistance</li><li>Connectivity: AT&amp;T: 3G/4G/5G supported (not all 5G). voLTE and voNR is supported. LTE: 2/4/5/12/48/66 5G: n2/n5/n48/n66/n77 T-Mobile: 3G/4G/5G supported (not all 5G). LTE: 2/4/12/17/25/41/66/71, 5G: n2/n12/n25/n41/n66/n72</li><li>Verizon Network: Not recommended (No CDMA). Limited 3G/4G/5G bands. You MUST contact Verizon customer service to add the device IMEI to their database before use. LTE: 2/4/5/48/66. 5G: n2/n5/n48/n66/n78</li></ul></div>',32,3,1,4,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (9,'2026-10-11 14:37:57.123084','2026-10-11 14:37:57.123084','Nothing Phone (4a) Pro Cell Phone 2026 New, Unlocked Android 16 Smartphone, Triple-Lens Camera, AI Photo Edits, 6.83\" 144Hz AMOLED Display, 5080mAh Battery, Dual-SIM Glyph Matrix (Pink, 12+256GB)','<ul><li>Brand :- Nothing</li><li>Operating System :- Android 16</li><li>Ram Memory Installed Size :- 12 GB</li><li>CPU Speed :- 2.8 GHz</li><li>Memory Storage Capacity :- 12 GB</li><li>Screen Size :- 6.83 inches</li><li>Resolution :- 1260 x 2800</li><li>Refresh Rate :- 144</li><li>Model Name :- Nothing Phone 4a Pro</li><li>Wireless Carrier :- Unlocked</li></ul><div><br>About this item :-<br><br></div><div><ul><li>Pro-Grade Camera with AI Edits: Capture every detail from a distance with the advanced triple camera system. Nothing Phone 4a Pro Features a 50MP Sony main sensor with OIS, a 50MP periscope telephoto lens supporting 3.5x optical up to 140x ultra zoom, and an 8MP ultra-wide lens. Whether it’s expansive landscapes or distant cityscapes, this unlocked cell phone with AI-enhanced imaging ensures uncompromising clarity in every shot</li><li>6.83\" 144Hz AMOLED &amp; 5,000-Nit Peak Brightness: Immerse yourself in a stunning 6.83-inch 1.5K AMOLED display. Delivering a buttery-smooth 144Hz adaptive refresh rate and a record-breaking 5,000 nits of peak brightness, this unlocked Nothing smartphone ensures your content remains crystal clear and vibrant even under direct sunlight. Plus, it is protected by Gorilla Glass 7i for superior scratch and drop resistance</li><li>All-Day 5080mAh Battery &amp; Android 16 Nothing OS 4.1: Stay powered throughout your busiest days with a high-capacity 5,080mAh battery and 50W fast charging (recharge to 60% in just 30 minutes). Experience the clean, bloatware-free Nothing OS 4.1 based on Android 16 and advanced AI tools for a truly intuitive user Interaction</li><li>Snapdragon 7 Gen 4 &amp; 5G Performance: Experience blazing-fast speeds and seamless multitasking powered by the Qualcomm Snapdragon 7 Gen 4 processor. Unlocked android phones pair with up to 12GB of LPDDR5X RAM and UFS 3.1 storage, this device is optimized for smooth 4K video editing and reliable 5G connectivity for the ultimate mobile Journey</li><li>Iconic Glyph Matrix &amp; Metal Unibody: Stand out from the crowd with the all-new Glyph Matrix, featuring 137 individually controllable mini-LEDs that act as a secondary display for notifications, timers, and battery status. Crafted with a premium, ultra-slim 7.9mm aluminum unibody and a transparent camera module, the Nothing Phone 4a Pro merges futuristic aesthetics with an IP65-rated dust and water resistance</li><li>Connectivity: AT&amp;T: 3G/4G/5G supported (not all 5G). voLTE and voNR is supported. LTE: 2/4/5/12/48/66 5G: n2/n5/n48/n66/n77 T-Mobile: 3G/4G/5G supported (not all 5G). LTE: 2/4/12/17/25/41/66/71, 5G: n2/n12/n25/n41/n66/n73</li><li>Verizon Network: Not recommended (No CDMA). Limited 3G/4G/5G bands. You MUST contact Verizon customer service to add the device IMEI to their database before use. LTE: 2/4/5/48/66. 5G: n2/n5/n48/n66/n79</li></ul></div>',32,10,1,4,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (10,'2026-10-11 14:43:20.955770','2026-10-11 14:43:20.955770','XIAOMI Poco M8S 5G Ai (Compatible with Tmobile Mint Tello & Global) (256GB + 8GB) 50MP Dual Camera NFC 6.9\" 144Hz Octa Core Unlocked Dual Sim Model 26067PC09G Liberado (Black.)','<div><ul><li>Brand :- Xiaomi</li><li>Operating System :- Hyper OS</li><li>Ram Memory Installed Size :- 8 GB</li><li>CPU Model :- Snapdragon</li><li>CPU Speed :- 2300 MHz</li><li>Memory Storage Capacity :- 256 GB</li><li>Screen Size :- 6.9 inches</li><li>Resolution :- 2392 X 1080</li><li>Refresh Rate :- 144</li><li>Model Name :- M8S 5G</li></ul></div><div>About this item :-<br><br></div><div><ul><li>USA MARKET ONLY WORK ON TMOBILE MINT TELLO OR ANY UNDER TMOBILE NETWORK PHONE NEEDS A SIM CARD ALREADY ACTIVATED ,OUTSIDE USA WORKS ANY GSM CARRIER SIM GSM FCC ID: 2AFZZPCA6G</li><li>nano SIM 1 + Hybrid (nano SIM or microSD)(No Esim supported), 5G: 1/2/3/5/7/8/20/28/38/40/41/48/66/71/77/78 4G: LTE FDD: B1/2/3/4/5/7/8/18/19/20/26/28/66/71 4G: LTE TDD: B38/40/41/42/48 -3G: WCDMA::B1/2/4/5/6/8/19 - 2G QUAD BAND</li><li>6.9\" FHD+ DotDisplayResolution: 2340 × 1080PPI 374Refresh rate: Up to 144Hz**Refresh rate can be adjusted up to 144Hz for supported apps.Touch sampling rate: Up to 288HzBrightness: 700nits (typ) 850 nits HBMColour depth: 8-bitContrast ratio: 1400:1Colour gamut: 85% NTSCReading modeTÜV Rheinland Low Blue Light (Software Solution) Certified TÜV Rheinland Circadian Friendly Certified TÜV Rheinland Flicker Free Certified</li><li>50MP main camera5P lensf/1.8Auxiliary lensHDR mode | Soft-light ring | Portrait mode | Night mode | Time-lapseRear camera video recording1080p (1920x1080) HD video recording at 30 fps720p (1280x720) HD video recording at 30 fps - 8MP front cameraf/2HDR mode | Soft-light ring | Portrait mode | Night mode | Time-lapseFront camera video recording1080p (1920x1080) HD video recording at 30 fps720p (1280x720) HD video recording at 30 fps</li><li>NFC - GPS: L1 | GLONASS: G1 | BDS: B1I+B1C | Galileo E1 - Virtual proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | IR blaster | Gyroscope - Side fingerprint sensor AI face unlock. Virtual proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | IR blaster</li><li>Snapdragon 6s Gen 3 Mobile Platform6nm manufacturing processCPU: Octa-core, up to 2.3GHzGPU: Adreno 7000mAh (typ) battery33W fast chargingUp to 18W reverse charging</li></ul></div>',33,1,1,4,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (11,'2026-10-11 14:51:33.748321','2026-10-11 14:51:33.748321','XIAOMI Poco X8 PRO MAX 5G Ai (Compatible with Tmobile Mint Tello & Global) (512GB + 12GB) 6.83\" 50MP Gamers Phone NFC Dual sim Unlocked Model 2602BPC18G Liberado (Black)','<div><ul><li>Brand :- Xiaomi</li><li>Operating System :- HyperOS Base on Android 16</li><li>Ram Memory Installed Size :- 12 GB</li><li>CPU Model :- Mediatek Dimensity 9400</li><li>CPU Speed :- 3.73 GHz</li><li>Memory Storage Capacity :- 512 GB</li><li>Screen Size :- 6.83 inches</li><li>Resolution :- 2772 x 1280</li><li>Refresh Rate :- 120</li><li>Model Name :- Poco X8 Pro MAX 5G<br><br>About this item :-<br><br></li><li>USA MARKET ONLY WORK ON TMOBILE BOOST MINT TELLO OR ANY UNDER TMOBILE NETWORK PHONE NEEDS A SIM CARD ALREADY ACTIVATED ,OUTSIDE USA WORKS ANY GSM CARRIER SIM GSM FCC ID: 2AFZZPC0AG</li><li>Dual SIM (nano SIM + nano SIM or nano SIM + eSIM) (NO MICRO SD) 5G: 5G SA: n1/2/3/5/7/8/20/26/28/38/40/41/66*(70M)//77/78/485G NSA: n1/3/5/7/8/20/28/38/40/41/66*(70M)/77/784G LTE TDD: B38/40/41/42/484G LTE FDD: B1/2/3/4/5/7/8/18/19/20/26/28/66*(70M)/713G WCDMA: 1/2/4/5/6/8/192G GSM: 2/3/5/8Supports 4x4 MIMO*5G SA/NSA N66(70M) and 4G B66(70M): UL 1710MHz～1780MHz, DL 2110MHz～2180MHz*5G connectivity may vary based on region availability and local operator support.</li><li>6.83\" 1.5K 120Hz ultra-bright AMOLED displayResolution: 2772 x 1280Contrast ratio: 8,000,000:1Refresh rate: Up to 120Hz*Refresh rate can be adjusted to up to 120Hz for supported apps.Peak brightness: 3500 nits covering 25% display areaHBM brightness: 2000 nitsTypical brightness: 800 n: 2560Hz*480Hz touch sampling rate is activated under Game Turbo Mode.*2560Hz instant touch sampling rate is activated under Game Turbo Mode.12-bit color depth, 68 billion colorsDCI-P3 wide display 2.0HDR10+TÜV Rheinland Low Blue Light (Hardware Solution) CertifiedTÜV Rheinland Flicker Free CertifiedTÜV Rheinland Circadian Friendly CertifiedDolby VisionCorning Gorilla Glass 7i</li><li>MediaTek Dimensity 9500s3nm manufacturing process1 x Cortex-X925, up to 3.73GHz3 x Cortex-X4, up to 3.3GHz4 x Cortex-A720, up to 2.4GHzImmortalis-G925 MC11NPU 890 - 8500mAh (typ)100W HyperChargeUSB Type-C charging portUp to 27W reverse chargingSupports PD3.0/PD2.0Smart charging</li><li>Wi-Fi Protocol: Wi-Fi 7/ Wi-Fi 6/ Wi-Fi 5/ Wi-Fi 4 and 802.11a/b/g*Wi-Fi 7/Wi-Fi 6 capability may vary based on regional availability and local network support. Wi-Fi connectivity (including Wi-Fi frequency bands, Wi-Fi standards and other features as ratified in IEEE Standard 802.11 specifications) may vary based on regional availability and local network support. The function may be added via OTA when and where applicable.WLAN frequency: 2.4G Wi-Fi | 5G Wi-FiSupports Multi-Link Operation, 2x2 MIMO, 8x8 Sounding for MU-MIMO, Wi-Fi Direct, MiracastBluetooth 6.0, Dual-BluetoothSupports SBC / AAC / LDAC / LHDC 5.0 / LC3 / ASHA / AuracastNFC</li><li>50MP main cameraLight Fusion 600 image sensor1.6μm 4-in-1 Super Pixelf/1.51/1.95\" sensor size6P lensOIS8MP ultra-wide cameraf/2.2Rear camera photography featuresDynamic shotsMotion captureDocuments modeMotion tracking focusPortrait modePro mode50MP modeVoice shutterHDRPanoramaLong exposureTimed burstRear camera video recording4K (3840x2160) video recording at 30 fps, or 60 fps1080p (1920x1080) HD video recording at 30 fps, or 60 fps720p (1280x720) HD video recording at 30 fpsSlow motion video:720p (1280x720) at 120fps / 240fps / 960fps1080p (1920x1080) at 120fps / 240fps / 960fpsRear camera video featuresSlow</li><li>Ultrasonic in-display fingerprint sensorAI Face Unlock Face Unlock Proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | Gyroscope | IR blaster | Flicker sensor | X-axis linear vibration motorBeidou: B1I+B1C+B2a | GPS: L1+L5 | Galileo: E1+E5a | GLONASS: G1 | QZSS: L1+L5 | NavIC: L5AGNSS | Data network | Wireless network | Sensor Assisted Positioning</li><li>Beidou: B1I+B1C+B2a+B2b | GPS: L1+L5 | Galileo: E1+E5a+E5b | GLONASS: G1 | QZSS: L1+L5 | NavIC: L1+L5AGNSS | Data network | Wireless network | Sensor Assisted Positioning Proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | Gyroscope | IR blaster | Flicker sensor | X-axis linear vibration motor</li></ul></div>',34,1,1,5,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (12,'2026-10-11 14:54:17.305969','2026-10-11 14:54:17.305969','XIAOMI Poco X8 PRO MAX 5G Ai (Compatible with Tmobile Mint Tello & Global) (512GB + 12GB) 6.83\" 50MP Gamers Phone NFC Dual sim Unlocked Model 2602BPC18G Liberado (White)','<ol><li>Brand :- Xiaomi</li><li>Operating System :- HyperOS Base on Android 16</li><li>Ram Memory Installed Size :- 12 GB</li><li>CPU Model :- Mediatek Dimensity 9400</li><li>CPU Speed :- 3.73 GHz</li><li>Memory Storage Capacity :- 512 GB</li><li>Screen Size :- 6.83 inches</li><li>Resolution :- 2772 x 1280</li><li>Refresh Rate :- 120</li><li>Model Name :- Poco X8 Pro MAX 5G</li></ol><div>About this item :-</div><div><br></div><div><ul><li>USA MARKET ONLY WORK ON TMOBILE BOOST MINT TELLO OR ANY UNDER TMOBILE NETWORK PHONE NEEDS A SIM CARD ALREADY ACTIVATED ,OUTSIDE USA WORKS ANY GSM CARRIER SIM GSM FCC ID: 2AFZZPC0AG</li><li>Dual SIM (nano SIM + nano SIM or nano SIM + eSIM) (NO MICRO SD) 5G: 5G SA: n1/2/3/5/7/8/20/26/28/38/40/41/66*(70M)//77/78/485G NSA: n1/3/5/7/8/20/28/38/40/41/66*(70M)/77/784G LTE TDD: B38/40/41/42/484G LTE FDD: B1/2/3/4/5/7/8/18/19/20/26/28/66*(70M)/713G WCDMA: 1/2/4/5/6/8/192G GSM: 2/3/5/8Supports 4x4 MIMO*5G SA/NSA N66(70M) and 4G B66(70M): UL 1710MHz～1780MHz, DL 2110MHz～2180MHz*5G connectivity may vary based on region availability and local operator support.</li><li>6.83\" 1.5K 120Hz ultra-bright AMOLED displayResolution: 2772 x 1280Contrast ratio: 8,000,000:1Refresh rate: Up to 120Hz*Refresh rate can be adjusted to up to 120Hz for supported apps.Peak brightness: 3500 nits covering 25% display areaHBM brightness: 2000 nitsTypical brightness: 800 n: 2560Hz*480Hz touch sampling rate is activated under Game Turbo Mode.*2560Hz instant touch sampling rate is activated under Game Turbo Mode.12-bit color depth, 68 billion colorsDCI-P3 wide display 2.0HDR10+TÜV Rheinland Low Blue Light (Hardware Solution) CertifiedTÜV Rheinland Flicker Free CertifiedTÜV Rheinland Circadian Friendly CertifiedDolby VisionCorning Gorilla Glass 7i</li><li>MediaTek Dimensity 9500s3nm manufacturing process1 x Cortex-X925, up to 3.73GHz3 x Cortex-X4, up to 3.3GHz4 x Cortex-A720, up to 2.4GHzImmortalis-G925 MC11NPU 890 - 8500mAh (typ)100W HyperChargeUSB Type-C charging portUp to 27W reverse chargingSupports PD3.0/PD2.0Smart charging</li><li>Wi-Fi Protocol: Wi-Fi 7/ Wi-Fi 6/ Wi-Fi 5/ Wi-Fi 4 and 802.11a/b/g*Wi-Fi 7/Wi-Fi 6 capability may vary based on regional availability and local network support. Wi-Fi connectivity (including Wi-Fi frequency bands, Wi-Fi standards and other features as ratified in IEEE Standard 802.11 specifications) may vary based on regional availability and local network support. The function may be added via OTA when and where applicable.WLAN frequency: 2.4G Wi-Fi | 5G Wi-FiSupports Multi-Link Operation, 2x2 MIMO, 8x8 Sounding for MU-MIMO, Wi-Fi Direct, MiracastBluetooth 6.0, Dual-BluetoothSupports SBC / AAC / LDAC / LHDC 5.0 / LC3 / ASHA / AuracastNFC</li><li>50MP main cameraLight Fusion 600 image sensor1.6μm 4-in-1 Super Pixelf/1.51/1.95\" sensor size6P lensOIS8MP ultra-wide cameraf/2.2Rear camera photography featuresDynamic shotsMotion captureDocuments modeMotion tracking focusPortrait modePro mode50MP modeVoice shutterHDRPanoramaLong exposureTimed burstRear camera video recording4K (3840x2160) video recording at 30 fps, or 60 fps1080p (1920x1080) HD video recording at 30 fps, or 60 fps720p (1280x720) HD video recording at 30 fpsSlow motion video:720p (1280x720) at 120fps / 240fps / 960fps1080p (1920x1080) at 120fps / 240fps / 960fpsRear camera video featuresSlow</li><li>Ultrasonic in-display fingerprint sensorAI Face Unlock Face Unlock Proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | Gyroscope | IR blaster | Flicker sensor | X-axis linear vibration motorBeidou: B1I+B1C+B2a | GPS: L1+L5 | Galileo: E1+E5a | GLONASS: G1 | QZSS: L1+L5 | NavIC: L5AGNSS | Data network | Wireless network | Sensor Assisted Positioning</li><li>Beidou: B1I+B1C+B2a+B2b | GPS: L1+L5 | Galileo: E1+E5a+E5b | GLONASS: G1 | QZSS: L1+L5 | NavIC: L1+L5AGNSS | Data network | Wireless network | Sensor Assisted Positioning Proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | Gyroscope | IR blaster | Flicker sensor | X-axis linear vibration motor</li></ul></div>',34,2,1,5,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (13,'2026-10-11 14:56:37.289125','2026-10-11 14:56:37.289125','XIAOMI Poco X8 PRO MAX 5G Ai (Compatible with Tmobile Mint Tello & Global) (512GB + 12GB) 6.83\" 50MP Gamers Phone NFC Dual sim Unlocked Model 2602BPC18G Liberado (Orange)','<ul><li>Brand :- Xiaomi</li><li>Operating System :- HyperOS Base on Android 16</li><li>Ram Memory Installed Size :- 12 GB</li><li>CPU Model :- Mediatek Dimensity 9400</li><li>CPU Speed :- 3.73 GHz</li><li>Memory Storage Capacity :- 512 GB</li><li>Screen Size :- 6.83 inches</li><li>Resolution :- 2772 x 1280</li><li>Refresh Rate :- 120</li><li>Model Name :- Poco X8 Pro MAX 5G</li></ul><div>About this item :-</div><div><br></div><div><ul><li>USA MARKET ONLY WORK ON TMOBILE BOOST MINT TELLO OR ANY UNDER TMOBILE NETWORK PHONE NEEDS A SIM CARD ALREADY ACTIVATED ,OUTSIDE USA WORKS ANY GSM CARRIER SIM GSM FCC ID: 2AFZZPC0AG</li><li>Dual SIM (nano SIM + nano SIM or nano SIM + eSIM) (NO MICRO SD) 5G: 5G SA: n1/2/3/5/7/8/20/26/28/38/40/41/66*(70M)//77/78/485G NSA: n1/3/5/7/8/20/28/38/40/41/66*(70M)/77/784G LTE TDD: B38/40/41/42/484G LTE FDD: B1/2/3/4/5/7/8/18/19/20/26/28/66*(70M)/713G WCDMA: 1/2/4/5/6/8/192G GSM: 2/3/5/8Supports 4x4 MIMO*5G SA/NSA N66(70M) and 4G B66(70M): UL 1710MHz～1780MHz, DL 2110MHz～2180MHz*5G connectivity may vary based on region availability and local operator support.</li><li>6.83\" 1.5K 120Hz ultra-bright AMOLED displayResolution: 2772 x 1280Contrast ratio: 8,000,000:1Refresh rate: Up to 120Hz*Refresh rate can be adjusted to up to 120Hz for supported apps.Peak brightness: 3500 nits covering 25% display areaHBM brightness: 2000 nitsTypical brightness: 800 n: 2560Hz*480Hz touch sampling rate is activated under Game Turbo Mode.*2560Hz instant touch sampling rate is activated under Game Turbo Mode.12-bit color depth, 68 billion colorsDCI-P3 wide display 2.0HDR10+TÜV Rheinland Low Blue Light (Hardware Solution) CertifiedTÜV Rheinland Flicker Free CertifiedTÜV Rheinland Circadian Friendly CertifiedDolby VisionCorning Gorilla Glass 7i</li><li>MediaTek Dimensity 9500s3nm manufacturing process1 x Cortex-X925, up to 3.73GHz3 x Cortex-X4, up to 3.3GHz4 x Cortex-A720, up to 2.4GHzImmortalis-G925 MC11NPU 890 - 8500mAh (typ)100W HyperChargeUSB Type-C charging portUp to 27W reverse chargingSupports PD3.0/PD2.0Smart charging</li><li>Wi-Fi Protocol: Wi-Fi 7/ Wi-Fi 6/ Wi-Fi 5/ Wi-Fi 4 and 802.11a/b/g*Wi-Fi 7/Wi-Fi 6 capability may vary based on regional availability and local network support. Wi-Fi connectivity (including Wi-Fi frequency bands, Wi-Fi standards and other features as ratified in IEEE Standard 802.11 specifications) may vary based on regional availability and local network support. The function may be added via OTA when and where applicable.WLAN frequency: 2.4G Wi-Fi | 5G Wi-FiSupports Multi-Link Operation, 2x2 MIMO, 8x8 Sounding for MU-MIMO, Wi-Fi Direct, MiracastBluetooth 6.0, Dual-BluetoothSupports SBC / AAC / LDAC / LHDC 5.0 / LC3 / ASHA / AuracastNFC</li><li>50MP main cameraLight Fusion 600 image sensor1.6μm 4-in-1 Super Pixelf/1.51/1.95\" sensor size6P lensOIS8MP ultra-wide cameraf/2.2Rear camera photography featuresDynamic shotsMotion captureDocuments modeMotion tracking focusPortrait modePro mode50MP modeVoice shutterHDRPanoramaLong exposureTimed burstRear camera video recording4K (3840x2160) video recording at 30 fps, or 60 fps1080p (1920x1080) HD video recording at 30 fps, or 60 fps720p (1280x720) HD video recording at 30 fpsSlow motion video:720p (1280x720) at 120fps / 240fps / 960fps1080p (1920x1080) at 120fps / 240fps / 960fpsRear camera video featuresSlow</li><li>Ultrasonic in-display fingerprint sensorAI Face Unlock Face Unlock Proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | Gyroscope | IR blaster | Flicker sensor | X-axis linear vibration motorBeidou: B1I+B1C+B2a | GPS: L1+L5 | Galileo: E1+E5a | GLONASS: G1 | QZSS: L1+L5 | NavIC: L5AGNSS | Data network | Wireless network | Sensor Assisted Positioning</li><li>Beidou: B1I+B1C+B2a+B2b | GPS: L1+L5 | Galileo: E1+E5a+E5b | GLONASS: G1 | QZSS: L1+L5 | NavIC: L1+L5AGNSS | Data network | Wireless network | Sensor Assisted Positioning Proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | Gyroscope | IR blaster | Flicker sensor | X-axis linear vibration motor</li></ul></div>',34,11,1,5,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (14,'2026-10-11 15:01:40.904646','2026-10-11 15:01:40.904646','UMIDIGI G9 5G Cell Phone, Android 14 Unlocked Phones, 6.75\" HD+ 90Hz Display，12(6+6) GB +128GB/1TB TF Expand, 5G Dual SIM, 50MP Camera, AI Face & Fingerprint Unlock, 5000mAh Android Phone Black','<ol><li>Brand :- UMIDIGI</li><li>Operating System :- Android 14</li><li>Ram Memory Installed Size :- 6 GB</li><li>Memory Storage Capacity :- 128 GB</li><li>Screen Size :- 6.75 inches</li><li>Refresh Rate :- 90</li><li>Model Name :- G9 5G</li><li>Wireless Carrier :- Unlocked for All Carriers</li><li>Cellular Technology :- 5G</li><li>Connectivity Technology :- 5G<br><br>About this item :-&nbsp;<br></li></ol><ul><li>Android: UMIDIGI G9 5G unlocked cell phone runs with android 14 system. Which makes your device more personal, protected, and accessible with the newest OS upgrade.Improved photo quality, latest themes, and AI-generated wallpapers</li><li>Octa-Core 5G Processor: This unlocked android phone is equipped with the powerful 8-core CPU UNISOC T765 5G processor and utilizes a 6nm manufacturing process, which enhances its performance in multitasking and processing complex data simultaneously</li><li>12GB RAM+128GB ROM: G9 5G unlocked phone comes with 12GB RAM (6GB RAM + 6GB virtual RAM) and 128GB ROM (expandable up to 1TB), plenty of storage space to suit your needs.This dual sim phones can use nano-SIM cards and is equipped with dual SIM card slots</li><li>6.75inch+ 90Hz Display: UMIDIGI G9 5G unlocked cell phones 5g features a 6.75-inch large display, with a smooth 90 Hz refresh rate; with a 20:9 aspect ratio, providing a more comfortable viewing experience</li><li>50MP+8MP+2MP Camera: The G9 5G smartphone boasts a professional-grade camera system with 50MP ultra-clear main camera, 8MP front comera,and 2MP macro camera; three distinct lenses capturing your beautiful moments flawlessly</li></ul>',35,1,2,3,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (15,'2026-10-11 15:18:29.520988','2026-10-11 15:18:29.520988','Samsung Galaxy A37 5G (2026), Unlocked Android Smartphone, 128GB, AMOLED Display, Triple Camera, IP68, Slim Phone, 5000mAh Battery, US Version, US 1 Yr Warranty, Awesome Lavender','<ul><li>Brand :- Samsung</li><li>Operating System :- Android 16, ONEUI 8.5</li><li>Ram Memory Installed Size :- 6 GB</li><li>CPU Model :- Exynos 1480 S5E8845</li><li>CPU Speed :- 2.75 GHz</li><li>Memory Storage Capacity :- 256 GB</li><li>Screen Size :- 6.7 inches</li><li>Resolution :- 1080 x 2340</li><li>Refresh Rate :- 120</li><li>Model Name :- Galaxy A37</li></ul>About this item :-&nbsp;<br><br><ul><li>AWESOME SCREEN. ENDLESS POSSIBILITIES: Elevate your content no matter where you are with the 6.7\" Super AMOLED display¹ of the Galaxy A37 5G. Enjoy massive detail and stunning clarity on the go</li><li>LONG-LASTING BATTERY: Whether it’s family moments or important calls, stay powered without the wait with Galaxy A37 5G. Super Fast Charging 2.0² ensures you’re back up and running quickly</li><li>NEVER MISS THE SHOT, EVEN IN LOW LIGHT: With effortless Nightography, you can capture clear high-quality photos and videos on your phone automatically without adjusting manual settings</li><li>PRO SHOTS, NO PROBLEM: Capture every moment effortlessly on Galaxy A37 5G with the advanced triple-lens camera system featuring a 50MP HDR main camera and a studio-grade front camera</li><li>MORE POWER. MORE AWESOME: Enjoy smooth performance on Galaxy A37 5G. From detailed graphics while gaming to quick app launches and effortless multitasking, the Exynos 1480 keeps everything running</li><li>AWESOMELY DURABLE: Experience amazing durability and value with the water-, dirt- and dust-resistant Galaxy A37 5G.³ Power through your day with unstoppable confidence</li><li>AWESOME SECURITY: Galaxy A37 5G is built to last with 6 OS and 6 years of security updates⁴, keeping it protected, efficient and always up to date</li></ul>',36,12,1,4,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (16,'2026-10-11 15:23:00.123194','2026-10-11 15:23:00.123194','Samsung Galaxy A16 4G LTE (128GB + 4GB) International Model SM-A165F/DS Factory Unlocked, 6.7\", Dual SIM, 50MP Triple Camera (Case Bundle), Black','<div><ul><li>Brand :- Samsung</li><li>Operating System :- Android</li><li>Ram Memory Installed Size :- 4 GB</li><li>CPU Model :- MediaTek Helio</li><li>CPU Speed :- 2 GHz</li><li>Memory Storage Capacity :- 128 GB</li><li>Screen Size :- 6.7 inches</li><li>Resolution :- 1080 x 2340</li><li>Refresh Rate :- 90</li><li>Model Name :- Samsung Galaxy A16</li></ul></div><div>About this item :-<br><br></div><div><ul><li>Please note, this device does not support E-SIM; This 4G model is compatible with all GSM networks worldwide outside of the U.S. In the US, ONLY compatible with T-Mobile and their MVNO\'s (Metro and Standup). It will NOT work with other CDMA carriers, and it is also not compatible with their MVNO (Visible, Xfinity Mobile, US Mobile, Cricket Wireless, etc).</li><li>Compatibility with certain third-party devices and accessibility accessories, including some hearing aids, may vary depending on manufacturer support, Bluetooth protocols, software compatibility, and regional firmware limitations. For additional hearing aid compatibility information, please refer to Samsung’s official support documentation.</li><li>Camera: 50 MP, f/1.8, (wide), 1/2.76\", 0.64µm, AF | 50 MP, f/1.8, (wide), 1/2.76\", 0.64µm, AF | 2 MP, f/2.4, (macro). Battery: 5000 mAh, non-removable | A power adapter is NOT included.</li></ul></div>',37,1,2,3,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (17,'2026-10-11 15:35:25.321474','2026-10-11 15:35:25.321474','Google Pixel 10 Pro - Unlocked Smartphone with Gemini - Moonstone - 128 GB','<div><ul><li>Brand :- Google</li><li>Operating System :- Android 16</li><li>Ram Memory Installed Size :- 16 GB</li><li>CPU Model :- Google Tensor</li><li>CPU Speed :- 3.78 GHz</li><li>Memory Storage Capacity :- 128 GB</li><li>Screen Size :- 6.3 inches</li><li>Resolution :- 1280 x 2856 pixels</li><li>Refresh Rate :- 120</li><li>Model Name :- Pixel 10 Pro</li></ul></div><div>About this item :-<br><br></div><div><ul><li>Google Pixel 10 Pro is the ultimate Pixel experience, featuring advanced AI with Gemini, unbelievable camera quality, impeccable design in two sizes, and the next-gen Google Tensor G5 chip[1]</li><li>Unlocked Android phone gives you the flexibility to change carriers and choose your own data plan[2]; it works - Google Fi, Verizon, T-Mobile, AT&amp;T, and other major carriers</li><li>Get a head start on syncing your data before it even arrives: After you purchase your new Pixel, look for an email that explains how to transfer your photos, videos, passwords, and more in just a few quick steps[11]</li><li>Pixel’s pro camera system makes everything look amazing, even in low light; capture more of the scene with advanced Google AI models, and bring out incredible details with 100x Pro Res Zoom, stunning 50 MP images, and super steady videos in 8K[10]</li><li>Pixel 10 Pro is built with durable aluminum and Corning Gorilla Glass Victus 2 for scratch and drop resistance; the 6.3-inch Super Actua display with 3,300-nit peak brightness is easy on the eyes, even in direct sunlight[3,13,18]</li></ul></div>',38,13,1,3,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (18,'2026-10-11 15:55:53.977674','2026-10-11 15:55:53.977674','Google Pixel 9 Pro XL - Unlocked Android Smartphone with Gemini, Triple Rear Camera System, 24-Hour Battery, and 6.8\" Super Actua Display - Porcelain - 128 GB','<div><ul><li>Brand :- Google</li><li>Operating System :- Android 14</li><li>Ram Memory Installed Size :- 16 GB</li><li>CPU Model :- Unknown</li><li>CPU Speed :- 3.05 GHz</li><li>Memory Storage Capacity :- 128 GB</li><li>Screen Size :- 6.8 inches</li><li>Resolution :- 1440 x 2560</li><li>Refresh Rate :- 120</li><li>Model Name :- Google Pixel 9 Pro XL</li></ul></div><div>About this item :-<br><br></div><div><ul><li>Pixel 9 Pro XL with Gemini is the most powerful Pixel yet, and now comes in two sizes; it has a pro-level camera and it’s engineered by Google, so it gets the best of Google AI first</li><li>Unlocked Android phone gives you the flexibility to change carriers and choose your own data plan[1]; it works with Google Fi, Verizon, T-Mobile, AT&amp;T, and other major carriers</li><li>Google Pixel 9 Pro’s triple rear camera is the most powerful Pixel Camera yet; get high-quality video up to 20x with Super Res Zoom Video[2]; enhance your videos up to 8K with Video Boost; and take sharp videos in low light with Night Sight Video</li><li>Make your photos better than you can imagine with Google’s AI; take a picture and be in it too with Add Me[4]; Best Take helps everyone look their best[2]; and with Magic Editor, you can reframe photos, reimagine the scenery, and more[4]</li><li>Get more info quickly with Gemini, your built-in AI assistant[3]; instead of typing, use Gemini Live; it follows along even if you change the topic or switch the question[31]; and Pixel Screenshots helps you save things you’ll want to remember later</li></ul></div>',39,14,1,3,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (19,'2026-10-11 15:59:24.944945','2026-10-11 15:59:24.944945','UMIDIGI Unlocked Cell Phone, G5A MTK Helio A22 4+4GB +64GB Android Smartphone Unlocked with 6.52\" Full Screen, 13MP+2MP+5MP AI Triple Camera, 5000mAh Battery Fast Charging Mobile Phones,Purple','<ul><li>Brand :- UMIDIGI</li><li>Operating System :- Android</li><li>Ram Memory Installed Size :- 4 GB</li><li>CPU Model :- MediaTek Kompanio 828</li><li>CPU Speed :- 2 GHz</li><li>Memory Storage Capacity :- 64 GB</li><li>Screen Size :- 6.52 inches</li><li>Resolution :- 1600 x 720</li><li>Refresh Rate :-&nbsp; 60</li><li>Model Name :- G5A<br><br>About this item :-<br><br></li><li>【 Octa-Core Processor】G5A unlock cell phones are powered by Octa-Core Processor, support 4GB +64GB memory. UMIDIGI G5A unlocked smartphone can provide you with powerful device control and smoother operation. Featuring an advanced Processor with an octa-core 4xARM-A53 2.0GHz. Performance has been significantly improved with the upgraded configuration.</li><li>【4GB +64GB】G5A unlocked smartphones support 4GB RAM+64GB ROM. With \"Memory Expansion\", you can expand your ROM up to 1TB, multitasking will run very smoothly at the same time. With 256GB ROM, bid farewell to memory anxiety, massive photos, music, videos, and storage as you like.</li><li>【13MP+2MP+5MP Camera】13MP primary camera +2MP depth camera +5MP front camera, G5A smartphones can record the magnificent scenery in front of you, expand your horizons and show your beauty. Steady handheld night photography, Macro mode, portrait mode, HDR, AI beautify, face recognition, real-time filters, side-fingerprint sensor. Superior technology showing your subject more clearly.</li><li>【6.52-inch screen &amp; Ultra-thin body】 G5A unlock smartphone is 8.8mm thick and weighs only 195 grams. It is compact and portable with USB Type-C connectivity, a large 6.52-inch screen, and 1600*720 HD+ resolution. With FHD+ display with 60Hz refresh rate, the screen looks wider and can be used comfortably.</li><li>【Android &amp; 5000mAh Battery】G5A unlocked cell phones support Android, equipped with a 5000mAh battery and a 6.52-inch mobile phone screen, support dual SIM card, bluetooth 5.0, GPS. With Wi-Fi 2.4G&amp;5G, the device increased efficiency, reliability, and security.The side fingerprint sensor and AI face unlock provide you with easier and instant unlocking while keeping your information safe.</li></ul>',40,9,2,2,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (20,'2026-10-11 16:05:18.127730','2026-10-11 16:05:18.127730','XIAOMI Redmi Note 15 5G Ai (Compatible with Tmobile Mint Tello Global) (256GB + 8GB) 6.77\" NFC 120Hz 108MP Triple Cam Unlocked Model 25098RA98G Dual Sim (Mist Purple)','<div><ul><li>Brand :- Xiaomi</li><li>Operating System :- Hyper OS Based on Android 14</li><li>Ram Memory Installed Size :- 8 GB</li><li>CPU Model :- Snapdragon</li><li>CPU Speed :- 2.4 GHz</li><li>Memory Storage Capacity :- 256 GB</li><li>Screen Size :- 6.77 inches</li><li>Resolution :- 2392 x 1080</li><li>Refresh Rate :- 120</li><li>Model Name :- Note 15 5g</li></ul></div><div>About this item :-&nbsp;<br><br></div><div><ul><li>USA MARKET ONLY WORK ON TMOBILE MINT TELLO OR ANY UNDER TMOBILE NETWORK PHONE NEEDS A SIM CARD ALREADY ACTIVATED ,OUTSIDE USA WORKS ANY GSM CARRIER SIM GSM FCC ID: 2AFZZRAD4G</li><li>nano SIM 1 + Hybrid (nano SIM or microSD) Dual standby , n1/2/3/5/7/8/12/20/26/28/38/40/41/48/66/77/78 4G : B1/2/3/4/5/7/8/12/13/17/18/19/20/26/28/32/66 4G: LTE TDD: B38/40/41/42/48 3G: WCDMA: Band 2/4/5/8 2G: GSM: Quad Band.</li><li>6.77\" AMOLED displayResolution: 2392 × 1080Refresh rate: Up to 120HzTouch sampling rate: 240HzBrightness: 3200 nits peak brightness, 800 nits (typ)Colour depth: 12 bitContrast ratio: 8,000,000:1DCI- P3 wide color gamutSunlight displayReading mode3840Hz PWM dimming｜16,000-level brightness adjustmentScreen-to-body ratio: 92%*Data obtained from Xiaomi Internal Labs. Actual results may vary.TÜV Rheinland Low Blue Light (Hardware Solution) Certified TÜV Rheinland Circadian Friendly Certified TÜV Rheinland Flicker Free Certified</li><li>Snapdragon 6 Gen 3 Mobile Platform4nm manufacturing process technologyCPU: Octa-core processor, up to 2.4GHzGPU: Adreno</li><li>Virtual proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | IR blaster | Gyroscope / GPS: L1 | GLONASS: G1 | BDS: B1I | Galileo E1 / Wireless NetworksBluetooth 5.3Wi-Fi Protocol: 802.11a/b/g/n/ac Supports 2.4GHz Wi-Fi | 5GHz Wi-Fi Wi-Fi Direct / In-screen fingerprint sensorAI Face Unlock</li><li>108MP main camera0.64μm, 9in1 1.92μmf/1.76P lens1/1.67\" sensor size8MP ultra-wide cameraf/2.2Rear camera video recording4K at 30fps1080p at 30fps720p at 30fps - Front Camera 20MP front camera1/4\'\' sensor sizef/2.24P lensFront camera video recording1080p at 30fps720p at 30fps</li><li>In-screen fingerprint sensorAI face unlock GPS: L1 | GLONASS: G1 | BDS: B1I + B1C | Galileo: E1 Virtual proximity sensor | Ambient light sensor | Accelerometer | Electronic compass | IR blaster | Gyroscope Xiaomi HyperOS 2 Bluetooth 5.1Wi-Fi Protocol: 802.11a/b/g/n/acSupports 2.4GHz Wi-Fi | 5GHz Wi-Fi Supports Wi-Fi Direct</li></ul></div>',41,15,3,4,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (21,'2026-10-11 16:10:07.217920','2026-10-11 16:10:07.217920','Motorola Moto Edge 60 Fusion 5G Ai (Compatible with Tmobile Mint Tello & Global) (256GB+8GB) NFC 50MP Triple Cam Dual Sim 6.67\" Curved XT2503-1 Latino American Version (Zephyr (Rose))','<div><ul><li>Brand :- Motorola</li><li>Operating System :- Android 15</li><li>Ram Memory Installed Size :- 8 GB</li><li>CPU Model :- Mediatek Dimensity 7300</li><li>CPU Speed :- 2.5 GHz</li><li>Memory Storage Capacity :- 256 GB</li><li>Screen Size :- 6.67 inches</li><li>Resolution :- 2712 x 1220</li><li>Refresh Rate :- 120</li><li>Model Name :- EDGE 60 Fusion 5G<br><br>About this item :-<br><br></li><li>Warning USA Market , Phone in Global Unlocked BUT Only works on Tmobile / Metro / Mint / Tello or Any under TMOBILE Network. OUTISE THE USA WORKS ANY CARRIER WORLDWIDE - SIM Card Dual SIM (/ Wi-Fi Wi-Fi 802.11 a/b/g/n/ac/ax / 2.4 GHz | 5 GHz | 6 GHz / Wi-Fi 6E Wi-Fi hotspot</li><li>Dual SIM (1 Nano SIM + eSIM) (NO MicroSD Slot) : 5G: NR n1/2/3/5/7/8/20/26/28/38/40/41/66/75/77/78 - 4G: 1/2/3/4/5/7/8/12/13/17/18/19/20/26/28/32/38/39/40/41/42/43/48/66 - 3G: B1/2/4/5/8 - 2G: B2/3/5/8</li><li>Processor MediaTek Dimensity 7300 Octa-core (4x2.5 GHz Cortex-A78 &amp; 4x2.0 GHz Cortex-A55) - Global / gpu Mali-G615 MC2 Security On-screen fingerprint reader Face Unlock ThinkShield Sensors Proximity Sensor Ambient Light Sensor Accelerometer Gyroscope Sar sensor Sensor Hub Magnetometer (e-Compass)</li><li>Rear Camera Hardware 50 MP Sony - LYTIA 700C sensor 2.0 µm Ultra Pixel 100% focus pixels f/1.88 aperture OIS Rear Camera Software Auto Smile Capture Portrait Mode (24mm, 35mm, 50mm) Photo Enhancement Engine Gesture Capture Auto Night Vision Dual Capture Face Retouch HDR Live Filters Macro Night Vision Panorama Pantone Skintone Validated13 Pantone Validated Color13 Google Photos editing features: Auto Enhance HDR Effect Magic Eraser Magic Editor Portrait Blur Photo Unblur Portrait Light Sky 32mp Front Camera</li><li>Display Size 6.67 quad-curved display Display Technology pOLED HDR10+ 100% DCI-P3 color space 10-bit, Over a billion shades of color Refresh rate up to 120Hz11 Peak Brightness: 4500 nits Touch sampling rate: up to 300Hz12 Water Touch Resolution Super HD (2712 × 1220) 446 dpi</li></ul></div>',42,16,1,4,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (22,'2026-10-11 16:24:26.105613','2026-10-11 16:24:26.105613','HONOR Magic V6 5G PNM-N49 (GSM Unlocked, International Version) 512GB Storage 16GB RAM Factory Unlocked Android 16 Foldable Smartphone (Red)','<div><ul><li>Brand :- HONOR</li><li>Operating System :- Android 16</li><li>Ram Memory Installed Size :- 16 GB</li><li>Memory Storage Capacity :- 16 GB</li><li>Screen Size :- 6.52 inches</li><li>Resolution :- 2352 x 2172</li><li>Model Name :- Magic V6</li><li>Wireless Carrier :- T-Mobile</li><li>Cellular Technology :- 5G</li><li>Connectivity Technology :- Bluetooth, NFC, Wi-Fi<br><br>About this item :-<br><br></li><li>For USA Buyers: Only work with T-Mobile in the US, as it is not optimized for Verizon and AT&amp;T. This means that as long as there\'s T-Mobile coverage, it\'ll work. For Intentional Buyers: Work outside of the USA with any network provider without any restrictions.</li><li>Inner screen: 7.95 inch, 2352 x 2172 Pixels, foldable OLED, 1.07 billion colors</li><li>External screen: 6.52 inch, 2420 x 1080 Pixels, OLED, 1.07 billion colors</li><li>512GB Storage, 16GB RAM</li><li>Qualcomm Snapdragon 8 Elite (Gen 5) Mobile Platform, Adreno 840</li><li>Rear Camera: 50MP + 50MP + 64MP telephoto Lens, Front Camera: 20MP + 20MP</li><li>6660 mAh (typical value) Lithium polymer battery</li><li>Wi-Fi, Bluetooth 6.0, NFC, Nano-SIM, eSIM</li><li>Android 16 (MagicOS 10)</li><li>Dimensions: 156.7 x 74.5 x 8.75 mm (Folded) / 156.7 x 145.6 x 4.0 mm (Unfolded), Weight: 219 g</li></ul></div>',43,6,1,5,2);
INSERT INTO `product` (`id`, `created_at`, `updated_at`, `title`, `description`, `model_id`, `color_id`, `quality_id`, `storage_id`, `seller_id`) VALUES (23,'2026-10-11 16:27:49.256823','2026-10-11 16:27:49.256823','Nothing Phone (3a) Pro 5G Smartphone, 12+256GB, Grey','<ul><li>Brand :- Nothing</li><li>Operating System :- Android 15</li><li>Memory Storage Capacity :- 12 GB</li><li>Screen Size :- 6.75 inches</li><li>Refresh Rate :- 120 hertz</li><li>Model Name :- Phone (3a) Pro</li><li>Wireless Carrier :- Unlocked</li><li>Cellular Technology :- 5G</li><li>Connectivity Technology :- Bluetooth, NFC, USB, Wi-Fi</li><li>Color :- Grey</li></ul><div>About this item :-&nbsp;<br><br></div><div><ul><li>Pro Triple 50MP Camera &amp; 60x Ultra Zoom: Unleash professional mobile photography with a 50MP periscope lens, featuring 3x optical zoom and 60x ultra zoom. OIS stabilization and the AI TrueLens Engine bring ultimate photo clarity to live concerts, sports, and wildlife—delivering breathtaking depth and natural light-shadow details</li><li>Ultra-Bright 120Hz Eye-Care AMOLED: The 6.77\" flexible AMOLED screen reaches a peak 3,000 nits for seamless sunlight outdoor visibility. Featuring an adaptive 120Hz refresh rate and high-frequency 2160Hz PWM dimming, it ensures buttery-smooth visuals while minimizing eye strain during long gaming or viewing sessions</li><li>Smart Glyph Interface &amp; Essential Key: Assign unique light and sound patterns to key contacts or notifications to minimize screen distractions. The physical Essential Key offers instant, one-press access to local voice memos, quick screenshots, and task management</li><li>Snapdragon 7s Gen 3 &amp; Pro Endurance: Powered by a 4nm Qualcomm chip, this phone handles heavy gaming effortlessly. Paired with a massive 5,000mAh battery and 50W fast charging (50% in 20 mins), it balances peak performance with up to two days of battery life</li><li>Broad 5G Connectivity &amp; Compatibility: T-Mobile: 3G/4G/5G supported (not all 5G). AT&amp;T: 3G/4G/5G supported (not all 5G). Verizon: Not Recommended; requires manual IMEI registration before use. SIM: Dual SIM (Nano-SIM + eSIM). 5G NR: N1/2/3/5/7/8/12/20/28/38/40/ 41/48/66/77/78. Note: VoLTE/VoWiFi and 5G service availability depend on carrier policy</li></ul></div>',44,4,1,4,2);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `pr_id` int NOT NULL,
  `images` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `fk_product_images_product` (`pr_id`),
  CONSTRAINT `fk_product_images_product` FOREIGN KEY (`pr_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (4,'http://localhost:8080/smarttrade/uploads/product/4/1763915055843.png');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (4,'http://localhost:8080/smarttrade/uploads/product/4/1763915055852.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (4,'http://localhost:8080/smarttrade/uploads/product/4/1763915055856.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (5,'http://localhost:8080/smarttrade/uploads/product/5/1763915242697.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (5,'http://localhost:8080/smarttrade/uploads/product/5/1763915242699.png');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (5,'http://localhost:8080/smarttrade/uploads/product/5/1763915242703.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (6,'http://localhost:8082/smarttrade/uploads/product/6/1791709038880.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (6,'http://localhost:8082/smarttrade/uploads/product/6/1791709038882.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (6,'http://localhost:8082/smarttrade/uploads/product/6/1791709038884.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (6,'http://localhost:8082/smarttrade/uploads/product/6/1791709038887.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (6,'http://localhost:8082/smarttrade/uploads/product/6/1791709038888.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (6,'http://localhost:8082/smarttrade/uploads/product/6/1791709038889.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (6,'http://localhost:8082/smarttrade/uploads/product/6/1791709038890.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (6,'http://localhost:8082/smarttrade/uploads/product/6/1791709038891.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (8,'http://localhost:8082/smarttrade/uploads/product/8/1791709460937.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (8,'http://localhost:8082/smarttrade/uploads/product/8/1791709460940.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (8,'http://localhost:8082/smarttrade/uploads/product/8/1791709460942.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (8,'http://localhost:8082/smarttrade/uploads/product/8/1791709460943.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (8,'http://localhost:8082/smarttrade/uploads/product/8/1791709460944.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (8,'http://localhost:8082/smarttrade/uploads/product/8/1791709460946.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (8,'http://localhost:8082/smarttrade/uploads/product/8/1791709460949.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (8,'http://localhost:8082/smarttrade/uploads/product/8/1791709460950.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (9,'http://localhost:8082/smarttrade/uploads/product/9/1791709677163.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (9,'http://localhost:8082/smarttrade/uploads/product/9/1791709677167.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (9,'http://localhost:8082/smarttrade/uploads/product/9/1791709677169.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (9,'http://localhost:8082/smarttrade/uploads/product/9/1791709677171.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (9,'http://localhost:8082/smarttrade/uploads/product/9/1791709677174.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (9,'http://localhost:8082/smarttrade/uploads/product/9/1791709677175.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (9,'http://localhost:8082/smarttrade/uploads/product/9/1791709677178.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (9,'http://localhost:8082/smarttrade/uploads/product/9/1791709677180.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (10,'http://localhost:8082/smarttrade/uploads/product/10/1791710000990.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (10,'http://localhost:8082/smarttrade/uploads/product/10/1791710000992.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (10,'http://localhost:8082/smarttrade/uploads/product/10/1791710000994.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (10,'http://localhost:8082/smarttrade/uploads/product/10/1791710000997.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (10,'http://localhost:8082/smarttrade/uploads/product/10/1791710000999.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (10,'http://localhost:8082/smarttrade/uploads/product/10/1791710001002.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (10,'http://localhost:8082/smarttrade/uploads/product/10/1791710001005.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (10,'http://localhost:8082/smarttrade/uploads/product/10/1791710001007.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (11,'http://localhost:8082/smarttrade/uploads/product/11/1791710493784.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (11,'http://localhost:8082/smarttrade/uploads/product/11/1791710493787.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (11,'http://localhost:8082/smarttrade/uploads/product/11/1791710493789.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (11,'http://localhost:8082/smarttrade/uploads/product/11/1791710493792.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (11,'http://localhost:8082/smarttrade/uploads/product/11/1791710493793.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (11,'http://localhost:8082/smarttrade/uploads/product/11/1791710493796.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (11,'http://localhost:8082/smarttrade/uploads/product/11/1791710493799.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (12,'http://localhost:8082/smarttrade/uploads/product/12/1791710657339.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (12,'http://localhost:8082/smarttrade/uploads/product/12/1791710657341.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (12,'http://localhost:8082/smarttrade/uploads/product/12/1791710657343.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (12,'http://localhost:8082/smarttrade/uploads/product/12/1791710657346.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (12,'http://localhost:8082/smarttrade/uploads/product/12/1791710657348.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (12,'http://localhost:8082/smarttrade/uploads/product/12/1791710657349.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (12,'http://localhost:8082/smarttrade/uploads/product/12/1791710657352.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (12,'http://localhost:8082/smarttrade/uploads/product/12/1791710657354.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (13,'http://localhost:8082/smarttrade/uploads/product/13/1791710797324.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (13,'http://localhost:8082/smarttrade/uploads/product/13/1791710797326.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (13,'http://localhost:8082/smarttrade/uploads/product/13/1791710797328.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (13,'http://localhost:8082/smarttrade/uploads/product/13/1791710797329.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (13,'http://localhost:8082/smarttrade/uploads/product/13/1791710797331.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (13,'http://localhost:8082/smarttrade/uploads/product/13/1791710797333.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (13,'http://localhost:8082/smarttrade/uploads/product/13/1791710797334.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (14,'http://localhost:8082/smarttrade/uploads/product/14/1791711100941.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (14,'http://localhost:8082/smarttrade/uploads/product/14/1791711100944.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (14,'http://localhost:8082/smarttrade/uploads/product/14/1791711100946.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (14,'http://localhost:8082/smarttrade/uploads/product/14/1791711100949.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (14,'http://localhost:8082/smarttrade/uploads/product/14/1791711100951.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (14,'http://localhost:8082/smarttrade/uploads/product/14/1791711100953.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (14,'http://localhost:8082/smarttrade/uploads/product/14/1791711100956.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (15,'http://localhost:8082/smarttrade/uploads/product/15/1791712109574.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (15,'http://localhost:8082/smarttrade/uploads/product/15/1791712109576.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (15,'http://localhost:8082/smarttrade/uploads/product/15/1791712109580.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (15,'http://localhost:8082/smarttrade/uploads/product/15/1791712109582.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (15,'http://localhost:8082/smarttrade/uploads/product/15/1791712109585.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (15,'http://localhost:8082/smarttrade/uploads/product/15/1791712109586.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (15,'http://localhost:8082/smarttrade/uploads/product/15/1791712109591.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (15,'http://localhost:8082/smarttrade/uploads/product/15/1791712109594.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (16,'http://localhost:8082/smarttrade/uploads/product/16/1791712380158.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (16,'http://localhost:8082/smarttrade/uploads/product/16/1791712380159.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (16,'http://localhost:8082/smarttrade/uploads/product/16/1791712380165.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (16,'http://localhost:8082/smarttrade/uploads/product/16/1791712380167.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (16,'http://localhost:8082/smarttrade/uploads/product/16/1791712380169.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (16,'http://localhost:8082/smarttrade/uploads/product/16/1791712380172.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (16,'http://localhost:8082/smarttrade/uploads/product/16/1791712380176.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (16,'http://localhost:8082/smarttrade/uploads/product/16/1791712380181.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (17,'http://localhost:8082/smarttrade/uploads/product/17/1791713125358.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (17,'http://localhost:8082/smarttrade/uploads/product/17/1791713125360.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (17,'http://localhost:8082/smarttrade/uploads/product/17/1791713125363.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (17,'http://localhost:8082/smarttrade/uploads/product/17/1791713125365.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (17,'http://localhost:8082/smarttrade/uploads/product/17/1791713125367.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (17,'http://localhost:8082/smarttrade/uploads/product/17/1791713125369.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (17,'http://localhost:8082/smarttrade/uploads/product/17/1791713125374.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (17,'http://localhost:8082/smarttrade/uploads/product/17/1791713125377.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (18,'http://localhost:8082/smarttrade/uploads/product/18/1791714354010.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (18,'http://localhost:8082/smarttrade/uploads/product/18/1791714354011.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (18,'http://localhost:8082/smarttrade/uploads/product/18/1791714354013.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (18,'http://localhost:8082/smarttrade/uploads/product/18/1791714354014.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (18,'http://localhost:8082/smarttrade/uploads/product/18/1791714354015.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (18,'http://localhost:8082/smarttrade/uploads/product/18/1791714354016.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (18,'http://localhost:8082/smarttrade/uploads/product/18/1791714354020.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (18,'http://localhost:8082/smarttrade/uploads/product/18/1791714354022.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (19,'http://localhost:8082/smarttrade/uploads/product/19/1791714564982.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (19,'http://localhost:8082/smarttrade/uploads/product/19/1791714564983.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (19,'http://localhost:8082/smarttrade/uploads/product/19/1791714564984.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (19,'http://localhost:8082/smarttrade/uploads/product/19/1791714564986.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (19,'http://localhost:8082/smarttrade/uploads/product/19/1791714564989.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (19,'http://localhost:8082/smarttrade/uploads/product/19/1791714564994.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (20,'http://localhost:8082/smarttrade/uploads/product/20/1791714918155.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (20,'http://localhost:8082/smarttrade/uploads/product/20/1791714918156.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (20,'http://localhost:8082/smarttrade/uploads/product/20/1791714918157.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (20,'http://localhost:8082/smarttrade/uploads/product/20/1791714918160.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (21,'http://localhost:8082/smarttrade/uploads/product/21/1791715207239.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (21,'http://localhost:8082/smarttrade/uploads/product/21/1791715207242.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (21,'http://localhost:8082/smarttrade/uploads/product/21/1791715207243.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (21,'http://localhost:8082/smarttrade/uploads/product/21/1791715207244.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (22,'http://localhost:8082/smarttrade/uploads/product/22/1791716066151.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (22,'http://localhost:8082/smarttrade/uploads/product/22/1791716066155.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (22,'http://localhost:8082/smarttrade/uploads/product/22/1791716066159.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (22,'http://localhost:8082/smarttrade/uploads/product/22/1791716066161.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (22,'http://localhost:8082/smarttrade/uploads/product/22/1791716066163.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (22,'http://localhost:8082/smarttrade/uploads/product/22/1791716066165.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (22,'http://localhost:8082/smarttrade/uploads/product/22/1791716066170.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (22,'http://localhost:8082/smarttrade/uploads/product/22/1791716066173.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (23,'http://localhost:8082/smarttrade/uploads/product/23/1791716269286.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (23,'http://localhost:8082/smarttrade/uploads/product/23/1791716269288.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (23,'http://localhost:8082/smarttrade/uploads/product/23/1791716269290.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (23,'http://localhost:8082/smarttrade/uploads/product/23/1791716269291.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (23,'http://localhost:8082/smarttrade/uploads/product/23/1791716269293.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (23,'http://localhost:8082/smarttrade/uploads/product/23/1791716269295.jpg');
INSERT INTO `product_images` (`pr_id`, `images`) VALUES (23,'http://localhost:8082/smarttrade/uploads/product/23/1791716269297.jpg');
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quality`
--

DROP TABLE IF EXISTS `quality`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quality` (
  `id` int NOT NULL AUTO_INCREMENT,
  `value` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quality`
--

LOCK TABLES `quality` WRITE;
/*!40000 ALTER TABLE `quality` DISABLE KEYS */;
INSERT INTO `quality` (`id`, `value`) VALUES (1,'Brand New');
INSERT INTO `quality` (`id`, `value`) VALUES (2,'Like New');
INSERT INTO `quality` (`id`, `value`) VALUES (3,'Used - Good');
INSERT INTO `quality` (`id`, `value`) VALUES (4,'Refurbished');
/*!40000 ALTER TABLE `quality` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seller`
--

DROP TABLE IF EXISTS `seller`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `company_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `company_mobile` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `company_email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_seller_user` (`user_id`),
  KEY `fk_seller_status` (`status_id`),
  CONSTRAINT `fk_seller_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  CONSTRAINT `fk_seller_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seller`
--

LOCK TABLES `seller` WRITE;
/*!40000 ALTER TABLE `seller` DISABLE KEYS */;
INSERT INTO `seller` (`id`, `created_at`, `updated_at`, `company_name`, `company_mobile`, `company_email`, `status_id`, `user_id`) VALUES (1,'2026-10-11 13:50:01.319296','2026-10-11 13:50:01.319296','SmartTrade Demo Store','0771234567','store@smarttrade.lk',7,1);
INSERT INTO `seller` (`id`, `created_at`, `updated_at`, `company_name`, `company_mobile`, `company_email`, `status_id`, `user_id`) VALUES (2,'2026-10-11 14:15:43.220622','2026-10-11 14:15:43.220622','Isuru Rathnayaka','0727938613','isururathnayaka999@gmail.com',7,7);
INSERT INTO `seller` (`id`, `created_at`, `updated_at`, `company_name`, `company_mobile`, `company_email`, `status_id`, `user_id`) VALUES (3,'2026-10-11 17:54:53.509246','2026-10-11 17:54:53.509246','Isuru Rathnayaka','0727938613','isururathnayakayt999@gmail.com',7,8);
/*!40000 ALTER TABLE `seller` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status`
--

DROP TABLE IF EXISTS `status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `status` (
  `id` int NOT NULL AUTO_INCREMENT,
  `value` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_status_value` (`value`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` (`id`, `value`) VALUES (1,'ACTIVE');
INSERT INTO `status` (`id`, `value`) VALUES (7,'APPROVED');
INSERT INTO `status` (`id`, `value`) VALUES (4,'BLOCKED');
INSERT INTO `status` (`id`, `value`) VALUES (9,'CANCELED');
INSERT INTO `status` (`id`, `value`) VALUES (12,'COMPLETED');
INSERT INTO `status` (`id`, `value`) VALUES (5,'DELIVERED');
INSERT INTO `status` (`id`, `value`) VALUES (3,'INACTIVE');
INSERT INTO `status` (`id`, `value`) VALUES (6,'PACKING');
INSERT INTO `status` (`id`, `value`) VALUES (2,'PENDING');
INSERT INTO `status` (`id`, `value`) VALUES (11,'RECEIVED');
INSERT INTO `status` (`id`, `value`) VALUES (8,'REJECTED');
INSERT INTO `status` (`id`, `value`) VALUES (10,'VERIFIED');
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock`
--

DROP TABLE IF EXISTS `stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `price` double NOT NULL,
  `qty` int NOT NULL,
  `product_id` int DEFAULT NULL,
  `discount_id` int DEFAULT NULL,
  `status_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_stock_product` (`product_id`),
  KEY `fk_stock_discount` (`discount_id`),
  KEY `fk_stock_status` (`status_id`),
  CONSTRAINT `fk_stock_discount` FOREIGN KEY (`discount_id`) REFERENCES `discount` (`id`),
  CONSTRAINT `fk_stock_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_stock_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock`
--

LOCK TABLES `stock` WRITE;
/*!40000 ALTER TABLE `stock` DISABLE KEYS */;
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (1,'2026-10-11 13:50:01.385087','2026-10-11 13:50:01.385087',54900,12,4,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (2,'2026-10-11 13:50:01.391390','2026-10-11 13:50:01.391390',42500,5,5,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (3,'2026-10-11 14:27:18.839381','2026-10-11 14:27:18.839381',140000,5,6,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (5,'2026-10-11 14:34:20.907780','2026-10-11 14:34:20.907780',168000,6,8,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (6,'2026-10-11 14:37:57.124081','2026-10-11 14:37:57.124081',168000,6,9,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (7,'2026-10-11 14:43:20.957306','2026-10-11 14:43:20.957306',90000,5,10,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (8,'2026-10-11 14:51:33.754845','2026-10-11 14:51:33.754845',185000,5,11,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (9,'2026-10-11 14:54:17.308978','2026-10-11 14:54:17.308978',185500,4,12,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (10,'2026-10-11 14:56:37.290125','2026-10-11 14:56:37.290125',186000,2,13,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (11,'2026-10-11 15:01:40.906160','2026-10-11 15:01:40.906160',36500,1,14,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (12,'2026-10-11 15:18:29.522989','2026-10-11 15:18:29.522989',200000,5,15,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (13,'2026-10-11 15:23:00.124204','2026-10-11 15:23:00.124204',47500,1,16,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (14,'2026-10-11 15:35:25.324479','2026-10-11 15:35:25.324479',318000,5,17,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (15,'2026-10-11 15:55:53.978680','2026-10-11 15:55:53.978680',245000,2,18,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (16,'2026-10-11 15:59:24.945944','2026-10-11 15:59:24.945944',25000,2,19,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (17,'2026-10-11 16:05:18.130352','2026-10-11 16:05:18.130352',94000,1,20,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (18,'2026-10-11 16:10:07.219460','2026-10-11 16:10:07.219460',119900,5,21,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (19,'2026-10-11 16:24:26.106675','2026-10-11 18:53:58.560340',645000,5,22,1,7);
INSERT INTO `stock` (`id`, `created_at`, `updated_at`, `price`, `qty`, `product_id`, `discount_id`, `status_id`) VALUES (20,'2026-10-11 16:27:49.259336','2026-10-11 18:50:08.058031',119000,7,23,1,7);
/*!40000 ALTER TABLE `stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage`
--

DROP TABLE IF EXISTS `storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `storage` (
  `id` int NOT NULL AUTO_INCREMENT,
  `value` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage`
--

LOCK TABLES `storage` WRITE;
/*!40000 ALTER TABLE `storage` DISABLE KEYS */;
INSERT INTO `storage` (`id`, `value`) VALUES (1,'32 GB');
INSERT INTO `storage` (`id`, `value`) VALUES (2,'64 GB');
INSERT INTO `storage` (`id`, `value`) VALUES (3,'128 GB');
INSERT INTO `storage` (`id`, `value`) VALUES (4,'256 GB');
INSERT INTO `storage` (`id`, `value`) VALUES (5,'512 GB');
INSERT INTO `storage` (`id`, `value`) VALUES (6,'1 TB');
/*!40000 ALTER TABLE `storage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `first_name` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `verification_code` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status_id` int NOT NULL,
  `profile_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_admin` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_email` (`email`),
  KEY `fk_users_status` (`status_id`),
  CONSTRAINT `fk_users_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (1,'2026-10-11 13:50:01.287804','2026-10-11 13:50:01.287804','Demo','Seller','demo@smarttrade.lk','demo@123','',10,NULL,_binary '\0');
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (2,'2026-10-11 13:50:01.296206','2026-10-11 18:07:16.979502','Admin','User','admin@gmail.com','admin@123','',10,NULL,_binary '');
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (3,'2026-10-11 13:58:19.299058','2026-10-11 18:57:03.863386','TestFirst','TestLast','testunique1791707299240@gmail.com','Password@123','183028',4,NULL,_binary '\0');
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (4,'2026-10-11 13:58:19.361786','2026-10-11 18:57:02.568201','TestFirst','TestLast','testunique1791707299457@gmail.com','Password@123','909880',4,NULL,_binary '\0');
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (5,'2026-10-11 14:04:40.999195','2026-10-11 18:57:01.880098','SameName','SameFamily','user1_1791707679299@gmail.com','Pass_1234','544550',4,NULL,_binary '\0');
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (6,'2026-10-11 14:04:41.081166','2026-10-11 18:41:06.319027','SameName','SameFamily','user2_1791707681078@gmail.com','Pass_1234','242799',10,NULL,_binary '\0');
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (7,'2026-10-11 14:14:44.114218','2026-10-11 14:20:30.414235','Isuru','Rathnayaka','isururathnayaka999@gmail.com','I@suru2005110','',10,'/smarttrade/uploads/profile/7/1791708630404.jpg',_binary '\0');
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (8,'2026-10-11 16:44:05.339950','2026-10-11 16:45:25.844189','Isuru','Rathnayaka','isururathnayakayt999@gmail.com','I@suru2005110','',10,NULL,_binary '\0');
INSERT INTO `users` (`id`, `created_at`, `updated_at`, `first_name`, `last_name`, `email`, `password`, `verification_code`, `status_id`, `profile_image`, `is_admin`) VALUES (9,'2026-10-11 18:50:08.029818','2026-10-11 18:50:08.029818','John','Doe','test@example.com','691354ec-5','VERIFIED',1,NULL,_binary '\0');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist`
--

DROP TABLE IF EXISTS `wishlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlist` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `product_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKq85ckb2tlq3h7k56ovtqnjls2` (`user_id`,`product_id`),
  KEY `FK7jtw3scpmuvm08bxdd4hoqdf2` (`product_id`),
  CONSTRAINT `FK7jtw3scpmuvm08bxdd4hoqdf2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKtrd6335blsefl2gxpb8lr0gr7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist`
--

LOCK TABLES `wishlist` WRITE;
/*!40000 ALTER TABLE `wishlist` DISABLE KEYS */;
INSERT INTO `wishlist` (`id`, `created_at`, `updated_at`, `product_id`, `user_id`) VALUES (2,'2026-10-11 18:27:46.354312','2026-10-11 18:27:46.354312',23,1);
INSERT INTO `wishlist` (`id`, `created_at`, `updated_at`, `product_id`, `user_id`) VALUES (3,'2026-10-11 18:40:03.489327','2026-10-11 18:40:03.489327',23,7);
INSERT INTO `wishlist` (`id`, `created_at`, `updated_at`, `product_id`, `user_id`) VALUES (4,'2026-10-11 18:40:04.871692','2026-10-11 18:40:04.871692',21,7);
/*!40000 ALTER TABLE `wishlist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'smarttrade'
--

--
-- Dumping routines for database 'smarttrade'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-11 19:14:08
