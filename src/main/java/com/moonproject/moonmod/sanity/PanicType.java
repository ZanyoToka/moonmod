package com.moonproject.moonmod.sanity;

/** 恐慌类型（原著：战斗单位理智过低时的三种反应）。 */
public enum PanicType {
    NONE("无"),
    WRATH("愤怒"),
    DESPAIR("绝望"),
    PANIC("恐慌");

    public final String displayName;

    PanicType(String displayName) {
        this.displayName = displayName;
    }
}