---
name: bmi-food-reference
description: >
  BMI体质评估系统的食物参考模块。实现50+种常见食物数据的录入与管理，
  支持2~3种食物营养对比（高亮热量最低/蛋白质最高）、按分类食物榜单（前十低卡推荐）。
  触发词：食物对比、食物榜单、食物数据库、热量对比、营养数据、食物参考、
  低卡推荐、食物表、foods表、食物分类、卡路里排行。
  当用户提到食物营养数据、食物对比或食物榜单相关功能时，使用此 Skill。
---
# 食物参考模块 (Food Reference)

## 开发目标
构建常见食物营养数据库（>= 50 种），实现食物对比和按分类食物榜单功能。

## 前置依赖
- MySQL 数据库可用
- 导航菜单已搭建，本模块对应"食物参考"菜单项

---

## 数据库

### 建表 SQL

```sql
USE bmi_system;

CREATE TABLE foods (
    foodId INT PRIMARY KEY AUTO_INCREMENT,
    foodName VARCHAR(30) NOT NULL UNIQUE,
    category VARCHAR(10) NOT NULL COMMENT '主食/肉类/蔬菜/水果/饮品',
    calories DECIMAL(6,1) NOT NULL COMMENT '每100g热量(大卡)',
    protein DECIMAL(5,1) COMMENT '每100g蛋白质(g)',
    fat DECIMAL(5,1) COMMENT '每100g脂肪(g)',
    carb DECIMAL(5,1) COMMENT '每100g碳水(g)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 种子数据（50 种食物）

将以下 SQL 作为初始化脚本 `data/foods_seed.sql`：

```sql
INSERT INTO foods (foodName, category, calories, protein, fat, carb) VALUES
-- 主食类 (10种)
('白米饭', '主食', 116, 2.6, 0.3, 25.9),
('馒头', '主食', 223, 7.0, 1.1, 44.2),
('面条(煮)', '主食', 110, 3.5, 0.2, 24.3),
('全麦面包', '主食', 246, 8.5, 3.4, 46.4),
('小米粥', '主食', 46, 1.4, 0.7, 8.4),
('红薯', '主食', 86, 1.6, 0.1, 20.1),
('玉米', '主食', 112, 4.0, 1.2, 22.8),
('燕麦片', '主食', 377, 13.5, 6.7, 66.3),
('糙米饭', '主食', 123, 2.7, 0.9, 25.6),
('土豆', '主食', 76, 2.0, 0.2, 17.2),

-- 肉类 (10种)
('鸡胸肉', '肉类', 133, 31.0, 0.5, 1.3),
('猪瘦肉', '肉类', 143, 20.3, 6.2, 1.5),
('牛肉(瘦)', '肉类', 106, 20.2, 2.3, 1.2),
('羊肉', '肉类', 203, 19.0, 14.1, 0.1),
('鸭肉', '肉类', 240, 15.5, 19.7, 0.2),
('三文鱼', '肉类', 208, 20.4, 13.4, 0.1),
('虾仁', '肉类', 99, 20.3, 0.7, 1.5),
('鸡蛋', '肉类', 144, 13.3, 8.8, 2.8),
('猪肝', '肉类', 129, 19.3, 3.5, 5.0),
('带鱼', '肉类', 127, 17.7, 4.9, 3.1),

-- 蔬菜类 (10种)
('西兰花', '蔬菜', 34, 2.8, 0.4, 6.6),
('菠菜', '蔬菜', 23, 2.9, 0.3, 4.5),
('番茄', '蔬菜', 18, 0.9, 0.2, 3.9),
('黄瓜', '蔬菜', 15, 0.7, 0.1, 2.9),
('胡萝卜', '蔬菜', 41, 0.9, 0.2, 10.0),
('白菜', '蔬菜', 13, 1.5, 0.1, 2.6),
('芹菜', '蔬菜', 14, 0.8, 0.1, 3.3),
('冬瓜', '蔬菜', 10, 0.4, 0.2, 2.4),
('豆芽', '蔬菜', 18, 2.1, 0.3, 2.3),
('生菜', '蔬菜', 13, 1.3, 0.3, 2.0),

