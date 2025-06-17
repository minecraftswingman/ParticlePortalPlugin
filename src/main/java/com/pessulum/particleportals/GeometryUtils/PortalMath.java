package com.pessulum.particleportals.GeometryUtils;

import com.pessulum.particleportals.ParticlePortals;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PortalMath{

    static ParticlePortals plugin;
    static int duration;


    static final double particleDistance = 0.1;
    static final double halfSize = 0.5;

    public static final int MAX_PARTICLES = 1000;
    public static final int MAX_LAYERS = 50;

    public static Vector[][] cachedLocalCubeVectors = new Vector[MAX_LAYERS][MAX_PARTICLES];
    public static Location[][] cachedRotatedCubeLocations = new Location[MAX_LAYERS][MAX_PARTICLES];

    public static List<List<Vector>> cachedSquareOutline = new ArrayList<>();
    public static List<Vector>  cachedRotatedSquareLocations = new ArrayList<>();
    public static List<List<Vector>> cachedSquareFrames = new ArrayList<>();

    public static List<List<Vector>> cachedHexagonOutline = new ArrayList<>();
    public static List<Vector>  cachedRotatedHexagonLocations = new ArrayList<>();
    public static List<List<Vector>> cachedHexagonFrames = new ArrayList<>();

    public static List<List<Vector>> cachedTriangleOutline = new ArrayList<>();
    public static List<Vector>  cachedRotatedTriangleLocations = new ArrayList<>();
    public static List<List<Vector>> cachedTriangleFrames = new ArrayList<>();

    public static List<double[]> cachedTorusResults = new ArrayList<>();
    public static List<double[]> cachedCircleResults = new ArrayList<>();

    public static List<double[]> cachedSphereResults = new ArrayList<>();
    public static List<double[]> cachedSphereBaseLocations = new ArrayList<>();


    public PortalMath(ParticlePortals plugin, int duration){
        PortalMath.plugin = plugin;
        PortalMath.duration = plugin.getConfig().getInt("portal.portal-duration") * 20;

    }


    public static void cacheCubePortalData(Location center, double halfSize, double particleDistance) {
        List<Vector> allVectors = new ArrayList<>();

        for (double t = -halfSize; t <= halfSize; t += particleDistance) {
            double x = t;
            double y = t;
            double z = t;

            allVectors.add(new Vector(-halfSize, y, -halfSize));
            allVectors.add(new Vector(-halfSize, y, halfSize));
            allVectors.add(new Vector(halfSize, y, -halfSize));
            allVectors.add(new Vector(halfSize, y, halfSize));

            allVectors.add(new Vector(x, -halfSize, -halfSize));
            allVectors.add(new Vector(x, -halfSize, halfSize));
            allVectors.add(new Vector(-halfSize, -halfSize, z));
            allVectors.add(new Vector(halfSize, -halfSize, z));

            allVectors.add(new Vector(x, halfSize, -halfSize));
            allVectors.add(new Vector(x, halfSize, halfSize));
            allVectors.add(new Vector(-halfSize, halfSize, z));
            allVectors.add(new Vector(halfSize, halfSize, z));
        }

        new BukkitRunnable() {

            int tick = 0;

            public void run() {
                if (tick++ >= duration * 20) {
                    cancel();
                    return;
                }

                double angleY = Math.toRadians(tick * 2);
                double angleX = Math.toRadians(tick);
                double sinY = Math.sin(angleY), cosY = Math.cos(angleY);
                double sinX = Math.sin(angleX), cosX = Math.cos(angleX);


                int index = 0;
                for (int i = 0; i < MAX_LAYERS && index < allVectors.size(); i++) {
                    for (int j = 0; j < MAX_PARTICLES && index < allVectors.size(); j++) {
                        Vector vec = cachedLocalCubeVectors[i][j] = allVectors.get(index++);

                        double y1 = vec.getY() * cosX - vec.getZ() * sinX;
                        double z1 = vec.getY() * sinX + vec.getZ() * cosX;
                        double x1 = vec.getX();

                        double x2 = x1 * cosY - z1 * sinY;
                        double z2 = x1 * sinY + z1 * cosY;
                        double y2 = y1;

                        cachedRotatedCubeLocations[i][j] = new Location(center.getWorld(), x2, y2, z2);
                    }
                }
            }

        }.runTaskTimer(plugin, 0 ,1L);

    }



    public static void cacheSquarePortalData(int rotatingAngle) {

        cachedSquareFrames.clear();
        cachedRotatedSquareLocations.clear();

        int counter = -1;
        final int points = 4;
        final double[][] coordinates = new double[points][2];
        final double[][] nextAngles = new double[points][2];
        final List<Vector> singleFrame = new ArrayList<>();

        double baseRotation = 0;

        for (double i = 0; i <= duration; i += 0.05) {
            counter++;
            baseRotation += Math.toRadians(rotatingAngle);

            double cosRotation = Math.cos(baseRotation);
            double sinRotation = Math.sin(baseRotation);

            for (int j = 0; j < points; j++) {
                double angle = Math.toRadians(360.0 / points * j + baseRotation);
                coordinates[j][0] = Math.cos(angle);
                coordinates[j][1] = Math.sin(angle);

                double nextAngle = Math.toRadians(360.0 / points * (j + 1) + baseRotation);
                nextAngles[j][0] = Math.cos(nextAngle);
                nextAngles[j][1] = Math.sin(nextAngle);
            }

            List<Vector> frame = new ArrayList<>();
            List<Vector> outline = new ArrayList<>();

            for (double r = 1; r >= 0; r -= 0.23) {
                for (int j = 0; j < points; j++) {
                    double stepSize = 0.18 / r;

                    double x = coordinates[j][0] * r;
                    double z = coordinates[j][1] * r;

                    double rotatedX = x * cosRotation - z * sinRotation;
                    double rotatedZ = x * sinRotation + z * cosRotation;

                    double nextX = nextAngles[j][0] * r;
                    double nextZ = nextAngles[j][1] * r;

                    double rotatedNextX = nextX * cosRotation - nextZ * sinRotation;
                    double rotatedNextZ = nextX * sinRotation + nextZ * cosRotation;

                    double dX = rotatedNextX - rotatedX;
                    double dZ = rotatedNextZ - rotatedZ;
                    double dist = Math.sqrt((dX - rotatedX) * (dX - rotatedX) + (dZ - rotatedZ) * (dZ - rotatedZ)) / r;

                    for (double d = 0; d < (dist - (2.0 - (2 * ((double) points / 10)))); d += stepSize) {
                        Vector vec = new Vector(rotatedX + dX * d, rotatedZ + dZ * d, 0); // Assuming upright Y = vertical

                        if (r == 1) outline.add(vec.clone());
                        else frame.add(vec);

                        if (counter == 0) singleFrame.add(vec.clone());
                    }
                }
            }

            cachedSquareFrames.add(frame);
            cachedSquareOutline.add(outline);

        }

        if (cachedRotatedSquareLocations.isEmpty()) {
            cachedRotatedSquareLocations.addAll(singleFrame);
        }
}

public static void cacheHexagonPortalData(int rotatingAngle) {

    cachedHexagonFrames.clear();
    cachedRotatedHexagonLocations.clear();

    int counter = -1;
    final int points = 5;
    final double[][] coordinates = new double[points][2];
    final double[][] nextAngles = new double[points][2];
    final List<Vector> singleFrame = new ArrayList<>();

    double baseRotation = 0;

    for (double i = 0; i <= duration; i += 0.05) {
        counter++;
        baseRotation += Math.toRadians(rotatingAngle);

        double cosRotation = Math.cos(baseRotation);
        double sinRotation = Math.sin(baseRotation);

        for (int j = 0; j < points; j++) {
            double angle = Math.toRadians(360.0 / points * j + baseRotation);
            coordinates[j][0] = Math.cos(angle);
            coordinates[j][1] = Math.sin(angle);

            double nextAngle = Math.toRadians(360.0 / points * (j + 1) + baseRotation);
            nextAngles[j][0] = Math.cos(nextAngle);
            nextAngles[j][1] = Math.sin(nextAngle);
        }

        List<Vector> frame = new ArrayList<>();
        List<Vector> outline = new ArrayList<>();

        for (double r = 1; r >= 0; r -= 0.23) {
            for (int j = 0; j < points; j++) {
                double stepSize = 0.18 / r;

                double x = coordinates[j][0] * r;
                double z = coordinates[j][1] * r;

                double rotatedX = x * cosRotation - z * sinRotation;
                double rotatedZ = x * sinRotation + z * cosRotation;

                double nextX = nextAngles[j][0] * r;
                double nextZ = nextAngles[j][1] * r;

                double rotatedNextX = nextX * cosRotation - nextZ * sinRotation;
                double rotatedNextZ = nextX * sinRotation + nextZ * cosRotation;

                double dX = rotatedNextX - rotatedX;
                double dZ = rotatedNextZ - rotatedZ;
                double dist = Math.sqrt((dX - rotatedX) * (dX - rotatedX) + (dZ - rotatedZ) * (dZ - rotatedZ)) / r;

                for (double d = 0; d < (dist - (2.0 - (2 * ((double) points / 10)))); d += stepSize) {
                    Vector vec = new Vector(rotatedX + dX * d, rotatedZ + dZ * d, 0); // Assuming upright Y = vertical

                    if (r == 1) outline.add(vec.clone());
                    else frame.add(vec);

                    if (counter == 0) singleFrame.add(vec.clone());
                }
            }
        }

        cachedHexagonFrames.add(frame);
        cachedHexagonOutline.add(outline);

    }

    if (cachedRotatedHexagonLocations.isEmpty()) {
        cachedRotatedHexagonLocations.addAll(singleFrame);
    }
}
    public static void cacheTrianglePortalData(int rotatingAngle) {

    cachedTriangleFrames.clear();
    cachedRotatedTriangleLocations.clear();

    int counter = -1;
    final int points = 3;
    final double[][] coordinates = new double[points][2];
    final double[][] nextAngles = new double[points][2];
    final List<Vector> singleFrame = new ArrayList<>();

    double baseRotation = 0;

    for (double i = 0; i <= duration; i += 0.05) {
        counter++;
        baseRotation += Math.toRadians(rotatingAngle);

        double cosRotation = Math.cos(baseRotation);
        double sinRotation = Math.sin(baseRotation);

        for (int j = 0; j < points; j++) {
            double angle = Math.toRadians(360.0 / points * j + baseRotation);
            coordinates[j][0] = Math.cos(angle);
            coordinates[j][1] = Math.sin(angle);

            double nextAngle = Math.toRadians(360.0 / points * (j + 1) + baseRotation);
            nextAngles[j][0] = Math.cos(nextAngle);
            nextAngles[j][1] = Math.sin(nextAngle);
        }

        List<Vector> frame = new ArrayList<>();
        List<Vector> outline = new ArrayList<>();

        for (double r = 1; r >= 0; r -= 0.23) {
            for (int j = 0; j < points; j++) {
                double stepSize = 0.18 / r;

                double x = coordinates[j][0] * r;
                double z = coordinates[j][1] * r;

                double rotatedX = x * cosRotation - z * sinRotation;
                double rotatedZ = x * sinRotation + z * cosRotation;

                double nextX = nextAngles[j][0] * r;
                double nextZ = nextAngles[j][1] * r;

                double rotatedNextX = nextX * cosRotation - nextZ * sinRotation;
                double rotatedNextZ = nextX * sinRotation + nextZ * cosRotation;

                double dX = rotatedNextX - rotatedX;
                double dZ = rotatedNextZ - rotatedZ;
                double dist = Math.sqrt((dX - rotatedX) * (dX - rotatedX) + (dZ - rotatedZ) * (dZ - rotatedZ)) / r;

                for (double d = 0; d < (dist - (2.0 - (2 * ((double) points / 10)))); d += stepSize) {
                    Vector vec = new Vector(rotatedX + dX * d, rotatedZ + dZ * d, 0); // Assuming upright Y = vertical

                    if (r == 1) outline.add(vec.clone());
                    else frame.add(vec);

                    if (counter == 0) singleFrame.add(vec.clone());
                }
            }
        }

        cachedTriangleFrames.add(frame);
        cachedTriangleOutline.add(outline);

    }

    if (cachedRotatedTriangleLocations.isEmpty()) {
        cachedRotatedTriangleLocations.addAll(singleFrame);
    }
}


public static void cacheTorusPortalData(Location location) {

    Location loc = location.clone();
    List<Location> result = new ArrayList<>();

    final double R = 2;
    final double r = 0.5;
    int i = 0;
    int count = 0;
    for (double v = 0; v <= 2 * Math.PI; v += Math.PI / 12) {
        double v1 = R + r * Math.cos(v);
        for (double u = 0; u <= 2 * Math.PI; u += Math.PI / 12) {
            double x = v1 * Math.cos(u);
            double y = v1 * Math.sin(u);
            double z = r * Math.sin(v);
            int skipRate = 8;
            if (i % skipRate == 0) {
                cachedTorusResults.add(new double[]{x, y, z});
                count++;
            }
            i++;
        }
    }


    int circCount2 = 0;
    int step = 2;
    for (double rad = 1.1; rad >= 0; rad -= 0.1) {
        for (double angle = 0; angle <= Math.PI * 2; angle += Math.PI / 12) {
            double x = Math.cos(angle) * rad;
            double y = Math.sin(angle) * rad;
            if (circCount2 % step == 0) {
                cachedCircleResults.add(new double[]{x, y, 0});
            }
            circCount2++;

        }
        step++;
    }

}

 public static void cacheSpherePortalData(Location location){

    Location loc = location.clone();
    List<Location> result = new ArrayList<>();
    int duration = plugin.getConfig().getInt("portal.portal-duration");

    for (double i = 0; i <= Math.PI; i += Math.PI / 6) {
        double radius = Math.sin(i);
        double y = Math.cos(i);
        for (double a = 0; a < Math.PI * 2; a += Math.PI / 6) {
            double x = Math.cos(a) * radius;
            double z = Math.sin(a) * radius;
            cachedSphereBaseLocations.add(new double[]{x, y, z});
        }
    }
    for (double counter = 0; counter <= duration * 20; counter++) {
     double angle = Math.toRadians(counter * 4);

         for (double[] coord : cachedSphereBaseLocations) {
             double x = coord[0];
             double y = coord[1];
             double z = coord[2];


             double[] rotated = rotateXYZ(x, y, z, angle);

             cachedSphereResults.add(rotated);
         }
     }

    }

private static double[] rotateXYZ(double x, double y, double z, double angle) {
    double cosA = Math.cos(angle);
    double sinA = Math.sin(angle);
    double y1 = y * cosA - z * sinA;
    double z1 = y * sinA + z * cosA;
    double x2 = x * cosA + z1 * sinA;
    double z2 = -x * sinA + z1 * cosA;
    double x3 = x2 * cosA - y1 * sinA;
    double y3 = x2 * sinA + y1 * cosA;

    return new double[]{x3, y3, z2};
}

}
