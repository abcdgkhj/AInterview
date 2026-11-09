package com.example.interviewai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewai.R;
import com.example.interviewai.model.HeroSlideItem;

import java.util.List;

/**
 * Adapter for the hero carousel showing onboarding PNG images only.
 */
public class HeroCarouselAdapter extends RecyclerView.Adapter<HeroCarouselAdapter.HeroSlideViewHolder> {

    private final List<HeroSlideItem> slideItems;

    public HeroCarouselAdapter(List<HeroSlideItem> slideItems) {
        this.slideItems = slideItems;
    }

    @NonNull
    @Override
    public HeroSlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_hero_slide, parent, false);
        return new HeroSlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeroSlideViewHolder holder, int position) {
        HeroSlideItem item = slideItems.get(position);
        holder.imgSlide.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return slideItems != null ? slideItems.size() : 0;
    }

    static class HeroSlideViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgSlide;

        HeroSlideViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSlide = itemView.findViewById(R.id.imgSlideIllustration);
        }
    }
}