-- 水果类 (10种)
('苹果', '水果', 52, 0.3, 0.2, 13.5),
('香蕉', '水果', 89, 1.1, 0.3, 22.8),
('橙子', '水果', 47, 0.9, 0.1, 11.8),
('葡萄', '水果', 67, 0.7, 0.2, 17.2),
('西瓜', '水果', 30, 0.6, 0.1, 6.8),
('草莓', '水果', 32, 0.7, 0.3, 7.7),
('猕猴桃', '水果', 61, 1.1, 0.5, 14.7),
('火龙果', '水果', 55, 1.1, 0.4, 13.3),
('蓝莓', '水果', 57, 0.7, 0.3, 14.5),
('柚子', '水果', 41, 0.8, 0.2, 9.5),

-- 饮品/其他 (10种)
('纯牛奶', '饮品', 65, 3.0, 3.5, 5.0),
('豆浆', '饮品', 31, 3.0, 1.6, 1.2),
('酸奶(原味)', '饮品', 72, 2.5, 2.7, 9.3),
('豆腐', '蔬菜', 81, 8.1, 3.7, 4.2),
('核桃', '肉类', 646, 14.9, 65.2, 19.1),
('蜂蜜', '饮品', 321, 0.4, 1.9, 80.3),
('黑巧克力', '饮品', 546, 4.9, 31.3, 61.1),
('咖啡(黑)', '饮品', 2, 0.1, 0.1, 0.4),
('绿茶', '饮品', 1, 0.1, 0.1, 0.2),
('可乐', '饮品', 42, 0.1, 0.1, 10.8);
```

---

## 功能实现

### 1. 食物对比

**FXML 文件**：`food_compare.fxml`
**Controller**：`FoodCompareController.java`

#### 核心逻辑

```java
public class FoodCompareController {
    // 当前选中的食物列表（最多3个）
    private ObservableList<Food> selectedFoods = FXCollections.observableArrayList();
    // 对比表格
    @FXML private TableView<NutrientRow> compareTable;

    @FXML
    private void handleAddFood() {
        if (selectedFoods.size() >= 3) {
            showAlert("最多对比3种食物");
            return;
        }
        // 弹出搜索/选择对话框
        Food selected = showFoodPickerDialog();
        if (selected != null && !selectedFoods.contains(selected)) {
            selectedFoods.add(selected);
            refreshCompareTable();
        }
    }

    private void refreshCompareTable() {
        compareTable.getColumns().clear();
        compareTable.getItems().clear();

        // 构建行数据
        String[] nutrients = {"热量(kcal)", "蛋白质(g)", "脂肪(g)", "碳水(g)"};
        List<NutrientRow> rows = new ArrayList<>();

        for (String nutrient : nutrients) {
            NutrientRow row = new NutrientRow(nutrient);
            for (Food food : selectedFoods) {
                row.addValue(food.getNutrientValue(nutrient));
            }
            rows.add(row);
        }

        // 动态创建列
        TableColumn<NutrientRow, String> colNutrient = new TableColumn<>("营养指标");
        colNutrient.setCellValueFactory(new PropertyValueFactory<>("nutrient"));
        compareTable.getColumns().add(colNutrient);

        for (int i = 0; i < selectedFoods.size(); i++) {
            final int idx = i;
            TableColumn<NutrientRow, String> col = new TableColumn<>(selectedFoods.get(i).getFoodName());
            col.setCellValueFactory(cell -> cell.getValue().valueAt(idx));
            compareTable.getColumns().add(col);
        }

        compareTable.getItems().addAll(rows);
        highlightExtremes(); // 高亮热量最低和蛋白质最高
    }

