package ru.school21.rogue.domain.model.world;

import ru.school21.rogue.domain.model.common.Position;

public record Room(Position topLeft, int width, int height, boolean startRoom, boolean exitRoom) {
}
