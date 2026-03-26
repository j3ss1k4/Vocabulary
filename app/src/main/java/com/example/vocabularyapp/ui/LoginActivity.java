package com.example.vocabularyapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.vocabularyapp.MainActivity;
import com.example.vocabularyapp.base.BaseActivity;
import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.entity.User;
import com.example.vocabularyapp.databinding.ActivityLoginBinding;

import java.util.concurrent.Executors;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    @Override
    protected ActivityLoginBinding inflateViewBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        binding.btnLogin.setOnClickListener(v -> login());
    }

    @Override
    protected void initData() {}

    private void login() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            User user = AppDatabase.getInstance(this).userDao().login(email, password);
            runOnUiThread(() -> {
                if (user != null) {
                    saveLoginState(user.id);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void saveLoginState(int userId) {
        SharedPreferences sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("currentUserId", userId);
        editor.apply();
    }
}
