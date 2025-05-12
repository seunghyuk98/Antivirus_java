package com.happ.antivirus_java.ui.onboarding;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.happ.antivirus_java.R;
import com.happ.antivirus_java.databinding.ItemOnboardingPageBinding;

import java.util.Arrays;
import java.util.List;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private final List<OnboardingPage> onboardingContents = Arrays.asList(
            new OnboardingPage(R.drawable.im_onboard01),
            new OnboardingPage(R.drawable.im_onboard02),
            new OnboardingPage(R.drawable.im_onboard03),
            new OnboardingPage(R.drawable.im_onboard04)
    );

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOnboardingPageBinding binding = ItemOnboardingPageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OnboardingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(onboardingContents.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingContents.size();
    }

    class OnboardingViewHolder extends RecyclerView.ViewHolder {

        private final ItemOnboardingPageBinding binding;

        public OnboardingViewHolder(@NonNull ItemOnboardingPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(OnboardingPage page) {
            binding.imOnboard.setImageResource(page.getImageResId());
        }
    }
}
