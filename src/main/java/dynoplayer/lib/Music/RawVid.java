package dynoplayer.lib.Music;

public class RawVid extends Playable{

    public RawVid(long time, String name, String author, String url, String image, String ID){
        super(time, name, author, url, image, ID);
    }


    // Weak constructor
    public RawVid(String name, String author, String url, String image, String ID){
        super(67, name, author, url, image, ID);
    }

    public RawVid(Playable playable){
        super(playable);
    }


    @Override
    public String toString(){
        return String.format("%s | by: %s | %s", this.name, this.author, this.formatTime(null));
    }

}
