package pro.sketchware.feature.webviewbuilder;

import java.util.ArrayList;
import java.util.List;

public class WebViewProjectModel {
    public String appName = "";
    public String packageName = "";
    public String versionName = "1.0";
    public int versionCode = 1;
    public int minSdk = 21;
    public String iconPath = "";
    public List<WebViewPageModel> pages = new ArrayList<>();
    public List<String> permissions = new ArrayList<>();
    public String customCode = "";

    public WebViewProjectModel() {
        permissions.add("android.permission.INTERNET");
        pages.add(new WebViewPageModel("page_1", "MainActivity", "https://", true));
    }

    public static String generatePackageName(String appName) {
        String slug = appName.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
        if (slug.isEmpty()) slug = "myapp";
        return "com." + slug + ".app";
    }
}

