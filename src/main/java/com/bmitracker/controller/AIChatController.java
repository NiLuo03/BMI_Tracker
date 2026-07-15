package com.bmitracker.controller;

import javafx.animation.FadeTransition;
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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

public class AIChatController {

    // 单例
    private static AIChatController instance;

    // 三个窗口：猫脸、聊天、粒子尾迹
    private Stage ballStage;
    private Stage chatStage;
    private Stage mainStage;
    private Stage particleStage;
    private Pane particlePane;

    // 聊天组件
    private VBox messageArea;
    private ScrollPane scrollPane;
    private TextField inputField;
    private Button sendBtn;

    // 拖拽状态
    private double dragStartX, dragStartY;
    private double stageStartX, stageStartY;
    private boolean dragging = false;
    private boolean longPressed = false;
    private Timeline longPressTimer;

    // TODO: 后续挪到配置文件
    private static final String API_KEY = "ark-bbc33ed4-cfb8-403d-bfa1-c180e8d9e02f-606ca";
    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String MODEL = "ep-20260714154339-vkt22";

    // HTTP 客户端（线程安全）
    private final HttpClient httpClient;
    // 对话上下文
    private final List<ChatMessage> messages = new ArrayList<>();

    private AIChatController() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    // 单例获取
    public static AIChatController getInstance() {
        if (instance == null) {
            instance = new AIChatController();
        }
        return instance;
    }

    // 显示猫脸按钮，贴在主窗口右侧中间
    public void show() {
        if (ballStage != null && ballStage.isShowing()) return;
        createBallStage();
        if (mainStage != null) {
            double x = mainStage.getX() + mainStage.getWidth() - 44 - 15;
            double y = mainStage.getY() + mainStage.getHeight() / 2 - 32;
            y = Math.max(mainStage.getY() + 15, Math.min(y, mainStage.getY() + mainStage.getHeight() - 64 - 15));
            ballStage.setX(x);
            ballStage.setY(y);
        }
        ballStage.show();
    }

    public void setMainStage(Stage stage) {
        this.mainStage = stage;
    }

    // 隐藏全部
    public void hide() {
        if (ballStage != null && ballStage.isShowing()) {
            ballStage.hide();
        }
        if (chatStage != null && chatStage.isShowing()) {
            chatStage.close();
            chatStage = null;
        }
        hideParticleStage();
    }

