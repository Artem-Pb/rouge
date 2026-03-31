package ru.school21.rogue.domain.model.item;

public final class Scroll extends Item {
    private final int bonusAgility, bonusMaxHealth, bonusStrength;

    public Scroll(int bonusStrength, int bonusAgility, int bonusHealth) {
        super("Scroll");
        this.bonusStrength = bonusStrength;
        this.bonusAgility = bonusAgility;
        this.bonusMaxHealth = bonusHealth;
    }

    public int getBonusAgility() {
        return bonusAgility;
    }

    public int getBonusMaxHealth() {
        return bonusMaxHealth;
    }

    public int getBonusStrength() {
        return bonusStrength;
    }
}
