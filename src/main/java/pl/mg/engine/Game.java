package pl.mg.engine;

import javafx.scene.canvas.GraphicsContext;

import java.security.NoSuchAlgorithmException;

public class Game implements Carousel.Result {

    private int balance = 0;
    private int lastWin = 0;
    private int stake = 10;

    private final Carousel carousel;
    private GameResultCallback callback;

    public Game(GraphicsContext graphicsContext2D, int[][] carousels, SymbolRecord[] inGameSymbols) {
        try {
            carousel = new Carousel(graphicsContext2D.getCanvas(), carousels, inGameSymbols, this);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void onCarouselStop(GameResultCallback callback) {
        this.callback = callback;
    }

    public void play() {
        lastWin = 0;
        carousel.play();
    }

    @Override
    public void callback(int result) {
        int reward = result * stake;
        if (reward > 0) {
            lastWin = reward;
            balance += reward;
        } else {
            balance -= stake;
        }
        callback.handle(reward);
    }

    public void setStake(int stake) {
        this.stake = stake;
    }

    public int getStake() {
        return stake;
    }

    public int getLastWin() {
        return lastWin;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public static interface GameResultCallback {
        void handle(int win);
    }
}
