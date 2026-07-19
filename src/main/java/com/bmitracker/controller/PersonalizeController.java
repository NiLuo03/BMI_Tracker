package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class PersonalizeController {

    @FXML private Label hintLabel;

    @FXML void selectBlack(MouseEvent e) { apply("#000000", "纯黑"); }
    @FXML void selectWhite(MouseEvent e) { apply("#ffffff", "纯白"); }

    private void apply(String color, String name) {
        MainController.getInstance().changeBackdrop(color);
        if (hintLabel != null) hintLabel.setText("已切换为 " + name);
    }
}
