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
        adapter = new ChatListAdapter(chatList, FirebaseAuth.getInstance().getCurrentUser().getUid(),
                (chat, otherUserId) -> openChatActivity(chat.getId(), otherUserId));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadChatList();
    }

    private void loadChatList() {
        db.collection("chats")
                .whereArrayContains("members", currentUserId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    chatList.clear();
                    for (DocumentSnapshot doc : value) {
                        Chat chat = doc.toObject(Chat.class);
                        chat.setId(doc.getId());
                        chatList.add(chat);
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
