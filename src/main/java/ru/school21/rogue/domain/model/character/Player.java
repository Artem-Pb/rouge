package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.Position;
import ru.school21.rogue.domain.model.inventory.Inventory;
import ru.school21.rogue.domain.model.item.Weapon;

public class Player extends Actor {
    private int maxHealth;
    private boolean isSleeping;
    private Weapon weapon;
    private Inventory inventory;

    public Player(int health, int agility, int strength, Position position, int maxHealth, Weapon weapon, Inventory inventory) {
        super(health, agility, strength, position);
        this.maxHealth = maxHealth;
        this.weapon = weapon;
        this.inventory = inventory;
    }

    @Override
    public int attack() {
        return 0;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isSleeping() {
        return isSleeping;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setSleeping(boolean sleeping) {
        isSleeping = sleeping;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
