package ru.school21.rogue.gameplay.movement;

import ru.school21.rogue.domain.model.common.Position;
import ru.school21.rogue.domain.model.world.Level;
import ru.school21.rogue.domain.model.world.Tile;
import ru.school21.rogue.domain.model.world.TileType;

import java.util.*;

public class PathFinder {
    public static Optional<Position> findNextStep(Position from, Position to, Level level) {
        Queue<Position> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Tile[][] tiles = level.tiles();

        queue.add(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            if (current.equals(to)) {
                Position step = to;
                while (!cameFrom.get(step).equals(from)) {
                    step = cameFrom.get(step);
                }
                return Optional.of(step);
            }

            List<Position> neighbor = List.of(
                    current.translate(0, -1),
                    current.translate(0, 1),
                    current.translate(-1, 0),
                    current.translate(1, 0)
            );

            for (Position n : neighbor) {
                int x = n.x();
                int y = n.y();

                if (x >= 0 && x < tiles[0].length && y >= 0 && y < tiles.length
                        && (tiles[y][x].getType() == TileType.FLOOR
                        || tiles[y][x].getType() == TileType.CORRIDOR)
                        && !visited.contains(n)) {
                    queue.add(n);
                    visited.add(n);
                    cameFrom.put(n, current);
                }
            }
        }

        return Optional.empty();
    }
}
