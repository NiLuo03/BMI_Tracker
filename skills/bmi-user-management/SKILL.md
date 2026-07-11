---
name: bmi-user-management
description: >
  BMI体质评估系统的用户管理模块。实现注册、登录、密码修改、个人信息查看/编辑功能。
  触发词：用户管理、注册登录、用户注册、用户登录、密码修改、个人信息、用户模块、
  UserController、login、register、用户表、users表。
  当用户提到 BMI 项目中的任何用户相关功能时，使用此 Skill。
---
# BMI 用户管理模块 (User Management)

## 开发目标
实现 BMI 体质评估系统的用户认证与信息管理功能，包括注册、登录、密码修改、个人信息展示与编辑。

## 前置依赖
- JavaFX 项目已创建，SceneBuilder 可用
- MySQL 数据库已安装并可访问
- JDBC 驱动已配置（mysql-connector-j）

## 数据库

### 建表 SQL

在 MySQL 中执行以下建表语句后再开始编码：

```sql
CREATE DATABASE IF NOT EXISTS bmi_system DEFAULT CHARACTER SET utf8mb4;
USE bmi_system;

CREATE TABLE users (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    userName VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(64) NOT NULL,
    userAge INT NOT NULL,
    sex TINYINT NOT NULL DEFAULT 0 COMMENT '0=男, 1=女',
    height DECIMAL(5,2),
    weight DECIMAL(5,2),
    preferences VARCHAR(200) COMMENT '偏好标签，逗号分隔',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### JDBC 连接工具类

创建 `util/DBUtil.java`，封装数据库连接：

```java
public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/bmi_system?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password";

    static { try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (Exception e) { e.printStackTrace(); } }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
```

> 提示用户将 `your_password` 替换为实际 MySQL 密码。

---

## 功能实现

### 1. 注册功能 (Register)

**FXML 文件**：`register.fxml`
**Controller**：`RegisterController.java`

#### 界面元素
- `TextField`：账号 (`tfUsername`)、年龄 (`tfAge`)
- `PasswordField`：密码 (`pfPassword`)、确认密码 (`pfConfirm`)
- `RadioButton`：男 (`rbMale`)、女 (`rbFemale`)，放入同一个 `ToggleGroup`
- `Button`：注册 (`btnRegister`)、返回登录 (`btnBack`)

#### 注册逻辑 (btnRegister 点击事件)

```
1. 校验输入：
   - 任意字段为空 → 弹窗 "请完整填写所有字段"
   - 年龄 <= 0 → 弹窗 "请输入有效年龄"
   - 两次密码不一致 → 弹窗 "两次输入的密码不一致"
2. 构建 SQL：INSERT INTO users (userName, password, userAge, sex) VALUES (?, ?, ?, ?)
3. 使用 PreparedStatement 执行插入
4. 处理异常：
   - SQLIntegrityConstraintViolationException（账号重复）→ 弹窗 "该账号已被占用，请更换"
   - 其他异常 → 弹窗 "注册失败，请稍后再试"
5. 成功 → 弹窗 "注册成功！" → 关闭注册窗口 → 打开登录窗口
```

### 2. 登录功能 (Login)

**FXML 文件**：`login.fxml`
**Controller**：`LoginController.java`

#### 界面元素
- `TextField`：账号 (`tfUsername`)
- `PasswordField`：密码 (`pfPassword`)
- `Button`：登录 (`btnLogin`)、去注册 (`btnToRegister`)
- `Label`：登录状态提示

#### 登录逻辑 (btnLogin 点击事件)

```
1. 校验输入：
   - 账号或密码为空 → 弹窗 "请输入账号和密码"
2. 查询数据库：SELECT * FROM users WHERE userName = ? AND password = ?
3. 判断结果：
   - 查到记录 → 将 userId、userName、userAge、sex、height、weight、preferences 存入全局 Session（创建 Session.java 单例）
   - 未查到 → 弹窗 "账号或密码错误"（不区分是账号不存在还是密码错误，防止撞库）
