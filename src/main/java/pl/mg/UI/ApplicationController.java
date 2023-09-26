package pl.mg.UI;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import pl.mg.engine.Game;
import pl.mg.engine.SymbolRecord;

import java.io.IOException;

public class ApplicationController {
    private Stage primaryStage;
    private Game slotMachine;
    private Button spin;
    private Button payIn;

    private Label balance;
    private Label lastWin;
    private Label stake;

    public void spin(MouseEvent actionEvent) {
        enableButtons(false);
        slotMachine.setStake(10);
        slotMachine.play();
        refreshView();
    }

    public void payIn(MouseEvent actionEvent)
    {
        slotMachine.setBalance(slotMachine.getBalance() + 1000);
        refreshView();
    }

    private void enableButtons(boolean enabled)
    {
        spin.setDisable(!enabled);
        payIn.setDisable(!enabled);
    }

    public void start() throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        SymbolRecord[] inGameSymbols = mapper.readValue(getClass().getClassLoader().getResourceAsStream("data/symbols.json"), SymbolRecord[].class);
        int[][] carousels = mapper.readValue(getClass().getClassLoader().getResourceAsStream("data/carouselsDefinitions.json"), int[][].class);

        Scene scene = primaryStage.getScene();
        Canvas canvas = (Canvas) scene.lookup("#carouselCanvas");
        spin = (Button) scene.lookup("#start");
        payIn = (Button) scene.lookup("#payin");
        balance = (Label) scene.lookup("#balance");
        lastWin = (Label) scene.lookup("#lastWin");
        stake = (Label) scene.lookup("#stake");

        slotMachine = new Game(canvas.getGraphicsContext2D(), carousels, inGameSymbols);

        slotMachine.onCarouselStop(isAWin -> {
            enableButtons(true);
            refreshView();
        });
        slotMachine.setBalance(1000);
        refreshView();
        primaryStage.show();
    }

    private void refreshView() {
        lastWin.setText(String.valueOf(slotMachine.getLastWin()));
        balance.setText(String.valueOf(slotMachine.getBalance()));
        stake.setText(String.valueOf(slotMachine.getStake()));
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
