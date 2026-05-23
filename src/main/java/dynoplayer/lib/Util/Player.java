package dynoplayer.lib.Util;

import java.io.File;

import dynoplayer.lib.Music.Music;
import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Player {
    // Modes on the player
    public enum playModes{
        linear("Linear"),
        single("Single"),
        random("Random");

        private final String name;
        playModes(String name){this.name = name;}
        public String getName(){return this.name;}
    };

    private static MediaPlayer mp;
    private static Music currentTrack;
    private static boolean isPlaying = false;
    private static playModes mode = playModes.linear;

    /**
     * The Method to play MUSIC. I had to switch from .mp3 to .wav because of Ubuntu.
     * @param track
     * @param time
     * @param volume
     * @param next<Runnable> The function to determine the next song.
     */
    public static void play(Music track, Slider time, Slider volume, Runnable next){
        if(mp != null) {
            mp.stop();
            mp.dispose();
        }

        currentTrack = track;

        try {
            File file = new File(dynoplayer.lib.Version.Setting.download_dir, track.getFile());

            if(!file.exists()){
                System.out.println("MISSING AUDIO BRO>>>>>!!???");
                return;
            }


            String URI = "file://" + file.getAbsolutePath();
            Media media = new Media(URI);
            mp = new MediaPlayer(media);

            // Volume
            mp.setVolume(volume.getValue());

            volume.valueProperty().addListener((obs, old, New) -> {
                mp.setVolume(New.doubleValue());
            });

            mp.setOnReady(() ->
                {
                    time.setMin(0);
                    time.setMax(mp.getTotalDuration().toSeconds());
                });
            
            mp.currentTimeProperty().addListener((obs, old, New) -> {
                if(!time.isValueChanging()) {
                    time.setValue(New.toSeconds());
                }
            });

            time.setOnMousePressed(event -> {
                if( mp != null && isPlaying) {
                    mp.pause();
                }
            });

            mp.setOnEndOfMedia(() -> {
                Platform.runLater(next);
            });

            time.setOnMouseReleased(event -> {
                if(mp != null){
                    mp.seek(Duration.seconds(time.getValue()));
                    mp.play();
                }
            });

            mp.play();
            isPlaying = true;
        } catch (Exception e){
            System.out.println("Ubuntu is bugging " + e.getMessage());
        }
    }

    /**
     * CONTROLLER FOR MY APP WINDOW
     */

    /**
     * Controlling the flow state of the media
     */
    public static void togglePlayPause() {
        if (mp == null) return;
        if (isPlaying) {
            mp.pause();
            isPlaying = false;
        } else {
            mp.play();
            isPlaying = true;
        }
    }

    /**
     * Helper function for the program to know if it's playing
     * @return isPlaying
     */
    public static boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Returns the song that is currently playing in the media
     * @return currentTrack
     */
    public static Music getCurrentTrack() {
        return currentTrack;
    }

    /**
     * Cycle the playing mode
     * @return
     */
    public static playModes cycle(){
        playModes[] modes = playModes.values();
        mode = modes[(mode.ordinal() + 1) % modes.length];
        return mode;
    }

    /**
     * Getter method for getting the current mode;
     * @return
     */
    public static playModes getMode(){
        return mode;
    }
}
