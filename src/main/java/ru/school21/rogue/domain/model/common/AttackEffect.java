package ru.school21.rogue.domain.model.common;

import ru.school21.rogue.domain.model.character.Actor;

public interface AttackEffect {
    void applyEffect(Actor target);
}
