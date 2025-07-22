package com.example.apptradeup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptradeup.Message;
import com.example.apptradeup.R;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private String currentUserId;

    public ChatMessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        return msg.getSenderId().equals(currentUserId) ? 1 : 0;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == 1) ? R.layout.item_message_sent : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messages.get(position);

        // 1. Xử lý hiển thị nội dung text
        if (msg.getContent() != null && !msg.getContent().isEmpty()) {
            holder.txtMessage.setText(msg.getContent());
            holder.txtMessage.setVisibility(View.VISIBLE);
        } else {
            holder.txtMessage.setVisibility(View.GONE);
        }

        // 2. Hiển thị ảnh nếu có imageUrl
        if (holder.imgMessage != null) {
            if (msg.getImageUrl() != null && !msg.getImageUrl().isEmpty()) {
                holder.imgMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(msg.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.imgMessage);
            } else {
                holder.imgMessage.setVisibility(View.GONE);
            }
        }

        // 3. Hiển thị giờ gửi tin nhắn
        if (holder.txtTime != null && msg.getTimestamp() > 0) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(msg.getTimestamp()));
            holder.txtTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;
        ImageView imgMessage;

        MessageViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgMessage = itemView.findViewById(R.id.imgMessage); // Phải thêm vào cả 2 layout
        }
    }
}
