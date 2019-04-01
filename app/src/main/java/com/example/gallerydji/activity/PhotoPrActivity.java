package com.example.gallerydji.activity;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Re
lativeLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gallerydji.R;
import com.example.gallerydji.adapter.ImagePagerAdapter;
import com.example.gallerydji.adapter.ViewPagerAdapter;
import com.example.gallerydji.view.ShowImagesViewPager;

public class PhotoPrActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = PhotoPreActivity.class.getName();

    ViewPager bigImgVp;

    private       int position;
    public static int deletePosition;

    ViewPagerAdapter mImagePagerAdapter;

    private TextView       tvIndicator;
    private RelativeLayout rlTopBar;            //状态栏
    private ImageView      ivDownload;
    private ImageView      ivDelete;
    private ImageView      ivShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pr);
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        initView();
        initViewPager();
        bigImgVp.setCurrentItem(position);
    }

    private void initView() {
        bigImgVp = (ShowImagesViewPager) findViewById(R.id.big_img_vp);
        bigImgVp.setOnClickListener(this);
        tvIndicator = (TextView) findViewById(R.id.tv_indicator);
        rlTopBar = (RelativeLayout) findViewById(R.id.rl_top_bar);

        ivDownload = (ImageView) findViewById(R.id.iv_down);
        ivDelete = (ImageView) findViewById(R.id.iv_delete);
        ivShare = (ImageView) findViewById(R.id.iv_share);

        bigImgVp = (ShowImagesViewPager) findViewById(R.id.big_img_vp);

        ivDownload.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        ivShare.setOnClickListener(this);
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        mImagePagerAdapter = new ViewPagerAdapter(this, MainActivity.photos);

        Log.i(TAG, "initViewPager: " + MainActivity.photos.get(2).getMimeType().split("/")[1]);

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
                            Toast.makeText(PhotoPrActivity.this, "click_setOnTouchListener", Toast.LENGTH_LONG).show();

                        } else if (touchFlag == -1) {

                        }
                        break;
                }
                return false;
            }
        });

        mImagePagerAdapter.setOnItemClickListener(new ViewPagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {

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

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}
