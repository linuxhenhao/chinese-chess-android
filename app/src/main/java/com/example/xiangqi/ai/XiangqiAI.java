package com.example.xiangqi.ai;

import com.example.xiangqi.core.Board;
import com.example.xiangqi.core.Move;

public interface XiangqiAI {
    Move getBestMove(Board board, boolean isRed);
}
