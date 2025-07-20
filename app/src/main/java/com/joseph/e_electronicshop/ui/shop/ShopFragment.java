package com.joseph.e_electronicshop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.joseph.e_electronicshop.databinding.FragmentShopBinding;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ShopFragment extends Fragment {
    private FragmentShopBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView (you’ll need your own Adapter & Model)
        binding.recyclerShop.setLayoutManager(new LinearLayoutManager(getContext()));
        // binding.recyclerShop.setAdapter(new ShopAdapter(yourProductList));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
