package chaitasnikolaosjavafx220005;

import java.io.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.animation.*;
import javafx.util.*;
import java.util.*;

public class ChaitasNikolaosJavaFX220005 extends Application {
    
    Stage stage;
    
    private int mistakes = 0;
    private ArrayList<String> words = new ArrayList<>();
    private String chosenWord;
    private int secondsRemaining = 60;
    private Timeline timeline;
    private char[] wordArr;
    private int arrIdx = 0;
    private boolean[] checkArr;
    private int correctClicks = 0;
    private boolean continuityCheck;
    
    
    //Checks if any of the letters of the first and last index exist in between the word
    //so it doesn't skip them during the search process
    private boolean startOrEndExistsInBetween(String word) {
        char[] chWord = word.toCharArray();
        for (int i=1; i < word.length()-1; i++) {
            if (chWord[i] == chWord[0] || chWord[i] == chWord[word.length()-1]) {
                return true;
            }
        }
        return false;
    }
    
    //Counts if there more than 1 of the same letter, so they all get drawn correctly
    private int countSameLetter(String word, char letter) {
        int count = 0;
        char[] chWord = word.toCharArray();
        for (int i = 1; i < word.length()-1; i++) {
            if (chWord[i] == letter) {
                ++count;
            }
        }
        return count;
    }
    
    //Checks if the 1st argument of the method exists in the character array of the 2nd argument
    //Used in the hint event handler
    private static boolean Contains(char ch, char[] arr) {
            boolean containsChar = false;
            for (int j = 0; j < arr.length; j++) {
                if (ch == arr[j]) containsChar = true;
            }
            if (!containsChar) return false;
        return true;
    }
    //Quit app button
    private void btnClose_Clicked() {
        boolean reallyQuit = false;
        reallyQuit = ConfirmationBox.show("Are you sure you want to quit?","Confirmation","Yes","No");
        if (reallyQuit) {
            stage.close();
        }
    }
    //Loads the letters to be initialized in our buttons contained in the Tile Pane
    private char[] loadLetters() {  
        return "ABCDFEGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    }
    //Loads the words from the text file
    private void loadWordsFromFile(File filename) {
        try {
            Scanner scanner = new Scanner(filename);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) 
                    words.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //Initializes the clock for each word. If it runs out of time, it stops the timeline.
    private Label initializeTimer(GraphicsContext gc, TilePane keysPane) {
        Label timerLabel = new Label();
        timerLabel.setFont(Font.font(20));
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), 
                e -> {
                    secondsRemaining--;
                    
                    timerLabel.setText("Time: " + secondsRemaining);
                    
                    if (secondsRemaining <= 0) {
                        for (int i=mistakes;i<=6;++i) {
                            drawBody(gc,keysPane);
                            timeline.stop();
                            secondsRemaining = 60;
                        }
                    }
                }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        return timerLabel;
    }
    //Chooses a word randomly from the ArrayList initialized in loadWordsFromFile()
    private void chooseWord() {
        Random rand = new Random();
        chosenWord = words.get(rand.nextInt(words.size()));
        wordArr = new char[chosenWord.length()];
        wordArr[0] = chosenWord.charAt(0);
        wordArr[chosenWord.length()-1] = chosenWord.charAt(chosenWord.length()-1);
        checkArr = new boolean[chosenWord.length()];
        checkArr[0] = true; checkArr[chosenWord.length()-1] = true;
        continuityCheck = startOrEndExistsInBetween(chosenWord);
    }
    
    //Checks if the whole word has been filled out in time (conditions are evaluated every time a button is pressed)
    private void checkForVictory(TilePane keysPane) {
        boolean check = false;
        for (int i = 0; i < chosenWord.length(); i++) {
            if (checkArr[i]) {
                check = true;
            }
            else {
                check = false;
            }
        }
        if (check && correctClicks == chosenWord.length()-2) {
            timeline.stop();
            MessageBox.show("You won!", "Success!");
            for (Node node : keysPane.getChildren()) {
            keysPane.setDisable(true);
            }
        }
    }
    
    //Draws the appropriate amount of lines for each word
    private void drawLines(GraphicsContext gc) {
        int wordLength = chosenWord.length();
        
        double spacing = 600.0 / (wordLength + 1);
        double lineHeight = 10;
        
        double x = 100;
        double y = 100;
        
        for (int i=0;i<wordLength;i++) {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeLine(x+10,y,x+spacing,y);
            x += spacing;
        }
    }
    
    //Alphabet button event handler
    private void btn_Clicked(TilePane keysPane, GraphicsContext gc) {
        for (Node node : keysPane.getChildren()) {
            if (node instanceof Button) {
            Button button = (Button) node;
            String letter = button.getText();
            button.setOnMouseClicked(e-> {
            if (button.isDisabled()) {
                drawBody(gc,null);
            }
            else {    
            if (chosenWord.contains(letter)) {
                updateCanvasWithLetter(gc, letter, false,continuityCheck,keysPane);
                wordArr[arrIdx] = letter.charAt(0);
                ++correctClicks;
                int countLetter = countSameLetter(chosenWord,letter.charAt(0));
                if (countLetter>1) {
                    correctClicks = correctClicks + (countLetter - 1);
                }
                checkArr[arrIdx] = true;
                ++arrIdx;
            } else {
                drawBody(gc,keysPane);
            }
            }
            button.setDisable(true); //so that the user can't press it again
            checkForVictory(keysPane);
            });
        }
        }
    }
    //Hint buttton event handler 
    private void hintBtn_Clicked(GraphicsContext gc, TilePane keysPane, Button button) {
        Random rand = new Random();
        int random_number = rand.nextInt(chosenWord.length()-2) + 1;
        char[] word = chosenWord.toCharArray();
        char randomLetter = word[random_number];
        checkArr[random_number] = true;
        boolean buttonCheck = true;  //Checks if input came from hint button so it doesn't draw a body part
        if (!Contains(randomLetter,wordArr) && checkArr[random_number]) {
            updateCanvasWithLetter(gc,String.valueOf(randomLetter),buttonCheck, continuityCheck, keysPane);
            int countLetter = countSameLetter(chosenWord,randomLetter);
                if (countLetter>1) {
                correctClicks = correctClicks + (countLetter - 1);
                }
            ++correctClicks;
            checkForVictory(keysPane);
    }
        else {
            hintBtn_Clicked(gc,keysPane,button);
        }
     button.setDisable(true);
    }
    
    //Chooses the next word to be displayed (basically restarts the game after reseting every value that needs to be reseted)
    private void nextBtn_Clicked() {
        correctClicks = 0;
        mistakes = 0;
        arrIdx = 0;
        timeline.stop();
        secondsRemaining = 60;
        for (int i=1;i<chosenWord.length()-1;i++)
            checkArr[i] = false;
        startGame();
    }
    //Prints first and last letter of our chosen word
    private void showFirstAndLastLetter(GraphicsContext gc, String letter) {
        int wordLength = chosenWord.length();
        double spacing = 600.0 / (wordLength + 1); // Canvas width: 800px

        // Draw the first letter
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20)); 
        gc.fillText(chosenWord.substring(0, 1), 65 + spacing, 90); 