    // 创建猫脸 Stage：圆形脸 + 椭圆耳朵/眼睛/腮红 + 无边框透明窗口
    private void createBallStage() {
        ballStage = new Stage();
        ballStage.initStyle(StageStyle.TRANSPARENT);
        ballStage.setAlwaysOnTop(true);
        ballStage.setResizable(false);

        ballStage.setX(0);
        ballStage.setY(100);

        // 脸
        Circle face = new Circle(17);
        face.setFill(Color.WHITE);
        face.setStroke(Color.web("#ddd"));
        face.setStrokeWidth(0.5);

        // 左耳
        Ellipse earL = new Ellipse(8, 7);
        earL.setFill(Color.web("#222"));
        earL.setCenterX(-12);
        earL.setCenterY(-12);

        // 右耳
        Ellipse earR = new Ellipse(8, 7);
        earR.setFill(Color.web("#222"));
        earR.setCenterX(12);
        earR.setCenterY(-12);

        // 左眼
        Ellipse eyeL = new Ellipse(3.5, 4);
        eyeL.setFill(Color.web("#222"));
        eyeL.setCenterX(-6);
        eyeL.setCenterY(-3);

        // 右眼
        Ellipse eyeR = new Ellipse(3.5, 4);
        eyeR.setFill(Color.web("#222"));
        eyeR.setCenterX(6);
        eyeR.setCenterY(-3);

        // 左眼高光
        Ellipse eyeL2 = new Ellipse(1.5, 2);
        eyeL2.setFill(Color.WHITE);
        eyeL2.setCenterX(-5);
        eyeL2.setCenterY(-4.5);

        // 右眼高光
        Ellipse eyeR2 = new Ellipse(1.5, 2);
        eyeR2.setFill(Color.WHITE);
        eyeR2.setCenterX(7);
        eyeR2.setCenterY(-4.5);

        // 鼻子
        Ellipse nose = new Ellipse(2.5, 2);
        nose.setFill(Color.web("#333"));
        nose.setCenterX(0);
        nose.setCenterY(3);

        // 左腮红
        Ellipse cheekL = new Ellipse(4, 3);
        cheekL.setFill(Color.web("#ffcdd2"));
        cheekL.setCenterX(-10);
        cheekL.setCenterY(4);

        // 右腮红
        Ellipse cheekR = new Ellipse(4, 3);
        cheekR.setFill(Color.web("#ffcdd2"));
        cheekR.setCenterX(10);
        cheekR.setCenterY(4);

        // 组装到 StackPane
        StackPane ballPane = new StackPane(face, earL, earR, eyeL, eyeR, eyeL2, eyeR2, nose, cheekL, cheekR);
        ballPane.setPrefSize(44, 44);
        Circle clip = new Circle(22, 22, 22);
        ballPane.setClip(clip);
        ballPane.setEffect(new DropShadow(6, 0, 2, Color.gray(0.4)));

        // 下方"AI助手"文字
        Label aiLabel = new Label("AI助手");
        aiLabel.setFont(Font.font("System", 10));
        aiLabel.setTextFill(Color.web("#333"));
        aiLabel.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        aiLabel.setAlignment(Pos.CENTER);

        VBox root = new VBox(ballPane, aiLabel);
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        Scene ballScene = new Scene(root, 44, 64);
        ballScene.setFill(null);
        ballStage.setScene(ballScene);

        // 鼠标按下：记录起始位置 + 启动长按计时器（300ms）
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

        // 鼠标拖拽：长按后才移动，限制在主窗口范围内，同时生成粒子
        ballPane.setOnMouseDragged(e -> {
            if (longPressed) {
                dragging = true;
                double dx = e.getScreenX() - dragStartX;
                double dy = e.getScreenY() - dragStartY;
                double nx = stageStartX + dx;
                double ny = stageStartY + dy;
                if (mainStage != null) {
                    double mX = mainStage.getX();
                    double mY = mainStage.getY();
                    double mW = mainStage.getWidth();
                    double mH = mainStage.getHeight();
                    nx = Math.max(mX + 5, Math.min(nx, mX + mW - 49));
                    ny = Math.max(mY + 5, Math.min(ny, mY + mH - 49));
                }
                ballStage.setX(nx);
                ballStage.setY(ny);
                spawnParticle(e.getScreenX(), e.getScreenY());
            }
        });

        // 鼠标释放：短按 → 切换聊天；长按拖拽后 → 吸附到边缘
        ballPane.setOnMouseReleased(e -> {
            if (longPressTimer != null) {
                longPressTimer.stop();
            }
            if (!longPressed && !dragging) {
                toggleChat();
            }
            if (dragging) {
                snapToEdge();
                hideParticleStage();
            }
            dragging = false;
            longPressed = false;
        });
    }

    // 计算最近的主窗口边，吸附过去（上下左右四边）
    private void snapToEdge() {
        if (mainStage == null) return;
        double bw = 44, bh = 64;
        double mainX = mainStage.getX();
        double mainY = mainStage.getY();
        double mainW = mainStage.getWidth();
        double mainH = mainStage.getHeight();
        double margin = 15;

        double cx = ballStage.getX() + bw / 2;
        double cy = ballStage.getY() + bh / 2;

        double toLeft = cx - mainX;
        double toRight = (mainX + mainW) - cx;
        double toTop = cy - mainY;
        double toBottom = (mainY + mainH) - cy;

        double minDist = Math.min(Math.min(toLeft, toRight), Math.min(toTop, toBottom));

        double nx = ballStage.getX();
        double ny = ballStage.getY();

        if (minDist == toLeft) {
            nx = mainX + margin;
            ny = Math.max(mainY + margin, Math.min(ballStage.getY(), mainY + mainH - bh - margin));
        } else if (minDist == toRight) {
            nx = mainX + mainW - bw - margin;
            ny = Math.max(mainY + margin, Math.min(ballStage.getY(), mainY + mainH - bh - margin));
        } else if (minDist == toTop) {
            nx = Math.max(mainX + margin, Math.min(ballStage.getX(), mainX + mainW - bw - margin));
            ny = mainY + margin;
        } else {
            nx = Math.max(mainX + margin, Math.min(ballStage.getX(), mainX + mainW - bw - margin));
            ny = mainY + mainH - bh - margin;
        }

        ballStage.setX(nx);
        ballStage.setY(ny);
    }

