package com.joseph.e_electronicshop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);

        BottomNavigationView nav = binding.bottomNav;
        nav.setOnItemSelectedListener(item -> {
            Fragment selected;

            int id = item.getItemId();
            if (id == R.id.nav_shop) {
                selected = new com.joseph.e_electronicshop.ui.home.ShopFragment();
            }
            else if (id == R.id.nav_cart) {
                selected = new com.joseph.e_electronicshop.ui.cart.CartFragment();
            }
            else if (id == R.id.nav_account) {
                selected = new AccountFragment();
            }
            else {  // includes R.id.nav_home fallback
                selected = new HomeContentFragment();
            }

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.tab_fragment_container, selected)
                    .commit();

            return true;
        });

        // Show Home tab on first load
        if (savedState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}