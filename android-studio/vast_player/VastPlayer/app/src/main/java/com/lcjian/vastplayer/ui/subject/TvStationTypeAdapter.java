package com.lcjian.vastplayer.ui.subject;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.vastplayer.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class TvStationTypeAdapter extends RecyclerView.Adapter<TvStationTypeAdapter.TVStationTypeViewHolder> {

    private List<String> mData;

    private String mCheckedTypeName;

    TvStationTypeAdapter(List<String> data) {
        this.mData = data;
    }

    void setCheckedTypeName(String checkedTypeName) {
        this.mCheckedTypeName = checkedTypeName;
    }

    public void replaceAll(final List<String> data) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {

            @Override
            public int getOldListSize() {
                return mData == null ? 0 : mData.size();
            }

            @Override
            public int getNewListSize() {
                return data == null ? 0 : data.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return TextUtils.equals(mData.get(oldItemPosition), data.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }
        }, true);
        mData = data;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public TVStationTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TVStationTypeViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull TVStationTypeViewHolder holder, int position) {
        String typeName = mData.get(position);
        holder.bindTo(typeName, TextUtils.equals(typeName, mCheckedTypeName));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    static class TVStationTypeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_tv_station_type_name)
        TextView tv_tv_station_type_name;

        String typeName;

        TVStationTypeViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_station_type_item, parent, false));
            ButterKnife.bind(this, this.itemView);
        }

        void bindTo(String typeName, boolean checked) {
            this.typeName = typeName;
            tv_tv_station_type_name.setText(typeName);
            tv_tv_station_type_name.setTextColor(checked ? ContextCompat.getColor(itemView.getContext(), R.color.accent) : 0xff222222);
            itemView.setBackgroundColor(checked ? 0xffe5e5e5 : 0xffffffff);
        }
    }
}
