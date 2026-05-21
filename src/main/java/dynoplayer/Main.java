package dynoplayer;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javafx.stage.Stage;

import dynoplayer.Chrono.Chrono;
import dynoplayer.Music.RawVid;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
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


    /**
     * Running Vars
     */

    private final static String VERSION = "0.0.67";
    private final static String TITLE = "Dynoplayer";
    private final static String AUTHOR = "Dyno";

    // IGNORE THE THINGS ABOVE ^^^^

    TextField Query = new TextField();
    private RawVid selectedVideo;
    private final ArrayList<RawVid> selected = new ArrayList<>();

    

    public RawVid getSelectedVideo() {
        return selectedVideo;
    }

    public static void main(String[] args) throws IOException {
        // Change this to whatever you want to search

        launch(args);

        // Early debug please neglect and continue on after the */
        /*
         * Scanner keys = new Scanner(System.in);
         * do{
         * System.out.print("Enter search query: ");
         * String content = keys.nextLine();
         * 
         * if(content.equals("")){
         * break;
         * }
         * try{
         * ArrayList<RawVid> vids = searchAndDisplay(content);
         * for(int i=0; i < 10; i ++){
         * System.out.println(Integer.toString(i + 1) + ". " + vids.get(i));
         * }
         * 
         * System.out.print("Which video do you wanna download: ");
         * content = keys.nextLine();
         * 
         * Download(vids.get(Integer.parseInt(content) - 1));
         * } catch(Exception e){
         * System.out.println(e.getStackTrace());
         * }
         * 
         * } while(true);
         */
    }

    @Override
    public void start(Stage stage) {

        // FAHHHHHHHH I GOTTA ADD A DAMN VERSION CONTROL AND CHECK SUM FOR TS
        stage.setTitle(String.format("%s v%s | By: %s", TITLE, VERSION, AUTHOR));

        // Search bar
        this.Query.getStyleClass().add("input");
        this.Query.setPromptText("Ex: Hot N Cold / 周杰伦");
        this.Query.setMinWidth(300);
        this.Query.setFont(Font.font("Noto Sans CJK SC", 16));
        this.Query.setMinHeight(24);

        Button searchBTN = new Button("Search!");
        searchBTN.getStyleClass().add("btn");
        HBox searchBar = new HBox(10, this.Query, searchBTN);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(10));
        searchBar.setFillHeight(true);

        // Search Button action
        searchBTN.setOnAction(event -> this.searchWindow(this.Query.getText()));

        VBox root = new VBox(20, searchBar);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        Scene scene = new Scene(root, 1200, 720);

        scene.getStylesheets().add(getClass().getResource("static/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static ArrayList<RawVid> searchAndDisplay(String query) {
        ArrayList<RawVid> musics = new ArrayList<>();

        // FAHHHHHHHH I HAVE TO SEARCH THIS UP BRO. W STACK OVERFLOW
        String ENquery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://www.youtube.com/results?search_query=" + ENquery;

        try {
            // Stack overflow PLEASE save me from this hot internet world wide web mess.
            // Every bum wbesite should have a API :pray:
            // Get the raw file, I ain't tryna deal with allat; lwk js search
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    // Only Chinese and English support cuz I only know these 2 languages
                    .header("Accept-Language", "zh-CN,zh-TW;q=0.9,en-US;q=0.8,en;q=0.7")
                    .get();

            Matcher dataMatcher = Pattern.compile("var ytInitialData = (\\{.*?\\});").matcher(doc.html());

            if (dataMatcher.find()) {
                String jsonData = dataMatcher.group(1);
                String[] videoBlocks = jsonData.split("\"videoRenderer\":\\{");

                /**
                 * DEBUGGER
                 * System.out.printf("%-50s | %-20s%n", "Video Title", "Uploader");
                 * System.out.println("=".repeat(75));
                 */

                int count = 0;
                // index 0 is all the website header garbage before the first video
                for (int i = 1; i < videoBlocks.length && count < 20; i++) {
                    String block = videoBlocks[i];

                    // Extract Title
                    String title = extract(block, "\"title\":\\{\"runs\":\\[\\{\"text\":\"(.*?)\"");

                    // Extract Uploader
                    String uploader = extract(block, "\"ownerText\":\\{\"runs\":\\[\\{\"text\":\"(.*?)\"") == null
                            ? extract(block, "\"longBylineText\":\\{\"runs\":\\[\\{\"text\":\"(.*?)\"")
                            : extract(block, "\"ownerText\":\\{\"runs\":\\[\\{\"text\":\"(.*?)\"");

                    // Extract Length

                    String time = extract(block, "\"lengthText\":\\{.*?\"simpleText\":\"(.*?)\"");
                    if (time == null)
                        continue; // How the hell does live even get into here
                    long length = Chrono.time_cast(time);
                    String Vidurl = "https://www.youtube.com/watch?v=" + extract(block, "\"videoId\":\"(.*?)\"");

                    String IMGUrl = extract(block, "\"(https?[^\"]*\\.(?:jpg|jpeg|png|gif|webp)[^\"]*)\"");
                    //System.out.println(IMGUrl);

                    musics.add(new RawVid(length, title, uploader, Vidurl, IMGUrl));

                    // Debugger display part
                    /*
                     * if (title != null && uploader != null) {
                     * title = title.replace("\\u0026", "&");
                     * uploader = uploader.replace("\\u0026", "&");
                     * 
                     * String displayTitle = title.length() > 47 ? title.substring(0, 44) + "..." :
                     * title;
                     * System.out.printf("%-50s | %-20s%n", displayTitle, uploader);
                     * count++;
                     * }
                     */
                }
            } else
                throw new Exception("Youtube may or may not have rate limited you. Idk how fix fr");

        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage() + "\n" + e.getStackTrace());
            System.exit(1);
        }
        return musics;
    }

    // helper func to match the simpler regex
    private static String extract(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Helper function for now. I will replace ts later
     * 
     * @param vid
     * @throws IOException
     */
    private static void Download(RawVid vid) throws IOException {
        ProcessBuilder downloader = new ProcessBuilder("yt-dlp", "-x", "--audio-format", "mp3", "--audio-quality", "0",
                "--embed-thumbnail", "--embed-metadata", "-P",
                "/mnt/5C6E956E6E954226/Java/Shared/JavaFX/player/src/main/cache", vid.Url());
        downloader.start();
    }

    private void searchWindow(String query) {

        // Error page
        if (query.isEmpty()) {
            Stage stage = new Stage();
            stage.setTitle("Dynoplayer - Bum you forgot the query lil bro. Close the window");

            Label root = new Label("BUM LOOK @ TITLE NOW!!!!");
            root.setAlignment(Pos.CENTER);
            Scene scene = new Scene(root, 720, 600);
            stage.setScene(scene);
            stage.show();
            return;
        }


        // Main page
        Stage stage = new Stage();
        stage.setTitle(String.format("%s v%s - Search Results for: %s", TITLE, VERSION, query));


        // Musics
        ArrayList<RawVid> musics = searchAndDisplay(query);
        ListView<String> resultList = new ListView<>();
        resultList.setPrefWidth(480);
        resultList.setPrefHeight(620);

        // Append musics lmao fun part
        for (RawVid curr: musics) {
            resultList.getItems().add(String.format("%s — %s", curr.getName(), curr.getAuthor()));
        }

        Label titleLabel = new Label("Select a result to preview details");
        titleLabel.setWrapText(true);
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
            if (selectedIndex >= 0 && selectedIndex < musics.size()) {
                this.selectedVideo = musics.get(selectedIndex);
                RawVid selected = this.selectedVideo;
                titleLabel.setText(selected.getName());
                authorLabel.setText("By: " + selected.getAuthor());
                preview.setImage(new Image(selected.getIMG()));
            }
        });


        // Bum ass BTN
        Button BTN_ADD = new Button("Add to list");
        Button BTN_RM = new Button("Remove from list");


        Label outputText = new Label();

        ListView<String> selects = new ListView<>();

        for (RawVid curr : this.selected){
            selects.getItems().add(String.format("%s — %s", curr.getName(), curr.getAuthor()));
        }

        selects.setPrefWidth(480);
        selects.setPrefHeight(240);
        ScrollPane scrollSelect = new ScrollPane(selects);
        scrollSelect.setFitToWidth(true);
        scrollSelect.setFitToHeight(true);
        

        VBox outputs = new VBox(5, outputText, scrollSelect);


        HBox BTN_GROUP = new HBox(5, BTN_ADD, BTN_RM);
        VBox right = new VBox(50, detailsPane, BTN_GROUP, outputs);



        // ACTUAL content
        HBox contentBox = new HBox(20, resultList, right);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(20));

        
        VBox root = new VBox(20, contentBox);
        root.setAlignment(Pos.TOP_CENTER);


        /**
         * BTN DEFINITION
         */


        BTN_ADD.setOnAction(event -> {
            // Bro I am not writing js. Why is js so similar to java.
            if(this.selectedVideo != null){
                if(!this.selected.contains(this.selectedVideo)){
                    this.selected.add(this.selectedVideo);
                    outputText.setText(String.format("Song {%s} added.", this.selectedVideo.getName()));
                    outputText.setTextFill(Color.GREEN);
                    selects.getItems().add(String.format("%s — %s", this.selectedVideo.getName(), this.selectedVideo.getAuthor()));
                } else{
                    outputText.setText(String.format("Song {%s} exists.", this.selectedVideo.getName()));
                    outputText.setTextFill(Color.RED);
                }
            }
        });






        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 1200, 720);
        stage.setScene(scene);
        stage.show();
    }

}