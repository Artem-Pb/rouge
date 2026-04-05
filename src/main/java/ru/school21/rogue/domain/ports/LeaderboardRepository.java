package ru.school21.rogue.domain.ports;

import ru.school21.rogue.domain.model.session.LeaderboardEntry;

import java.util.List;

public interface LeaderboardRepository {
    void save(LeaderboardEntry leaderboardEntry);
    List<LeaderboardEntry> getAll();
}
