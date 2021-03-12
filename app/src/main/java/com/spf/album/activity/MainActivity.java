package com.spf.album.activity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.spf.album.ImageFileLoader;
import com.spf.album.R;
import com.spf.album.databinding.ActivityMainBinding;
import com.spf.album.event.LatLntImageClickEvent;
import com.spf.album.fragment.AlbumFragment;
import com.spf.album.fragment.GaoDeLocationFragment;
import com.spf.album.fragment.PhotoFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private static final String KEY_TAB = "key_tab";
    private static final int TAB_PHOTO = 0;
    private static final int TAB_ALBUM = 1;
    private static final int TAB_LOCATION = 2;

    private final int REQUEST_CODE = 10;
    private final String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ActivityMainBinding binding;
    private PhotoFragment photoFragment;
    private AlbumFragment albumFragment;
    private GaoDeLocationFragment locationFragment;
    private int currentTab = TAB_PHOTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initFragment(savedInstanceState);
        initBottomBar();
        EventBus.getDefault().register(this);

        if (EasyPermissions.hasPermissions(this, permissions)) {
            ImageFileLoader.getInstance().init();
        } else {
            EasyPermissions.requestPermissions(this, "为了正常使用应用，需要读写存储权限", REQUEST_CODE, permissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE) {
            ImageFileLoader.getInstance().init();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE) {
            finish();
        }
    }

    private void initStatusBar() {
        //binding.viewPager.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void initFragment(Bundle bundle) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (bundle != null) {
            photoFragment = (PhotoFragment) fragmentManager.getFragment(bundle, KEY_TAB + TAB_PHOTO);
            albumFragment = (AlbumFragment) fragmentManager.getFragment(bundle, KEY_TAB + TAB_ALBUM);
            locationFragment = (GaoDeLocationFragment) fragmentManager.getFragment(bundle, KEY_TAB + TAB_LOCATION);
        } else {
            photoFragment = new PhotoFragment();
            albumFragment = new AlbumFragment();
            locationFragment = new GaoDeLocationFragment();
        }

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(photoFragment);
        fragments.add(albumFragment);
        fragments.add(locationFragment);
        binding.viewPager.setAdapter(new MainPageAdapter(fragmentManager, fragments));
        binding.viewPager.setOffscreenPageLimit(fragments.size());
        binding.viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentTab = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                setTabBackground();
            }
        });
    }

    private void initBottomBar() {
        binding.barPhoto.setOnClickListener(this);
        binding.barAlbum.setOnClickListener(this);
        binding.barLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int tab = -1;
        int id = v.getId();
        if (id == R.id.bar_photo) {
            tab = TAB_PHOTO;
        } else if (id == R.id.bar_album) {
            tab = TAB_ALBUM;
        } else if (id == R.id.bar_location) {
            tab = TAB_LOCATION;
        }
        if (currentTab != tab) {
            currentTab = tab;
            setTabBackground();
            binding.viewPager.setCurrentItem(currentTab);
        }
    }

    private void setTabBackground() {
        if (currentTab == TAB_PHOTO) {
            binding.barPhoto.setSelect(true);
            binding.barAlbum.setSelect(false);
            binding.barLocation.setSelect(false);
        } else if (currentTab == TAB_ALBUM) {
            binding.barPhoto.setSelect(false);
            binding.barAlbum.setSelect(true);
            binding.barLocation.setSelect(false);
        } else if (currentTab == TAB_LOCATION) {
            binding.barPhoto.setSelect(false);
            binding.barAlbum.setSelect(false);
            binding.barLocation.setSelect(true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (photoFragment != null && photoFragment.isAdded()
                && albumFragment != null && albumFragment.isAdded()
                && locationFragment != null && locationFragment.isAdded()) {
            fragmentManager.putFragment(outState, KEY_TAB + TAB_PHOTO, photoFragment);
            fragmentManager.putFragment(outState, KEY_TAB + TAB_ALBUM, albumFragment);
            fragmentManager.putFragment(outState, KEY_TAB + TAB_LOCATION, locationFragment);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventLatLntImageClick(LatLntImageClickEvent event) {
        currentTab = TAB_PHOTO;
        setTabBackground();
        binding.viewPager.setCurrentItem(currentTab);
        photoFragment.scrollToImage(event.getImageFile());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageFileLoader.getInstance().clear();
        EventBus.getDefault().unregister(this);
    }

    static class MainPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        MainPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment getItem(int arg0) {
            return fragments.get(arg0);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}