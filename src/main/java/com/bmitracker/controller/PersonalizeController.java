package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class PersonalizeController {

    @FXML private Label hintLabel;

    private static final String[][] COLORS = {
        {"#050f0a", "墨绿"}, {"#0a0a1a", "深蓝"}, {"#100a1a", "暗紫"},
        {"#000000", "纯黑"}, {"#111111", "炭灰"}, {"#ffffff", "纯白"}
    };

    @FXML void selectColor1(MouseEvent e) { apply(0); }
    @FXML void selectColor2(MouseEvent e) { apply(1); }
    @FXML void selectColor3(MouseEvent e) { apply(2); }
    @FXML void selectColor4(MouseEvent e) { apply(3); }
    @FXML void selectColor5(MouseEvent e) { apply(4); }
    @FXML void selectColor6(MouseEvent e) { apply(5); }

    private void apply(int idx) {
        String color = COLORS[idx][0];
        String name = COLORS[idx][1];
        Region backdrop = MainController.getInstance().getBackdrop();
        if (backdrop != null) {
            backdrop.setStyle("-fx-background-color: " + color + ";");
        }
        if (hintLabel != null) hintLabel.setText("已切换为 " + name);
    }
}
