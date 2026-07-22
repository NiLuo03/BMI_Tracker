package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.component.ImageCropper;
import com.bmitracker.component.TitleBar;
import com.bmitracker.component.WheelPicker;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.paint.CycleMethod;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.Random;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class MainController {

    private static MainController instance;

    @FXML private StackPane contentPane;
    @FXML private StackPane glassPanel;
    @FXML private StackPane glassContent;
    @FXML private TitleBar titleBar;
    @FXML private StackPane root;
    @FXML private Rectangle rootClip;
    @FXML private Region backdrop;
    @FXML private Button toggleNavBtn;
    @FXML private Button btnHome, btnBmi, btnHistory, btnPredict, btnDiet, btnCompare, btnRank, btnMealRecord, btnQuiz;
    @FXML private MenuButton menuSettings;
    @FXML private VBox sidebar;
    @FXML private HBox avatarHBox;
    @FXML private VBox avatarStatusBox;
    @FXML private VBox mainMenuBox, mainMenuInner, sidebarSpacer, serviceMenuBox, serviceMenuInner, sidebarBottomSpacer;
    @FXML private Label categoryMainLabel, categoryServiceLabel;
    @FXML private Label avatarLabel, sidebarUserName;
    @FXML private Label welcomeLabel, bmiStatusLabel, trendLabel;
    @FXML private Label dashBmi, dashStatus, dashIdealWeight, dashRecords, dashTrend;
    @FXML private Label historySummary;
    @FXML private VBox homeContent;
    @FXML private TextField homeHeightField;
    @FXML private TextField homeWeightField;
    @FXML private Label heroBmiValue, heroBmiStatus, lastRecordLabel, weightChangeLabel;
    @FXML private Label zoneDescription;
    @FXML private StackPane zoneTrack;
    @FXML private Region zoneIndicator;
    @FXML private LineChart<Number, Number> miniChart;
    @FXML private NumberAxis miniXAxis, miniYAxis;
    @FXML private Label miniChartEmpty;
    @FXML private Label sloganLabel;
    @FXML private Label statAvgBmi, statMaxBmi, statMinBmi, statHealthyDays;
    private Popup homeHeightPopup;
    private Popup homeWeightPopup;
    private Node quizView;

    private boolean navExpanded = true;
    private final BmiService bmiService = new BmiService();
    private final List<String> navHistory = new ArrayList<>();
    private int navIndex = -1;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static List<String> slogans;
    private static final Random RANDOM = new Random();
    private Pane rootPane;

    private static final String[] BACKDROP_COLORS = {
        "#000000", "#050f0a", "#0a0a1a", "#100a1a", "#111111", "#ffffff"
    };

    private void applyNavState(boolean expanded) {
        Button[] btns = {btnHome, btnBmi, btnHistory, btnPredict, btnDiet, btnCompare, btnRank, btnMealRecord, btnQuiz};
        String[][] btnDefs = {
            {" 首页", ""}, {" 记录", ""}, {" 数据分析", ""}, {" 趋势预测", ""},
            {" 膳食推荐", ""}, {" 食物对比", ""}, {" 食物榜单", ""}, {" 膳食记录", ""}, {" 健康问答", ""}
        };
        for (int i = 0; i < btns.length; i++) {
            btns[i].setText(expanded ? btnDefs[i][0] : btnDefs[i][1]);
            btns[i].setMaxWidth(expanded ? Double.MAX_VALUE : 38);
            btns[i].setAlignment(expanded ? javafx.geometry.Pos.CENTER_LEFT : javafx.geometry.Pos.CENTER);
        }
        toggleNavBtn.setMaxWidth(expanded ? Double.MAX_VALUE : 38);
        toggleNavBtn.setAlignment(expanded ? javafx.geometry.Pos.CENTER_LEFT : javafx.geometry.Pos.CENTER);
        StackPane toggleBox = (StackPane) toggleNavBtn.getGraphic();
        if (toggleBox != null && !toggleBox.getChildren().isEmpty()) {
            SVGPath toggleSvg = (SVGPath) toggleBox.getChildren().get(0);
            toggleSvg.setScaleX(expanded ? 0.018 : -0.018);
        }
        menuSettings.setVisible(expanded);
        menuSettings.setManaged(expanded);
        sidebar.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        if (avatarHBox != null) avatarHBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        if (avatarStatusBox != null) {
            avatarStatusBox.setVisible(expanded);
            avatarStatusBox.setManaged(expanded);
        }
        if (categoryMainLabel != null) {
            categoryMainLabel.setVisible(expanded);
            categoryMainLabel.setManaged(expanded);
        }
        if (categoryServiceLabel != null) {
            categoryServiceLabel.setVisible(expanded);
            categoryServiceLabel.setManaged(expanded);
        }
        if (sidebarSpacer != null) {
            sidebarSpacer.setVisible(expanded);
            sidebarSpacer.setManaged(expanded);
        }
        if (mainMenuBox != null) VBox.setVgrow(mainMenuBox, expanded ? null : javafx.scene.layout.Priority.ALWAYS);
        if (serviceMenuBox != null) VBox.setVgrow(serviceMenuBox, expanded ? null : javafx.scene.layout.Priority.ALWAYS);
        if (mainMenuBox != null) mainMenuBox.setAlignment(expanded ? javafx.geometry.Pos.TOP_LEFT : javafx.geometry.Pos.CENTER);
        if (serviceMenuBox != null) serviceMenuBox.setAlignment(expanded ? javafx.geometry.Pos.TOP_LEFT : javafx.geometry.Pos.CENTER);
        Insets margin = expanded ? Insets.EMPTY : new Insets(0, 0, 0, 7);
        for (Button b : btns) VBox.setMargin(b, margin);
        VBox.setMargin(toggleNavBtn, margin);
    }

    @FXML
    void toggleNav() {
        navExpanded = !navExpanded;
        double target = navExpanded ? 150 : 48;
        Timeline anim = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(sidebar.prefWidthProperty(), target))
        );
        anim.play();
        applyNavState(navExpanded);
    }

    private static boolean isBrightColor(String hex) {
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            if (h.length() == 3) h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
            int rgb = Integer.parseInt(h, 16);
            double r = ((rgb >> 16) & 0xff) / 255.0;
            double g = ((rgb >> 8) & 0xff) / 255.0;
            double b = (rgb & 0xff) / 255.0;
            return 0.299 * r + 0.587 * g + 0.114 * b > 0.5;
        } catch (Exception e) { return true; }
    }

    private static boolean isBrightImage(Image image) {
        try {
            PixelReader pr = image.getPixelReader();
            int w = (int) image.getWidth();
            int h = (int) image.getHeight();
            int sx = w / 2, sy = h / 2;
            double total = 0;
            int count = 0;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int px = sx + dx, py = sy + dy;
                    if (px >= 0 && px < w && py >= 0 && py < h) {
                        int argb = pr.getArgb(px, py);
                        double r = ((argb >> 16) & 0xff) / 255.0;
                        double g = ((argb >> 8) & 0xff) / 255.0;
                        double b = (argb & 0xff) / 255.0;
                        total += 0.299 * r + 0.587 * g + 0.114 * b;
                        count++;
                    }
                }
            }
            return count > 0 && total / count > 0.5;
        } catch (Exception e) { return true; }
    }

    private void applyTheme(boolean bright) {
        if (rootPane == null) return;
        rootPane.getStyleClass().removeAll("light-theme", "black-theme");
        rootPane.getStyleClass().add(bright ? "light-theme" : "black-theme");
    }

    public void changeBackdrop(String hexColor) {
        backdrop.setStyle("-fx-background-color: " + hexColor + ";");
        applyTheme(isBrightColor(hexColor));
        saveBackdropPref(hexColor);
    }

    private void clearOtherBackdropPrefs(Preferences p, String keepKey) {
        if (!keepKey.equals("backdrop_"))      p.remove("backdrop_" + BMIApplication.currentUserId);
        if (!keepKey.equals("backdrop_image_")) p.remove("backdrop_image_" + BMIApplication.currentUserId);
        if (!keepKey.equals("backdrop_crop_"))  p.remove("backdrop_crop_" + BMIApplication.currentUserId);
    }

    private void saveBackdropPref(String value) {
        if (BMIApplication.currentUserId <= 0) return;
        try {
            Preferences p = Preferences.userNodeForPackage(getClass());
            clearOtherBackdropPrefs(p, "backdrop_");
            p.put("backdrop_" + BMIApplication.currentUserId, value);
            p.flush();
        } catch (Exception e) { /* ignore */ }
    }

    private void saveBackdropImagePref(File file) {
        if (BMIApplication.currentUserId <= 0) return;
        try {
            Preferences p = Preferences.userNodeForPackage(getClass());
            clearOtherBackdropPrefs(p, "backdrop_image_");
            p.put("backdrop_image_" + BMIApplication.currentUserId, file.getAbsolutePath());
            p.flush();
        } catch (Exception e) { /* ignore */ }
    }

    private void saveBackdropCropPref(File originalFile, int x, int y, int w, int h) {
        if (BMIApplication.currentUserId <= 0) return;
        try {
            Preferences p = Preferences.userNodeForPackage(getClass());
            clearOtherBackdropPrefs(p, "backdrop_crop_");
            p.put("backdrop_crop_" + BMIApplication.currentUserId,
                  originalFile.getAbsolutePath() + "|" + x + "|" + y + "|" + w + "|" + h);
            p.flush();
        } catch (Exception e) { /* ignore */ }
    }

    private void loadBackdropPref() {
        if (BMIApplication.currentUserId <= 0) return;
        try {
            Preferences p = Preferences.userNodeForPackage(getClass());

            String cropStr = p.get("backdrop_crop_" + BMIApplication.currentUserId, null);
            if (cropStr != null) {
                String[] parts = cropStr.split("\\|");
                if (parts.length == 5) {
                    File f = new File(parts[0]);
                    if (f.exists()) {
                        int cx = Integer.parseInt(parts[1]);
                        int cy = Integer.parseInt(parts[2]);
                        int cw = Integer.parseInt(parts[3]);
                        int ch = Integer.parseInt(parts[4]);
                        Image full = new Image(f.toURI().toString());
                        if (cx + cw <= (int) full.getWidth() && cy + ch <= (int) full.getHeight()) {
                            Image cropped = new WritableImage(full.getPixelReader(), cx, cy, cw, ch);
                            applyBackdropImage(cropped);
                            return;
                        }
                    }
                }
            }

            String imagePath = p.get("backdrop_image_" + BMIApplication.currentUserId, null);
            if (imagePath != null) {
                File f = new File(imagePath);
                if (f.exists()) { applyBackdropImageDirect(f); return; }
            }

            String color = p.get("backdrop_" + BMIApplication.currentUserId, null);
            if (color != null) {
                applyBackdropDirect(color);
                return;
            }
        } catch (Exception e) { /* ignore */ }
        applyBackdropDirect("#ffffff");
    }

    private void applyBackdropDirect(String hexColor) {
        backdrop.setStyle("-fx-background-color: " + hexColor + ";");
        applyTheme(isBrightColor(hexColor));
    }

    private void applyBackdropImageDirect(File imageFile) {
        Image img = new Image(imageFile.toURI().toString());
        backdrop.setStyle("-fx-background-image: url('" + imageFile.toURI() + "'); -fx-background-size: cover; -fx-background-position: center;");
        applyTheme(isBrightImage(img));
    }

    private void applyBackdropImage(Image image) {
        backdrop.setStyle(null);
        backdrop.setBackground(new Background(new BackgroundImage(
            image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
        )));
        applyTheme(isBrightImage(image));
    }

    public void changeBackdropImage(java.io.File imageFile) {
        Image img = new Image(imageFile.toURI().toString());
        backdrop.setStyle("-fx-background-image: url('" + imageFile.toURI() + "'); -fx-background-size: cover; -fx-background-position: center;");
        applyTheme(isBrightImage(img));
        saveBackdropImagePref(imageFile);
    }

    public void changeBackdropCropped(File originalFile, int cropX, int cropY, int cropW, int cropH) {
        Image full = new Image(originalFile.toURI().toString());
        Image cropped = new WritableImage(full.getPixelReader(), cropX, cropY, cropW, cropH);
        applyBackdropImage(cropped);
        saveBackdropCropPref(originalFile, cropX, cropY, cropW, cropH);
    }

    public void changeBackdrop(int idx) {
        if (idx >= 0 && idx < BACKDROP_COLORS.length) {
            changeBackdrop(BACKDROP_COLORS[idx]);
        }
    }

    @FXML
    void initialize() {
        try {
            java.net.URL fontUrl = getClass().getResource("/fonts/AlimamaShuHeiTi-Bold.ttf");
            if (fontUrl != null) {
                Font.loadFont(fontUrl.toExternalForm(), 14);
            } else {
                System.err.println("字体文件未找到: /fonts/AlimamaShuHeiTi-Bold.ttf");
            }
        } catch (Exception e) {
            System.err.println("加载字体失败: " + e.getMessage());
        }
        rootClip.widthProperty().bind(root.widthProperty());
        rootClip.heightProperty().bind(root.heightProperty());
        instance = this;
        loadSidebarUser();
        loadDashboardData();
        if (root != null) rootPane = root;

        setupHomePickers();

        SVGPath homeIcon = new SVGPath();
        homeIcon.setContent("M861.866667 401.749333a13.960533 13.960533 0 0 0-6.485334-11.400533l-332.8-225.041067a19.285333 19.285333 0 0 0-21.162666 0l-332.8 225.041067a13.960533 13.960533 0 0 0-6.485334 11.434667v421.205333c0 19.8656 17.476267 38.912 42.666667 38.912h614.4c25.1904 0 42.666667-19.0464 42.666667-38.912V401.749333z m-288.017067 158.242134a61.883733 61.883733 0 1 0-123.733333 0 61.883733 61.883733 0 0 0 123.767466 0z m68.266667 0a130.116267 130.116267 0 1 1-260.232534 0 130.116267 130.116267 0 0 1 260.232534 0z m288.017066 262.9632c0 60.791467-51.3024 107.178667-110.933333 107.178666H204.8c-59.630933 0-110.933333-46.3872-110.933333-107.178666V401.749333c0-27.5456 14.0288-52.736 36.522667-67.925333l332.8-225.0752c29.354667-19.831467 68.266667-19.831467 97.621334 0l332.8 225.041067a82.158933 82.158933 0 0 1 36.522666 67.9936v421.205333z");
        homeIcon.setStyle("-fx-fill: -text-primary;");
        homeIcon.setScaleX(0.018);
        homeIcon.setScaleY(0.018);
        StackPane homeBox = new StackPane(homeIcon);
        homeBox.setPrefSize(18, 18);
        homeBox.setMinSize(18, 18);
        homeBox.setMaxSize(18, 18);
        btnHome.setGraphic(homeBox);
        btnHome.setText(" 首页");

        SVGPath gearIcon = new SVGPath();
        gearIcon.setContent("M407.313067 176.059733c26.692267-109.568 182.6816-109.568 209.373866 0l0.341334 1.467734a39.424 39.424 0 0 0 22.869333 25.770666 39.389867 39.389867 0 0 0 35.601067-2.7648c96.426667-58.743467 206.7456 51.541333 148.002133 147.968h-0.034133a39.424 39.424 0 0 0 24.3712 58.88c106.359467 25.736533 109.568 172.987733 9.966933 206.370134l-9.9328 2.8672a39.458133 39.458133 0 0 0-24.3712 58.88l4.983467 9.079466c46.865067 94.071467-59.5968 195.822933-152.9856 138.922667a39.458133 39.458133 0 0 0-58.88 24.3712c-26.589867 109.738667-182.613333 109.636267-209.237334 0v-0.034133a39.389867 39.389867 0 0 0-58.845866-24.3712l-0.034134 0.034133c-96.426667 58.709333-206.7456-51.575467-148.002133-148.002133h0.034133a39.458133 39.458133 0 0 0-24.3712-58.845867c-109.7728-26.555733-109.6704-182.647467 0-209.271467a39.458133 39.458133 0 0 0 24.337067-58.845866v-0.034134c-58.709333-96.392533 51.541333-206.711467 147.968-148.002133a39.424 39.424 0 0 0 58.845867-24.3712v-0.068267z m142.677333 14.882134c-10.717867-38.8096-66.628267-38.4-76.356267 1.2288a107.690667 107.690667 0 0 1-112.913066 82.0224 107.690667 107.690667 0 0 1-47.7184-15.394134c-35.293867-21.504-75.707733 18.909867-54.203734 54.203734 37.956267 62.2592 4.3008 143.496533-66.56 160.699733-40.174933 9.762133-40.106667 66.901333 0 76.5952l6.144 1.672533a107.758933 107.758933 0 0 1 63.556267 153.565867l-3.140267 5.495467c-21.504 35.293867 18.875733 75.639467 54.1696 54.1696l5.495467-3.140267a107.690667 107.690667 0 0 1 155.2384 69.666133l1.024 3.6864c12.253867 36.522667 66.184533 35.157333 75.5712-3.652266a107.758933 107.758933 0 0 1 160.699733-66.56c35.293867 21.504 75.707733-18.875733 54.203734-54.1696a107.690667 107.690667 0 0 1 66.56-160.733867c38.912-9.454933 40.0384-63.351467 3.652266-75.537067l-3.652266-1.058133a107.656533 107.656533 0 0 1-66.56-160.768c21.504-35.259733-18.909867-75.6736-54.203734-54.1696l0.034134 0.034133a107.656533 107.656533 0 0 1-160.733867-66.56l-0.341333-1.297066zM563.2 512a51.2 51.2 0 1 0-102.4 0 51.2 51.2 0 0 0 102.4 0z m68.266667 0a119.466667 119.466667 0 1 1-238.933334 0 119.466667 119.466667 0 0 1 238.933334 0z");
        gearIcon.setStyle("-fx-fill: -text-secondary;");
        gearIcon.setScaleX(0.018);
        gearIcon.setScaleY(0.018);
        StackPane gearBox = new StackPane(gearIcon);
        gearBox.setPrefSize(18, 18);
        gearBox.setMinSize(18, 18);
        gearBox.setMaxSize(18, 18);
        menuSettings.setGraphic(gearBox);

        SVGPath chartIcon = new SVGPath();
        chartIcon.setContent("M344.951467 677.888v187.733333H162.133333v-187.733333h182.818134zM603.477333 158.378667v291.157333l-0.068266 1.365333 0.068266 414.72h-190.2592V158.378667h190.2592z m258.389334 707.242666h-190.122667V450.901333h190.122667v414.72z m-516.9152-256H162.133333a68.266667 68.266667 0 0 0-68.266666 68.266667v187.733333a68.266667 68.266667 0 0 0 68.266666 68.266667h699.733334a68.266667 68.266667 0 0 0 68.266666-68.266667V450.901333a68.266667 68.266667 0 0 0-68.266666-68.266666h-190.122667V158.378667a68.266667 68.266667 0 0 0-68.266667-68.266667h-190.2592a68.266667 68.266667 0 0 0-68.266666 68.266667v451.242666z");
        chartIcon.setStyle("-fx-fill: -text-primary;");
        chartIcon.setScaleX(0.018);
        chartIcon.setScaleY(0.018);
        StackPane chartBox = new StackPane(chartIcon);
        chartBox.setPrefSize(18, 18);
        chartBox.setMinSize(18, 18);
        chartBox.setMaxSize(18, 18);
        btnHistory.setGraphic(chartBox);
        btnHistory.setText(" 数据分析");

        SVGPath bmiIcon = new SVGPath();
        bmiIcon.setContent("M634.299733 180.667733a85.333333 85.333333 0 0 1 120.661334-0.034133l88.337066 88.234667a85.333333 85.333333 0 0 1 0.034134 120.695466L426.359467 806.8096a85.367467 85.367467 0 0 1-43.52 23.313067l-186.299734 37.546666A34.133333 34.133333 0 0 1 156.330667 827.392l37.614933-186.0608a85.333333 85.333333 0 0 1 23.313067-43.383467L634.299733 180.6336zM265.557333 646.2464a17.066667 17.066667 0 0 0-4.642133 8.635733l-27.4432 135.68 135.918933-27.374933a17.066667 17.066667 0 0 0 8.704-4.642133L682.666667 453.7344l-112.4352-112.4352-304.708267 304.9472zM706.696533 228.932267a17.066667 17.066667 0 0 0-24.132266 0L618.496 293.034667l112.401067 112.401066 64.170666-64.136533a17.066667 17.066667 0 0 0 0-24.132267l-88.337066-88.234666z");
        bmiIcon.setStyle("-fx-fill: -text-primary;");
        bmiIcon.setScaleX(0.018);
        bmiIcon.setScaleY(0.018);
        StackPane bmiBox = new StackPane(bmiIcon);
        bmiBox.setPrefSize(18, 18);
        bmiBox.setMinSize(18, 18);
        bmiBox.setMaxSize(18, 18);
        btnBmi.setGraphic(bmiBox);
        btnBmi.setText(" 记录");

        SVGPath predictIcon = new SVGPath();
        predictIcon.setContent("M781.038933 102.673067c39.492267-1.979733 77.9264 6.9632 105.642667 34.645333 27.716267 27.716267 36.625067 66.1504 34.645333 105.642667-1.9456 39.492267-14.779733 83.899733-35.362133 130.013866-19.626667 44.032-47.104 91.067733-81.066667 139.025067 33.9968 47.957333 61.44 95.0272 81.066667 139.025067 20.5824 46.08 33.416533 90.5216 35.362133 130.013866 1.979733 39.492267-6.9632 77.9264-34.645333 105.642667-27.716267 27.716267-66.1504 36.625067-105.642667 34.645333-39.492267-1.9456-83.899733-14.779733-130.013866-35.362133-44.032-19.626667-91.067733-47.104-139.025067-81.066667-47.957333 33.9968-95.0272 61.44-139.025067 81.066667-46.08 20.5824-90.5216 33.416533-130.013866 35.362133-39.492267 1.979733-77.9264-6.929067-105.642667-34.645333-27.682133-27.716267-36.625067-66.1504-34.645333-105.642667 1.9456-39.492267 14.779733-83.899733 35.362133-130.013866 19.626667-44.032 47.035733-91.067733 81.032533-139.025067-33.9968-47.957333-61.405867-95.0272-81.032533-139.025067-20.5824-46.08-33.416533-90.5216-35.362133-130.013866-1.979733-39.492267 6.929067-77.9264 34.645333-105.642667 27.716267-27.716267 66.1504-36.625067 105.642667-34.645333 39.492267 1.9456 83.899733 14.779733 130.013866 35.362133 44.032 19.626667 91.067733 47.035733 139.025067 81.032533 47.957333-33.9968 95.0272-61.405867 139.025067-81.032533 46.08-20.5824 90.5216-33.416533 130.013866-35.362133zM262.4512 569.207467c-25.873067 38.331733-46.762667 75.3664-62.0544 109.636266-18.568533 41.642667-28.125867 77.482667-29.525333 105.5744-1.365333 28.125867 5.393067 44.612267 14.7456 53.9648 9.352533 9.386667 25.838933 16.1792 53.930666 14.779734 28.125867-1.365333 64-10.9568 105.608534-29.525334 34.269867-15.291733 71.2704-36.2496 109.602133-62.122666a1295.5648 1295.5648 0 0 1-101.1712-91.136 1294.6432 1294.6432 0 0 1-91.136-101.1712z m499.029333 0a1295.2576 1295.2576 0 0 1-192.273066 192.3072c38.331733 25.9072 75.3664 46.830933 109.636266 62.122666 41.642667 18.568533 77.482667 28.125867 105.5744 29.525334 28.125867 1.365333 44.612267-5.393067 53.9648-14.779734 9.386667-9.352533 16.145067-25.838933 14.7456-53.930666-1.365333-28.125867-10.922667-64-29.525333-105.608534-15.291733-34.269867-36.181333-71.304533-62.122667-109.636266zM512 304.128a1214.5664 1214.5664 0 0 0-110.114133 97.757867A1214.5664 1214.5664 0 0 0 304.128 512a1214.122667 1214.122667 0 0 0 97.757867 110.114133A1213.610667 1213.610667 0 0 0 512 719.837867a1212.689067 1212.689067 0 0 0 110.114133-97.723734A1213.1328 1213.1328 0 0 0 719.837867 512a1213.610667 1213.610667 0 0 0-97.723734-110.114133A1214.0544 1214.0544 0 0 0 512 304.128z m-34.133333 211.421867V512a34.133333 34.133333 0 1 1 68.266666 0v3.549867a34.133333 34.133333 0 0 1-68.266666 0z m-238.2848-344.746667c-28.125867-1.365333-44.612267 5.461333-53.9648 14.813867-9.386667 9.352533-16.1792 25.838933-14.779734 53.930666 1.365333 28.125867 10.9568 64 29.525334 105.608534 15.291733 34.269867 36.181333 71.2704 62.122666 109.602133a1294.813867 1294.813867 0 0 1 91.136-101.137067 1294.267733 1294.267733 0 0 1 101.137067-91.136c-38.331733-25.941333-75.332267-46.830933-109.568-62.122666-41.642667-18.568533-77.482667-28.125867-105.608533-29.525334z m544.8704 0c-28.125867 1.4336-64 10.990933-105.608534 29.559467-34.269867 15.291733-71.304533 36.181333-109.636266 62.122666a1294.677333 1294.677333 0 0 1 101.1712 91.136 1295.5648 1295.5648 0 0 1 91.136 101.137067c25.873067-38.331733 46.830933-75.332267 62.122666-109.568 18.568533-41.642667 28.125867-77.482667 29.525334-105.608533 1.365333-28.125867-5.393067-44.612267-14.779734-53.9648-9.352533-9.386667-25.838933-16.1792-53.930666-14.779734z");
        predictIcon.setStyle("-fx-fill: -text-primary;");
        predictIcon.setScaleX(0.018);
        predictIcon.setScaleY(0.018);
        StackPane predictBox = new StackPane(predictIcon);
        predictBox.setPrefSize(18, 18);
        predictBox.setMinSize(18, 18);
        predictBox.setMaxSize(18, 18);
        btnPredict.setGraphic(predictBox);
        btnPredict.setText(" 趋势预测");

        SVGPath dietIcon = new SVGPath();
        dietIcon.setContent("M539.584 191.936c152.768-141.888 399.104-32.128 399.104 177.92 0 64-25.088 125.312-69.76 170.624l-346.56 351.168a14.528 14.528 0 0 1-20.736 0L155.072 540.48a243.008 243.008 0 0 1-69.76-170.688c0-209.92 246.4-319.744 399.104-177.92L512 217.6l27.584-25.6z m58.112 62.464l-27.648 25.664L512 334.08l-58.048-53.952-27.648-25.6c-97.792-90.88-255.616-20.544-255.616 115.328 0 41.6 16.256 81.472 45.12 110.72L512 780.672l296.256-300.16a157.44 157.44 0 0 0 44.8-101.12l0.32-9.6c0-135.872-157.824-206.208-255.616-115.392z");
        dietIcon.setStyle("-fx-fill: -text-primary;");
        dietIcon.setScaleX(0.018);
        dietIcon.setScaleY(0.018);
        StackPane dietBox = new StackPane(dietIcon);
        dietBox.setPrefSize(18, 18);
        dietBox.setMinSize(18, 18);
        dietBox.setMaxSize(18, 18);
        btnDiet.setGraphic(dietBox);
        btnDiet.setText(" 膳食推荐");

        SVGPath compareIcon = new SVGPath();
        compareIcon.setContent("M426.666667 768h341.333333v-341.333333h-42.666667v298.666666h-298.666666v42.666667z m0 85.333333H341.333333v-128H213.333333V213.333333h512v128h128v512H426.666667z m213.333333-426.666666h-213.333333v213.333333h213.333333v-213.333333z m0-85.333334V298.666667H298.666667v341.333333h42.666666V341.333333h298.666667z");
        compareIcon.setStyle("-fx-fill: -text-primary;");
        compareIcon.setScaleX(0.018);
        compareIcon.setScaleY(0.018);
        StackPane compareBox = new StackPane(compareIcon);
        compareBox.setPrefSize(18, 18);
        compareBox.setMinSize(18, 18);
        compareBox.setMaxSize(18, 18);
        btnCompare.setGraphic(compareBox);
        btnCompare.setText(" 食物对比");

        SVGPath rankIcon = new SVGPath();
        rankIcon.setContent("M512 266.709333a160 160 0 0 1 160 160l-0.213333 8.277334a160.042667 160.042667 0 0 1-151.509334 151.552l-8.277333 0.213333a160 160 0 0 1 0-320z m0 64a96 96 0 0 0-96 96l0.512 9.813334A96 96 0 0 0 512 522.709333l9.813333-0.469333a96.042667 96.042667 0 0 0 0-191.018667L512 330.709333zM464.64 40.106667a74.709333 74.709333 0 0 1 94.72 0l58.538667 48.085333c2.090667 1.706667 4.736 2.56 7.424 2.432l75.562666-4.48a74.666667 74.666667 0 0 1 76.629334 55.722667l19.072 73.216a10.794667 10.794667 0 0 0 4.608 6.314666l63.786666 40.789334 5.418667 3.84c24.277333 18.773333 34.56 50.602667 25.941333 80.042666l-2.133333 6.229334-27.562667 70.485333a10.709333 10.709333 0 0 0 0 7.765333l27.562667 70.528a74.666667 74.666667 0 0 1-23.808 86.272l-5.418667 3.84-63.786666 40.789334a10.752 10.752 0 0 0-4.608 6.314666l-15.274667 58.538667 103.04 137.429333a53.333333 53.333333 0 0 1-20.608 80.554667l-187.690667 85.290667a53.333333 53.333333 0 0 1-73.514666-34.517334l-41.856-153.472-1.28 1.066667a74.666667 74.666667 0 0 1-94.762667 0l-2.645333-2.133333-41.514667 152.448a53.333333 53.333333 0 0 1-73.514667 34.474666l-187.733333-85.290666a53.333333 53.333333 0 0 1-20.565333-80.554667l103.338666-137.813333-14.592-56.021334a10.752 10.752 0 0 0-2.986666-5.12l-1.578667-1.194666-63.786667-40.789334a74.666667 74.666667 0 0 1-29.269333-90.112l27.605333-70.528 0.512-1.92a10.752 10.752 0 0 0 0-3.925333l-0.512-1.92-27.605333-70.485333a74.666667 74.666667 0 0 1 29.269333-90.112l63.786667-40.789334 1.536-1.194666a10.88 10.88 0 0 0 3.029333-5.12l19.114667-73.216a74.666667 74.666667 0 0 1 76.629333-55.722667l75.562667 4.48a10.752 10.752 0 0 0 7.424-2.432L464.64 40.106667z m280.746667 715.52a74.410667 74.410667 0 0 1-44.501334 11.605333l-75.605333-4.48a10.709333 10.709333 0 0 0-7.381333 2.389333l-3.029334 2.474667 46.08 169.045333 164.181334-74.666666-79.786667-106.410667zM197.845333 859.818667l164.181334 74.666666 45.781333-168.021333-1.706667-1.365333a10.752 10.752 0 0 0-7.424-2.389334l-75.52 4.48a74.538667 74.538667 0 0 1-46.293333-12.8l-79.018667 105.386667zM518.784 89.6a10.752 10.752 0 0 0-13.568 0l-58.453333 48.042667a74.666667 74.666667 0 0 1-51.797334 16.853333l-75.562666-4.48a10.666667 10.666667 0 0 0-10.965334 7.978667l-19.072 73.173333a74.794667 74.794667 0 0 1-26.282666 40.021333l-5.717334 4.096-63.829333 40.789334a10.666667 10.666667 0 0 0-4.138667 12.842666l27.562667 70.485334 2.261333 6.613333c3.84 13.482667 3.84 27.733333 0 41.216l-2.261333 6.613333-27.562667 70.485334a10.666667 10.666667 0 0 0 4.138667 12.885333l63.829333 40.789333 5.674667 4.053334c12.842667 10.026667 22.186667 24.064 26.325333 40.021333l19.072 73.216a10.666667 10.666667 0 0 0 10.922667 7.936l75.562667-4.48c18.773333-1.066667 37.290667 4.949333 51.797333 16.853333l58.496 48.042667a10.709333 10.709333 0 0 0 13.610667 0l58.453333-48.042667c14.506667-11.904 33.024-17.92 51.754667-16.853333l75.605333 4.48a10.666667 10.666667 0 0 0 10.965333-7.936l19.072-73.216c4.736-18.218667 16.213333-33.962667 32-44.074667l63.786667-40.789333 1.450667-1.109333a10.666667 10.666667 0 0 0 2.730666-11.776l-27.605333-70.485334a74.666667 74.666667 0 0 1 0-54.442666l27.605333-70.485334 0.512-1.792a10.709333 10.709333 0 0 0-3.242666-9.941333l-1.450667-1.109333-63.786667-40.789334a74.709333 74.709333 0 0 1-32-44.117333l-19.072-73.173333a10.666667 10.666667 0 0 0-10.965333-7.978667l-75.605333 4.48a74.666667 74.666667 0 0 1-51.712-16.853333l-58.538667-48.042667z");
        rankIcon.setStyle("-fx-fill: -text-primary;");
        rankIcon.setScaleX(0.018);
        rankIcon.setScaleY(0.018);
        StackPane rankBox = new StackPane(rankIcon);
        rankBox.setPrefSize(18, 18);
        rankBox.setMinSize(18, 18);
        rankBox.setMaxSize(18, 18);
        btnRank.setGraphic(rankBox);
        btnRank.setText(" 食物榜单");

        SVGPath mealIcon = new SVGPath();
        mealIcon.setContent("M778.24 61.44a122.88 122.88 0 0 1 122.88 122.88v655.36a122.88 122.88 0 0 1-122.88 122.88H245.76a122.88 122.88 0 0 1-122.88-122.88V184.32a122.88 122.88 0 0 1 122.88-122.88h532.48z m0 61.44H245.76a61.44 61.44 0 0 0-61.3376 57.83552L184.32 184.32v655.36a61.44 61.44 0 0 0 57.83552 61.3376L245.76 901.12h532.48a61.44 61.44 0 0 0 61.3376-57.83552L839.68 839.68V184.32a61.44 61.44 0 0 0-57.83552-61.3376L778.24 122.88zM563.2 532.48a30.72 30.72 0 0 1 0 61.44h-266.24a30.72 30.72 0 0 1 0-61.44h266.24z m163.84-225.28a30.72 30.72 0 0 1 0 61.44h-430.08a30.72 30.72 0 0 1 0-61.44h430.08z");
        mealIcon.setStyle("-fx-fill: -text-primary;");
        mealIcon.setScaleX(0.018);
        mealIcon.setScaleY(0.018);
        StackPane mealBox = new StackPane(mealIcon);
        mealBox.setPrefSize(18, 18);
        mealBox.setMinSize(18, 18);
        mealBox.setMaxSize(18, 18);
        btnMealRecord.setGraphic(mealBox);
        btnMealRecord.setText(" 膳食记录");

        SVGPath quizIcon = new SVGPath();
        quizIcon.setContent("M853.333333 768H651.946667l-109.504 117.482667a42.752 42.752 0 0 1-60.586667-0.042667L371.84 768H170.666667C135.36 768 106.666667 747.306667 106.666667 712.021333V170.581333A63.872 63.872 0 0 1 170.666667 106.666667h682.666666c35.328 0 64 28.629333 64 63.914666v541.44C917.333333 747.349333 888.746667 768 853.333333 768z m-225.28-28.501333l15.082667-14.165334H853.333333c11.861333 0 21.333333-1.557333 21.333334-13.312V170.581333A21.312 21.312 0 0 0 853.333333 149.333333H170.666667c-11.861333 0-21.333333 9.472-21.333334 21.248v541.44c0 11.690667 9.557333 13.312 21.333334 13.312h210.496l15.082666 14.165334s115.904 115.904 116.010667 115.797333l115.797333-115.797333zM490.666667 618.666667A21.269333 21.269333 0 0 1 512 597.333333c11.776 0 21.333333 9.450667 21.333333 21.312v21.376A21.269333 21.269333 0 0 1 512 661.333333c-11.776 0-21.333333-9.450667-21.333333-21.312v-21.376z m66.944-115.754667a145.92 145.92 0 0 0-12.501334 5.418667 20.693333 20.693333 0 0 0-12.16 19.029333c-0.106667 3.648 0.042667 7.424-0.768 10.944-2.325333 10.304-12.16 17.344-22.144 16.277333a21.248 21.248 0 0 1-19.562666-20.8c-1.152-30.037333 11.349333-51.925333 38.613333-65.002666a121.813333 121.813333 0 0 1 11.264-4.864c36.970667-13.333333 60.096-49.92 56-88.64-4.117333-38.912-34.773333-70.570667-73.216-75.605334a85.354667 85.354667 0 0 0-96.576 82.154667c-0.085333 2.773333-0.213333 5.653333-0.96 8.32a21.12 21.12 0 0 1-22.954667 15.168 21.248 21.248 0 0 1-18.645333-21.696c0.384-31.36 10.197333-59.541333 31.146667-82.88 34.410667-38.357333 77.653333-52.906667 127.530666-40.277333 50.346667 12.757333 81.557333 46.4 93.717334 97.109333 2.069333 8.661333 2.453333 17.728 3.605333 26.624-1.130667 53.248-32.981333 99.477333-82.389333 118.72z");
        quizIcon.setStyle("-fx-fill: -text-primary;");
        quizIcon.setScaleX(0.018);
        quizIcon.setScaleY(0.018);
        StackPane quizBox = new StackPane(quizIcon);
        quizBox.setPrefSize(18, 18);
        quizBox.setMinSize(18, 18);
        quizBox.setMaxSize(18, 18);
        btnQuiz.setGraphic(quizBox);
        btnQuiz.setText(" 健康问答");

        SVGPath toggleIcon = new SVGPath();
        toggleIcon.setContent("M128 757.312v85.376h768v-85.376H128z m0-192v85.376h469.312V565.312H128zM896 384l-213.312 128L896 640V384zM128 373.312v85.376h469.312V373.312H128z m0-192v85.376h768V181.312H128z");
        toggleIcon.setStyle("-fx-fill: -text-primary;");
        toggleIcon.setScaleX(0.018);
        toggleIcon.setScaleY(0.018);
        StackPane toggleBox = new StackPane(toggleIcon);
        toggleBox.setPrefSize(18, 18);
        toggleBox.setMinSize(18, 18);
        toggleBox.setMaxSize(18, 18);
        toggleNavBtn.setGraphic(toggleBox);
        toggleNavBtn.setText("");

        Platform.runLater(() -> {
            AIChatController ai = AIChatController.getInstance();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            ai.setMainStage(stage);
            ai.show();
            stage.focusedProperty().addListener((o, ov, focused) -> {
                if (focused) return;
                if (homeHeightPopup != null) homeHeightPopup.hide();
                if (homeWeightPopup != null) homeWeightPopup.hide();
            });
        });

        DropShadow wideShadow = new DropShadow(35, 0, 12, Color.rgb(0,0,0,0.10));
        DropShadow tightShadow = new DropShadow(14, 0, 4, Color.rgb(0,0,0,0.12));
        tightShadow.setInput(wideShadow);
        glassPanel.setEffect(tightShadow);

        applyNavState(true);
        loadBackdropPref();
    }

    private void setupHomePickers() {
        homeHeightField.setEditable(false);
        homeHeightField.setFocusTraversable(false);
        homeWeightField.setEditable(false);
        homeWeightField.setFocusTraversable(false);

        homeHeightField.sceneProperty().addListener((obs, old, sc) -> {
            if (sc == null) return;
            sc.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> dismissHomePopup(e, homeHeightPopup, homeHeightField));
            sc.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> dismissHomePopup(e, homeWeightPopup, homeWeightField));
        });

        homeHeightField.setOnMouseClicked(e -> {
            if (homeHeightPopup != null && homeHeightPopup.isShowing()) { homeHeightPopup.hide(); return; }
            if (homeHeightField.getText() == null || homeHeightField.getText().trim().isEmpty())
                homeHeightField.setText("165");
            showHomePicker(homeHeightField, homeHeightPopup, 100, 220, 165, p -> homeHeightPopup = p);
        });

        homeWeightField.setOnMouseClicked(e -> {
            if (homeWeightPopup != null && homeWeightPopup.isShowing()) { homeWeightPopup.hide(); return; }
            if (homeWeightField.getText() == null || homeWeightField.getText().trim().isEmpty())
                homeWeightField.setText("60");
            showHomePicker(homeWeightField, homeWeightPopup, 25, 250, 60, p -> homeWeightPopup = p);
        });
    }

    private void dismissHomePopup(MouseEvent event, Popup popup, TextField field) {
        if (popup == null || !popup.isShowing()) return;
        EventTarget target = event.getTarget();
        if (target instanceof Node) {
            Node node = (Node) target;
            if (node == field) return;
            for (Node n : popup.getContent()) {
                if (isNodeChild(node, n)) return;
            }
        }
        popup.hide();
    }

    private void showHomePicker(TextField field, Popup existingPopup, int min, int max, int defaultVal,
                                Consumer<Popup> setter) {
        if (existingPopup != null) existingPopup.hide();
        double pw = Math.max(field.getWidth(), 100);
        WheelPicker<Integer> wheel = new WheelPicker<>();
        wheel.setVisibleItems(3);
        wheel.setItemHeight(32);
        wheel.setPrefWidth(pw);
        wheel.setPrefHeight(110);
        List<Integer> items = new ArrayList<>();
        for (int i = min; i <= max; i++) items.add(i);
        wheel.setItems(items);
        int current = defaultVal;
        try {
            current = Integer.parseInt(field.getText().trim());
            if (current < min || current > max) current = defaultVal;
        } catch (NumberFormatException ignored) {}
        final int def = current;
        wheel.valueProperty().addListener((obs, old, val) -> {
            if (val != null) field.setText(String.valueOf(val));
        });
        wheel.setLightTheme(isLightTheme());
        StackPane container = new StackPane(wheel);
        container.setPadding(new Insets(12, 0, 12, 0));
        container.setMinWidth(pw);
        container.getStyleClass().add("wheel-popup");
        if (isLightTheme()) container.getStyleClass().add("light-theme");
        container.setVisible(false);
        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.setHideOnEscape(true);
        popup.getContent().add(container);
        setter.accept(popup);
        Bounds bounds = field.localToScreen(field.getBoundsInLocal());
        popup.show(field, bounds.getMinX(), bounds.getMaxY());
        Platform.runLater(() -> {
            wheel.resize(pw, 180);
            wheel.setSelectedIndex(def - min);
            container.setVisible(true);
        });
    }

    private boolean isLightTheme() {
        if (rootPane != null) return rootPane.getStyleClass().contains("light-theme");
        return false;
    }

    private static boolean isNodeChild(Node target, Node root) {
        Node n = target;
        while (n != null) {
            if (n == root) return true;
            n = n.getParent();
        }
        return false;
    }

    @FXML
    void handleHomeRecord(ActionEvent event) {
        String heightText = homeHeightField.getText();
        String weightText = homeWeightField.getText();
        if (heightText == null || heightText.trim().isEmpty()) { homeHeightField.setText("165"); heightText = "165"; }
        if (weightText == null || weightText.trim().isEmpty()) { homeWeightField.setText("60"); weightText = "60"; }
        try {
            double height = Double.parseDouble(heightText);
            double weight = Double.parseDouble(weightText);
            String error = bmiService.saveRecord(BMIApplication.currentUserId, height, weight);
            if (error == null) {
                double bmi = bmiService.calculateBMI(height, weight);
                String status = bmiService.getHealthStatus(bmi);
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("BMI结果"); a.setHeaderText(null);
                a.setContentText(String.format("您的 BMI 为 %.1f，状态：%s", bmi, status));
                a.showAndWait();
                loadDashboardData();
            }
        } catch (NumberFormatException ignored) {}
    }

    private void loadSidebarUser() {
        if (sidebarUserName != null) sidebarUserName.setText("用户 " + BMIApplication.currentUserId);
        if (avatarLabel != null) avatarLabel.setText(String.valueOf(BMIApplication.currentUserId).charAt(0) + "");
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                List<BmiRecord> records = bmiService.getRecordsDesc(BMIApplication.currentUserId);
                Platform.runLater(() -> {
                    if (records == null || records.isEmpty()) {
                        if (dashRecords != null) dashRecords.setText("0");
                        if (heroBmiValue != null) heroBmiValue.setText("--");
                        if (heroBmiStatus != null) heroBmiStatus.setText("暂无数据");
                        if (lastRecordLabel != null) lastRecordLabel.setText("");
                        if (weightChangeLabel != null) weightChangeLabel.setText("");
                        if (zoneDescription != null) zoneDescription.setText("记录体重数据以查看区间");
                        if (miniChartEmpty != null) miniChartEmpty.setVisible(true);
                        if (miniChart != null) miniChart.setVisible(false);
                        resetStats();
                        loadRandomSlogan();
                        return;
                    }
                    BmiRecord latest = records.get(0);
                    double bmi = latest.getBmi();
                    String status = latest.getStatus();

                    if (dashBmi != null) dashBmi.setText(String.format("%.1f", bmi));
                    if (dashStatus != null) {
                        dashStatus.setText(status);
                        dashStatus.getStyleClass().clear();
                        if ("正常".equals(status)) dashStatus.getStyleClass().add("data-value-green");
                        else if ("偏瘦".equals(status) || "超重".equals(status)) dashStatus.getStyleClass().add("data-value-yellow");
                        else dashStatus.getStyleClass().add("data-value-red");
                    }
                    if (dashRecords != null) dashRecords.setText(String.valueOf(records.size()));
                    if (bmiStatusLabel != null) bmiStatusLabel.setText(String.format("BMI %.1f · %s", bmi, status));
                    if (dashIdealWeight != null) dashIdealWeight.setText(String.format("%.0f–%.0f kg", latest.getHeight() * 0.185 * latest.getHeight() / 100, latest.getHeight() * 0.24 * latest.getHeight() / 100));

                    // Hero card
                    if (heroBmiValue != null) heroBmiValue.setText(String.format("%.1f", bmi));
                    if (heroBmiStatus != null) {
                        heroBmiStatus.setText(status);
                        heroBmiStatus.getStyleClass().removeAll("hero-bmi-status-green", "hero-bmi-status-yellow", "hero-bmi-status-red");
                        if ("正常".equals(status)) heroBmiStatus.getStyleClass().add("hero-bmi-status-green");
                        else if ("偏瘦".equals(status) || "超重".equals(status)) heroBmiStatus.getStyleClass().add("hero-bmi-status-yellow");
                        else heroBmiStatus.getStyleClass().add("hero-bmi-status-red");
                    }
                    if (lastRecordLabel != null && latest.getCreateTime() != null) {
                        lastRecordLabel.setText("上次记录：" + latest.getCreateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
                    }
                    if (weightChangeLabel != null && records.size() >= 2) {
                        double prevWeight = records.get(1).getWeight();
                        double diff = latest.getWeight() - prevWeight;
                        if (Math.abs(diff) < 0.01) weightChangeLabel.setText("体重持平");
                        else if (diff > 0) weightChangeLabel.setText(String.format("↑ +%.1f kg", diff));
                        else weightChangeLabel.setText(String.format("↓ %.1f kg", diff));
                    } else if (weightChangeLabel != null) {
                        weightChangeLabel.setText("");
                    }

                    // Trend comparison
                    if (records.size() >= 2) {
                        double prev = records.get(1).getBmi();
                        if (dashTrend != null) {
                            if (bmi > prev) { dashTrend.setText("↑ 上升"); dashTrend.getStyleClass().setAll("data-value-red"); }
                            else if (bmi < prev) { dashTrend.setText("↓ 下降"); dashTrend.getStyleClass().setAll("data-value-green"); }
                            else { dashTrend.setText("→ 持平"); dashTrend.getStyleClass().setAll("data-value"); }
                        }
                        if (trendLabel != null) trendLabel.setText(bmi > prev ? "📈 趋势上升" : bmi < prev ? "📉 趋势下降" : "➡ 趋势平稳");
                    }

                    // Zone visual
                    updateZoneIndicator(bmi);

                    // Mini chart
                    updateMiniChart(records);

                    // Slogan
                    loadRandomSlogan();

                    // Parameter statistics
                    double sum = 0, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
                    int healthyCount = 0;
                    for (BmiRecord r : records) {
                        double b = r.getBmi();
                        sum += b;
                        if (b > max) max = b;
                        if (b < min) min = b;
                        if (b >= 18.5 && b < 24) healthyCount++;
                    }
                    if (statAvgBmi != null) statAvgBmi.setText(String.format("%.1f", sum / records.size()));
                    if (statMaxBmi != null) statMaxBmi.setText(String.format("%.1f", max));
                    if (statMinBmi != null) statMinBmi.setText(String.format("%.1f", min));
                    if (statHealthyDays != null) statHealthyDays.setText(String.valueOf(healthyCount));

                    if (historySummary != null) historySummary.setText(String.format("共%d条记录，最近BMI %.1f", records.size(), bmi));
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void updateZoneIndicator(double bmi) {
        double minBmi = 14.0, maxBmi = 36.0;
        double pct = Math.max(0, Math.min(1, (bmi - minBmi) / (maxBmi - minBmi)));
        if (zoneTrack != null) {
            double trackWidth = zoneTrack.getWidth();
            if (trackWidth > 0) {
                double indicatorW = zoneIndicator != null ? zoneIndicator.prefWidth(-1) : 12;
                zoneIndicator.setTranslateX((pct - 0.5) * (trackWidth - indicatorW));
            }
        }
        if (zoneDescription != null) {
            String desc;
            if (bmi < 18.5) desc = "偏瘦 · 建议增加营养摄入";
            else if (bmi < 24) desc = "正常 · 继续保持良好习惯";
            else if (bmi < 28) desc = "超重 · 建议控制饮食增加运动";
            else desc = "肥胖 · 建议咨询专业医生";
            zoneDescription.setText(desc);
        }
    }

    private void updateMiniChart(List<BmiRecord> records) {
        if (miniChart == null || miniChartEmpty == null) return;
        if (records.size() < 2) {
            miniChartEmpty.setVisible(true);
            miniChart.setVisible(false);
            return;
        }
        miniChartEmpty.setVisible(false);
        miniChart.setVisible(true);

        List<BmiRecord> last10 = records.size() > 10 ? records.subList(0, 10) : records;
        java.util.Collections.reverse(last10);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < last10.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, last10.get(i).getBmi()));
        }
        miniChart.getData().clear();
        miniChart.getData().add(series);
    }

    private void resetStats() {
        if (statAvgBmi != null) statAvgBmi.setText("--");
        if (statMaxBmi != null) statMaxBmi.setText("--");
        if (statMinBmi != null) statMinBmi.setText("--");
        if (statHealthyDays != null) statHealthyDays.setText("--");
    }

    private void loadRandomSlogan() {
        if (sloganLabel == null) return;
        if (slogans == null) {
            try (java.io.InputStream is = getClass().getResourceAsStream("/data/slogans.txt");
                 java.util.Scanner sc = new java.util.Scanner(is)) {
                slogans = new ArrayList<>();
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (!line.isEmpty()) slogans.add(line);
                }
            } catch (Exception e) {
                slogans = List.of("记录每一次体重数据，管理好健康人生");
            }
        }
        if (!slogans.isEmpty()) {
            sloganLabel.setText(slogans.get(RANDOM.nextInt(slogans.size())));
        }
    }

    @FXML
    void onSloganClick() {
        loadRandomSlogan();
    }

    @FXML
    void showHome(ActionEvent event) {
        if (homeContent != null && glassContent != null) {
            glassContent.getChildren().setAll(homeContent);
            loadDashboardData();
            navHistory.clear();
            navIndex = -1;
        }
    }

    @FXML
    void showBmiRecord(ActionEvent event) { loadView("bmi_record.fxml"); }
    @FXML
    void showHistory(ActionEvent event) { loadView("history.fxml"); }
    @FXML
    void showChart(ActionEvent event) { loadView("chart.fxml"); }
    @FXML
    void showPrediction(ActionEvent event) { loadView("prediction.fxml"); }
    @FXML
    void showPersonalize(ActionEvent event) { loadView("personalize.fxml"); }
    @FXML
    void showDiet(ActionEvent event) { loadView("diet.fxml"); }

    public static MainController getInstance() { return instance; }
    public Region getBackdrop() { return backdrop; }
    @FXML
    void showFood(ActionEvent event) { loadView("food_compare.fxml"); }
    @FXML
    void showFoodRank(ActionEvent event) { loadView("food_rank.fxml"); }
    @FXML
    void showMealRecord(ActionEvent event) { loadView("meal_record.fxml"); }
    @FXML
    void showQuiz(ActionEvent event) {
        glassContent.getChildren().setAll(loadingLabel());
        Platform.runLater(() -> {
            try {
                if (quizView == null) {
                    quizView = FXMLLoader.load(getClass().getResource("/fxml/quiz.fxml"));
                }
                glassContent.getChildren().setAll(quizView);
                navHistory.clear();
                navIndex = -1;
            } catch (Exception e) {
                e.printStackTrace();
                Label err = new Label("加载失败: " + e.getMessage());
                err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
                StackPane.setAlignment(err, javafx.geometry.Pos.CENTER);
                glassContent.getChildren().setAll(err);
            }
        });
    }
    @FXML
    void showProfile(ActionEvent event) { loadView("profile.fxml"); }

    @FXML
    void showLogout(ActionEvent event) {
        BMIApplication.currentUserId = -1;
        AIChatController.getInstance().hide();
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            scene.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#050f0a")),
                    new Stop(1, Color.web("#000000"))));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadView(String fxml) {
        if (!fxml.equals(navHistory.isEmpty() ? "" : navHistory.get(navIndex))) {
            while (navIndex < navHistory.size() - 1) {
                navHistory.remove(navHistory.size() - 1);
            }
            navHistory.add(fxml);
            navIndex = navHistory.size() - 1;
        }
        glassContent.getChildren().setAll(loadingLabel());
        Platform.runLater(() -> {
            try {
                Node view = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
                glassContent.getChildren().setAll(view);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                if (msg != null && msg.length() > 120) msg = msg.substring(0, 120) + "…";
                Label err = new Label("加载失败: " + (msg != null ? msg : e.getClass().getSimpleName()));
                err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
                err.setWrapText(true);
                err.setMaxWidth(500);
                StackPane.setAlignment(err, javafx.geometry.Pos.CENTER);
                glassContent.getChildren().setAll(err);
            }
        });
    }

    @FXML
    void handleNavBack() {
        if (navIndex > 0) {
            navIndex--;
            loadViewAt(navHistory.get(navIndex));
        }
    }

    @FXML
    void handleNavForward() {
        if (navIndex < navHistory.size() - 1) {
            navIndex++;
            loadViewAt(navHistory.get(navIndex));
        }
    }

    private void loadViewAt(String fxml) {
        glassContent.getChildren().setAll(loadingLabel());
        Platform.runLater(() -> {
            try {
                Node view = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
                glassContent.getChildren().setAll(view);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                if (msg != null && msg.length() > 120) msg = msg.substring(0, 120) + "…";
                Label err = new Label("加载失败: " + (msg != null ? msg : e.getClass().getSimpleName()));
                err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
                err.setWrapText(true);
                err.setMaxWidth(500);
                StackPane.setAlignment(err, javafx.geometry.Pos.CENTER);
                glassContent.getChildren().setAll(err);
            }
        });
    }

    private Label loadingLabel() {
        Label l = new Label("加载中…");
        l.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 14px;");
        StackPane.setAlignment(l, javafx.geometry.Pos.CENTER);
        return l;
    }

    private Label errorLabel(String msg) {
        Label l = new Label(msg);
        l.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
        StackPane.setAlignment(l, javafx.geometry.Pos.CENTER);
        return l;
    }
}
