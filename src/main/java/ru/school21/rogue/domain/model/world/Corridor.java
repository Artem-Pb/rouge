package ru.school21.rogue.domain.model.world;

import ru.school21.rogue.domain.model.common.Position;

import java.util.List;

public record Corridor(int fromIndex, int toIndex, List<Position> path) {
}
