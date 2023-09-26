package pl.mg.engine;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

class Carousel {
    private final Result onResult;
    private SecureRandom random;
    private int imageWidth;
    private int imageHeight;
    private Canvas canvas;
    private double canvasHeight;
    private double canvasWidth;
    private GraphicsContext graphicsContext;
    private int[][] reelsDefinition;
    private Image[] imagesCache;
    private int[] multipliers;
    private SymbolRecord[] symbolDefinitions;

    private double margin = 35;
    private double padding = 80;
    private int drawnSymbolsCount = 5;
    int reelsStopped = 0;

    private int[] spins;
    private int[] currentIdx;
    private int[] winningIdx;
    private double[][] visibleImagesOnFrameYOffset;
    private double[] visibleImagesOnFrameXOffset;
    private AnimationTimer spinAnimation;
    private double animationSpeed = 50;
    private double[] reelsSpeed;

    public Carousel(Canvas canvas, int[][] carousels, SymbolRecord[] inGameSymbols, Result onResult) throws NoSuchAlgorithmException {
        this.onResult = onResult;
        random = SecureRandom.getInstanceStrong();
        setCanvas(canvas);
        setReelsDefinition(carousels);
        setSymbolDefinitions(inGameSymbols);
    }

    public void setImageSize(int imageWidth, int imageHeight) {

        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        graphicsContext = canvas.getGraphicsContext2D();
    }

    public void setReelsDefinition(int[][] carousels) {
        reelsDefinition = carousels;
        int reelsCount = carousels.length;
        spins = new int[reelsCount];
        currentIdx = new int[reelsCount];
        winningIdx = new int[reelsCount];
        visibleImagesOnFrameYOffset = new double[reelsCount][drawnSymbolsCount];
        visibleImagesOnFrameXOffset = new double[reelsCount];
        reelsSpeed = new double[reelsCount];
    }

    public void setSymbolDefinitions(SymbolRecord[] symbolDefinitions) {
        imagesCache = new Image[symbolDefinitions.length];
        multipliers = new int[symbolDefinitions.length];
        this.symbolDefinitions = symbolDefinitions;

        for (SymbolRecord record : symbolDefinitions) {
            imagesCache[record.getId()] = new Image(getClass().getClassLoader().getResourceAsStream("images/" + record.getFile()));
            multipliers[record.getId()] = record.getMultiplier();
        }

        setImageSize((int) imagesCache[0].getWidth(), (int) imagesCache[0].getHeight());
        for (int i = 0; i < 3; i++)
            currentIdx[i] = random.nextInt(reelsDefinition[i].length);

        initReelsPosition();
        redrawReels();
    }

    public void redrawReel(Image img, double x, double y, double width, double height) {
        graphicsContext.drawImage(img, x, y, width, height);
    }

    private void initReelsPosition() {
        for (int i = 0; i < 3; i++) {
            visibleImagesOnFrameXOffset[i] = i * margin + padding * i + imageWidth * i;
            for (int k = 0; k < drawnSymbolsCount; k++) {
                visibleImagesOnFrameYOffset[i][k] = (imageHeight * k) - imageHeight;
            }
        }

        reelsStopped = 0;
    }

    void play() {
        System.out.println("Winners ");
        for (int i = 0; i < 3; i++) {
            winningIdx[i] = random.nextInt(reelsDefinition[i].length);
            spins[i] = (currentIdx[i] - winningIdx[i]) + (reelsDefinition[i].length) * (i + 1);
            System.out.println(winningIdx[i] + " " + symbolDefinitions[reelsDefinition[i][winningIdx[i]]].getFile());
            reelsSpeed[i] = animationSpeed;
        }


        initReelsPosition();

        canvas.setEffect(new BoxBlur(10, 10, 2));

        if (spinAnimation == null)
            spinAnimation = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);

                    for (int i = 0; i < 3; i++) {
                        if (spins[i] < 1) // center winning symbols
                        {
                            onReelDamping();

                            switch (spins[i]) {
                                case -2:
                                    onReelStop();
                                    spins[i] = -3;
                                case -3:
                                    continue;
                            }

                            double targetLocation = imageHeight * 2;
                            double currentLocation = visibleImagesOnFrameYOffset[i][1];
                            double dest = targetLocation - currentLocation;

                            reelsSpeed[i] = logInterpolate(1, animationSpeed, dest / targetLocation);
                        }
                        for (int k = 0; k < drawnSymbolsCount; k++) {
                            recalculateReelsPositions(i, k, reelsSpeed[i]);
                        }
                    }
                    redrawReels();
                }
            };

        spinAnimation.start();
    }

    private void redrawReels() {
        for (int i = 0; i < 3; i++)
            for (int k = 0; k < drawnSymbolsCount; k++) {
                redrawReel(imagesCache[reelsDefinition[i][(currentIdx[i] + k) % reelsDefinition[i].length]],
                        visibleImagesOnFrameXOffset[i],
                        visibleImagesOnFrameYOffset[i][k],
                        imageWidth,
                        imageHeight);

            }
    }

    private void onReelStop() {
        reelsStopped++;

        if (reelsStopped == reelsDefinition.length) {
            onResult.callback(calculateMultiplier());
            spinAnimation.stop();
        }
    }

    private int calculateMultiplier() {

        int calc = reelsDefinition[0][winningIdx[0]];

        for (int i = 1; i < reelsDefinition.length; i++)
            if (calc != reelsDefinition[i][winningIdx[i]])
                return 0;

        return multipliers[calc];
    }

    private void onReelDamping() {
        canvas.setEffect(null);
    }

    private void recalculateReelsPositions(int i, int k, double animationSpeed) {
        if (visibleImagesOnFrameYOffset[i][k] > canvasHeight + imageHeight) {
            currentIdx[i] = currentIdx[i] - 1 < 0 ? reelsDefinition[i].length - 1 : currentIdx[i] - 1;
            for (int j = k; j > 0; j--) {
                visibleImagesOnFrameYOffset[i][j] = visibleImagesOnFrameYOffset[i][j - 1];
            }
            visibleImagesOnFrameYOffset[i][0] = visibleImagesOnFrameYOffset[i][1] - imageHeight;
            spins[i]--;
        }

        visibleImagesOnFrameYOffset[i][k] += animationSpeed;
    }

    private double logInterpolate(double val1, double val2, double t) {

        double logValue1 = Math.log(val1);
        double logValue2 = Math.log(val2);

        double interpolatedLog = logValue1 + t * (logValue2 - logValue1);

        return Math.exp(interpolatedLog);
    }

    public static interface Result {
        void callback(int result);
    }
}
