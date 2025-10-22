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
    int[][] stateID; // Stores the ID (1, 2, 3...) of the state owning this cell
    Color[] stateColors; // Stores a random color for each state ID

    public World(int size, double seaLevel, double worldScale, int worldOctaves) {
        this.size = size;
        this.seaLevel = seaLevel;
        this.worldScale = worldScale;
        this.worldOctaves = worldOctaves;
        this.elevation = new double[size][size];
        this.temperature = new double[size][size];
        this.humidity = new double[size][size];
        this.biomes = new Biome[size][size];
        this.stateID = new int[size][size];
    }


    /**
     * Helper class for the state generation algorithm (Dijkstra's)
     * Implements Comparable to be used in a PriorityQueue.
     */
    private static class StateCell implements Comparable<StateCell> {
        int x, y, ownerID;
        double cost;

        public StateCell(int x, int y, int ownerID, double cost) {
            this.x = x;
            this.y = y;
            this.ownerID = ownerID;
            this.cost = cost;
        }

        @Override
        public int compareTo(StateCell other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    public void generate(boolean generateStates) {
        Random rand = new Random();
        SimplexNoise elevationNoise = new SimplexNoise(rand.nextLong());
        SimplexNoise tempNoise = new SimplexNoise(rand.nextLong());
        SimplexNoise humidNoise = new SimplexNoise(rand.nextLong());

        // --- New 3D spherical mapping ---
        double baseFrequency;
        double amplitude;
        double maxValue;

        // --- 1. Generate Elevation ---
        baseFrequency = this.worldScale;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                // Convert (x, y) to 3D coordinates on a sphere
                double nx_map = x / (double) size; // 0 to 1
                double ny_map = y / (double) size; // 0 to 1

                // Convert 2D (longitude, latitude) to 3D (x, y, z)
                // nx_map -> Longitude (0 to 2*PI)
                // ny_map -> Latitude (-PI/2 to PI/2)
                double lon = nx_map * 2 * Math.PI;
                double lat = ny_map * Math.PI - (Math.PI / 2.0);

                // Coordinates on a unit sphere
                double x_coord = Math.cos(lat) * Math.cos(lon);
                double y_coord = Math.cos(lat) * Math.sin(lon);
                double z_coord = Math.sin(lat);

                // Generate noise with octaves using 3D coordinates
                double e = 0;
                amplitude = 1.0;
                maxValue = 0;

                for (int i = 0; i < this.worldOctaves; i++) { // 5 octaves
                    double freq = Math.pow(2, i) * baseFrequency;
                    e += amplitude * elevationNoise.noise(
                            x_coord * freq,
                            y_coord * freq,
                            z_coord * freq
                    );
                    maxValue += amplitude;
                    amplitude *= 0.5;
                }
                elevation[x][y] = (e / maxValue + 1) / 2; // Normalize 0-1
            }
        }

        // --- 2. Generate Temperature ---
        baseFrequency = 0.5; // Lower frequency for smoother temperatures
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                // Get 3D coordinates again
                double nx_map = x / (double) size;
                double ny_map = y / (double) size;
                double lon = nx_map * 2 * Math.PI;
                double lat = ny_map * Math.PI - (Math.PI / 2.0);
                double x_coord = Math.cos(lat) * Math.cos(lon);
                double y_coord = Math.cos(lat) * Math.sin(lon);
                double z_coord = Math.sin(lat);

                // 1. Base temperature from latitude
                double lat_normalized = Math.abs(y / (double) size - 0.5) * 2; // 0 at equator, 1 at poles
                double baseTemp = 30 - lat_normalized * 60; // 30°C at equator, -30°C at poles

                // 2. Altitude modifier
                double altitudeMod = 0;
                if (elevation[x][y] > seaLevel) {
                    // -6.5°C per 1000m (assuming 1.0 elevation = 8000m)
                    altitudeMod = (elevation[x][y] - seaLevel) * (1.0 / (1.0 - seaLevel)) * 8000 * -0.0065;
                }

                // 3. 3D noise for variation
                double noise = tempNoise.noise(
                        x_coord * baseFrequency,
                        y_coord * baseFrequency,
                        z_coord * baseFrequency
                ) * 10; // Variation +/- 10 degrees

                temperature[x][y] = baseTemp + altitudeMod + noise;
            }
        }

        // --- 3. Generate Humidity ---
        baseFrequency = 0.8; // Medium frequency for humidity
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                // Get 3D coordinates again
                double nx_map = x / (double) size;
                double ny_map = y / (double) size;
                double lon = nx_map * 2 * Math.PI;
                double lat = ny_map * Math.PI - (Math.PI / 2.0);
                double x_coord = Math.cos(lat) * Math.cos(lon);
                double y_coord = Math.cos(lat) * Math.sin(lon);
                double z_coord = Math.sin(lat);

                if (elevation[x][y] < seaLevel) {
                    humidity[x][y] = 1.0; // Ocean = 100% humidity
                } else {
                    // 1. Simplified "distance to ocean" (replaced with 3D noise)
                    // The old radius search was slow and inaccurate
                    // We use 3D noise to create "humidity systems"

                    // Noise 0-1
                    double baseHumidity = (humidNoise.noise(
                            x_coord * baseFrequency,
                            y_coord * baseFrequency,
                            z_coord * baseFrequency
                    ) + 1) / 2.0;

                    // 2. Temperature modifier (warm air holds more moisture)
                    double tempMod = (temperature[x][y] + 30) / 70.0; // 0-1

                    humidity[x][y] = Math.max(0, Math.min(1, baseHumidity * tempMod));
                }
            }
        }

        // --- 4. Determine Biomes  ---
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                biomes[x][y] = determineBiome(elevation[x][y], temperature[x][y], humidity[x][y]);
            }
        }


        // --- 5. Generate States (if requested) ---
        if (generateStates) {
            runStateGeneration();
        } else {
            // Clear any old state data
            this.stateID = new int[size][size];
        }

    }


    private void runStateGeneration() {
        int numStates = 50; // How many states to create
        Random rand = new Random();

        // 1. Initialize data structures
        this.stateID = new int[size][size];
        this.stateColors = new Color[numStates + 1]; // +1 because IDs start at 1
        this.stateColors[0] = Color.TRANSPARENT; // ID 0 is "ocean/unclaimed"

        double[][] totalCost = new double[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                totalCost[y][x] = Double.MAX_VALUE;
            }
        }

        PriorityQueue<StateCell> queue = new PriorityQueue<>();

        // 2. Select Seeds (Capitals)
        for (int i = 1; i <= numStates; i++) {
            int x, y;
            // Find a random spot on land
            do {
                x = rand.nextInt(size);
                y = rand.nextInt(size);
            } while (elevation[x][y] < seaLevel);

            this.stateID[x][y] = i; // Claim this cell
            this.stateColors[i] = Color.rgb(rand.nextInt(200) + 55, rand.nextInt(200) + 55, rand.nextInt(200) + 55);
            totalCost[x][y] = 0;
            queue.add(new StateCell(x, y, i, 0));
        }

        // 3. Run Multi-Source Dijkstra (The Growth)
        int[] dx = {0, 0, 1, -1}; // 4-way neighbors
        int[] dy = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            StateCell current = queue.poll();

            // If this cell has already been processed with a lower cost, skip it
            if (current.cost > totalCost[current.x][current.y]) {
                continue;
            }

            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                // Handle Y bounds (no wrapping)
                if (ny < 0 || ny >= size) continue;

                // Handle X wrapping (seamless map)
                nx = (nx + size) % size;

                // --- (NEW) Check if the neighbor is ocean. If so, stop. ---
                if (elevation[nx][ny] < seaLevel) {
                    continue; // Cannot expand into the ocean
                }

                // --- This is our simplified Cost Logic ---
                double moveCost;
                // (We already know it's not ocean, so we can remove that check)
                if (elevation[nx][ny] > 0.75) {
                    moveCost = 10.0; // Mountains are expensive
                } else {
                    moveCost = 1.0;  // Plains are cheap
                }
                // ---

                double newCost = current.cost + moveCost;

                if (newCost < totalCost[nx][ny]) {
                    totalCost[nx][ny] = newCost;
                    this.stateID[nx][ny] = current.ownerID;
                    queue.add(new StateCell(nx, ny, current.ownerID, newCost));
                }
            }
        }
    }


    private Biome determineBiome(double elev, double temp, double humid) {
        if (elev < seaLevel) return Biome.OCEAN;
        if (elev > 0.75) return Biome.MOUNTAIN; // High altitude mountains

        // Temperature-humidity matrix
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