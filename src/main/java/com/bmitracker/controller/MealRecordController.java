package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.MealRecord;
import com.bmitracker.model.User;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MealRecordController {

    @FXML private HBox contentArea, monthNav;
    @FXML private VBox leftArea, rightArea, chartBox, mealListBox, calendarBox;
    @FXML private ScrollPane listScroll;
    @FXML private Canvas chartCanvas;
    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private Button prevMonthBtn, nextMonthBtn;

    private final MealRecordService recordService = new MealRecordService();
    private final UserService userService = new UserService();

    enum Param { CALORIE, PROTEIN, FAT, CARB }
    private Param currentParam = Param.CALORIE;
    private LocalDate selectedDate = LocalDate.now();
    private LocalDate calendarMonth;
    private int userSex = 0;

    private Map<LocalDate, List<MealRecord>> recordsByDate = new LinkedHashMap<>();

    private static final DateTimeFormatter DATE_MMDD = DateTimeFormatter.ofPattern("MM.dd");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy.MM");
    private static final String[] WEEKDAYS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    @FXML
    void initialize() {
        calendarMonth = LocalDate.now().withDayOfMonth(1);

        contentArea.widthProperty().addListener((obs, ov, nv) -> {
            double total = nv.doubleValue() - 14;
            double leftW = total * 1.616 / 2.616;
            double rightW = total / 2.616;
            leftArea.setPrefWidth(leftW);
            rightArea.setPrefWidth(rightW);
            buildCalendar();
        });
        contentArea.heightProperty().addListener((obs, ov, nv) -> {
            if (nv.doubleValue() > 0) buildCalendar();
        });

        chartBox.widthProperty().addListener((obs, ov, nv) -> {
            chartCanvas.setWidth(Math.max(1, nv.doubleValue() - 32));
        });
        chartBox.heightProperty().addListener((obs, ov, nv) -> {
            chartCanvas.setHeight(Math.max(1, nv.doubleValue() - 24));
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

            VBox card = new VBox(4);
            card.setStyle("-fx-background-color: rgba(0,0,0,0.02);"
                    + "-fx-background-radius: 10px;"
                    + "-fx-border-color: rgba(0,0,0,0.06);"
                    + "-fx-border-width: 1px;"
                    + "-fx-border-radius: 10px;"
                    + "-fx-padding: 12 14;");

            if (date.equals(selectedDate)) {
                card.setStyle(card.getStyle() + "-fx-border-color: rgba(16,185,129,0.35);");
            }

            HBox header = new HBox(6);
            header.setAlignment(Pos.CENTER_LEFT);
            Label dateLbl = new Label(formatDateTitle(date));
            dateLbl.setStyle("-fx-text-fill: #111111; -fx-font-size: 15px; -fx-font-weight: bold;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label sumLbl = new Label(String.format("%.0f %s", sum, getParamUnit()));
            sumLbl.setStyle("-fx-text-fill: #10b981; -fx-font-size: 15px; -fx-font-weight: bold;");
            header.getChildren().addAll(dateLbl, spacer, sumLbl);
            header.setPadding(new Insets(0, 0, 4, 0));
            card.getChildren().add(header);

            for (int i = 0; i < recs.size(); i++) {
                if (i > 0) {
                    Separator sep = new Separator();
                    sep.setStyle("-fx-background: rgba(0,0,0,0.06);");
                    sep.setPadding(new Insets(4, 0, 4, 0));
                    card.getChildren().add(sep);
                }

                MealRecord r = recs.get(i);
                double val = getParamValue(r, currentParam);

                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 0, 6, 0));

                VBox nameBox = new VBox(2);
                nameBox.setAlignment(Pos.CENTER_LEFT);
                Label nameLbl = new Label(r.getFoodName());
                nameLbl.setStyle("-fx-text-fill: #222222; -fx-font-size: 15px;");
                Label gramLbl = new Label(String.format("%.0f g", r.getGrams()));
                gramLbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
                nameBox.getChildren().addAll(nameLbl, gramLbl);

                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);
                Label valLbl = new Label(String.format("%.0f %s", val, getParamUnit()));
                valLbl.setStyle("-fx-text-fill: #10b981; -fx-font-size: 15px; -fx-font-weight: bold;");

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

        double available = rightArea.getWidth() - 12;
        if (available <= 0) return;
        double cellSize = available / 7;
        if (cellSize <= 0) return;

        for (int i = 0; i < 7; i++) {
            calendarGrid.getColumnConstraints().add(new ColumnConstraints(cellSize));
        }
        calendarGrid.getRowConstraints().add(new RowConstraints(cellSize * 0.55));
        for (int i = 0; i < 6; i++) {
            calendarGrid.getRowConstraints().add(new RowConstraints(cellSize));
        }

        for (int col = 0; col < 7; col++) {
            Label dayLabel = new Label(WEEKDAYS[col]);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 11px; -fx-font-weight: bold;");
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
            numLbl.setStyle("-fx-text-fill: #222222; -fx-font-size: 14px; "
                    + (isSelected ? "-fx-font-weight: bold;" : ""));

            cell.getChildren().add(numLbl);

            if (hasData && sum > 0) {
                Label valLbl = new Label(String.format("%.0f", sum));
                valLbl.setAlignment(Pos.CENTER);
                valLbl.setStyle("-fx-text-fill: #10b981; -fx-font-size: 10px;");
                cell.getChildren().add(valLbl);
            }

            if (hasData) {
                cell.setStyle(cell.getStyle() + "-fx-cursor: hand;");
            }
            final LocalDate cellDate = date;
            cell.setOnMouseClicked(e -> {
                selectedDate = cellDate;
                drawChart();
                buildCalendar();
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

    private String formatDateTitle(LocalDate date) {
        LocalDate today = LocalDate.now();
        String prefix = DATE_MMDD.format(date);
        if (date.equals(today)) return prefix + " 今天";
        if (date.equals(today.minusDays(1))) return prefix + " 昨天";
        String[] cn = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return prefix + " " + cn[date.getDayOfWeek().getValue()];
    }

    @FXML
    void handleAddMeal() {
    }
}
