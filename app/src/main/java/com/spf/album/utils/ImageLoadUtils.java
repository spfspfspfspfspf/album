package com.spf.album.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.spf.album.GalleryApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ImageLoadUtils {
    private static ImageLoader imageLoader = new GlideImageLoader();

    private ImageLoadUtils() {
    }

    public static void loadImage(ImageBuilder imageBuilder) {
        imageLoader.loadImage(imageBuilder);
    }

    public static Bitmap getBitmap(ImageBuilder imageBuilder) {
        return imageLoader.getBitmap(imageBuilder);
    }

    public static void saveToGallery(Bitmap bitmap) {
        // 首先保存图片
        File file = null;
        String fileName = System.currentTimeMillis() + ".jpg";
        File root = new File(Environment.getExternalStorageDirectory(), GalleryApplication.getApplication().getPackageName());
        File dir = new File(root, "images");
        if (dir.mkdirs() || dir.isDirectory()) {
            file = new File(dir, fileName);
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(GalleryApplication.getApplication().getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 通知图库更新
        MediaScannerConnection.scanFile(GalleryApplication.getApplication(), new String[]{file.getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(uri);
                        GalleryApplication.getApplication().sendBroadcast(mediaScanIntent);
                    }
                });
    }

    public static class ImageBuilder {
        private Context context;
        private Uri uri;
        private ImageView imageView;
        private ImageView.ScaleType scaleType;
        private int width;
        private int height;
        private int roundCorner;
        private int placeHolder;

        public ImageBuilder(Context context, Uri uri) {
            this.context = context;
            this.uri = uri;
        }

        public ImageBuilder(Context context, Uri uri, ImageView imageView) {
            this(context, uri);
            this.imageView = imageView;
        }

        public ImageBuilder setScaleType(ImageView.ScaleType scaleType) {
            this.scaleType = scaleType;
            return this;
        }

        public ImageBuilder setRoundCorner(int roundCorner) {
            this.roundCorner = roundCorner;
            return this;
        }

        public ImageBuilder setPlaceHolder(int placeHolder) {
            this.placeHolder = placeHolder;
            return this;
        }

        public ImageBuilder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }
    }

    private interface ImageLoader {
        void loadImage(ImageBuilder imageBuilder);

        Bitmap getBitmap(ImageBuilder imageBuilder);
    }

    private static class GlideImageLoader implements ImageLoader {
        @Override
        public void loadImage(ImageBuilder imageBuilder) {
            RequestBuilder<Drawable> requestBuilder = Glide.with(imageBuilder.context).load(imageBuilder.uri);
            if (imageBuilder.placeHolder > 0) {
                requestBuilder = requestBuilder.placeholder(imageBuilder.placeHolder);
            }

            Transformation<Bitmap>[] transformations = getTransformations(imageBuilder);
            if (transformations != null) {
                requestBuilder = requestBuilder.transform(transformations);
            }

            if (imageBuilder.width > 0 && imageBuilder.height > 0) {
                requestBuilder = requestBuilder.override(imageBuilder.width, imageBuilder.height);
            }

            requestBuilder.into(imageBuilder.imageView);
        }

        @Override
        public Bitmap getBitmap(ImageBuilder imageBuilder) {
            RequestBuilder<Bitmap> requestBuilder = Glide.with(imageBuilder.context).asBitmap().load(imageBuilder.uri);
            if (imageBuilder.placeHolder > 0) {
                requestBuilder = requestBuilder.placeholder(imageBuilder.placeHolder);
            }

            // TODO: 2021/3/17 DrawImageView 获取 Bitmap 时用 transform() 方法无效
            requestBuilder = requestBuilder.fitCenter();
//            Transformation<Bitmap>[] transformations = getTransformations(imageBuilder);
//            if (transformations != null) {
//                requestBuilder = requestBuilder.transform(transformations);
//            }

            if (imageBuilder.width > 0 && imageBuilder.height > 0) {
                requestBuilder = requestBuilder.override(imageBuilder.width, imageBuilder.height);
            }

            try {
                return requestBuilder.submit().get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Transformation<Bitmap>[] getTransformations(ImageBuilder imageBuilder) {
            Transformation<Bitmap>[] transformations = null;
            Transformation<Bitmap> scaleTypeTransform = getScaleTypeTransform(imageBuilder.scaleType);
            if (scaleTypeTransform != null && imageBuilder.roundCorner > 0) {
                transformations = new BitmapTransformation[2];
                transformations[0] = scaleTypeTransform;
                transformations[1] = new RoundedCorners(imageBuilder.roundCorner);
            } else if (scaleTypeTransform != null) {
                transformations = new BitmapTransformation[1];
                transformations[0] = scaleTypeTransform;
            } else if (imageBuilder.roundCorner > 0) {
                transformations = new BitmapTransformation[1];
                transformations[0] = new RoundedCorners(imageBuilder.roundCorner);
            }
            return transformations;
        }

        private Transformation<Bitmap> getScaleTypeTransform(ImageView.ScaleType scaleType) {
            if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                return new CenterCrop();
            } else if (scaleType == ImageView.ScaleType.CENTER_INSIDE) {
                return new CenterInside();
            } else if (scaleType == ImageView.ScaleType.FIT_CENTER) {
                return new FitCenter();
            }
            return null;
        }
    }
}