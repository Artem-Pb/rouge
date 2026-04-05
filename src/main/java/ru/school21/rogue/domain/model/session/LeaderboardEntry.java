package ru.school21.rogue.domain.model.session;

import java.time.LocalDateTime;

public record LeaderboardEntry(String namePlayer, int treasure, int kills, int level, int foodEaten, int scrollsRead,
                               int elixirsUsed, int hitsDealt, int hitsMissed, LocalDateTime endTime) {
}
