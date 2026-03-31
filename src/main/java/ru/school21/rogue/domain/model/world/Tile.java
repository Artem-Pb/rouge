package ru.school21.rogue.domain.model.world;

public class Tile {
    private final TileType type;
    private boolean isVisible;
    private boolean isExplored;

    public Tile(TileType type) {
        this.type = type;
    }

    public TileType getType() {
        return type;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isExplored() {
        return isExplored;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setExplored(boolean explored) {
        isExplored = explored;
    }
}
