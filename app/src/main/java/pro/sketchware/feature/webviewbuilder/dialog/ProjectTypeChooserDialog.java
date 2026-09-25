package pro.sketchware.feature.webviewbuilder.dialog;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import pro.sketchware.R;
import pro.sketchware.feature.webviewbuilder.WebViewBuilderActivity;

public class ProjectTypeChooserDialog {
    private final Context context;
    private final Runnable onSketchwareSelected;

    public ProjectTypeChooserDialog(Context context, Runnable onSketchwareSelected) {
        this.context = context;
        this.onSketchwareSelected = onSketchwareSelected;
    }

    public void show() {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_project_type_chooser, null);
        dialog.setContentView(view);

        LinearLayout cardSketchware = view.findViewById(R.id.card_sketchware_builder);
        cardSketchware.setOnClickListener(v -> {
            dialog.dismiss();
            onSketchwareSelected.run();
        });

        LinearLayout cardWebView = view.findViewById(R.id.card_webview_builder);
        cardWebView.setOnClickListener(v -> {
            dialog.dismiss();
            context.startActivity(new Intent(context, WebViewBuilderActivity.class));
        });

        dialog.show();
    }
}

