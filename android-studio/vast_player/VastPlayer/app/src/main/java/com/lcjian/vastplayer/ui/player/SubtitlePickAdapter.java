package com.lcjian.vastplayer.ui.player;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.RxBus;
import com.lcjian.vastplayer.data.network.entity.Sub;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubtitlePickAdapter extends RecyclerView.Adapter<SubtitlePickAdapter.SubtitlePickViewHolder> {

    private RxBus mBus;

    private List<Sub> mSubs;

    private int mCheckedPosition;

    public SubtitlePickAdapter(List<Sub> subs, RxBus bus, int currentSubId) {
        this.mSubs = subs;
        this.mSubs.add(0, new Sub());
        this.mBus = bus;
        int i = 0;
        for (Sub sub : mSubs) {
            if (currentSubId == sub.id) {
                mCheckedPosition = i;
                break;
            }
            i++;
        }
    }

    @NonNull
    @Override
    public SubtitlePickViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final SubtitlePickViewHolder holder = new SubtitlePickViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.subtitle_pick_item, parent, false));
        View.OnClickListener onClickListener = v -> {
            if (mCheckedPosition != holder.getAdapterPosition()) {
                mCheckedPosition = holder.getAdapterPosition();
                if (mBus.hasObservers()) {
                    mBus.send(mSubs.get(mCheckedPosition));
                }
                notifyDataSetChanged();
            }
        };
        holder.itemView.setOnClickListener(onClickListener);
        holder.rb_pick.setOnClickListener(onClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SubtitlePickViewHolder holder, int position) {
        if (position == 0) {
            holder.tv_subtitle_display_name.setText(holder.itemView.getResources().getText(R.string.no_subtitle));
        } else {
            holder.tv_subtitle_display_name.setText(mSubs.get(position).native_name);
        }
        if (position == mCheckedPosition) {
            holder.rb_pick.setChecked(true);
        } else {
            holder.rb_pick.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return mSubs == null ? 0 : mSubs.size();
    }

    static class SubtitlePickViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rb_pick)
        RadioButton rb_pick;
        @BindView(R.id.tv_subtitle_display_name)
        TextView tv_subtitle_display_name;

        public SubtitlePickViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
