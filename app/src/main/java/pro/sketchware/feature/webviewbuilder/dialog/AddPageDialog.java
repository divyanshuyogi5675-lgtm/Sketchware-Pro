package pro.sketchware.feature.webviewbuilder.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import pro.sketchware.R;

public class AddPageDialog {
    public interface OnPageAddedListener {
        void onPageAdded(String name, String url);
    }

    private final Context context;
    private final OnPageAddedListener listener;

    public AddPageDialog(Context context, OnPageAddedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_page, null);
        EditText name = view.findViewById(R.id.et_page_name);
        EditText url = view.findViewById(R.id.et_page_url);
        new AlertDialog.Builder(context)
                .setTitle("Add Page")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> listener.onPageAdded(
                        name.getText().toString().trim(), url.getText().toString().trim()))
                .show();
    }
}

