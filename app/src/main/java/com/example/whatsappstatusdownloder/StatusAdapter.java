package com.example.whatsappstatusdownloder;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private final List<File> files;
    private final Context context;

    public StatusAdapter(List<File> files, Context context) {
        this.files = files;
        this.context = context;
    }

    public void update(ArrayList<File> newFiles) {
        this.files.clear();
        this.files.addAll(newFiles);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        File file = files.get(position);
        boolean isVideo = file.getName().toLowerCase().endsWith(".mp4");

        Glide.with(context).load(file.getAbsolutePath()).centerCrop().into(holder.imageView);
        holder.playIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        holder.downloadIcon.setOnClickListener(v -> saveFile(file));
    }

    private void saveFile(File file) {
        Toast.makeText(context, "Saving…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            boolean ok = MediaSaver.save(context, file);
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context,
                                ok ? "Saved to Gallery ✓" : "Save failed",
                                Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView downloadIcon;
        ImageView playIcon;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.statusImage);
            downloadIcon = itemView.findViewById(R.id.downloadIcon);
            playIcon = itemView.findViewById(R.id.playIcon);
        }
    }
}
