# Contracts: BMI体质评估与预测系统

## DAO 接口契约

### UserDao
```java
// 注册
int insertUser(User user) throws SQLException;
// 根据账号查询
User findByUserName(String userName) throws SQLException;
// 更新个人信息
int updateUser(User user) throws SQLException;
// 更新密码
int updatePassword(int userId, String newPassword) throws SQLException;
```

### BmiRecordDao
```java
// 插入BMI记录
int insertRecord(BmiRecord record) throws SQLException;
// 按用户查询（倒序）
List<BmiRecord> findByUserIdDesc(int userId) throws SQLException;
// 按用户查询（升序）
List<BmiRecord> findByUserIdAsc(int userId) throws SQLException;
// 统计记录数
int countByUserId(int userId) throws SQLException;
```

### FoodDao
```java
// 按分类查询（按热量升序）
List<Food> findByCategoryOrderByCalories(String category) throws SQLException;
// 根据ID列表查询
List<Food> findByIds(List<Integer> ids) throws SQLException;
// 查询所有分类
List<String> findAllCategories() throws SQLException;
```

### RecommendationDao
```java
// 插入推荐记录
int insertRecommendation(Recommendation rec) throws SQLException;
// 查询最近一条推荐
Recommendation findLatestByUserId(int userId) throws SQLException;
```

## Coze API 接口

### 请求
```
POST https://api.coze.cn/v1/chat/completions
Content-Type: application/json
Authorization: Bearer {API_KEY}
Timeout: 5s

{
  "messages": [
    {
      "role": "user",
      "content": "用户年龄: 20, 性别: 男, 身高: 175cm, 体重: 70kg, BMI: 22.9, 状态: 正常, 偏好: 不吃辣"
    }
  ]
}
```

### 响应
```json
{
  "breakfast": "全麦面包2片+牛奶250ml (450kcal)",
  "lunch": "糙米饭150g+鸡胸肉100g+西兰花200g (650kcal)",
  "dinner": "红薯200g+清蒸鱼150g+菠菜200g (550kcal)",
  "totalCal": "1650kcal"
}
```
