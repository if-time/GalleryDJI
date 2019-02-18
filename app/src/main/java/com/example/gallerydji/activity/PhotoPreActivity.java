package com.example.gallerydji.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gallerydji.R;
import com.example.gallerydji.adapter.GalleryAdapter;
import com.example.gallerydji.adapter.ImagePagerAdapter;
import com.example.gallerydji.bean.Photo;
import com.example.gallerydji.entity.ResourcesManager;
import com.example.gallerydji.view.ShowImagesViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

import static android.animation.ObjectAnimator.ofFloat;
import static com.example.gallerydji.adapter.ImagePagerAdapter.exoPlayerManager;
import static com.example.gallerydji.adapter.ImagePagerAdapter.videoPlayerView;


public class PhotoPreActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback {

    private Handler handler;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static final String TAG = PhotoPreActivity.class.getName();

    ViewPager bigImgVp;

    private TextView       tvIndicator;
    private RelativeLayout rlTopBar;            //状态栏
    private ImageView      ivDownload;
    private ImageView      ivDelete;
    private ImageView      ivShare;

    private       int position;
    public static int deletePosition;

    private boolean isShowBar = true;

    private ImagePagerAdapter mImagePagerAdapter;

    private int getPosition;

    private List<MediaFile> mediaFileList = new ArrayList<MediaFile>();
    private MediaManager    mMediaManager;

    int mTouchSlop;

    File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Gallery/");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pre);

        handler = new Handler(Looper.getMainLooper(),this);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        //        photos = intent.getParcelableArrayListExtra("photos");

        //        mediaFileList = MainActivity.mediaFileList;

        initView();
        initListener();
        initViewPager();

        ViewConfiguration configuration = ViewConfiguration.get(this);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        System.out.print(mTouchSlop);


        tvIndicator.setText(1 + "/" + MainActivity.photos.size());

