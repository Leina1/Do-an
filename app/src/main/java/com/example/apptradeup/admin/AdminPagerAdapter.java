// AdminPagerAdapter.java
package com.example.apptradeup.admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.apptradeup.admin.AdminReportListFragment;
import com.example.apptradeup.admin.AdminUserListFragment;

public class AdminPagerAdapter extends FragmentStateAdapter {
    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new AdminReportListFragment();
            case 1: return new AdminUserListFragment();
            default: return new AdminReportListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
