package com.example.interviewai.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewai.R;
import com.example.interviewai.model.HeroSlideItem;

import java.util.List;

/**
 * Adapter for the hero carousel supporting circular infinite loop with scale touch animation & click handling.
 */
public class HeroCarouselAdapter extends RecyclerView.Adapter<HeroCarouselAdapter.HeroSlideViewHolder> {

    public interface OnSlideClickListener {
        void onSlideClick(int realPosition);
    }

    private static final float PRESSED_SCALE = 0.965f;
    private static final long SCALE_DURATION_MS = 140L;

    private final List<HeroSlideItem> slideItems;
    private final OnSlideClickListener clickListener;
    private final DecelerateInterpolator interpolator = new DecelerateInterpolator();

    public HeroCarouselAdapter(List<HeroSlideItem> slideItems) {
        this(slideItems, null);
    }

    public HeroCarouselAdapter(List<HeroSlideItem> slideItems, OnSlideClickListener clickListener) {
        this.slideItems = slideItems;
        this.clickListener = clickListener;
    }

    /**
     * Maps adapter position in circular setup [Fake4, Real1, Real2, Real3, Real4, Fake1]
     * to the 0-indexed real slide position (0 to 3).
     */
    public static int toRealPosition(int adapterPosition) {
        if (adapterPosition <= 0) return 3; // Fake Slide 4 -> Real Slide 4 (index 3)
        if (adapterPosition >= 5) return 0; // Fake Slide 1 -> Real Slide 1 (index 0)
        return adapterPosition - 1;         // 1->0, 2->1, 3->2, 4->3
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

        // Reset scale for safety
        holder.itemView.setScaleX(1.0f);
        holder.itemView.setScaleY(1.0f);

        // Smooth Scale Animation on touch (press down -> scale down; release/cancel -> return to 1.0f)
        holder.itemView.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                v.animate()
                        .scaleX(PRESSED_SCALE)
                        .scaleY(PRESSED_SCALE)
                        .setDuration(SCALE_DURATION_MS)
                        .setInterpolator(interpolator)
                        .start();
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(SCALE_DURATION_MS)
                        .setInterpolator(interpolator)
                        .start();
            }
            return false; // Return false so OnClickListener and ViewPager2 swipe gesture continue normally
        });

        // Click listener with smooth transition handling
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    int realPos = toRealPosition(pos);
                    if (realPos == 0) {
                        // Slide 1 stays on HomeActivity: trigger immediately
                        clickListener.onSlideClick(realPos);
                    } else {
                        // Slides 2, 3, 4 navigate to another Activity:
                        // Small 120ms delay allows the scale-up bounce to complete smoothly before the new page opens
                        v.postDelayed(() -> clickListener.onSlideClick(realPos), 120L);
                    }
                }
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull HeroSlideViewHolder holder) {
        super.onViewRecycled(holder);
        holder.itemView.animate().cancel();
        holder.itemView.setScaleX(1.0f);
        holder.itemView.setScaleY(1.0f);
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
