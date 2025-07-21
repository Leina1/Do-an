package com.example.apptradeup.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.R;
import com.example.apptradeup.Chat;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    public interface OnChatClickListener {
        void onChatClick(Chat chat, String otherUserId);
    }

    private List<Chat> chatList;
    private String currentUserId;
    private OnChatClickListener listener;

    private Context context; // thêm dòng này trên đầu class

    public ChatListAdapter(Context context, List<Chat> chatList, String currentUserId, OnChatClickListener listener) {
        this.context = context;
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
            if (listener != null) {
                // Check block trước khi cho chat!
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("blocks")
                        .whereIn("blockerId", Arrays.asList(currentUserId, otherUserId))
                        .whereIn("blockedId", Arrays.asList(currentUserId, otherUserId))
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            boolean currentBlocksOther = false;
                            boolean otherBlocksCurrent = false;
                            for (DocumentSnapshot doc : querySnapshot) {
                                String blockerId = doc.getString("blockerId");
                                String blockedId = doc.getString("blockedId");
                                if (blockerId.equals(currentUserId) && blockedId.equals(otherUserId)) {
                                    currentBlocksOther = true;
                                }
                                if (blockerId.equals(otherUserId) && blockedId.equals(currentUserId)) {
                                    otherBlocksCurrent = true;
                                }
                            }

                            if (currentBlocksOther && otherBlocksCurrent) {
                                Toast.makeText(context, "Hai bạn đã chặn nhau. Không thể trò chuyện.", Toast.LENGTH_SHORT).show();
                            } else if (currentBlocksOther) {
                                Toast.makeText(context, "Bạn đã chặn người này. Không thể nhắn tin.", Toast.LENGTH_SHORT).show();
                            } else if (otherBlocksCurrent) {
                                Toast.makeText(context, "Bạn đã bị người này chặn. Không thể nhắn tin.", Toast.LENGTH_SHORT).show();
                            } else {
                                listener.onChatClick(chat, otherUserId);
                            }
                        });
            }
        });
        holder.btnMore.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("blocks")
                    .whereEqualTo("blockerId", currentUserId)
                    .whereEqualTo("blockedId", otherUserId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        boolean isBlocked = !querySnapshot.isEmpty();
                        PopupMenu popup = new PopupMenu(context, holder.btnMore);
                        popup.getMenuInflater().inflate(R.menu.menu_chat_options, popup.getMenu());
                        MenuItem blockItem = popup.getMenu().findItem(R.id.action_block);
                        if (isBlocked) {
                            blockItem.setTitle("Bỏ chặn");
                        } else {
                            blockItem.setTitle("Chặn người này");
                        }
                        popup.setOnMenuItemClickListener(item -> {
                            if (item.getItemId() == R.id.action_block) {
                                if (isBlocked) {
                                    // Thực hiện bỏ chặn
                                    for (DocumentSnapshot doc : querySnapshot) {
                                        doc.getReference().delete()
                                                .addOnSuccessListener(aVoid ->
                                                        Toast.makeText(context, "Đã bỏ chặn!", Toast.LENGTH_SHORT).show());
                                    }
                                } else {
                                    // Thực hiện chặn
                                    blockUser(otherUserId);
                                }
                                return true;
                            }else if (item.getItemId() == R.id.action_report) {
                                // ====== SHOW DIALOG NHẬP REPORT ======
                                showReportDialog(otherUserId, chat.getId());
                                return true;
                            }
                            // ... Xử lý báo cáo hội thoại
                            return false;
                        });
                        popup.show();
                    });
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
    private void blockUser(String blockedId) {
        HashMap<String, Object> block = new HashMap<>();
        block.put("blockerId", currentUserId); // ID của người dùng hiện tại (bạn)
        block.put("blockedId", blockedId);     // ID của người bị chặn

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("blocks")
                .add(block)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(context, "Đã chặn người dùng này!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Chặn thất bại!", Toast.LENGTH_SHORT).show();
                });
    }
    private void showReportDialog(String reportedUserId, String chatId) {
        // Tạo layout dialog đơn giản
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * context.getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText edtTitle = new EditText(context);
        edtTitle.setHint("Tiêu đề báo cáo");

        EditText edtContent = new EditText(context);
        edtContent.setHint("Nội dung báo cáo");
        edtContent.setMinLines(3);
        edtContent.setGravity(Gravity.TOP);

        layout.addView(edtTitle);
        layout.addView(edtContent);

        new android.app.AlertDialog.Builder(context)
                .setTitle("Báo cáo hội thoại")
                .setView(layout)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String title = edtTitle.getText().toString().trim();
                    String content = edtContent.getText().toString().trim();

                    if (title.isEmpty() || content.isEmpty()) {
                        Toast.makeText(context, "Nhập đầy đủ tiêu đề và nội dung!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Lưu vào Firestore collection "reports"
                    HashMap<String, Object> report = new HashMap<>();
                    report.put("reporterId", currentUserId);
                    report.put("reportedUserId", reportedUserId);
                    report.put("title", title);
                    report.put("content", content);
                    report.put("timestamp", System.currentTimeMillis());

                    FirebaseFirestore.getInstance().collection("reports")
                            .add(report)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(context, "Đã gửi báo cáo!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Gửi báo cáo thất bại!", Toast.LENGTH_SHORT).show();
                            });

                })
                .setNegativeButton("Huỷ", null)
                .show();
    }


}
