package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.dao.QuizResultDao.LeaderboardEntry;
import com.bmitracker.service.QuizService;
import com.bmitracker.service.QuizService.QuizQuestion;
import com.bmitracker.util.WrongQuestionStore;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class QuizController {

    @FXML private Label questionNum, questionText;
    @FXML private RadioButton optA, optB, optC, optD;
    @FXML private Label resultScore, resultCorrect, resultWrong;
    @FXML private Button btnClose;
    @FXML private HBox quizArea;
    @FXML private VBox resultArea, resultContent;
    @FXML private HBox prepareArea;
    @FXML private VBox leaderboardPanel, leaderboardList;
    @FXML private ToggleButton btnWeekly, btnAlltime;
    @FXML private StackPane root;
    @FXML private HBox quizNav, resultNav;
    @FXML private Button btnPrevQ, btnNextQ, btnSubmitQ, btnQuitQuiz;
    @FXML private Button btnPrevAll, btnNextAll, btnPrevW, btnNextW, btnBackToPrepare;
    @FXML private VBox quizSheetPanel, quizSheetContent;
    @FXML private ToggleButton quizSheetToggle;
    @FXML private VBox wrongBookPanel;
    @FXML private Label wrongBookCount;
    @FXML private Button btnWrongPractice;
    @FXML private Label sloganLabel;

    private boolean wrongPracticeMode = false;
    private List<String> slogans;
    private final Random random = new Random();
    private List<QuizQuestion> wrongQuestions;

    private final QuizService quizService = new QuizService();
    private List<QuizQuestion> questions;
    private String[] userAnswers;
    private int currentIndex = 0;
    private final ToggleGroup optionGroup = new ToggleGroup();

    private final List<Integer> wrongIdxList = new ArrayList<>();
    private int wrongCursor = 0;
    private VBox reviewPanel;

    @FXML
    void initialize() {
        root.setStyle("-fx-padding: 0;");
        optA.setToggleGroup(optionGroup);
        optB.setToggleGroup(optionGroup);
        optC.setToggleGroup(optionGroup);
        optD.setToggleGroup(optionGroup);
        root.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (quizArea.isVisible()) {
                    RadioButton s = (RadioButton) optionGroup.getSelectedToggle();
                    if (s != null) {
                        e.consume();
                        if (currentIndex == questions.size() - 1) onSubmit();
                        else onNext();
                    }
                } else if (resultArea.isVisible()) {
                    e.consume();
                }
            }
        });
        ToggleGroup tabGroup = new ToggleGroup();
        btnWeekly.setToggleGroup(tabGroup);
        btnAlltime.setToggleGroup(tabGroup);
        tabGroup.selectedToggleProperty().addListener((obs, old, sel) -> {
            boolean weekly = sel == btnWeekly;
            btnWeekly.setStyle(weekly
                ? "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 4 16;"
                : "-fx-background-color: rgba(16,185,129,0.1); -fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 4 16;");
            btnAlltime.setStyle(!weekly
                ? "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 4 16;"
                : "-fx-background-color: rgba(16,185,129,0.1); -fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 4 16;");
            loadLeaderboard(weekly);
        });
        loadLeaderboard(true);
        loadWrongBook();
        loadSlogans();
    }

    private void loadSlogans() {
        slogans = new ArrayList<>();
        try (java.io.InputStream is = getClass().getResourceAsStream("/data/slogans.txt")) {
            if (is == null) return;
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.isEmpty()) slogans.add(line);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        showRandomSlogan();
    }

    private void showRandomSlogan() {
        if (slogans == null || slogans.isEmpty()) return;
        sloganLabel.setText("\"" + slogans.get(random.nextInt(slogans.size())) + "\"");
    }

    @FXML
    void onSloganClick() {
        showRandomSlogan();
    }

    private void loadLeaderboard(boolean weekly) {
        leaderboardList.getChildren().clear();
        List<LeaderboardEntry> entries = weekly ? quizService.getLeaderboardWeekly() : quizService.getLeaderboard();
        if (entries.isEmpty()) {
            Label empty = new Label("暂无记录");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-secondary; -fx-padding: 12 0 0 0;");
            leaderboardList.getChildren().add(empty);
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry e = entries.get(i);
            int rank = i + 1;
            int points = e.getMaxScore() * 5;
            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER_LEFT);
            String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : rank + ".";
            String rankStyle = rank <= 3
                ? "-fx-font-size: 16px;"
                : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: -text-secondary;";
            Label rankLabel = new Label(medal);
            rankLabel.setStyle(rankStyle);
            rankLabel.setPrefWidth(28);
            Label nameLabel = new Label(e.getUserName());
            nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
            nameLabel.setMaxWidth(100);
            nameLabel.setEllipsisString("..");
            Label scoreLabel = new Label(points + "分");
            scoreLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(rankLabel, nameLabel, spacer, scoreLabel);
            leaderboardList.getChildren().add(row);
        }
    }

    private void loadWrongBook() {
        int count = WrongQuestionStore.count(BMIApplication.currentUserId);
        wrongBookCount.setText(String.valueOf(count));
    }

    @FXML
    void onStart() {
        wrongPracticeMode = false;
        sloganLabel.setVisible(false);
        sloganLabel.setManaged(false);
        prepareArea.setVisible(false);
        prepareArea.setManaged(false);
        quizArea.setVisible(true);
        quizArea.setManaged(true);
        loadQuestions();
    }

    @FXML
    void onWrongPractice() {
        if (WrongQuestionStore.count(BMIApplication.currentUserId) < 1) {
            showToast("当前没有错题，先去答题吧！");
            return;
        }
        wrongPracticeMode = true;
        sloganLabel.setVisible(false);
        sloganLabel.setManaged(false);
        prepareArea.setVisible(false);
        prepareArea.setManaged(false);
        quizArea.setVisible(true);
        quizArea.setManaged(true);
        loadQuestions();
    }

    private void loadQuestions() {
        if (wrongPracticeMode) {
            List<WrongQuestionStore.Entry> entries = WrongQuestionStore.load(BMIApplication.currentUserId);
            questions = new ArrayList<>();
            for (WrongQuestionStore.Entry e : entries) {
                String[] parts = e.questionLine.split("\\|", 8);
                if (parts.length < 7) continue;
                String exp = parts.length >= 8 ? parts[7] : "";
                questions.add(new QuizQuestion(Integer.parseInt(parts[0]),
                        parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], exp));
            }
        } else {
            questions = quizService.pickRandom(20);
        }
        userAnswers = new String[questions.size()];
        if (questions.isEmpty()) {
            quizArea.setVisible(false);
            quizArea.setManaged(false);
            prepareArea.setVisible(true);
            prepareArea.setManaged(true);
            loadLeaderboard(btnWeekly.isSelected());
            loadWrongBook();
            return;
        }
        currentIndex = 0;
        btnPrevQ.setOnAction(e -> onPrev());
        btnNextQ.setOnAction(e -> onNext());
        btnSubmitQ.setOnAction(e -> onSubmit());
        btnQuitQuiz.setOnAction(e -> onQuitQuiz());
        buildQuizSheet();
        refresh();
    }

    private void buildQuizSheet() {
        quizSheetContent.getChildren().clear();
        quizSheetPanel.setVisible(true);
        quizSheetPanel.setManaged(true);

        GridPane grid = new GridPane();
        grid.setHgap(6); grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);
        int cols = 4;
        for (int i = 0; i < questions.size(); i++) {
            int idx = i;
            Label cell = new Label(String.valueOf(i + 1));
            cell.setPrefSize(36, 36);
            cell.setMinSize(36, 36);
            cell.setAlignment(Pos.CENTER);
            cell.setStyle("-fx-background-color: rgba(128,128,128,0.12); -fx-text-fill: -text-secondary; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");
            int fi = i;
            cell.setOnMouseClicked(e -> jumpToQuestion(fi));
            grid.add(cell, i % cols, i / cols);
        }
        quizSheetContent.getChildren().add(grid);

        quizSheetToggle.setSelected(true);
        quizSheetContent.setVisible(true);
        quizSheetContent.setManaged(true);
        quizSheetPanel.setPrefWidth(180);
        quizSheetPanel.setMinWidth(180);
        quizSheetPanel.setMaxWidth(180);
        quizSheetToggle.setText("答题卡 ▶");
        quizSheetToggle.setOnAction(e -> {
            boolean expanded = quizSheetToggle.isSelected();
            double target = expanded ? 180 : 40;
            javafx.animation.Timeline anim = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                    new javafx.animation.KeyValue(quizSheetPanel.prefWidthProperty(), target))
            );
            anim.play();
            quizSheetContent.setVisible(expanded);
            quizSheetContent.setManaged(expanded);
            quizSheetToggle.setText(expanded ? "答题卡 ▶" : "◀");
            quizSheetPanel.setMinWidth(expanded ? 180 : 40);
            quizSheetPanel.setMaxWidth(expanded ? 180 : 40);
        });
    }

    private void jumpToQuestion(int idx) {
        saveCurrentAnswer();
        currentIndex = idx;
        refresh();
    }

    private void refresh() {
        if (questions.isEmpty()) return;
        QuizQuestion q = questions.get(currentIndex);
        questionNum.setText("问题 " + (currentIndex + 1) + " / " + questions.size());
        questionText.setText((currentIndex + 1) + ". " + q.getQuestion());
        optA.setText("A. " + q.getOption(0)); optB.setText("B. " + q.getOption(1));
        optC.setText("C. " + q.getOption(2)); optD.setText("D. " + q.getOption(3));

        optionGroup.selectToggle(null);
        if (userAnswers[currentIndex] != null) {
            switch (userAnswers[currentIndex]) {
                case "A" -> optA.setSelected(true);
                case "B" -> optB.setSelected(true);
                case "C" -> optC.setSelected(true);
                case "D" -> optD.setSelected(true);
            }
        }

        btnPrevQ.setVisible(currentIndex > 0);
        btnPrevQ.setManaged(currentIndex > 0);
        btnNextQ.setVisible(currentIndex < questions.size() - 1);
        btnNextQ.setManaged(currentIndex < questions.size() - 1);
        btnSubmitQ.setVisible(currentIndex == questions.size() - 1);
        btnSubmitQ.setManaged(currentIndex == questions.size() - 1);
        refreshQuizSheet();
    }

    private void refreshQuizSheet() {
        GridPane grid = quizSheetContent.getChildren().isEmpty() ? null : (GridPane) quizSheetContent.getChildren().get(0);
        if (grid == null) return;
        for (int i = 0; i < questions.size(); i++) {
            Label cell = (Label) grid.getChildren().get(i);
            String ua = userAnswers[i];
            String style;
            if (i == currentIndex) {
                style = "-fx-background-color: rgba(96,165,250,0.2); -fx-text-fill: #3b82f6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-border-color: #3b82f6; -fx-border-width: 1.5px; -fx-border-radius: 6px;";
            } else if (ua != null) {
                style = "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;";
            } else {
                style = "-fx-background-color: rgba(128,128,128,0.12); -fx-text-fill: -text-secondary; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;";
            }
            cell.setStyle(style);
        }
    }

    @FXML
    void onPrev() { saveCurrentAnswer(); if (currentIndex > 0) { currentIndex--; refresh(); } }
    @FXML
    void onNext() { saveCurrentAnswer(); if (currentIndex < questions.size() - 1) { currentIndex++; refresh(); } }

    private void saveCurrentAnswer() {
        RadioButton s = (RadioButton) optionGroup.getSelectedToggle();
        if (s == optA) userAnswers[currentIndex] = "A";
        else if (s == optB) userAnswers[currentIndex] = "B";
        else if (s == optC) userAnswers[currentIndex] = "C";
        else if (s == optD) userAnswers[currentIndex] = "D";
        else userAnswers[currentIndex] = null;
    }

    @FXML
    void onSubmit() {
        saveCurrentAnswer();

        int correct = 0;
        wrongIdxList.clear();
        for (int i = 0; i < questions.size(); i++) {
            String ua = userAnswers[i];
            QuizQuestion q = questions.get(i);
            if (ua != null && ua.equals(q.getAnswer())) correct++;
            else wrongIdxList.add(i);
        }

        int userId = BMIApplication.currentUserId;
        for (int i = 0; i < questions.size(); i++) {
            String ua = userAnswers[i];
            QuizQuestion q = questions.get(i);
            String fullLine = q.getId() + "|" + q.getQuestion() + "|"
                    + q.getOption(0) + "|" + q.getOption(1) + "|"
                    + q.getOption(2) + "|" + q.getOption(3) + "|"
                    + q.getAnswer() + "|" + q.getExplanation();
            if (ua == null || !ua.equals(q.getAnswer())) {
                WrongQuestionStore.add(userId, fullLine, ua == null ? "-" : ua);
            } else if (wrongPracticeMode) {
                WrongQuestionStore.remove(userId, fullLine);
            }
        }

        quizService.saveResult(BMIApplication.currentUserId, correct, questions.size(),
                String.join(",", Arrays.stream(userAnswers).map(s -> s == null ? "-" : s).toArray(String[]::new)));

        quizArea.setVisible(false);
        quizArea.setManaged(false);
        resultArea.setVisible(true);
        resultArea.setManaged(true);
        int totalScore = correct * 5;
        resultScore.setText(totalScore + " / 100");
        resultCorrect.setText(String.valueOf(correct));
        resultWrong.setText(String.valueOf(questions.size() - correct));

        buildResultLayout(correct);
    }

    private void buildResultLayout(int correct) {
        resultContent.getChildren().clear();

        VBox left = new VBox(12);

        VBox scoreBox = new VBox(4);
        scoreBox.setStyle("-fx-background-color: rgba(16,185,129,0.06); -fx-background-radius: 12px; -fx-padding: 16px;");
        Label scoreTitle = new Label("答题完成！");
        scoreTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        HBox scoreRow = new HBox(20);
        scoreRow.setAlignment(Pos.CENTER_LEFT);
        Label sVal = new Label(); sVal.textProperty().bind(resultScore.textProperty());
        sVal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        Label cLbl = new Label("正确: "); cLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-secondary;");
        Label cVal = new Label(); cVal.textProperty().bind(resultCorrect.textProperty());
        cVal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        Label wLbl = new Label("  错误: "); wLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-secondary;");
        Label wVal = new Label(); wVal.textProperty().bind(resultWrong.textProperty());
        wVal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        Label pLbl = new Label("  得分: "); pLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-secondary;");
        Label pVal = new Label(String.valueOf(correct * 5) + " 分");
        pVal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        scoreRow.getChildren().addAll(sVal, cLbl, cVal, wLbl, wVal, pLbl, pVal);
        scoreBox.getChildren().addAll(scoreTitle, scoreRow);
        left.getChildren().add(scoreBox);

        Label reviewTitle = new Label("错题回顾");
        reviewTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -text-secondary;");
        left.getChildren().add(reviewTitle);

        reviewPanel = new VBox(8);
        reviewPanel.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(reviewPanel, Priority.ALWAYS);
        left.getChildren().add(reviewPanel);
        left.getChildren().add(btnClose);

        // Answer sheet panel (right side)
        HBox main = new HBox(16);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(8);
        right.setPrefWidth(180);
        right.setMinWidth(0);
        right.setMaxWidth(180);
        right.setAlignment(Pos.TOP_CENTER);

        ToggleButton sheetToggle = new ToggleButton("答题卡 ▶");
        sheetToggle.setStyle("-fx-background-color: rgba(16,185,129,0.1); -fx-text-fill: #10b981; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 6 14;");
        sheetToggle.setMaxWidth(Double.MAX_VALUE);

        VBox sheetContent = new VBox(8);
        sheetContent.setAlignment(Pos.TOP_CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(6); grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);
        int cols = 4;
        for (int i = 0; i < questions.size(); i++) {
            int idx = i;
            Label cell = new Label(String.valueOf(i + 1));
            cell.setPrefSize(36, 36);
            cell.setMinSize(36, 36);
            cell.setAlignment(Pos.CENTER);
            String ua = userAnswers[idx];
            QuizQuestion q = questions.get(idx);
            boolean isCorrectAns = ua != null && ua.equals(q.getAnswer());
            String style = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;";
            if (ua == null) style += "-fx-background-color: rgba(128,128,128,0.12); -fx-text-fill: -text-secondary;";
            else if (isCorrectAns) style += "-fx-background-color: rgba(16,185,129,0.25); -fx-text-fill: #10b981;";
            else style += "-fx-background-color: rgba(239,68,68,0.25); -fx-text-fill: #ef4444;";
            cell.setStyle(style);
            cell.setOnMouseClicked(e -> showWrongReview(idx));
            grid.add(cell, i % cols, i / cols);
        }

        HBox legend = new HBox(10);
        legend.setAlignment(Pos.CENTER);
        Label gl = new Label("● 正确"); gl.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px;");
        Label rl = new Label("● 错误"); rl.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
        Label al = new Label("● 未答"); al.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        legend.getChildren().addAll(gl, rl, al);

        sheetContent.getChildren().addAll(grid, legend);

        sheetToggle.setOnAction(e -> {
            boolean expanded = sheetToggle.isSelected();
            double target = expanded ? 180 : 40;
            javafx.animation.Timeline anim = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                    new javafx.animation.KeyValue(right.prefWidthProperty(), target))
            );
            anim.play();
            sheetContent.setVisible(expanded);
            sheetContent.setManaged(expanded);
            sheetToggle.setText(expanded ? "答题卡 ▶" : "◀");
            if (!expanded) {
                right.setMinWidth(40);
                right.setMaxWidth(40);
            } else {
                right.setMinWidth(0);
                right.setMaxWidth(180);
            }
            main.setSpacing(expanded ? 16 : 0);
        });

        right.getChildren().addAll(sheetToggle, sheetContent);
        main.getChildren().addAll(left, right);
        resultContent.getChildren().add(main);

        // Setup result nav buttons
        btnPrevAll.setVisible(true); btnPrevAll.setManaged(true);
        btnNextAll.setVisible(true); btnNextAll.setManaged(true);
        btnPrevW.setVisible(true); btnPrevW.setManaged(true);
        btnNextW.setVisible(true); btnNextW.setManaged(true);

        btnPrevAll.setOnAction(e -> { if (currentIndex > 0) showWrongReview(currentIndex - 1); });
        btnNextAll.setOnAction(e -> { if (currentIndex < questions.size() - 1) showWrongReview(currentIndex + 1); });
        btnPrevW.setOnAction(e -> { int p = findPrevWrong(currentIndex); if (p >= 0) showWrongReview(p); });
        btnNextW.setOnAction(e -> { int n = findNextWrong(currentIndex); if (n >= 0) showWrongReview(n); });
        btnBackToPrepare.setOnAction(e -> onBackToPrepare());

        wrongCursor = 0;
        showWrongReview(wrongIdxList.isEmpty() ? 0 : wrongIdxList.get(0));
    }

    private void showWrongReview(int idx) {
        reviewPanel.getChildren().clear();

        if (idx < 0) idx = 0;
        if (idx >= questions.size()) idx = questions.size() - 1;
        currentIndex = idx;

        if (wrongIdxList.isEmpty()) {
            Label banner = new Label("🎉 全部正确！");
            banner.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #10b981; -fx-padding: 0 0 8 0;");
            reviewPanel.getChildren().add(banner);
        }

        boolean isWrong = wrongIdxList.contains(currentIndex);
        if (isWrong) wrongCursor = wrongIdxList.indexOf(currentIndex);

        QuizQuestion q = questions.get(currentIndex);
        String ua = userAnswers[currentIndex];

        if (isWrong) {
            Label numLabel = new Label("错题 " + (wrongCursor + 1) + " / " + wrongIdxList.size());
            numLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            reviewPanel.getChildren().add(numLabel);
        }

        Label qLabel = new Label((currentIndex + 1) + ". " + q.getQuestion());
        qLabel.setWrapText(true);
        qLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -text-primary; -fx-line-spacing: 4;");
        reviewPanel.getChildren().add(qLabel);

        String[] labels = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            String optText = labels[i] + ". " + q.getOption(i);
            Label optLabel = new Label(optText);
            optLabel.setWrapText(true);
            optLabel.setMaxWidth(Double.MAX_VALUE);
            String base = "-fx-background-radius: 10px; -fx-padding: 10px 14px; -fx-font-size: 13px; -fx-text-fill: -text-primary; -fx-border-radius: 10px;";
            if (q.getAnswer().equals(labels[i])) {
                optLabel.setStyle(base + "-fx-background-color: rgba(16,185,129,0.12); -fx-border-color: #10b981; -fx-border-width: 1.5px;");
            } else if (ua != null && ua.equals(labels[i])) {
                optLabel.setStyle(base + "-fx-background-color: rgba(239,68,68,0.12); -fx-border-color: #ef4444; -fx-border-width: 1.5px;");
            } else {
                optLabel.setStyle(base + "-fx-background-color: rgba(16,185,129,0.05); -fx-border-color: rgba(16,185,129,0.12); -fx-border-width: 1px;");
            }
            reviewPanel.getChildren().add(optLabel);
        }

        Label expTitle = new Label("【解析】");
        expTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -text-secondary; -fx-padding: 8 0 2 0;");
        reviewPanel.getChildren().add(expTitle);

        String expText = q.getExplanation();
        if (expText == null || expText.isEmpty()) {
            String[] abc = {"A", "B", "C", "D"};
            String correctText = "";
            for (int i = 0; i < 4; i++) {
                if (q.getAnswer().equals(abc[i])) { correctText = q.getOption(i); break; }
            }
            expText = q.getQuestion().replaceAll("[？?（）()]+", "") + "正确答案" + q.getAnswer() + "、" + correctText + "。";
        }
        Label expBody = new Label(expText);
        expBody.setWrapText(true);
        expBody.setStyle("-fx-font-size: 13px; -fx-text-fill: #10b981; -fx-padding: 0 0 4 0;");
        reviewPanel.getChildren().add(expBody);

        if (ua != null && !ua.equals(q.getAnswer())) {
            String wrongLabel = q.getOption(ua.charAt(0) - 'A');
            Label yourAns = new Label("❌ 你的答案: " + ua + ". " + wrongLabel);
            yourAns.setStyle("-fx-font-size: 13px; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 2 0 0 0;");
            reviewPanel.getChildren().add(yourAns);
        }

        // Update result nav button states
        btnPrevAll.setDisable(currentIndex <= 0);
        btnNextAll.setDisable(currentIndex >= questions.size() - 1);
        btnPrevW.setDisable(findPrevWrong(currentIndex) < 0);
        btnNextW.setDisable(findNextWrong(currentIndex) < 0);
    }

    private int findPrevWrong(int from) {
        int best = -1;
        for (int w : wrongIdxList) {
            if (w < from && w > best) best = w;
        }
        return best;
    }

    private int findNextWrong(int from) {
        int best = -1;
        for (int w : wrongIdxList) {
            if (w > from && (best < 0 || w < best)) best = w;
        }
        return best;
    }

    @FXML
    void onQuitQuiz() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出答题");
        alert.setHeaderText(null);
        alert.setContentText("退出将不记录此次成绩，确定要退出吗？");
        alert.initOwner(optA.getScene().getWindow());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            quizArea.setVisible(false);
            quizArea.setManaged(false);
            prepareArea.setVisible(true);
            prepareArea.setManaged(true);
            sloganLabel.setVisible(true);
            sloganLabel.setManaged(true);
            showRandomSlogan();
            loadLeaderboard(btnWeekly.isSelected());
        }
    }

    @FXML
    void onBackToPrepare() {
        resultArea.setVisible(false);
        resultArea.setManaged(false);
        prepareArea.setVisible(true);
        prepareArea.setManaged(true);
        leaderboardPanel.setVisible(true);
        leaderboardPanel.setManaged(true);
        sloganLabel.setVisible(true);
        sloganLabel.setManaged(true);
        showRandomSlogan();
        loadLeaderboard(btnWeekly.isSelected());
        loadWrongBook();
    }

    @FXML
    void onClose() {
        try {
            Stage stage = (Stage) root.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String msg) {
        Label toast = new Label(msg);
        toast.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8px;");
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        toast.setTranslateY(-40);
        toast.setOpacity(0);
        root.getChildren().add(toast);

        javafx.animation.FadeTransition ftIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), toast);
        ftIn.setFromValue(0); ftIn.setToValue(1);
        ftIn.setOnFinished(e -> {
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
            pt.setOnFinished(e2 -> {
                javafx.animation.FadeTransition ftOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), toast);
                ftOut.setFromValue(1); ftOut.setToValue(0);
                ftOut.setOnFinished(e3 -> root.getChildren().remove(toast));
                ftOut.play();
            });
            pt.play();
        });
        ftIn.play();
    }
}
