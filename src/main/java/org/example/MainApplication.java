package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Random;

public class MainApplication extends Application {

    private World world;
    private Canvas mapCanvas;
    private SubScene globeScene;
    private Sphere globe;
    private ToggleGroup layerGroup;
    private Label tooltipLabel;
    private Slider seaLevelSlider;
    private Slider worldSizeSlider;
    private TabPane tabPane;

    private double mouseX = 0, mouseY = 0;
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private boolean autoRotate = true;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("World Simulator");

        // Initialize world with default settings
        world = new World(512, 0.5);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top controls
        root.setTop(createControlPanel());

        // Center - tabbed view
        tabPane = new TabPane();

        // 2D Map tab
        Tab mapTab = new Tab("2D Map");
        mapTab.setClosable(false);
        mapCanvas = new Canvas(1000, 600);
        setupMapInteraction();
        StackPane mapPane = new StackPane(mapCanvas);
        mapTab.setContent(mapPane);

        // 3D Globe tab
        Tab globeTab = new Tab("3D Globe");
        globeTab.setClosable(false);
        globeTab.setContent(create3DGlobe());

        tabPane.getTabs().addAll(mapTab, globeTab);
        root.setCenter(tabPane);

        // Bottom tooltip
        tooltipLabel = new Label("Hover over the map to see details");
        tooltipLabel.setPadding(new Insets(5));
        tooltipLabel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");
        root.setBottom(tooltipLabel);

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Generate initial world
        generateWorld();

