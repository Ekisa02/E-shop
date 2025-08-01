package com.joseph.e_electronicshop.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.joseph.e_electronicshop.ui.Adapters.Product;

public class SharedProductViewModel extends ViewModel {
    private final MutableLiveData<Product> selectedProduct = new MutableLiveData<>();

    public void setProduct(Product product) {
        selectedProduct.setValue(product);
    }

    public LiveData<Product> getProduct() {
        return selectedProduct;
    }
}
