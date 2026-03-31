package ru.school21.rogue.domain.model.item;

public final class Food extends Item {
    private final int healthAmount;

    public Food(int healthAmount) {
        super("Food");
        this.healthAmount = healthAmount;
    }

    public int getHealthAmount() {
        return healthAmount;
    }
}
