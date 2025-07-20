package com.joseph.e_electronicshop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.joseph.e_electronicshop.databinding.FragmentHomeContentBinding;

public class HomeContentFragment extends Fragment {
    private FragmentHomeContentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeContentBinding.inflate(inflater, container, false);
        HomeViewModel vm = new ViewModelProvider(this).get(HomeViewModel.class);
        vm.getText().observe(getViewLifecycleOwner(), binding.textHome::setText);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
