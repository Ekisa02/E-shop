package com.joseph.e_electronicshop.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.joseph.e_electronicshop.ui.Adapters.Product;

public class SharedProductViewModel extends AndroidViewModel {
    private final MutableLiveData<Product> selectedProduct = new MutableLiveData<>();

    public SharedProductViewModel(@NonNull Application application) {
        super(application);
    }

    public void setProduct(Product product) {
        selectedProduct.postValue(product); // Using postValue for thread safety
    }

    public LiveData<Product> getProduct() {
        return selectedProduct;
    }
}