import editor.ImageEditor;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageEditorTest {

    @Test
    void testRotateLeft() {
        ImageView imageView = new ImageView();
        double initialRotation = 45;
        imageView.setRotate(initialRotation);

        ImageEditor editor = new ImageEditor(imageView);
        editor.rotateLeft();

        double expectedRotation = initialRotation - 90;
        double actualRotation = imageView.getRotate();
        assertEquals(expectedRotation, actualRotation, 0.001);
    }
}

