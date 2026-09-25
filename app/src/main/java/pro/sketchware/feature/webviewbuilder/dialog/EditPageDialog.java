package pro.sketchware.feature.webviewbuilder.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;

import pro.sketchware.R;
import pro.sketchware.feature.webviewbuilder.WebViewPageModel;

public class EditPageDialog {
    public interface OnPageEditedListener {
        void onPageEdited(String name, String url, String navigationTrigger);
    }

    private final Context context;
    private final WebViewPageModel page;
    private final OnPageEditedListener listener;

    public EditPageDialog(Context context, WebViewPageModel page, OnPageEditedListener listener) {
        this.context = context;
        this.page = page;
        this.listener = listener;
    }

    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_page, null);
        EditText name = view.findViewById(R.id.et_edit_page_name);
        EditText url = view.findViewById(R.id.et_edit_page_url);
        Spinner navigation = view.findViewById(R.id.spinner_navigation_type);

        name.setText(page.name);
        url.setText(page.webviewUrl);
        String[] values = {"none", "auto", "button"};
        String[] labels = {"No navigation", "Automatic navigation", "Button navigation"};
        navigation.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, labels));
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(page.navigationTrigger)) {
                navigation.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(context)
                .setTitle("Edit Page")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> listener.onPageEdited(
                        name.getText().toString().trim(),
                        url.getText().toString().trim(),
                        values[navigation.getSelectedItemPosition()]))
                .show();
    }
}

