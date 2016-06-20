/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.patternlock;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

// For AOSP implementations, see:
// https://android.googlesource.com/platform/packages/apps/Settings/+/master/src/com/android/settings/ConfirmLockPattern.java
// https://android.googlesource.com/platform/frameworks/base/+/43d8451/policy/src/com/android/internal/policy/impl/keyguard/KeyguardPatternView.java
// https://android.googlesource.com/platform/frameworks/base/+/master/packages/Keyguard/src/com/android/keyguard/KeyguardPatternView.java
public class ConfirmPatternActivity extends BasePatternActivity
        implements PatternView.OnPatternListener {

    private static final String KEY_NUM_FAILED_ATTEMPTS = "num_failed_attempts";

    public static final int RESULT_FORGOT_PASSWORD = RESULT_FIRST_USER;

    protected int numFailedAttempts;
    /**
     * 沉浸式
     * @param activity
     * @param color
     */
    public static void immerge(Activity activity, int color) {
        /**沉浸式状态栏设置部分**/
        //Android5.0版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            setStatusBarColor(activity, color);//阴影绘制
            //设置状态栏颜色
//            getWindow().setStatusBarColor(getResources().getColor(color));
            //设置导航栏颜色
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(color));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //透明状态栏
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //创建状态栏的管理实例
//            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
//            //激活状态栏设置
//            tintManager.setStatusBarTintEnabled(true);
//            //设置状态栏颜色
//            tintManager.setTintResource(color);
            setStatusBarColor(activity, color);//阴影绘制
//            //透明导航栏
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            //激活导航栏设置
//            tintManager.setNavigationBarTintEnabled(true);
//            //设置导航栏颜色
//            tintManager.setNavigationBarTintResource(color);
        }
    }

    /**
     * 设置状态栏颜色
     * 也就是所谓沉浸式状态栏
     */
    public static void setStatusBarColor(Activity activity, int color) {
        /**
         * Android4.4以上可用
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintResource(color);
            tintManager.setStatusBarTintEnabled(true);
            //Window window = getWindow();
            //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(color);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        immerge(ConfirmPatternActivity.this, R.color.lightSkyBlue);
        mMessageText.setText(R.string.pl_draw_pattern_to_unlock);
        mPatternView.setInStealthMode(isStealthModeEnabled());
        mPatternView.setOnPatternListener(this);
        mLeftButton.setText(R.string.pl_cancel);
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
        mRightButton.setText(R.string.pl_forgot_pattern);
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onForgotPassword();
            }
        });
        ViewAccessibilityCompat.announceForAccessibility(mMessageText, mMessageText.getText());

        if (savedInstanceState == null) {
            numFailedAttempts = 0;
        } else {
            numFailedAttempts = savedInstanceState.getInt(KEY_NUM_FAILED_ATTEMPTS);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_NUM_FAILED_ATTEMPTS, numFailedAttempts);
    }

    @Override
    public void onPatternStart() {

        removeClearPatternRunnable();

        // Set display mode to correct to ensure that pattern can be in stealth mode.
        mPatternView.setDisplayMode(PatternView.DisplayMode.Correct);
    }

    @Override
    public void onPatternCellAdded(List<PatternView.Cell> pattern) {}

    @Override
    public void onPatternDetected(List<PatternView.Cell> pattern) {
        if (isPatternCorrect(pattern)) {
            onConfirmed();
        } else {
            mMessageText.setText(R.string.pl_wrong_pattern);
            mPatternView.setDisplayMode(PatternView.DisplayMode.Wrong);
            postClearPatternRunnable();
            ViewAccessibilityCompat.announceForAccessibility(mMessageText, mMessageText.getText());
            onWrongPattern();
        }
    }

    @Override
    public void onPatternCleared() {
        removeClearPatternRunnable();
    }

    protected boolean isStealthModeEnabled() {
        return false;
    }

    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        return true;
    }

    protected void onConfirmed() {
        setResult(RESULT_OK);
        finish();
    }

    protected void onWrongPattern() {
        ++numFailedAttempts;
    }

    protected void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void onForgotPassword() {
        setResult(RESULT_FORGOT_PASSWORD);
        finish();
    }
}
