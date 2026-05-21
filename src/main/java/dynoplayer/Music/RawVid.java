package dynoplayer.Music;

public class RawVid {
    private final long time; // Time in second
    private final String name; // Name
    private final String author; // Author
    private final String url; // For YT-DLP implementation
    private final String image; // IMAGE AS A URLLLLLLLLLLL

    public RawVid(long time, String name, String author, String url, String image){
        this.time = time;
        this.name = name;
        this.author = author;
        this.url = url;
        this.image = image; // Image as a URL
    }

    /**
     * Converts time based on a format
     * @param fmt
     * @return
     */
    public String formatTime(String fmt){
        long h = this.time / 3600;
        long m = (this.time % 3600) / 60;
        long s = this.time % 60;

        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        } else {
            return String.format("%d:%02d", m, s);
        }
    }

    /*
     * Straight forward getters
     * @return
     */

    public String getName(){
        return this.name;
    }

    public String getAuthor(){
        return this.author;
    }

    public String Url(){
        return this.url;
    }

    public long length(){
        return this.time;
    }

    public String getIMG(){
        return this.image;
    }


    @Override
    public String toString(){
        return String.format("%s | by: %s | %s", this.name, this.author, this.formatTime(null));
    }

}
