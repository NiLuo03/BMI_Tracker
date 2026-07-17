package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PersonalizeController {

    @FXML private VBox previewBox, color1, color2, color3, color4, color5, color6;
    @FXML private Label previewLabel, previewColorName, hintLabel;

    private String selectedColor = "#050f0a";
    private String selectedName = "#050f0a 墨绿";

    private static final String[][] COLORS = {
        {"#050f0a", "墨绿"}, {"#0a0a1a", "深蓝"}, {"#100a1a", "暗紫"},
        {"#000000", "纯黑"}, {"#111111", "炭灰"}, {"#ffffff", "纯白"}
    };

    @FXML void selectColor1(MouseEvent e) { select(0); }
    @FXML void selectColor2(MouseEvent e) { select(1); }
    @FXML void selectColor3(MouseEvent e) { select(2); }
    @FXML void selectColor4(MouseEvent e) { select(3); }
    @FXML void selectColor5(MouseEvent e) { select(4); }
    @FXML void selectColor6(MouseEvent e) { select(5); }

    private void select(int idx) {
        selectedColor = COLORS[idx][0];
        selectedName = COLORS[idx][0] + " " + COLORS[idx][1];
        previewBox.setStyle("-fx-background-color: " + selectedColor + "; -fx-background-radius: 14px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-width: 1px; -fx-border-radius: 14px; -fx-min-height: 120;");
        previewColorName.setText(selectedName);
        hintLabel.setText("已预览 " + COLORS[idx][1] + "，点击「应用背景」生效");
    }

    @FXML
    void applyColor() {
        Region backdrop = MainController.getInstance().getBackdrop();
        if (backdrop != null) {
            backdrop.setStyle("-fx-background-color: " + selectedColor + ";");
        }
        hintLabel.setText("✓ 背景已应用为 " + selectedName);
    }
}
