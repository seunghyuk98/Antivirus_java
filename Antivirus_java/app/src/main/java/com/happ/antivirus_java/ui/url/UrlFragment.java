package com.happ.antivirus_java.ui.url;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.happ.antivirus_java.databinding.FragmentUrlBinding;


public class UrlFragment extends Fragment {

    private FragmentUrlBinding binding;
    private UrlViewModel urlViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUrlBinding.inflate(inflater, container, false);
        urlViewModel = new ViewModelProvider(this).get(UrlViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
        setEvent();
    }

    private void init() {
        urlViewModel = new ViewModelProvider(this).get(UrlViewModel.class);
    }

    private void setEvent() {
        observeViewModel();

        binding.btUrlConfirm.setOnClickListener(v -> {
            String url = binding.edUrl.getText().toString();
            if (!url.isEmpty()) {
                startLoadingAnimation();

                urlViewModel.getURLInfo(url);
            }
        });
    }

    private Thread loadingThread;
    private volatile boolean isRunning = true; // 🔸 플래그

    public void startLoadingAnimation() {
        isRunning = true; // 시작할 때 true
        loadingThread = new Thread(() -> {
            final String baseText = "검색중입니다";
            int i = 0;
            while (isRunning) {
                int dotCount = i % 3 + 1;
                String dots = new String(new char[dotCount]).replace("\0", ".");
                String animatedText = baseText + dots;

                new Handler(Looper.getMainLooper()).post(() -> {
                    binding.tempTxt.setText(animatedText);
                });

                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    break; // sleep 중 인터럽트되면 루프 종료
                }
                i++;
            }
        });
        loadingThread.start();
    }

    public void stopLoadingAnimation() {
        isRunning = false; // 🔸 루프 빠져나오게 함
        if (loadingThread != null && loadingThread.isAlive()) {
            loadingThread.interrupt(); // sleep 중이면 즉시 깨움
        }
    }


    private void observeViewModel() {
        urlViewModel.urlInfo.observe(getViewLifecycleOwner(), urlInfo -> {
            stopLoadingAnimation();
            // Update UI with the URL information
            binding.tempTxt.setText(urlInfo);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
