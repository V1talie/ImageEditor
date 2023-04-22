package editor;

import editor.filters.Filter;
import editor.filters.SepiaFilter;
import editor.filters.SharpenFilter;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ImageEditor extends Application {

    private ImageView imageView;
    private Stage stage;
    private Image image;
    private Rectangle2D cropRect = null;
    private Rectangle cropVisual;
    private Canvas drawingCanvas;
    private GraphicsContext graphicsContext;

    private final List<Filter> filters = Arrays.asList(
            new Filter("Invert", Color::invert),
            new Filter("Grayscale", Color::grayscale),
            new Filter("Saturate", Color::saturate),
            new Filter("Black and White", c -> valueOf(c) < 1.5 ? Color.BLACK : Color.WHITE),
            new Filter("Red", c -> Color.color(1.0, c.getGreen(), c.getBlue())),
            new Filter("Green", c -> Color.color(c.getRed(), 1.0, c.getBlue())),
            new Filter("Blue", c -> Color.color(c.getRed(), c.getGreen(), 1.0)),
            new SepiaFilter(),
            new SharpenFilter()
    );

    public ImageEditor(ImageView imageView) {
        this.imageView = imageView;
    }

    public ImageEditor() {

    }

    private double valueOf(Color c) {
        return c.getRed() + c.getGreen() + c.getBlue();
    }

    @Override
    public void start(Stage stage) {

        this.stage = stage;
        stage.setMinWidth(1600);
        stage.setMinHeight(900);
        // Load the image
        image = new Image("https://via.placeholder.com/800");

        // Create the ImageView
        imageView = new ImageView();
        imageView.setImage(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(image.getWidth());
        imageView.setFitHeight(image.getHeight());

        // Create the menu
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(event -> openImage());
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(actionEvent -> saveImage());
        fileMenu.getItems().addAll(openItem, saveItem);

        Menu editMenu = new Menu("Edit");
        MenuItem rotateLeftItem = new MenuItem("Rotate Left");
        rotateLeftItem.setOnAction(event -> rotateLeft());
        MenuItem rotateRightItem = new MenuItem("Rotate Right");
        rotateRightItem.setOnAction(event -> rotateRight());

        MenuItem cropImage = new MenuItem("Crop");
        cropImage.setOnAction(event -> cropImage());
        editMenu.getItems().addAll(rotateLeftItem, rotateRightItem, cropImage);

        Menu filterMenu = new Menu("Filters");

        filters.forEach(filter -> {
            MenuItem item = new MenuItem(filter.name);
            item.setOnAction(e -> imageView.setImage(filter.apply(imageView.getImage())));
            filterMenu.getItems().add(item);
        });

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, editMenu, filterMenu);

        // Create the BorderPane
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(imageView);


        drawingCanvas = new Canvas();
        drawingCanvas.widthProperty().bind(imageView.fitWidthProperty());
        drawingCanvas.heightProperty().bind(imageView.fitHeightProperty());
        graphicsContext = drawingCanvas.getGraphicsContext2D();
        graphicsContext.setLineWidth(2);
        graphicsContext.setStroke(Color.RED);
        Pane imagePane = new Pane(imageView, drawingCanvas);
        root.setCenter(imagePane);
        drawingCanvas.setOnMousePressed(this::drawOnCanvas);
        drawingCanvas.setOnMouseDragged(this::drawOnCanvas);

        // Create the Scene
        Scene scene = new Scene(root, 500, 500);

        // Set the stage
        stage.setScene(scene);
        stage.setTitle("Image Editor");
       // this.addZoomListener(drawingCanvas);
        stage.show();
    }

    private void drawOnCanvas(MouseEvent mouseEvent) {
        drawingCanvas.setOnMousePressed(event -> {
            graphicsContext.beginPath();
            graphicsContext.moveTo(event.getX(), event.getY());
            graphicsContext.stroke();
        });

        drawingCanvas.setOnMouseDragged(event -> {
            graphicsContext.lineTo(event.getX(), event.getY());
            graphicsContext.stroke();
        });

        drawingCanvas.setOnMouseReleased(event -> {
            graphicsContext.closePath();
        });

        // Clear
        graphicsContext.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
    }


    public void rotateLeft() {
        imageView.setRotate(imageView.getRotate() - 90);
        drawingCanvas.setRotate(drawingCanvas.getRotate() - 90);
    }

    public void rotateRight() {
        imageView.setRotate(imageView.getRotate() + 90);
        drawingCanvas.setRotate(drawingCanvas.getRotate() + 90);
    }


    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            clearCanvas();
            Image image = new Image(selectedFile.toURI().toString());
            imageView.setImage(image);
        }
    }

    private void clearCanvas() {
        graphicsContext.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
    }

    private void saveImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            WritableImage modifiedImage = new WritableImage((int) imageView.getFitWidth(), (int) imageView.getFitHeight());
            imageView.snapshot(null, modifiedImage);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            Canvas tempCanvas = new Canvas(modifiedImage.getWidth(), modifiedImage.getHeight());
            tempCanvas.getGraphicsContext2D().drawImage(modifiedImage, 0, 0);
            tempCanvas.getGraphicsContext2D().drawImage(drawingCanvas.snapshot(parameters, null), 0, 0);
            modifiedImage = tempCanvas.snapshot(parameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(modifiedImage, null), "png", selectedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cropImage() {

        cropVisual = new Rectangle();
        cropVisual.setStroke(Color.WHITE);
        cropVisual.setStrokeWidth(2);
        cropVisual.setFill(Color.rgb(255, 255, 255, 0.3));
        Pane parentPane = (Pane) imageView.getParent();
        parentPane.getChildren().add(cropVisual);

        cropRect = new Rectangle2D(0, 0, image.getWidth(), image.getHeight());

        drawingCanvas.setOnMousePressed(event -> {
            cropRect = new Rectangle2D(event.getX(), event.getY(), 0, 0);
            cropVisual.setX(cropRect.getMinX());
            cropVisual.setY(cropRect.getMinY());
            updateCropVisual();
        });

        drawingCanvas.setOnMouseDragged(event -> {
            cropRect = new Rectangle2D(cropRect.getMinX(), cropRect.getMinY(), event.getX() - cropRect.getMinX(), event.getY() - cropRect.getMinY());
            updateCropVisual();
        });
        drawingCanvas.setOnMouseReleased(event -> {
            drawingCanvas.setOnMousePressed(null);
            drawingCanvas.setOnMouseDragged(null);
            drawingCanvas.setOnMouseReleased(null);
            drawingCanvas.setPickOnBounds(false);

            if (cropRect.getWidth() > 0 && cropRect.getHeight() > 0) {
                finishCrop();
            } else {
                cancelCrop();
            }
        });
    }

    private void cancelCrop() {
        drawingCanvas.setOnMousePressed(null);
        drawingCanvas.setOnMouseDragged(null);
        drawingCanvas.setOnMouseReleased(null);
        drawingCanvas.setPickOnBounds(false);
        drawingCanvas.getParent().getChildrenUnmodifiable().remove(cropVisual);
    }

    private void updateCropVisual() {
        cropVisual.setX(cropRect.getMinX());
        cropVisual.setY(cropRect.getMinY());
        cropVisual.setWidth(cropRect.getWidth());
        cropVisual.setHeight(cropRect.getHeight());
    }

    private void finishCrop() {
        Pane parentPane = (Pane) drawingCanvas.getParent();
        parentPane.getChildren().remove(cropVisual);
        imageView.setViewport(cropRect);
        cancelCrop();
    }

//    private void addZoomListener(Canvas drawingCanvas) {
//        final double SCALE_DELTA = 1.1;
//        final double MIN_SCALE = 0.1;
//        final double MAX_SCALE = 10.0;
//        Scale scale = new Scale(1, 1, 0, 0);
//        drawingCanvas.getTransforms().add(scale);
//        drawingCanvas.setOnScroll(event -> {
//            System.out.println("asdasd");
//            double deltaY = event.getDeltaY();
//            double scaleFactor = deltaY > 0 ? SCALE_DELTA : 1 / SCALE_DELTA;
//            double oldScale = scale.getX();
//            double newScale = oldScale * scaleFactor;
//            if (newScale > MAX_SCALE) {
//                newScale = MAX_SCALE;
//            } else if (newScale < MIN_SCALE) {
//                newScale = MIN_SCALE;
//            }
//
//            scale.setX(newScale);
//            scale.setY(newScale);
//
//            // adjust scroll position to keep the mouse position fixed
//            double mousePosX = (event.getX() - drawingCanvas.getBoundsInParent().getMinX()) / oldScale;
//            double mousePosY = (event.getY() - drawingCanvas.getBoundsInParent().getMinY()) / oldScale;
//            double scrollPosX = (mousePosX * newScale + drawingCanvas.getBoundsInParent().getMinX() - event.getX());
//            double scrollPosY = (mousePosY * newScale + drawingCanvas.getBoundsInParent().getMinY() - event.getY());
//
//            drawingCanvas.setTranslateX(drawingCanvas.getTranslateX() + scrollPosX);
//            imageView.setTranslateX(imageView.getTranslateX() + scrollPosX);
//            drawingCanvas.setTranslateY(drawingCanvas.getTranslateY() + scrollPosY);
//            imageView.setTranslateY(imageView.getTranslateY() + scrollPosY);
//
//            event.consume();
//        });
//    }


    public static void main(String[] args) {
        launch(args);
    }
}