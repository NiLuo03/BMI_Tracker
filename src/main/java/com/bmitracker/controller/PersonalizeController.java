package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class PersonalizeController {

    @FXML private Label hintLabel;

    @FXML void selectColor1(MouseEvent e) { apply(0); }
    @FXML void selectColor2(MouseEvent e) { apply(1); }
    @FXML void selectColor3(MouseEvent e) { apply(2); }
    @FXML void selectColor4(MouseEvent e) { apply(3); }
    @FXML void selectColor5(MouseEvent e) { apply(4); }
    @FXML void selectColor6(MouseEvent e) { apply(5); }

    private void apply(int idx) {
        String[] names = {"墨绿", "深蓝", "暗紫", "纯黑", "炭灰", "纯白"};
        MainController.getInstance().changeBackdrop(idx);
        if (hintLabel != null) hintLabel.setText("已切换为 " + names[idx]);
    }
}
