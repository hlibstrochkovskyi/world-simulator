package org.example;

import java.util.Random;
import java.util.PriorityQueue;
import javafx.scene.paint.Color;

class World {
    int size;
    double seaLevel;
    double worldScale;
    int worldOctaves;
    double[][] elevation;
    double[][] temperature;
    double[][] humidity;
    Biome[][] biomes;
    int[][] stateID;
    Color[] stateColors;
    String[] stateNames;


    /**
     * Constructs a new World instance with the specified parameters.
     * Initializes the world grid and its properties.
     * @param size The size of the world grid (size x size).
     * @param seaLevel The sea level threshold (0.0 to 1.0).
     * @param worldScale The scale of the world for noise generation.
     * @param worldOctaves The number of octaves for noise generation.
     * @param stateNames A pre-generated array of state names, or null if states are not generated.
     */
    public World(int size, double seaLevel, double worldScale, int worldOctaves, String[] stateNames) {
        this.size = size;
        this.seaLevel = seaLevel;
        this.worldScale = worldScale;
        this.worldOctaves = worldOctaves;
        this.elevation = new double[size][size];
        this.temperature = new double[size][size];
        this.humidity = new double[size][size];
        this.biomes = new Biome[size][size];
        this.stateID = new int[size][size];
        this.stateNames = stateNames;
    }


    /**
     * Helper class for the state generation algorithm (Dijkstra's)
     * Implements Comparable to be used in a PriorityQueue.
     */
    private static class StateCell implements Comparable<StateCell> {
        int x, y, ownerID;
        double cost;

        /**
         * Constructs a new StateCell instance.
         * @param x The x-coordinate of the cell.
         * @param y The y-coordinate of the cell.
         * @param ownerID The ID of the state owning this cell.
         * @param cost The movement cost to reach this cell.
         */
        public StateCell(int x, int y, int ownerID, double cost) {
            this.x = x;
            this.y = y;
            this.ownerID = ownerID;
            this.cost = cost;
        }


        /**
         * Compares this StateCell with another based on movement cost.
         * @param other The other StateCell to compare to.
         * @return A negative value if this cell has a lower cost, zero if equal, or a positive value otherwise.
         */
        @Override
        public int compareTo(StateCell other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    /**
     * Generates the world by calculating elevation, temperature, humidity, and biomes.
     * Optionally generates states if the `generateStates` parameter is true.
     * @param generateStates Whether to generate states in the world.
     * @param numStates The number of states to generate if generateStates is true.
     */
    public void generate(boolean generateStates, int numStates) {
        Random rand = new Random();
        SimplexNoise elevationNoise = new SimplexNoise(rand.nextLong());
        SimplexNoise tempNoise = new SimplexNoise(rand.nextLong());
        SimplexNoise humidNoise = new SimplexNoise(rand.nextLong());

        double baseFrequency;
        double amplitude;
        double maxValue;

        baseFrequency = this.worldScale;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                double nx_map = x / (double) size; // 0 to 1
                double ny_map = y / (double) size; // 0 to 1


                double lon = nx_map * 2 * Math.PI;
                double lat = ny_map * Math.PI - (Math.PI / 2.0);

                double x_coord = Math.cos(lat) * Math.cos(lon);
                double y_coord = Math.cos(lat) * Math.sin(lon);
                double z_coord = Math.sin(lat);

                double e = 0;
                amplitude = 1.0;
                maxValue = 0;

                for (int i = 0; i < this.worldOctaves; i++) {
                    double freq = Math.pow(2, i) * baseFrequency;
                    e += amplitude * elevationNoise.noise(
                            x_coord * freq,
                            y_coord * freq,
                            z_coord * freq
                    );
                    maxValue += amplitude;
                    amplitude *= 0.5;
                }
                elevation[x][y] = (e / maxValue + 1) / 2;
            }
        }


        baseFrequency = 0.5;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                double nx_map = x / (double) size;
                double ny_map = y / (double) size;
                double lon = nx_map * 2 * Math.PI;
                double lat = ny_map * Math.PI - (Math.PI / 2.0);
                double x_coord = Math.cos(lat) * Math.cos(lon);
                double y_coord = Math.cos(lat) * Math.sin(lon);
                double z_coord = Math.sin(lat);

                double lat_normalized = Math.abs(y / (double) size - 0.5) * 2;
                double baseTemp = 30 - lat_normalized * 60;

                double altitudeMod = 0;
                if (elevation[x][y] > seaLevel) {

                    altitudeMod = (elevation[x][y] - seaLevel) * (1.0 / (1.0 - seaLevel)) * 8000 * -0.0065;
                }


