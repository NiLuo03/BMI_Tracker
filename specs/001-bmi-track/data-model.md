# Data Model: BMI体质评估与预测系统

## 表结构（4张表）

### users（用户表）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| userId | INT | PK, AUTO_INCREMENT | 用户ID |
| userName | VARCHAR(20) | UNIQUE, NOT NULL | 登录账号 |
| password | VARCHAR(64) | NOT NULL | 密码（明文） |
| userAge | INT | NOT NULL | 年龄 |
| sex | TINYINT | NOT NULL, DEFAULT 0 | 0=男 1=女 |
| height | DECIMAL(5,2) | | 身高(cm) |
| weight | DECIMAL(5,2) | | 体重(kg) |
| preferences | VARCHAR(200) | | 偏好标签（逗号分隔） |
| createTime | DATETIME | DEFAULT CURRENT_TIMESTAMP | 注册时间 |

### bmi_records（BMI记录表）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| recordId | INT | PK, AUTO_INCREMENT | 记录ID |
| userId | INT | FK → users.userId | 对应用户 |
| height | DECIMAL(5,2) | NOT NULL | 身高 |
| weight | DECIMAL(5,2) | NOT NULL | 体重 |
| bmi | DECIMAL(4,1) | NOT NULL | BMI值 |
| status | VARCHAR(10) | NOT NULL | 偏瘦/正常/超重/肥胖 |
| createTime | DATETIME | DEFAULT CURRENT_TIMESTAMP | 记录时间 |

### foods（食物表）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| foodId | INT | PK, AUTO_INCREMENT | 食物ID |
| foodName | VARCHAR(30) | UNIQUE, NOT NULL | 食物名称 |
| category | VARCHAR(10) | NOT NULL | 主食/肉类/蔬菜/水果/饮品 |
| calories | DECIMAL(6,1) | NOT NULL | 每100g热量 |
| protein | DECIMAL(5,1) | | 蛋白质(g) |
| fat | DECIMAL(5,1) | | 脂肪(g) |
| carb | DECIMAL(5,1) | | 碳水(g) |

### recommendations（膳食推荐记录表）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| recId | INT | PK, AUTO_INCREMENT | 推荐ID |
| userId | INT | FK → users.userId | 对应用户 |
| breakfast | VARCHAR(100) | | 早餐 |
| lunch | VARCHAR(100) | | 午餐 |
| dinner | VARCHAR(100) | | 晚餐 |
| totalCal | VARCHAR(20) | | 总热量 |
| createTime | DATETIME | DEFAULT CURRENT_TIMESTAMP | 生成时间 |

## 实体类

```java
// model/User.java
public class User {
    private int userId;
    private String userName;
    private String password;
    private int userAge;
    private int sex;          // 0男 1女
    private double height;
    private double weight;
    private String preferences;
    private LocalDateTime createTime;
}

// model/BmiRecord.java
public class BmiRecord {
    private int recordId;
    private int userId;
    private double height;
    private double weight;
    private double bmi;
    private String status;    // 偏瘦/正常/超重/肥胖
    private LocalDateTime createTime;
}

// model/Food.java
public class Food {
    private int foodId;
    private String foodName;
    private String category;
    private double calories;
    private double protein;
    private double fat;
    private double carb;
}

// model/Recommendation.java
public class Recommendation {
    private int recId;
    private int userId;
    private String breakfast;
    private String lunch;
    private String dinner;
    private String totalCal;
    private LocalDateTime createTime;
}
```
