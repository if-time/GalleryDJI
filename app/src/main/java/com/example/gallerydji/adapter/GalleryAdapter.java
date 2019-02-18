package com.example.gallerydji.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import com.example.gallerydji.R;
import com.example.gallerydji.activity.MainActivity;
import com.example.gallerydji.bean.Photo;
import com.example.gallerydji.view.CustomSquareImgView;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.bumptech.glide.request.RequestOptions.centerCropTransform;

/**
 *
 */
public class GalleryAdapter extends SectionedRecyclerViewAdapter<GalleryAdapter.MyHeadView, GalleryAdapter.MyViewHolder, GalleryAdapter.MyFootView> {//remember extends

    public List<Photo> photos;
    Context context;
    public        List<String> photoDates;
    public static int          sectionCount;
    String[][] path;//将list转变为这个数组//前面的[]是某天，后面[]是某天里面有几张照片

    OnItemClickListener     onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;

    private List<Photo> checkedPhotos = new ArrayList<>();  // 选中的数据
    private List<Integer> checkPositionlist = new ArrayList<>();

    public GalleryAdapter(Context context) {
        this.context = context;
    }

    public void setMediaData(List<Photo> photos, List<String> photoDates) {
        this.photos = photos;
        this.photoDates = photoDates;
        notifyDataSetChanged();

        path = new String[photoDates.size()][photos.size()];

        sectionCount = photoDates.size();
        // System.out.println(sectionCount);

        getPath();//其实是将list转变为2D数组
        System.out.println(photos.size() + "@@@" + photoDates.size());
    }

