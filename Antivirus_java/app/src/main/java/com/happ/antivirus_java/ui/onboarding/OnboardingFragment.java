package com.happ.antivirus_java.ui.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.happ.antivirus_java.databinding.FragmentOnboardingBinding;

public class OnboardingFragment extends Fragment {

    private FragmentOnboardingBinding binding;
    private OnboardingViewModel onBoardingViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);
        onBoardingViewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OnboardingPagerAdapter pagerAdapter = new OnboardingPagerAdapter();
        binding.vpOnboarding.setAdapter(pagerAdapter);
        binding.indicator.setViewPager(binding.vpOnboarding);

        // 페이지 전환 시 버튼 텍스트 업데이트
        binding.vpOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int lastPage = pagerAdapter.getItemCount() - 1;
                binding.btnNext.setText(position == lastPage ? "이동" : "다음");
            }
        });

        // 버튼 클릭 처리
        binding.btnNext.setOnClickListener(v -> {
            int current = binding.vpOnboarding.getCurrentItem();
            if (current < pagerAdapter.getItemCount() - 1) {
                binding.vpOnboarding.setCurrentItem(current + 1);
            } else {
                // 마지막 페이지 → 카카오톡 채널 보호나라로 이동
                String url = "http://pf.kakao.com/_xnJVxoxj";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
