package com.example.xiangqi.core;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final int COLS = 9;
    public static final int ROWS = 10;
    private Piece[][] pieces;
    private List<Move> moveHistory;

    public Board() {
        pieces = new Piece[COLS][ROWS];
        moveHistory = new ArrayList<>();
        initBoard();
    }

    public Board clone() {
        Board newBoard = new Board(); // This re-initializes, which is wasteful, but let's clear it
        newBoard.pieces = new Piece[COLS][ROWS];
        newBoard.moveHistory = new ArrayList<>(this.moveHistory);
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (this.pieces[x][y] != null) {
                    newBoard.pieces[x][y] = new Piece(this.pieces[x][y].type, this.pieces[x][y].isRed);
                }
            }
        }
        return newBoard;
    }

    private void initBoard() {
        // Red is usually at the bottom (y=9), Black at top (y=0)

        // Black pieces
        pieces[0][0] = new Piece(PieceType.JU, false);
        pieces[1][0] = new Piece(PieceType.MA, false);
        pieces[2][0] = new Piece(PieceType.XIANG, false);
        pieces[3][0] = new Piece(PieceType.SHI, false);
        pieces[4][0] = new Piece(PieceType.SHUAI, false);
        pieces[5][0] = new Piece(PieceType.SHI, false);
        pieces[6][0] = new Piece(PieceType.XIANG, false);
        pieces[7][0] = new Piece(PieceType.MA, false);
        pieces[8][0] = new Piece(PieceType.JU, false);
        pieces[1][2] = new Piece(PieceType.PAO, false);
        pieces[7][2] = new Piece(PieceType.PAO, false);
        for (int i = 0; i < 5; i++) {
            pieces[i * 2][3] = new Piece(PieceType.BING, false);
        }

        // Red pieces
        pieces[0][9] = new Piece(PieceType.JU, true);
        pieces[1][9] = new Piece(PieceType.MA, true);
        pieces[2][9] = new Piece(PieceType.XIANG, true);
        pieces[3][9] = new Piece(PieceType.SHI, true);
        pieces[4][9] = new Piece(PieceType.SHUAI, true);
        pieces[5][9] = new Piece(PieceType.SHI, true);
        pieces[6][9] = new Piece(PieceType.XIANG, true);
        pieces[7][9] = new Piece(PieceType.MA, true);
        pieces[8][9] = new Piece(PieceType.JU, true);
        pieces[1][7] = new Piece(PieceType.PAO, true);
        pieces[7][7] = new Piece(PieceType.PAO, true);
        for (int i = 0; i < 5; i++) {
            pieces[i * 2][6] = new Piece(PieceType.BING, true);
        }
    }

    public Piece getPiece(int x, int y) {
        if (x < 0 || x >= COLS || y < 0 || y >= ROWS)
            return null;
        return pieces[x][y];
    }

    public boolean executeMove(Move move) {
        if (move == null)
            return false;

        move.capturedPiece = pieces[move.endX][move.endY];
        pieces[move.endX][move.endY] = pieces[move.startX][move.startY];
        pieces[move.startX][move.startY] = null;
        moveHistory.add(move);
        return true;
    }

    public void undoMove() {
        if (moveHistory.isEmpty())
            return;
        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        pieces[lastMove.startX][lastMove.startY] = pieces[lastMove.endX][lastMove.endY];
        pieces[lastMove.endX][lastMove.endY] = lastMove.capturedPiece; // Restore captured pieces
    }

    public List<Move> generateLegalMoves(boolean isRedTurn) {
        List<Move> moves = new ArrayList<>();
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                Piece p = pieces[x][y];
                if (p != null && p.isRed == isRedTurn) {
                    moves.addAll(getMovesForPiece(x, y));
                }
            }
        }

        // Filter out moves that leave own general in check
        // List<Move> validMoves = new ArrayList<>();
        // for (Move m : moves) {
        // executeMove(m);
        // if (!isGeneralInCheck(isRedTurn)) {
        // validMoves.add(m);
        // }
        // undoMove();
        // }
        // return validMoves;
        // Optimization: For now returning all pseudo-legal moves.
        // Real implementation must check for General safety.
        // Let's implement the safety check in getMovesForPiece or post-processing?
        // Post-processing is safer.

        List<Move> safeMoves = new ArrayList<>();
        for (Move m : moves) {
            executeMove(m);
            if (!isGeneralInCheck(isRedTurn)) {
                safeMoves.add(m);
            }
            undoMove();
        }

        return safeMoves;
    }

    private boolean isGeneralInCheck(boolean isRed) {
        // Find General
        int gx = -1, gy = -1;
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                Piece p = pieces[x][y];
                if (p != null && p.isRed == isRed && p.type == PieceType.SHUAI) {
                    gx = x;
                    gy = y;
                    break;
                }
            }
        }
        // General not found (should not happen in valid game)
        if (gx == -1)
            return true;

        // Check if any enemy piece attacks (gx, gy)
        // Optimization: Invert logic, check if General is attacked by specific piece
        // types from their directions
        // But for simplicity, let's iterate all enemy pieces.
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                Piece p = pieces[x][y];
                if (p != null && p.isRed != isRed) {
                    // Check if this piece attacks gx, gy
                    if (canAttack(x, y, gx, gy)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canAttack(int sx, int sy, int ex, int ey) {
        // Simplified move check, ignoring "leaving general in check" since we are just
        // checking attack
        return isValidPseudoMove(sx, sy, ex, ey);
    }

    private List<Move> getMovesForPiece(int x, int y) {
        List<Move> moves = new ArrayList<>();
        Piece p = pieces[x][y];
        if (p == null)
            return moves;

        // Iterate over all possible target squares (optimization possible based on
        // piece type)
        // Or better, logic per piece type
        switch (p.type) {
            case SHUAI: // General
                addMoveIfValid(x, y, x + 1, y, moves);
                addMoveIfValid(x, y, x - 1, y, moves);
                addMoveIfValid(x, y, x, y + 1, moves);
                addMoveIfValid(x, y, x, y - 1, moves);
                // Flying General rule? Not strictly standard in all amateur engines but let's
                // check basic movement first
                // Flying general check is usually processed during move validation or check
                // status
                break;
            case SHI: // Advisor
                addMoveIfValid(x, y, x + 1, y + 1, moves);
                addMoveIfValid(x, y, x + 1, y - 1, moves);
                addMoveIfValid(x, y, x - 1, y + 1, moves);
                addMoveIfValid(x, y, x - 1, y - 1, moves);
                break;
            case XIANG: // Elephant
                addMoveIfValid(x, y, x + 2, y + 2, moves);
                addMoveIfValid(x, y, x + 2, y - 2, moves);
                addMoveIfValid(x, y, x - 2, y + 2, moves);
                addMoveIfValid(x, y, x - 2, y - 2, moves);
                break;
            case MA: // Horse
                int[] dx = { 1, 2, 2, 1, -1, -2, -2, -1 };
                int[] dy = { 2, 1, -1, -2, -2, -1, 1, 2 };
                for (int i = 0; i < 8; i++)
                    addMoveIfValid(x, y, x + dx[i], y + dy[i], moves);
                break;
            case JU: // Chariot
            case PAO: // Cannon
                // Linear moves
                int[][] dirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
                for (int[] d : dirs) {
                    for (int k = 1; k < 10; k++) {
                        int tx = x + d[0] * k;
                        int ty = y + d[1] * k;
                        if (tx < 0 || tx >= COLS || ty < 0 || ty >= ROWS)
                            break;
                        Piece target = pieces[tx][ty];
                        if (p.type == PieceType.JU) {
                            if (target == null) {
                                moves.add(new Move(x, y, tx, ty));
                            } else {
                                if (target.isRed != p.isRed)
                                    moves.add(new Move(x, y, tx, ty));
                                break; // Blocked
                            }
                        } else { // PAO
                            if (target == null) {
                                moves.add(new Move(x, y, tx, ty));
                            } else {
                                // Jump over first piece to capture second?
                                // Cannon moves like rook if not capturing?
                                // Actually No.
                                // Move: Like rook to empty square.
                                // Capture: Jump exactly one piece.

                                // Logic loop for cannon:
                                // Move until hit piece.
                                // If hit piece, look BEHIND it for capture.

                                // Wait, the simplified loop above is for Rook. Let's rewrite for Pao.
                                break; // Handled separately below to avoid confusion in this shared loop
                            }
                        }
                    }
                }
                if (p.type == PieceType.PAO) {
                    for (int[] d : dirs) {
                        boolean jump = false;
                        for (int k = 1; k < 10; k++) {
                            int tx = x + d[0] * k;
                            int ty = y + d[1] * k;
                            if (tx < 0 || tx >= COLS || ty < 0 || ty >= ROWS)
                                break;
                            Piece target = pieces[tx][ty];
                            if (!jump) {
                                if (target == null) {
                                    moves.add(new Move(x, y, tx, ty));
                                } else {
                                    jump = true; // Found the screen/platform
                                }
                            } else {
                                if (target != null) {
                                    if (target.isRed != p.isRed) {
                                        moves.add(new Move(x, y, tx, ty, target)); // Capture
                                    }
                                    break; // Cannot jump over two pieces
                                }
                            }
                        }
                    }
                }
                break;
            case BING: // Soldier
                // Move forward.
                // If crossing river, can also move sideways.
                int forward = p.isRed ? -1 : 1;
                addMoveIfValid(x, y, x, y + forward, moves);
                boolean crossedRiver = p.isRed ? y < 5 : y > 4;
                if (crossedRiver) {
                    addMoveIfValid(x, y, x + 1, y, moves);
                    addMoveIfValid(x, y, x - 1, y, moves);
                }
                break;
        }
        return moves;
    }

    // Helper to add move if target is valid
    private void addMoveIfValid(int startX, int startY, int endX, int endY, List<Move> moves) {
        if (isValidPseudoMove(startX, startY, endX, endY)) {
            moves.add(new Move(startX, startY, endX, endY, pieces[endX][endY]));
        }
    }

    private boolean isValidPseudoMove(int startX, int startY, int endX, int endY) {
        if (endX < 0 || endX >= COLS || endY < 0 || endY >= ROWS)
            return false;
        Piece p = pieces[startX][startY];
        Piece target = pieces[endX][endY];

        if (target != null && target.isRed == p.isRed)
            return false; // Cannot capture own piece

        switch (p.type) {
            case SHUAI:
                // Must be in palace
                if (endX < 3 || endX > 5)
                    return false;
                if (p.isRed) {
                    if (endY < 7)
                        return false;
                } else {
                    if (endY > 2)
                        return false;
                }
                return true;
            case SHI:
                // Must be in palace
                if (endX < 3 || endX > 5)
                    return false;
                if (p.isRed) {
                    if (endY < 7)
                        return false;
                } else {
                    if (endY > 2)
                        return false;
                }
                return true;
            case XIANG:
                // Cannot cross river
                if (p.isRed) {
                    if (endY < 5)
                        return false;
                } else {
                    if (endY > 4)
                        return false;
                }
                // Check eye (obstructing point)
                int eyeX = (startX + endX) / 2;
                int eyeY = (startY + endY) / 2;
                if (pieces[eyeX][eyeY] != null)
                    return false;
                return true;
            case MA:
                // Check hobbling leg
                int lx = startX, ly = startY;
                if (Math.abs(endX - startX) == 2) {
                    lx = (startX + endX) / 2;
                } else {
                    ly = (startY + endY) / 2;
                }
                if (pieces[lx][ly] != null)
                    return false;
                return true;
            default:
                return true;
        }
    }

    public List<Move> getHistory() {
        return moveHistory;
    }

    public void reset() {
        moveHistory.clear();
        pieces = new Piece[COLS][ROWS];
        initBoard();
    }
}
