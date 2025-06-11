package GUI;

import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * The {@code ResizeHelper} class provides utility methods to enable custom window resizing
 * for undecorated JavaFX stages. It attaches mouse listeners to a {@link Region} to allow
 * resizing the window by dragging its edges or corners.
 * <p>
 * This is useful for applications with custom window decorations that need manual resize logic.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class ResizeHelper {
    /**
     * The margin (in pixels) from the window edge where resizing is enabled.
     */
    private static final int RESIZE_MARGIN = 10;

    /**
     * Adds mouse listeners to the specified {@link Region} to allow resizing the given {@link Stage}.
     * The cursor changes to indicate the resize direction when hovering near the window edges or corners.
     *
     * @param stage The JavaFX {@link Stage} to resize.
     * @param root  The {@link Region} to attach the resize listeners to.
     */
    public static void addResizeListener(Stage stage, Region root) {
        root.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            double width = root.getWidth();
            double height = root.getHeight();

            Cursor cursor = Cursor.DEFAULT;
            if (mouseX < RESIZE_MARGIN && mouseY < RESIZE_MARGIN) {
                cursor = Cursor.NW_RESIZE;
            } else if (mouseX > width - RESIZE_MARGIN && mouseY < RESIZE_MARGIN) {
                cursor = Cursor.NE_RESIZE;
            } else if (mouseX < RESIZE_MARGIN && mouseY > height - RESIZE_MARGIN) {
                cursor = Cursor.SW_RESIZE;
            } else if (mouseX > width - RESIZE_MARGIN && mouseY > height - RESIZE_MARGIN) {
                cursor = Cursor.SE_RESIZE;
            } else if (mouseX < RESIZE_MARGIN) {
                cursor = Cursor.W_RESIZE;
            } else if (mouseX > width - RESIZE_MARGIN) {
                cursor = Cursor.E_RESIZE;
            } else if (mouseY < RESIZE_MARGIN) {
                cursor = Cursor.N_RESIZE;
            } else if (mouseY > height - RESIZE_MARGIN) {
                cursor = Cursor.S_RESIZE;
            }

            root.setCursor(cursor);
        });

        root.setOnMousePressed(event -> {
            root.setUserData(new double[]{event.getSceneX(), event.getSceneY(), stage.getWidth(), stage.getHeight()});
        });

        root.setOnMouseDragged(event -> {
            Cursor cursor = root.getCursor();
            double[] userData = (double[]) root.getUserData();

            double dx = event.getScreenX() - stage.getX();
            double dy = event.getScreenY() - stage.getY();

            if (cursor == Cursor.E_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.NE_RESIZE) {
                stage.setWidth(Math.max(userData[2] + dx - userData[0], 200));
            }
            if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
                stage.setHeight(Math.max(userData[3] + dy - userData[1], 200));
            }
            if (cursor == Cursor.W_RESIZE || cursor == Cursor.SW_RESIZE || cursor == Cursor.NW_RESIZE) {
                double newWidth = Math.max(userData[2] - dx + userData[0], 200);
                double newX = event.getScreenX();
                stage.setX(newX);
                stage.setWidth(newWidth);
            }
            if (cursor == Cursor.N_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.NE_RESIZE) {
                double newHeight = Math.max(userData[3] - dy + userData[1], 200);
                double newY = event.getScreenY();
                stage.setY(newY);
                stage.setHeight(newHeight);
            }
        });
    }
}
