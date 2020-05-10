package com.spf.album.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.concurrent.ExecutionException;

public class ImageLoadUtils {
    private ImageLoadUtils() {
    }

    public static void loadImage(ImageBuilder imageBuilder) {
        RequestBuilder<Drawable> requestBuilder = Glide.with(imageBuilder.context).load(imageBuilder.uri);
        if (imageBuilder.placeHolder > 0) {
            requestBuilder = requestBuilder.placeholder(imageBuilder.placeHolder);
        }

        if (imageBuilder.scaleType != null && imageBuilder.roundedCorners != null) {
            requestBuilder = requestBuilder.transform(imageBuilder.scaleType, imageBuilder.roundedCorners);
        } else if (imageBuilder.scaleType != null) {
            requestBuilder = requestBuilder.transform(imageBuilder.scaleType);
        } else if (imageBuilder.roundedCorners != null) {
            requestBuilder = requestBuilder.transform(imageBuilder.roundedCorners);
        }

        if(imageBuilder.width > 0 && imageBuilder.height > 0) {
            requestBuilder = requestBuilder.override(imageBuilder.width, imageBuilder.height);
        }

        requestBuilder.into(imageBuilder.imageView);
    }

    public static Bitmap getBitmap(ImageBuilder imageBuilder) throws ExecutionException, InterruptedException {
        RequestBuilder<Bitmap> requestBuilder = Glide.with(imageBuilder.context).asBitmap().load(imageBuilder.uri);
        if (imageBuilder.placeHolder > 0) {
            requestBuilder = requestBuilder.placeholder(imageBuilder.placeHolder);
        }

        if (imageBuilder.scaleType != null && imageBuilder.roundedCorners != null) {
            requestBuilder = requestBuilder.transform(imageBuilder.scaleType, imageBuilder.roundedCorners);
        } else if (imageBuilder.scaleType != null) {
            requestBuilder = requestBuilder.transform(imageBuilder.scaleType);
        } else if (imageBuilder.roundedCorners != null) {
            requestBuilder = requestBuilder.transform(imageBuilder.roundedCorners);
        }

        if(imageBuilder.width > 0 && imageBuilder.height > 0) {
            requestBuilder = requestBuilder.override(imageBuilder.width, imageBuilder.height);
        }

        return requestBuilder.submit().get();
    }

    public static class ImageBuilder {
        private Context context;
        private Uri uri;
        private ImageView imageView;
        private int width;
        private int height;
        private BitmapTransformation scaleType;
        private BitmapTransformation roundedCorners;
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
            if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                this.scaleType = new CenterCrop();
            } else if (scaleType == ImageView.ScaleType.FIT_CENTER) {
                this.scaleType = new FitCenter();
            }
            return this;
        }

        public ImageBuilder setRoundCorner(int roundCorner) {
            if (roundCorner > 0) {
                this.roundedCorners = new RoundedCorners(roundCorner);
            }
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
}