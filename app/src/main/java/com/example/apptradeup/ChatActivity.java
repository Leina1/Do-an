package com.example.apptradeup;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.adapter.ChatMessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChatActivity extends AppCompatActivity {
    private String chatId, otherUserId, currentUserId;
    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private EditText edtMessage;
    private ImageButton btnSend;
    private ImageView AvatarHeader;
    private TextView txtNameHeader;

    // Có thể truyền avatarUrl của đối phương vào nếu muốn hiển thị avatar cho tin nhắn nhận
    // private String otherUserAvatarUrl;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Lấy chatId và otherUserId từ Intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerViewMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        AvatarHeader = findViewById(R.id.imgAvatar); // Nếu bạn có ImageView cho avatar
        txtNameHeader = findViewById(R.id.txtUserName); // Nếu bạn có TextView cho tên người chat

        db = FirebaseFirestore.getInstance();

        // Khởi tạo Adapter (truyền avatarUrl nếu cần)
        adapter = new ChatMessageAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Lắng nghe tin nhắn realtime (order theo thời gian tăng dần)
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    messageList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        messageList.add(msg);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });

        // Gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();
            if (TextUtils.isEmpty(content)) return;

            Message msg = new Message(currentUserId, content, System.currentTimeMillis());

            db.collection("chats").document(chatId)
                    .collection("messages")
                    .add(msg)
                    .addOnSuccessListener(ref -> {
                        edtMessage.setText("");
                        // Cập nhật lastMessage cho chat
                        db.collection("chats").document(chatId)
                                .update("lastMessage", content,
                                        "lastTimestamp", System.currentTimeMillis());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show());
        });

        // (Tuỳ chọn) Có thể load avatar, tên đối phương lên header nếu cần
        // Nếu bạn có TextView tên và ImageView avatar trên header thì:
         loadUserInfo(otherUserId);
    }

    // Nếu bạn cần load avatar, tên lên header:
     private void loadUserInfo(String userId) {
         db.collection("users").document(userId).get()
                 .addOnSuccessListener(doc -> {
                     if (doc.exists()) {
                         String displayName = doc.getString("display_name");
                         String avatarUrl = doc.getString("profile_pic_url");

                         // Set tên
                         if (displayName != null) {
                             txtNameHeader.setText(displayName);
                         }

                         // Set avatar
                         if (avatarUrl != null && !avatarUrl.isEmpty()) {
                             // Dùng Picasso:
                             Picasso.get().load(avatarUrl)
                                     .placeholder(R.drawable.ic_profile) // Ảnh tạm khi loading
                                     .error(R.drawable.ic_profile)       // Ảnh khi lỗi
                                     .into(AvatarHeader);

                             // Hoặc dùng Glide:
                             // Glide.with(this).load(avatarUrl)
                             //      .placeholder(R.drawable.ic_profile)
                             //      .into(AvatarHeader);
                         } else {
                             AvatarHeader.setImageResource(R.drawable.ic_profile);
                         }
                     }
                 })
                 .addOnFailureListener(e -> {
                     // Có thể thông báo lỗi nếu cần
                 });
     }
}
