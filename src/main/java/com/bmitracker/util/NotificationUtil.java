package com.bmitracker.util;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.Window;

import java.util.Optional;

public class NotificationUtil {

    public enum Type { SUCCESS, INFO, WARNING, ERROR }

    public static void show(Window owner, Type type, String title, String subtitle) {
        Alert alert = build(owner, type, title, subtitle);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }

    public static boolean showConfirm(Window owner, String title, String subtitle) {
        Alert alert = build(owner, Type.INFO, title, subtitle);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((Button) alert.getDialogPane().lookupButton(ButtonType.YES)).setText("确定");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.NO)).setText("取消");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private static Alert build(Window owner, Type type, String title, String subtitle) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle("");
        alert.setHeaderText(null);

        alert.getDialogPane().getStyleClass().add("ntf-card");

        alert.setGraphic(createIcon(type));

        VBox content = new VBox(4);
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("ntf-title");
        Label subLbl = new Label(subtitle);
        subLbl.getStyleClass().add("ntf-subtitle");
        content.getChildren().addAll(titleLbl, subLbl);
        alert.getDialogPane().setContent(content);

        alert.getDialogPane().getStylesheets().add(
            alert.getClass().getResource("/css/dashboard.css").toExternalForm()
        );

        return alert;
    }

    private static StackPane createIcon(Type type) {
        StackPane circle = new StackPane();
        circle.getStyleClass().addAll("ntf-icon-circle", "ntf-" + type.name().toLowerCase());

        SVGPath path = new SVGPath();
        path.setContent(iconPath(type));
        path.getStyleClass().add("ntf-icon");
        circle.getChildren().add(path);
        return circle;
    }

    private static String iconPath(Type type) {
        return switch (type) {
            case SUCCESS -> "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z";
            case INFO -> "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";
            case WARNING -> "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z";
            case ERROR -> "M13 2L2 13l11 11 11-11L13 2zm-1 14h-2v-2h2v2zm0-4h-2V7h2v5z";
        };
    }
}
