package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.Position;

public final class Ghost extends Enemy {
    private boolean isInvisible = false;

    public Ghost(int health, int agility, int strength, Position position, int hostility) {
        super(health, agility, strength, position, EnemyType.GHOST, hostility);
    }


    public boolean isInvisible() {
        return isInvisible;
    }

    public void setInvisible(boolean invisible) {
        isInvisible = invisible;
    }
}
