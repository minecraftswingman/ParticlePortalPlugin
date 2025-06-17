package com.pessulum.particleportals.PortalSpawner;

public enum CustomPortals {
    CUBE("Cube"),
    SQUARE("Square"),
    HEXAGON("Hexagon"),
    TRIANGLE("Triangle"),
    TORUS("Torus"),
    SPHERE("Sphere");

    private final String shapeName;

    CustomPortals(String shapeName) {
        this.shapeName = shapeName;
    }

    public String getShapeName() {
        return shapeName;
    }

    public static CustomPortals fromShapeName(String shapeName) {
        for (CustomPortals portal : CustomPortals.values()) {
            if (portal.getShapeName().equalsIgnoreCase(shapeName)) {
                return portal;
            }
        }
        return null;
    }

}
