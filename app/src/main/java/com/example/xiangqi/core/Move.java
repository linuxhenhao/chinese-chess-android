package com.example.xiangqi.core;

public class Move {
    public int startX, startY;
    public int endX, endY;
    public Piece capturedPiece;

    public Move(int startX, int startY, int endX, int endY) {
        this(startX, startY, endX, endY, null);
    }

    public Move(int startX, int startY, int endX, int endY, Piece capturedPiece) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.capturedPiece = capturedPiece;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d] -> [%d,%d]", startX, startY, endX, endY);
    }
}
