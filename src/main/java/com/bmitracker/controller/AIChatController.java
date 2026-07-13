package com.bmitracker.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AIChatController {

    private static AIChatController instance;

    private Stage ballStage;
    private Stage chatStage;
    private Stage mainStage;

    private Circle ball;
    private VBox messageArea;
    private ScrollPane scrollPane;
    private TextField inputField;
    private Button sendBtn;

    private double dragStartX, dragStartY;
    private double stageStartX, stageStartY;
    private boolean dragging = false;
    private boolean longPressed = false;
    private Timeline longPressTimer;

    private static final String API_KEY = "ark-bbc33ed4-cfb8-403d-bfa1-c180e8d9e02f-606ca";
    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String MODEL = "ep-20260713112535-75rjx";

    private final HttpClient httpClient;
    private final List<ChatMessage> messages = new ArrayList<>();

    private AIChatController() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    public static AIChatController getInstance() {
        if (instance == null) {
            instance = new AIChatController();
        }
        return instance;
    }

    public void show() {
        if (ballStage != null && ballStage.isShowing()) return;
        createBallStage();
        ballStage.show();
    }

    public void setMainStage(Stage stage) {
        this.mainStage = stage;
    }

    public void hide() {
        if (ballStage != null && ballStage.isShowing()) {
            ballStage.hide();
        }
        if (chatStage != null && chatStage.isShowing()) {
            chatStage.close();
            chatStage = null;
        }
    }

    private void createBallStage() {
        ballStage = new Stage();
        ballStage.initStyle(StageStyle.TRANSPARENT);
        ballStage.setAlwaysOnTop(true);
        ballStage.setResizable(false);

        ball = new Circle(22, Color.web("#1a6b3c"));
        ball.setStroke(Color.web("#145530"));
        ball.setStrokeWidth(2);
        ball.setEffect(new DropShadow(6, 0, 2, Color.gray(0.4)));

        Label aiLabel = new Label("AI");
        aiLabel.setTextFill(Color.WHITE);
        aiLabel.setFont(Font.font("System Bold", 12));

        StackPane ballPane = new StackPane(ball, aiLabel);
        ballPane.setPrefSize(44, 44);

        Scene ballScene = new Scene(ballPane, 44, 44);
        ballScene.setFill(null);
        ballStage.setScene(ballScene);

        ballStage.setX(0);
        ballStage.setY(100);

        ballPane.setOnMousePressed(e -> {
            dragStartX = e.getScreenX();
            dragStartY = e.getScreenY();
            stageStartX = ballStage.getX();
            stageStartY = ballStage.getY();
            dragging = false;
            longPressed = false;

            longPressTimer = new Timeline(
                    new KeyFrame(Duration.millis(300), ev -> longPressed = true)
            );
            longPressTimer.setCycleCount(1);
            longPressTimer.play();
        });

        ballPane.setOnMouseDragged(e -> {
            if (longPressed) {
                dragging = true;
                double dx = e.getScreenX() - dragStartX;
                double dy = e.getScreenY() - dragStartY;
                ballStage.setX(stageStartX + dx);
                ballStage.setY(stageStartY + dy);
            }
        });

        ballPane.setOnMouseReleased(e -> {
            if (longPressTimer != null) {
                longPressTimer.stop();
            }
            if (!longPressed && !dragging) {
                toggleChat();
            }
            if (dragging) {
                snapToEdge();
            }
            dragging = false;
            longPressed = false;
        });
    }

    private void snapToEdge() {
        if (mainStage == null) return;
        double bw = 44, bh = 44;
        double mainX = mainStage.getX();
        double mainY = mainStage.getY();
        double mainW = mainStage.getWidth();
        double mainH = mainStage.getHeight();
        double margin = 5;

        double cx = ballStage.getX() + bw / 2;
        double cy = ballStage.getY() + bh / 2;

        double left = mainX;
        double right = mainX + mainW;
        double top = mainY;
        double bottom = mainY + mainH;

        double toLeft = cx - left;
        double toRight = right - cx;
        double toTop = cy - top;
        double toBottom = bottom - cy;

        double minDist = Math.min(Math.min(toLeft, toRight), Math.min(toTop, toBottom));

        if (minDist == toLeft) {
            ballStage.setX(left - margin);
            ballStage.setY(Math.max(top, Math.min(ballStage.getY(), bottom - bh)));
        } else if (minDist == toRight) {
            ballStage.setX(right - bw + margin);
            ballStage.setY(Math.max(top, Math.min(ballStage.getY(), bottom - bh)));
        } else if (minDist == toTop) {
            ballStage.setX(Math.max(left, Math.min(ballStage.getX(), right - bw)));
            ballStage.setY(top - margin);
        } else {
            ballStage.setX(Math.max(left, Math.min(ballStage.getX(), right - bw)));
            ballStage.setY(bottom - bh + margin);
        }
    }

    private void toggleChat() {
        if (chatStage != null && chatStage.isShowing()) {
            chatStage.requestFocus();
            return;
        }
        createChatStage();
        chatStage.show();
    }

    private void createChatStage() {
        chatStage = new Stage();
        chatStage.initStyle(StageStyle.UTILITY);
        chatStage.setTitle("AI 助手");
        chatStage.setAlwaysOnTop(true);
        chatStage.setResizable(false);

        VBox titleBar = new VBox();
        titleBar.setStyle("-fx-background-color: #1a6b3c; -fx-padding: 8 12;");
        Label titleLabel = new Label("AI 助手");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System Bold", 14));
        titleBar.getChildren().add(titleLabel);

        messageArea = new VBox(8);
        messageArea.setPadding(new Insets(10));
        messageArea.setStyle("-fx-background-color: #f5f7f6;");

        scrollPane = new ScrollPane(messageArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(320);
        scrollPane.setStyle("-fx-background: #f5f7f6; -fx-border-color: transparent;");

        inputField = new TextField();
        inputField.setPromptText("输入消息...");
        inputField.setPrefHeight(36);
        inputField.setStyle("-fx-background-radius: 18; -fx-border-radius: 18; -fx-padding: 0 12;");

        sendBtn = new Button("发送");
        sendBtn.setStyle("-fx-background-color: #1a6b3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
        sendBtn.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        HBox inputBox = new HBox(8, inputField, sendBtn);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        VBox root = new VBox(titleBar, scrollPane, inputBox);
        root.setPrefSize(340, 430);

        Scene chatScene = new Scene(root);
        chatStage.setScene(chatScene);

        messages.clear();

        chatStage.setOnCloseRequest(e -> {
            chatStage = null;
        });

        addMessage("AI", "你好！我是AI助手，有什么可以帮你的吗？");
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        inputField.clear();

        addMessage("我", text);
        messages.add(new ChatMessage("user", text));

        inputField.setDisable(true);
        sendBtn.setDisable(true);

        new Thread(() -> {
            try {
                String response = callCozeChat(messages);
                Platform.runLater(() -> {
                    addMessage("AI", response);
                    messages.add(new ChatMessage("assistant", response));
                    inputField.setDisable(false);
                    sendBtn.setDisable(false);
                    inputField.requestFocus();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    addMessage("AI", "抱歉，服务繁忙，请稍后再试。");
                    inputField.setDisable(false);
                    sendBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void addMessage(String sender, String content) {
        Label label = new Label(content);
        label.setWrapText(true);
        label.setMaxWidth(240);
        label.setPadding(new Insets(8, 12, 8, 12));

        HBox box = new HBox();
        if ("AI".equals(sender)) {
            label.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 12; -fx-text-fill: #333; -fx-font-size: 13;");
            box.getChildren().add(label);
            box.setAlignment(Pos.CENTER_LEFT);
        } else {
            label.setStyle("-fx-background-color: #1a6b3c; -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 13;");
            box.getChildren().add(label);
            box.setAlignment(Pos.CENTER_RIGHT);
        }
        messageArea.getChildren().add(box);

        Platform.runLater(() -> {
            if (scrollPane != null) {
                scrollPane.setVvalue(1.0);
            }
        });
    }

    private String callCozeChat(List<ChatMessage> chatMessages) throws Exception {
        StringBuilder messagesJson = new StringBuilder();
        messagesJson.append("{\"role\":\"system\",\"content\":\"你是一位友好的AI助手，可以回答用户的各种问题，请用中文回复。\"}");

        for (ChatMessage msg : chatMessages) {
            messagesJson.append(",{\"role\":\"")
                    .append(msg.role)
                    .append("\",\"content\":\"")
                    .append(escapeJson(msg.content))
                    .append("\"}");
        }

        String json = "{\"model\":\"" + MODEL + "\",\"messages\":[" + messagesJson + "]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .timeout(java.time.Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return extractMessage(response.body());
        }
        throw new RuntimeException("API error: " + response.statusCode());
    }

    private String extractMessage(String body) {
        String key = "\"message\":{\"content\":\"";
        int start = body.indexOf(key);
        if (start < 0) return body;
        start += key.length();
        StringBuilder result = new StringBuilder();
        for (int i = start; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '\\' && i + 1 < body.length()) {
                char next = body.charAt(i + 1);
                if (next == '"') { result.append('"'); i++; }
                else if (next == 'n') { result.append('\n'); i++; }
                else if (next == 'r') { result.append('\r'); i++; }
                else if (next == 't') { result.append('\t'); i++; }
                else if (next == '\\') { result.append('\\'); i++; }
                else { result.append(c); }
            } else if (c == '"') { break; }
            else { result.append(c); }
        }
        return result.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static class ChatMessage {
        String role;
        String content;
        ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
