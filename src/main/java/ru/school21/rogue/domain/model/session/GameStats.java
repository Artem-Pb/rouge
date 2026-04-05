package ru.school21.rogue.domain.model.session;

public class GameStats {
    private int treasure;
    private int kills;
    private int foodEaten;
    private int elixirUsed;
    private int scrollsRead;
    private int hitsDealt;
    private int hitsMissed;
    private int steps;

    public void addTreasure() {
        this.treasure++;
    }

    public void addKill() {
        this.kills++;
    }

    public void addFood() {
        this.foodEaten++;
    }

    public void addElixir() {
        this.elixirUsed++;
    }

    public void addScroll() {
        this.scrollsRead++;
    }

    public void addHitsDealt() {
        this.hitsDealt++;
    }

    public void addHitsMissed() {
        this.hitsMissed++;
    }

    public void addStep() {
        this.steps++;
    }

    public int getTreasure() {
        return treasure;
    }

    public int getKills() {
        return kills;
    }

    public int getSteps() {
        return steps;
    }

    public int getFoodEaten() {
        return foodEaten;
    }

    public int getElixirUsed() {
        return elixirUsed;
    }

    public int getScrollsRead() {
        return scrollsRead;
    }

    public int getHitsDealt() {
        return hitsDealt;
    }

    public int getHitsMissed() {
        return hitsMissed;
    }
}
