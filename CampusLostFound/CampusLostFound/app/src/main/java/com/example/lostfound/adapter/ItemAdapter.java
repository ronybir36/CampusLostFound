package com.example.lostfound.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfound.R;
import com.example.lostfound.model.Item;
import com.example.lostfound.util.ImageUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * RecyclerView adapter. Each row is a CardView showing the photo, type,
 * title, location and date. A ViewHolder keeps references to the views
 * so they are not searched again on every scroll.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    private final List<Item> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public ItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /** Replaces the whole list. Called every time the Firebase listener fires. */
    public void setItems(List<Item> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView type;
        private final TextView title;
        private final TextView location;
        private final TextView date;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            type = itemView.findViewById(R.id.itemType);
            title = itemView.findViewById(R.id.itemTitle);
            location = itemView.findViewById(R.id.itemLocation);
            date = itemView.findViewById(R.id.itemDate);
        }

        void bind(Item item) {
            boolean isLost = "LOST".equals(item.getType());
            type.setText(isLost ? "LOST" : "FOUND");
            type.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    isLost ? R.color.lost_red : R.color.found_green));

            title.setText(item.getTitle());
            location.setText(item.getLocation());
            date.setText(dateFormat.format(new Date(item.getCreatedAt())));

            Bitmap bitmap = ImageUtils.fromBase64(item.getImageBase64());
            if (bitmap != null) {
                image.setImageBitmap(bitmap);
            } else {
                image.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}
