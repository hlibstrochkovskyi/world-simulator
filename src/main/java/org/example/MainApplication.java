package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
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
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MainApplication extends Application {

    private World world;
    private Canvas mapCanvas;
    private SubScene globeScene;
    private Sphere globe;
    private ToggleGroup layerGroup;
    private Label tooltipLabel;
    private Slider seaLevelSlider;
    private Slider worldScaleSlider;
    private Slider numStatesSlider;
    private CheckBox statesCheckBox;
    private Slider worldDetailSlider;
    private Slider worldSizeSlider;
    private TabPane tabPane;

    private double mouseX = 0, mouseY = 0;
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private boolean autoRotate = true;
    private static final String[] CAPITAL_NAMES = new String[] {
            "Avalen", "Mirath", "Solmera", "Virelia", "Dravon", "Lantora", "Orenvale", "Castrel", "Zandira", "Helvorn",
            "Trisora", "Nuvane", "Ralden", "Etralia", "Mornath", "Cydara", "Alvenor", "Polmera", "Kyronis", "Thelmar",
            "Rivaren", "Zolcar", "Ferin", "Bryndal", "Elvoria", "Northel", "Pryden", "Arisca", "Velthir", "Sundora",
            "Olthar", "Lysmar", "Quavon", "Noreth", "Valenra", "Darmstadt", "Tyvra", "Merthil", "Azuron", "Delvra",
            "Soltaren", "Arvenia", "Clyria", "Dornath", "Tyrelon", "Ismara", "Halven", "Breltar", "Fendris", "Qarthia",
            "Lormund", "Zephyra", "Vantera", "Asmir", "Pelanor", "Drevin", "Corvane", "Mynora", "Rethen", "Estalia",
            "Falren", "Jandor", "Zenvia", "Teralon", "Vrosia", "Melthra", "Astire", "Korvale", "Vandra", "Olyrion",
            "Tashven", "Myrden", "Elnora", "Galdir", "Sovria", "Lureth", "Fenora", "Cravon", "Bellith", "Drasden",
            "Yorath", "Zelven", "Amrya", "Talven", "Crestin", "Vulmar", "Trubasos", "Marthos", "Sevren", "Paltha",
            "Nelora", "Irvane", "Wendar", "Avenor", "Phyra", "Colven", "Thalir", "Evandor", "Mireth", "Solvir",
            "Erthyn", "Rovanis", "Caelra", "Orthil", "Vireth", "Trandor", "Lurelia", "Cendral", "Borthen", "Elyra",
            "Dalthor", "Monira", "Zorath", "Faldra", "Arvion", "Kestra", "Yundar", "Molven", "Prestal", "Zandor",
            "Thirion", "Orenda", "Malthea", "Xyrel", "Vorath", "Lurven", "Enlira", "Phandor", "Surnen", "Velis",
            "Novara", "Odessa", "Eryndor", "Torsen", "Havria", "Crendal", "Vathen", "Eltira", "Forvia", "Dranor",
            "Lysten", "Korvia", "Nemora", "Thyndal", "Olevra", "Calith", "Vordel", "Elmyra", "Sornath", "Triven",
            "Yarven", "Avyra", "Renlor", "Mistra", "Polven", "Jorath", "Zelvia", "Dalmor", "Venora", "Althir",
            "Cryneth", "Nolvar", "Perden", "Vyrona", "Ostrel", "Myrath", "Glendor", "Salvia", "Craven", "Belthor",
            "Venith", "Ormira", "Valmar", "Fenlir", "Qendra", "Sylven", "Ravora", "Aleris", "Torven", "Deryn",
            "Zorvia", "Relmar", "Athenra", "Vunor", "Caldra", "Nirven", "Faldir", "Velria", "Orisar", "Kendria",
            "Meral", "Thraven", "Salnor", "Pfungstadt", "Dorven", "Elthra", "Moryn", "Vaslen", "Trilora", "Olyven",
            "Jandria", "Sylmar", "Feroth", "Bronel", "Avyron", "Delmar", "Corven", "Tyvora", "Nistra", "Welyra",
            "Envar", "Lyssen", "Rynor", "Thalven", "Qyros", "Zelmar", "Arenth", "Fenora", "Vornis", "Lysend",
            "Halria", "Odrin", "Myrsel", "Valtor", "Enoria", "Trisden", "Orlven", "Sylvenia", "Derath", "Phyron",
            "Nolvera", "Ardal", "Belmar", "Xendria", "Coris", "Vashor", "Lentis", "Yorven", "Halden", "Fynora",
            "Voltris", "Elrath", "Melvon", "Tarven", "Zorlin", "Avendra", "Rilmar", "Prylia", "Dorath", "Cynora"
    };



    /**
     * The main entry point for the JavaFX application.
     * Sets up the primary stage, initializes the world, and creates the UI layout.
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("World Simulator");

        world = new World(512, 0.5, 2.0, 5, null);

        BorderPane root = new BorderPane();
        root.setTop(createToolBar());

        root.setLeft(createSettingsPanel());

        tabPane = new TabPane();

        Tab mapTab = new Tab("2D Map");
        mapTab.setClosable(false);
        mapCanvas = new Canvas(1000, 600);
        setupMapInteraction();
        StackPane mapPane = new StackPane(mapCanvas);
        mapPane.setStyle("-fx-background-color: #222;");
        mapTab.setContent(mapPane);

        Tab globeTab = new Tab("3D Globe");
        globeTab.setClosable(false);
        globeTab.setContent(create3DGlobe());

        tabPane.getTabs().addAll(mapTab, globeTab);
        root.setCenter(tabPane);

        tooltipLabel = new Label("Hover over the map to see details");
        tooltipLabel.setPadding(new Insets(5));
        tooltipLabel.getStyleClass().add("tooltip-bar");
        root.setBottom(tooltipLabel);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        Text emojiText = new Text("\uD83C\uDF0D");
        emojiText.setFont(Font.font("Segoe UI Emoji", 64));
        Image icon = emojiText.snapshot(null, null);
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene);
        primaryStage.show();

        generateWorld();

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

    /**
     * Creates the top ToolBar with action buttons and layer toggles.
     * @return A `Node` containing the ToolBar.
     */
    private Node createToolBar() {
        Button generateBtn = new Button("Generate World");
        generateBtn.setOnAction(e -> generateWorld());

        Button saveBtn = new Button("Save Image");
        saveBtn.setOnAction(e -> saveImage());

        // Layer selection
        layerGroup = new ToggleGroup();

        ToggleButton terrainBtn = new ToggleButton("Terrain");
        terrainBtn.setToggleGroup(layerGroup);
        terrainBtn.setSelected(true);
        terrainBtn.setOnAction(e -> onLayerChange());

        ToggleButton biomeBtn = new ToggleButton("Biomes");
        biomeBtn.setToggleGroup(layerGroup);
        biomeBtn.setOnAction(e -> onLayerChange());

        ToggleButton tempBtn = new ToggleButton("Temperature");
        tempBtn.setToggleGroup(layerGroup);
        tempBtn.setOnAction(e -> onLayerChange());

        ToggleButton humidBtn = new ToggleButton("Humidity");
        humidBtn.setToggleGroup(layerGroup);
        humidBtn.setOnAction(e -> onLayerChange());

        ToggleButton statesBtn = new ToggleButton("States");
        statesBtn.setToggleGroup(layerGroup);
        statesBtn.setOnAction(e -> onLayerChange());

        ToolBar toolBar = new ToolBar(
                generateBtn,
                saveBtn,
                new Separator(),
                new Label("Layers:"),
                terrainBtn,
                biomeBtn,
                tempBtn,
                humidBtn,
                statesBtn
        );

        return toolBar;
    }

    /**
     * Creates the left settings panel with sliders and options in an Accordion.
     * @return A `Node` containing the settings panel.
     */
    private Node createSettingsPanel() {
        Accordion accordion = new Accordion();

        // --- Section 1: World Parameters ---
        VBox worldSettingsBox = new VBox(10);
        worldSettingsBox.setPadding(new Insets(10));

        // --- World Size Slider ---
        Label sizeLabel = new Label("World Size: 512");
        worldSizeSlider = new Slider(256, 1024, 512);
        worldSizeSlider.setShowTickLabels(true);
        worldSizeSlider.setShowTickMarks(true);
        worldSizeSlider.setMajorTickUnit(256);
        worldSizeSlider.valueProperty().addListener((obs, old, val) ->
                sizeLabel.setText("World Size: " + val.intValue()));

        // --- Sea Level Slider ---
        Label seaLabel = new Label("Sea Level: 0.50");
        seaLevelSlider = new Slider(0.3, 0.7, 0.5);
        seaLevelSlider.valueProperty().addListener((obs, old, val) ->
                seaLabel.setText(String.format("Sea Level: %.2f", val.doubleValue())));

        // --- World Scale Slider ---
        Label scaleLabel = new Label("World Scale: 2.0");
        worldScaleSlider = new Slider(0.5, 5.0, 2.0);
        worldScaleSlider.valueProperty().addListener((obs, old, val) ->
                scaleLabel.setText(String.format("World Scale: %.1f", val.doubleValue())));

        // --- Detail Level Slider ---
        Label detailLabel = new Label("Detail Level: 5");
        worldDetailSlider = new Slider(1, 8, 5);
        worldDetailSlider.setBlockIncrement(1);
        worldDetailSlider.setMajorTickUnit(1);
        worldDetailSlider.setMinorTickCount(0);
        worldDetailSlider.setSnapToTicks(true);
        worldDetailSlider.valueProperty().addListener((obs, old, val) ->
                detailLabel.setText("Detail Level: " + val.intValue()));

        worldSettingsBox.getChildren().addAll(
                sizeLabel, worldSizeSlider,
                seaLabel, seaLevelSlider,
                scaleLabel, worldScaleSlider,
                detailLabel, worldDetailSlider
        );

        TitledPane worldPane = new TitledPane("World Shape", worldSettingsBox);

        // --- Section 2: States ---
        VBox statesSettingsBox = new VBox(10);
        statesSettingsBox.setPadding(new Insets(10));

        // States CheckBox
        statesCheckBox = new CheckBox("Generate States");
        statesCheckBox.setSelected(false);

        // Number of States
        Label statesLabel = new Label("Number of States: 50");
        numStatesSlider = new Slider(10, 250, 50);
        numStatesSlider.setBlockIncrement(1);
        numStatesSlider.setSnapToTicks(true);
        numStatesSlider.valueProperty().addListener((obs, old, val) ->
                statesLabel.setText("Number of States: " + val.intValue()));

        // Bind slider to checkbox
        numStatesSlider.disableProperty().bind(statesCheckBox.selectedProperty().not());
        statesLabel.disableProperty().bind(statesCheckBox.selectedProperty().not());

        statesSettingsBox.getChildren().addAll(
                statesCheckBox,
                statesLabel,
                numStatesSlider
        );

        TitledPane statesPane = new TitledPane("States", statesSettingsBox);

        VBox settingsContainer = new VBox(10);
        settingsContainer.getChildren().addAll(worldPane, statesPane);

        settingsContainer.setStyle("-fx-background-color: #2D2D2D;");


        return settingsContainer;
    }


    /**
     * Creates the 3D globe view using a JavaFX `SubScene`.
     * Adds mouse interaction for rotating the globe and sets up auto-rotation.
     * @return A `Parent` node containing the 3D globe view.
     */
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

        subScene.setCursor(Cursor.OPEN_HAND);

        subScene.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            autoRotate = false;
            subScene.setCursor(Cursor.CLOSED_HAND);
        });

        subScene.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - mouseX;
            double dy = event.getSceneY() - mouseY;
            rotateY.setAngle(rotateY.getAngle() - dx * 0.5);
            rotateX.setAngle(rotateX.getAngle() + dy * 0.5);
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        subScene.setOnMouseReleased(event -> {
            autoRotate = true;
            subScene.setCursor(Cursor.OPEN_HAND);
        });

        subScene.setOnMouseExited(event -> {
            subScene.setCursor(Cursor.DEFAULT);
        });

        globeScene = subScene;

        StackPane container = new StackPane(subScene);
        subScene.widthProperty().bind(container.widthProperty());
        subScene.heightProperty().bind(container.heightProperty());

        return container;
    }


    /**
     * Sets up mouse move interactions for the 2D map canvas.
     * Displays a tooltip with details of the cell under the cursor.
     */
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

                String capitalInfo = "";
                if (world.stateNames != null && world.stateID[x][y] > 0) {
                    String capitalName = world.stateNames[world.stateID[x][y]];
                    if (capitalName != null) {
                        capitalInfo = " | Capital: " + capitalName;
                    }
                }

                tooltipLabel.setText(String.format(
                        "Lat: %.1f\u00B0, Lon: %.1f\u00B0 | Elevation: %.2f | Temp: %.1f\u00B0C | Humidity: %.0f%% | Biome: %s%s",
                        lat, lon, elevation, temp, humid * 100, biome, capitalInfo
                ));
            }
        });
    }


    /**
     * Generates a new world based on the current slider values.
     * Updates the world size, sea level, scale, detail level, and state generation settings.
     * Renders the 2D map and updates the 3D globe texture.
     */
    private void generateWorld() {
        int size = (int)worldSizeSlider.getValue();
        double seaLevel = seaLevelSlider.getValue();
        double scale = worldScaleSlider.getValue();
        int octaves = (int)worldDetailSlider.getValue();
        boolean generateStates = statesCheckBox.isSelected();
        int numStates = (int)numStatesSlider.getValue();

        String[] stateNames = null;
        if (generateStates) {
            Random rand = new Random();
            stateNames = new String[numStates + 1];
            for (int i = 1; i <= numStates; i++) {
                stateNames[i] = CAPITAL_NAMES[rand.nextInt(CAPITAL_NAMES.length)];
            }
        }

        world = new World(size, seaLevel, scale, octaves, stateNames);
        world.generate(generateStates, numStates);

        renderMap();
        updateGlobeTexture();
    }

    /**
     * Called when the layer selection changes.
     * Redraws both the 2D map and the 3D globe.
     */
    private void onLayerChange() {
        renderMap();
        updateGlobeTexture();
    }


    /**
     * Renders the 2D map onto the canvas based on the selected layer.
     * Iterates through each cell of the world grid and determines the color
     * based on the selected layer (e.g., Terrain, Biomes, Temperature, etc.).
     * Handles special cases such as state borders for the "States" layer.
     */
    private void renderMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double w = mapCanvas.getWidth();
        double h = mapCanvas.getHeight();

        if (layerGroup.getSelectedToggle() == null) {
            return;
        }
        ToggleButton selected = (ToggleButton) layerGroup.getSelectedToggle();
        String layer = selected.getText();

        for (int y = 0; y < world.size; y++) {
            for (int x = 0; x < world.size; x++) {
                Color color = switch (layer) {
                    case "Terrain" -> getTerrainColor(world.elevation[x][y], world.seaLevel);
                    case "Biomes" -> getBiomeColor(world.biomes[x][y]);
                    case "Temperature" -> getTemperatureColor(world.temperature[x][y]);
                    case "Humidity" -> getHumidityColor(world.humidity[x][y]);
                    case "States" -> {
                        if (world.stateColors == null || world.stateID == null) {
                            yield getTerrainColor(world.elevation[x][y], world.seaLevel);
                        }
                        if (world.elevation[x][y] < world.seaLevel) {
                            yield getTerrainColor(world.elevation[x][y], world.seaLevel);
                        }

                        int owner = world.stateID[x][y];
                        if (owner == 0) {
                            yield getTerrainColor(world.elevation[x][y], world.seaLevel);
                        }

                        boolean isBorder = false;
                        int[] dx = {0, 0, 1, -1};
                        int[] dy = {1, -1, 0, 0};

                        for (int i = 0; i < 4; i++) {
                            int nx = (x + dx[i] + world.size) % world.size;
                            int ny = y + dy[i];
                            if (ny < 0 || ny >= world.size) continue;

                            if (world.elevation[nx][ny] < world.seaLevel) {
                                isBorder = true; // Border with the ocean
                                break;
                            }

                            int neighborOwner = world.stateID[nx][ny];
                            if (neighborOwner != 0 && neighborOwner != owner) {
                                isBorder = true; // Border with another state
                                break;
                            }
                        }

                        yield isBorder ? Color.BLACK : world.stateColors[owner];
                    }

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


    /**
     * Updates the globe texture by rendering the selected layer (e.g., Terrain, Biomes, Temperature, etc.)
     * onto a WritableImage and applying it as the diffuse map of the globe.
     */
    private void updateGlobeTexture() {
        WritableImage texture = new WritableImage(world.size, world.size);

        if (layerGroup.getSelectedToggle() == null) {
            return;
        }
        ToggleButton selected = (ToggleButton) layerGroup.getSelectedToggle();
        String layer = selected.getText();

        for (int y = 0; y < world.size; y++) {
            for (int x = 0; x < world.size; x++) {

                Color color = switch (layer) {
                    case "Terrain" -> getTerrainColor(world.elevation[x][y], world.seaLevel);
                    case "Biomes" -> getBiomeColor(world.biomes[x][y]);
                    case "Temperature" -> getTemperatureColor(world.temperature[x][y]);
                    case "Humidity" -> getHumidityColor(world.humidity[x][y]);
                    case "States" -> {
                        if (world.stateColors == null || world.stateID == null) {
                            yield getTerrainColor(world.elevation[x][y], world.seaLevel);
                        }
                        if (world.elevation[x][y] < world.seaLevel) {
                            yield getTerrainColor(world.elevation[x][y], world.seaLevel);
                        }

                        int owner = world.stateID[x][y];
                        if (owner == 0) {
                            yield getTerrainColor(world.elevation[x][y], world.seaLevel);
                        }

                        boolean isBorder = false;
                        int[] dx = {0, 0, 1, -1};
                        int[] dy = {1, -1, 0, 0};

                        for (int i = 0; i < 4; i++) {
                            int nx = (x + dx[i] + world.size) % world.size;
                            int ny = y + dy[i];
                            if (ny < 0 || ny >= world.size) continue;

                            if (world.elevation[nx][ny] < world.seaLevel) {
                                isBorder = true; // Border with the ocean
                                break;
                            }

                            int neighborOwner = world.stateID[nx][ny];
                            if (neighborOwner != 0 && neighborOwner != owner) {
                                isBorder = true; // Border with another state
                                break;
                            }
                        }

                        yield isBorder ? Color.BLACK : world.stateColors[owner];
                    }

                    default -> Color.BLACK;
                };

                texture.getPixelWriter().setColor(x, y, color);
            }
        }

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(texture);
        globe.setMaterial(material);
    }


    /**
     * Determines the terrain color based on elevation and sea level.
     * @param elevation The elevation value of the terrain (0.0 to 1.0).
     * @param seaLevel The sea level threshold (0.0 to 1.0).
     * @return The color representing the terrain, varying from ocean blue to mountain white.
     */
    private Color getTerrainColor(double elevation, double seaLevel) {
        if (elevation < seaLevel) {
            double depth = (seaLevel - elevation) / seaLevel;
            return Color.rgb(0, (int)(100 * (1-depth)), (int)(150 + 105 * (1-depth)));
        } else {
            double height = (elevation - seaLevel) / (1.0 - seaLevel);
            if (height < 0.05) return Color.rgb(81, 154, 45);
            else if (height < 0.2) return Color.rgb(34, 139, 34);  // Green lowlands
            else if (height < 0.4) return Color.rgb(107, 142, 35);  // Olive hills
            else if (height < 0.65) return Color.rgb(139, 137, 137);
            else if (height < 0.70) return Color.rgb(105, 105, 100);// Gray mountains
            else return Color.rgb(255, 250, 250);  // White peaks
        }
    }


    /**
     * Determines the biome color based on the biome type.
     * @param biome The biome type (e.g., OCEAN, TUNDRA, GRASSLAND, etc.).
     * @return The color representing the biome.
     */
    private Color getBiomeColor(Biome biome) {
        return switch (biome) {
            case OCEAN -> Color.rgb(0, 105, 148);
            case TUNDRA -> Color.rgb(182, 182, 107);
            case TAIGA -> Color.rgb(143, 173, 83);
            case GRASSLAND -> Color.rgb(154, 199, 80);
            case TEMPERATE_FOREST -> Color.rgb(84, 108, 47);
            case TROPICAL_RAINFOREST -> Color.rgb(99, 197, 53);
            case DESERT -> Color.rgb(238, 218, 130);
            case SAVANNA -> Color.rgb(155, 190, 82);
            case MEDITERRANEAN -> Color.rgb(164, 189, 100);
            case MOUNTAIN -> Color.rgb(158, 158, 158);
        };
    }


    /**
     * Determines the temperature color based on the temperature value.
     * Maps temperatures from -30°C to 40°C to a gradient from blue to red.
     * @param temp The temperature value in degrees Celsius.
     * @return The color representing the temperature.
     */
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


    /**
     * Determines the humidity color based on the humidity value.
     * @param humidity The humidity value (0.0 to 1.0).
     * @return The color representing the humidity, where higher humidity is darker.
     */
    private Color getHumidityColor(double humidity) {
        int val = (int)(humidity * 255);
        return Color.rgb(255 - val, 255 - val, 255);
    }


    /**
     * Saves the current map canvas as a PNG image file.
     * Opens a file chooser dialog for the user to select the save location.
     * Displays a success or error alert based on the outcome.
     */
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


    /**
     * The main entry point for the JavaFX application.
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}