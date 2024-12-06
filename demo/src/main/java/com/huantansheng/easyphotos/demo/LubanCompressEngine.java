package com.huantansheng.easyphotos.demo;

import android.content.Context;
import android.text.TextUtils;

import com.huantansheng.easyphotos.callback.CompressCallback;
import com.huantansheng.easyphotos.engine.CompressEngine;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;

public class LubanCompressEngine implements CompressEngine {
    //单例
    private volatile static LubanCompressEngine instance = null;

    //单例模式，私有构造方法
    private LubanCompressEngine() {
    }

    //获取单例
    public static LubanCompressEngine getInstance() {
        if (null == instance) {
            synchronized (LubanCompressEngine.class) {
                if (null == instance) {
                    instance = new LubanCompressEngine();
                }
            }
        }
        return instance;
    }

    private String getPath(Context context) {
        String path = context.getFilesDir() + "/Luban/image/";
        new File(path).mkdirs();
        return path;
    }

    @Override
    public void compress(final Context context, final ArrayList<Photo> photos, final CompressCallback callback) {
        //TODO 演示使用只简单进行图片压缩，根据实际使用情况修改
        callback.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<String> paths = new ArrayList<>();
                    for (Photo photo : photos) {
                        if (photo.selectedOriginal) continue;
                        if (!TextUtils.isEmpty(photo.cropPath)) {
                            paths.add(photo.cropPath);
                        } else {
                            paths.add(photo.filePath);
                        }
                    }
                    if (paths.isEmpty()) {
                        callback.onSuccess(photos);
                        return;
                    }

                    List<File> files = Luban.with(context).load(paths)
                            .ignoreBy(100)
                            .setTargetDir(getPath(context))
                            .filter(new CompressionPredicate() {
                                @Override
                                public boolean apply(String path) {
                                    return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif") || path.toLowerCase().endsWith(".mp4"));
                                }
                            }).get();
                    for (int i = 0, j = photos.size(); i < j; i++) {
                        Photo photo = photos.get(i);
                        photo.compressPath = files.get(i).getPath();
                    }
                    callback.onSuccess(photos);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onFailed(photos, e.getMessage());
                }
            }
        }).start();
    }
}