                double noise = tempNoise.noise(
                        x_coord * baseFrequency,
                        y_coord * baseFrequency,
                        z_coord * baseFrequency
                ) * 10;

                temperature[x][y] = baseTemp + altitudeMod + noise;
            }
        }


        baseFrequency = 0.8;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                double nx_map = x / (double) size;
                double ny_map = y / (double) size;
                double lon = nx_map * 2 * Math.PI;
                double lat = ny_map * Math.PI - (Math.PI / 2.0);
                double x_coord = Math.cos(lat) * Math.cos(lon);
                double y_coord = Math.cos(lat) * Math.sin(lon);
                double z_coord = Math.sin(lat);

                if (elevation[x][y] < seaLevel) {
                    humidity[x][y] = 1.0;
                } else {


                    double baseHumidity = (humidNoise.noise(
                            x_coord * baseFrequency,
                            y_coord * baseFrequency,
                            z_coord * baseFrequency
                    ) + 1) / 2.0;

                    double tempMod = (temperature[x][y] + 30) / 70.0; // 0-1

                    humidity[x][y] = Math.max(0, Math.min(1, baseHumidity * tempMod));
                }
            }
        }


        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                biomes[x][y] = determineBiome(elevation[x][y], temperature[x][y], humidity[x][y]);
            }
        }



        if (generateStates) {
            runStateGeneration(numStates);
        } else {

            this.stateID = new int[size][size];
            this.stateColors = null;
            this.stateNames = null;
        }

    }


    /**
     * Runs the state generation algorithm using a modified Dijkstra's algorithm.
     * Assigns state ownership to each cell in the world grid.
     * @param numStates The number of states to generate.
     */
    private void runStateGeneration(int numStates) {

        Random rand = new Random();

        this.stateID = new int[size][size];
        this.stateColors = new Color[numStates + 1];
        this.stateColors[0] = Color.TRANSPARENT;

        double[][] totalCost = new double[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                totalCost[y][x] = Double.MAX_VALUE;
            }
        }

        PriorityQueue<StateCell> queue = new PriorityQueue<>();


        for (int i = 1; i <= numStates; i++) {
            int x, y;
            do {
                x = rand.nextInt(size);
                y = rand.nextInt(size);
            } while (elevation[x][y] < seaLevel);

            this.stateID[x][y] = i;
            this.stateColors[i] = Color.rgb(rand.nextInt(200) + 55, rand.nextInt(200) + 55, rand.nextInt(200) + 55);
            totalCost[x][y] = 0;
            queue.add(new StateCell(x, y, i, 0));
        }

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            StateCell current = queue.poll();

            if (current.cost > totalCost[current.x][current.y]) {
                continue;
            }

            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];


                if (ny < 0 || ny >= size) continue;


                nx = (nx + size) % size;


                double moveCost;
                if (elevation[nx][ny] < seaLevel) {

                    moveCost = 250.0;
                } else if (elevation[nx][ny] > 0.75) {
                    moveCost = 10.0;
                } else {
                    moveCost = 1.0;
                }


                double newCost = current.cost + moveCost;

                if (newCost < totalCost[nx][ny]) {
                    totalCost[nx][ny] = newCost;
                    this.stateID[nx][ny] = current.ownerID;
                    queue.add(new StateCell(nx, ny, current.ownerID, newCost));
                }
            }
        }



    }


    /**
     * Determines the biome of a cell based on elevation, temperature, and humidity.
     * @param elev The elevation value of the cell (0.0 to 1.0).
     * @param temp The temperature value of the cell in degrees Celsius.
     * @param humid The humidity value of the cell (0.0 to 1.0).
     * @return The biome type for the cell.
     */
    private Biome determineBiome(double elev, double temp, double humid) {
        if (elev < seaLevel) return Biome.OCEAN;
        if (elev > 0.75) return Biome.MOUNTAIN;

        if (temp < -10) return Biome.TUNDRA;
        if (temp < 0) return Biome.TAIGA;

        if (temp < 15) {
            return humid > 0.5 ? Biome.TEMPERATE_FOREST : Biome.GRASSLAND;
        } else if (temp < 25) {
            if (humid > 0.6) return Biome.TEMPERATE_FOREST;
            if (humid > 0.3) return Biome.MEDITERRANEAN;
            return Biome.GRASSLAND;
        } else {
            if (humid > 0.7) return Biome.TROPICAL_RAINFOREST;
            if (humid > 0.3) return Biome.SAVANNA;
            return Biome.DESERT;
        }
    }
}