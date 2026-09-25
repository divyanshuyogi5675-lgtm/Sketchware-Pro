package pro.sketchware.feature.webviewbuilder;

public class WebViewPageModel {
    public String id;
    public String name;
    public String activityClass;
    public String webviewUrl;
    public boolean isLandingPage;
    public boolean enableJavaScript = true;
    public boolean enableFileDownload = false;
    public boolean hideStatusBar = false;
    public String orientation = "portrait";
    public String navigationTrigger = "none";

    public WebViewPageModel(String id, String name, String url, boolean isLanding) {
        this.id = id;
        this.name = name;
        this.activityClass = name;
        this.webviewUrl = url;
        this.isLandingPage = isLanding;
    }
}

