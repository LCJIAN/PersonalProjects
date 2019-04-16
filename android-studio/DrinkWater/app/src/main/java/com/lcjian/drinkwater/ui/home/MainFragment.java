package com.lcjian.drinkwater.ui.home;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Cup;
import com.lcjian.drinkwater.data.db.entity.Record;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.AdvanceAdapter;
import com.lcjian.drinkwater.ui.base.BaseFragment;
import com.lcjian.drinkwater.ui.base.SlimAdapter;
import com.lcjian.drinkwater.util.DateUtils;
import com.lcjian.drinkwater.util.DimenUtils;
import com.lcjian.drinkwater.util.Spans;
import com.lcjian.drinkwater.util.StringUtils;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainFragment extends BaseFragment {

    @BindView(R.id.tv_message_keep)
    TextView tv_message_keep;
    @BindView(R.id.arc_progress)
    ArcProgress arc_progress;
    @BindView(R.id.tv_daily_intake)
    TickerView tv_daily_intake;
    @BindView(R.id.tv_daily_intake_goal)
    TextView tv_daily_intake_goal;
    @BindView(R.id.ll_drink)
    LinearLayout ll_drink;
    @BindView(R.id.iv_cup_type)
    ImageView iv_cup_type;
    @BindView(R.id.tv_cup_capacity)
    TextView tv_cup_capacity;
    @BindView(R.id.fl_change_cup)
    FrameLayout fl_change_cup;
    @BindView(R.id.iv_cup_type_full)
    ImageView iv_cup_type_full;

    @BindView(R.id.rv_today_records)
    RecyclerView rv_today_records;

    private Unbinder unbinder;

    private Disposable mDisposable;
    private Disposable mDisposable2;

    private SlimAdapter mAdapter;

    private DataHolder mDataHolder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_daily_intake.setCharacterLists(TickerUtils.provideNumberList());
        tv_daily_intake.setText("00");
        arc_progress.setMax(100);

        ll_drink.setOnClickListener(v -> {
            Record record = new Record();
            record.intake = mDataHolder.cup.cupCapacity;
            record.cupCapacity = mDataHolder.cup.cupCapacity;
            record.timeAdded = DateUtils.now();
            record.timeModified = record.timeAdded;
            mAppDatabase.recordDao().insert(record);

            mDisposable2 = Single.just(true)
                    .delay(600, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aBoolean -> new WellDoneFragment().show(getChildFragmentManager(), "WellDoneFragment"));
        });
        fl_change_cup.setOnClickListener(v -> new ChangeCupFragment().show(getChildFragmentManager(), "ChangeCupFragment"));

        rv_today_records.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<Record>() {

                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.daily_intake_record_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<Record> viewHolder) {
                        viewHolder.clicked(v -> {
                            PopupMenu popupMenu = new PopupMenu(v.getContext(), v, Gravity.END);
                            popupMenu.inflate(R.menu.menu_record);
                            popupMenu.setOnMenuItemClickListener(item -> {
                                switch (item.getItemId()) {
                                    case R.id.action_delete:
                                        mAppDatabase.recordDao().delete(viewHolder.itemData);
                                        break;
                                    case R.id.action_modify:
                                        ModifyRecordFragment.newInstance(viewHolder.itemData.id)
                                                .show(getChildFragmentManager(), "ModifyRecordFragment");
                                        break;
                                }
                                return true;
                            });
                            popupMenu.show();
                        });
                    }

                    @Override
                    public void onBind(Record data, SlimAdapter.SlimViewHolder<Record> viewHolder) {
                        switch (StringUtils.formatDecimalToInt(data.cupCapacity)) {
                            case 100:
                                viewHolder.image(R.id.iv_cup, R.drawable.ic_cup_100_ml_full);
                                break;
                            case 200:
                                viewHolder.image(R.id.iv_cup, R.drawable.ic_cup_200_ml_full);
                                break;
                            case 300:
                                viewHolder.image(R.id.iv_cup, R.drawable.ic_cup_300_ml_full);
                                break;
                            case 400:
                                viewHolder.image(R.id.iv_cup, R.drawable.ic_cup_400_ml_full);
                                break;
                            case 500:
                                viewHolder.image(R.id.iv_cup, R.drawable.ic_cup_500_ml_full);
                                break;
                            default:
                                viewHolder.image(R.id.iv_cup, R.drawable.ic_cup_custom_ml_full);
                                break;
                        }

                        viewHolder.text(R.id.tv_record_time, DateUtils.convertDateToStr(data.timeAdded, "HH:mm"))
                                .text(R.id.tv_intake,
                                        StringUtils.formatDecimalToString(data.intake * Double.parseDouble(mDataHolder.unit.rate.split(",")[1]))
                                                + " " + mDataHolder.unit.name.split(",")[1]);
                    }
                })
                .enableDiff(new SlimAdapter.DiffCallback() {
                    @Override
                    public boolean areItemsTheSame(Object oldItem, Object newItem) {
                        return ((Record) oldItem).id.equals(((Record) newItem).id);
                    }

                    @Override
                    public boolean areContentsTheSame(Object oldItem, Object newItem) {
                        return ((Record) oldItem).intake.equals(((Record) newItem).intake)
                                && ((Record) oldItem).timeAdded.equals(((Record) newItem).timeAdded);
                    }
                });
        AdvanceAdapter advanceAdapter = new AdvanceAdapter(mAdapter);
        View header = LayoutInflater.from(view.getContext()).inflate(R.layout.daily_intake_remind_item, rv_today_records, false);
        advanceAdapter.addHeader(header);
        rv_today_records.setAdapter(advanceAdapter);

        Date today = DateUtils.today();
        mDisposable = Flowable.combineLatest(mAppDatabase.settingDao().getAllAsync().map(settings -> settings.get(0)),
                mAppDatabase.unitDao().getCurrentUnitAsync().map(units -> units.get(0)),
                mAppDatabase.cupDao().getCurrentCupAsync().map(cups -> cups.get(0)),
                mAppDatabase.recordDao().getAllAsyncByTime(today, DateUtils.addDays(today, 1)),
                mAppDatabase.recordDao().getFirstAsync().map(records -> records.get(0))
                        .onErrorReturnItem(new Record()),
                DataHolder::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataHolder -> {
                            mDataHolder = dataHolder;

                            int a = dataHolder.firstRecord.timeAdded == null ? 0
                                    : DateUtils.dayDiff(DateUtils.today(), dataHolder.firstRecord.timeAdded) + 1;
                            tv_message_keep.setText(getResources().getQuantityString(R.plurals.b, a, a));

                            double intake = 0d;
                            if (!dataHolder.records.isEmpty()) {
                                for (Record record : dataHolder.records) {
                                    intake += record.intake;
                                }
                            }

                            int progress;
                            if (intake < dataHolder.setting.intakeGoal) {
                                progress = (int) (intake * 100 / dataHolder.setting.intakeGoal);
                            } else {
                                progress = 100;
                            }
                            ObjectAnimator.ofInt(arc_progress, "progress", arc_progress.getProgress(), progress).start();

                            Context context = tv_daily_intake.getContext();
                            double rate = Double.parseDouble(dataHolder.unit.rate.split(",")[1]);
                            tv_daily_intake.setText(StringUtils.formatDecimalToString(intake * rate));
                            tv_daily_intake_goal.setText(new Spans()
                                    .append("/" + StringUtils.formatDecimalToString(dataHolder.setting.intakeGoal * rate),
                                            new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorTextBlack)),
                                            new AbsoluteSizeSpan(DimenUtils.spToPixels(40, context)))
                                    .append(dataHolder.unit.name.split(",")[1],
                                            new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorTextBlack)),
                                            new AbsoluteSizeSpan(DimenUtils.spToPixels(25, context))));
                            tv_cup_capacity.setText(new Spans().append(StringUtils.formatDecimalToString(dataHolder.cup.cupCapacity * rate))
                                    .append(" ").append(dataHolder.unit.name.split(",")[1]));

                            switch (StringUtils.formatDecimalToInt(dataHolder.cup.cupCapacity)) {
                                case 100:
                                    iv_cup_type.setImageResource(R.drawable.ic_cup_100_ml_drink);
                                    iv_cup_type_full.setImageResource(R.drawable.ic_cup_100_ml_full);
                                    break;
                                case 200:
                                    iv_cup_type.setImageResource(R.drawable.ic_cup_200_ml_drink);
                                    iv_cup_type_full.setImageResource(R.drawable.ic_cup_200_ml_full);
                                    break;
                                case 300:
                                    iv_cup_type.setImageResource(R.drawable.ic_cup_300_ml_drink);
                                    iv_cup_type_full.setImageResource(R.drawable.ic_cup_300_ml_full);
                                    break;
                                case 400:
                                    iv_cup_type.setImageResource(R.drawable.ic_cup_400_ml_drink);
                                    iv_cup_type_full.setImageResource(R.drawable.ic_cup_400_ml_full);
                                    break;
                                case 500:
                                    iv_cup_type.setImageResource(R.drawable.ic_cup_500_ml_drink);
                                    iv_cup_type_full.setImageResource(R.drawable.ic_cup_500_ml_full);
                                    break;
                                default:
                                    iv_cup_type.setImageResource(R.drawable.ic_cup_custom_ml_drink);
                                    iv_cup_type_full.setImageResource(R.drawable.ic_cup_custom_ml_full);
                                    break;
                            }

                            mAdapter.updateData(dataHolder.records);
                        },
                        throwable -> {
                        });
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        mDisposable.dispose();
        if (mDisposable2 != null) {
            mDisposable2.dispose();
        }
        super.onDestroyView();
    }

    private static class DataHolder {
        private Setting setting;
        private Unit unit;
        private Cup cup;
        private List<Record> records;
        private Record firstRecord;

        private DataHolder(Setting setting, Unit unit, Cup cup, List<Record> records, Record firstRecord) {
            this.unit = unit;
            this.cup = cup;
            this.setting = setting;
            this.records = records;
            this.firstRecord = firstRecord;
        }
    }
}
