package com.joseph.e_electronicshop.ui.shop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentShopBinding;
import com.joseph.e_electronicshop.ui.viewmodels.SharedProductViewModel;

import java.util.HashMap;
import java.util.Map;

public class ShopFragment extends Fragment {

    private FragmentShopBinding binding;

    public ShopFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedProductViewModel.class);

        viewModel.getProduct().observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                binding.productName.setText(product.getProductName());
                binding.productPrice.setText("Ksh " + product.getPriceKsh());
                binding.productDescription.setText(product.getDescription());

                try {
                    byte[] decodedString = Base64.decode(product.getImageBase64(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    binding.productImage.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    Log.e("ShopFragment", "Image decode error", e);
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
