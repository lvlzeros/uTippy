package com.levelzeros.utippy.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Poon on 2/3/2017.
 */

public class ClassObject implements Parcelable {

    private final long mId;
    private final String mClassName;
    private final String mClassDescription;
    private final String mClassDay;
    private final int mClassDayNum;
    private final long mClassStartTime;
    private final int mClassStartHour;
    private final int mClassStartMinute;
    private final long mClassEndTime;
    private final int mClassEndHour;
    private final int mClassEndMinute;
    private final String mClassVenue;

    public ClassObject(long mId, String mClassName, String mClassDescription, String mClassDay, int mClassDayNum, long mClassStartTime, int mClassStartHour, int mClassStartMinute, long mClassEndTime, int mClassEndHour, int mClassEndMinute, String mClassVenue) {
        this.mId = mId;
        this.mClassName = mClassName;
        this.mClassDescription = mClassDescription;
        this.mClassDay = mClassDay;
        this.mClassDayNum = mClassDayNum;
        this.mClassStartTime = mClassStartTime;
        this.mClassStartHour = mClassStartHour;
        this.mClassStartMinute = mClassStartMinute;
        this.mClassEndTime = mClassEndTime;
        this.mClassEndHour = mClassEndHour;
        this.mClassEndMinute = mClassEndMinute;
        this.mClassVenue = mClassVenue;
    }

    public long getId() {
        return mId;
    }

    public String getClassName() {
        return mClassName;
    }

    public String getClassDescription() {
        return mClassDescription;
    }

    public String getClassDay() {
        return mClassDay;
    }

    public int getClassDayNum() {
        return mClassDayNum;
    }

    public long getClassStartTime() {
        return mClassStartTime;
    }

    public int getClassStartHour() {
        return mClassStartHour;
    }

    public int getClassStartMinute() {
        return mClassStartMinute;
    }

    public long getClassEndTime() {
        return mClassEndTime;
    }

    public int getClassEndHour() {
        return mClassEndHour;
    }

    public int getClassEndMinute() {
        return mClassEndMinute;
    }

    public String getClassVenue() {
        return mClassVenue;
    }

    @Override
    public String toString() {
        return "ClassObject{" +
                "mId=" + mId +
                ", mClassName='" + mClassName + '\'' +
                ", mClassDescription='" + mClassDescription + '\'' +
                ", mClassDay='" + mClassDay + '\'' +
                ", mClassDayNum=" + mClassDayNum +
                ", mClassStartTime=" + mClassStartTime +
                ", mClassStartHour=" + mClassStartHour +
                ", mClassStartMinute=" + mClassStartMinute +
                ", mClassEndTime=" + mClassEndTime +
                ", mClassEndHour=" + mClassEndHour +
                ", mClassEndMinute=" + mClassEndMinute +
                ", mClassVenue='" + mClassVenue + '\'' +
                '}';
    }

    protected ClassObject(Parcel in) {
        mId = in.readLong();
        mClassName = in.readString();
        mClassDescription = in.readString();
        mClassDay = in.readString();
        mClassDayNum = in.readInt();
        mClassStartTime = in.readLong();
        mClassStartHour = in.readInt();
        mClassStartMinute = in.readInt();
        mClassEndTime = in.readLong();
        mClassEndHour = in.readInt();
        mClassEndMinute = in.readInt();
        mClassVenue = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mClassName);
        dest.writeString(mClassDescription);
        dest.writeString(mClassDay);
        dest.writeInt(mClassDayNum);
        dest.writeLong(mClassStartTime);
        dest.writeInt(mClassStartHour);
        dest.writeInt(mClassStartMinute);
        dest.writeLong(mClassEndTime);
        dest.writeInt(mClassEndHour);
        dest.writeInt(mClassEndMinute);
        dest.writeString(mClassVenue);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ClassObject> CREATOR = new Parcelable.Creator<ClassObject>() {
        @Override
        public ClassObject createFromParcel(Parcel in) {
            return new ClassObject(in);
        }

        @Override
        public ClassObject[] newArray(int size) {
            return new ClassObject[size];
        }
    };
}