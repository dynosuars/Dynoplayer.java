package dynoplayer.lib.Util;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import dynoplayer.lib.Chrono.Chrono;
import dynoplayer.lib.Music.Music;
import dynoplayer.lib.Music.RawVid;
import dynoplayer.lib.Version.Setting;



public class Util {
        /**
         * The method used to FETCH youtube (API) using a QUERY.
         * @param query<String> query the you search
         * @return music<ArrayList<RawVid>> Returns a UNDOWNLOADED List of 
         */
        public static ArrayList<RawVid> fetch(String query) {
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
                    String id = extract(block, "\"videoId\":\"(.*?)\"");
                    String Vidurl = "https://www.youtube.com/watch?v=" + id;

                    String IMGUrl = extract(block, "\"(https?[^\"]*\\.(?:jpg|jpeg|png|gif|webp)[^\"]*)\"");

                    musics.add(new RawVid(length, title, uploader, Vidurl, IMGUrl, id));
                }
            } else{
                throw new Exception("Youtube may or may not have rate limited you. Idk how fix fr. Wait for me to switch limiter frfr");
            }

        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage() + "\n" + e.getStackTrace());
        }
        return musics;
    }

    /**
     * Save the Cache of a certain raw video.
     * @apiNote I am going to turn this into MUSIC
     * @param video
     */
    public static void saveCache(RawVid video){
        File target = new File(Setting.Cache);
        StringBuilder builder = new StringBuilder();

        // Clean out quotes inside fields to prevent premature string terminations
        String Name = video.getName().replace("\"", "'");
        String Author = video.getAuthor().replace("\"", "'");
        String Img = video.getIMG().replace("\"", "'");

        // Added clean wrapped double quotes (\"%s\") around metadata strings
        String in = String.format("  %s: {\n    name: \"%s\",\n    author: \"%s\",\n    img: \"%s\"\n  }",
            video.getID(), Name, Author, Img
        );

        try {
            if (!target.exists()) {
                if (target.getParentFile() != null) {
                    target.getParentFile().mkdirs();
                }
                builder.append("{\n").append(in).append("\n}");
            } else {
                String existingJson = java.nio.file.Files.readString(target.toPath()).trim();
                
                if (existingJson.startsWith("{") && existingJson.endsWith("}")) {
                    existingJson = existingJson.substring(1, existingJson.length() - 1).trim();
                }

                if (existingJson.contains(video.getID() + ":")) {
                    System.out.println("Track " + video.getID() + " already exists in master cache configuration.");
                    return; 
                }

                builder.append("{\n");
                if (!existingJson.isEmpty()) {
                    builder.append(existingJson).append(",\n");
                }
                builder.append(in).append("\n}");
            }

            java.nio.file.Files.writeString(target.toPath(), builder.toString());
            System.out.println("Cached: " + video.getName());

        } catch (IOException e) {
            System.out.println("Could not update master .dynocache: " + e.getMessage());
        }
    }

    /**
     * Init function that reads from the cache 
     * @param cache
     * @return
     */
    public static ArrayList<Music> init(String cache){
        ArrayList<Music> result = new ArrayList<>();

        if(cache == null || !new File(cache).exists()){
            System.out.println("No cache file exists at destination target, starting as empty");
            return result;
        }

        try {
            final String content = Files.readString(new File(cache).toPath());
            // Group 1: ID, Group 2: Internal metadata properties block
            Pattern blockPattern = Pattern.compile("([\\w\\-]+)\\s*:\\s*\\{([^}]+)\\}");
            Matcher blockMatcher = blockPattern.matcher(content);

            while (blockMatcher.find()) {
                String id = blockMatcher.group(1).trim();
                String innerProperties = blockMatcher.group(2);

                String name = Util.extractDynoProp(innerProperties, "name");
                String author = Util.extractDynoProp(innerProperties, "author");
                String img = Util.extractDynoProp(innerProperties, "img");

                // Check for the .wav assets we set up earlier!
                File wavFile = new File(Setting.download_dir, id + ".wav");
                
                if (wavFile.exists()) {
                    RawVid rebuiltVid = new RawVid(name, author, "https://www.youtube.com/watch?v=" + id, img, id);
                    result.add(new Music(rebuiltVid));
                } else {
                    // Corrected log tracking statement to reflect WAV updates
                    System.out.println(String.format("Skipping cache entry [%s] - physical WAV audio asset missing from cache dir.", id));
                }
            }

        } catch (IOException e){
            System.out.println("Failed reading master dynamic cache file: " + e.getMessage());
        }
        return result;
    }   

    /**
     * A util function used to delete the music 
     * @param music
     * @return
     */
    public static boolean remove(Music music){
        if(music == null) return false;
        boolean result = false;

        if(music.getFile() != null){
            try{
                File file = new File(Setting.download_dir, music.getFile());
                if(file.exists()){
                    result = file.delete();
                }
            } catch( Exception e ){
                System.out.println("Error deleting the .wav file: " + e.getMessage());
            } 
        }

        try{
            File Cache = new File(Setting.Cache);

            if(Cache.exists()){
                String content = Files.readString(Cache.toPath());
                String ID = music.getID();

                String regex = "\\s*" + Pattern.quote(ID) + "\\s*:\\s*\\{[^}]*\\},?\\s*\\n?";
                String updatedContent = content.replaceAll(regex, "").trim();

                updatedContent = updatedContent.replace(",\n}", "\n}").replace("{\n,", "{\n");

                if(updatedContent.equals("{\n}")){
                    updatedContent = "{\n}";
                }

                Files.writeString(Cache.toPath(), updatedContent);
            }
        } catch (IOException e){
            System.out.println("FILE FAILED TO DELETE IDK WHY. E= " + e.getMessage());
        }
        return result;
    }

    /**
     * @param innterContent<String> contents
     * @param key<String> key of the thing u wanna extract
     * regex helper to find individual keys and values
     */
    private static String extractDynoProp(String innerContent, String key) {
        Pattern p = Pattern.compile(key + "\\s*:\\s*\"?(.*?)\"?(?:,\\s*\\n|\\s*\\n|$)");
        Matcher m = p.matcher(innerContent);
        if (m.find()) {
            String value = m.group(1).trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        }
        return "Unknown";
    }

    /**
     * Regex helper to save myself time...
     * @param text
     * @param regex
     * @return
     */
    private static String extract(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

}
