package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.component.WheelPicker;
import com.bmitracker.model.Food;
import com.bmitracker.model.MealRecord;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import com.bmitracker.service.MealRecordService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MealAddController {

    @FXML private HBox contentArea;
    @FXML private VBox leftArea, rightArea, calcCard;
    @FXML private TextField searchField;
    @FXML private Label amountLabel, servingDescLbl;
    @FXML private FlowPane foodGrid;
    @FXML private GridPane mealGrid;
    @FXML private GridPane calcGrid;
    @FXML private WheelPicker<String> dateWheel;

    @FXML private Button cancelBtn;

    private final FoodService foodService = new FoodServiceImpl();
    private final MealRecordService recordService = new MealRecordService();

    public static MealRecord pendingEdit = null;
    public static LocalDate defaultDate = null;
    private boolean editMode = false;
    private int editRecordId = -1;

    private List<Food> allFoods = Collections.emptyList();
    private Food selectedFood = null;
    private int selectedFoodId = -1;

    private final StringBuilder expr = new StringBuilder();
    private List<LocalDate> recordDates = new ArrayList<>();
    private String currentMealType = "BREAKFAST";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM.dd");
    private static final String[] MEAL_KEYS = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"};
    private static final String[] MEAL_EMOJI = {"🌅", "☀️", "🌙", "🍪"};
    private static final String[] MEAL_NAMES = {"早餐", "午餐", "晚餐", "加餐"};                   // <字符> 餐类

    @FXML
    void initialize() {
        if (pendingEdit != null) {
            editMode = true;
            editRecordId = pendingEdit.getRecordId();
        }

        if (editMode) {
            cancelBtn.setText("删除");
        }
        contentArea.widthProperty().addListener(o -> {
            double total = contentArea.getWidth() - 14;                                            // <间距> contentArea spacing=14
            leftArea.setPrefWidth(total / 2.414);
            rightArea.setPrefWidth(total * 1.414 / 2.414);
        });

        rightArea.heightProperty().addListener(o -> {
            double th = rightArea.getHeight() - 10;
            if (th <= 0) return;
            mealGrid.setPrefHeight(th / 2.414);
            calcCard.setPrefHeight(th * 1.414 / 2.414);
        });

        servingDescLbl.managedProperty().bind(servingDescLbl.visibleProperty());

        dateWheel.setLightTheme(true);
        buildMealCards();
        buildCalcGrid();
        setupKeyboard();

        new Thread(() -> {
            allFoods = foodService.getAllFoods();
            Platform.runLater(() -> {
                if (allFoods.isEmpty()) servingDescLbl.setText("");
                updateFoodGrid("");
                rebuildDateWheel();
                if (defaultDate != null) {
                    for (int i = 0; i < recordDates.size(); i++) {
                        if (recordDates.get(i).equals(defaultDate)) {
                            dateWheel.setSelectedIndex(i);
                            break;
                        }
                    }
                    defaultDate = null;
                }
                if (editMode) prefillFromPending();
            });
        }).start();

        searchField.textProperty().addListener(o -> updateFoodGrid(searchField.getText()));
    }

    private void prefillFromPending() {
        MealRecord e = pendingEdit;
        pendingEdit = null;

        for (int i = 0; i < MEAL_KEYS.length; i++) {
            if (MEAL_KEYS[i].equals(e.getMealType())) {
                selectMeal(i);
                break;
            }
        }

        for (int i = 0; i < recordDates.size(); i++) {
            if (recordDates.get(i).equals(e.getRecordDate())) {
                dateWheel.setSelectedIndex(i);
                break;
            }
        }

        for (Food f : allFoods) {
            if (f.getFoodId() == e.getFoodId()) {
                selectedFood = f;
                selectedFoodId = f.getFoodId();
                break;
            }
        }
        updateFoodGrid(searchField.getText());

        expr.setLength(0);
        expr.append((int) e.getGrams());
        updateAmount();
    }

    private void rebuildDateWheel() {
        List<String> labels = new ArrayList<>();
        recordDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.of(today.getYear(), 1, 1);
        LocalDate d = start;
        while (!d.isAfter(today)) {
            if (d.equals(today)) labels.add("今天");
            else if (d.equals(today.minusDays(1))) labels.add("昨天");
            else if (d.equals(today.minusDays(2))) labels.add("前天");
            else labels.add(DATE_FMT.format(d));
            recordDates.add(d);
            d = d.plusDays(1);
        }
        dateWheel.setItems(labels);
        dateWheel.setSelectedIndex(labels.size() - 1);
    }

    private void buildMealCards() {
        mealGrid.getColumnConstraints().clear();
        mealGrid.getRowConstraints().clear();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        cc.setHgrow(Priority.ALWAYS);
        cc.setFillWidth(true);
        mealGrid.getColumnConstraints().addAll(cc, cc);
        for (int r = 0; r < 2; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(50);
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            mealGrid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            VBox card = new VBox(6);                         // <间距> 餐卡内部垂直间距6px
            card.setPadding(new Insets(10, 12, 10, 12));  // <间距> 餐卡内边距 上10 右12 下10 左12
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: rgba(0,0,0,0.02);"
                    + "-fx-background-radius: 10px;"
                    + "-fx-border-color: rgba(0,0,0,0.06);"
                    + "-fx-border-width: 1px;"
                    + "-fx-border-radius: 10px;"
                    + "-fx-cursor: hand;");

            HBox topRow = new HBox();
            topRow.setAlignment(Pos.CENTER_LEFT);
            Label emoji = new Label(MEAL_EMOJI[i]);
            emoji.setStyle("-fx-font-size: 36px;");                          // <字体> 餐卡emoji 字号36
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label dot = new Label("○");
            dot.setStyle("-fx-font-size: 37px; -fx-text-fill: #cccccc;");    // <字体> 圆点 字号37 颜色#cccccc（初始态，随后被selectMeal覆盖）
            topRow.getChildren().addAll(emoji, sp, dot);

            Label name = new Label(MEAL_NAMES[i]);
            name.setStyle("-fx-font-size: 25px; -fx-text-fill: #555555;"
                    + "-fx-font-family: \"Alimama ShuHeiTi Bold\", \"阿里妈妈数黑体 Bold\", \"Microsoft YaHei\", \"PingFang SC\", sans-serif;");   // <字体> 餐卡名称 字号25 颜色#555555（初始态，随后被selectMeal覆盖）

            card.getChildren().addAll(topRow, name);
            card.setUserData(new Object[]{dot, card, name});
            card.setOnMouseClicked(e -> selectMeal(idx));

            GridPane.setFillWidth(card, true);
            GridPane.setFillHeight(card, true);
            mealGrid.add(card, i % 2, i / 2);
        }
        selectMeal(0);                                                   // 默认选中早餐，覆盖上述dot/name初始样式
    }

    private void selectMeal(int idx) {
        currentMealType = MEAL_KEYS[idx];
        for (javafx.scene.Node node : mealGrid.getChildren()) {
            if (!(node instanceof VBox)) continue;
            VBox card = (VBox) node;
            Integer c = GridPane.getColumnIndex(node);
            Integer r = GridPane.getRowIndex(node);
            int i = (r == null ? 0 : r) * 2 + (c == null ? 0 : c);
            Object[] parts = (Object[]) card.getUserData();
            Label dot = (Label) parts[0];
            Label name = (Label) parts[2];

            if (i == idx) {
                card.setStyle("-fx-background-color: rgba(16,185,129,0.06);"
                        + "-fx-background-radius: 10px;"
                        + "-fx-border-color: rgba(16,185,129,0.40);"
                        + "-fx-border-width: 1.5px;"
                        + "-fx-border-radius: 10px;"
                        + "-fx-cursor: hand;");
                dot.setText("●");
                dot.setStyle("-fx-font-size: 37px; -fx-text-fill: #10b981;");                      // <字体> 选中态：圆点 ● 字号37 颜色#10b981
                name.setStyle("-fx-font-size: 25px; -fx-text-fill: #10b981; -fx-font-weight: bold;"
                        + "-fx-font-family: \"Alimama ShuHeiTi Bold\", \"阿里妈妈数黑体 Bold\", \"Microsoft YaHei\", \"PingFang SC\", sans-serif;"); // <字体> 选中态：名称 字号25 颜色#10b981 字重bold
            } else {
                card.setStyle("-fx-background-color: rgba(0,0,0,0.02);"
                        + "-fx-background-radius: 10px;"
                        + "-fx-border-color: rgba(0,0,0,0.06);"
                        + "-fx-border-width: 1px;"
                        + "-fx-border-radius: 10px;"
                        + "-fx-cursor: hand;");
                dot.setText("○");
                dot.setStyle("-fx-font-size: 37px; -fx-text-fill: #cccccc;");                      // <字体> 未选中态：圆点 ○ 字号37 颜色#cccccc
                name.setStyle("-fx-font-size: 25px; -fx-text-fill: #555555;"
                        + "-fx-font-family: \"Alimama ShuHeiTi Bold\", \"阿里妈妈数黑体 Bold\", \"Microsoft YaHei\", \"PingFang SC\", sans-serif;");                     // <字体> 未选中态：名称 字号25 颜色#555555
            }
        }
    }

    private void setupKeyboard() {
        searchField.sceneProperty().addListener(o -> {
            if (searchField.getScene() == null) return;
            searchField.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                switch (e.getCode()) {
                    case DIGIT0, NUMPAD0 -> appendExpr('0');
                    case DIGIT1, NUMPAD1 -> appendExpr('1');
                    case DIGIT2, NUMPAD2 -> appendExpr('2');
                    case DIGIT3, NUMPAD3 -> appendExpr('3');
                    case DIGIT4, NUMPAD4 -> appendExpr('4');
                    case DIGIT5, NUMPAD5 -> appendExpr('5');
                    case DIGIT6, NUMPAD6 -> appendExpr('6');
                    case DIGIT7, NUMPAD7 -> appendExpr('7');
                    case DIGIT8, NUMPAD8 -> appendExpr('8');
                    case DIGIT9, NUMPAD9 -> appendExpr('9');
                    case PLUS, ADD -> appendOp('+');
                    case MULTIPLY -> appendOp('×');
                    case PERIOD, DECIMAL -> appendExpr('.');
                    case BACK_SPACE -> backspace();
                    case ENTER -> { handleSave(); e.consume(); }
                    default -> {}
                }
            });
        });
    }

    private void buildCalcGrid() {
        String[][] btnLabels = {
            {"1", "2", "3", "⌫"},
            {"4", "5", "6", "×"},
            {"7", "8", "9", "+"},
            {"再记", "0", ".", "保存"}
        };

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                String label = btnLabels[r][c];
                Button btn = new Button(label);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setMaxHeight(Double.MAX_VALUE);
                btn.setPrefHeight(34);
                boolean act1 = label.equals("保存");
                boolean act2 = label.equals("再记");
                boolean op = label.equals("+") || label.equals("×") || label.equals("⌫");
                if (act1) {
                    btn.setStyle("-fx-background-color: rgba(16,185,129,0.12);"
                            + "-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");  // <字体> 保存/再记按钮 字号14 颜色#10b981
                }else if (act2) {
                    btn.setStyle("-fx-background-color: rgba(16,185,129,0.12);"
                            + "-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-background-radius: 6; -fx-cursor: hand;");                         // <字体> 再记按钮 字号14 颜色#10b981
                } else if (op) {
                    btn.setStyle("-fx-background-color: rgba(0,0,0,0.05);"
                            + "-fx-text-fill: #333333; -fx-font-size: 18px; -fx-background-radius: 6; -fx-cursor: hand;");                         // <字体> 运算符按钮 字号18 颜色#333333
                } else {
                    btn.setStyle("-fx-background-color: rgba(0,0,0,0.03);"
                            + "-fx-text-fill: #222222; -fx-font-size: 18px; -fx-background-radius: 6; -fx-cursor: hand;");                         // <字体> 数字按钮 字号18 颜色#222222
                }
                btn.setOnAction(e -> {
                    switch (label) {
                        case "保存" -> handleSave();
                        case "再记" -> handleSaveAndContinue();
                        case "⌫" -> backspace();
                        case "+", "×" -> appendOp(label.charAt(0));
                        case "." -> appendExpr('.');
                        default -> appendExpr(label.charAt(0));
                    }
                });
                GridPane.setFillWidth(btn, true);
                GridPane.setFillHeight(btn, true);
                calcGrid.add(btn, c, r);
            }
        }
        for (int c = 0; c < 4; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            cc.setHgrow(Priority.ALWAYS);
            calcGrid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 4; r++) {
            calcGrid.getRowConstraints().add(new RowConstraints());
            calcGrid.getRowConstraints().get(r).setPercentHeight(25);
            calcGrid.getRowConstraints().get(r).setVgrow(Priority.ALWAYS);
        }
    }

    private void appendExpr(char c) { expr.append(c); updateAmount(); }
    private void appendOp(char op) {
        int len = expr.length();
        if (len == 0) return;
        char last = expr.charAt(len - 1);
        if (last == '+' || last == '×') return;
        expr.append(op);
        updateAmount();
    }
    private void backspace() { if (expr.length() > 0) { expr.deleteCharAt(expr.length() - 1); updateAmount(); } }
    private void updateAmount() { amountLabel.setText(expr.length() == 0 ? "0.00" : expr.toString()); }  // 克数显示（字号/颜色见fxml）

    private void updateFoodGrid(String keyword) {
        foodGrid.getChildren().clear();
        if (allFoods.isEmpty()) return;
        List<Food> shown = (keyword == null || keyword.isEmpty())
                ? allFoods
                : allFoods.stream().filter(f -> f.getFoodName().contains(keyword)).collect(Collectors.toList());
        for (Food food : shown) {
            boolean sel = food.getFoodId() == selectedFoodId;
            Button btn = new Button(food.getFoodName());
            btn.setStyle(sel
                ? "-fx-background-color: rgba(16,185,129,0.18); -fx-text-fill: #10b981; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: rgba(16,185,129,0.35); -fx-border-radius: 6;"
                : "-fx-background-color: rgba(0,0,0,0.04); -fx-text-fill: #333333; -fx-font-size: 13px; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");  // <按钮大小> 字号13 上下p6 左右p12 圆角6
            btn.setOnAction(e -> {
                if (food.getFoodId() == selectedFoodId) { selectedFood = null; selectedFoodId = -1; }
                else { selectedFood = food; selectedFoodId = food.getFoodId(); }
                updateFoodGrid(searchField.getText());
            });
            foodGrid.getChildren().add(btn);
        }
        if (selectedFood != null) {
            String desc = selectedFood.getServingDesc();
            if (desc != null && !desc.isEmpty()) {
                servingDescLbl.setText("参考份量：" + desc);                                        // 参考份量（字号/颜色见fxml）
                servingDescLbl.setVisible(true);
            } else {
                servingDescLbl.setVisible(false);
            }
        } else {
            servingDescLbl.setVisible(false);
        }
    }

    private double evaluate() {
        String s = expr.toString();
        if (s.isEmpty()) return 0;
        char last = s.charAt(s.length() - 1);
        if (last == '+' || last == '×') s = s.substring(0, s.length() - 1);
        if (s.isEmpty()) return 0;
        try {
            String[] addParts = s.split("\\+");
            double total = 0;
            for (String part : addParts) {
                String[] mulParts = part.split("×");
                double product = 1;
                for (String m : mulParts) {
                    String t = m.trim();
                    if (!t.isEmpty()) product *= Double.parseDouble(t);
                }
                total += product;
            }
            return total;
        } catch (NumberFormatException e) { return 0; }
    }

    private void handleSave() { if (doSave()) MainController.getInstance().loadView("meal_record.fxml"); }
    private void handleSaveAndContinue() {
        if (!doSave()) return;
        selectedFood = null; selectedFoodId = -1;
        expr.setLength(0); updateAmount();
        updateFoodGrid(searchField.getText());
    }

    private boolean doSave() {
        if (selectedFood == null) { showAlert("请先选择一种食物"); return false; }
        double grams = evaluate();
        if (grams <= 0) { showAlert("请输入有效克数"); return false; }
        int uid = BMIApplication.currentUserId;
        if (uid < 0) { showAlert("请先登录"); return false; }
        LocalDate date;
        int di = dateWheel.getSelectedIndex();
        date = (di >= 0 && di < recordDates.size()) ? recordDates.get(di) : LocalDate.now();
        MealRecord mr = new MealRecord();
        mr.setUserId(uid); mr.setFoodId(selectedFoodId);
        mr.setMealType(currentMealType); mr.setGrams(grams); mr.setRecordDate(date);
        try {
            if (editMode) {
                mr.setRecordId(editRecordId);
                recordService.updateRecord(mr);
            } else {
                recordService.addRecord(mr);
            }
        } catch (Exception e) { showAlert("保存失败：" + e.getMessage()); return false; }
        return true;
    }

    @FXML void handleCancel() {
        if (editMode) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("确认删除"); confirm.setHeaderText(null);
            confirm.setContentText("确定要删除这条膳食记录吗？");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try { recordService.deleteRecord(editRecordId); } catch (Exception e) { showAlert("删除失败：" + e.getMessage()); return; }
                    MainController.getInstance().loadView("meal_record.fxml");
                }
            });
        } else {
            MainController.getInstance().loadView("meal_record.fxml");
        }
    }
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
