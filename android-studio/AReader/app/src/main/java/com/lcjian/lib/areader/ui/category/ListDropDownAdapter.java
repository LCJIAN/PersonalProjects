package com.lcjian.lib.areader.ui.category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcjian.lib.areader.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 下拉通用Adapter
 *
 * @author LCJIAN
 */
public class ListDropDownAdapter extends BaseAdapter {

    private Context context;
    private List<String> list;
    private int checkItemPosition = 0;

    ListDropDownAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_default_drop_down, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        viewHolder.tv_title.setText(list.get(position));
        if (checkItemPosition != -1) {
            if (checkItemPosition == position) {
                viewHolder.tv_title.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                viewHolder.iv_checked.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_title.setTextColor(context.getResources().getColor(R.color.colorTextBlack));
                viewHolder.iv_checked.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    public void setCheckItem(int position) {
        checkItemPosition = position;
        notifyDataSetChanged();
    }

    public int getCheckedItemPosion() {
        return checkItemPosition;
    }

    static class ViewHolder {
        @BindView(R.id.tv_title)
        TextView tv_title;
        @BindView(R.id.iv_checked)
        ImageView iv_checked;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