        // Auto-rotation animation
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                if (autoRotate && tabPane.getSelectionModel().getSelectedIndex() == 1) {
                    rotateY.setAngle(rotateY.getAngle() + 0.04);
                }
            }
        };
        timer.start();
    }

    private HBox createControlPanel() {
        HBox controlPanel = new HBox(15);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER_LEFT);

        Button generateBtn = new Button("Generate World");
        generateBtn.setOnAction(e -> generateWorld());

        Button saveBtn = new Button("Save Image");
        saveBtn.setOnAction(e -> saveImage());

        // Layer selection
        Label layerLabel = new Label("Layer:");
        layerGroup = new ToggleGroup();

        RadioButton terrainBtn = new RadioButton("Terrain");
        terrainBtn.setToggleGroup(layerGroup);
        terrainBtn.setSelected(true);
        terrainBtn.setOnAction(e -> renderMap());

        RadioButton biomeBtn = new RadioButton("Biomes");
        biomeBtn.setToggleGroup(layerGroup);
        biomeBtn.setOnAction(e -> renderMap());

        RadioButton tempBtn = new RadioButton("Temperature");
        tempBtn.setToggleGroup(layerGroup);
        tempBtn.setOnAction(e -> renderMap());

        RadioButton humidBtn = new RadioButton("Humidity");
        humidBtn.setToggleGroup(layerGroup);
        humidBtn.setOnAction(e -> renderMap());

        Separator sep1 = new Separator();
        sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Separator sep2 = new Separator();
        sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Sliders
        VBox sliderBox = new VBox(5);
        Label sizeLabel = new Label("World Size: 512");
        worldSizeSlider = new Slider(256, 1024, 512);
        worldSizeSlider.setShowTickLabels(false);
        worldSizeSlider.setShowTickMarks(true);
        worldSizeSlider.setMajorTickUnit(256);
        worldSizeSlider.valueProperty().addListener((obs, old, val) ->
                sizeLabel.setText("World Size: " + val.intValue()));

        Label seaLabel = new Label("Sea Level: 0.50");
        seaLevelSlider = new Slider(0, 0.7, 0.5);
        seaLevelSlider.setShowTickLabels(false);
        seaLevelSlider.setShowTickMarks(true);
        seaLevelSlider.valueProperty().addListener((obs, old, val) ->
                seaLabel.setText(String.format("Sea Level: %.2f", val.doubleValue())));

        sliderBox.getChildren().addAll(sizeLabel, worldSizeSlider, seaLabel, seaLevelSlider);

        controlPanel.getChildren().addAll(
                generateBtn, saveBtn, sep1, layerLabel,
                terrainBtn, biomeBtn, tempBtn, humidBtn, sep2, sliderBox
        );

        return controlPanel;
    }

    private Parent create3DGlobe() {
        Group root3D = new Group();

        globe = new Sphere(150);
        globe.getTransforms().addAll(rotateX, rotateY);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.LIGHTBLUE);
        globe.setMaterial(material);

        root3D.getChildren().add(globe);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-800);
        camera.setNearClip(0.1);
        camera.setFarClip(2000.0);

        SubScene subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);

        // Mouse interaction
        subScene.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            autoRotate = false;
        });

        subScene.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - mouseX;
            double dy = event.getSceneY() - mouseY;
            rotateY.setAngle(rotateY.getAngle() + dx * 0.5);
            rotateX.setAngle(rotateX.getAngle() - dy * 0.5);
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        subScene.setOnMouseReleased(event -> autoRotate = true);

        globeScene = subScene;

        StackPane container = new StackPane(subScene);

        subScene.widthProperty().bind(container.widthProperty());
        subScene.heightProperty().bind(container.heightProperty());

        return container;
    }

    private void setupMapInteraction() {
        mapCanvas.setOnMouseMoved(event -> {
            int x = (int)(event.getX() / mapCanvas.getWidth() * world.size);
            int y = (int)(event.getY() / mapCanvas.getHeight() * world.size);

            if (x >= 0 && x < world.size && y >= 0 && y < world.size) {
                double lat = 90 - (y * 180.0 / world.size);
                double lon = (x * 360.0 / world.size) - 180;
                double elevation = world.elevation[x][y];
                double temp = world.temperature[x][y];
                double humid = world.humidity[x][y];
                String biome = world.biomes[x][y].toString();

                tooltipLabel.setText(String.format(
                        "Lat: %.1f\u00B0, Lon: %.1f\u00B0 | Elevation: %.2f | Temp: %.1f\u00B0C | Humidity: %.0f%% | Biome: %s",
                        lat, lon, elevation, temp, humid * 100, biome
                ));
            }
        });
    }

    private void generateWorld() {
        int size = (int)worldSizeSlider.getValue();
        double seaLevel = seaLevelSlider.getValue();

        world = new World(size, seaLevel);
        world.generate();

        renderMap();
        updateGlobeTexture();
    }

    private void renderMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double w = mapCanvas.getWidth();
        double h = mapCanvas.getHeight();

        RadioButton selected = (RadioButton) layerGroup.getSelectedToggle();
        String layer = selected.getText();

        for (int y = 0; y < world.size; y++) {
            for (int x = 0; x < world.size; x++) {
                Color color = switch (layer) {
                    case "Terrain" -> getTerrainColor(world.elevation[x][y], world.seaLevel);
                    case "Biomes" -> getBiomeColor(world.biomes[x][y]);
                    case "Temperature" -> getTemperatureColor(world.temperature[x][y]);
                    case "Humidity" -> getHumidityColor(world.humidity[x][y]);
                    default -> Color.BLACK;
                };

                gc.setFill(color);
                double px = x * w / world.size;
                double py = y * h / world.size;
                // Use ceiling + 1 to prevent 1-pixel gaps
                double pw = Math.ceil(w / world.size) + 1;
                double ph = Math.ceil(h / world.size) + 1;
                gc.fillRect(px, py, pw, ph);
            }
        }
    }

    private void updateGlobeTexture() {
        WritableImage texture = new WritableImage(world.size, world.size);

        for (int y = 0; y < world.size; y++) {
            for (int x = 0; x < world.size; x++) {
                Color color = getBiomeColor(world.biomes[x][y]);
                texture.getPixelWriter().setColor(x, y, color);
            }
        }

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(texture);
        globe.setMaterial(material);
    }

    private Color getTerrainColor(double elevation, double seaLevel) {
        if (elevation < seaLevel) {
            double depth = (seaLevel - elevation) / seaLevel;
            return Color.rgb(0, (int)(100 * (1-depth)), (int)(150 + 105 * (1-depth)));
        } else {
            double height = (elevation - seaLevel) / (1.0 - seaLevel);
            if (height < 0.2) return Color.rgb(34, 139, 34);  // Green lowlands
            else if (height < 0.4) return Color.rgb(107, 142, 35);  // Olive hills
            else if (height < 0.7) return Color.rgb(139, 137, 137);  // Gray mountains
            else return Color.rgb(255, 250, 250);  // White peaks
        }
    }

    private Color getBiomeColor(Biome biome) {
        return switch (biome) {
            case OCEAN -> Color.rgb(0, 105, 148);
            case TUNDRA -> Color.rgb(221, 221, 187);
            case TAIGA -> Color.rgb(153, 170, 119);
            case GRASSLAND -> Color.rgb(196, 212, 170);
            case TEMPERATE_FOREST -> Color.rgb(136, 170, 85);
            case TROPICAL_RAINFOREST -> Color.rgb(61, 130, 61);
            case DESERT -> Color.rgb(238, 218, 130);
            case SAVANNA -> Color.rgb(177, 209, 110);
            case MEDITERRANEAN -> Color.rgb(200, 200, 120);
            case MOUNTAIN -> Color.rgb(158, 158, 158);
        };
    }

    private Color getTemperatureColor(double temp) {
        // -30°C to 40°C mapped to blue -> red
        double normalized = (temp + 30) / 70.0;
        normalized = Math.max(0, Math.min(1, normalized));

        if (normalized < 0.5) {
            return Color.rgb(0, (int)(normalized * 255 * 2), 255);
        } else {
            return Color.rgb((int)((normalized - 0.5) * 255 * 2), (int)((1-normalized) * 255 * 2), 0);
        }
    }

    private Color getHumidityColor(double humidity) {
        int val = (int)(humidity * 255);
        return Color.rgb(255 - val, 255 - val, 255);
    }

    private void saveImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save World Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );

        File file = fileChooser.showSaveDialog(mapCanvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage image = new WritableImage((int)mapCanvas.getWidth(),
                        (int)mapCanvas.getHeight());
                mapCanvas.snapshot(null, image);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Image saved successfully!");
                alert.showAndWait();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to save image");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}