        // Draw the last letter
        gc.fillText(chosenWord.substring(wordLength - 1), 65 + wordLength * spacing, 90); 
    }
    //Draws the letters of our word
    private void updateCanvasWithLetter(GraphicsContext gc, String letter, boolean buttonCheck, boolean continuityCheck, TilePane keysPane) {
        if ((chosenWord.charAt(0) == letter.charAt(0) && !buttonCheck && !continuityCheck) || 
                (chosenWord.charAt(chosenWord.length()-1) == letter.charAt(0) && !buttonCheck && !continuityCheck)) {
                drawBody(gc,keysPane);
        }
        
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 1; i < chosenWord.length()-1; i++) {
            if (chosenWord.charAt(i) == letter.charAt(0))
                positions.add(i);
        }
        
        int wordLength = chosenWord.length();
        double spacing = 600 / (wordLength+1);
        
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD,20));
        for (int position : positions) {
            double x = 65 + (position +1) * spacing;
            double y = 90;
            gc.fillText(letter,x,y);
        }
    }
    //Draws the head of the stickman
    private void drawHead(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(260,150,80,80);
    }
    //Draws the torso of the stickman
    private void drawTorso(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(300,230,300,380);
    }
    //Draws the left arm of the stickman
    private void drawLeftArm(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(300,260,240,310);
    }
    //Draws the right arm of the stickman
    private void drawRightArm(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(300,260,360,310);
    }
    //Draws the left leg of the stickman
    private void drawLeftLeg(GraphicsContext gc) {
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(2);
    gc.strokeLine(300, 380, 240, 470);
    }
    //Draws the right leg of the stickman
    private void drawRightLeg(GraphicsContext gc) {
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(2);
    gc.strokeLine(300, 380, 360, 470); 
    }
    
    //Selects the correct body part to be drawn based on the mistakes variable initialized as a class member
    private void drawBody(GraphicsContext gc, TilePane keysPane) {
        ++mistakes;
        switch (mistakes) {
            case 1:
                drawHead(gc);
                break;
            case 2:
                drawTorso(gc);
                break;
            case 3:
                drawLeftArm(gc);
                break;
            case 4:
                drawRightArm(gc);
                break;
            case 5:
                drawLeftLeg(gc);
                break;
            case 6:
                timeline.stop();
                for (Node node : keysPane.getChildren()) {
                    keysPane.setDisable(true);
                }
                drawRightLeg(gc);
                MessageBox.show("You lost!", "Loss");
                break;
            default:
        }
    }   
    //Starts the game, initializes background, word files and the whole scenery (buttons, panes, etc.)
    private void playGame() throws FileNotFoundException {
        
        File words = new File("./src/chaitasnikolaosjavafx220005/words.txt");
         
        loadWordsFromFile(words);
        //Game Screen
        Canvas gameCanvas = new Canvas(800,500);
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        FileInputStream input =
        new FileInputStream("./src/chaitasnikolaosjavafx220005/image.jpeg");
        Image image = new Image(input);
        gc.drawImage(image, 0, 0,  gameCanvas.getWidth(), gameCanvas.getHeight());
         
        
        //Letters panel
        char[] keys = loadLetters();
        TilePane keysPane = new TilePane();
        keysPane.setMaxWidth(600);
        keysPane.setMaxHeight(100);
        keysPane.setPadding(new Insets(10));
        keysPane.setHgap(5);
        keysPane.setVgap(5);
        
        for (int i = 0; i<keys.length;i++) {
            Button button = new Button(String.valueOf(keys[i]));
            keysPane.getChildren().add(button);
            //add functionality for the buttons
            button.setOnAction(e->btn_Clicked(keysPane,gc));
            button.setPrefSize(40,40);
        }
        
        //Close and Hint buttons
        Button closeBtn = new Button("Quit");
        closeBtn.setOnAction(e->btnClose_Clicked());
        Button hintBtn = new Button("Hint");
        hintBtn.setOnAction(e -> hintBtn_Clicked(gc,keysPane,hintBtn));
        Button nextWordBtn = new Button("Next Word");
        nextWordBtn.setOnAction(e -> nextBtn_Clicked());
        
        final Label timerLabel = initializeTimer(gc,keysPane);
        timerLabel.setPadding(new Insets (10));
        
        HBox btnPane = new HBox();
        btnPane.getChildren().addAll(nextWordBtn,hintBtn,closeBtn);
        btnPane.setAlignment(Pos.BOTTOM_RIGHT);
        btnPane.setPadding(new Insets(10,10,10,10));
        btnPane.setSpacing(10);
        
        
        chooseWord();
        drawLines(gc);
        showFirstAndLastLetter(gc,chosenWord);
        
        StackPane root = new StackPane();
        StackPane mainPane = new StackPane();        
        mainPane.getChildren().addAll(gameCanvas,btnPane,keysPane, timerLabel);
        mainPane.setAlignment(gameCanvas,Pos.TOP_CENTER);
        mainPane.setAlignment(btnPane,Pos.BOTTOM_RIGHT);
        mainPane.setAlignment(keysPane,Pos.BOTTOM_LEFT);
        mainPane.setAlignment(timerLabel,Pos.TOP_LEFT);
        root.getChildren().addAll(mainPane);        
        
        Scene scene = new Scene(root,800,600);
        
        stage.setTitle("Hangman Game");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    //If the word text file is not found, game does not start and exception is thrown
    private void startGame() {
        try {
            playGame();
        }
        catch (FileNotFoundException e) {
            System.out.println("Words or background could not be found!");
            e.printStackTrace();
        }
    }
    
    //Initializes menu screen. Allows user to either choose to play the game or quit
    @Override
    public void start(Stage primaryStage) {
        
        Label welcomeLabel = new Label("Welcome to the Hangman game!");
        Button playButton = new Button("Play game");
        playButton.setOnAction(e -> startGame());
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> btnClose_Clicked());
        
        welcomeLabel.setAlignment(Pos.CENTER);
        
        VBox menuPane = new VBox();
        menuPane.getChildren().addAll(welcomeLabel,playButton,quitButton);
        menuPane.setAlignment(Pos.CENTER);
        menuPane.setPadding(new Insets(10,10,10,10));
        menuPane.setSpacing(10);
        
        Scene scene = new Scene(menuPane,400,300);
        
        stage = primaryStage;
        
        stage.setTitle("Hangman Game");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();    
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
