package com.russianresale.app.ui.listing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.data.repo.ListingRepository;
import com.russianresale.app.databinding.ItemPhotoThumbBinding;
import com.russianresale.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Миниатюры фото в редакторе объявления. Первая плитка — «Добавить». */
public class PhotoThumbAdapter extends RecyclerView.Adapter<PhotoThumbAdapter.Holder> {

    public interface Listener {
        void onAdd();
    }

    private final List<String> photos = new ArrayList<>();
    private final Listener listener;

    public PhotoThumbAdapter(Listener listener) {
        this.listener = listener;
    }

    public List<String> getPhotos() {
        return new ArrayList<>(photos);
    }

    public void setPhotos(List<String> list) {
        photos.clear();
        photos.addAll(list);
        notifyDataSetChanged();
    }

    public void add(String path) {
        if (photos.size() >= ListingRepository.MAX_PHOTOS) return;
        photos.add(path);
        notifyDataSetChanged();
    }

    public int remaining() {
        return ListingRepository.MAX_PHOTOS - photos.size();
    }

    private boolean hasAddTile() {
        return photos.size() < ListingRepository.MAX_PHOTOS;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemPhotoThumbBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        if (hasAddTile() && position == 0) {
            h.b.addTile.setVisibility(View.VISIBLE);
            h.b.image.setImageDrawable(null);
            h.b.remove.setVisibility(View.GONE);
            h.b.coverLabel.setVisibility(View.GONE);
            h.itemView.setOnClickListener(v -> listener.onAdd());
            h.itemView.setOnLongClickListener(null);
            return;
        }
        int index = hasAddTile() ? position - 1 : position;
        String photo = photos.get(index);
        h.b.addTile.setVisibility(View.GONE);
        h.b.remove.setVisibility(View.VISIBLE);
        h.b.coverLabel.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        ImageLoader.load(h.b.image, photo);
        h.b.remove.setOnClickListener(v -> {
            photos.remove(photo);
            notifyDataSetChanged();
        });
        h.itemView.setOnClickListener(null);
        h.itemView.setOnLongClickListener(v -> {
            photos.remove(photo);
            photos.add(0, photo);
            notifyDataSetChanged();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return photos.size() + (hasAddTile() ? 1 : 0);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemPhotoThumbBinding b;

        Holder(ItemPhotoThumbBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
