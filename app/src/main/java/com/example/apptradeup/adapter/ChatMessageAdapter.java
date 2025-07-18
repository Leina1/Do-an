package com.example.apptradeup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptradeup.Message;
import com.example.apptradeup.R;
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
        // 1: tin nhắn của mình, 0: đối phương
        Message msg = messages.get(position);
        return msg.getSenderId().equals(currentUserId) ? 1 : 0;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.txtMessage.setText(msg.getContent());

        // Hiển thị giờ (nếu có trường timestamp)
        if (holder.txtTime != null && msg.getTimestamp() > 0) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(msg.getTimestamp()));
            holder.txtTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;

        MessageViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime); // nhớ đặt id này trong cả 2 layout!
        }
    }
}
