package com.example.vocabularyapp.ui.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.databinding.FragmentProfileBinding;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    @Override
    protected FragmentProfileBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle notification toggle
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            // Handle edit profile
        });
    }

    @Override
    protected void initData() {
        // Load user profile data
    }
}
