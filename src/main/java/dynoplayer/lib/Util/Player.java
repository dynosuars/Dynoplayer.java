package dynoplayer.lib.Util;

import java.io.File;

import dynoplayer.lib.Music.Music;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Player {
    private static MediaPlayer mp;
    private static Music currentTrack;
    private static boolean isPlaying = false;

    public static void play(Music track, Slider time, Slider volume){
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

            time.setOnMouseReleased(event -> {
                if(mp != null){
                    mp.seek(Duration.seconds(time.getValue()));
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

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static Music getCurrentTrack() {
        return currentTrack;
    }
}
