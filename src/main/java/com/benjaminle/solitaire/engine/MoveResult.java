package com.benjaminle.solitaire.engine;

/** Result returned by every player action instead of printing from game logic. */
public record MoveResult(boolean success, String message) {
    public static MoveResult success(String message) {
        return new MoveResult(true, message);
    }

    public static MoveResult failure(String message) {
        return new MoveResult(false, message);
    }
}
