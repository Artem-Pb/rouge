package ru.school21.rogue.domain.model.item;

public final class Weapon extends Item {
    private final int bonusStrength;

    public Weapon(int bonusStrength) {
        super("Weapon");
        this.bonusStrength = bonusStrength;
    }

    public int getBonusStrength() {
        return bonusStrength;
    }
}
