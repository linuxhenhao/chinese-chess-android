const boardConfig = { rows: 10, cols: 9 };
const initialBoard = [
    ['br', 'bn', 'bb', 'ba', 'bk', 'ba', 'bb', 'bn', 'br'],
    ['', '', '', '', '', '', '', '', ''],
    ['', 'bc', '', '', '', '', '', 'bc', ''],
    ['bp', '', 'bp', '', 'bp', '', 'bp', '', 'bp'],
    ['', '', '', '', '', '', '', '', ''],
    ['', '', '', '', '', '', '', '', ''],
    ['rp', '', 'rp', '', 'rp', '', 'rp', '', 'rp'],
    ['', 'rc', '', '', '', '', '', 'rc', ''],
    ['', '', '', '', '', '', '', '', ''],
    ['rr', 'rn', 'rb', 'ra', 'rk', 'ra', 'rb', 'rn', 'rr']
];

let board = [];
let currentTurn = 'red';
let selectedPiece = null;
let moveHistory = [];
let historyIndex = -1;
let gameMode = 'pvp';
let isThinking = false;

const pieceValues = { 'k': 1000, 'r': 10, 'c': 5, 'n': 4, 'b': 2, 'a': 2, 'p': 1 };
const pieceInfo = {
    'br': { char: '車', color: 'black' }, 'bn': { char: '馬', color: 'black' },
    'bb': { char: '象', color: 'black' }, 'ba': { char: '士', color: 'black' },
    'bk': { char: '將', color: 'black' }, 'bc': { char: '砲', color: 'black' },
    'bp': { char: '卒', color: 'black' }, 'rr': { char: '俥', color: 'red' },
    'rn': { char: '傌', color: 'red' }, 'rb': { char: '相', color: 'red' },
    'ra': { char: '仕', color: 'red' }, 'rk': { char: '帥', color: 'red' },
    'rc': { char: '炮', color: 'red' }, 'rp': { char: '兵', color: 'red' }
};

function initGame() {
    board = JSON.parse(JSON.stringify(initialBoard));
    currentTurn = 'red';
    selectedPiece = null;
    moveHistory = [];
    historyIndex = -1;
    isThinking = false;
    drawGrid();
    renderBoard();
    updateStatus();
    updateControls();
}

function renderBoard() {
    const piecesLayer = document.getElementById('pieces-layer');
    const interactionLayer = document.getElementById('interaction-layer');
    piecesLayer.innerHTML = '';
    interactionLayer.innerHTML = '';
    const root = document.documentElement;
    const pieceRadius = parseInt(getComputedStyle(root).getPropertyValue('--piece-radius'));

    for (let r = 0; r < 10; r++) {
        for (let c = 0; c < 9; c++) {
            const code = board[r][c];
            if (code) {
                const piece = document.createElement('div');
                piece.className = `piece ${pieceInfo[code].color}`;
                if (selectedPiece && selectedPiece.r === r && selectedPiece.c === c) piece.classList.add('selected');
                piece.textContent = pieceInfo[code].char;
                piece.style.top = (r * pieceRadius) + 'px';
                piece.style.left = (c * pieceRadius) + 'px';
                piece.onclick = (e) => { e.stopPropagation(); handleSquareClick(r, c); };
                piecesLayer.appendChild(piece);
            }
        }
    }

    if (selectedPiece && !isThinking && (gameMode === 'pvp' || currentTurn === 'red')) {
        for (let r = 0; r < 10; r++) {
            for (let c = 0; c < 9; c++) {
                if (isValidMove(selectedPiece.r, selectedPiece.c, r, c, selectedPiece.code, board)) {
                    const highlight = document.createElement('div');
                    highlight.className = 'highlight-move';
                    highlight.style.top = (r * pieceRadius) + 'px';
                    highlight.style.left = (c * pieceRadius) + 'px';
                    highlight.onclick = () => makeMove(selectedPiece.r, selectedPiece.c, r, c);
                    interactionLayer.appendChild(highlight);
                }
            }
        }
    }
}

function handleSquareClick(r, c) {
    if (isThinking) return;
    if (gameMode === 'ai' && currentTurn === 'black') return;
    const code = board[r][c];
    if (code && code.startsWith(currentTurn[0])) {
        selectedPiece = { r, c, code };
    } else if (selectedPiece && isValidMove(selectedPiece.r, selectedPiece.c, r, c, selectedPiece.code, board)) {
        makeMove(selectedPiece.r, selectedPiece.c, r, c);
        return;
    } else {
        selectedPiece = null;
    }
    renderBoard();
}

