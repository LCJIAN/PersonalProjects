package com.aitangba.pickdatetime.adapter.datetime;

import androidx.annotation.NonNull;

import com.aitangba.pickdatetime.bean.DateParams;
import com.aitangba.pickdatetime.bean.DatePick;

/**
 * Created by fhf11991 on 2017/8/29.
 */

public class HourAdapter extends DatePickAdapter {

    public HourAdapter(@NonNull DateParams dateParams, @NonNull DatePick datePick) {
        super(dateParams, datePick);
    }

    @Override
    public int getCurrentIndex() {
        return mData.indexOf(mDatePick.hour);
    }

    @Override
    public void refreshValues() {
        setData(getArray(24));
    }
}
