package com.moonproject.moonmod.ai.npc.data;

import com.google.gson.annotations.SerializedName;

public class ChatRequest {
    @SerializedName("player_id")
    private String playerId;

    @SerializedName("player_name")
    private String playerName;

    @SerializedName("npc_id")
    private String npcId;

    @SerializedName("npc_name")
    private String npcName;

    @SerializedName("message")
    private String message;

    @SerializedName("environment")
    private Environment environment;

    public ChatRequest() {}

    public ChatRequest(String playerId, String playerName, String npcId, String npcName, String message, Environment environment) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.npcId = npcId;
        this.npcName = npcName;
        this.message = message;
        this.environment = environment;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getNpcId() {
        return npcId;
    }

    public void setNpcId(String npcId) {
        this.npcId = npcId;
    }

    public String getNpcName() {
        return npcName;
    }

    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public static class Environment {
        @SerializedName("biome")
        private String biome;

        @SerializedName("is_day")
        private boolean isDay;

        @SerializedName("weather")
        private String weather;

        @SerializedName("held_item")
        private String heldItem;

        public Environment() {}

        public Environment(String biome, boolean isDay, String weather, String heldItem) {
            this.biome = biome;
            this.isDay = isDay;
            this.weather = weather;
            this.heldItem = heldItem;
        }

        public String getBiome() {
            return biome;
        }

        public void setBiome(String biome) {
            this.biome = biome;
        }

        public boolean isDay() {
            return isDay;
        }

        public void setDay(boolean day) {
            isDay = day;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getHeldItem() {
            return heldItem;
        }

        public void setHeldItem(String heldItem) {
            this.heldItem = heldItem;
        }
    }
}
