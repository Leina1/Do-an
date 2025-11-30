// AdminReportListFragment.java
package com.example.apptradeup.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.apptradeup.R;
import com.google.firebase.firestore.*;

import java.util.*;

public class AdminReportListFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Map<String, Object>> reportList = new ArrayList<>();
    private ReportAdapter reportAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_report_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportAdapter = new ReportAdapter(reportList);
        recyclerView.setAdapter(reportAdapter);
        loadReports();
        return view;
    }


    private void loadReports() {
        FirebaseFirestore.getInstance().collection("reports")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    reportList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> reportData = doc.getData();
                        if (reportData != null) {
                            // Thêm documentId vào map với key "reportId"
                            reportData.put("reportId", doc.getId());
                            reportList.add(reportData);
                            Log.d("REPORT", "Doc: " + reportData);
                        }
                    }
                    reportAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("REPORT", "Failed to load reports", e);
                });

    }

}

