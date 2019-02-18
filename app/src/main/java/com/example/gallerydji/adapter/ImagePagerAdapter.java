package com.example.gallerydji.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gallerydji.R;
import com.example.gallerydji.activity.PhotoPreActivity;
import com.example.gallerydji.bean.Photo;
import com.google.android.exoplayer2.ExoPlaybackException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chuangyuan.ycj.videolibrary.listener.VideoInfoListener;
import chuangyuan.ycj.videolibrary.video.ManualPlayer;
import chuangyuan.ycj.videolibrary.widget.VideoPlayerView;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.bumptech.glide.request.RequestOptions.centerCropTransform;

public class ImagePagerAdapter extends PagerAdapter {

    private Context         mContext;
    private List<PhotoView> viewList = new ArrayList<>(4);

    private List<View> videoviewList = new ArrayList<>(4);

    List<Photo> mImgList;
    private OnItemClickListener mListener;

    public static VideoPlayerView videoPlayerView;
    public static ManualPlayer    exoPlayerManager;

    public ImagePagerAdapter(Context context, List<Photo> imgList) {
        this.mContext = context;
        createImageViews();
        createVideoViews();
        mImgList = imgList;
    }

    private void createImageViews() {
        for (int i = 0; i < 4; i++) {
            PhotoView imageView = new PhotoView(mContext);
            //            View imageView = new View(mContext);
            imageView.setAdjustViewBounds(true);

            viewList.add(imageView);
        }
    }

    private void createVideoViews() {
        for (int i = 0; i < 4; i++) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_preview_video, null, false);

            videoPlayerView = view.findViewById(R.id.exo_play_context_id);
            exoPlayerManager = new ManualPlayer((Activity) mContext, videoPlayerView);

            videoviewList.add(view);
        }
    }

    @Override
    public int getCount() {
        return mImgList == null ? 0 : mImgList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof PhotoView) {
            PhotoView view = (PhotoView) object;
            view.setImageDrawable(null);
            viewList.add(view);
            container.removeView((View) view);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        // 最简单解决 notifyDataSetChanged() 页面不刷新问题的方法
        PhotoView view = (PhotoView) object;

        int currentPage = PhotoPreActivity.deletePosition;

        if (mImgList.get(currentPage).getMimeType().contains("mp4")) {

        }

        if (currentPage == (Integer) view.getTag()) {
            return POSITION_NONE;
        } else {
            return POSITION_UNCHANGED;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        if (mImgList.get(position).getMimeType().contains("mp4")) {

            //            if (new View().getTag())

            if (videoviewList.size() == 0) {
                createVideoViews();
            }

            if (videoPlayerView.getTag() == null) {
                View view = videoviewList.remove(0);

                videoPlayerView = view.findViewById(R.id.exo_play_context_id);
                exoPlayerManager = new ManualPlayer((Activity) mContext, videoPlayerView);

                videoPlayerView.setTag(position);

                view.setTag(videoPlayerView);

                container.addView(view);

                videoPlayerView.setClickable(true);

                videoPlayerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onItemClick(position, v);
                        }
                    }
                });

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onItemClick(position, v);
                        }
                    }
                });


                exoPlayerManager.setPlayUri(mImgList.get(position).getPath());

                exoPlayerManager.setLooping(10);

                RequestOptions myOptions = new RequestOptions()
                        .fitCenter()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);

                Glide.with(mContext)
                        .load(mImgList.get(position))
                        .transition(withCrossFade())            // .crossFade()()
                        .apply(centerCropTransform())           // .centerCrop()

                        .apply(myOptions)

                        .thumbnail(0.5f)
                        .into(videoPlayerView.getPreviewImage());

                videoPlayerView.getPreviewImage().setScaleType(ImageView.ScaleType.CENTER);

                exoPlayerManager.addVideoInfoListener(new VideoInfoListener() {
                    @Override
                    public void onPlayStart(long currPosition) {

                    }

                    @Override
                    public void onLoadingChanged() {

                    }

                    @Override
                    public void onPlayerError(@Nullable ExoPlaybackException e) {

                    }

                    @Override
                    public void onPlayEnd() {
                        exoPlayerManager.onDestroy();
                    }

                    @Override
                    public void isPlaying(boolean playWhenReady) {

                    }
                });
                return view;
            }  else {
                return videoPlayerView.getTag();
            }

        } else {
            viewList.remove(0);

            final PhotoView currentView = new PhotoView(mContext);

            final Photo image = mImgList.get(position);

            currentView.setTag(position);

            container.addView(currentView);

            Glide.with(mContext)
                    .load(image.getPath())
                    //                .error(R.mipmap.ic_launcher)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            currentView.setImageDrawable(resource);
                        }
                    });

            currentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(position, v);
                    }
                }
            });

            currentView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    if (mListener != null) {
                        mListener.onItemClick(position, view);
                    }
                }
            });
            return currentView;
        }
    }


    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    public interface OnItemClickListener {
        //        void onItemClick(int position, Photo image);
        void onItemClick(int position, View view);

    }

}
