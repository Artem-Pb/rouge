package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.Position;

public class Player extends Actor {
    private int maxHealth;
    private boolean isSleeping;
    //private Backpack backpack;

    public Player(int health, int agility, int strength, Position position, int maxHealth /*, Backpack backpack*/) {
        super(health, agility, strength, position);
        this.maxHealth = maxHealth;
        //this.backpack = backpack;
    }

    @Override
    public int attack() {
        return 0;
    }

    @Override
    public void takeDamage(int damage) {

    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isSleeping() {
        return isSleeping;
    }

    public void setSleeping(boolean sleeping) {
        isSleeping = sleeping;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
}
