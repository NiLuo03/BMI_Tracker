package com.bmitracker.component;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class ImageCropper {

    private enum Mode { NONE, MOVE, NEW, RESIZE_TL, RESIZE_TR, RESIZE_BL, RESIZE_BR, RESIZE_T, RESIZE_B, RESIZE_L, RESIZE_R }

    private static final double MIN_SIZE = 50;
    private static final double HANDLE_RADIUS = 6;

    private static final Cursor[] RESIZE_CURSORS = {
        Cursor.NW_RESIZE, Cursor.NE_RESIZE, Cursor.SW_RESIZE, Cursor.SE_RESIZE,
        Cursor.N_RESIZE, Cursor.S_RESIZE, Cursor.W_RESIZE, Cursor.E_RESIZE
    };

    private final Image originalImage;
    private final ImageView imageView;
    private final Rectangle selection;
    private final Rectangle topOverlay, bottomOverlay, leftOverlay, rightOverlay;
    private final Circle[] handles = new Circle[8];
    private final Pane overlayPane;
    private final double displayW, displayH, scale;

    private Mode mode = Mode.NONE;
    private double startX, startY;
    private double origX, origY, origW, origH;
    private double sx, sy, sw, sh; // crop coords in original image space

    private ImageCropper(File imageFile) {
        originalImage = new Image(imageFile.toURI().toString());

        double iw = originalImage.getWidth();
        double ih = originalImage.getHeight();
        double maxW = 800, maxH = 600;
        scale = Math.min(1.0, Math.min(maxW / iw, maxH / ih));
        displayW = iw * scale;
        displayH = ih * scale;

        sx = 0; sy = 0; sw = iw; sh = ih;

        imageView = new ImageView(originalImage);
        imageView.setFitWidth(displayW);
        imageView.setFitHeight(displayH);

        selection = new Rectangle(displayW * 0.05, displayH * 0.05, displayW * 0.9, displayH * 0.9);
        selection.setFill(new Color(1, 1, 1, 0.15));
        selection.setStroke(Color.WHITE);
        selection.setStrokeWidth(2);

        topOverlay    = new Rectangle(); topOverlay.setFill(Color.rgb(0, 0, 0, 0.55));
        bottomOverlay = new Rectangle(); bottomOverlay.setFill(Color.rgb(0, 0, 0, 0.55));
        leftOverlay   = new Rectangle(); leftOverlay.setFill(Color.rgb(0, 0, 0, 0.55));
        rightOverlay  = new Rectangle(); rightOverlay.setFill(Color.rgb(0, 0, 0, 0.55));

        overlayPane = new Pane(topOverlay, bottomOverlay, leftOverlay, rightOverlay, selection);
        overlayPane.setPickOnBounds(true);

        for (int i = 0; i < 8; i++) {
            handles[i] = new Circle(HANDLE_RADIUS, Color.WHITE);
            handles[i].setStroke(Color.rgb(0, 0, 0, 0.4));
            handles[i].setStrokeWidth(1);
            handles[i].setCursor(RESIZE_CURSORS[i]);
        }
        overlayPane.getChildren().addAll(handles);

        selection.xProperty().addListener(o -> { updateOverlay(); updateHandles(); });
        selection.yProperty().addListener(o -> { updateOverlay(); updateHandles(); });
        selection.widthProperty().addListener(o -> { updateOverlay(); updateHandles(); });
        selection.heightProperty().addListener(o -> { updateOverlay(); updateHandles(); });

        updateOverlay();
        updateHandles();
        bindMouse();
    }

    private void updateOverlay() {
        double x = selection.getX(), y = selection.getY();
        double w = selection.getWidth(), h = selection.getHeight();

        topOverlay.setRect(0, 0, displayW, Math.max(0, y));
        bottomOverlay.setRect(0, y + h, displayW, Math.max(0, displayH - y - h));
        leftOverlay.setRect(0, y, Math.max(0, x), h);
        rightOverlay.setRect(x + w, y, Math.max(0, displayW - x - w), h);
    }

    private void updateHandles() {
        double x = selection.getX(), y = selection.getY();
        double w = selection.getWidth(), h = selection.getHeight();
        handles[0].setCenterX(x);          handles[0].setCenterY(y);
        handles[1].setCenterX(x + w);      handles[1].setCenterY(y);
        handles[2].setCenterX(x);          handles[2].setCenterY(y + h);
        handles[3].setCenterX(x + w);      handles[3].setCenterY(y + h);
        handles[4].setCenterX(x + w / 2);  handles[4].setCenterY(y);
        handles[5].setCenterX(x + w / 2);  handles[5].setCenterY(y + h);
        handles[6].setCenterX(x);          handles[6].setCenterY(y + h / 2);
        handles[7].setCenterX(x + w);      handles[7].setCenterY(y + h / 2);
    }

    private void bindMouse() {
        overlayPane.setOnMousePressed(this::onPressed);
        overlayPane.setOnMouseDragged(this::onDragged);
        overlayPane.setOnMouseReleased(e -> mode = Mode.NONE);
    }

    private void onPressed(MouseEvent e) {
        Object target = e.getTarget();
        for (int i = 0; i < handles.length; i++) {
            if (target == handles[i]) {
                mode = getModeForHandle(i);
                startX = e.getSceneX(); startY = e.getSceneY();
                origX = selection.getX(); origY = selection.getY();
                origW = selection.getWidth(); origH = selection.getHeight();
                return;
            }
        }
        if (target == selection) {
            mode = Mode.MOVE;
            startX = e.getSceneX(); startY = e.getSceneY();
            origX = selection.getX(); origY = selection.getY();
            return;
        }
        mode = Mode.NEW;
        startX = e.getSceneX(); startY = e.getSceneY();
        origX = e.getX(); origY = e.getY();
        selection.setX(origX); selection.setY(origY);
        selection.setWidth(0); selection.setHeight(0);
    }

    private void onDragged(MouseEvent e) {
        double dx = e.getSceneX() - startX;
        double dy = e.getSceneY() - startY;

        switch (mode) {
            case MOVE -> {
                double nx = clamp(origX + dx, 0, displayW - selection.getWidth());
                double ny = clamp(origY + dy, 0, displayH - selection.getHeight());
                selection.setX(nx); selection.setY(ny);
            }
            case NEW -> {
                double cx = e.getX(), cy = e.getY();
                double nx = Math.min(origX, Math.max(0, cx));
                double ny = Math.min(origY, Math.max(0, cy));
                double nw = Math.min(Math.abs(cx - origX), displayW - nx);
                double nh = Math.min(Math.abs(cy - origY), displayH - ny);
                selection.setX(nx); selection.setY(ny);
                selection.setWidth(Math.max(1, nw)); selection.setHeight(Math.max(1, nh));
            }
            default -> resize(dx, dy);
        }
    }

    private void resize(double dx, double dy) {
        double nx = origX, ny = origY, nw = origW, nh = origH;
        switch (mode) {
            case RESIZE_TL -> { nx = origX + dx; ny = origY + dy; nw = origW - dx; nh = origH - dy; }
            case RESIZE_TR -> { ny = origY + dy; nw = origW + dx; nh = origH - dy; }
            case RESIZE_BL -> { nx = origX + dx; nw = origW - dx; nh = origH + dy; }
            case RESIZE_BR -> { nw = origW + dx; nh = origH + dy; }
            case RESIZE_T  -> { ny = origY + dy; nh = origH - dy; }
            case RESIZE_B  -> { nh = origH + dy; }
            case RESIZE_L  -> { nx = origX + dx; nw = origW - dx; }
            case RESIZE_R  -> { nw = origW + dx; }
        }
        if (nw < MIN_SIZE) { if (nx != origX) nx = origX + origW - MIN_SIZE; nw = MIN_SIZE; }
        if (nh < MIN_SIZE) { if (ny != origY) ny = origY + origH - MIN_SIZE; nh = MIN_SIZE; }
        if (nx < 0) { nw += nx; nx = 0; }
        if (ny < 0) { nh += ny; ny = 0; }
        nw = clamp(nw, MIN_SIZE, displayW - nx);
        nh = clamp(nh, MIN_SIZE, displayH - ny);
        selection.setX(nx); selection.setY(ny);
        selection.setWidth(nw); selection.setHeight(nh);
    }

    private static Mode getModeForHandle(int idx) {
        return switch (idx) {
            case 0 -> Mode.RESIZE_TL;  case 1 -> Mode.RESIZE_TR;
            case 2 -> Mode.RESIZE_BL;  case 3 -> Mode.RESIZE_BR;
            case 4 -> Mode.RESIZE_T;   case 5 -> Mode.RESIZE_B;
            case 6 -> Mode.RESIZE_L;   case 7 -> Mode.RESIZE_R;
            default -> Mode.NONE;
        };
    }

    private static double clamp(double v, double min, double max) {
        return Math.min(Math.max(v, min), max);
    }

    public static Optional<CropResult> show(File imageFile, Stage owner) {
        ImageCropper cropper = new ImageCropper(imageFile);
        return cropper.showAndWait(owner);
    }

    private Optional<CropResult> showAndWait(Stage owner) {
        StackPane imageStack = new StackPane(imageView, overlayPane);
        imageStack.setMaxSize(displayW, displayH);

        Button confirmBtn = new Button("确认");
        Button cancelBtn = new Button("取消");
        confirmBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 24; -fx-cursor: hand;");
        cancelBtn.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 24; -fx-cursor: hand;");

        HBox buttonBar = new HBox(16, confirmBtn, cancelBtn);
        buttonBar.setStyle("-fx-alignment: center; -fx-padding: 12 0 16 0;");

        VBox root = new VBox(0, imageStack, buttonBar);
        root.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 16;");

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("选择图片区域");
        stage.setScene(scene);
        stage.setResizable(false);

        confirmBtn.setOnAction(e -> {
            if (selection.getWidth() > 0 && selection.getHeight() > 0) {
                int cropX = (int)(selection.getX() / scale);
                int cropY = (int)(selection.getY() / scale);
                int cropW = (int)(selection.getWidth() / scale);
                int cropH = (int)(selection.getHeight() / scale);
                Image cropped = new WritableImage(originalImage.getPixelReader(), cropX, cropY, cropW, cropH);
                stage.getProperties().put("cropResult", new CropResult(cropped, cropX, cropY, cropW, cropH));
            }
            stage.close();
        });

        cancelBtn.setOnAction(e -> stage.close());

        stage.showAndWait();
        Object data = stage.getProperties().get("cropResult");
        if (data instanceof CropResult r) return Optional.of(r);
        return Optional.empty();
    }

    public record CropResult(Image image, int x, int y, int width, int height) {}
}
