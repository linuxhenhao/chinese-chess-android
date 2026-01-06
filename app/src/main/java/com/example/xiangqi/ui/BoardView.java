package com.example.xiangqi.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.xiangqi.R;
import com.example.xiangqi.core.Board;
import com.example.xiangqi.core.Move;
import com.example.xiangqi.core.Piece;
import com.example.xiangqi.core.PieceType;

import java.util.List;

public class BoardView extends View {
    private Board board;
    private float cellWidth, cellHeight;
    private Paint linePaint;
    private Paint piecePaint;
    private Paint textPaint;
    private Paint selectedPaint;
    private Paint lastMovePaint;

    private int selectedX = -1;
    private int selectedY = -1;

    private MoveListener moveListener;
    private boolean isInteractionEnabled = true;
    private boolean isFlipped = false; // For POV flipping if needed, but standard is Red bottom

    public interface MoveListener {
        void onMoveAttempt(int startX, int startY, int endX, int endY);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(3);

        piecePaint = new Paint();
        piecePaint.setAntiAlias(true);
        piecePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);

        selectedPaint = new Paint();
        selectedPaint.setColor(getResources().getColor(R.color.selected_color, null));

        lastMovePaint = new Paint();
        lastMovePaint.setColor(getResources().getColor(R.color.last_move_color, null));
    }

    public void setBoard(Board board) {
        this.board = board;
        this.selectedX = -1;
        this.selectedY = -1;
        invalidate();
    }

    public void setMoveListener(MoveListener listener) {
        this.moveListener = listener;
    }

    public void setInteractionEnabled(boolean enabled) {
        this.isInteractionEnabled = enabled;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * 1.15); // Aspect ratio roughly 9:10
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (board == null)
            return;

        int width = getWidth();
        int height = getHeight();
        cellWidth = width / 9f;
        cellHeight = height / 10f;

        drawBoardGrid(canvas);
        drawLastMove(canvas);
        drawSelection(canvas);
        drawPieces(canvas);
    }

    private void drawBoardGrid(Canvas canvas) {
        // Horizontal lines
        for (int i = 0; i < 10; i++) {
            float y = i * cellHeight + cellHeight / 2;
            canvas.drawLine(cellWidth / 2, y, getWidth() - cellWidth / 2, y, linePaint);
        }
        // Vertical lines
        for (int i = 0; i < 9; i++) {
            float x = i * cellWidth + cellWidth / 2;
            if (i == 0 || i == 8) {
                canvas.drawLine(x, cellHeight / 2, x, getHeight() - cellHeight / 2, linePaint);
            } else {
                canvas.drawLine(x, cellHeight / 2, x, 4 * cellHeight + cellHeight / 2, linePaint);
                canvas.drawLine(x, 5 * cellHeight + cellHeight / 2, x, getHeight() - cellHeight / 2, linePaint);
            }
        }

        // Palace diagonals
        canvas.drawLine(3 * cellWidth + cellWidth / 2, cellHeight / 2, 5 * cellWidth + cellWidth / 2,
                2 * cellHeight + cellHeight / 2, linePaint);
        canvas.drawLine(5 * cellWidth + cellWidth / 2, cellHeight / 2, 3 * cellWidth + cellWidth / 2,
                2 * cellHeight + cellHeight / 2, linePaint);

        canvas.drawLine(3 * cellWidth + cellWidth / 2, 7 * cellHeight + cellHeight / 2, 5 * cellWidth + cellWidth / 2,
                9 * cellHeight + cellHeight / 2, linePaint);
        canvas.drawLine(5 * cellWidth + cellWidth / 2, 7 * cellHeight + cellHeight / 2, 3 * cellWidth + cellWidth / 2,
                9 * cellHeight + cellHeight / 2, linePaint);

        // River text? Maybe later.
    }

    private void drawLastMove(Canvas canvas) {
        List<Move> history = board.getHistory();
        if (!history.isEmpty()) {
            Move last = history.get(history.size() - 1);
            drawHighlight(canvas, last.startX, last.startY, lastMovePaint);
            drawHighlight(canvas, last.endX, last.endY, lastMovePaint);
        }
    }

    private void drawSelection(Canvas canvas) {
        if (selectedX != -1 && selectedY != -1) {
            drawHighlight(canvas, selectedX, selectedY, selectedPaint);
        }
    }

    private void drawHighlight(Canvas canvas, int x, int y, Paint paint) {
        float cx = x * cellWidth + cellWidth / 2;
        float cy = y * cellHeight + cellHeight / 2;
        canvas.drawCircle(cx, cy, cellWidth / 2.2f, paint);
    }

    private void drawPieces(Canvas canvas) {
        for (int x = 0; x < Board.COLS; x++) {
            for (int y = 0; y < Board.ROWS; y++) {
                Piece piece = board.getPiece(x, y);
                if (piece != null) {
                    drawPiece(canvas, x, y, piece);
                }
            }
        }
    }

    private void drawPiece(Canvas canvas, int x, int y, Piece piece) {
        float cx = x * cellWidth + cellWidth / 2;
        float cy = y * cellHeight + cellHeight / 2;
        float radius = Math.min(cellWidth, cellHeight) / 2 - 5;

        piecePaint.setColor(getResources().getColor(R.color.board_bg, null)); // Background of piece
        canvas.drawCircle(cx, cy, radius, piecePaint);

        piecePaint.setStyle(Paint.Style.STROKE);
        piecePaint.setColor(piece.isRed ? Color.RED : Color.BLACK);
        piecePaint.setStrokeWidth(5);
        canvas.drawCircle(cx, cy, radius, piecePaint);

        piecePaint.setStyle(Paint.Style.FILL); // Reset

        textPaint.setColor(piece.isRed ? Color.RED : Color.BLACK);
        float textY = cy - (textPaint.descent() + textPaint.ascent()) / 2;
        // Map English enum to Chinese Character
        String label = getChineseName(piece);
        canvas.drawText(label, cx, textY, textPaint);
    }

    private String getChineseName(Piece piece) {
        // Simplified Chinese
        switch (piece.type) {
            case SHUAI:
                return piece.isRed ? "帅" : "将";
            case SHI:
                return piece.isRed ? "仕" : "士";
            case XIANG:
                return piece.isRed ? "相" : "象";
            case MA:
                return "马";
            case JU:
                return "车";
            case PAO:
                return "炮"; // Could distinguish 炮 vs 砲 but simplified often uses one
            case BING:
                return piece.isRed ? "兵" : "卒";
            default:
                return "?";
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInteractionEnabled)
            return false;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int col = (int) (event.getX() / cellWidth);
            int row = (int) (event.getY() / cellHeight);

            if (col >= 0 && col < Board.COLS && row >= 0 && row < Board.ROWS) {
                if (selectedX == -1) {
                    // Try select
                    Piece p = board.getPiece(col, row);
                    // Only select own pieces? Or UI handles turns?
                    // Better to let logic handle turn, but UI should only allow selecting friendly
                    // pieces if start of move
                    if (p != null) {
                        // Callback to activity to check if it's this player's turn?
                        // For simplicity, just store selection. The activity listener will validate.
                        selectedX = col;
                        selectedY = row;
                        invalidate();
                    }
                } else {
                    // Move attempt
                    if (moveListener != null) {
                        moveListener.onMoveAttempt(selectedX, selectedY, col, row);
                    }
                    selectedX = -1;
                    selectedY = -1;
                    invalidate();
                }
            }
        }
        return true;
    }
}
