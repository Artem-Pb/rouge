package ru.school21.rogue.domain.model.item;

public final class Treasure extends Item {
    private final int value;

    public Treasure(int value) {
        super("Treasure");
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
