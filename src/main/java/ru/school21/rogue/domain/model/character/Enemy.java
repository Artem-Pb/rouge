package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.Position;
public sealed class Enemy extends Actor permits Zombie, Vampire, Ogr, Ghost, SnakeMage {
    private final EnemyType type;
    private final int hostility;

    public Enemy(int health, int agility, int strength, Position position, EnemyType type, int hostility) {
        super(health, agility, strength, position);
        this.type = type;
        this.hostility = hostility;
    }

    @Override
    public int attack() {
        return 0;
    }

    @Override
    public void takeDamage(int damage) {

    }

    public EnemyType getType() {
        return type;
    }

    public int getHostility() {
        return hostility;
    }
}
