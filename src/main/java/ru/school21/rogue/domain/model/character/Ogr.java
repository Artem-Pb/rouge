package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.Position;

public final class Ogr extends Enemy {
    private boolean isResting = false;

    public Ogr(int health, int agility, int strength, Position position, int hostility) {
        super(health, agility, strength, position, EnemyType.OGR, hostility);
    }


    public boolean isResting() {
        return isResting;
    }

    public void setResting(boolean resting) {
        isResting = resting;
    }
}
