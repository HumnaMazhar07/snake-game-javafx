package application;


	import javafx.animation.AnimationTimer;
	import javafx.application.Application;
	import javafx.geometry.Pos;
	import javafx.scene.Scene;
	import javafx.scene.control.Label;
	import javafx.scene.input.KeyCode;
	import javafx.scene.layout.BorderPane;
	import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
	import javafx.scene.shape.Rectangle;
	import javafx.scene.text.Font;
	import javafx.stage.Stage;

	import java.io.*;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Random;

	public class Mainn extends Application {

	    // --- Game Constants ---
	    private static final int TILE_SIZE = 20;
	    private static final int WIDTH = 30; // Number of tiles wide
	    private static final int HEIGHT = 20; // Number of tiles high
	    private static final int GAME_WIDTH = WIDTH * TILE_SIZE;
	    private static final int GAME_HEIGHT = HEIGHT * TILE_SIZE;
	    private static final long FRAME_DELAY_NS = 100_000_000L; // 0.1 seconds (100 million nanoseconds) for 10 FPS
	    private static final String HIGHSCORE_FILE = "highscore.txt";

	    // --- Game Variables ---
	    private List<Point> snake;
	    private Point food;
	    private Direction direction;
	    private boolean gameOver;
	    private boolean gameStarted;
	    private Random random;
	    private long lastFrameTime;
	    private int score;
	    private int highScore;

	    // --- GUI Elements ---
	    private Pane gamePane;
	    private Label scoreLabel;
	    private Label highScoreLabel; // New label for high score
	    private Label welcomeLabel;
	    private Label instructionsLabel;
	    private Stage primaryStage;

	    // --- Directions Enum ---
	    private enum Direction {
	        UP, DOWN, LEFT, RIGHT
	    }

	    // --- Point Class for Snake/Food Coordinates ---
	    private static class Point {
	        int x, y;

	        public Point(int x, int y) {
	            this.x = x;
	            this.y = y;
	        }

	        @Override
	        public boolean equals(Object obj) {
	            if (this == obj) return true;
	            if (obj == null || getClass() != obj.getClass()) return false;
	            Point point = (Point) obj;
	            return x == point.x && y == point.y;
	        }

	        @Override
	        public int hashCode() {
	            return 31 * x + y;
	        }
	    }

	    @Override
	    public void start(Stage primaryStage) {
	        this.primaryStage = primaryStage;
	        random = new Random();
	        loadHighScore(); // Load high score at startup
	        showWelcomeScreen();
	    }

	    private void showWelcomeScreen() {
	        BorderPane root = new BorderPane();
	        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
	        root.setStyle("-fx-background-color: purple;"); // Dark Slate Gray

	        welcomeLabel = new Label("SNAKE GAME");
	        welcomeLabel.setFont(Font.font("Impact", 60));
	        welcomeLabel.setTextFill(Color.LIGHTGREEN);
	        BorderPane.setAlignment(welcomeLabel, Pos.CENTER);
	        root.setTop(welcomeLabel);

	        VBox centerContent = new VBox(10); // Use VBox to stack labels
	        centerContent.setAlignment(Pos.CENTER);

	        instructionsLabel = new Label("Press SPACE to Start\nUse Arrow Keys to Move");
	        instructionsLabel.setFont(Font.font("Arial black", 20));
	        instructionsLabel.setTextFill(Color.WHITE);
	        instructionsLabel.setAlignment(Pos.CENTER);

	        highScoreLabel = new Label("High Score: " + highScore);
	        highScoreLabel.setFont(Font.font("Arial black", 24));
	        highScoreLabel.setTextFill(Color.GOLD);
	        highScoreLabel.setAlignment(Pos.CENTER);

	        centerContent.getChildren().addAll(instructionsLabel, highScoreLabel);
	        root.setCenter(centerContent);


	        Scene scene = new Scene(root);
	        scene.setOnKeyPressed(e -> {
	            if (e.getCode() == KeyCode.SPACE && !gameStarted) {
	                startGame();
	            }
	        });

	        primaryStage.setTitle("Snake Game");
	        primaryStage.setScene(scene);
	        primaryStage.setResizable(false);
	        primaryStage.show();
	    }

	    private void startGame() {
	        gameStarted = true;
	        gameOver = false;
	        score = 0;
	        lastFrameTime = System.nanoTime();

	        // Initialize snake in the center
	        snake = new ArrayList<>();
	        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
	        snake.add(new Point(WIDTH / 2 - 1, HEIGHT / 2)); // Add a second segment
	        direction = Direction.RIGHT; // Initial direction

	        // Initialize UI
	        gamePane = new Pane();
	        gamePane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
	        gamePane.setStyle("-fx-background-color: black;");

	        scoreLabel = new Label("Score: " + score);
	        scoreLabel.setFont(Font.font("Arial Black", 17));
	        scoreLabel.setTextFill(Color.PURPLE);
	        scoreLabel.setLayoutX(10);
	        scoreLabel.setLayoutY(10);

	        highScoreLabel = new Label("High Score: " + highScore); // Display high score during game
	        highScoreLabel.setFont(Font.font("Arial Black", 17));
	        highScoreLabel.setTextFill(Color.PURPLE);
	        highScoreLabel.setLayoutX(GAME_WIDTH - 155); // Position it on the right
	        highScoreLabel.setLayoutY(10);


	        BorderPane mainLayout = new BorderPane();
	        mainLayout.setCenter(gamePane);

	        Pane topBar = new Pane(); // Use a Pane to place labels at specific coordinates
	        topBar.setPrefHeight(30); // Give it some height
	        topBar.getChildren().addAll(scoreLabel, highScoreLabel);
	        mainLayout.setTop(topBar);

	        Scene gameScene = new Scene(mainLayout);
	        gameScene.setOnKeyPressed(e -> {
	            if (e.getCode() == KeyCode.UP && direction != Direction.DOWN) {
	                direction = Direction.UP;
	            } else if (e.getCode() == KeyCode.DOWN && direction != Direction.UP) {
	                direction = Direction.DOWN;
	            } else if (e.getCode() == KeyCode.LEFT && direction != Direction.RIGHT) {
	                direction = Direction.LEFT;
	            } else if (e.getCode() == KeyCode.RIGHT && direction != Direction.LEFT) {
	                direction = Direction.RIGHT;
	            } else if (e.getCode() == KeyCode.R && gameOver) { // Restart on 'R' key
	                startGame();
	            }
	        });

	        primaryStage.setScene(gameScene);
	        primaryStage.setTitle("Snake Game - In Progress");

	        placeFood();
	        gameLoop.start(); // Start the game animation
	    }

	    private void placeFood() {
	        int foodX, foodY;
	        do {
	            foodX = random.nextInt(WIDTH);
	            foodY = random.nextInt(HEIGHT);
	            food = new Point(foodX, foodY);
	        } while (snake.contains(food)); // Ensure food doesn't spawn on the snake
	    }

	    private void updateGame() {
	        if (gameOver) return;

	        // Move the snake's head
	        Point head = snake.get(0);
	        int newHeadX = head.x;
	        int newHeadY = head.y;

	        switch (direction) {
	            case UP:
	                newHeadY--;
	                break;
	            case DOWN:
	                newHeadY++;
	                break;
	            case LEFT:
	                newHeadX--;
	                break;
	            case RIGHT:
	                newHeadX++;
	                break;
	        }

	        Point newHead = new Point(newHeadX, newHeadY);

	        // Check for collisions
	        if (newHeadX < 0 || newHeadX >= WIDTH || newHeadY < 0 || newHeadY >= HEIGHT || snake.contains(newHead)) {
	            endGame();
	            return;
	        }

	        // Add new head
	        snake.add(0, newHead);

	        // Check if food is eaten
	        if (newHead.equals(food)) {
	            score++;
	            scoreLabel.setText("Score: " + score);
	            placeFood(); // Place new food
	        } else {
	            // Remove tail if no food eaten (normal movement)
	            snake.remove(snake.size() -1);
	        }

	        drawGame(); // Redraw the game
	    }

	    private void drawGame() {
	        gamePane.getChildren().clear(); // Clear previous drawings

	        // Draw food
	        Rectangle foodRect = new Rectangle(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
	        foodRect.setFill(Color.RED);
	        gamePane.getChildren().add(foodRect);

	        // Draw snake
	        for (int i = 0; i < snake.size(); i++) {
	            Point segment = snake.get(i);
	            Rectangle rect = new Rectangle(segment.x * TILE_SIZE, segment.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
	            if (i == 0) { // Head
	                rect.setFill(Color.LIMEGREEN);
	            } else { // Body
	                rect.setFill(Color.GREEN);
	            }
	            gamePane.getChildren().add(rect);
	        }
	    }

	    private void endGame() {
	        gameOver = true;
	        gameLoop.stop(); // Stop the animation timer

	        // Update high score
	        if (score > highScore) {
	            highScore = score;
	            saveHighScore();
	        }

	        Label gameOverLabel = new Label("GAME OVER!\nScore: " + score + "\nHigh Score: " + highScore + "\nPress 'R' to Restart");
	        gameOverLabel.setFont(Font.font("Impact", 40));
	        gameOverLabel.setTextFill(Color.ORANGE);
	        gameOverLabel.setAlignment(Pos.CENTER);
	        // Center the label manually, as BorderPane.setAlignment works better with setTop/Center/Bottom
	        gameOverLabel.setTranslateX((GAME_WIDTH - gameOverLabel.prefWidth(-1)) / 4);
	        gameOverLabel.setTranslateY((GAME_HEIGHT - gameOverLabel.prefHeight(-1)) / 4);


	        gamePane.getChildren().add(gameOverLabel);
	    }

	    private AnimationTimer gameLoop = new AnimationTimer() {
	        @Override
	        public void handle(long now) {
	            // Control game speed (throttle updates to achieve desired FPS)
	            if (now - lastFrameTime >= FRAME_DELAY_NS) {
	                updateGame();
	                lastFrameTime = now;
	            }
	        }
	    };

	    private void loadHighScore() {
	        try (BufferedReader reader = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
	            String line = reader.readLine();
	            if (line != null) {
	                highScore = Integer.parseInt(line);
	            }
	        } catch (IOException | NumberFormatException e) {
	            highScore = 0; // If file doesn't exist or is invalid, high score is 0
	        }
	    }

	    private void saveHighScore() {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
	            writer.write(String.valueOf(highScore));
	        } catch (IOException e) {
	            System.err.println("Error saving high score: " + e.getMessage());
	        }
	    }

	    public static void main(String[] args) {
	        launch(args);
	    }
	}

