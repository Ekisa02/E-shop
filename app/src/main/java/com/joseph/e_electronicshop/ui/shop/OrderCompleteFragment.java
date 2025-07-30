package com.joseph.e_electronicshop.ui.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentOrderCompleteBinding;

public class OrderCompleteFragment extends Fragment {
    private FragmentOrderCompleteBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderCompleteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backToHomeButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(),
                    R.id.nav_host_fragment_content_main);
            navController.popBackStack(R.id.nav_home, false);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}