4. 成功 → 关闭登录窗口 → 打开主界面（Main.fxml）
```

#### Session 单例类

```java
public class Session {
    private static Session instance;
    private int userId;
    private String userName;
    private int userAge;
    private int sex;
    private double height;
    private double weight;
    private String preferences;

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }
    // getter & setter ...
}
```

### 3. 密码修改 (ChangePassword)

**方式**：在主界面菜单或个人信息页面中放置"修改密码"按钮。

**FXML 文件**：`change_password.fxml`

#### 界面元素
- `PasswordField`：旧密码、新密码、确认新密码
- `Button`：确认修改、取消

#### 修改逻辑

```
1. 校验：
   - 任意字段为空 → "请填写完整"
   - 新密码与确认不一致 → "两次输入的密码不一致"
2. 验证旧密码：
   SELECT password FROM users WHERE userId = ?
   比对旧密码是否匹配 → 不匹配则 "原密码不正确"
3. 更新密码：
   UPDATE users SET password = ? WHERE userId = ?
4. 成功 → "密码修改成功，请重新登录" → 关闭主界面 → 打开登录窗口
```

### 4. 个人信息查看/编辑 (Profile)

**FXML 文件**：`profile.fxml`
**Controller**：`ProfileController.java`

#### 展示字段（Label 只读）
- 用户ID (`userId`)
- 账号 (`userName`)
- 注册时间 (`createTime`)

#### 可编辑字段（TextField）
- 年龄 (`tfAge`)
- 身高 (`tfHeight`)
- 体重 (`tfWeight`)
- 性别（RadioButton）
- 偏好标签（CheckBox：不吃辣、不吃海鲜、低卡偏好、高蛋白偏好、素食）

#### 编辑逻辑

```
1. 初始化：从 Session 读取当前登录用户数据，填充到界面
2. 点击"保存"：
   - 身高 <= 0 或体重 <= 0 → "请输入有效数值"
   - 年龄 <= 0 → "请输入有效年龄"
   - 拼接 preferences 字符串（逗号分隔选中的标签）
   - UPDATE users SET userAge=?, sex=?, height=?, weight=?, preferences=? WHERE userId=?
3. 更新 Session 中的数据
4. 成功 → "个人信息已更新"
```

---

## 通用约束

### 异常处理模板

所有数据库操作必须遵循以下模板：

```java
try {
    Connection conn = DBUtil.getConnection();
    PreparedStatement ps = conn.prepareStatement(sql);
    // ... 参数设置
    ResultSet rs = ps.executeQuery(); // 或 executeUpdate()
    // ... 处理结果
} catch (SQLException e) {
    e.printStackTrace();
    // 弹出友好中文提示，禁止显示堆栈信息
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("错误");
    alert.setHeaderText(null);
    alert.setContentText("操作失败，请稍后再试");
    alert.showAndWait();
} finally {
    DBUtil.close(conn, ps, rs);
}
```

### 弹窗规范

- 所有弹窗使用 `Stage + Modality.WINDOW_MODAL` 确保模态
- 错误提示用 `AlertType.ERROR`
- 成功提示用 `AlertType.INFORMATION`
- 确认操作用 `AlertType.CONFIRMATION`

### 代码风格

- 类名：大驼峰（`RegisterController`）
- 方法名：小驼峰（`handleRegister`）
- 变量名：小驼峰（`tfUsername`）
- 常量：全大写（`DB_URL`）
- 关键逻辑上方加中文注释

## 验证清单

- [ ] 能用新账号注册成功，并自动跳转登录页
- [ ] 重复账号注册会提示"账号已被占用"
- [ ] 正确密码能登录成功，进入主界面
- [ ] 错误密码登录会提示"账号或密码错误"（不泄露具体原因）
- [ ] 旧密码错误时修改密码会提示"原密码不正确"
- [ ] 修改密码成功后跳回登录页
- [ ] 个人信息页正确展示当前用户数据
- [ ] 修改身高/体重/年龄后数据持久化
- [ ] 偏好标签选中后保存并正确回显
- [ ] 任何空字段提交均有明确提示