    @Override
    protected MyFootView onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }//必须继承

    @Override
    protected MyHeadView onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {//必须继承，添加时间item的
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_headitem, parent, false);

        return new MyHeadView(view);
    }

    @Override
    protected MyViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {//必须继承，添加主体photo
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_imageitem, parent, false);
        //  System.out.println("photoSize: "+photos.size()+"\n"+"photoDates: "+photoDates.size());
        return new MyViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(MyViewHolder holder, final int section, int position) {//绑定数据，也就是绑定照片
        final MyViewHolder viewHolder = (MyViewHolder) holder;

        if (path[section][position] != null) {

            if (photos.get(viewHolder.getAdapterPosition() - section - 1).getMimeType().split("/")[1] == "MOV" ||
                    photos.get(viewHolder.getAdapterPosition() - section - 1).getMimeType().split("/")[1] == "mp4") {
                viewHolder.ivplay.setVisibility(View.VISIBLE);
                System.out.println("mimeType: " + photos.get(viewHolder.getAdapterPosition() - section - 1).getMimeType());
            }

            RequestOptions myOptions = new RequestOptions()
                    .fitCenter()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(context)
                    .load(new File(path[section][position]))
                    .transition(withCrossFade())            // .crossFade()()
                    .apply(centerCropTransform())           // .centerCrop()

                    .apply(myOptions)

                    .thumbnail(0.5f)
                    .into(viewHolder.thumb);
            //Glide就是加载图片的工具，android官方推荐的
            viewHolder.cardView.setOnClickListener(v -> {
                onItemClickListener.onItemClick(v, viewHolder.getAdapterPosition() - section - 1);//-1是因为从0开始的，第一个位置就要变成第0个；
            });

            viewHolder.cardView.setOnLongClickListener(v -> {
                viewHolder.mCheckBox.setChecked(true);
                viewHolder.mCheckBox.setTag(new Integer(position));//设置tag 否则划回来时选中消失
                return true;
            });

            viewHolder.itemView.setOnLongClickListener(v -> {
//                    onItemClickListener.onItemClick(v, viewHolder.getAdapterPosition() - section - 1);//-1是因为从0开始的，第一个位置就要变成第0个；
                viewHolder.mCheckBox.setChecked(true);
                viewHolder.mCheckBox.setTag(new Integer(position));//设置tag 否则划回来时选中消失
                return true;
            });

            viewHolder.mCheckBox.setTag(new Integer(position));//设置tag 否则划回来时选中消失
        }

        //checkbox  复用问题
        if (checkPositionlist != null) {
            ((MyViewHolder) holder).mCheckBox.setChecked((checkPositionlist.contains(new Integer(position)) ? true : false));
        } else {
            ((MyViewHolder) holder).mCheckBox.setChecked(false);
        }

        //cb 的选中事件
        onChecked(viewHolder, position);
    }

    private void onChecked(MyViewHolder viewHolder, int position) {

        viewHolder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Photo image = photos.get(position);

            if (isChecked) {
                Log.e("fcccc 1", viewHolder.mCheckBox.getTag() + "");
                if (!checkPositionlist.contains(viewHolder.mCheckBox.getTag())) {
                    checkedPhotos.add(image);
                    checkPositionlist.add(new Integer(position));
                }
            } else {
                Log.e("fcccc 2", viewHolder.mCheckBox.getTag() + "");
                if (checkPositionlist.contains(viewHolder.mCheckBox.getTag())) {
                    checkedPhotos.remove(image);
                    checkPositionlist.remove(new Integer(position));
                }
            }
        });

        Log.e("checked", "onChecked: " + checkedPhotos.size());
    }

    public void setOnItemClickListener(OnItemClickListener itemClick) {
        this.onItemClickListener = itemClick;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener itemLongClick) {
        this.onItemLongClickListener = itemLongClick;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }//有了这四个可以有点击的效果


    @Override
    protected void onBindSectionFooterViewHolder(MyFootView holder, int section) {

    }//必须继承，

    @Override
    protected void onBindSectionHeaderViewHolder(MyHeadView holder, int section) {
        MyHeadView viewHolder = (MyHeadView) holder;
        viewHolder.dateTV.setText(photoDates.get(section));

    }//必须继承，绑定头部时间

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }//必须继承，有脚吗？


    /**
     * 一共有多少个日期
     * @return
     */
    @Override
    public int getSectionCount() {

        if (photoDates != null) {
            return photoDates.size();
        }
        return 0;
    }//必须继承这个就是一共有多少个日期的。

    @Override
    public int getItemCount() {
        return photos.size() + photoDates.size();
    }//必须继承 一个有多少个item，有天数的和照片个数的

    @Override
    protected int getItemCountForSection(int section) {
        return getCountInsection(section);
    }//必须继承


    /**
     * 某一天里面有多少张照片
     * @param section
     * @return
     */
    public int getCountInsection(int section) {
        List<Photo> sectionPhotos = new ArrayList<>();
        for (int i = 0; i < MainActivity.photos.size(); i++) {

            if (MainActivity.photoDates.get(section).equals(MainActivity.photos.get(i).getDate())) {
                sectionPhotos.add(MainActivity.photos.get(i));
            }
        }
        //这个是获得某一天里面有多少张照片。
        return sectionPhotos.size();
    }

    public void getPath() {

        int count = 0;
        for (int i = 0; i < getSectionCount(); i++) {
            for (int j = 0; j < getCountInsection(i); j++) {

//                for (int d = 0; d < photoDates.size(); d++) {
//
//                }
//                if (photoDates.get())
                path[i][j] = photos.get(count).getPath();
                count++;
            }
        }
        //这个是将list转变为2D数据的方法

        System.out.println(photos.get(0).getPath());
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        CardView            cardView;
        CustomSquareImgView thumb;
        ImageView           ivplay;
        CheckBox            mCheckBox;

        //        ImageView thumb;

        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            //            img=(SquareImg)itemView.findViewById(R.id.img);
            thumb = itemView.findViewById(R.id.iv_thumb);

            ivplay = itemView.findViewById(R.id.iv_play);
            mCheckBox = itemView.findViewById(R.id.cb_check);
        }
    }

    class MyHeadView extends RecyclerView.ViewHolder {
        TextView dateTV;

        public MyHeadView(View itemView) {
            super(itemView);
            dateTV = (TextView) itemView.findViewById(R.id.headTV);
        }
    }

    class MyFootView extends RecyclerView.ViewHolder {
        public MyFootView(View itemView) {
            super(itemView);
        }
    }
}