function makeMove(fR, fC, tR, tC, isAI = false) {
    if (historyIndex < moveHistory.length - 1) {
        moveHistory = moveHistory.slice(0, historyIndex + 1);
    }

    const captured = board[tR][tC];
    const moved = board[fR][fC];
    const prevState = JSON.stringify(board);

    board[tR][tC] = moved;
    board[fR][fC] = '';

    moveHistory.push({ fR, fC, tR, tC, captured, moved, prevState });
    historyIndex++;

    selectedPiece = null;
    currentTurn = (currentTurn === 'red' ? 'black' : 'red');

    renderBoard();
    updateStatus();
    updateControls();

    if (captured && captured.endsWith('k')) {
        setTimeout(() => { alert((captured.startsWith('r') ? '黑方' : '红方') + ' 获胜！'); initGame(); }, 300);
        return;
    }

    if (gameMode === 'ai' && currentTurn === 'black' && !isAI) {
        startAI();
    }
}

function startAI() {
    isThinking = true;
    updateStatus("AI 正在思考...");
    setTimeout(() => {
        const bestMove = getBestMove(board, 3);
        isThinking = false;
        if (bestMove) {
            makeMove(bestMove.fR, bestMove.fC, bestMove.tR, bestMove.tC, true);
        } else {
            alert("绝代双骄？（无路可走）");
        }
    }, 100);
}

function getBestMove(currentBoard, depth) {
    const moves = getAllValidMoves(currentBoard, 'black');
    if (moves.length === 0) return null;

    let bestScore = -Infinity;
    let bestMoves = [];
    let alpha = -Infinity;
    let beta = Infinity;

    for (const move of moves) {
        const captured = currentBoard[move.tR][move.tR];
        const moved = currentBoard[move.fR][move.fC];

        // Execute move
        const targetCaptured = currentBoard[move.tR][move.tC];
        currentBoard[move.tR][move.tC] = moved;
        currentBoard[move.fR][move.fC] = '';

        let score = minimax(currentBoard, depth - 1, alpha, beta, false);

        // Undo move
        currentBoard[move.fR][move.fC] = moved;
        currentBoard[move.tR][move.tC] = targetCaptured;

        if (score > bestScore) {
            bestScore = score;
            bestMoves = [move];
        } else if (score === bestScore) {
            bestMoves.push(move);
        }
        alpha = Math.max(alpha, bestScore);
    }
    return bestMoves[Math.floor(Math.random() * bestMoves.length)];
}

function minimax(mBoard, depth, alpha, beta, isMaximizing) {
    if (depth === 0) return evaluateBoard(mBoard);

    const moves = getAllValidMoves(mBoard, isMaximizing ? 'black' : 'red');
    if (moves.length === 0) return isMaximizing ? -10000 : 10000;

    if (isMaximizing) {
        let maxEval = -Infinity;
        for (const move of moves) {
            const moved = mBoard[move.fR][move.fC];
            const captured = mBoard[move.tR][move.tC];
            mBoard[move.tR][move.tC] = moved;
            mBoard[move.fR][move.fC] = '';

            const ev = minimax(mBoard, depth - 1, alpha, beta, false);

            mBoard[move.fR][move.fC] = moved;
            mBoard[move.tR][move.tC] = captured;

            maxEval = Math.max(maxEval, ev);
            alpha = Math.max(alpha, ev);
            if (beta <= alpha) break;
        }
        return maxEval;
    } else {
        let minEval = Infinity;
        for (const move of moves) {
            const moved = mBoard[move.fR][move.fC];
            const captured = mBoard[move.tR][move.tC];
            mBoard[move.tR][move.tC] = moved;
            mBoard[move.fR][move.fC] = '';

            const ev = minimax(mBoard, depth - 1, alpha, beta, true);

            mBoard[move.fR][move.fC] = moved;
            mBoard[move.tR][move.tC] = captured;

            minEval = Math.min(minEval, ev);
            beta = Math.min(beta, ev);
            if (beta <= alpha) break;
        }
        return minEval;
    }
}

