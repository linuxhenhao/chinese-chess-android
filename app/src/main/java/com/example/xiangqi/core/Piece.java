package com.example.xiangqi.core;

public class Piece {
    public final PieceType type;
    public final boolean isRed;

    public Piece(PieceType type, boolean isRed) {
        this.type = type;
        this.isRed = isRed;
    }

    public String getName() {
        return type.getName(isRed);
    }
}
