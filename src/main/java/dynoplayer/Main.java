package dynoplayer;

import java.io.IOException;
import java.util.ArrayList;

import javafx.stage.Stage;

import dynoplayer.lib.Music.Music;
import dynoplayer.lib.Music.RawVid;
import dynoplayer.lib.Util.Player;
import dynoplayer.lib.Util.Util;
import dynoplayer.lib.Version.Setting;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * JavaFX App
 */
public class Main extends Application {

    // Javafx bum variables
    private RawVid select;
    private final ObservableList<RawVid> raw = FXCollections.observableArrayList();
    private final ObservableList<Music> musics = FXCollections.observableArrayList();

    public static void main(String[] args) throws IOException {
        System.setProperty("libav.gstreamer.FFmpeg", "false");
        System.setProperty("javafx.verbose", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        // FAHHHHHHHH I GOTTA ADD A DAMN VERSION CONTROL AND CHECK SUM FOR TS
        stage.setTitle(String.format("%s v%s", Setting.name, Setting.version));

        // Search bar
        // Query used for finding the videos on youtube
        TextField Query = new TextField();
        Query.getStyleClass().add("input");
        Query.setPromptText("Ex: Hot N Cold / 周杰伦");

        Button searchBTN = new Button("Search!");
        searchBTN.getStyleClass().add("btn");
        HBox searchBar = new HBox(10, Query, searchBTN);
        searchBar.setAlignment(Pos.TOP_CENTER);
        searchBar.setPadding(new Insets(10));
        searchBar.setFillHeight(true);

        // List viewer
        Label queue_title = new Label("Queue (Await Download)");
        Label library = new Label("Music library");

        ListView<RawVid> lv = new ListView<>();
        lv.setPrefWidth(240);
        lv.setItems(this.raw);
        // Thank STACK OVERFLOW
        lv.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(RawVid item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s — %s", item.getName(), item.getAuthor()));
                }
            }
        });

        ListView<Music> mLV = new ListView<>();
        mLV.setPrefWidth(240);
        mLV.setItems(this.musics);

        mLV.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Music item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s — %s", item.getName(), item.getAuthor()));
                }
            }
        });

        Button downloadALL = new Button("Download");

        HBox Listviewers = new HBox(10, new VBox(10, queue_title, lv, downloadALL), new VBox(10, library, mLV ));

        VBox left = new VBox(20, Listviewers, downloadALL);


        // Right, the media player

        // Cover
        ImageView cover = new ImageView();
        cover.setFitHeight(250);
        cover.setFitWidth(250);
        cover.setPreserveRatio(true);
        cover.getStyleClass().add("cover");


        // Title
        Label currTitle = new Label("Nullptr");
        currTitle.getStyleClass().add("player-title");
        currTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Author
        Label currAuthor = new Label("Nullptr");
        currAuthor.getStyleClass().add("player-author");
        currAuthor.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        // Volume slider
        Slider volumeSlider = new Slider(0, 1.0, 0.5);
        volumeSlider.setPrefWidth(80);
        volumeSlider.getStyleClass().add("volumer-slider");

        // Time slider
        Slider timeSlider = new Slider(0, 100, 0);
        timeSlider.setPrefWidth(200);
        timeSlider.getStyleClass().add("time-slider");

        HBox slidersRow = new HBox(15, new Label("Time: "), timeSlider, new Label("Vol:"), volumeSlider);
        slidersRow.setAlignment(Pos.CENTER);


        // Bum Btns
        Button lastBTN = new Button(" < ");
        Button playPauseBTN = new Button(" Veron =>");
        Button skipBTN = new Button(" > ");

        lastBTN.getStyleClass().add("control-btn");
        playPauseBTN.getStyleClass().add("control-btn");
        skipBTN.getStyleClass().add("control-btn");

        HBox controlButtonsRow = new HBox(20, lastBTN, playPauseBTN, skipBTN);
        controlButtonsRow.setAlignment(Pos.CENTER);

        VBox right = new VBox(15, cover, currTitle, currAuthor, slidersRow, controlButtonsRow);
        right.setAlignment(Pos.TOP_CENTER);
        right.setPrefWidth(400);
        right.setPadding(new Insets(10));
        right.getStyleClass().add("player-panel");

        HBox TopBar = new HBox(10, new Label("Very good fetcher by Dynosuars"), searchBar);
        VBox root = new VBox(20, TopBar, new HBox(100, left, right));
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        Scene scene = new Scene(root, 1200, 720);

        var cssResource = Main.class.getResource("/static/style/style.css");
        if (cssResource == null) {
            throw new RuntimeException("CRITICAL: style.css not found on the classpath!");
        }
        scene.getStylesheets().add(cssResource.toExternalForm());


        /**
         * BTN DECLARATION
         */

        // Search Button action
        searchBTN.setOnAction(event -> {
            String query = Query.getText();
            new Thread(() -> {
                ArrayList<RawVid> rawVids = Util.fetch(query);
                javafx.application.Platform.runLater(
                    () -> {
                        this.searchWindow(query, rawVids);
                    }
                );
            }).start();
        });

        downloadALL.setOnAction(event -> {

            if (this.raw.isEmpty()) {
                stage.setTitle("Queue is empty. Add some songs to download.");
                return;
            }

            downloadALL.setDisable(true);
            searchBTN.setDisable(true);
            downloadALL.setText("Downloading queue...");

            ArrayList<RawVid> snapshotToDownload = new ArrayList<>(this.raw);

            new Thread(() -> {
                for (RawVid track : snapshotToDownload) {
                    
                    // System.out.println("Processing: " + track.getName());
                    
                    Music.download(track);
                    
                    javafx.application.Platform.runLater(() -> {
                        Music completedTrack = new Music(track);
                        this.musics.add(completedTrack);
                        this.raw.remove(track);
                    });
                }

                javafx.application.Platform.runLater(() -> {
                    downloadALL.setDisable(false);
                    searchBTN.setDisable(false);
                    downloadALL.setText("Download");
                    stage.setTitle(String.format("%s v%s", Setting.name, Setting.version));
                });

            }).start();
        });

        playPauseBTN.setOnAction(event -> {
            Music currentTrack = mLV.getSelectionModel().getSelectedItem();
            if (currentTrack == null) {
                // System.out.println("No song selected to play/pause!");
                return;
            }

            if (Player.isPlaying()) {
                Player.togglePlayPause(); 
                
                playPauseBTN.setText(" Veron =>"); 
            } else {
                Player.togglePlayPause();
                playPauseBTN.setText(" Veron =<");
            }
        });

        skipBTN.setOnAction(event -> {
            int currentIndex = mLV.getSelectionModel().getSelectedIndex();

            if (currentIndex != -1 && currentIndex < this.musics.size() - 1){
                int nextIndex = currentIndex + 1;

                mLV.getSelectionModel().select(nextIndex);
                mLV.scrollTo(nextIndex);

                Music next = mLV.getSelectionModel().getSelectedItem();
                if(next != null) {
                    currTitle.setText(next.getName());
                    currAuthor.setText(next.getAuthor());
                    playPauseBTN.setText("Veron =>");

                    if (next.getIMG() != null && !next.getIMG().isEmpty()){
                        cover.setImage(new Image(next.Url()));
                    }

                    Player.play(next, timeSlider, volumeSlider);
                }
            }
        });

        lastBTN.setOnAction(event -> {
            int currentIndex = mLV.getSelectionModel().getSelectedIndex();

            if (currentIndex > 0){
                int nextIndex = currentIndex - 1;

                mLV.getSelectionModel().select(nextIndex);
                mLV.scrollTo(nextIndex);

                Music next = mLV.getSelectionModel().getSelectedItem();
                if(next != null) {
                    currTitle.setText(next.getName());
                    currAuthor.setText(next.getAuthor());
                    playPauseBTN.setText(" = ");

                    if (next.getIMG() != null && !next.getIMG().isEmpty()){
                        cover.setImage(new Image(next.Url()));
                    }

                    Player.play(next, timeSlider, volumeSlider);
                }
            }
        });

        // Double click play
        mLV.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2){
                Music curr = mLV.getSelectionModel().getSelectedItem();

                if(curr != null){
                    currTitle.setText(curr.getName());
                    currAuthor.setText(curr.getAuthor());
                    playPauseBTN.setText(" = ");
                }

                if (curr.getIMG() != null && !curr.getIMG().isEmpty()){
                    cover.setImage(new Image(curr.getIMG(), true));
                }

                Player.play(curr, timeSlider, volumeSlider);
            }
        });


        //INIT
        this.musics.addAll(Util.init(Setting.Cache));

        stage.setScene(scene);
        stage.show();
    }





    // So sad I can't place them into another file.
    private void searchWindow(String query, ArrayList<RawVid> raw) {

        Stage stage = new Stage();
        stage.setTitle(String.format("%s v%s - Search Results for: %s", Setting.name, Setting.version, query));
        // Error page
        if (query.isEmpty()) {
            stage.setTitle("Dynoplayer - Bum you forgot the query lil bro. Close the window");

            Label root = new Label("BUM LOOK @ TITLE NOW!!!!");
            root.setAlignment(Pos.CENTER);
            Scene scene = new Scene(root, 720, 600);
            stage.setScene(scene);
            stage.show();
            return;
        }


        // Main page


        // Musics
        ListView<String> resultList = new ListView<>();
        resultList.setPrefWidth(480);
        resultList.setPrefHeight(620);

        // Append raw lmao fun part
        for (RawVid curr: raw) {
            resultList.getItems().add(String.format("%s — %s", curr.getName(), curr.getAuthor()));
        }

        Label titleLabel = new Label("Select a result to preview details");
        Label authorLabel = new Label();

        titleLabel.setMaxWidth(500); 
        titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        titleLabel.setWrapText(true);

        ImageView preview = new ImageView();

        preview.setFitWidth(480);
        preview.setFitHeight(280);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);

        VBox detailsPane = new VBox(10, titleLabel, authorLabel, preview);
        detailsPane.setAlignment(Pos.TOP_LEFT);
        detailsPane.setPadding(new Insets(10));
        detailsPane.setMinWidth(500);


        // FAHHHH I HATE JAVA FX. WHY CAN'T I JS WRITE MY OWN FRONTEND BY COMPILING HTML FK TS
        resultList.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int selectedIndex = newIndex.intValue();
            if (selectedIndex >= 0 && selectedIndex < raw.size()) {
                this.select = raw.get(selectedIndex);
                RawVid selected = this.select;
                titleLabel.setText(selected.getName());
                authorLabel.setText("By: " + selected.getAuthor());
                preview.setImage(new Image(selected.getIMG()));
            }
        });



        Label outputText = new Label();
        outputText.setFont(new Font("Hack", 24));
        outputText.setWrapText(true);
        outputText.setMaxWidth(480);



        // Selected view
        ListView<RawVid> selects = new ListView<>();
        selects.setPrefWidth(480);
        selects.setPrefHeight(240);
        selects.setItems(this.raw);
        selects.setCellFactory(param -> new javafx.scene.control.ListCell<>(){
            @Override
            protected void updateItem(RawVid item, boolean empty){
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s — %s", item.getName(), item.getAuthor()));
                }
            }
        });


        ScrollPane scrollSelect = new ScrollPane(selects);
        scrollSelect.setFitToWidth(true);
        scrollSelect.setFitToHeight(true);
        
        VBox outputs = new VBox(10, outputText, scrollSelect);


        Button BTN_ADD = new Button("Add to list");
        Button BTN_RM = new Button("Remove from list");

        HBox BTN_GROUP = new HBox(5, BTN_ADD, BTN_RM);
        VBox right = new VBox(50, detailsPane, BTN_GROUP, outputs);



        // ACTUAL content
        HBox contentBox = new HBox(20, resultList, right);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(20));

        
        VBox root = new VBox(20, contentBox);
        root.setAlignment(Pos.TOP_CENTER);


        /*
         * BTN DEFINITION
         *
        */

        BTN_ADD.setOnAction(event -> {
            if (this.select != null) {
                if (!this.raw.contains(this.select)) {
                    this.raw.add(this.select); 
                    
                    outputText.setText(String.format("Song {%s} added.", this.select.getName()));
                    outputText.setTextFill(Color.GREEN);
                } else {
                    outputText.setText(String.format("Song {%s} exists.", this.select.getName()));
                    outputText.setTextFill(Color.RED);
                }
            } else {
                outputText.setText("No song selected");
                outputText.setTextFill(Color.RED);
            }
        });

        BTN_RM.setOnAction(event -> {
            if (this.select != null) {
                if (this.raw.contains(this.select)) {
                    this.raw.remove(this.select); 
                    
                    outputText.setText(String.format("Song {%s} removed.", this.select.getName()));
                    outputText.setTextFill(Color.GREEN);
                } else {
                    outputText.setText(String.format("Song {%s} doesn't EXIST in your list", this.select.getName()));
                    outputText.setTextFill(Color.RED);
                }
            } else {
                outputText.setText("No song selected to remove");
                outputText.setTextFill(Color.RED);
            }
        });

        // Scroll bar for the root
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 1200, 720);
        stage.setScene(scene);
        stage.show();
    }

}