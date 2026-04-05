package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.Damageable;
import ru.school21.rogue.domain.model.common.Position;

public abstract class Actor implements Damageable {
    private int health, agility, strength;
    private Position position;

    protected Actor(int health, int agility, int strength, Position position) {
        this.health = health;
        this.agility = agility;
        this.strength = strength;
        this.position = position;
    }

    abstract public int attack();

    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    @Override
    public boolean isAlive() {
        return this.health > 0;
    }

    public int getHealth() {
        return health;
    }

    public int getAgility() {
        return agility;
    }

    public int getStrength() {
        return strength;
    }

    public Position getPosition() {
        return position;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
