package com.huantansheng.easyphotos.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.result.Result;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.ui.widget.PressedImageView;
import com.huantansheng.easyphotos.utils.media.MediaUtils;
import com.huantansheng.easyphotos.utils.uri.UriUtils;

/**
 * 预览所有选中图片集合的适配器
 * Created by huan on 2017/12/1.
 */

public class PreviewPhotosFragmentAdapter extends RecyclerView.Adapter<PreviewPhotosFragmentAdapter.PreviewPhotoVH> {
    private final LayoutInflater inflater;
    private final OnClickListener listener;
    private int checkedPosition = -1;

    public PreviewPhotosFragmentAdapter(Context context, OnClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }


    @Override
    public PreviewPhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PreviewPhotoVH(inflater.inflate(R.layout.item_preview_selected_photos_easy_photos, parent, false));
    }

    @Override
    public void onBindViewHolder(PreviewPhotoVH holder, int position) {
        final int p = position;
        final Photo photo = Result.getPhoto(p);
        final String path = photo.getAvailablePath();
        final String type = photo.type;
        final long duration = photo.duration;

        final boolean isGif = path.endsWith(Type.GIF) || type.endsWith(Type.GIF);
        Uri uri = UriUtils.getUriByPath(path);
        if (Setting.showGif && isGif) {
            Setting.imageEngine.loadGifAsBitmap(holder.ivPhoto.getContext(), uri, holder.ivPhoto);
            holder.tvType.setText(R.string.gif_easy_photos);
            holder.tvType.setVisibility(View.VISIBLE);
        } else if (Setting.showVideo() && type.contains(Type.VIDEO)) {
            Setting.imageEngine.loadPhoto(holder.ivPhoto.getContext(), uri, holder.ivPhoto);
            holder.tvType.setText(MediaUtils.format(duration));
            holder.tvType.setVisibility(View.VISIBLE);
        } else {
            Setting.imageEngine.loadPhoto(holder.ivPhoto.getContext(), uri, holder.ivPhoto);
            holder.tvType.setVisibility(View.GONE);
        }

        if (checkedPosition == p) {
            holder.frame.setVisibility(View.VISIBLE);
        } else {
            holder.frame.setVisibility(View.GONE);
        }
        holder.ivPhoto.setOnClickListener(view ->
                listener.onPhotoClick(p)
        );
    }

    @Override
    public int getItemCount() {
        return Result.count();
    }

    public void setChecked(int position) {
        if (checkedPosition == position) {
            return;
        }
        checkedPosition = position;
        notifyDataSetChanged();
    }

    class PreviewPhotoVH extends RecyclerView.ViewHolder {
        PressedImageView ivPhoto;
        View frame;
        TextView tvType;

        public PreviewPhotoVH(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            frame = itemView.findViewById(R.id.v_selector);
            tvType = itemView.findViewById(R.id.tv_type);
        }
    }

    public interface OnClickListener {
        void onPhotoClick(int position);
    }
}
