package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PersonalizeController {

    @FXML private VBox colorGrid;
    @FXML private Label hintLabel;

    private static final String[] ROW_NAMES = {
        "紫调→深灰黑", "青蓝渐变→藏青黑", "橄榄绿", "豆沙砖红玫红", "土黄焦糖棕", "浅湖绿→深墨绿", "浅灰→深棕灰"
    };

    private static final String[][] ROWS = {
        {"#B478A2","#AF709E","#A86696","#703868","#62325E","#582E56","#726E74","#3C3C40","#302C32","#34303E"},
        {"#488890","#3094B8","#2888A8","#486890","#406078","#206070","#0068A0","#405080","#203860","#102038"},
        {"#88A058","#90A848","#788840","#808060","#606040","#807838","#486010","#484828","#505030","#403818"},
        {"#C89888","#C07870","#B85858","#A04848","#B06058","#B86090","#A85080","#B05050","#884860","#984840"},
        {"#C8A068","#D0A838","#C8A840","#B09038","#987850","#C07838","#986038","#906848","#787040","#505040"},
        {"#90C0C8","#7898A8","#709088","#00A098","#40B090","#009880","#587088","#608080","#007888","#005848"},
        {"#807C78","#686868","#907868","#A08878","#987058","#906850","#806050","#685048","#484038","#403830"},
    };

    @FXML
    void initialize() {
        colorGrid.getChildren().clear();
        for (int row = 0; row < ROWS.length; row++) {
            colorGrid.getChildren().add(buildRow(row));
        }
    }

    private HBox buildRow(int rowIdx) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 0, 0, 0));

        Label nameLabel = new Label(ROW_NAMES[rowIdx]);
        nameLabel.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 11px; -fx-min-width: 100px; -fx-max-width: 100px;");
        nameLabel.setWrapText(true);
        row.getChildren().add(nameLabel);

        for (int col = 0; col < ROWS[rowIdx].length; col++) {
            String hex = ROWS[rowIdx][col];
            Rectangle swatch = new Rectangle(40, 28);
            swatch.setArcWidth(6);
            swatch.setArcHeight(6);
            swatch.setFill(Color.web(hex));
            String border = isLight(hex) ? "rgba(0,0,0,0.12)" : "rgba(255,255,255,0.10)";
            swatch.setStyle("-fx-stroke: " + border + "; -fx-stroke-width: 1; -fx-cursor: hand;");

            int globalIdx = rowIdx * 10 + col;
            swatch.setOnMouseClicked(e -> apply(globalIdx));

            VBox cell = new VBox(2, swatch);
            cell.setAlignment(Pos.CENTER);
            row.getChildren().add(cell);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().add(spacer);

        return row;
    }

    private boolean isLight(String hex) {
        Color c = Color.web(hex);
        return (c.getRed() * 255 * 0.299 + c.getGreen() * 255 * 0.587 + c.getBlue() * 255 * 0.114) > 180;
    }

    private void apply(int idx) {
        MainController.getInstance().changeBackdrop(idx);
        if (hintLabel != null) {
            int r = idx / 10, c = idx % 10;
            hintLabel.setText("已切换 — " + ROW_NAMES[r] + " / 第" + (c + 1) + "列");
        }
    }
}
