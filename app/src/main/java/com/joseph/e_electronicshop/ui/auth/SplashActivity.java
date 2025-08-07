package com.joseph.e_electronicshop.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joseph.e_electronicshop.MainActivity;
import com.joseph.e_electronicshop.R;

public class SplashActivity extends AppCompatActivity {

    private View circleBackground;
    private View logo;
    private View appName;
    private CircularProgressIndicator progressIndicator;
    private View tagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        // Initialize views
        circleBackground = findViewById(R.id.circleBackground);
        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);
        progressIndicator = findViewById(R.id.progressIndicator);
        tagline = findViewById(R.id.tagline);

        // Start animations
        startSplashAnimations();
    }

    private void startSplashAnimations() {
        // Create animation set
        AnimatorSet animatorSet = new AnimatorSet();

        // Circle background animation
        ObjectAnimator circleScaleX = ObjectAnimator.ofFloat(circleBackground, "scaleX", 0.1f, 1f);
        ObjectAnimator circleScaleY = ObjectAnimator.ofFloat(circleBackground, "scaleY", 0.1f, 1f);
        circleScaleX.setDuration(800);
        circleScaleY.setDuration(800);
        circleScaleX.setInterpolator(new OvershootInterpolator(0.8f));
        circleScaleY.setInterpolator(new OvershootInterpolator(0.8f));

        // Logo animation
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.1f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.1f, 1f);
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        logoScaleX.setDuration(600);
        logoScaleY.setDuration(600);
        logoAlpha.setDuration(600);
        logoScaleX.setStartDelay(300);
        logoScaleY.setStartDelay(300);
        logoAlpha.setStartDelay(300);
        logoScaleX.setInterpolator(new OvershootInterpolator(1.2f));
        logoScaleY.setInterpolator(new OvershootInterpolator(1.2f));

        // App name animation
        ObjectAnimator appNameAlpha = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f);
        appNameAlpha.setDuration(500);
        appNameAlpha.setStartDelay(900);

        // Progress indicator animation
        ObjectAnimator progressAlpha = ObjectAnimator.ofFloat(progressIndicator, "alpha", 0f, 1f);
        progressAlpha.setDuration(400);
        progressAlpha.setStartDelay(1100);

        // Tagline animation
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f);
        taglineAlpha.setDuration(400);
        taglineAlpha.setStartDelay(1200);

        // Play animations together
        animatorSet.playTogether(
                circleScaleX, circleScaleY,
                logoScaleX, logoScaleY, logoAlpha,
                appNameAlpha,
                progressAlpha,
                taglineAlpha
        );

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Animation ended, check auth status after delay
                new Handler().postDelayed(() -> {
                    checkAuthStatus();
                }, 1000);
            }
        });

        animatorSet.start();
    }

    private void checkAuthStatus() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in, go to main activity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // No user is signed in, go to sign-in
            startActivity(new Intent(SplashActivity.this, Sign_UpActivity.class));
        }

        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}