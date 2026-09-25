package pro.sketchware.feature.webviewbuilder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pro.sketchware.R;
import pro.sketchware.feature.webviewbuilder.WebViewPageModel;

public class PageListAdapter extends RecyclerView.Adapter<PageListAdapter.PageViewHolder> {
    public interface OnPageClickListener {
        void onPageClick(WebViewPageModel page, int position);
    }

    private final List<WebViewPageModel> pages;
    private final OnPageClickListener listener;

    public PageListAdapter(List<WebViewPageModel> pages, OnPageClickListener listener) {
        this.pages = pages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page_card, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        WebViewPageModel page = pages.get(position);
        holder.name.setText(page.name);
        holder.url.setText(page.webviewUrl);
        holder.itemView.setOnClickListener(v -> listener.onPageClick(page, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView url;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_page_name);
            url = itemView.findViewById(R.id.tv_page_url);
        }
    }
}

