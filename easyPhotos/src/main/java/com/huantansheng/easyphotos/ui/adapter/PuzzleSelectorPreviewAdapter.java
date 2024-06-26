package com.huantansheng.easyphotos.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.utils.media.MediaUtils;
import com.huantansheng.easyphotos.utils.uri.UriUtils;

import java.util.ArrayList;

/**
 * 拼图相册适配器
 * Created by huan on 2017/10/23.
 */

public class PuzzleSelectorPreviewAdapter extends RecyclerView.Adapter {


    private final ArrayList<Photo> dataList;
    private final LayoutInflater mInflater;
    private final OnClickListener listener;


    public PuzzleSelectorPreviewAdapter(Context cxt, ArrayList<Photo> dataList, OnClickListener listener) {
        this.dataList = dataList;
        this.listener = listener;
        this.mInflater = LayoutInflater.from(cxt);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new PhotoViewHolder(mInflater.inflate(R.layout.item_puzzle_selector_preview_easy_photos, parent, false));

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final int p = position;
        final Photo photo = dataList.get(position);
        final String path = photo.getAvailablePath();
        final String type = photo.type;
        final long duration = photo.duration;
        final boolean isGif = path.endsWith(Type.GIF) || type.endsWith(Type.GIF);
        Uri uri = UriUtils.getUriByPath(path);
        if (Setting.showGif && isGif) {
            Setting.imageEngine.loadGifAsBitmap(((PhotoViewHolder) holder).ivPhoto.getContext(), uri, ((PhotoViewHolder) holder).ivPhoto);
            ((PhotoViewHolder) holder).tvType.setText(R.string.gif_easy_photos);
            ((PhotoViewHolder) holder).tvType.setVisibility(View.VISIBLE);
        } else if (Setting.showVideo() && type.contains(Type.VIDEO)) {
            Setting.imageEngine.loadPhoto(((PhotoViewHolder) holder).ivPhoto.getContext(), uri, ((PhotoViewHolder) holder).ivPhoto);
            ((PhotoViewHolder) holder).tvType.setText(MediaUtils.format(duration));
            ((PhotoViewHolder) holder).tvType.setVisibility(View.VISIBLE);
        } else {
            Setting.imageEngine.loadPhoto(((PhotoViewHolder) holder).ivPhoto.getContext(), uri, ((PhotoViewHolder) holder).ivPhoto);
            ((PhotoViewHolder) holder).tvType.setVisibility(View.GONE);
        }
        ((PhotoViewHolder) holder).ivDelete.setOnClickListener(v -> listener.onDeleteClick(p));
    }


    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size();
    }


    public interface OnClickListener {
        void onDeleteClick(int position);
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageView ivDelete;
        TextView tvType;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            this.ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            this.ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            this.tvType = (TextView) itemView.findViewById(R.id.tv_type);
        }
    }
}
