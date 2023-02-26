package editor.filters;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.function.Function;

public class Filter implements Function<Image, Image> {

    public String name;
    private final Function<Color, Color> colorMap;

    public Filter(String name, Function<Color, Color> colorMap) {
        this.name = name;
        this.colorMap = colorMap;
    }

    @Override
    public Image apply(Image image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();

        WritableImage newImage = new WritableImage(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c1 = image.getPixelReader().getColor(x, y);
                Color c2 = colorMap.apply(c1);

                newImage.getPixelWriter().setColor(x, y, c2);
            }
        }

        return newImage;
    }
}
