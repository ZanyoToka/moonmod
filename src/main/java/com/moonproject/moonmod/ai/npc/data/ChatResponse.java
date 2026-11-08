package com.moonproject.moonmod.ai.npc.data;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ChatResponse {
    @SerializedName("speech")
    private String speech;

    @SerializedName("mood")
    private int mood = 65;

    @SerializedName("affection")
    private int affection = 20;

    @SerializedName("emotion")
    private String emotion = "happy";

    @SerializedName("actions")
    private List<Action> actions = new ArrayList<>();

    public ChatResponse() {}

    public ChatResponse(String speech, int mood, int affection, String emotion, List<Action> actions) {
        this.speech = speech;
        this.mood = mood;
        this.affection = affection;
        this.emotion = emotion != null ? emotion : "happy";
        this.actions = actions != null ? actions : new ArrayList<>();
    }

    public String getSpeech() {
        return speech != null ? speech : "";
    }

    public void setSpeech(String speech) {
        this.speech = speech;
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        this.mood = mood;
    }

    public int getAffection() {
        return affection;
    }

    public void setAffection(int affection) {
        this.affection = affection;
    }

    public String getEmotion() {
        return emotion != null ? emotion : "happy";
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public List<Action> getActions() {
        return actions != null ? actions : new ArrayList<>();
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public static class Action {
        @SerializedName("type")
        private String type;

        @SerializedName("item")
        private String item;

        @SerializedName("count")
        private int count = 1;

        @SerializedName("sound")
        private String sound;

        @SerializedName("emotion")
        private String emotion;

        @SerializedName("anim")
        private String anim;

        @SerializedName("duration")
        private int duration = 50;

        public Action() {}

        public Action(String type, String item, int count, String sound, String emotion, String anim, int duration) {
            this.type = type;
            this.item = item;
            this.count = count;
            this.sound = sound;
            this.emotion = emotion;
            this.anim = anim;
            this.duration = duration;
        }

        public String getType() {
            return type != null ? type : "";
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public int getCount() {
            return count <= 0 ? 1 : count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getSound() {
            return sound;
        }

        public void setSound(String sound) {
            this.sound = sound;
        }

        public String getEmotion() {
            return emotion;
        }

        public void setEmotion(String emotion) {
            this.emotion = emotion;
        }

        public String getAnim() {
            return anim;
        }

        public void setAnim(String anim) {
            this.anim = anim;
        }

        public int getDuration() {
            return duration <= 0 ? 50 : duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
}
