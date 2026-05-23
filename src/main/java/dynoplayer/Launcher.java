package dynoplayer;

public class Launcher {
    public static void main(String[] args){
        try{

            System.setProperty("jdk.gtk.wm.classname", "dynoplayer");
            System.setProperty("javafx.platform", "linux");
            Main.main(args);
        } catch( Exception e){
            e.printStackTrace();
        }
    }
}
