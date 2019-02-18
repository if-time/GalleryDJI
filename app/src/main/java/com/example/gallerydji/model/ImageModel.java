package com.example.gallerydji.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.gallerydji.bean.Photo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ImageModel {

    static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");//把long转成String

    /**
     * 从SDCard加载图片
     *
     * @param context
     * @param callback
     */
    public static void loadImageForSDCard(final Context context, final DataCallback callback) {
        //由于扫描图片是耗时的操作，所以要在子线程处理。
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<Photo> list = new ArrayList<Photo>();
                List<String> list1 = new ArrayList<String>();//记得初始化
                //扫描图片
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = context.getContentResolver();

                Cursor ImageCursor = mContentResolver.query(mImageUri, new String[]{
                                MediaStore.Images.Media.DATA,
                                MediaStore.Images.Media.DISPLAY_NAME,
                                MediaStore.Images.Media.DATE_TAKEN,
                                MediaStore.Images.Media._ID,
                                MediaStore.Images.Media.MIME_TYPE,
                                MediaStore.Images.Media.BUCKET_DISPLAY_NAME},
                        null,
                        null,
                        MediaStore.Images.Media.DATE_ADDED);

                //读取扫描到的图片
                if (ImageCursor != null) {
                    while (ImageCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = ImageCursor.getString(
                                ImageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        //获取图片名称
                        String name = ImageCursor.getString(
                                ImageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        //获取图片时间
                        long time = ImageCursor.getLong(
                                ImageCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));

                        //获取图片类型
                        String mimeType = ImageCursor.getString(
                                ImageCursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));

                        long id = ImageCursor.getLong(
                                ImageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

                        String dec = ImageCursor.getString(
                                ImageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                        Date date = new Date(time);
                        String Sdate = format.format(date);//将long变string

                            Photo photo = new Photo(path, Sdate, id, dec, name, mimeType);//new一个新photo

                        if (!list1.contains(Sdate)) {//判断日期的list是否已经有了这个照片的日期
                            list1.add(Sdate);//这个是将日期添加到日期的list
                        }
                        list.add(photo);//这个是照片的list
                    }
                    ImageCursor.close();
                }

                //扫描视频
                Cursor VideoCursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Video.Media.DATE_ADDED);

                if (VideoCursor != null) {
                    while (VideoCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = VideoCursor.getString(
                                VideoCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        //获取图片名称
                        String name = VideoCursor.getString(
                                VideoCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        //获取图片时间
                        long time = VideoCursor.getLong(
                                VideoCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));

                        //获取图片类型
                        String mimeType = VideoCursor.getString(
                                VideoCursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));

                        long id = VideoCursor.getLong(
                                VideoCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

                        String dec = VideoCursor.getString(
                                VideoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                        Date date = new Date(time);
                        String Sdate = format.format(date);//将long变string

                        Photo photo = new Photo(path, Sdate, id, dec, name, mimeType);//new一个新photo

                        if (!list1.contains(Sdate)) {//判断日期的list是否已经有了这个照片的日期
                            list1.add(Sdate);//这个是将日期添加到日期的list
                        }
                        list.add(photo);//这个是照片的list
                    }
                    VideoCursor.close();
                }

//                Collections.sort(list, new Comparator<Photo>() {
//                    @Override
//                    public int compare(Photo o1, Photo o2) {
//
//                        long t1 = 0;
//                        long t2 = 0;
//
//                        try {
//                            t1 = format.parse(o1.getDate()).getTime();
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            t2 = format.parse(o2.getDate()).getTime();
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (t1 < t2) {
//                            return 1;
//                        } else if (t1 > t2) {
//                            return -1;
//                        }
//                        return 0;
//                    }
//                });
                Collections.sort(list);
                Collections.sort(list1);

                ArrayList Alist = new ArrayList();//待会以大的list传送数据
                Alist.add(list);//大的list 加入之前的照片list
                Alist.add(list1);//大的list加入日期的list

                callback.onSuccess(Alist);
            }
        }).start();
    }


    public interface DataCallback {
        void onSuccess(ArrayList arrayList);
    }
}
