package dynoplayer.lib.Music;

public abstract class Playable {

    /**
     * Variable declaration
     */
    protected final long time; // Time in second (Used later if I want to make it part by part).
    protected final String name; // Name
    protected final String author; // Author
    protected final String url; // For YT-DLP implementation
    protected final String image; // IM AGE AS A URLLLLLLLLLLL
    protected final String ID;

    public Playable(long time, String name, String author, String url, String image, String ID){
        this.time = time;
        this.name = name;
        this.author = author;
        this.url = url;
        this.image = image; // Image as a URL
        this.ID = ID;
    }

    public Playable(Playable playable){
        this.time = playable.length();
        this.name = playable.getName();
        this.image = playable.getIMG();
        this.url = playable.getIMG();
        this.author = playable.getAuthor();
        this.ID = playable.getID();
    }

    /**
     * Converts time based on a format
     * @param fmt, format
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
     *============================
     * Straight forward getters ||
     *============================
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

    public String getID(){
        return this.ID;
    }
}