        bigImgVp.setCurrentItem(position);
    }

    private void initView() {

        tvIndicator = (TextView) findViewById(R.id.tv_indicator);
        rlTopBar = (RelativeLayout) findViewById(R.id.rl_top_bar);

        ivDownload = (ImageView) findViewById(R.id.iv_down);
        ivDelete = (ImageView) findViewById(R.id.iv_delete);
        ivShare = (ImageView) findViewById(R.id.iv_share);

        bigImgVp = (ShowImagesViewPager) findViewById(R.id.big_img_vp);
        bigImgVp.setOnClickListener(this);

        ivDownload.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        ivShare.setOnClickListener(this);

    }

    private void initListener() {
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        mImagePagerAdapter = new ImagePagerAdapter(this, MainActivity.photos);
        bigImgVp.setAdapter(mImagePagerAdapter);

        bigImgVp.setOnTouchListener(new View.OnTouchListener() {
            int touchFlag = 0;
            float x = 0, y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchFlag = 0;
                        x = event.getX();
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float xDiff = Math.abs(event.getX() - x);
                        float yDiff = Math.abs(event.getY() - y);
                        if (xDiff == 0 && yDiff == 0) {
                            touchFlag = 0;
                        } else {
                            touchFlag = -1;
                        }
                        /*if (xDiff < mTouchSlop && xDiff >= yDiff)
                            touchFlag = 0;
                        else
                            touchFlag = -1;*/
                        break;
                    case MotionEvent.ACTION_UP:
                        if (touchFlag == 0) {
                            Toast.makeText(PhotoPreActivity.this, "click_setOnTouchListener", Toast.LENGTH_LONG).show();
                            if (isShowBar) {
                                hideBar();
                            } else {
                                showBar();
                            }
                        } else if (touchFlag == -1) {
                            if (isShowBar) {
                                hideBar();
                            }
                        }
                        break;
                }
                return false;
            }
        });

        mImagePagerAdapter.setOnItemClickListener(new ImagePagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Toast.makeText(PhotoPreActivity.this, "click_setOnItemClickListener", Toast.LENGTH_LONG).show();
                if (isShowBar) {
                    hideBar();
                } else {
                    showBar();
                }
            }
        });

        bigImgVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tvIndicator.setText(position + 1 + "/" + MainActivity.photos.size());
                deletePosition = position;
                exoPlayerManager.onPause();

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    /**
     * 显示和隐藏状态栏
     *
     * @param show
     */
    private void setStatusBarVisible(boolean show) {
        if (show) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    /**
     * 显示头部
     */
    private void showBar() {
        isShowBar = true;
        setStatusBarVisible(true);
        //添加延时，保证StatusBar完全显示后再进行动画。
        rlTopBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (rlTopBar != null) {
                    ObjectAnimator animator = ofFloat(rlTopBar, "translationY",
                            rlTopBar.getTranslationY(), 0).setDuration(300);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            if (rlTopBar != null) {
                                rlTopBar.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    animator.start();
                }
            }
        }, 100);
    }

    /**
     * 隐藏头部
     */
    private void hideBar() {
        isShowBar = false;
        ObjectAnimator animator = ObjectAnimator.ofFloat(rlTopBar, "translationY",
                0, -rlTopBar.getHeight()).setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (rlTopBar != null) {
                    rlTopBar.setVisibility(View.GONE);
                    //添加延时，保证rlTopBar完全隐藏后再隐藏StatusBar。
                    rlTopBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setStatusBarVisible(false);
                        }
                    }, 5);
                }
            }
        });
        animator.start();

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_delete) {
            Toast.makeText(PhotoPreActivity.this, "Delete" + deletePosition, Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(PhotoPreActivity.this);
            builder.setTitle("删除");
            builder.setMessage("确定删除吗？");
            builder.setPositiveButton("确定",
                    (dialog, which) -> {

                        MainActivity.photos.remove(deletePosition);
                        tvIndicator.setText(position + 1 + "/" + MainActivity.photos.size());
                        mImagePagerAdapter.notifyDataSetChanged();
                        MainActivity.mGalleryAdapter.setMediaData(MainActivity.photos, MainActivity.photoDates);

                        Toast.makeText(PhotoPreActivity.this, "SIZE : " + MainActivity.photos.size(), Toast.LENGTH_SHORT).show();

                    });
            builder.setNegativeButton("取消",
                    (dialog, which) -> {

                    });
            builder.show();
        } else if (v.getId() == R.id.iv_down) {
            //            downloadFileByIndex();
            onOneKeyShare();
        } else {
            if (MainActivity.photos.get(deletePosition).getMimeType().split("/")[1] == "MOV" ||
                    MainActivity.photos.get(deletePosition).getMimeType().split("/")[1] == "mp4") {
               //分享视频
                shareVideo();
            }else {
                shareImage();
            }

        }
    }

    private void shareVideo() {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);            //分享视频只能单个分享
        File f = new File(MainActivity.photos.get(deletePosition).getPath());
        Uri uri = Uri.fromFile(f);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("audio/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));

    }

    /**
     * 保存图片到本地
     */
    private void saveImage() {

    }

    /**
     * 删除图片
     */
    private void deleteImage() {

    }

    /**
     * Android原生分享功能
     */
    private void shareImage() {
        Intent share_intent = new Intent();
        //        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        share_intent.setType("image/*");//设置分享内容的类型.

        File f = new File(MainActivity.photos.get(deletePosition).getPath());
        Uri uri = Uri.fromFile(f);
        share_intent.putExtra(Intent.EXTRA_STREAM, uri);
        share_intent.putExtra(Intent.EXTRA_SUBJECT, "分享");//添加分享内容标题
        //        share_intent.putExtra(Intent.EXTRA_TEXT, "dsy");//添加分享内容
        //创建分享的Dialog
        share_intent = Intent.createChooser(share_intent, "分享");
        try {
            startActivity(share_intent);
        } catch (android.content.ActivityNotFoundException ex) {

            Toast.makeText(this, "找不到该分享应用组件", Toast.LENGTH_SHORT).show();
        }
    }


    /*private void downloadFileByIndex(final int index){
        if ((mediaFileList.get(index).getMediaType() == MediaFile.MediaType.PANORAMA)
                || (mediaFileList.get(index).getMediaType() == MediaFile.MediaType.SHALLOW_FOCUS)) {
            return;
        }

        mediaFileList.get(index).fetchFileData(destDir, null, new DownloadListener<String>() {
            @Override
            public void onFailure(DJIError error) {
                HideDownloadProgressDialog();
                setResultToToast("Download File Failed" + error.getDescription());
                currentProgress = -1;
            }

            @Override
            public void onProgress(long total, long current) {
            }

            @Override
            public void onRateUpdate(long total, long current, long persize) {
                int tmpProgress = (int) (1.0 * current / total * 100);
                if (tmpProgress != currentProgress) {
                    mDownloadDialog.setProgress(tmpProgress);
                    currentProgress = tmpProgress;
                }
            }

            @Override
            public void onStart() {
                currentProgress = -1;
                ShowDownloadProgressDialog();
            }

            @Override
            public void onSuccess(String filePath) {
                HideDownloadProgressDialog();
                setResultToToast("Download File Success" + ":" + filePath);
                currentProgress = -1;
            }
        });
    }
*/

    /**
     * 删除图片
     */
    private void deleteFileByIndex(final int index) {
        ArrayList<MediaFile> fileToDelete = new ArrayList<MediaFile>();
        if (mediaFileList.size() > index) {
            fileToDelete.add(mediaFileList.get(index));
            mMediaManager.deleteFiles(fileToDelete, new CommonCallbacks.CompletionCallbackWithTwoParam<List<MediaFile>, DJICameraError>() {
                @Override
                public void onSuccess(List<MediaFile> x, DJICameraError y) {
                    DJILog.e(TAG, "Delete file success");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            MediaFile file = mediaFileList.remove(index);

                            if (MainActivity.photos.size() > index) {
                                MainActivity.photos.remove(index);
                            }

                            //Reset select view


                            //Update ViewPager
                            mImagePagerAdapter.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onFailure(DJIError error) {
                    setResultToToast("Delete file failed");
                }
            });
        }
    }

    private void setResultToToast(final String result) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(PhotoPreActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 一键分享
     */
    private void onOneKeyShare(){
        cn.sharesdk.onekeyshare.OnekeyShare oks = new cn.sharesdk.onekeyshare.OnekeyShare();
        oks.setAddress("12345678901");
        ResourcesManager manager = ResourcesManager.getInstace(this);
        if(!TextUtils.isEmpty(manager.getFilePath())){
            oks.setFilePath(manager.getFilePath());
        } else{
            oks.setFilePath(manager.getFilePath());
        }
        oks.setTitle(manager.getTitle());
        oks.setTitleUrl(manager.getTitleUrl());
        oks.setUrl(manager.getUrl());
        oks.setMusicUrl(manager.getMusicUrl());
        String customText = manager.getText();
        if (customText != null) {
            oks.setText(customText);
        } else if (manager.getText() != null && manager.getText().contains("0")) {
            oks.setText(manager.getText());
        } else {
            oks.setText(this.getString(R.string.share_content));
        }
        oks.setComment(manager.getComment());
        oks.setSite(manager.getSite());
        oks.setSiteUrl(manager.getSiteUrl());
        oks.setVenueName(manager.getVenueName());
        oks.setVenueDescription(manager.getVenueDescription());
        oks.setSilent(true);
        oks.setLatitude(23.169f);
        oks.setLongitude(112.908f);
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                String msg = ResourcesManager.actionToString(i);
                String text = platform.getName() + " completed at " + msg;
                Message message = new Message();
                message.obj = text;
                handler.sendMessage(message);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                String msg = ResourcesManager.actionToString(i);
                String text = platform.getName() + "caught error at " + msg;
                Message message = new Message();
                message.obj = text;
                handler.sendMessage(message);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                String msg = ResourcesManager.actionToString(i);
                String text = platform.getName() + " canceled at " + msg;
                Message message = new Message();
                message.obj = text;
                handler.sendMessage(message);
            }
        });
        Bitmap logo = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
        String label = this.getString(R.string.app_name);
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                String text = "Customer Logo -- ShareSDK ";
            }
        };
        oks.setCustomerLogo(logo, label, listener);
        oks.show(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        String toastMsg = (String) msg.obj;
        showToast(toastMsg);
        return false;
    }

    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
