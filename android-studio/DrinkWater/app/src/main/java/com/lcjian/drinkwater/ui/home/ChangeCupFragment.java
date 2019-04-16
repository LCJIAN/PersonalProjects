package com.lcjian.drinkwater.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Cup;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;
import com.lcjian.drinkwater.ui.base.SlimAdapter;
import com.lcjian.drinkwater.util.DateUtils;
import com.lcjian.drinkwater.util.StringUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChangeCupFragment extends BaseDialogFragment {

    @BindView(R.id.rv_cups)
    RecyclerView rv_cups;
    @BindView(R.id.ll_add_custom_cup)
    LinearLayout ll_add_custom_cup;
    @BindView(R.id.et_capacity)
    EditText et_capacity;
    @BindView(R.id.tv_unit)
    TextView tv_unit;
    @BindView(R.id.btn_cancel)
    Button btn_cancel;
    @BindView(R.id.btn_ok)
    Button btn_ok;

    private Unbinder unbinder;
    private Disposable mDisposable;

    private SlimAdapter mAdapter;

    private Unit mUnit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_cup, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_cancel.setOnClickListener(v -> {
            if (rv_cups.getVisibility() == View.VISIBLE) {
                dismiss();
            } else {
                rv_cups.setVisibility(View.VISIBLE);
                ll_add_custom_cup.setVisibility(View.GONE);
            }
        });
        btn_ok.setOnClickListener(v -> {
            if (ll_add_custom_cup.getVisibility() == View.VISIBLE) {
                if (TextUtils.isEmpty(et_capacity.getEditableText())) {
                    return;
                }
                Cup cup = new Cup();
                cup.cupCapacity = Double.parseDouble(et_capacity.getEditableText().toString());
                cup.timeAdded = DateUtils.now();
                cup.timeModified = cup.timeAdded;
                mAppDatabase.cupDao().insert(cup);
                rv_cups.setVisibility(View.VISIBLE);
                ll_add_custom_cup.setVisibility(View.GONE);
            } else {
                dismiss();
            }
        });

        rv_cups.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<Cup>() {



                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.cup_grid_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<Cup> viewHolder) {
                        viewHolder.clicked(v -> {

                            if (viewHolder.itemData.cupCapacity.intValue() == 0) {
                                rv_cups.setVisibility(View.GONE);
                                ll_add_custom_cup.setVisibility(View.VISIBLE);
                            } else {
                                Setting setting = mAppDatabase.settingDao().getAllSync().get(0);
                                setting.cupId = viewHolder.itemData.id;
                                mAppDatabase.settingDao().update(setting);
                                dismiss();
                            }
                        });
                    }

                    @Override
                    public void onBind(Cup data, SlimAdapter.SlimViewHolder<Cup> viewHolder) {
                        ImageView iv_cup = viewHolder.findViewById(R.id.iv_cup);
                        TextView tv_cup = viewHolder.findViewById(R.id.tv_cup);

                        String s = StringUtils.formatDecimalToString(data.cupCapacity * Double.parseDouble(mUnit.rate.split(",")[1]))
                                + mUnit.name.split(",")[1];
                        tv_cup.setText(s);
                        switch (data.cupCapacity.intValue()) {
                            case 0:
                                iv_cup.setImageResource(R.drawable.ic_cup_custom_ml_add);
                                break;
                            case 100:
                                iv_cup.setImageResource(R.drawable.ic_cup_100_ml_for_change);
                                break;
                            case 200:
                                iv_cup.setImageResource(R.drawable.ic_cup_200_ml_for_change);
                                break;
                            case 300:
                                iv_cup.setImageResource(R.drawable.ic_cup_300_ml_for_change);
                                break;
                            case 400:
                                iv_cup.setImageResource(R.drawable.ic_cup_400_ml_for_change);
                                break;
                            case 500:
                                iv_cup.setImageResource(R.drawable.ic_cup_500_ml_for_change);
                                break;
                            default:
                                iv_cup.setImageResource(R.drawable.ic_cup_custom_ml);
                                tv_cup.setText("");
                                break;
                        }
                    }
                });
        rv_cups.setAdapter(mAdapter);

        mDisposable = Flowable.combineLatest(
                mAppDatabase.cupDao().getAllAsync(),
                mAppDatabase.unitDao().getCurrentUnitAsync().map(units -> units.get(0)),
                (cups, unit) -> {
                    Cup cup = new Cup();
                    cup.cupCapacity = 0d;
                    cups.add(cup);
                    return Pair.create(cups, unit);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                            mUnit = pair.second;
                            mAdapter.updateData(pair.first);

                            tv_unit.setText(mUnit.name.split(",")[1]);
                        },
                        throwable -> {
                        });
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        mDisposable.dispose();
        super.onDestroyView();
    }

}
