-- MySQL dump 10.13  Distrib 5.7.26, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: tao
-- ------------------------------------------------------
-- Server version	5.7.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `authorities`
--

DROP TABLE IF EXISTS `authorities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `authorities` (
  `id` int(11) NOT NULL COMMENT '菜单ID',
  `path` varchar(128) NOT NULL,
  `component` varchar(64) DEFAULT NULL,
  `hidden` varchar(255) DEFAULT '0' COMMENT '0-开启，1- 关闭',
  `always_show` varchar(255) DEFAULT '0' COMMENT '0-开启，1- 关闭',
  `redirect` varchar(32) DEFAULT NULL COMMENT 'if set noRedirect will no redirect in the breadcrumb',
  `name` varchar(32) DEFAULT NULL COMMENT 'the name is used by <keep-alive> (must set!!!)',
  `title` varchar(32) DEFAULT NULL COMMENT 'the name show in sidebar and breadcrumb (recommend set)',
  `icon` varchar(32) DEFAULT NULL COMMENT '图标',
  `parent_id` int(11) NOT NULL COMMENT '父菜单ID',
  `weight` int(11) NOT NULL DEFAULT '1' COMMENT '排序值',
  `type` char(1) NOT NULL DEFAULT '0' COMMENT '菜单类型 （0菜单 1按钮）',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `breadcrumb` int(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `authorities`
--

INSERT INTO `authorities` (`id`, `path`, `component`, `hidden`, `always_show`, `redirect`, `name`, `title`, `icon`, `parent_id`, `weight`, `type`, `create_time`, `update_time`, `breadcrumb`) VALUES (10,'external-link','Layout',NULL,NULL,'/github',NULL,'动态路由','link',-1,1,'0','2020-08-18 02:13:56','2020-08-20 08:26:43',NULL),(11,'https://github.com/taoroot/tao',NULL,NULL,NULL,NULL,'github','github','github',10,1,'0','2020-08-18 02:14:08','2020-08-19 07:32:06',NULL),(12,'https://doc-tao.flizi.cn',NULL,NULL,NULL,NULL,'vuepress','vuepress','link',10,1,'0','2020-08-18 02:14:08','2020-08-18 13:02:41',NULL);

--
-- Table structure for table `depts`
--

DROP TABLE IF EXISTS `depts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `depts` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '部门主键ID',
  `name` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '部门名称',
  `weight` int(11) DEFAULT NULL COMMENT '排序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `parent_id` int(11) DEFAULT NULL COMMENT '上级部门',
  `path` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='部门管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `depts`
--

INSERT INTO `depts` (`id`, `name`, `weight`, `create_time`, `update_time`, `parent_id`, `path`) VALUES (1000,'软件一部',1,'2020-08-20 07:36:35','2020-08-20 09:23:16',-1,'[1000]'),(1001,'软一前端',1,'2020-08-20 08:30:17','2020-08-20 09:23:16',1000,'[1000,1001]'),(1002,'软一后端',1,'2020-08-20 08:30:17','2020-08-20 09:09:45',1000,'[1000,1002]'),(2000,'软件二部',1,'2020-08-20 08:32:35','2020-08-20 09:23:16',-1,'[2000]'),(2001,'软二前端',1,'2020-08-20 08:32:35','2020-08-20 11:59:20',2000,'[2000,2001]'),(2002,'软二后端',1,'2020-08-20 08:32:35','2020-08-20 09:23:16',2000,'[2000,2002]');

--
-- Table structure for table `oauth2_authorized_client`
--

DROP TABLE IF EXISTS `oauth2_authorized_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth2_authorized_client` (
  `client_registration_id` varchar(100) NOT NULL,
  `principal_name` varchar(200) NOT NULL,
  `access_token_type` varchar(100) NOT NULL,
  `access_token_value` blob NOT NULL,
  `access_token_issued_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `access_token_expires_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `access_token_scopes` varchar(1000) DEFAULT NULL,
  `refresh_token_value` blob,
  `refresh_token_issued_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`client_registration_id`,`principal_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth2_authorized_client`
--


--
-- Table structure for table `role_authority`
--

DROP TABLE IF EXISTS `role_authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role_authority` (
  `role_id` int(11) NOT NULL,
  `authority_id` int(11) NOT NULL,
  PRIMARY KEY (`role_id`,`authority_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='角色菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_authority`
--

INSERT INTO `role_authority` (`role_id`, `authority_id`) VALUES (1,10),(1,11),(1,12);

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_bin NOT NULL,
  `role` varchar(64) COLLATE utf8mb4_bin NOT NULL,
  `scope` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `role_idx1_role_code` (`role`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='角色';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`id`, `name`, `role`, `scope`, `description`, `create_time`, `update_time`) VALUES (1,'普通用户','USER',NULL,'','2020-08-18 11:36:19','2020-08-18 13:09:45');

--
-- Table structure for table `user_oauth2`
--

DROP TABLE IF EXISTS `user_oauth2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_oauth2` (
  `client_registration_id` varchar(100) NOT NULL,
  `principal_name` varchar(200) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` varchar(255) DEFAULT NULL,
  `nickname` varchar(200) DEFAULT NULL,
  `avatar` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`client_registration_id`,`principal_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_oauth2`
--


--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='用户角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--


--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `enabled` tinyint(1) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(200) DEFAULT NULL,
  `dept_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-08-20 20:12:11
