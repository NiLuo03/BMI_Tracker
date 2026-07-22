package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.Food;
import com.bmitracker.model.MealRecord;
import com.bmitracker.model.User;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import com.bmitracker.service.MealRecordService;
import com.bmitracker.service.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MealRecordController {

    @FXML private HBox contentArea, monthNav;
    @FXML private VBox leftArea, rightArea, chartBox, mealListBox, calendarBox;
    @FXML private ScrollPane listScroll;
    @FXML private Canvas chartCanvas;
    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private Button prevMonthBtn, nextMonthBtn, addMealBtn;

    private final MealRecordService recordService = new MealRecordService();
    private final UserService userService = new UserService();
    private final FoodService foodService = new FoodServiceImpl();

    private VBox addPanel;
    private FlowPane foodGrid, selectedFlow;
    private TextField searchField;
    private Label foodCountLabel, mealCalLbl, totalCalLbl;
    private Button mealBreakfastBtn, mealLunchBtn, mealDinnerBtn, mealSnackBtn, clearMealBtn;

    private List<Food> allFoods = Collections.emptyList();

    static class FoodEntry {
        Food food; double grams;
        FoodEntry(Food f, double g) { food = f; grams = g; }
        double getCalories() { return food.getCalories() * grams / 100.0; }
        double getProtein() { return food.getProtein() * grams / 100.0; }
        double getFat() { return food.getFat() * grams / 100.0; }
        double getCarb() { return food.getCarb() * grams / 100.0; }
    }

    private final Map<String, List<FoodEntry>> entries = new HashMap<>();
    private String currentMealType = "LUNCH";

    private static final String[] MEAL_KEYS = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"};
    private static final String[] MEAL_NAMES = {"早餐", "午餐", "晚餐", "加餐"};

    enum Param { CALORIE, PROTEIN, FAT, CARB }
    private Param currentParam = Param.CALORIE;
    private LocalDate selectedDate = LocalDate.now();
    private LocalDate calendarMonth;
    private int userSex = 0;

    private Map<LocalDate, List<MealRecord>> recordsByDate = new LinkedHashMap<>();
    private double rightWidth;

    private static final DateTimeFormatter DATE_MMDD = DateTimeFormatter.ofPattern("MM.dd");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy年M月");
    private static final String[] WEEKDAYS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    @FXML
    void initialize() {
        calendarMonth = LocalDate.now().withDayOfMonth(1);

        contentArea.widthProperty().addListener((obs, ov, nv) -> {
            double total = nv.doubleValue() - 14;
            double leftW = total * 1.616 / 2.616;
            rightWidth = total / 2.616;
            leftArea.setPrefWidth(leftW);
            rightArea.setPrefWidth(rightWidth);
            buildCalendar();
        });
        contentArea.heightProperty().addListener((obs, ov, nv) -> {
            if (nv.doubleValue() > 0) buildCalendar();
        });

        chartCanvas.widthProperty().addListener(o -> drawChart());
        chartCanvas.heightProperty().addListener(o -> drawChart());

        prevMonthBtn.setOnAction(e -> {
            calendarMonth = calendarMonth.minusMonths(1);
            loadMonthRecords();
        });
        nextMonthBtn.setOnAction(e -> {
            calendarMonth = calendarMonth.plusMonths(1);
            loadMonthRecords();
        });

        loadMonthRecords();

        listScroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            double v = listScroll.getVvalue();
            double h = listScroll.getContent().getBoundsInLocal().getHeight();
            double vh = listScroll.getViewportBounds().getHeight();
            if (h <= vh) return;
            double maxScroll = h - vh;
            double speed = 3.0;
            listScroll.setVvalue(Math.min(1, Math.max(0, v - e.getDeltaY() / maxScroll * speed)));
            e.consume();
        });
    }

    private void loadMonthRecords() {
        int uid = BMIApplication.currentUserId;
        if (uid < 0) return;

        monthLabel.setText(MONTH_FMT.format(calendarMonth));

        new Thread(() -> {
            User user = userService.getUserById(uid);
            if (user != null) userSex = user.getSex();

            LocalDate start = calendarMonth.withDayOfMonth(1);
            LocalDate end = calendarMonth.withDayOfMonth(calendarMonth.lengthOfMonth());
            List<MealRecord> all = recordService.getRecordsInRange(uid, start, end);

            Platform.runLater(() -> {
                recordsByDate.clear();
                for (MealRecord r : all) {
                    recordsByDate.computeIfAbsent(r.getRecordDate(), k -> new ArrayList<>()).add(r);
                }
                drawChart();
                buildCalendar();
                refreshList();
            });
        }).start();
    }

    private void drawChart() {
        double w = chartCanvas.getWidth();
        double h = chartCanvas.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        List<MealRecord> todayRecords = recordsByDate.getOrDefault(selectedDate, Collections.emptyList());
        double userCal = todayRecords.stream().mapToDouble(MealRecord::getCalories).sum();
        double userPro = todayRecords.stream().mapToDouble(MealRecord::getProtein).sum();
        double userFat = todayRecords.stream().mapToDouble(MealRecord::getFat).sum();
        double userCarb = todayRecords.stream().mapToDouble(MealRecord::getCarb).sum();

        double stdCal = userSex == 0 ? 2150 : 1700;
        double stdPro = stdCal * 0.1 / 4;
        double stdFat = stdCal * 0.2 / 9;
        double stdCarb = stdCal * 0.5 / 4;

        double[][] data = {{userCal, stdCal}, {userPro, stdPro}, {userFat, stdFat}, {userCarb, stdCarb}};
        String[] labels = {"热量(kcal)", "蛋白质(g)", "脂肪(g)", "碳水(g)"};

        double maxVal = 0;
        for (double[] d : data) { maxVal = Math.max(maxVal, Math.max(d[0], d[1])); }
        if (maxVal <= 0) maxVal = 1;

        double marginL = 40, marginR = 16, marginT = 16, marginB = 28;
        double plotW = w - marginL - marginR;
        double plotH = h - marginT - marginB;
        double groupW = plotW / 4;
        double barW = groupW * 0.28;

        for (int i = 0; i < 4; i++) {
            double groupX = marginL + i * groupW;
            double centerX = groupX + groupW / 2;

            double h1 = (data[i][0] / maxVal) * plotH;
            double h2 = (data[i][1] / maxVal) * plotH;

            double bar1X = centerX - barW - 4;
            double bar1Y = marginT + plotH - h1;
            gc.setFill(Color.rgb(16, 185, 129, 0.8));
            gc.fillRoundRect(bar1X, bar1Y, barW, h1, 3, 3);

            double bar2X = centerX + 4;
            double bar2Y = marginT + plotH - h2;
            gc.setFill(Color.rgb(0, 0, 0, 0.12));
            gc.fillRoundRect(bar2X, bar2Y, barW, h2, 3, 3);

            gc.setFill(Color.rgb(51, 51, 51));
            gc.setFont(Font.font("Microsoft YaHei", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.format("%.0f", data[i][0]), bar1X + barW / 2, bar1Y - 4);
            gc.fillText(String.format("%.0f", data[i][1]), bar2X + barW / 2, bar2Y - 4);

            gc.setFill(Color.rgb(85, 85, 85));
            gc.setFont(Font.font("Microsoft YaHei", 11));
            gc.fillText(labels[i], centerX, marginT + plotH + 16);
        }
    }

    private void refreshList() {
        mealListBox.getChildren().clear();
        if (recordsByDate.isEmpty()) {
            Label empty = new Label("本月暂无膳食记录");
            empty.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px; -fx-padding: 20 0;");
            empty.setAlignment(Pos.CENTER);
            empty.setMaxWidth(Double.MAX_VALUE);
            mealListBox.getChildren().add(empty);
            return;
        }

        List<Map.Entry<LocalDate, List<MealRecord>>> sorted = new ArrayList<>(recordsByDate.entrySet());
        sorted.sort((a, b) -> {
            if (a.getKey().equals(selectedDate)) return -1;
            if (b.getKey().equals(selectedDate)) return 1;
            return b.getKey().compareTo(a.getKey());
        });

        for (Map.Entry<LocalDate, List<MealRecord>> entry : sorted) {
            LocalDate date = entry.getKey();
            List<MealRecord> recs = entry.getValue();
            double sum = getParamSum(recs, currentParam);

            VBox card = new VBox(0);
            card.setStyle("-fx-background-color: #ffffff;"
                    + "-fx-background-radius: 10px;"
                    + "-fx-border-color: rgba(0,0,0,0.06);"
                    + "-fx-border-width: 1px;"
                    + "-fx-border-radius: 10px;"
                    + "-fx-padding: 0;");

            if (date.equals(selectedDate)) {
                card.setStyle(card.getStyle() + "-fx-border-color: rgba(16,185,129,0.35);");
            }

            HBox header = new HBox(6);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setOnMouseEntered(e -> header.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 6px;"));
            header.setOnMouseExited(e -> header.setStyle(""));
            Label dateLbl = new Label(formatDateTitle(date));
            dateLbl.setStyle("-fx-text-fill: #111111; -fx-font-size: 15px; -fx-font-weight: bold;");
            Label suffixLbl = new Label(getDateSuffix(date));
            suffixLbl.setStyle("-fx-text-fill: #111111; -fx-font-size: 14px;");
            Region dateGap = new Region();
            dateGap.setPrefWidth(0.5);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label sumLbl = new Label(String.format("%.0f %s", sum, getParamUnit()));
            sumLbl.setStyle("-fx-text-fill: #39C5BB; -fx-font-size: 15px; -fx-font-weight: bold;");
            header.getChildren().addAll(dateLbl, dateGap, suffixLbl, spacer, sumLbl);
            header.setPadding(new Insets(12, 14, 4, 14));
            card.getChildren().add(header);

            Region headSep = new Region();
            headSep.setStyle("-fx-background-color: #f0f0f0;");
            headSep.setPrefHeight(0.1);
            headSep.setMaxHeight(2);
            headSep.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(headSep);

            for (int i = 0; i < recs.size(); i++) {
                if (i > 0) {
                    Region gap = new Region();
                    gap.setStyle("-fx-background-color: #f0f0f0;");
                    gap.setPrefHeight(0.1);
                    gap.setMaxHeight(2);
                    gap.setMaxWidth(Double.MAX_VALUE);
                    card.getChildren().add(gap);
                }

                MealRecord r = recs.get(i);
                double val = getParamValue(r, currentParam);

                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 6px;"));
                row.setOnMouseExited(e -> row.setStyle(""));
                row.setPadding(new Insets(6, 14, 6, 14));

                VBox nameBox = new VBox(2);
                nameBox.setAlignment(Pos.CENTER_LEFT);
                Label nameLbl = new Label(r.getFoodName());
                nameLbl.setStyle("-fx-text-fill: #222222; -fx-font-size: 16px;");
                Label gramLbl = new Label(String.format("%.0f g", r.getGrams()));
                gramLbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
                nameBox.getChildren().addAll(nameLbl, gramLbl);

                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);
                Label valLbl = new Label(String.format("%.0f %s", val, getParamUnit()));
                valLbl.setStyle("-fx-text-fill: #39C5BB; -fx-font-size: 16px;");

                row.getChildren().addAll(nameBox, rowSpacer, valLbl);
                card.getChildren().add(row);
            }

            mealListBox.getChildren().add(card);
        }
    }

    private void buildCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        double ca = contentArea.getWidth();
        if (ca > 0) rightWidth = (ca - 14) / 2.616;
        double cw = rightWidth;
        if (cw <= 0) return;
        double cellSize = (cw - 12) / 7;
        if (cellSize <= 0) return;
        double rh = Math.max(cellSize - 6, 1);

        calendarGrid.setPrefSize(7 * cellSize, cellSize * 0.65 + 6 * rh);
        calendarGrid.setMaxSize(7 * cellSize, cellSize * 0.65 + 6 * rh);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints(cellSize);
            col.setMinWidth(cellSize);
            col.setMaxWidth(cellSize);
            col.setHgrow(Priority.NEVER);
            calendarGrid.getColumnConstraints().add(col);
        }
        {
            RowConstraints hdr = new RowConstraints(cellSize * 0.65, cellSize * 0.65, cellSize * 0.65);
            hdr.setVgrow(Priority.NEVER);
            calendarGrid.getRowConstraints().add(hdr);
        }
        for (int i = 0; i < 5; i++) {
            RowConstraints row = new RowConstraints(cellSize, cellSize, cellSize);
            row.setVgrow(Priority.NEVER);
            calendarGrid.getRowConstraints().add(row);
        }

        for (int col = 0; col < 7; col++) {
            Label dayLabel = new Label(WEEKDAYS[col]);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 11px;");
            calendarGrid.add(dayLabel, col, 0);
            GridPane.setHalignment(dayLabel, HPos.CENTER);
        }

        LocalDate firstDay = calendarMonth.withDayOfMonth(1);
        int startCol = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = calendarMonth.lengthOfMonth();

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = calendarMonth.withDayOfMonth(d);
            int col = (startCol + d - 1) % 7;
            int row = 1 + (startCol + d - 1) / 7;

            boolean hasData = recordsByDate.containsKey(date);
            double sum = 0;
            if (hasData) {
                sum = getParamSum(recordsByDate.get(date), currentParam);
            }
            boolean isSelected = date.equals(selectedDate);

            VBox cell = new VBox(2);
            cell.setAlignment(Pos.CENTER);
            cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            cell.setPadding(new Insets(3, 0, 3, 0));

            if (isSelected) {
                cell.setStyle("-fx-background-color: rgba(16,185,129,0.12);"
                        + "-fx-border-color: #10b981;"
                        + "-fx-border-width: 1.5px;"
                        + "-fx-border-radius: 4px;"
                        + "-fx-background-radius: 4px;");
            }

            Label numLbl = new Label(String.valueOf(d));
            numLbl.setAlignment(Pos.CENTER);
            numLbl.setStyle("-fx-text-fill: #111111; -fx-font-size: 14px");

            cell.getChildren().add(numLbl);

            Label valLbl;
            if(sum>0)
                valLbl = new Label(String.format("%.0f", sum));
            else valLbl = new Label("");
            valLbl.setAlignment(Pos.CENTER);
            valLbl.setStyle("-fx-text-fill: #10b981; -fx-font-size: 8px;");
            cell.getChildren().add(valLbl);

            if (hasData) {
                cell.setStyle(cell.getStyle() + "-fx-cursor: hand;");
            }
            final LocalDate cellDate = date;
            cell.setOnMouseEntered(e -> {
                if (!cellDate.equals(selectedDate)) {
                    cell.setStyle(cell.getStyle() + "-fx-background-color: #f0f0f0; -fx-background-radius: 4px;");
                }
            });
            cell.setOnMouseExited(e -> {
                if (!cellDate.equals(selectedDate)) {
                    refreshCalendarStyles();
                }
            });
            cell.setOnMouseClicked(e -> {
                if (cellDate.equals(selectedDate)) return;
                selectedDate = cellDate;
                drawChart();
                refreshCalendarStyles();
                refreshList();
            });

            calendarGrid.add(cell, col, row);
            GridPane.setHalignment(cell, HPos.CENTER);
        }
    }

    private double getParamSum(List<MealRecord> recs, Param p) {
        return recs.stream().mapToDouble(r -> getParamValue(r, p)).sum();
    }

    private double getParamValue(MealRecord r, Param p) {
        return switch (p) {
            case CALORIE -> r.getCalories();
            case PROTEIN -> r.getProtein();
            case FAT -> r.getFat();
            case CARB -> r.getCarb();
        };
    }

    private String getParamUnit() {
        return currentParam == Param.CALORIE ? "kcal" : "g";
    }

    private void refreshCalendarStyles() {
        LocalDate firstDay = calendarMonth.withDayOfMonth(1);
        int startCol = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = calendarMonth.lengthOfMonth();

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = calendarMonth.withDayOfMonth(d);
            int col = (startCol + d - 1) % 7;
            int row = 1 + (startCol + d - 1) / 7;
            if (row < 1 || row > 6) continue;
            if (col < 0 || col > 6) continue;

            javafx.scene.Node node = getNodeByRowCol(calendarGrid, col, row);
            if (!(node instanceof VBox)) continue;
            VBox cell = (VBox) node;
            boolean isSelected = date.equals(selectedDate);

            if (isSelected) {
                cell.setStyle("-fx-background-color: rgba(16,185,129,0.12);"
                        + "-fx-border-color: #10b981;"
                        + "-fx-border-width: 1.5px;"
                        + "-fx-border-radius: 4px;"
                        + "-fx-background-radius: 4px;"
                        + (recordsByDate.containsKey(date) ? "-fx-cursor: hand;" : ""));
            } else {
                cell.setStyle(recordsByDate.containsKey(date) ? "-fx-cursor: hand;" : "");
            }

            if (!cell.getChildren().isEmpty() && cell.getChildren().get(0) instanceof Label) {
                Label numLbl = (Label) cell.getChildren().get(0);
                numLbl.setStyle("-fx-text-fill: #222222; -fx-font-size: 14px; "
                        + (isSelected ? "-fx-font-weight: bold;" : ""));
            }
        }
    }

    private static javafx.scene.Node getNodeByRowCol(GridPane grid, int col, int row) {
        for (javafx.scene.Node node : grid.getChildren()) {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            int nr = r == null ? 0 : r;
            int nc = c == null ? 0 : c;
            if (nr == row && nc == col) return node;
        }
        return null;
    }

    private String formatDateTitle(LocalDate date) {
        return DATE_MMDD.format(date);
    }

    private String getDateSuffix(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "今天";
        if (date.equals(today.minusDays(1))) return "昨天";
        String[] cn = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return cn[date.getDayOfWeek().getValue()];
    }

    @FXML
    void handleAddMeal() {
        for (String k : MEAL_KEYS) entries.put(k, new ArrayList<>());
        currentMealType = "LUNCH";
        addMealBtn.setVisible(false);
        new Thread(() -> {
            allFoods = foodService.getAllFoods();
            Platform.runLater(() -> contentArea.getChildren().setAll(buildAddPanel()));
        }).start();
    }

    private VBox buildAddPanel() {
        addPanel = new VBox(10);
        addPanel.setPadding(new Insets(8, 0, 0, 0));

        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("记一餐");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button cancelBtn = new Button("取消");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> reloadOverview());
        head.getChildren().addAll(title, sp, cancelBtn);

        HBox mealTabs = new HBox(4);
        mealTabs.setAlignment(Pos.CENTER_LEFT);
        mealBreakfastBtn = makeMealTab("早餐", "BREAKFAST");
        mealLunchBtn = makeMealTab("午餐", "LUNCH");
        mealDinnerBtn = makeMealTab("晚餐", "DINNER");
        mealSnackBtn = makeMealTab("加餐", "SNACK");
        mealLunchBtn.getStyleClass().setAll("meal-tab-active");
        mealTabs.getChildren().addAll(mealBreakfastBtn, mealLunchBtn, mealDinnerBtn, mealSnackBtn);

        VBox foodArea = new VBox(6);
        foodArea.setStyle("-fx-background-color: rgba(0,0,0,0.02); -fx-background-radius: 8px; -fx-padding: 10px;");

        HBox searchRow = new HBox(8);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("搜索食物...");
        searchField.getStyleClass().add("text-field-dark");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((o, ov, nv) -> updateFoodGrid(nv));
        foodCountLabel = new Label("共 0 种食物");
        foodCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-secondary;");
        searchRow.getChildren().addAll(searchField, foodCountLabel);

        foodGrid = new FlowPane(6, 6);
        foodGrid.setPrefHeight(200);

        ScrollPane foodScroll = new ScrollPane(foodGrid);
        foodScroll.setFitToWidth(true);
        foodScroll.setPrefHeight(200);
        foodScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        foodArea.getChildren().addAll(searchRow, foodScroll);

        VBox selectedArea = new VBox(6);
        selectedArea.setStyle("-fx-background-color: rgba(0,0,0,0.02); -fx-background-radius: 8px; -fx-padding: 10px;");

        HBox selHeader = new HBox(8);
        selHeader.setAlignment(Pos.CENTER_LEFT);
        Label selTitle = new Label("已选食物");
        selTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        mealCalLbl = new Label("0 kcal");
        mealCalLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        clearMealBtn = new Button("清空");
        clearMealBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 4; -fx-border-color: rgba(239,68,68,0.3); -fx-border-radius: 4;");
        clearMealBtn.setOnAction(e -> { getEntries().clear(); refreshSelected(); updateFoodGrid(searchField.getText()); });
        selHeader.getChildren().addAll(selTitle, mealCalLbl, sp2, clearMealBtn);

        selectedFlow = new FlowPane(6, 6);
        selectedFlow.setPrefHeight(120);

        ScrollPane selScroll = new ScrollPane(selectedFlow);
        selScroll.setFitToWidth(true);
        selScroll.setPrefHeight(120);
        selScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        selectedArea.getChildren().addAll(selHeader, selScroll);

        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        totalCalLbl = new Label("总热量: 0 kcal");
        totalCalLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        Button saveBtn = new Button("确认保存");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> saveMeal());
        bottom.getChildren().addAll(totalCalLbl, saveBtn);

        addPanel.getChildren().addAll(head, mealTabs, foodArea, selectedArea, bottom);
        updateFoodGrid("");
        refreshSelected();
        return addPanel;
    }

    private Button makeMealTab(String name, String key) {
        Button btn = new Button(name);
        btn.getStyleClass().add("meal-tab");
        btn.setOnAction(e -> {
            currentMealType = key;
            for (Button b : new Button[]{mealBreakfastBtn, mealLunchBtn, mealDinnerBtn, mealSnackBtn})
                b.getStyleClass().setAll("meal-tab");
            btn.getStyleClass().setAll("meal-tab-active");
            refreshSelected();
            updateFoodGrid(searchField.getText());
        });
        return btn;
    }

    private List<FoodEntry> getEntries() {
        return entries.get(currentMealType);
    }

    private void updateFoodGrid(String keyword) {
        foodGrid.getChildren().clear();
        if (allFoods.isEmpty()) return;

        List<Food> shown = (keyword == null || keyword.isEmpty())
            ? allFoods
            : allFoods.stream().filter(f -> f.getFoodName().contains(keyword)).collect(Collectors.toList());

        Set<Integer> selectedIds = getEntries().stream()
            .map(e -> e.food.getFoodId()).collect(Collectors.toSet());

        for (Food food : shown) {
            boolean isSelected = selectedIds.contains(food.getFoodId());
            Button btn = new Button(food.getFoodName());
            btn.setStyle(isSelected
                ? "-fx-background-color: rgba(16,185,129,0.20); -fx-text-fill: #34d399; -fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: rgba(16,185,129,0.3); -fx-border-radius: 6;"
                : "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #b0b0b0; -fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 6;"
            );
            int fid = food.getFoodId();
            btn.setOnAction(e -> {
                List<FoodEntry> entryList = getEntries();
                Optional<FoodEntry> existing = entryList.stream()
                    .filter(en -> en.food.getFoodId() == fid).findFirst();
                if (existing.isPresent()) {
                    entryList.remove(existing.get());
                } else {
                    entryList.add(new FoodEntry(food, 100));
                }
                refreshSelected();
                updateFoodGrid(searchField.getText());
            });
            foodGrid.getChildren().add(btn);
        }
        foodCountLabel.setText(String.format("共 %d 种食物", shown.size()));
    }

    private void refreshSelected() {
        List<FoodEntry> entryList = getEntries();
        selectedFlow.getChildren().clear();

        if (entryList.isEmpty()) {
            Label empty = new Label("暂无选择");
            empty.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");
            selectedFlow.getChildren().add(empty);
            mealCalLbl.setText("0 kcal");
            updateTotal();
            return;
        }

        double mealCal = 0;
        for (int i = 0; i < entryList.size(); i++) {
            FoodEntry entry = entryList.get(i);
            mealCal += entry.getCalories();
            final int idx = i;

            HBox card = new HBox(4);
            card.setStyle("-fx-padding: 4 6; -fx-background-color: rgba(16,185,129,0.06); -fx-background-radius: 6;");
            card.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(entry.food.getFoodName());
            name.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 12px; -fx-font-weight: bold;");
            name.setPrefWidth(80);
            name.setMaxWidth(80);

            TextField gramField = new TextField(String.valueOf((int) entry.grams));
            gramField.setPrefWidth(50);
            gramField.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: -text-primary; -fx-background-radius: 4; -fx-font-size: 11px;");
            gramField.textProperty().addListener((obs, ov, nv) -> {
                try { double g = Double.parseDouble(nv); if (g > 0) { entry.grams = g; refreshSelected(); } }
                catch (NumberFormatException ignored) {}
            });

            Label unit = new Label("g");
            unit.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

            Label cal = new Label(String.format("%.0f", entry.getCalories()));
            cal.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");
            cal.setPrefWidth(45);

            Button removeBtn = new Button("✕");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 1 4;");
            removeBtn.setOnAction(e -> { entryList.remove(idx); refreshSelected(); updateFoodGrid(searchField.getText()); });

            card.getChildren().addAll(name, gramField, unit, cal, removeBtn);
            selectedFlow.getChildren().add(card);
        }

        mealCalLbl.setText(String.format("%s小计: %.0f kcal", MEAL_NAMES[Arrays.asList(MEAL_KEYS).indexOf(currentMealType)], mealCal));
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (var list : entries.values())
            for (FoodEntry e : list) total += e.getCalories();
        totalCalLbl.setText(String.format("总热量: %.0f kcal", total));
    }

    private void saveMeal() {
        List<MealRecord> recs = new ArrayList<>();
        for (Map.Entry<String, List<FoodEntry>> e : entries.entrySet()) {
            for (FoodEntry fe : e.getValue()) {
                MealRecord r = new MealRecord();
                r.setUserId(BMIApplication.currentUserId);
                r.setFoodId(fe.food.getFoodId());
                r.setMealType(e.getKey());
                r.setGrams(fe.grams);
                r.setRecordDate(LocalDate.now());
                recs.add(r);
            }
        }
        if (recs.isEmpty()) return;

        try {
            recordService.saveRecords(BMIApplication.currentUserId, LocalDate.now(), recs);
            reloadOverview();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("提示"); a.setHeaderText(null); a.setContentText("保存失败: " + ex.getMessage());
            a.showAndWait();
        }
    }

    private void reloadOverview() {
        selectedDate = LocalDate.now();
        calendarMonth = LocalDate.now().withDayOfMonth(1);
        addMealBtn.setVisible(true);
        loadMonthRecords();
        contentArea.getChildren().setAll(leftArea, rightArea);
    }
}
