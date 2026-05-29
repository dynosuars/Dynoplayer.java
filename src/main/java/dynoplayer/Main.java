package dynoplayer;

import java.io.IOException;
import java.util.ArrayList;

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
import javafx.stage.Stage;

public class Main extends Application {

    // I wish I had pointers.
    private RawVid select;
    private final ObservableList<RawVid> raw = FXCollections.observableArrayList();
    private final ObservableList<Music> musics = FXCollections.observableArrayList();

    // Implement javafx components
    private ListView<Music> mLV;
    private Label currTitle;
    private Label currAuthor;
    private ImageView cover;
    private Button playPauseBTN;
    private Slider timeSlider;
    private Slider volumeSlider;
    private Button modeBTN;
    // Labels to display yt-dlp output info
    private Label ytDlpDirLabel;
    private Label ytDlpCodeLabel;

    public static void main(String[] args) throws IOException {
        // System.setProperty("libav.gstreamer.FFmpeg", "false");
        // System.setProperty("javafx.verbose", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle(String.format("%s v%s", Setting.name, Setting.version));

        TextField Query = new TextField();
        Query.getStyleClass().add("input");
        Query.setPromptText("Ex: Hot N Cold / 周杰伦");
        Query.setPrefWidth(450);
        Query.setPrefHeight(44);

        // Holy bro I AM gonna use a emoji
        Button searchBTN = new Button("🔍");
        searchBTN.getStyleClass().add("search-btn");

        HBox searchBar = new HBox(10, Query, searchBTN);
        searchBar.setAlignment(Pos.TOP_CENTER);
        searchBar.setPadding(new Insets(15, 0, 15, 0));
        searchBar.setPadding(new Insets(10));
        searchBar.setFillHeight(true);

        Label queue_title = new Label("Queue (Await Download)");
        Label library = new Label("Music library");

        ListView<RawVid> lv = new ListView<>();
        lv.setPrefWidth(320);
        lv.setItems(this.raw);
        lv.setCellFactory(param -> new javafx.scene.control.ListCell<RawVid>() {
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

        // mLV, the music ListView that is SAVED
        mLV = new ListView<>();
        mLV.setPrefWidth(320);
        mLV.setItems(this.musics);
        mLV.setCellFactory(param -> new javafx.scene.control.ListCell<Music>() {
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

        Button downloadALL = new Button("⬇");
        downloadALL.getStyleClass().add("download-btn");
        Button removeBTN = new Button("Remove");

        VBox queue = new VBox(10, queue_title, lv, downloadALL);
        VBox lib = new VBox(10, library, mLV);
        HBox Listviewers = new HBox(20, queue, lib);
        VBox left = new VBox(20, Listviewers);

        Listviewers.setAlignment(Pos.TOP_CENTER);

        cover = new ImageView();
        cover.setFitHeight(250);
        cover.setFitWidth(250);
        cover.setPreserveRatio(true);
        cover.getStyleClass().add("cover");

        currTitle = new Label("Nullptr");
        currTitle.getStyleClass().add("player-title");
        currTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        currTitle.setId("currTitle");

        currAuthor = new Label("Nullptr");
        currAuthor.getStyleClass().add("player-author");
        currAuthor.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
        currAuthor.setId("currAuthor");

        volumeSlider = new Slider(0, 1.0, 0.5);
        volumeSlider.setPrefWidth(100);
        volumeSlider.getStyleClass().add("volumer-slider");

        timeSlider = new Slider(0, 100, 0);
        timeSlider.setPrefWidth(250);
        timeSlider.getStyleClass().add("time-slider");

        Label timeTitle = new Label("Time:");
        Label volTitle = new Label("Vol:");

        timeTitle.setMinWidth(Label.USE_PREF_SIZE);
        volTitle.setMinWidth(Label.USE_PREF_SIZE);

        HBox slidersRow = new HBox(15, timeTitle, timeSlider, volTitle, volumeSlider);
        slidersRow.setAlignment(Pos.CENTER);
        slidersRow.setPadding(new Insets(10, 0, 10, 0));

        Button lastBTN = new Button(" ◀ ");
        playPauseBTN = new Button(" ❚❚ ");
        Button skipBTN = new Button(" ▶ ");
        modeBTN = new Button("Mode: Linear");

        lastBTN.getStyleClass().add("control-btn");
        playPauseBTN.getStyleClass().add("control-btn");
        skipBTN.getStyleClass().add("control-btn");
        modeBTN.getStyleClass().add("control-btn");

        removeBTN.setId("delete-btn");

        HBox utilRow = new HBox(15, modeBTN, removeBTN);
        HBox controls = new HBox(15, lastBTN, playPauseBTN, skipBTN);
        HBox ytInfoRow = new HBox(10);

        utilRow.setAlignment(Pos.CENTER);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-alignment: center; -fx-padding: 10px 0 0 0;");

        ytDlpDirLabel = new Label("yt-dlp dir: " + Setting.download_dir);
        ytDlpDirLabel.setTextFill(Color.GRAY);
        ytDlpDirLabel.setWrapText(true);
        ytDlpDirLabel.setMaxWidth(220);

        ytDlpCodeLabel = new Label("yt-dlp exit: N/A");
        ytDlpCodeLabel.setTextFill(Color.GRAY);
        ytDlpCodeLabel.setWrapText(true);
        ytDlpCodeLabel.setMaxWidth(220);

        ytInfoRow.getChildren().addAll(ytDlpDirLabel, ytDlpCodeLabel);
        ytInfoRow.setAlignment(Pos.CENTER);

        VBox right = new VBox(20, cover, currTitle, currAuthor, slidersRow, controls, utilRow, ytInfoRow);
        right.setAlignment(Pos.TOP_CENTER);
        right.setPrefWidth(480);
        right.setMinWidth(480);
        right.setPadding(new Insets(20));
        right.getStyleClass().add("player-panel");

        HBox Split = new HBox(40, left, right);
        HBox TopBar = new HBox(10, new Label("Very good fetcher by Dynosuars"), searchBar);

        Split.setAlignment(Pos.CENTER);
        TopBar.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, TopBar, new HBox(100, TopBar, Split));

        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-padding: 20px;");
        Scene scene = new Scene(root, 1200, 720);

        scene.getStylesheets().add(Setting.class.getResource(Setting.CSS).toExternalForm());

        /*
         * EVENT HANDLERS
         */
        searchBTN.setOnAction(event -> {
            String query = Query.getText();
            new Thread(() -> {
                ArrayList<RawVid> rawVids = Util.fetch(query);
                javafx.application.Platform.runLater(() -> this.searchWindow(query, rawVids));
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
                    int code = Music.download(track);
                    javafx.application.Platform.runLater(() -> {
                        if (code == 0) {
                            Music completedTrack = new Music(track);
                            this.musics.add(completedTrack);
                            this.raw.remove(track);
                        }

                        if (this.ytDlpDirLabel != null) {
                            this.ytDlpDirLabel.setText("yt-dlp dir: " + dynoplayer.lib.Version.Setting.download_dir);
                        }
                        if (this.ytDlpCodeLabel != null) {
                            this.ytDlpCodeLabel.setText("yt-dlp exit: " + code);
                            this.ytDlpCodeLabel.setTextFill(code == 0 ? Color.web("#1DB954") : Color.web("#FF6666"));
                        }
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

        removeBTN.setOnAction(event -> {
            Music curr = mLV.getSelectionModel().getSelectedItem();

            if (curr != null) {

                if (Player.isPlaying() && curr.equals(mLV.getSelectionModel().getSelectedItem())) {
                    Player.togglePlayPause();
                    currTitle.setText("Nullptr");
                    currAuthor.setText("Nullptr");
                    cover.setImage(null);
                    timeSlider.setValue(0);
                }

                Util.remove(curr);
                this.musics.remove(curr);
            }
        });

        // Mode Switching Action
        modeBTN.setOnAction(event -> {
            Player.playModes newMode = Player.cycle();
            modeBTN.setText("Mode: " + newMode.getName());
        });

        playPauseBTN.setOnAction(event -> {
            Music currentTrack = mLV.getSelectionModel().getSelectedItem();
            if (currentTrack == null)
                return;

            Player.togglePlayPause();
            if (Player.isPlaying()) {
                playPauseBTN.setText("❚❚");
            } else {
                playPauseBTN.setText("►");
            }
        });

        skipBTN.setOnAction(event -> {
            Next();
        });

        lastBTN.setOnAction(event -> {
            int currentIndex = mLV.getSelectionModel().getSelectedIndex();
            if (currentIndex > 0) {
                int nextIndex = currentIndex - 1;
                playTrackByIndex(nextIndex);
            }
        });

        mLV.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int currentIndex = mLV.getSelectionModel().getSelectedIndex();
                if (currentIndex != -1) {
                    playTrackByIndex(currentIndex);
                }
            }
        });

        this.musics.addAll(Util.init(Setting.Cache));

        try {
            var imageStream = Setting.class.getResourceAsStream(Setting.default_img);
            if (imageStream != null) {
                Image backgroundImage = new Image(imageStream);

                stage.getIcons().add(backgroundImage);
            } else {
                System.out.println("Could not load default background asset from inside the JAR.");
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Failed to assign window icon: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Unified method to load layout parameters and run tracks
     */
    private void playTrackByIndex(int index) {
        mLV.getSelectionModel().select(index);
        mLV.scrollTo(index);

        Music track = mLV.getSelectionModel().getSelectedItem();
        if (track != null) {
            currTitle.setText(track.getName());
            currAuthor.setText(track.getAuthor());
            playPauseBTN.setText("❚❚");

            if (track.getIMG() != null && !track.getIMG().isEmpty()) {
                cover.setImage(new Image(track.getIMG(), true));
            }

            // Passes our method down to handle track transitions automatically
            Player.play(track, timeSlider, volumeSlider, this::Next);
        }
    }

    /**
     * Algorithmic loop that handles playback transitions when songs end
     */
    private void Next() {
        int currentIndex = mLV.getSelectionModel().getSelectedIndex();
        int totalSongs = mLV.getItems().size();

        if (totalSongs == 0 || currentIndex == -1)
            return;

        int nextIndex = currentIndex;

        switch (Player.getMode()) {
            case single:
                nextIndex = currentIndex;
                break;
            case random:
                if (totalSongs > 1) {
                    int rand;
                    do {
                        rand = (int) (Math.random() * totalSongs);
                    } while (rand == currentIndex);
                    nextIndex = rand;
                }
                break;
            case linear:
                nextIndex = (currentIndex + 1) % totalSongs;
                break;
        }

        playTrackByIndex(nextIndex);
    }

    /**
     * Secondary window.
     * 
     * @param query
     * @param raw
     */
    private void searchWindow(String query, ArrayList<RawVid> raw) {
        Stage stage = new Stage();
        stage.setTitle(String.format("%s v%s - Search Results for: %s", Setting.name, Setting.version, query));

        // Error handling page
        if (query.isEmpty()) {
            stage.setTitle("Dynoplayer - Query missing");
            Label errorLabel = new Label("You forgot to enter a search query!");
            errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #FF5555; -fx-font-weight: bold;");
            VBox errorRoot = new VBox(errorLabel);
            errorRoot.setStyle("-fx-background-color: #121212; -fx-alignment: center;");
            Scene scene = new Scene(errorRoot, 500, 300);
            stage.setScene(scene);
            stage.show();
            return;
        }

        // Left Side: Search results List View
        ListView<String> resultList = new ListView<>();
        resultList.getStyleClass().add("search-list");
        resultList.setPrefWidth(450);
        resultList.setPrefHeight(640);

        for (RawVid curr : raw) {
            resultList.getItems().add(String.format("%s — %s", curr.getName(), curr.getAuthor()));
        }

        // Right Side Components: Metadata Panel
        Label titleLabel = new Label("Select a track to preview details");
        titleLabel.getStyleClass().add("search-title");
        titleLabel.setMaxWidth(480);
        titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        titleLabel.setWrapText(true);

        Label authorLabel = new Label();
        authorLabel.getStyleClass().add("search-author");

        ImageView preview = new ImageView();
        preview.setFitWidth(480);
        preview.setFitHeight(270); // 16:9 widescreen scaling ratio
        preview.setPreserveRatio(true);
        preview.setSmooth(true);

        // Wrap image preview in a styled container card
        VBox imageCard = new VBox(preview);
        imageCard.setStyle(
                "-fx-background-color: #1C1C1C; -fx-background-radius: 8px; -fx-alignment: center; -fx-min-height: 270px; -fx-max-height: 270px;");

        VBox detailsPane = new VBox(8, titleLabel, authorLabel, imageCard);
        detailsPane.setAlignment(Pos.TOP_LEFT);
        detailsPane.setPrefWidth(480);

        // Status update layout log
        Label outputText = new Label("Waiting for selection...");
        outputText.setId("status-output");
        outputText.setTextFill(Color.GRAY);
        outputText.setWrapText(true);
        outputText.setMaxWidth(480);

        // Selection Queue Preview List Box
        ListView<RawVid> selects = new ListView<>();
        selects.getStyleClass().add("queue-preview-list");
        selects.setPrefWidth(480);
        selects.setPrefHeight(180);
        selects.setItems(this.raw);
        selects.setCellFactory(param -> new javafx.scene.control.ListCell<RawVid>() {
            @Override
            protected void updateItem(RawVid item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Added: %s — %s", item.getName(), item.getAuthor()));
                }
            }
        });

        VBox ytInfoRow = new VBox(10, ytDlpDirLabel, ytDlpCodeLabel);
        ytInfoRow.setAlignment(Pos.CENTER_LEFT);
        VBox outputsContainer = new VBox(10, new Label("Current Download Queue Snapshot:"), selects, outputText,
                ytInfoRow);

        Button BTN_ADD = new Button("Append");
        Button BTN_RM = new Button("Remove");
        Button BTN_DONE = new Button("Done");

        BTN_ADD.getStyleClass().add("control-btn");
        BTN_RM.getStyleClass().add("control-btn");
        BTN_DONE.getStyleClass().add("control-btn");
        BTN_DONE.setStyle("-fx-background-color: #1A365D; -fx-text-fill: #99CCFF; -fx-min-width: 110px;");

        BTN_RM.setStyle("-fx-background-color: #241414; -fx-text-fill: #FF6666;");

        HBox BTN_GROUP = new HBox(15, BTN_ADD, BTN_RM, BTN_DONE);
        BTN_GROUP.setAlignment(Pos.CENTER_LEFT);

        VBox rightLayoutColumn = new VBox(20, detailsPane, BTN_GROUP, outputsContainer);
        rightLayoutColumn.setPrefWidth(480);

        HBox contentBox = new HBox(30, resultList, rightLayoutColumn);
        contentBox.setAlignment(Pos.TOP_LEFT);

        VBox root = new VBox(contentBox);
        root.getStyleClass().add("search-root");

        /*
         * UI INTERACTION HANDLERS
         */
        resultList.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int selectedIndex = newIndex.intValue();
            if (selectedIndex >= 0 && selectedIndex < raw.size()) {
                this.select = raw.get(selectedIndex);
                titleLabel.setText(this.select.getName());
                authorLabel.setText("Channel: " + this.select.getAuthor());

                if (this.select.getIMG() != null && !this.select.getIMG().isEmpty()) {
                    preview.setImage(new Image(this.select.getIMG(), true));
                }
            }
        });

        BTN_ADD.setOnAction(event -> {
            if (this.select != null) {
                if (!this.raw.contains(this.select)) {
                    this.raw.add(this.select);
                    outputText.setText(String.format("Added: %s", this.select.getName()));
                    outputText.setTextFill(Color.web("#1DB954"));
                } else {
                    outputText.setText("Already exist....");
                    outputText.setTextFill(Color.web("#FFCC00"));
                }
            } else {
                outputText.setText("PICK A SONG BRO");
                outputText.setTextFill(Color.web("#FF5555"));
            }
        });

        BTN_RM.setOnAction(event -> {
            if (this.select != null) {
                if (this.raw.contains(this.select)) {
                    this.raw.remove(this.select);
                    outputText.setText(String.format("Removed: %s", this.select.getName()));
                    outputText.setTextFill(Color.web("#FF5555"));
                } else {
                    outputText.setText("Music isn't in your download queue.");
                    outputText.setTextFill(Color.web("#FFCC00"));
                }
            } else {
                outputText.setText("Pick a music to remove.");
                outputText.setTextFill(Color.web("#FF5555"));
            }
        });

        BTN_DONE.setOnAction(event -> stage.close());

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #121212; -fx-background: #121212;");

        Scene scene = new Scene(scrollPane, 1020, 720);
        scene.getStylesheets().add(Setting.class.getResource(Setting.CSS).toExternalForm());

        stage.setScene(scene);
        stage.setOnHidden(e -> stage.close());
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.show();
    }
}