package pro.sketchware.feature.webviewbuilder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import pro.sketchware.R;
import pro.sketchware.feature.webviewbuilder.adapter.PageListAdapter;
import pro.sketchware.feature.webviewbuilder.dialog.AddPageDialog;
import pro.sketchware.feature.webviewbuilder.dialog.EditPageDialog;

public class WebViewBuilderActivity extends AppCompatActivity {
    private EditText etAppName, etPackageName, etVersionName;
    private RecyclerView rvPages;
    private MaterialButton btnAddPage, btnBuild;
    private CheckBox cbInternet, cbCamera, cbStorage, cbContacts,
            cbAccounts, cbPhoneState, cbLocation, cbNotifications;
    private EditText etCustomCode;
    private TextView tvBuildStatus;
    private LinearLayout layoutBuildConsole;
    private ScrollView svConsole;

    private WebViewProjectModel project = new WebViewProjectModel();
    private PageListAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_builder);
        initViews();
        setupPagesList();
        setupPermissionCheckboxes();
        setupBuildButton();
        setupAppNameWatcher();
    }

    private void initViews() {
        etAppName = findViewById(R.id.et_app_name);
        etPackageName = findViewById(R.id.et_package_name);
        etVersionName = findViewById(R.id.et_version_name);
        rvPages = findViewById(R.id.rv_pages);
        btnAddPage = findViewById(R.id.btn_add_page);
        btnBuild = findViewById(R.id.btn_build_apk);
        etCustomCode = findViewById(R.id.et_custom_code);
        tvBuildStatus = findViewById(R.id.tv_build_status);
        layoutBuildConsole = findViewById(R.id.layout_build_console_content);
        svConsole = findViewById(R.id.sv_console);

        cbInternet = findViewById(R.id.cb_internet);
        cbCamera = findViewById(R.id.cb_camera);
        cbStorage = findViewById(R.id.cb_storage);
        cbContacts = findViewById(R.id.cb_contacts);
        cbAccounts = findViewById(R.id.cb_accounts);
        cbPhoneState = findViewById(R.id.cb_phone_state);
        cbLocation = findViewById(R.id.cb_location);
        cbNotifications = findViewById(R.id.cb_notifications);

        cbInternet.setChecked(true);
        cbInternet.setEnabled(false);
        etVersionName.setText("1.0");
    }

    private void setupAppNameWatcher() {
        etAppName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable editable) {
                String name = editable.toString().trim();
                project.appName = name;
                if (!name.isEmpty()) {
                    String pkg = WebViewProjectModel.generatePackageName(name);
                    etPackageName.setText(pkg);
                    project.packageName = pkg;
                }
            }
        });

        etPackageName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable editable) {
                project.packageName = editable.toString().trim();
            }
        });
    }

    private void setupPagesList() {
        pageAdapter = new PageListAdapter(project.pages, new PageListAdapter.OnPageClickListener() {
            @Override
            public void onPageClick(WebViewPageModel page, int position) {
                new EditPageDialog(WebViewBuilderActivity.this, page,
                        (name, url, navigationTrigger) -> {
                            page.name = name;
                            page.activityClass = name;
                            page.webviewUrl = url;
                            page.navigationTrigger = navigationTrigger;
                            pageAdapter.notifyItemChanged(position);
                        }).show();
            }

            @Override
            public void onPageDelete(WebViewPageModel page, int position) {
                new AlertDialog.Builder(WebViewBuilderActivity.this)
                        .setTitle("Delete Page")
                        .setMessage("Delete " + page.name + "?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (!page.isLandingPage && position < project.pages.size()) {
                                project.pages.remove(position);
                                pageAdapter.notifyItemRemoved(position);
                            }
                        })
                        .show();
            }
        });
        rvPages.setLayoutManager(new LinearLayoutManager(this));
        rvPages.setAdapter(pageAdapter);

        btnAddPage.setOnClickListener(v -> {
            if (project.pages.size() >= 4) {
                Toast.makeText(this, "Maximum 4 pages allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            new AddPageDialog(this, (name, url) -> {
                String id = "page_" + (project.pages.size() + 1);
                project.pages.add(new WebViewPageModel(id, name, url, false));
                pageAdapter.notifyItemInserted(project.pages.size() - 1);
            }).show();
        });
    }

    private void setupPermissionCheckboxes() {
        cbCamera.setOnCheckedChangeListener((button, checked) -> togglePermission("android.permission.CAMERA", checked));
        cbStorage.setOnCheckedChangeListener((button, checked) -> {
            togglePermission("android.permission.READ_EXTERNAL_STORAGE", checked);
            togglePermission("android.permission.WRITE_EXTERNAL_STORAGE", checked);
        });
        cbContacts.setOnCheckedChangeListener((button, checked) -> togglePermission("android.permission.READ_CONTACTS", checked));
        cbAccounts.setOnCheckedChangeListener((button, checked) -> togglePermission("android.permission.GET_ACCOUNTS", checked));
        cbPhoneState.setOnCheckedChangeListener((button, checked) -> togglePermission("android.permission.READ_PHONE_STATE", checked));
        cbLocation.setOnCheckedChangeListener((button, checked) -> {
            togglePermission("android.permission.ACCESS_FINE_LOCATION", checked);
            togglePermission("android.permission.ACCESS_COARSE_LOCATION", checked);
        });
        cbNotifications.setOnCheckedChangeListener((button, checked) -> togglePermission("android.permission.POST_NOTIFICATIONS", checked));
    }

    private void togglePermission(String permission, boolean add) {
        if (add) {
            if (!project.permissions.contains(permission)) project.permissions.add(permission);
        } else {
            project.permissions.remove(permission);
        }
    }

    private void setupBuildButton() {
        btnBuild.setOnClickListener(v -> {
            if (project.appName.isEmpty()) {
                etAppName.setError("App name required");
                return;
            }
            if (project.packageName.isEmpty()) {
                etPackageName.setError("Package name required");
                return;
            }
            if (project.pages.isEmpty() || project.pages.get(0).webviewUrl.equals("https://")) {
                Toast.makeText(this, "Set URL for first page", Toast.LENGTH_SHORT).show();
                return;
            }

            project.customCode = etCustomCode.getText().toString();
            layoutBuildConsole.setVisibility(View.VISIBLE);
            btnBuild.setEnabled(false);
            btnBuild.setText("Building...");

            new Thread(() -> {
                WebViewBuilderBridge bridge = new WebViewBuilderBridge(
                        this, project, this::appendLog, this::onBuildComplete);
                bridge.build();
            }).start();
        });
    }

    private void appendLog(String line) {
        runOnUiThread(() -> {
            TextView tv = new TextView(this);
            tv.setText(line);
            tv.setTextSize(12f);
            tv.setTextColor(0xFFE2E8F0);
            layoutBuildConsole.addView(tv);
            svConsole.post(() -> svConsole.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void onBuildComplete(boolean success, String apkPath) {
        runOnUiThread(() -> {
            btnBuild.setEnabled(true);
            if (success) {
                btnBuild.setText("✓ Build Successful");
                tvBuildStatus.setVisibility(View.VISIBLE);
                tvBuildStatus.setText("APK saved: " + apkPath);
                Toast.makeText(this, "APK built successfully!", Toast.LENGTH_LONG).show();
            } else {
                btnBuild.setText("Build APK");
                Toast.makeText(this, "Build failed — check console", Toast.LENGTH_LONG).show();
            }
        });
    }
}
