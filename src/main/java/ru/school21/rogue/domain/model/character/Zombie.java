package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.Position;

public final class Zombie extends Enemy {
    public Zombie(int health, int agility, int strength, Position position, int hostility) {
        super(health, agility, strength, position, EnemyType.ZOMBIE, hostility);
    }
}
