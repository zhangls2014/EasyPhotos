package com.huantansheng.easyphotos.demo;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huantansheng.easyphotos.engine.ImageEngine;

/**
 * Glide4.x的加载图片引擎实现,单例模式
 * Glide4.x的缓存机制更加智能，已经达到无需配置的境界。如果使用Glide3.x，需要考虑缓存机制。
 * Created by huan on 2018/1/15.
 */
public class GlideEngine implements ImageEngine {
    //单例
    private static volatile GlideEngine instance = null;

    //单例模式，私有构造方法
    private GlideEngine() {
    }

    //获取单例
    public static GlideEngine getInstance() {
        if (null == instance) {
            synchronized (GlideEngine.class) {
                if (null == instance) {
                    instance = new GlideEngine();
                }
            }
        }
        return instance;
    }

    @Override
    public void loadPhoto(Context context, Uri uri, ImageView imageView) {
        Glide.with(context).load(uri).transition(withCrossFade()).into(imageView);
    }

    @Override
    public void loadGifAsBitmap(Context context, Uri uri, ImageView imageView) {
        Glide.with(context).asBitmap().load(uri).into(imageView);
    }

    @Override
    public void loadGif(Context context, Uri uri, ImageView imageView) {
        Glide.with(context).asGif().load(uri).transition(withCrossFade()).into(imageView);
    }

    @Override
    public Bitmap getCacheBitmap(Context context, Uri uri, int width, int height) throws Exception {
        return Glide.with(context).asBitmap().load(uri).submit(width, height).get();
    }

}
