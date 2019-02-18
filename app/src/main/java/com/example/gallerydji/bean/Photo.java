package com.example.gallerydji.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Photo implements Parcelable, Comparable<Photo> {

    static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");//把long转成String

    private String path;
    private String date;
    private long id;
    private String discr;
    private String name;
    private String mimeType;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Photo(String path, String date, long id, String discr, String name, String mimeType) {
        this.path = path;
        this.date = date;
        this.id = id;
        this.discr = discr;
        this.name = name;
        this.mimeType = mimeType;
    }

    public Photo(String path, String date) {
        this.path = path;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDiscr() {
        return discr;
    }

    public void setDiscr(String discr) {
        this.discr = discr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(date);
        dest.writeLong(id);
        dest.writeString(discr);
        dest.writeString(name);
        dest.writeString(mimeType);
    }

    protected Photo(Parcel in) {
        path = in.readString();
        date = in.readString();
        id = in.readLong();
        discr = in.readString();
        name = in.readString();
        mimeType = in.readString();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int compareTo(Photo o) {
        long t1 = 0;
        long t2 = 0;

        try {
            t1 = format.parse(this.getDate()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            t2 = format.parse(o.getDate()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (t1 < t2) {
            return -1;
        } else if (t1 > t2) {
            return 1;
        }
        return 0;
    }
}
