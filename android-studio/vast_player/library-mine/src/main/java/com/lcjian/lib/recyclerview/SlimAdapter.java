package com.lcjian.lib.recyclerview;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlimAdapter extends RecyclerView.Adapter<SlimAdapter.SlimViewHolder> {

    private static final int WHAT_NOTIFY_DATA_SET_CHANGED = 1;

    private List<?> data;
    private List<Type> dataTypes = new ArrayList<>();
    private Map<Type, ViewHolderCreator> creators = new HashMap<>();
    private ViewHolderCreator defaultCreator = null;
    private DiffCallback diffCallback = null;

    public static SlimAdapter create() {
        return new SlimAdapter();
    }

    private Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == WHAT_NOTIFY_DATA_SET_CHANGED) {
                notifyDataSetChanged();
            }
            super.dispatchMessage(msg);
        }
    };

    public SlimAdapter updateData(List<?> data) {
        if (diffCallback == null || getItemCount() == 0 || data == null || data.size() == 0) {
            this.data = data;
            if (Looper.myLooper() == Looper.getMainLooper()) {
                notifyDataSetChanged();
            } else {
                uiHandler.removeMessages(WHAT_NOTIFY_DATA_SET_CHANGED);
                uiHandler.sendEmptyMessage(WHAT_NOTIFY_DATA_SET_CHANGED);
            }
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SlimDiffUtil(this.data, data, diffCallback));
            this.data = data;
            if (Looper.myLooper() == Looper.getMainLooper()) {
                diffResult.dispatchUpdatesTo(this);
            } else {
                uiHandler.removeMessages(WHAT_NOTIFY_DATA_SET_CHANGED);
                uiHandler.sendEmptyMessage(WHAT_NOTIFY_DATA_SET_CHANGED);
            }
        }
        return this;
    }

    public List<?> getData() {
        return data;
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = data.get(position);
        int index = dataTypes.indexOf(item.getClass());
        if (index == -1) {
            dataTypes.add(item.getClass());
        }
        index = dataTypes.indexOf(item.getClass());
        return index;
    }

    @NonNull
    @Override
    public SlimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Type dataType = dataTypes.get(viewType);
        ViewHolderCreator creator = creators.get(dataType);
        if (creator == null) {
            for (Type t : creators.keySet()) {
                if (isTypeMatch(t, dataType)) {
                    creator = creators.get(t);
                    break;
                }
            }
        }
        if (creator == null) {
            if (defaultCreator == null) {
                throw new IllegalArgumentException(String.format("Neither the TYPE: %s not The DEFAULT injector found...", dataType));
            }
            creator = defaultCreator;
        }
        SlimViewHolder holder = creator.create(parent);
        holder.init();
        return holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void onBindViewHolder(SlimViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull SlimViewHolder holder) {
        holder.attachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull SlimViewHolder holder) {
        holder.detachedFromWindow();
    }

    public SlimAdapter enableDiff() {
        enableDiff(new DefaultDiffCallback());
        return this;
    }

    public SlimAdapter enableDiff(DiffCallback diffCallback) {
        this.diffCallback = diffCallback;
        return this;
    }

    @SuppressWarnings("unchecked")
    public SlimAdapter registerDefault(final SlimInjector injector) {
        defaultCreator = new ViewHolderCreator(injector);
        return this;
    }

    public <T> SlimAdapter register(final SlimInjector<T> injector) {
        Type type = getSlimInjectorActualTypeArguments(injector);
        if (type == null) {
            throw new IllegalArgumentException();
        }
        creators.put(type, new ViewHolderCreator<>(injector));
        return this;
    }

    public SlimAdapter attachTo(RecyclerView... recyclerViews) {
        for (RecyclerView recyclerView : recyclerViews) {
            recyclerView.setAdapter(this);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private boolean isTypeMatch(Type type, Type targetType) {
        if (type instanceof Class && targetType instanceof Class) {
            return ((Class) type).isAssignableFrom((Class) targetType);
        } else if (type instanceof ParameterizedType && targetType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            ParameterizedType parameterizedTargetType = (ParameterizedType) targetType;
            if (isTypeMatch(parameterizedType.getRawType(), ((ParameterizedType) targetType).getRawType())) {
                Type[] types = parameterizedType.getActualTypeArguments();
                Type[] targetTypes = parameterizedTargetType.getActualTypeArguments();
                if (types.length != targetTypes.length) {
                    return false;
                }
                int len = types.length;
                for (int i = 0; i < len; i++) {
                    if (!isTypeMatch(types[i], targetTypes[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private <T> Type getSlimInjectorActualTypeArguments(SlimInjector<T> slimInjector) {
        Type[] interfaces = new Type[]{slimInjector.getClass().getGenericSuperclass()};
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                if (((ParameterizedType) type).getRawType().equals(SlimInjector.class)) {
                    Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (actualType instanceof Class) {
                        return actualType;
                    } else {
                        throw new IllegalArgumentException("The generic type argument of SlimInjector is NOT support" +
                                " Generic Parameterized Type now, Please using a WRAPPER class install of it directly.");
                    }
                }
            }
        }
        return null;
    }

    public interface DiffCallback {
        boolean areItemsTheSame(Object oldItem, Object newItem);

        boolean areContentsTheSame(Object oldItem, Object newItem);
    }

    private static class DefaultDiffCallback implements DiffCallback {

        @Override
        public boolean areItemsTheSame(Object oldItem, Object newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(Object oldItem, Object newItem) {
            return true;
        }
    }

    private static final class SlimDiffUtil extends DiffUtil.Callback {

        private List<?> oldData;
        private List<?> newData;
        private DiffCallback diffCallback;

        private SlimDiffUtil(List<?> oldData, List<?> newData, DiffCallback diffCallback) {
            this.oldData = oldData;
            this.newData = newData;
            this.diffCallback = diffCallback;
        }

        @Override
        public int getOldListSize() {
            return oldData == null ? 0 : oldData.size();
        }

        @Override
        public int getNewListSize() {
            return newData == null ? 0 : newData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return diffCallback.areItemsTheSame(oldData.get(oldItemPosition), newData.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return diffCallback.areContentsTheSame(oldData.get(oldItemPosition), newData.get(newItemPosition));
        }
    }

    private static class ViewHolderCreator<D> {

        private SlimInjector<D> injector;

        private ViewHolderCreator(SlimInjector<D> injector) {
            this.injector = injector;
        }

        private SlimViewHolder<D> create(ViewGroup parent) {
            return new SlimViewHolder<>(parent, injector);
        }
    }

    public interface Action<V extends View> {
        void action(V view);
    }

    public static abstract class SlimInjector<D> {

        public abstract int onGetLayoutResource();

        public void onInit(SlimViewHolder<D> viewHolder) {

        }

        public abstract void onBind(D data, SlimViewHolder<D> viewHolder);

        public void onViewAttachedToWindow(SlimViewHolder<D> viewHolder) {

        }

        public void onViewDetachedFromWindow(SlimViewHolder<D> viewHolder) {

        }
    }

    public final static class SlimViewHolder<D> extends RecyclerView.ViewHolder {

        private SlimInjector<D> injector;

        private SparseArray<View> viewMap;

        public D itemData;

        private SlimViewHolder(ViewGroup parent, SlimInjector<D> injector) {
            this(LayoutInflater.from(parent.getContext()).inflate(injector.onGetLayoutResource(), parent, false));
            this.injector = injector;
        }

        private SlimViewHolder(View itemView) {
            super(itemView);
            viewMap = new SparseArray<>();
        }

        private void init() {
            injector.onInit(this);
        }

        private void bind(D data) {
            itemData = data;
            injector.onBind(data, this);
        }

        private void attachedToWindow() {
            injector.onViewAttachedToWindow(this);
        }

        private void detachedFromWindow() {
            injector.onViewDetachedFromWindow(this);
        }

        @SuppressWarnings("unchecked")
        public final <T extends View> T findViewById(int id) {
            View view = viewMap.get(id);
            if (view == null) {
                view = itemView.findViewById(id);
                viewMap.put(id, view);
            }
            return (T) view;
        }

        public SlimViewHolder tag(int id, Object object) {
            findViewById(id).setTag(object);
            return this;
        }

        public SlimViewHolder text(int id, int res) {
            TextView view = findViewById(id);
            view.setText(res);
            return this;
        }

        public SlimViewHolder text(int id, CharSequence charSequence) {
            TextView view = findViewById(id);
            view.setText(charSequence);
            return this;
        }

        public SlimViewHolder typeface(int id, Typeface typeface, int style) {
            TextView view = findViewById(id);
            view.setTypeface(typeface, style);
            return this;
        }

        public SlimViewHolder typeface(int id, Typeface typeface) {
            TextView view = findViewById(id);
            view.setTypeface(typeface);
            return this;
        }

        public SlimViewHolder textColor(int id, int color) {
            TextView view = findViewById(id);
            view.setTextColor(color);
            return this;
        }

        public SlimViewHolder textSize(int id, int sp) {
            TextView view = findViewById(id);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
            return this;
        }

        public SlimViewHolder alpha(int id, float alpha) {
            View view = findViewById(id);
            view.setAlpha(alpha);
            return this;
        }

        public SlimViewHolder image(int id, int res) {
            ImageView view = findViewById(id);
            view.setImageResource(res);
            return this;
        }

        public SlimViewHolder image(int id, Drawable drawable) {
            ImageView view = findViewById(id);
            view.setImageDrawable(drawable);
            return this;
        }

        public SlimViewHolder background(int id, int res) {
            View view = findViewById(id);
            view.setBackgroundResource(res);
            return this;
        }

        public SlimViewHolder background(int id, Drawable drawable) {
            View view = findViewById(id);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackground(drawable);
            } else {
                view.setBackgroundDrawable(drawable);
            }
            return this;
        }

        public SlimViewHolder visible(int id) {
            findViewById(id).setVisibility(View.VISIBLE);
            return this;
        }

        public SlimViewHolder invisible(int id) {
            findViewById(id).setVisibility(View.INVISIBLE);
            return this;
        }

        public SlimViewHolder gone(int id) {
            findViewById(id).setVisibility(View.GONE);
            return this;
        }

        public SlimViewHolder visibility(int id, int visibility) {
            findViewById(id).setVisibility(visibility);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <V extends View> SlimViewHolder with(int id, Action<V> action) {
            action.action((V) findViewById(id));
            return this;
        }

        public SlimViewHolder clicked(int id, View.OnClickListener listener) {
            findViewById(id).setOnClickListener(listener);
            return this;
        }

        public SlimViewHolder clicked(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            return this;
        }

        public SlimViewHolder longClicked(int id, View.OnLongClickListener listener) {
            findViewById(id).setOnLongClickListener(listener);
            return this;
        }

        public SlimViewHolder longClicked(View.OnLongClickListener listener) {
            itemView.setOnLongClickListener(listener);
            return this;
        }

        public SlimViewHolder enable(int id, boolean enable) {
            findViewById(id).setEnabled(enable);
            return this;
        }

        public SlimViewHolder enable(int id) {
            findViewById(id).setEnabled(true);
            return this;
        }

        public SlimViewHolder disable(int id) {
            findViewById(id).setEnabled(false);
            return this;
        }

        public SlimViewHolder checked(int id, boolean checked) {
            Checkable view = findViewById(id);
            view.setChecked(checked);
            return this;
        }

        public SlimViewHolder selected(int id, boolean selected) {
            findViewById(id).setSelected(selected);
            return this;
        }

        @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
        public SlimViewHolder activated(int id, boolean activated) {
            findViewById(id).setSelected(activated);
            return this;
        }

        public SlimViewHolder pressed(int id, boolean pressed) {
            findViewById(id).setPressed(pressed);
            return this;
        }

        public SlimViewHolder adapter(int id, RecyclerView.Adapter adapter) {
            RecyclerView view = findViewById(id);
            view.setAdapter(adapter);
            return this;
        }

        @SuppressWarnings("unchecked")
        public SlimViewHolder adapter(int id, Adapter adapter) {
            AdapterView view = findViewById(id);
            view.setAdapter(adapter);
            return this;
        }

        public SlimViewHolder layoutManager(int id, RecyclerView.LayoutManager layoutManager) {
            RecyclerView view = findViewById(id);
            view.setLayoutManager(layoutManager);
            return this;
        }

        public SlimViewHolder addView(int id, View... views) {
            ViewGroup viewGroup = findViewById(id);
            for (View view : views) {
                viewGroup.addView(view);
            }
            return this;
        }

        public SlimViewHolder addView(int id, View view, ViewGroup.LayoutParams params) {
            ViewGroup viewGroup = findViewById(id);
            viewGroup.addView(view, params);
            return this;
        }

        public SlimViewHolder removeAllViews(int id) {
            ViewGroup viewGroup = findViewById(id);
            viewGroup.removeAllViews();
            return this;
        }

        public SlimViewHolder removeView(int id, View view) {
            ViewGroup viewGroup = findViewById(id);
            viewGroup.removeView(view);
            return this;
        }
    }
}
