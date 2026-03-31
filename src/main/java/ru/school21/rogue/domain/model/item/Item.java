package ru.school21.rogue.domain.model.item;

public sealed abstract class Item permits Food, Weapon, Treasure, Scroll, Elixir {
    private final String name;

    protected Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
