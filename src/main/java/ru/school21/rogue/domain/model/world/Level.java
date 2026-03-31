package ru.school21.rogue.domain.model.world;

import java.util.List;

public record Level (Tile[][] tiles, List<Room> rooms, List<Corridor> corridors, int numberOfLevel) {
}
