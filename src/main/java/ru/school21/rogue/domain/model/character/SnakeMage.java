package ru.school21.rogue.domain.model.character;

import ru.school21.rogue.domain.model.common.AttackEffect;
import ru.school21.rogue.domain.model.common.Position;

public final class SnakeMage extends Enemy implements AttackEffect {
    public SnakeMage(int health, int agility, int strength, Position position, int hostility) {
        super(health, agility, strength, position, EnemyType.SNAKE_MAGE, hostility);
    }

    @Override
    public void applyEffect(Actor target) {
        if (target instanceof Player player) {
            player.setSleeping(true);
        }
    }
}
