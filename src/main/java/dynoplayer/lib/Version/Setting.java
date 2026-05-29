package dynoplayer.lib.Version;

import java.io.File;

public class Setting {
    public static final String version = "2.0.7";
    public static final String name = "Dynoplayer";
    public static final String download_dir = System.getProperty("user.home") + File.separator + ".cache" + File.separator + "dynoplayer" + File.separator;
    public static final String Cache = download_dir + ".dynocache";
    public static final String default_img = "/static/image/new-york-skyline-at-night-1476795446tYD.jpg";
    public static final String CSS = "/static/style/style.css";
    public static final String icon = "/static/image/Dynoplayer.ico";
}
