package org.example;

import java.util.Random;

class World {
    int size;
    double seaLevel;
    double worldScale;
    int worldOctaves;
    double[][] elevation;
    double[][] temperature;
    double[][] humidity;
    Biome[][] biomes;

    public World(int size, double seaLevel, double worldScale, int worldOctaves) {
        this.size = size;
        this.seaLevel = seaLevel;
        this.worldScale = worldScale;
        this.worldOctaves = worldOctaves;
        this.elevation = new double[size][size];
        this.temperature = new double[size][size];
        this.humidity = new double[size][size];
        this.biomes = new Biome[size][size];
    }

    public void generate() {
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

        // --- 4. Determine Biomes (no changes) ---
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                biomes[x][y] = determineBiome(elevation[x][y], temperature[x][y], humidity[x][y]);
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