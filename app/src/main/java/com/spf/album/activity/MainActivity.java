package com.spf.album.activity;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;

import com.spf.album.ImageFile;
import com.spf.album.R;
import com.spf.album.databinding.ActivityMainBinding;
import com.spf.album.event.LatLntImageClickEvent;
import com.spf.album.fragment.AlbumFragment;
import com.spf.album.fragment.LocationFragment;
import com.spf.album.fragment.PhotoFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
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
    private LocationFragment locationFragment;
    private int currentTab = TAB_PHOTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initFragment(savedInstanceState);
        initBottomBar();
        EventBus.getDefault().register(this);

        if (EasyPermissions.hasPermissions(this, permissions)) {
            LoaderManager.getInstance(this).initLoader(0, null, mLoaderCallback);
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
            LoaderManager.getInstance(this).initLoader(0, null, mLoaderCallback);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE) {
            finish();
        }
    }

    private void initFragment(Bundle bundle) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (bundle != null) {
            photoFragment = (PhotoFragment) fragmentManager.getFragment(bundle, KEY_TAB + TAB_PHOTO);
            albumFragment = (AlbumFragment) fragmentManager.getFragment(bundle, KEY_TAB + TAB_ALBUM);
            locationFragment = (LocationFragment) fragmentManager.getFragment(bundle, KEY_TAB + TAB_LOCATION);
        } else {
            photoFragment = new PhotoFragment();
            albumFragment = new AlbumFragment();
            locationFragment = new LocationFragment();
        }

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(photoFragment);
        fragments.add(albumFragment);
        fragments.add(locationFragment);
        binding.viewPager.setAdapter(new MainPageAdapter(fragmentManager, fragments));
        binding.viewPager.setOffscreenPageLimit(fragments.size());
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

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
        binding.llPhoto.setOnClickListener(this);
        binding.llAlbum.setOnClickListener(this);
        binding.llLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int tab = -1;
        int id = v.getId();
        if (id == R.id.ll_photo) {
            tab = TAB_PHOTO;
        } else if (id == R.id.ll_album) {
            tab = TAB_ALBUM;
        } else if (id == R.id.ll_location) {
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
            binding.ivPhoto.setImageResource(R.drawable.ic_tab_photo_selected);
            binding.tvPhoto.setTextColor(getResources().getColor(R.color.tab_text_color_select));
            binding.ivAlbum.setImageResource(R.drawable.ic_tab_album_normal);
            binding.tvAlbum.setTextColor(getResources().getColor(R.color.tab_text_color_normal));
            binding.ivLocation.setImageResource(R.drawable.ic_tab_location_normal);
            binding.tvLocation.setTextColor(getResources().getColor(R.color.tab_text_color_normal));
        } else if (currentTab == TAB_ALBUM) {
            binding.ivPhoto.setImageResource(R.drawable.ic_tab_photo_normal);
            binding.tvPhoto.setTextColor(getResources().getColor(R.color.tab_text_color_normal));
            binding.ivAlbum.setImageResource(R.drawable.ic_tab_album_selected);
            binding.tvAlbum.setTextColor(getResources().getColor(R.color.tab_text_color_select));
            binding.ivLocation.setImageResource(R.drawable.ic_tab_location_normal);
            binding.tvLocation.setTextColor(getResources().getColor(R.color.tab_text_color_normal));
        } else if (currentTab == TAB_LOCATION) {
            binding.ivPhoto.setImageResource(R.drawable.ic_tab_photo_normal);
            binding.tvPhoto.setTextColor(getResources().getColor(R.color.tab_text_color_normal));
            binding.ivAlbum.setImageResource(R.drawable.ic_tab_album_normal);
            binding.tvAlbum.setTextColor(getResources().getColor(R.color.tab_text_color_normal));
            binding.ivLocation.setImageResource(R.drawable.ic_tab_location_selected);
            binding.tvLocation.setTextColor(getResources().getColor(R.color.tab_text_color_select));
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
        EventBus.getDefault().unregister(this);
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            final String SELECTION = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";
            final String[] SELECTION_ARGS = {
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
            Uri uri = MediaStore.Files.getContentUri("external");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri = MediaStore.setRequireOriginal(uri);
            }
            return new CursorLoader(getApplicationContext(), uri, ImageFile.IMAGE_PROJECTION,
                    SELECTION, SELECTION_ARGS, ImageFile.IMAGE_PROJECTION[3] + " DESC");
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            if (!isFinishing() && data != null) {
                int count = data.getCount();
                if (count > 0) {
                    List<ImageFile> allImageFileList = new ArrayList<>();
                    List<ImageFile> cameraImageFileList = new ArrayList<>();
                    String cameraPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera").getAbsolutePath();
                    data.moveToFirst();
                    do {
                        ImageFile imageFile = ImageFile.createImageFile(data);
                        if (imageFile != null) {
                            allImageFileList.add(imageFile);
                            if (imageFile.getPath().contains(cameraPath)) {
                                cameraImageFileList.add(imageFile);
                            }
                        }
                    } while (data.moveToNext());
                    photoFragment.setImageFileList(cameraImageFileList);
                    albumFragment.setImageFileList(allImageFileList);
                    locationFragment.setImageFileList(cameraImageFileList);
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        }
    };

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