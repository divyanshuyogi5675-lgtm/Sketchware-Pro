package pro.sketchware.activities.webapkforge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.gson.Gson;
import com.besome.sketch.beans.ProjectFileBean;
import com.besome.sketch.beans.ViewBean;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.besome.sketch.projects.MyProjectSettingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import a.a.a.hC;
import a.a.a.jC;
import a.a.a.lC;
import a.a.a.wq;
import pro.sketchware.R;
import pro.sketchware.utility.FilePathUtil;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;

/**
 * ApkForge quick wizard inside Sketchware Pro.
 * It creates a real Sketchware project scaffold; the existing Sketchware
 * ProjectBuilder remains responsible for compiling/signing the APK.
 */
public class WebApkForgeActivity extends BaseAppCompatActivity {
    private final Pattern packagePattern = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$");
    private final List<CheckBox> permissionChecks = new ArrayList<>();
    private EditText appNameInput;
    private EditText packageInput;
    private EditText urlInput;
    private ActivityResultLauncher<Intent> projectSettingsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                    String scId = result.getData().getStringExtra("sc_id");
                    if (scId == null || scId.isEmpty()) return;
                    finishWebProject(scId, urlInput.getText().toString().trim());
                });
        setContentView(buildContent());
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 24, 32, 32);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("ApkForge Web APK");
        title.setTextSize(24);
        title.setTextColor(0xff202124);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Create a real WebView project using Sketchware Pro's native Gradle builder.");
        subtitle.setPadding(0, 8, 0, 20);
        root.addView(subtitle);

        appNameInput = field(root, "App name", "Cardweb");
        packageInput = field(root, "Package name", "com.example.cardweb");
        packageInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput = field(root, "HTTPS website URL", "https://");
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        TextView permissionsTitle = new TextView(this);
        permissionsTitle.setText("Android permissions");
        permissionsTitle.setTextSize(16);
        permissionsTitle.setPadding(0, 20, 0, 8);
        root.addView(permissionsTitle);
        addPermission(root, "android.permission.GET_ACCOUNTS", "Get accounts");
        addPermission(root, "android.permission.READ_PHONE_STATE", "Read phone state");
        addPermission(root, "android.permission.READ_CONTACTS", "Read contacts");
        addPermission(root, "android.permission.ACCESS_NETWORK_STATE", "Network state");
        addPermission(root, "android.permission.CAMERA", "Camera");
        addPermission(root, "android.permission.RECORD_AUDIO", "Microphone");
        addPermission(root, "android.permission.POST_NOTIFICATIONS", "Notifications");
        addPermission(root, "android.permission.VIBRATE", "Vibrate");
        addPermission(root, "android.permission.BLUETOOTH", "Bluetooth");
        addPermission(root, "android.permission.ACCESS_FINE_LOCATION", "Precise location");

        TextView internet = new TextView(this);
        internet.setText("✓ INTERNET (always included for WebView)");
        internet.setPadding(0, 12, 0, 10);
        root.addView(internet);

        Button create = new Button(this);
        create.setText("Create Web APK project");
        create.setOnClickListener(v -> createProject());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, -2);
        buttonParams.topMargin = 20;
        root.addView(create, buttonParams);
        return scroll;
    }

    private EditText field(LinearLayout root, String label, String hint) {
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setPadding(0, 10, 0, 4);
        root.addView(labelView);
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        root.addView(input, new LinearLayout.LayoutParams(-1, -2));
        return input;
    }

    private void addPermission(LinearLayout root, String permission, String label) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(label);
        checkBox.setTag(permission);
        root.addView(checkBox);
        permissionChecks.add(checkBox);
    }

    private void createProject() {
        String name = appNameInput.getText().toString().trim();
        String packageName = packageInput.getText().toString().trim();
        String url = urlInput.getText().toString().trim();
        if (name.length() < 2) {
            SketchwareUtil.toast("Enter an app name");
            return;
        }
        if (!packagePattern.matcher(packageName).matches()) {
            SketchwareUtil.toast("Use a valid package name, for example com.cardweb.app");
            return;
        }
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            SketchwareUtil.toast("Enter a valid website URL");
            return;
        }
        Intent intent = new Intent(this, MyProjectSettingActivity.class);
        intent.putExtra("my_ws_name", name);
        intent.putExtra("my_app_name", name);
        intent.putExtra("my_sc_pkg_name", packageName);
        projectSettingsLauncher.launch(intent);
    }

    private void finishWebProject(String scId, String url) {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add("android.permission.INTERNET");
        for (CheckBox checkBox : permissionChecks) {
            if (checkBox.isChecked()) permissions.add((String) checkBox.getTag());
        }
        FilePathUtil filePathUtil = new FilePathUtil();
        FileUtil.writeFile(filePathUtil.getPathPermission(scId), new Gson().toJson(permissions));

        HashMap<String, Object> metadata = lC.b(scId);
        if (metadata != null) {
            metadata.put("apkforge_url", url);
            lC.b(scId, metadata);
        }

        // Add a full-screen WebView to the first generated activity. Sketchware's
        // normal DesignActivity and ProjectBuilder then continue the standard flow.
        hC projectFiles = jC.b(scId);
        List<ProjectFileBean> files = projectFiles.b();
        if (files != null && !files.isEmpty()) {
            ProjectFileBean activity = files.get(0);
            ViewBean webView = new ViewBean();
            webView.type = ViewBean.VIEW_TYPE_WIDGET_WEBVIEW;
            webView.id = "webview1";
            webView.convert = "WebView";
            webView.layout.width = -1;
            webView.layout.height = -1;
            jC.a(scId).a(activity.getXmlName(), webView);
            jC.a(scId).o(activity.getJavaName(), "webview1.getSettings().setJavaScriptEnabled(true);\\nwebview1.setWebViewClient(new android.webkit.WebViewClient());\\nwebview1.loadUrl(\"" + escapeJava(url) + "\");");
        }
        SketchwareUtil.toast("Web APK project created. Open it and press Build to create the signed APK.");
        finish();
    }

    private String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
