package com.sjy.android.criminalintent;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by pc on 2017-07-22.
 */

public class Crime {
    private UUID mId;
    private String mTitle;
    private Instant mDate;
    private boolean mSolved;
    private String mSuspect;
    private String mPhoneNumber;


    public Crime() {
        // Generate unique id
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        mId = id;
        mDate = Instant.now();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Instant getDate() {
        return mDate;
    }

    public void setDate(Instant date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) { mSuspect = suspect; }

    public String getPhoneNumber() { return mPhoneNumber; }

    public void setPhoneNumber(String phoneNumber) { mPhoneNumber = phoneNumber; }

    public String getPhotoFileName() {
        return "IMG_" + getId().toString() + ".jpg";
    }

}
