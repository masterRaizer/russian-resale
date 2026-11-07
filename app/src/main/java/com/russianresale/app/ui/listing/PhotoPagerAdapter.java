package com.russianresale.app.ui.listing;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.R;
import com.russianresale.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Листание фотографий объявления. */
public class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.Holder> {
    private final List<String> photos = new ArrayList<>();

    public void setPhotos(List<String> list) {
        photos.clear();
        if (list != null) photos.addAll(list);
        if (photos.isEmpty()) photos.add(null);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder((ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ImageLoader.load(holder.image, photos.get(position), 0.45f);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;

        Holder(ImageView image) {
            super(image);
            this.image = image;
        }
    }
}