function evaluateBoard(mBoard) {
    let score = 0;
    for (let r = 0; r < 10; r++) {
        for (let c = 0; c < 9; c++) {
            const piece = mBoard[r][c];
            if (piece) {
                const val = pieceValues[piece[1]];
                score += (piece[0] === 'b' ? val : -val);
            }
        }
    }
    return score;
}

function getAllValidMoves(mBoard, turn) {
    const color = turn[0];
    const moves = [];
    for (let r = 0; r < 10; r++) {
        for (let c = 0; c < 9; c++) {
            const code = mBoard[r][c];
            if (code && code.startsWith(color)) {
                for (let tr = 0; tr < 10; tr++) {
                    for (let tc = 0; tc < 9; tc++) {
                        if (isValidMove(r, c, tr, tc, code, mBoard)) {
                            moves.push({ fR: r, fC: c, tR: tr, tC: tc });
                        }
                    }
                }
            }
        }
    }
    return moves;
}

function updateStatus(overrideText) {
    const s = document.getElementById('status');
    if (overrideText) {
        s.textContent = overrideText;
        s.style.color = '#e67e22';
        return;
    }
    s.textContent = currentTurn === 'red' ? '红方走子' : '黑方走子';
    s.style.color = currentTurn === 'red' ? '#ff5252' : '#999';
}

function updateControls() {
    const total = moveHistory.length;
    const current = historyIndex + 1;
    document.getElementById('move-count').textContent = `${current} / ${total}`;
    const canNav = !isThinking;
    document.getElementById('undo-btn').disabled = historyIndex < 0 || !canNav;
    document.getElementById('first-move').disabled = historyIndex < 0 || !canNav;
    document.getElementById('prev-move').disabled = historyIndex < 0 || !canNav;
    document.getElementById('next-move').disabled = historyIndex >= total - 1 || !canNav;
    document.getElementById('last-move').disabled = historyIndex >= total - 1 || !canNav;
}

function jumpToHistory(index) {
    if (index < -1 || index >= moveHistory.length) return;
    if (index === -1) {
        board = JSON.parse(JSON.stringify(initialBoard));
        currentTurn = 'red';
    } else {
        const move = moveHistory[index];
        board = JSON.parse(move.prevState);
        board[move.tR][move.tC] = move.moved;
        board[move.fR][move.fC] = '';
        currentTurn = (move.moved.startsWith('r') ? 'black' : 'red');
    }
    historyIndex = index;
    selectedPiece = null;
    renderBoard();
    updateStatus();
    updateControls();
}

document.getElementById('pvp-mode').onclick = function () {
    if (isThinking) return;
    this.classList.add('active');
    document.getElementById('ai-mode').classList.remove('active');
    gameMode = 'pvp';
    initGame();
};
document.getElementById('ai-mode').onclick = function () {
    if (isThinking) return;
    this.classList.add('active');
    document.getElementById('pvp-mode').classList.remove('active');
    gameMode = 'ai';
    initGame();
};
document.getElementById('undo-btn').onclick = () => jumpToHistory(historyIndex - 1);
document.getElementById('first-move').onclick = () => jumpToHistory(-1);
document.getElementById('prev-move').onclick = () => jumpToHistory(historyIndex - 1);
document.getElementById('next-move').onclick = () => jumpToHistory(historyIndex + 1);
document.getElementById('last-move').onclick = () => jumpToHistory(moveHistory.length - 1);
document.getElementById('reset-btn').onclick = initGame;

