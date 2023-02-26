package editor.filters;

import javafx.scene.paint.Color;

public class SepiaFilter extends Filter {

    public SepiaFilter() {
        super("Sepia", c -> {
            double r = Math.min(1.0, 0.393 * c.getRed() + 0.769 * c.getGreen() + 0.189 * c.getBlue());
            double g = Math.min(1.0, 0.349 * c.getRed() + 0.686 * c.getGreen() + 0.168 * c.getBlue());
            double b = Math.min(1.0, 0.272 * c.getRed() + 0.534 * c.getGreen() + 0.131 * c.getBlue());
            return Color.color(r, g, b);
        });
    }
}
