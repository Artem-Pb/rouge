package ru.school21.rogue.domain.model.inventory;

import ru.school21.rogue.domain.model.item.*;

import java.util.*;

public class Inventory {
    private final Map<Class<? extends Item>, List<Item>> inventory = new HashMap<>();
    private final Map<Class<? extends Item>, Integer> limits = new HashMap<>();

    public Inventory() {
        limits.put(Food.class, 9);
        limits.put(Weapon.class, 9);
        limits.put(Scroll.class, 9);
        limits.put(Elixir.class, 9);
    }

    public boolean canAdd(Item item) {
        var type = item.getClass();
        var items = inventory.getOrDefault(type, Collections.emptyList());
        int limit = limits.getOrDefault(type, 0);

        if (item instanceof Treasure) {
            return true;
        } else {
            return items.size() < limit;
        }
    }

    public void add(Item item) {
        var type = item.getClass();
        var items = this.inventory.computeIfAbsent(type, k -> new ArrayList<>());
        items.add(item);
    }

    public void remove(Item item) {
        var type = item.getClass();
        var items = this.inventory.get(type);
        if (items != null) items.remove(item);
    }

    public List<Item> getByType(Class<? extends Item> type) {
        return this.inventory.getOrDefault(type, Collections.emptyList());
    }
}
