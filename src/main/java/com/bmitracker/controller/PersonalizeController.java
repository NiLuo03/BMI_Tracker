package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.component.ImageCropper;
import com.bmitracker.util.BackgroundHistoryStore;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class PersonalizeController {

    @FXML private Label hintLabel;
    @FXML private FlowPane colorFlow;

    @FXML
    void initialize() {
        loadHistory();
    }

    @FXML void selectBlack(MouseEvent e) { apply("#000000", "纯黑"); }
    @FXML void selectWhite(MouseEvent e) { apply("#ffffff", "纯白"); }

    @FXML
    void selectCustom(MouseEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("选择背景图片");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        Stage stage = (Stage) hintLabel.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file == null) return;

        ImageCropper.show(file, stage).ifPresentOrElse(crop -> {
            MainController.getInstance().changeBackdropCropped(file, crop.x(), crop.y(), crop.width(), crop.height());
            BackgroundHistoryStore.add(BMIApplication.currentUserId,
                    new BackgroundHistoryStore.Entry(file.getAbsolutePath(), crop.x(), crop.y(), crop.width(), crop.height()));
            loadHistory();
            if (hintLabel != null) hintLabel.setText("已切换为自定义图片（已裁剪）");
        }, () -> {
            if (hintLabel != null) hintLabel.setText("已取消裁剪");
        });
    }

    private void loadHistory() {
        List<BackgroundHistoryStore.Entry> entries = BackgroundHistoryStore.load(BMIApplication.currentUserId);
        // remove previous history items (keep pure black, pure white, and custom button)
        colorFlow.getChildren().removeIf(node -> {
            if (node instanceof VBox) {
                String style = ((VBox) node).getStyle();
                return style != null && style.contains("history-item");
            }
            return false;
        });
        int insertIdx = colorFlow.getChildren().size() - 1; // before custom button
        for (BackgroundHistoryStore.Entry entry : entries) {
            File f = new File(entry.path);
            if (!f.exists()) continue;
            VBox item = new VBox(4);
            item.setAlignment(javafx.geometry.Pos.CENTER);
            item.setStyle("-fx-cursor: hand; -fx-history-item: true;");
            Image img = new Image(f.toURI().toString());
            ImageView iv = new ImageView(img);
            iv.setFitWidth(80); iv.setFitHeight(80); iv.setPreserveRatio(true);
            iv.setSmooth(true);
            StackPane sp = new StackPane(iv);
            sp.setPrefSize(80, 80);
            sp.setStyle("-fx-background-radius: 14; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1.5px; -fx-border-radius: 14;");
            sp.setClip(new javafx.scene.shape.Rectangle(80, 80));
            ((javafx.scene.shape.Rectangle) sp.getClip()).setArcWidth(14);
            ((javafx.scene.shape.Rectangle) sp.getClip()).setArcHeight(14);
            Label lb = new Label(f.getName().length() > 6 ? f.getName().substring(0, 6) + ".." : f.getName());
            lb.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-secondary;");
            final BackgroundHistoryStore.Entry e = entry;
            item.getChildren().addAll(sp, lb);
            item.setOnMouseClicked(ev -> {
                MainController.getInstance().changeBackdropCropped(
                        new File(e.path), e.cropX, e.cropY, e.cropW, e.cropH);
                if (hintLabel != null) hintLabel.setText("已切换为历史背景图片");
            });
            colorFlow.getChildren().add(insertIdx, item);
            insertIdx++;
        }
    }

    private void apply(String color, String name) {
        MainController.getInstance().changeBackdrop(color);
        if (hintLabel != null) hintLabel.setText("已切换为 " + name);
    }
}
