package ru.school21.rogue.domain.ports;

import ru.school21.rogue.domain.model.session.GameSession;

public interface SaveRepository {
    void save(GameSession gameSession);
    GameSession load();
}
