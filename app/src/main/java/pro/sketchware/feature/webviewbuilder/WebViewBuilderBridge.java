package pro.sketchware.feature.webviewbuilder;

import android.content.Context;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import a.a.a.ProjectBuilder;
import a.a.a.eC;
import a.a.a.hC;
import a.a.a.iC;
import a.a.a.jC;
import a.a.a.kC;
import a.a.a.yq;
import mod.jbk.build.BuiltInLibraries;

public class WebViewBuilderBridge {
    private final Context context;
    private final WebViewProjectModel project;
    private final Consumer<String> logCallback;
    private final BiConsumer<Boolean, String> onComplete;
    private static final String SK_ROOT = "/sdcard/.sketchware/mysc/";

    public WebViewBuilderBridge(Context context, WebViewProjectModel project,
                                Consumer<String> logCallback,
                                BiConsumer<Boolean, String> onComplete) {
        this.context = context;
        this.project = project;
        this.logCallback = logCallback;
        this.onComplete = onComplete;
    }

    public void build() {
        int scId = -1;
        try {
            scId = generateScId();
            String buildDir = SK_ROOT + scId + "/app/";
            log("► Preparing project structure...");
            new WebViewProjectGenerator(context, project, buildDir).generate();
            log("► Source files generated ✓");
            writeSketchwareMetadata(scId);
            log("► Project metadata written ✓");
            log("► Starting compilation (Sketchware build system)...");
            invokeSketchwareBuild(scId);
        } catch (Exception e) {
            log("✕ Error: " + e.getMessage());
            onComplete.accept(false, null);
        }
    }

    private void invokeSketchwareBuild(int scId) throws Exception {
        yq metadata = new yq(context, String.valueOf(scId));
        metadata.c(context);
        ProjectBuilder builder = new ProjectBuilder(context, metadata);

        hC fileManager = jC.b(String.valueOf(scId));
        eC dataManager = jC.a(String.valueOf(scId));
        iC libraryManager = jC.c(String.valueOf(scId));
        metadata.a(libraryManager, fileManager, dataManager);
        builder.buildBuiltInLibraryInformation();
        metadata.b(fileManager, dataManager, libraryManager, builder.getBuiltInLibraryManager());
        metadata.f();
        metadata.e();

        builder.maybeExtractAapt2();
        BuiltInLibraries.extractCompileAssets();
        log("► Compiling resources...");
        builder.compileResources();
        builder.generateViewBinding();
        log("► Compiling Java...");
        builder.compileJavaCode();
        builder.createDexFilesFromClasses();
        builder.getDexFilesReady();
        log("► Building APK...");
        builder.buildApk();
        builder.signDebugApk();

        String apkPath = SK_ROOT + scId + "/bin/" + project.appName + ".apk";
        log("✓ BUILD SUCCESSFUL");
        log("► APK: " + apkPath);
        onComplete.accept(true, apkPath);
    }

    private void writeSketchwareMetadata(int scId) throws Exception {
        String metadata = "{\n" +
                "  \"sc_id\": \"" + scId + "\",\n" +
                "  \"my_app_name\": \"" + project.appName + "\",\n" +
                "  \"my_sc_pkg_name\": \"" + project.packageName + "\",\n" +
                "  \"sc_ver_name\": \"" + project.versionName + "\",\n" +
                "  \"sc_ver_code\": \"" + project.versionCode + "\",\n" +
                "  \"sc_min_sdk_version\": \"" + project.minSdk + "\",\n" +
                "  \"sc_target_sdk_version\": \"34\",\n" +
                "  \"my_ws_name\": \"WebViewBuilder\",\n" +
                "  \"sketchware_ver\": 150\n" +
                "}";
        File metaDir = new File("/sdcard/.sketchware/data/" + scId + "/");
        metaDir.mkdirs();
        try (java.io.FileWriter writer = new java.io.FileWriter(new File(metaDir, "sketchware"))) {
            writer.write(metadata);
        }
    }

    private int generateScId() {
        File mysc = new File(SK_ROOT);
        int maxId = 700;
        File[] files = mysc.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    int id = Integer.parseInt(file.getName());
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException ignored) { }
            }
        }
        return maxId + 1;
    }

    private void log(String line) {
        logCallback.accept(line);
    }
}
