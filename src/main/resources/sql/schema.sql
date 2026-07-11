-- BMI体质评估与预测系统 数据库建表脚本
-- 数据库名: bmi_db

CREATE DATABASE IF NOT EXISTS bmi_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bmi_db;

-- 用户表
DROP TABLE IF EXISTS recommendations;
DROP TABLE IF EXISTS bmi_records;
DROP TABLE IF EXISTS foods;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    userName VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    userAge INT NOT NULL,
    sex TINYINT NOT NULL DEFAULT 0 COMMENT '0=男 1=女',
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    preferences VARCHAR(200) COMMENT '偏好标签，逗号分隔',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- BMI记录表
CREATE TABLE bmi_records (
    recordId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    height DECIMAL(5,2) NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    bmi DECIMAL(4,1) NOT NULL,
    status VARCHAR(10) NOT NULL COMMENT '偏瘦/正常/超重/肥胖',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 食物表
CREATE TABLE foods (
    foodId INT PRIMARY KEY AUTO_INCREMENT,
    foodName VARCHAR(30) NOT NULL UNIQUE,
    category VARCHAR(10) NOT NULL COMMENT '主食/肉类/蔬菜/水果/饮品',
    calories DECIMAL(6,1) NOT NULL COMMENT '每100g热量(大卡)',
    protein DECIMAL(5,1) COMMENT '每100g蛋白质(g)',
    fat DECIMAL(5,1) COMMENT '每100g脂肪(g)',
    carb DECIMAL(5,1) COMMENT '每100g碳水(g)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 膳食推荐记录表
CREATE TABLE recommendations (
    recId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    breakfast VARCHAR(200),
    lunch VARCHAR(200),
    dinner VARCHAR(200),
    totalCal VARCHAR(20),
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
