package org.plambert.oneShot.manager;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Arena {
    private final String name;
    private final List<Location> spawnPoints;
    private Location waitingSpawn;
    private int minPlayers = 2;
    private int maxPlayers = 16;
    private GameManager.GameState state;
    private final List<UUID> players;

    public Arena(String name) {
        this.name = name;
        this.spawnPoints = new ArrayList<>();
        this.state = GameManager.GameState.LOBBY;
        this.players = new ArrayList<>();
    }

    public boolean isReady() {
        return spawnPoints.size() >= 3 && minPlayers >= 1 && waitingSpawn != null;
    }

    public String getName() {
        return name;
    }

    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }

    public Location getWaitingSpawn() {
        return waitingSpawn;
    }

    public void setWaitingSpawn(Location waitingSpawn) {
        this.waitingSpawn = waitingSpawn;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = Math.max(1, minPlayers);
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public GameManager.GameState getState() {
        return state;
    }

    public void setState(GameManager.GameState state) {
        this.state = state;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }
}
