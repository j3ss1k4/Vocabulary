package com.example.vocabularyapp.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.WorkManager;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.dao.UserDao;
import com.example.vocabularyapp.data.local.dao.WordDao;
import com.example.vocabularyapp.databinding.FragmentProfileBinding;
import com.example.vocabularyapp.ui.LoginActivity;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private SharedPreferences sharedPref;
    private UserDao userDao;
    private WordDao wordDao;

    @Override
    protected FragmentProfileBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        userDao = AppDatabase.getInstance(requireContext()).userDao();
        wordDao = AppDatabase.getInstance(requireContext()).wordDao();

        binding.switchNotifications.setChecked(sharedPref.getBoolean("notifications_enabled", true));
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPref.edit().putBoolean("notifications_enabled", isChecked).apply();
            if (isChecked) {
                // Notifications are handled by WorkManager in VocabularyApplication (KEEP policy)
                Toast.makeText(getContext(), "Đã bật nhắc nhở hàng ngày", Toast.LENGTH_SHORT).show();
            } else {
                WorkManager.getInstance(requireContext()).cancelUniqueWork("DailyReminder");
                Toast.makeText(getContext(), "Đã tắt nhắc nhở hàng ngày", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnLogout.setOnClickListener(v -> logout());

        binding.btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void initData() {
        int userId = sharedPref.getInt("currentUserId", -1);
        if (userId != -1) {
            userDao.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    binding.tvUsername.setText(user.username);
                    binding.tvEmail.setText(user.email);
                    binding.tvStreak.setText(String.valueOf(user.streak));
                    binding.tvRank.setText(user.rank);
                }
            });
        }

        wordDao.getMasteredWordsCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvMasteredCount.setText(String.valueOf(count != null ? count : 0));
        });
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("isLoggedIn");
        editor.remove("currentUserId");
        editor.apply();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
