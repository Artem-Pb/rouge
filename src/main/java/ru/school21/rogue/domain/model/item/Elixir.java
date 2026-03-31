package ru.school21.rogue.domain.model.item;

public final class Elixir extends Item {
    private final int bonusAgility, bonusMaxHealth, bonusStrength, time;

    public Elixir( int bonusAgility, int bonusMaxHealth, int bonusStrength, int time) {
        super("Elixir");
        this.bonusAgility = bonusAgility;
        this.bonusMaxHealth = bonusMaxHealth;
        this.bonusStrength = bonusStrength;
        this.time = time;
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

    public long getTime() {
        return time;
    }
}
