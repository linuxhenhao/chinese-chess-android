package com.example.xiangqi.core;

public enum PieceType {
    SHUAI("Shuai", "Jiang"), // General
    SHI("Shi", "Shi"), // Advisor
    XIANG("Xiang", "Xiang"), // Elephant
    MA("Ma", "Ma"), // Horse
    JU("Ju", "Ju"), // Chariot
    PAO("Pao", "Pao"), // Cannon
    BING("Bing", "Zu"); // Soldier

    private final String redName;
    private final String blackName;

    PieceType(String redName, String blackName) {
        this.redName = redName;
        this.blackName = blackName;
    }

    public String getName(boolean isRed) {
        return isRed ? redName : blackName;
    }
}
