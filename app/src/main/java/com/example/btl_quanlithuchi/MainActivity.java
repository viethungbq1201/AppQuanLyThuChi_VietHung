package com.example.btl_quanlithuchi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    TabLayout tab_layout;
    ViewPager2 view_pager;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyWalletPrefs";
    private static final String THEME_MODE = "theme_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khôi phục theme từ SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int savedTheme = sharedPreferences.getInt(THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        setContentView(R.layout.activity_main);

        // Áp dụng theme ngay lập tức
        if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Chỉ hiển thị icon ứng dụng, không hiện chữ
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        addControl();
    }

    private void addControl() {
        tab_layout = findViewById(R.id.tab_layout);
        view_pager = findViewById(R.id.view_pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        view_pager.setAdapter(adapter);

        String[] tabTitles = {"Home", "Income", "Expense", "Note"};

        new TabLayoutMediator(tab_layout, view_pager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Cập nhật icon menu dựa trên theme hiện tại
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        MenuItem themeItem = menu.findItem(R.id.action_theme);

        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeItem.setIcon(R.drawable.ic_sun);
        } else {
            themeItem.setIcon(R.drawable.ic_moon);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_theme) {
            toggleTheme();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        int newNightMode;

        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
        }

        // Lưu theme vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(THEME_MODE, newNightMode);
        editor.apply();

        // Áp dụng theme mới
        AppCompatDelegate.setDefaultNightMode(newNightMode);

        // Tái tạo activity để áp dụng theme
        recreate();
    }
}