    private void highlightExtremes() {
        // 找到热量最低的食物
        Food minCalFood = selectedFoods.stream()
            .min(Comparator.comparing(Food::getCalories)).orElse(null);
        // 找到蛋白质最高的食物
        Food maxProteinFood = selectedFoods.stream()
            .max(Comparator.comparing(Food::getProtein)).orElse(null);

        // 更新摘要标签
        lblSummary.setText(String.format("热量最低：%s (%.1fkcal)  |  蛋白质最高：%s (%.1fg)",
            minCalFood != null ? minCalFood.getFoodName() : "-",
            minCalFood != null ? minCalFood.getCalories() : 0,
            maxProteinFood != null ? maxProteinFood.getFoodName() : "-",
            maxProteinFood != null ? maxProteinFood.getProtein() : 0));
    }
}
```

#### 高亮实现
通过 `TableCell` 的 `updateItem` 方法设置背景色：
```java
// 热量最低 → 绿色高亮 #e8f5e9
// 蛋白质最高 → 蓝色高亮 #e3f2fd
col.setCellFactory(column -> new TableCell<>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setText(null); setStyle(""); return; }
        setText(item);
        // 高亮逻辑（判断该食物是否是极值）
        if (isMinCalFood && column == calColumn) {
            setStyle("-fx-background-color: #e8f5e9; -fx-font-weight: bold;");
        } else if (isMaxProteinFood && column == proteinColumn) {
            setStyle("-fx-background-color: #e3f2fd; -fx-font-weight: bold;");
        } else {
            setStyle("");
        }
    }
});
```

---

### 2. 食物榜单

**FXML 文件**：`food_ranking.fxml`
**Controller**：`FoodRankingController.java`

#### 核心逻辑

```java
@FXML
private void handleLoadRanking() {
    String selectedCategory = cbCategory.getValue() != null
            ? cbCategory.getValue() : "";

    String sql;
    if (selectedCategory.isEmpty() || "全部".equals(selectedCategory)) {
        sql = "SELECT * FROM foods ORDER BY calories ASC LIMIT 10";
    } else {
        sql = "SELECT * FROM foods WHERE category = ? ORDER BY calories ASC LIMIT 10";
    }

    List<Food> ranking = new ArrayList<>();
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        if (!selectedCategory.isEmpty() && !"全部".equals(selectedCategory)) {
            ps.setString(1, selectedCategory);
        }
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ranking.add(new Food(
                rs.getInt("foodId"),
                rs.getString("foodName"),
                rs.getString("category"),
                rs.getDouble("calories"),
                rs.getDouble("protein"),
                rs.getDouble("fat"),
                rs.getDouble("carb")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("榜单加载失败");
        return;
    }

    // 填充 TableView
    ObservableList<Food> data = FXCollections.observableArrayList(ranking);
    rankingTable.setItems(data);
}
```

#### 徽章（低卡推荐）
在 TableCell 中为低卡食物添加徽章：
```java
// 热量 < 50kcal 显示 "低卡推荐" 徽章
Label badge = new Label("低卡推荐");
badge.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;" +
               "-fx-padding: 2 6; -fx-background-radius: 8; -fx-font-size: 11px;");
```

#### 分类下拉选项
```java
// 初始化分类列表
ObservableList<String> categories = FXCollections.observableArrayList(
    "全部", "主食", "肉类", "蔬菜", "水果", "饮品"
);
cbCategory.setItems(categories);
cbCategory.setValue("全部");
```

---

## 食物数据模型 (Food.java)

```java
package model;

public class Food {
    private int foodId;
    private String foodName;
    private String category;
    private double calories;  // 每100g
    private double protein;
    private double fat;
    private double carb;

    // 构造器 + getter/setter

    public double getNutrientValue(String nutrient) {
        switch (nutrient) {
            case "热量(kcal)": return calories;
            case "蛋白质(g)": return protein;
            case "脂肪(g)": return fat;
            case "碳水(g)": return carb;
            default: return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Food)) return false;
        return foodId == ((Food) o).foodId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(foodId);
    }
}
```

---

## 通用约束

- 数据库操作包裹 try-catch，异常弹窗中文提示
- 食物对比最多选 3 种，超过时弹窗提示
- 食物榜单默认显示前 10 名，按热量升序
- 删除食物功能可考虑增加（从对比列表中移除）

## 验证清单

- [ ] foods 表正确创建，种子数据 >= 50 条
- [ ] 食物搜索/选择器正常工作
- [ ] 选择 2~3 种食物后对比表格正确渲染
- [ ] 热量最低单元格高亮为绿色背景
- [ ] 蛋白质最高单元格高亮为蓝色背景
- [ ] 分类下拉切换后榜单数据正确刷新
- [ ] 热量 < 50kcal 的食物显示"低卡推荐"徽章
- [ ] 榜单按热量从低到高排列
- [ ] 超过 3 种食物时提示"最多对比3种"
