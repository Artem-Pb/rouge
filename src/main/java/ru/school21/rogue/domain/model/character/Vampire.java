package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.AttackEffect;
import ru.school21.rogue.domain.model.common.Position;

public final class Vampire extends Enemy implements AttackEffect {
    public Vampire(int health, int agility, int strength, Position position, int hostility) {
        super(health, agility, strength, position, EnemyType.VAMPIRE, hostility);
    }

    @Override
    public void applyEffect(Actor target) {
        if (target instanceof Player player) {
            player.setMaxHealth(player.getMaxHealth() - super.getStrength());
        }
    }
}
