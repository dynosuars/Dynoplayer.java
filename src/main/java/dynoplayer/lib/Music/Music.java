package dynoplayer.lib.Music;

import java.io.File;
import java.io.IOException;

import dynoplayer.lib.Util.Util;
import dynoplayer.lib.Version.Setting;

public class Music extends Playable {

    private final String file;
    /*
     * Plans for the future:
     * Add a count variable for the dice.
     * MAYBE A MEDIA file
     */

    public Music(RawVid video) {
        super(video);
        this.file = video.getID() + ".wav";
    }

    private static String resolveYtDlpExecutable() {
        File bundledWindowsExecutable = new File("bin", "yt-dlp.exe");
        if (bundledWindowsExecutable.exists()) {
            return bundledWindowsExecutable.getPath();
        }

        File bundledUnixExecutable = new File("bin", "yt-dlp");
        if (bundledUnixExecutable.exists()) {
            return bundledUnixExecutable.getPath();
        }

        return "yt-dlp";
    }

    /**
     * The "FUNCTION" used to download video. This is static because I lowkey don't
     * know why.
     * 
     * @param video
     * @return
     */
    public static int download(RawVid video) {
        // This command line is damn long.
        try {
            File downloadDir = new File(Setting.download_dir);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }

            File expectedAudio = new File(downloadDir, video.getID() + ".wav");

            ProcessBuilder downloader = new ProcessBuilder(
                    resolveYtDlpExecutable(), "-x",
                    "--audio-format", "wav",
                    "--audio-quality", "0",
                    "-P",
                    Setting.download_dir,
                    "-o", video.getID() + ".%(ext)s",
                    video.Url());

            downloader.redirectErrorStream(true);
            Process process = downloader.start();

            int code = process.waitFor();

            if (code == 0 || expectedAudio.exists()) {
                Util.saveCache(video);
                return 0;
            } else {
                System.out.println("TWIN, YOUR SHIT IS NOT DOWNLOADED " + code);
                return code;
            }

        } catch (IOException e) {
            System.out.println("Man what the hell, Youtube prob rate limited you. Error=" + e.getMessage());
            e.printStackTrace();
            return -1;
        } catch (InterruptedException e) {
            System.out.println("MAN WHAT THE HELL MAN I CAN'T JUST STOP YT-DLP!!!!");
            Thread.currentThread().interrupt();
            return -2;
        }
    }

    /*
     * ============================
     * Straight forward getters ||
     * ============================
     */

    public String getFile() {
        return this.file;
    }

    public File getMusic() {
        return new File(Setting.Cache + this.file);
    }
}
