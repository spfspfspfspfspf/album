package com.spf.album.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.concurrent.ExecutionException;

public class ImageLoadUtils {
    private static ImageLoader imageLoader = new GlideImageLoader();

    private ImageLoadUtils() {
    }

    public static void loadImage(ImageBuilder imageBuilder) {
        imageLoader.loadImage(imageBuilder);
    }

    public static Bitmap getBitmap(ImageBuilder imageBuilder) throws Exception {
        return imageLoader.getBitmap(imageBuilder);
    }

    public static class ImageBuilder {
        private Context context;
        private Uri uri;
        private ImageView imageView;
        private int width;
        private int height;
        private ImageView.ScaleType scaleType;
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

        Bitmap getBitmap(ImageBuilder imageBuilder) throws Exception;
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
        public Bitmap getBitmap(ImageBuilder imageBuilder) throws ExecutionException, InterruptedException {
            RequestBuilder<Bitmap> requestBuilder = Glide.with(imageBuilder.context).asBitmap().load(imageBuilder.uri);
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

            return requestBuilder.submit().get();
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
            } else if (scaleType == ImageView.ScaleType.FIT_CENTER) {
                return new FitCenter();
            }
            return null;
        }
    }
}