    // 懒创建粒子尾迹 Stage
    private void ensureParticleStage() {
        if (particleStage != null) return;
        if (mainStage == null) return;
        particleStage = new Stage();
        particleStage.initStyle(StageStyle.TRANSPARENT);
        particleStage.setAlwaysOnTop(true);
        particleStage.setResizable(false);
        particlePane = new Pane();
        particlePane.setStyle("-fx-background-color: transparent;");
        Scene ps = new Scene(particlePane, mainStage.getWidth(), mainStage.getHeight());
        ps.setFill(null);
        particleStage.setScene(ps);
        particleStage.setX(mainStage.getX());
        particleStage.setY(mainStage.getY());
    }

    private void showParticleStage() {
        if (mainStage == null) return;
        ensureParticleStage();
        if (particleStage == null) return;
        particleStage.setX(mainStage.getX());
        particleStage.setY(mainStage.getY());
        particleStage.setWidth(mainStage.getWidth());
        particleStage.setHeight(mainStage.getHeight());
        if (!particleStage.isShowing()) {
            particleStage.show();
            ballStage.toFront();
        }
    }

    private void hideParticleStage() {
        if (particleStage != null && particleStage.isShowing()) {
            particleStage.hide();
            particlePane.getChildren().clear();
        }
    }

    // 在拖拽位置生成一个随机大小/透明度的绿色圆形，淡出后自动清除
    private void spawnParticle(double screenX, double screenY) {
        if (mainStage == null) return;
        showParticleStage();
        double px = screenX - mainStage.getX() + (Math.random() - 0.5) * 8;
        double py = screenY - mainStage.getY() + (Math.random() - 0.5) * 8;
        Circle p = new Circle(2 + Math.random() * 2, Color.web("#1a6b3c", 0.25 + Math.random() * 0.15));
        p.setCenterX(px);
        p.setCenterY(py);
        particlePane.getChildren().add(p);

        FadeTransition ft = new FadeTransition(Duration.millis(400 + Math.random() * 200), p);
        ft.setFromValue(p.getOpacity());
        ft.setToValue(0);
        ft.setOnFinished(e -> particlePane.getChildren().remove(p));
        ft.play();
    }

    // 切换聊天窗口：已打开就聚焦，没打开就新建
    private void toggleChat() {
        if (chatStage != null && chatStage.isShowing()) {
            chatStage.requestFocus();
            return;
        }
        createChatStage();
        chatStage.show();
    }

