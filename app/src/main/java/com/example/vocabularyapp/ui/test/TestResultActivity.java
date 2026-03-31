package com.example.vocabularyapp.ui.test;

import com.example.vocabularyapp.base.BaseActivity;
import com.example.vocabularyapp.databinding.ActivityTestResultBinding;

public class TestResultActivity extends BaseActivity<ActivityTestResultBinding> {

    @Override
    protected ActivityTestResultBinding inflateViewBinding() {
        return ActivityTestResultBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 0);
        int wrong = total - correct;

        binding.tvScore.setText(correct + "/" + total);
        binding.tvCorrect.setText("Đúng: " + correct);
        binding.tvWrong.setText("Sai: " + wrong);

        binding.btnFinish.setOnClickListener(v -> finish());
    }

    @Override
    protected void initData() {
    }
}
