package com.example.apptradeup.admin;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptradeup.R;
import com.example.apptradeup.User;

import java.util.List;

public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onBanClicked(User user);
        void onDeleteClicked(User user);
    }

    public UserAdminAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserId.setText("ID: " + user.getId());
        holder.tvUserName.setText(user.getDisplay_name());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserRole.setText("Role: " + (user.getRole() == null ? "user" : user.getRole()));

        // Hiển thị trạng thái rõ ràng
        String statusLabel;
        if ("banned".equals(user.getStatus())) {
            statusLabel = "Bị cấm";
            holder.tvUserStatus.setTextColor(holder.itemView.getContext().getColor(R.color.red));
        } else {
            statusLabel = "Đang hoạt động";
            holder.tvUserStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        }
        holder.tvUserStatus.setText("Trạng thái: " + statusLabel);

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMore);
            popup.getMenuInflater().inflate(R.menu.menu_user_admin, popup.getMenu());
            // Đổi nhãn tuỳ trạng thái
            if ("banned".equals(user.getStatus())) {
                popup.getMenu().findItem(R.id.action_ban).setTitle("Mở cấm");
            } else {
                popup.getMenu().findItem(R.id.action_ban).setTitle("Cấm");
            }

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_ban) {
                    listener.onBanClicked(user);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    listener.onDeleteClicked(user);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserId, tvUserName, tvUserEmail, tvUserRole, tvUserStatus;
        ImageView btnMore;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
