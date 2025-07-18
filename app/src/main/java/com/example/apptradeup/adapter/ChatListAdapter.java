package com.example.apptradeup.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.R;
import com.example.apptradeup.Chat;
import com.squareup.picasso.Picasso;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    public interface OnChatClickListener {
        void onChatClick(Chat chat, String otherUserId);
    }

    private List<Chat> chatList;
    private String currentUserId;
    private OnChatClickListener listener;

    public ChatListAdapter(List<Chat> chatList, String currentUserId, OnChatClickListener listener) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        String otherUserId = chat.getMembers().get(0).equals(currentUserId) ? chat.getMembers().get(1) : chat.getMembers().get(0);

        // Lấy thông tin user đối phương
        FirebaseFirestore.getInstance().collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    holder.tvName.setText(doc.getString("display_name"));
                    String avatar = doc.getString("profile_pic_url");
                    if (avatar != null && !avatar.isEmpty()) {
                        Picasso.get().load(avatar).into(holder.imgAvatar);
                    }
                });
        holder.tvLastMessage.setText(chat.getLastMessage());
        holder.tvTime.setText(chat.getTimeString()); // Cần có hàm chuyển đổi time stamp → hh:mm

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChatClick(chat, otherUserId);
        });
        // Xử lý nút more để block/báo cáo tại đây nếu cần
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvLastMessage, tvTime;
        ImageButton btnMore;
        public ChatViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
