package com.example.apptradeup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import okio.BufferedSink;
import okio.Okio;

public class ChatActivity extends AppCompatActivity {
    private String chatId, otherUserId, currentUserId;
    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private EditText edtMessage;
    private ImageButton btnSend;
    private ImageView AvatarHeader, imgAddImage;
    private TextView txtNameHeader;

    private FirebaseFirestore db;
    private static final int PICK_IMAGE_REQUEST = 222;
    private Uri imageUri;

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImageToCloudinary(imageUri);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerViewMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        AvatarHeader = findViewById(R.id.imgAvatar);
        txtNameHeader = findViewById(R.id.txtUserName);
        imgAddImage = findViewById(R.id.btnAddImage);

        db = FirebaseFirestore.getInstance();

        adapter = new ChatMessageAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Chỉ kiểm tra block, khi xong mới load tin nhắn
        checkBlockStatus();

        btnSend.setOnClickListener(v -> {
            if (!edtMessage.isEnabled()) return;
            String content = edtMessage.getText().toString().trim();
            if (TextUtils.isEmpty(content)) return;

            Message msg = new Message(currentUserId, content, System.currentTimeMillis());

            db.collection("chats").document(chatId)
                    .collection("messages")
                    .add(msg)
                    .addOnSuccessListener(ref -> {
                        edtMessage.setText("");
                        db.collection("chats").document(chatId)
                                .update("lastMessage", content,
                                        "lastTimestamp", System.currentTimeMillis());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show());
        });

        imgAddImage.setOnClickListener(v -> openImagePicker());

        loadUserInfo(otherUserId);
    }

    // 1. Kiểm tra trạng thái block (đọc collection "blocks" tìm cặp bị chặn)
    private void checkBlockStatus() {
        db.collection("blocks")
                .whereIn("blockerId", Arrays.asList(currentUserId, otherUserId))
                .whereIn("blockedId", Arrays.asList(currentUserId, otherUserId))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean iBlocked = false;
                    boolean iAmBlocked = false;
                    for (DocumentSnapshot doc : querySnapshot) {
                        String blockerId = doc.getString("blockerId");
                        String blockedId = doc.getString("blockedId");
                        if (blockerId == null || blockedId == null) continue;
                        // Mình là người chặn
                        if (blockerId.equals(currentUserId) && blockedId.equals(otherUserId)) {
                            iBlocked = true;
                        }
                        // Mình là người bị chặn
                        if (blockerId.equals(otherUserId) && blockedId.equals(currentUserId)) {
                            iAmBlocked = true;
                        }
                    }
                    handleBlockUI(iBlocked, iAmBlocked);
                })
                .addOnFailureListener(e -> {
                    // Nếu không check được thì tạm thời cho chat
                    handleBlockUI(false, false);
                });
    }

    // 2. Xử lý UI tuỳ theo trạng thái block
    private void handleBlockUI(boolean iBlocked, boolean iAmBlocked) {
        if (iBlocked && iAmBlocked) {
            Toast.makeText(this, "Cả hai đã chặn nhau. Không thể trò chuyện.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (iAmBlocked) {
            edtMessage.setEnabled(false);
            edtMessage.setHint("Bạn đã bị chặn, không thể nhắn tin.");
            btnSend.setEnabled(false);
        } else if (iBlocked) {
            edtMessage.setEnabled(false);
            edtMessage.setHint("Bạn đã chặn người này.");
            btnSend.setEnabled(false);
        } else {
            edtMessage.setEnabled(true);
            edtMessage.setHint("Nhập tin nhắn...");
            btnSend.setEnabled(true);
        }
        // Cho đọc lịch sử chat (nếu muốn block là không đọc luôn thì bỏ gọi loadMessagesRealtime dưới)
        loadMessagesRealtime();
    }

    private void loadMessagesRealtime() {
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
                    recyclerView.scrollToPosition(Math.max(0, messageList.size() - 1));
                });
    }

    // Hàm load avatar, tên lên header
    private void loadUserInfo(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String displayName = doc.getString("display_name");
                        String avatarUrl = doc.getString("profile_pic_url");
                        if (displayName != null) txtNameHeader.setText(displayName);

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Picasso.get().load(avatarUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(AvatarHeader);
                        } else {
                            AvatarHeader.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });
    }

    // Gửi message dạng ảnh
    private void sendImageMessage(String imageUrl) {
        String caption = edtMessage.getText().toString().trim(); // Cho phép gửi caption cùng ảnh, hoặc để trống
        Message imgMsg = new Message(currentUserId, caption, imageUrl, System.currentTimeMillis());

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(imgMsg)
                .addOnSuccessListener(ref -> {
                    edtMessage.setText(""); // Xoá caption (nếu có)
                    db.collection("chats").document(chatId)
                            .update("lastMessage", "[Đã gửi ảnh]",
                                    "lastTimestamp", System.currentTimeMillis());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi ảnh", Toast.LENGTH_SHORT).show());
    }

    // Upload ảnh lên Cloudinary và nhận link public
    private void uploadImageToCloudinary(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
            BufferedSink sink = Okio.buffer(Okio.sink(tempFile));
            sink.writeAll(Okio.source(inputStream));
            sink.close();

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", tempFile.getName(), RequestBody.create(tempFile, MediaType.parse("image/*")))
                    .addFormDataPart("upload_preset", "MyShopApp")
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/diumhctnb/image/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Lỗi upload: " + response.code(), Toast.LENGTH_SHORT).show());
                        return;
                    }
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String imageUrl = jsonObject.getString("secure_url");
                        runOnUiThread(() -> sendImageMessage(imageUrl));
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Lỗi parse URL", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, "Lỗi đọc file ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
