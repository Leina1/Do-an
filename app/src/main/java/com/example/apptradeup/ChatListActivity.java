package com.example.apptradeup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.adapter.ChatListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();
    private String currentUserId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        adapter = new ChatListAdapter(
                this, // context
                chatList,
                currentUserId,
                (chat, otherUserId) -> openChatActivity(chat.getId(), otherUserId)
        );
        recyclerView.setAdapter(adapter);

        if (currentUserId == null) {
            Toast.makeText(this, "Không xác định được người dùng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadChatList();
    }

    private void loadChatList() {
        db.collection("chats")
                .whereArrayContains("members", currentUserId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
//                    if (error != null) {
//                        Toast.makeText(this, "Lỗi khi tải danh sách chat!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                    if (value == null) return;
                    chatList.clear();
                    for (DocumentSnapshot doc : value) {
                        Chat chat = doc.toObject(Chat.class);
                        if (chat != null) {
                            chat.setId(doc.getId()); // Đảm bảo class Chat có phương thức setId
                            chatList.add(chat);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void openChatActivity(String chatId, String otherUserId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUserId);
        startActivity(intent);
    }
}