function drawGrid() {
    const gridContainer = document.getElementById('grid-container');
    gridContainer.innerHTML = '';
    const root = document.documentElement;
    const pieceRadius = parseInt(getComputedStyle(root).getPropertyValue('--piece-radius'));
    const offset = pieceRadius / 2;
    const width = pieceRadius * 8, height = pieceRadius * 9;
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("width", "100%"); svg.setAttribute("height", "100%");
    svg.setAttribute("viewBox", `0 0 ${pieceRadius * 9} ${pieceRadius * 10}`);
    const stroke = getComputedStyle(root).getPropertyValue('--line-color');
    const createLine = (x1, y1, x2, y2) => {
        const l = document.createElementNS("http://www.w3.org/2000/svg", "line");
        l.setAttribute("x1", x1); l.setAttribute("y1", y1); l.setAttribute("x2", x2); l.setAttribute("y2", y2);
        l.setAttribute("stroke", stroke); l.setAttribute("stroke-width", "1"); return l;
    };
    for (let r = 0; r < 10; r++) svg.appendChild(createLine(offset, r * pieceRadius + offset, offset + width, r * pieceRadius + offset));
    for (let c = 0; c < 9; c++) {
        const x = c * pieceRadius + offset;
        if (c === 0 || c === 8) svg.appendChild(createLine(x, offset, x, offset + height));
        else { svg.appendChild(createLine(x, offset, x, offset + pieceRadius * 4)); svg.appendChild(createLine(x, offset + pieceRadius * 5, x, offset + height)); }
    }
    svg.appendChild(createLine(3 * pieceRadius + offset, offset, 5 * pieceRadius + offset, 2 * pieceRadius + offset));
    svg.appendChild(createLine(5 * pieceRadius + offset, offset, 3 * pieceRadius + offset, 2 * pieceRadius + offset));
    svg.appendChild(createLine(3 * pieceRadius + offset, 7 * pieceRadius + offset, 5 * pieceRadius + offset, 9 * pieceRadius + offset));
    svg.appendChild(createLine(5 * pieceRadius + offset, 7 * pieceRadius + offset, 3 * pieceRadius + offset, 9 * pieceRadius + offset));
    const drawStar = (r, c) => {
        const x = c * pieceRadius + offset, y = r * pieceRadius + offset, s = 4, g = 2;
        if (c > 0) {
            svg.appendChild(createLine(x - g, y - g - s, x - g, y - g)); svg.appendChild(createLine(x - g - s, y - g, x - g, y - g));
            svg.appendChild(createLine(x - g, y + g + s, x - g, y + g)); svg.appendChild(createLine(x - g - s, y + g, x - g, y + g));
        }
        if (c < 8) {
            svg.appendChild(createLine(x + g, y - g - s, x + g, y - g)); svg.appendChild(createLine(x + g + s, y - g, x + g, y - g));
            svg.appendChild(createLine(x + g, y + g + s, x + g, y + g)); svg.appendChild(createLine(x + g + s, y + g, x + g, y + g));
        }
    };
    drawStar(2, 1); drawStar(2, 7); drawStar(7, 1); drawStar(7, 7);
    for (let i = 0; i < 9; i += 2) { drawStar(3, i); drawStar(6, i); }
    gridContainer.appendChild(svg);
}

function isValidMove(r1, c1, r2, c2, piece, currentBoard) {
    const color = piece[0], type = piece[1], target = currentBoard[r2][c2];
    if (target && target[0] === color) return false;
    const dr = r2 - r1, dc = c2 - c1, adr = Math.abs(dr), adc = Math.abs(dc);
    switch (type) {
        case 'k': return (c2 >= 3 && c2 <= 5) && (color === 'r' ? r2 >= 7 : r2 <= 2) && (adr + adc === 1);
        case 'a': return (c2 >= 3 && c2 <= 5) && (color === 'r' ? r2 >= 7 : r2 <= 2) && (adr === 1 && adc === 1);
        case 'b': return (adr === 2 && adc === 2) && currentBoard[r1 + dr / 2][c1 + dc / 2] === '' && (color === 'r' ? r2 >= 5 : r2 <= 4);
        case 'n': return ((adr === 2 && adc === 1) && currentBoard[r1 + dr / 2][c1] === '') || ((adr === 1 && adc === 2) && currentBoard[r1][c1 + dc / 2] === '');
        case 'r': return (r1 === r2 || c1 === c2) && countObstacles(r1, c1, r2, c2, currentBoard) === 0;
        case 'c': if (r1 !== r2 && c1 !== c2) return false; const obs = countObstacles(r1, c1, r2, c2, currentBoard); return target === '' ? obs === 0 : obs === 1;
        case 'p': const fwd = (color === 'r' ? -1 : 1), crossed = (color === 'r' ? r1 < 5 : r1 > 4); return (r2 === r1 + fwd && c1 === c2) || (crossed && r1 === r2 && adc === 1);
    }
    return false;
}

function countObstacles(r1, c1, r2, c2, currentBoard) {
    let count = 0;
    if (r1 === r2) { for (let c = Math.min(c1, c2) + 1; c < Math.max(c1, c2); c++) if (currentBoard[r1][c] !== '') count++; }
    else { for (let r = Math.min(r1, r2) + 1; r < Math.max(r1, r2); r++) if (currentBoard[r][c1] !== '') count++; }
    return count;
}

initGame();
