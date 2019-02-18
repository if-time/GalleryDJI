package com.example.gallerydji.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;


import com.example.gallerydji.R;
import com.example.gallerydji.adapter.GalleryAdapter;
import com.example.gallerydji.bean.Photo;
import com.example.gallerydji.entity.ResourcesManager;
import com.example.gallerydji.model.ImageModel;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.truizlop.sectionedrecyclerview.SectionedSpanSizeLookup;

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
import eason.linyuzai.download.ELoad;
import eason.linyuzai.download.task.DownloadTask;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    private static final String TAG = MainActivity.class.getName();
    private static final int PERMISSION_WRITE_EXTERNAL_REQUEST_CODE = 0x00000011;

    private Handler handler;

    private boolean applyLoadImage = false;

    private Toolbar        tl_activity;
    private RecyclerView   rl_activity;
    public static GalleryAdapter mGalleryAdapter;

    public static List<Photo> photos;//这两个是可以在其他java都能访问到的。
    public static List<String>     photoDates;

    private static List<Photo> checkedPhotos = new ArrayList<>();  // 选中的数据

    private List<MediaFile> mediaFileList = new ArrayList<MediaFile>();
    private MediaManager    mMediaManager;

    @Override
    protected void onStart() {
        super.onStart();
        if (applyLoadImage) {
            applyLoadImage = false;
            checkPermissionAndLoadImages();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        handler = new Handler(Looper.getMainLooper(),this);

        initView();
        initData();

    }

    private void initData() {

        mGalleryAdapter = new GalleryAdapter(this);
        checkPermissionAndLoadImages();

        mGalleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //                            Toast.makeText(getContext(), position + ": " + photos.get(position).getPath(), Toast.LENGTH_LONG).show();

                Intent imgIntent = new Intent(MainActivity.this, PhotoPrActivity.class);
                imgIntent.putExtra("position", position);

                startActivity(imgIntent);
            }
        });
    }

    private void initView() {
        rl_activity = findViewById(R.id.rl_activity_main);
        tl_activity = findViewById(R.id.tb_toolbar);
        tl_activity.setTitle("");
        setSupportActionBar(tl_activity);

        //左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        tl_activity.setNavigationIcon(R.drawable.ic_ab_back);

        tl_activity.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * 检查权限并加载SD卡里的图片。
     */
    private void checkPermissionAndLoadImages() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            loadImageForSDCard();
            //            initDJIMedia();
        }
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "没有图片", Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，加载图片。
                loadImageForSDCard();
            } else {
                //拒绝权限，弹出提示框。
                showExceptionDialog(true);
            }
        }
    }

    /**
     * 发生没有权限等异常时，显示一个提示dialog.
     */
    private void showExceptionDialog(final boolean applyLoad) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage("该相册需要赋予访问存储和拍照的权限，请到“设置”>“应用”>“权限”中配置权限。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                if (applyLoad) {
                    applyLoadImage = true;
                }
            }
        }).show();
    }

    private void loadImageForSDCard() {
        ImageModel.loadImageForSDCard(this, new ImageModel.DataCallback() {
            @Override
            public void onSuccess(ArrayList arrayList) {

                photos = (ArrayList<Photo>) arrayList.get(0);
                photoDates = (ArrayList<String>) arrayList.get(1);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mGalleryAdapter.setMediaData(photos, photoDates);

                        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 5);//后面数字就是一行几个图片的意思

                        SectionedSpanSizeLookup lookup = new SectionedSpanSizeLookup(mGalleryAdapter, manager);//这是啥我不知道，关于头部分组的
                        manager.setSpanSizeLookup(lookup);//设置之后，一个位置占满一行

                        rl_activity.setLayoutManager(manager);//设RV的布局
                        rl_activity.setAdapter(mGalleryAdapter);
                    }
                });
            }
        });
    }

    /**
     * 选择
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tab_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                Toast.makeText(MainActivity.this,"download : " + MainActivity.photos.size() , Toast.LENGTH_SHORT).show();
//                downloadFileByIndex();
                break;
            case R.id.action_share:
               shareImage(photos.get(4).getPath());

             break;
            case R.id.action_delete:
//                deleteFileByIndex();
                onOneKeyShare();

                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Android原生分享功能
     * @param path
     */
    private void shareImage(String path) {
        Intent share_intent = new Intent();
//        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        share_intent.setType("image/*");//设置分享内容的类型.

        //添加图片
        checkedPhotos.add(photos.get(10));
        checkedPhotos.add(photos.get(30));
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        ArrayList<File> files = new ArrayList<>();

        if (checkedPhotos != null || checkedPhotos.size() > 0) {
            for (int i = 0; i < checkedPhotos.size(); i++) {
                File f = new File(checkedPhotos.get(i).getPath());
                files.add(f);
            }
        }

        for(File f : files){

            imageUris.add(Uri.fromFile(f));
        }

        share_intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);

//        File f = new File(photos.get(2).getPath());
//        Uri uri = Uri.fromFile(f);
//        share_intent.putExtra(Intent.EXTRA_STREAM, uri);
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
                            mGalleryAdapter.notifyDataSetChanged();
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

    private void setResultToToast(final String result) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
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
