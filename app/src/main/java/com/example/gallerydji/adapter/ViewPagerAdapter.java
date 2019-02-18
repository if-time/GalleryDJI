package com.example.gallerydji.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gallerydji.R;
import com.example.gallerydji.activity.MainActivity;
import com.example.gallerydji.bean.Photo;
import com.example.gallerydji.util.UniversalImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ViewPagerAdapter extends PagerAdapter {

    private Context     mContext;
    private Activity    mActivity;
    private List<Photo> mImgList;
    private boolean     showToolbar = false;
    private ActionBar   mActionBar;

    private MediaController mediaController;

    boolean isPlay = false;

    private boolean isShowPlayBtn = true;


    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    public interface OnItemClickListener {
        //        void onItemClick(int position, Photo image);
        void onItemClick(int position, View view);

    }


    public ViewPagerAdapter(Context context, List<Photo> imgList) {
        this.mContext = context;
        this.mImgList = imgList;
        this.mActivity = (Activity) context;

    }

    @Override
    public int getCount() {
        return mImgList == null ? 0 : mImgList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View imageLayout = LayoutInflater.from(mContext).inflate(R.layout.item_image_pager, null);
        assert imageLayout != null;

        final ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.loading);
        PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.photoview);
        final ImageView imgPlay = (ImageView) imageLayout.findViewById(R.id.img_play);
        final VideoView videoView = (VideoView) imageLayout.findViewById(R.id.video_view);

        Photo photo = mImgList.get(position);

        final String path = photo.getPath();

        String thumbFromCP = photo.getPath();


        if (mImgList.get(position).getMimeType().split("/")[1] == "MOV" ||
                mImgList.get(position).getMimeType().split("/")[1] == "mp4") {

            Log.i("!!!!!", "instantiateItem: " + mImgList.get(position).getMimeType().split("/")[1]);

            videoView.setVisibility(View.VISIBLE);
            imgPlay.setVisibility(View.VISIBLE);
            imgPlay.setImageResource(R.drawable.ic_play);
            photoView.setVisibility(View.GONE);

            //            videoView.setBackgroundDrawable(Drawable.createFromPath(createVideoThumbnail(uri)));
            videoView.setVideoPath(path);


            Log.d("VV", "thumbFromCP >> " + thumbFromCP);

            videoView.setBackgroundDrawable(Drawable.createFromPath(createVideoThumbnail(thumbFromCP, path)));

            imgPlay.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:

                            if (!isPlay) {
                                videoView.setBackgroundColor(Color.parseColor("#00000000"));
                                videoView.start();
                                imgPlay.setImageResource(R.drawable.ic_activity_pause);
                                imgPlay.setVisibility(View.GONE);
                                isShowPlayBtn = false;
                                isPlay = true;

                            } else {
                                imgPlay.setImageResource(R.drawable.ic_album_play);
                                videoView.pause();
                                isPlay = false;
                            }
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            return false;
                    }


                    return false;
                }
            });

            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            Log.d("ViewPagerAdapter", "ACTION_DOWN");
                            return true;

                        case MotionEvent.ACTION_UP:
                            Log.d("ViewPagerAdapter", "ACTION_UP");
                            Log.d("ViewPagerAdapter", "isShowPlayBtn:" + isShowPlayBtn);
                            if (isShowPlayBtn) {
                                imgPlay.setVisibility(View.GONE);
                            } else {
                                imgPlay.setVisibility(View.VISIBLE);
                            }
                            isShowPlayBtn = !isShowPlayBtn;


                            return true;

                        case MotionEvent.ACTION_MOVE:
                            return false;
                    }
                    return false;
                }
            });

            videoView.requestFocus();

        } else {
            Log.i("!!!!!", "instantiateItem: " + mImgList.get(position).getMimeType().split("/")[1]);
            imgPlay.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);

            Glide.with(mContext)
                    .load(photo.getPath())
                    //                .error(R.mipmap.ic_launcher)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            photoView.setImageDrawable(resource);
                        }
                    });

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(position, v);
                    }
                }
            });


            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {

                @Override
                public void onPhotoTap(View arg0, float arg1, float arg2) {
                    if (showToolbar) {
                        Log.d("ViewPagerAdapter", "show tool bar");
                    } else {
                        Log.d("ViewPagerAdapter", "hide tool bar");
                    }
                    showToolbar = !showToolbar;
                }

            });

        }

        container.addView(imageLayout);


        return imageLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }


    private static boolean isVideoFile(String sourcePath) {
        String type = sourcePath.substring(sourcePath.length() - 4, sourcePath.length());
        return type.equals(".mp4") ? true : false;

    }

    private static final String videoThumbUri = "/storage/emulated/0/DCIM/.thumbnails/";

    private String createVideoThumbnail(String thumbFromCP, String videoPath) {

        String thumbPath = videoThumbUri + videoPath.substring(39, videoPath.length() - 4) + ".jpg";

        return thumbFromCP == null ? thumbPath : thumbFromCP;
    }

}
