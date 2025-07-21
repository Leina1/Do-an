package com.example.apptradeup.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private List<Map<String, Object>> reports;

    public ReportAdapter(List<Map<String, Object>> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Map<String, Object> report = reports.get(position);

        holder.tvTitle.setText("Tiêu đề: " + (report.get("title") != null ? report.get("title") : ""));
        holder.tvContent.setText("Nội dung: " + (report.get("content") != null ? report.get("content") : ""));
        holder.tvReporter.setText("Người báo cáo: " + (report.get("reporterId") != null ? report.get("reporterId") : ""));
        holder.tvReported.setText("Người bị báo cáo: " + (report.get("reportedUserId") != null ? report.get("reportedUserId") : ""));

        // Hiển thị thời gian
        long timestamp = 0;
        Object tsObj = report.get("timestamp");
        if (tsObj instanceof Long) {
            timestamp = (Long) tsObj;
        } else if (tsObj instanceof Double) {
            // Firestore có thể trả ra double nếu dùng emulator/local
            timestamp = ((Double) tsObj).longValue();
        }
        if (timestamp > 0) {
            String time = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(timestamp));
            holder.tvTime.setText("Thời gian: " + time);
        } else {
            holder.tvTime.setText("Thời gian: Không rõ");
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvReporter, tvReported, tvTime;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReportTitle);
            tvContent = itemView.findViewById(R.id.tvReportContent);
            tvReporter = itemView.findViewById(R.id.tvReportReporter);
            tvReported = itemView.findViewById(R.id.tvReportReported);
            tvTime = itemView.findViewById(R.id.tvReportTime);
        }
    }
}
