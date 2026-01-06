package com.example.xiangqi.ai;

import com.example.xiangqi.core.Board;
import com.example.xiangqi.core.Move;
import com.example.xiangqi.core.Piece;
import com.example.xiangqi.core.PieceType;
import java.util.List;

public class MinimaxAI implements XiangqiAI {
    private int searchDepth;

    public MinimaxAI(int depth) {
        this.searchDepth = depth;
    }

    public void setDifficulty(int level) {
        // level 1: easy (depth 2)
        // level 2: medium (depth 3)
        // level 3: hard (depth 4)
        this.searchDepth = level + 1;
    }

    @Override
    public Move getBestMove(Board board, boolean isRed) {
        long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        List<Move> moves = board.generateLegalMoves(isRed);
        int bestVal = Integer.MIN_VALUE;

        for (Move move : moves) {
            board.executeMove(move);
            int val = -minimax(board, searchDepth - 1, alpha, beta, !isRed);
            board.undoMove();

            if (val > bestVal) {
                bestVal = val;
                bestMove = move;
            }
            alpha = Math.max(alpha, val);
        }
        System.out.println("AI Move time: " + (System.currentTimeMillis() - startTime) + "ms, Val: " + bestVal);
        return bestMove;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        if (depth == 0) {
            return evaluate(board, isMaximizingPlayer);
        }

        List<Move> moves = board.generateLegalMoves(isMaximizingPlayer);
        if (moves.isEmpty()) {
            return -100000; // Checkmate (Loss)
        }

        int maxEval = Integer.MIN_VALUE;
        for (Move move : moves) {
            board.executeMove(move);
            int eval = -minimax(board, depth - 1, -beta, -alpha, !isMaximizingPlayer);
            board.undoMove();

            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(alpha, eval);
            if (beta <= alpha) {
                break;
            }
        }
        return maxEval;
    }

    // Simple evaluation function
    // Positive for the current player perspective
    private int evaluate(Board board, boolean isRed) {
        int redScore = 0;
        int blackScore = 0;

        for (int x = 0; x < Board.COLS; x++) {
            for (int y = 0; y < Board.ROWS; y++) {
                Piece p = board.getPiece(x, y);
                if (p != null) {
                    int val = getPieceValue(p.type);
                    // Add position value? Simplified for now.
                    if (p.isRed)
                        redScore += val;
                    else
                        blackScore += val;
                }
            }
        }

        return isRed ? (redScore - blackScore) : (blackScore - redScore);
    }

    private int getPieceValue(PieceType type) {
        switch (type) {
            case SHUAI:
                return 10000;
            case JU:
                return 1000;
            case PAO:
                return 450;
            case MA:
                return 400;
            case XIANG:
                return 200;
            case SHI:
                return 200;
            case BING:
                return 100;
            default:
                return 0;
        }
    }
}
