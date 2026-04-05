package ru.school21.rogue.gameplay.ai;

import ru.school21.rogue.domain.model.character.Enemy;
import ru.school21.rogue.domain.model.common.Position;
import ru.school21.rogue.domain.model.world.Level;

public interface EnemyAI {
    void takeTurn(Enemy enemy, Level level, Position playerPos);
}