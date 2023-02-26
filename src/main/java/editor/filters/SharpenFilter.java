package editor.filters;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class SharpenFilter extends Filter {

    private final double[][] kernel = {
            {-0.1, -0.1, -0.1},
            {-0.1, 1.8, -0.1},
            {-0.1, -0.1, -0.1}
    };

    public SharpenFilter() {
        super("Sharpen", null);
    }

    @Override
    public Image apply(Image image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();

        WritableImage newImage = new WritableImage(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double red = 0, green = 0, blue = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int nx = x + i;
                        int ny = y + j;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            Color color = image.getPixelReader().getColor(nx, ny);
                            red += color.getRed() * kernel[i+1][j+1];
                            green += color.getGreen() * kernel[i+1][j+1];
                            blue += color.getBlue() * kernel[i+1][j+1];
                        }
                    }
                }
                red = Math.min(Math.max(red, 0), 1);
                green = Math.min(Math.max(green, 0), 1);
                blue = Math.min(Math.max(blue, 0), 1);
                Color newColor = Color.color(red, green, blue);
                newImage.getPixelWriter().setColor(x, y, newColor);
            }
        }

        return newImage;
    }
}