    // 创建聊天窗口：标题栏 + 消息列表 + 输入框（含拍照按钮）
    private void createChatStage() {
        chatStage = new Stage();
        chatStage.initStyle(StageStyle.UTILITY);
        chatStage.setTitle("AI 助手");
        chatStage.setAlwaysOnTop(true);
        chatStage.setResizable(false);

        // 绿色标题栏
        HBox titleBar = new HBox();
        titleBar.setStyle("-fx-background-color: #10b981; -fx-padding: 8 12;");
        titleBar.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("AI 助手");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System Bold", 14));
        titleBar.getChildren().add(titleLabel);

        // 消息区域
        messageArea = new VBox(8);
        messageArea.setPadding(new Insets(10));
        messageArea.setStyle("-fx-background-color: transparent;");

        scrollPane = new ScrollPane(messageArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(320);
        scrollPane.setStyle("-fx-background: linear-gradient(from 0% 0% to 0% 100%, #c8ecec, #fffdf5); -fx-border-color: transparent;");

        // 输入框 + 发送按钮
        inputField = new TextField();
        inputField.setPromptText("输入消息...");
        inputField.setPrefHeight(36);
        inputField.setStyle("-fx-background-radius: 18; -fx-border-radius: 18; -fx-padding: 0 12;");

        sendBtn = new Button("发送");
        sendBtn.setStyle("-fx-background-color: #1a6b3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
        sendBtn.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        // 拍照按钮（调用摄像头识别食物热量）
        Button cameraBtn = new Button("📷");
        cameraBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 18; -fx-cursor: hand; -fx-padding: 0 4;");
        cameraBtn.setPrefHeight(36);
        cameraBtn.setOnAction(e -> handleCameraCapture());
        Tooltip tt = new Tooltip("拍摄食物并识别热量");
        Tooltip.install(cameraBtn, tt);

        sendBtn.setPrefHeight(36);

        HBox inputBox = new HBox(6, cameraBtn, inputField, sendBtn);
        inputBox.setAlignment(Pos.CENTER_LEFT);
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

    // 发送消息 → 异步调 AI → 更新界面
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

    // 拍照识别：反射调用 sarxos webcam 库 → base64 编码 → 调 AI 识别
    private void handleCameraCapture() {
        addMessage("我", "📷 正在通过摄像头拍摄食物...");
        inputField.setDisable(true);
        sendBtn.setDisable(true);

        new Thread(() -> {
            try {
                // 反射加载 Webcam 类（避免编译时强制依赖）
                Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
                Object webcam = webcamClass.getMethod("getDefault").invoke(null);
                if (webcam == null) {
                    Platform.runLater(() -> {
                        addMessage("AI", "未检测到摄像头，请检查设备连接");
                        inputField.setDisable(false);
                        sendBtn.setDisable(false);
                    });
                    return;
                }
                webcamClass.getMethod("open").invoke(webcam);
                BufferedImage img = (BufferedImage) webcamClass.getMethod("getImage").invoke(webcam);
                webcamClass.getMethod("close").invoke(webcam);

                // 转 base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();
                String base64 = Base64.getEncoder().encodeToString(imageBytes);

                // 聊天区域显示图片缩略图
                Image fxImage = new Image(new ByteArrayInputStream(imageBytes));
                Platform.runLater(() -> {
                    messageArea.getChildren().remove(messageArea.getChildren().size() - 1);

                    ImageView iv = new ImageView(fxImage);
                    iv.setFitWidth(200);
                    iv.setFitHeight(150);
                    iv.setPreserveRatio(true);
                    iv.setStyle("-fx-background-radius: 8;");

                    HBox box = new HBox(iv);
                    box.setAlignment(Pos.CENTER_RIGHT);
                    messageArea.getChildren().add(box);
                    scrollToBottom();

                    addMessage("我", "请识别图中的食物并估算热量");
                });

                // 调 API 识别食物
                String response = callCozeWithImage("请识别图中的食物，列出每种食物的名称，并估算每100g的热量以及图片中食物的总热量。用中文回答。", base64);
                Platform.runLater(() -> {
                    addMessage("AI", response);
                    inputField.setDisable(false);
                    sendBtn.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    messageArea.getChildren().remove(messageArea.getChildren().size() - 1);
                    addMessage("AI", "摄像头识别失败：" + e.getMessage() + "，请检查摄像头权限");
                    inputField.setDisable(false);
                    sendBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (scrollPane != null) scrollPane.setVvalue(1.0);
        });
    }

    // Coze 图片识别 API：先试 inline 格式，不行再换结构化 image_url 格式
    private String callCozeWithImage(String text, String base64Image) throws Exception {
        String userContent = escapeJson(text)
                + "\\n\\n![photo](data:image/jpeg;base64," + base64Image + ")";

        String messagesJson = "{\"role\":\"system\",\"content\":\"你是一位专业的营养师，擅长识别食物并估算热量。请用中文回答。\"}";
        messagesJson += ",{\"role\":\"user\",\"content\":\"" + userContent + "\"}";

        String json = "{\"model\":\"" + MODEL + "\",\"messages\":[" + messagesJson + "]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .timeout(java.time.Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        // 第一次尝试成功且未被拒绝就返回
        if (response.statusCode() == 200) {
            String msg = extractMessage(body);
            if (msg != null && !msg.isEmpty() && !msg.contains("不支持") && !msg.contains("无法识别")) {
                return msg;
            }
        }
        // 备用：结构化 image_url 格式
        String altJson = "{\"model\":\"" + MODEL + "\",\"messages\":["
                + "{\"role\":\"system\",\"content\":\"你是一位专业的营养师，擅长识别食物并估算热量。请用中文回答。\"},"
                + "{\"role\":\"user\",\"content\":["
                + "{\"type\":\"text\",\"text\":\"" + escapeJson(text) + "\"},"
                + "{\"type\":\"image_url\",\"image_url\":{\"url\":\"data:image/jpeg;base64," + base64Image + "\"}}"
                + "]}]}";

        HttpRequest altRequest = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .timeout(java.time.Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(altJson))
                .build();

        HttpResponse<String> altResponse = httpClient.send(altRequest, HttpResponse.BodyHandlers.ofString());
        if (altResponse.statusCode() == 200) {
            return extractMessage(altResponse.body());
        }
        throw new RuntimeException("API识别失败: " + altResponse.body());
    }

    // 在聊天区域加一条消息（AI 绿色气泡居左，用户深绿气泡居右）
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
        scrollToBottom();
    }

    // 调 Coze 文字对话 API（携带历史上下文）
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

    // 手写解析 Coze 响应中的 content 字段，避免引入 JSON 库
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

    // JSON 字符串转义（反斜杠、引号、换行等）
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

    // 对话消息结构
    private static class ChatMessage {
        String role;
        String content;
        ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
