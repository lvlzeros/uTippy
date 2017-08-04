package com.levelzeros.utippy.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Poon on 26/2/2017.
 */

public class TodoObject implements Parcelable {

    private final long m_Id;
    private final String mTodoName;
    private final String mTodoDescription;
    private final int mPriority;
    private final int mDisplayNum;

    public TodoObject(long m_Id, String mTodoName, String mTodoDescription, int mPriority, int mDisplayNum) {
        this.m_Id = m_Id;
        this.mTodoName = mTodoName;
        this.mTodoDescription = mTodoDescription;
        this.mPriority = mPriority;
        this.mDisplayNum = mDisplayNum;
    }

    public long getId() {
        return m_Id;
    }

    public String getTodoName() {
        return mTodoName;
    }

    public String getTodoDescription() {
        return mTodoDescription;
    }

    public int getPriority() {
        return mPriority;
    }

    public int getDisplayNum() {
        return mDisplayNum;
    }

    @Override
    public String toString() {
        return "TodoObject{" +
                "m_Id=" + m_Id +
                ",\n mTodoName=" + mTodoName +
                ",\n mTodoDescription=" + mTodoDescription +
                ",\n mPriority=" + mPriority +
                ",\n mDisplayNum=" + mDisplayNum +
                '}';
    }

    protected TodoObject(Parcel in) {
        m_Id = in.readLong();
        mTodoName = in.readString();
        mTodoDescription = in.readString();
        mPriority = in.readInt();
        mDisplayNum = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(m_Id);
        dest.writeString(mTodoName);
        dest.writeString(mTodoDescription);
        dest.writeInt(mPriority);
        dest.writeInt(mDisplayNum);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TodoObject> CREATOR = new Parcelable.Creator<TodoObject>() {
        @Override
        public TodoObject createFromParcel(Parcel in) {
            return new TodoObject(in);
        }

        @Override
        public TodoObject[] newArray(int size) {
            return new TodoObject[size];
        }
    };
}