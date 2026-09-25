package pro.sketchware.feature.webviewbuilder;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WebViewProjectGenerator {
    private final Context context;
    private final WebViewProjectModel project;
    private final String buildDir;

    public WebViewProjectGenerator(Context context, WebViewProjectModel project, String buildDir) {
        this.context = context;
        this.project = project;
        this.buildDir = buildDir;
    }

    public void generate() throws IOException {
        generateMainActivity();
        generateExtraActivities();
        generateLayouts();
        generateManifest();
        generateStrings();
    }

    private void generateManifest() throws IOException {
        StringBuilder permissions = new StringBuilder();
        for (String perm : project.permissions) {
            permissions.append("    <uses-permission android:name=\"").append(perm).append("\"/>\n");
        }
        StringBuilder activities = new StringBuilder();
        for (int i = 1; i < project.pages.size(); i++) {
            WebViewPageModel page = project.pages.get(i);
            activities.append("        <activity\n")
                    .append("            android:name=\".").append(page.activityClass).append("\"\n")
                    .append("            android:screenOrientation=\"").append(page.orientation).append("\"/>\n");
        }
        String manifest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"" + project.packageName + "\">\n\n" + permissions + "\n" +
                "    <application\n" +
                "        android:allowBackup=\"true\"\n" +
                "        android:icon=\"@mipmap/ic_launcher\"\n" +
                "        android:label=\"" + project.appName + "\"\n" +
                "        android:usesCleartextTraffic=\"true\"\n" +
                "        android:theme=\"@style/Theme.AppCompat.Light.NoActionBar\">\n\n" +
                "        <activity\n" +
                "            android:name=\".MainActivity\"\n" +
                "            android:exported=\"true\"\n" +
                "            android:screenOrientation=\"portrait\">\n" +
                "            <intent-filter>\n" +
                "                <action android:name=\"android.intent.action.MAIN\"/>\n" +
                "                <category android:name=\"android.intent.category.LAUNCHER\"/>\n" +
                "            </intent-filter>\n" +
                "        </activity>\n\n" + activities +
                "    </application>\n</manifest>\n";
        writeFile(buildDir + "src/main/AndroidManifest.xml", manifest);
    }

    private void generateMainActivity() throws IOException {
        WebViewPageModel mainPage = project.pages.get(0);
        String nextActivity = project.pages.size() > 1 ? project.pages.get(1).activityClass : null;
        String navCode = "";
        if (nextActivity != null && "button".equals(mainPage.navigationTrigger)) {
            navCode = "\n        findViewById(R.id.btn_next).setOnClickListener(v -> {\n" +
                    "            startActivity(new Intent(this, " + nextActivity + ".class));\n        });\n";
        } else if (nextActivity != null && "auto".equals(mainPage.navigationTrigger)) {
            navCode = "\n        new Handler(Looper.getMainLooper()).postDelayed(() -> {\n" +
                    "            startActivity(new Intent(this, " + nextActivity + ".class));\n        }, 2000);\n";
        }
        writeFile(buildDir + "src/main/java/" + project.packageName.replace(".", "/") + "/MainActivity.java",
                generateActivitySource("MainActivity", mainPage.webviewUrl, project.customCode, navCode, mainPage.enableJavaScript));
    }

    private void generateExtraActivities() throws IOException {
        String pkgPath = project.packageName.replace(".", "/");
        for (int i = 1; i < project.pages.size(); i++) {
            WebViewPageModel page = project.pages.get(i);
            String nextActivity = i + 1 < project.pages.size() ? project.pages.get(i + 1).activityClass : null;
            String navCode = "";
            if (nextActivity != null && "button".equals(page.navigationTrigger)) {
                navCode = "\n        findViewById(R.id.btn_next).setOnClickListener(v -> {\n" +
                        "            startActivity(new Intent(this, " + nextActivity + ".class));\n        });\n";
            }
            writeFile(buildDir + "src/main/java/" + pkgPath + "/" + page.activityClass + ".java",
                    generateActivitySource(page.activityClass, page.webviewUrl, "", navCode, page.enableJavaScript));
        }
    }

    private String generateActivitySource(String className, String url, String customCode, String navCode, boolean enableJs) {
        return "package " + project.packageName + ";\n\n" +
                "import android.app.Activity;\nimport android.content.Intent;\nimport android.os.Bundle;\n" +
                "import android.os.Handler;\nimport android.os.Looper;\nimport android.view.Window;\nimport android.webkit.*;\n\n" +
                "public class " + className + " extends Activity {\n    private WebView webView;\n\n" +
                "    @Override\n    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n        requestWindowFeature(Window.FEATURE_NO_TITLE);\n" +
                "        setContentView(R.layout.activity_" + toSnakeCase(className) + ");\n\n" +
                (customCode.isEmpty() ? "" : "        // Custom Code\n        " + customCode.replace("\n", "\n        ") + "\n\n") + navCode +
                "\n        webView = (WebView) findViewById(R.id.webview);\n" +
                "        WebSettings settings = webView.getSettings();\n        settings.setJavaScriptEnabled(" + enableJs + ");\n" +
                "        settings.setDomStorageEnabled(true);\n        settings.setLoadWithOverviewMode(true);\n" +
                "        settings.setUseWideViewPort(true);\n        settings.setBuiltInZoomControls(false);\n\n" +
                "        webView.setWebViewClient(new WebViewClient() {\n            @Override\n" +
                "            public boolean shouldOverrideUrlLoading(WebView v, String url) {\n                v.loadUrl(url);\n                return true;\n            }\n        });\n\n" +
                "        webView.loadUrl(\"" + url + "\");\n    }\n\n" +
                "    @Override\n    public void onBackPressed() {\n        if (webView != null && webView.canGoBack()) {\n" +
                "            webView.goBack();\n        } else {\n            super.onBackPressed();\n        }\n    }\n}\n";
    }

    private void generateLayouts() throws IOException {
        for (WebViewPageModel page : project.pages) {
            boolean needsNavButton = "button".equals(page.navigationTrigger);
            String layout = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                    "    android:layout_width=\"match_parent\" android:layout_height=\"match_parent\">\n\n" +
                    "    <WebView android:id=\"@+id/webview\" android:layout_width=\"match_parent\" android:layout_height=\"match_parent\"/>\n" +
                    (needsNavButton ? "    <Button android:id=\"@+id/btn_next\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:text=\"Next\" android:layout_alignParentBottom=\"true\" android:layout_alignParentEnd=\"true\" android:layout_margin=\"16dp\"/>\n" : "") +
                    "</RelativeLayout>\n";
            writeFile(buildDir + "src/main/res/layout/activity_" + toSnakeCase(page.activityClass) + ".xml", layout);
        }
    }

    private void generateStrings() throws IOException {
        writeFile(buildDir + "src/main/res/values/strings.xml", "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    <string name=\"app_name\">" + project.appName + "</string>\n</resources>\n");
    }

    private void writeFile(String path, String content) throws IOException {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private String toSnakeCase(String className) {
        return className.replaceAll("([A-Z])", "_$1").toLowerCase().replaceFirst("^_", "");
    }
}

