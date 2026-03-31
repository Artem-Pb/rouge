package ru.school21.rogue.domain.model.session;

import java.time.LocalDateTime;

public record GameStats(String namePlayer, int level, int treasuresCollected, int kills, LocalDateTime dateTimeOfEnd) {
}
