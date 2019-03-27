package com.lcjian.mmt.ui.common;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aitangba.pickdatetime.adapter.BaseWheelAdapter;
import com.aitangba.pickdatetime.view.WheelView;
import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseBottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PickerFragment extends BaseBottomSheetDialogFragment {

    @BindView(R.id.tv_picker_cancel)
    TextView tv_picker_cancel;
    @BindView(R.id.tv_picker_title)
    TextView tv_picker_title;
    @BindView(R.id.tv_picker_confirm)
    TextView tv_picker_confirm;
    @BindView(R.id.tv_picker_action_extra)
    TextView tv_picker_action_extra;
    @BindView(R.id.wv_picker)
    WheelView wv_picker;
    Unbinder unbinder;

    private String mTitle;
    private ArrayList<String> mData;
    private OnPickListener mOnPickListener;
    private View.OnClickListener mExtraOnClickListener;

    public static PickerFragment newInstance(String title, ArrayList<String> data) {
        PickerFragment fragment = new PickerFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putStringArrayList("data", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString("title");
            mData = getArguments().getStringArrayList("data");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PickerAdapter adapter = new PickerAdapter();
        adapter.setData(mData);
        wv_picker.setCyclic(false);
        wv_picker.setAdapter(adapter);
        wv_picker.setCurrentItem(0);

        tv_picker_title.setText(mTitle);
        if (mExtraOnClickListener != null) {
            tv_picker_action_extra.setVisibility(View.VISIBLE);
            tv_picker_action_extra.setOnClickListener(mExtraOnClickListener);
        } else {
            tv_picker_action_extra.setVisibility(View.GONE);
        }
        tv_picker_cancel.setOnClickListener(v -> dismiss());
        tv_picker_confirm.setOnClickListener(v -> {
            if (mOnPickListener != null) {
                mOnPickListener.onPick(mData.get(wv_picker.getCurrentItem()), wv_picker.getCurrentItem());
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public PickerFragment setOnPickListener(OnPickListener onPickListener) {
        mOnPickListener = onPickListener;
        return this;
    }

    public PickerFragment setExtraOnClickListener(View.OnClickListener onClickListener) {
        mExtraOnClickListener = onClickListener;
        return this;
    }

    public interface OnPickListener {

        void onPick(String item, int position);
    }

    public static class PickerAdapter extends BaseWheelAdapter {

        private ArrayList<String> mData = new ArrayList<>();
        private LayoutInflater mLayoutInflater;
        private int selectTextColor = 0xff444444;
        private int textColor = 0xffdddddd;

        public void setData(@NonNull List<String> data) {
            mData.clear();
            mData.addAll(data);
            notifyDataChangedEvent();
        }

        @Override
        public int getItemsCount() {
            return mData.size();
        }

        private LayoutInflater getLayoutInflater(Context context) {
            if (mLayoutInflater == null) {
                mLayoutInflater = LayoutInflater.from(context);
            }
            return mLayoutInflater;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = getLayoutInflater(parent.getContext()).inflate(com.aitangba.pickdatetime.R.layout.cbk_wheel_default_inner_text, parent, false);
                holder = new ViewHolder();
                holder.text = convertView.findViewById(com.aitangba.pickdatetime.R.id.text);
                convertView.setTag(holder);
            }
            holder.text.setText(mData.get(position));
            return convertView;
        }

        @Override
        public void refreshStatus(View convertView, boolean isSelected) {
            TextView textview = convertView.findViewById(com.aitangba.pickdatetime.R.id.text);
            textview.setTextColor(isSelected ? selectTextColor : textColor);
        }

        private static class ViewHolder {
            public TextView text;
        }
    }
}
