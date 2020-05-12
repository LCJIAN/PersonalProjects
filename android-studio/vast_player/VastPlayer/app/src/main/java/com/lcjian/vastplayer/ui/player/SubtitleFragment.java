package com.lcjian.vastplayer.ui.player;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.Subtitle;
import com.lcjian.vastplayer.data.network.service.SubtitleService;
import com.lcjian.vastplayer.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SubtitleFragment extends BaseDialogFragment {

    @BindView(R.id.tv_sub_process_state)
    TextView tv_sub_process_state;
    @BindView(R.id.rv_subtitle)
    RecyclerView rv_subtitle;
    @BindView(R.id.pb_loading)
    ProgressBar pb_loading;
    @BindView(R.id.tv_no_subtitle)
    TextView tv_no_subtitle;
    private Unbinder mUnBinder;
    private SubtitleService mSubtitleService;
    private CompositeDisposable mDisposables;
    private Disposable mDisposable;

    private String mKeyword;
    private int mCurrentSubId;

    public static SubtitleFragment newInstance(String keyword, int currentSubId) {
        SubtitleFragment subtitleFragment = new SubtitleFragment();
        Bundle args = new Bundle();
        args.putString("keyword", keyword);
        args.putInt("current_sub_id", currentSubId);
        subtitleFragment.setArguments(args);
        return subtitleFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKeyword = getArguments().getString("keyword");
        mCurrentSubId = getArguments().getInt("current_sub_id");
        mSubtitleService = (new Subtitle()).subtitleService();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AppCompatDialog(getActivity(), R.style.Theme_VastPlayer_Player_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subtitle, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_subtitle.setLayoutManager(new LinearLayoutManager(getActivity()));
        String keyword;
        if (mKeyword.contains(".")) {
            keyword = mKeyword.substring(0, mKeyword.indexOf("."));
        } else {
            keyword = mKeyword;
        }
        pb_loading.setVisibility(View.VISIBLE);
        mDisposable = mSubtitleService
                .subSearch(keyword, 0, 15)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subResponse -> rv_subtitle.setAdapter(
                        new SubtitlePickAdapter(subResponse.sub.subs, mRxBus, mCurrentSubId)),
                        throwable -> {
                            tv_no_subtitle.setVisibility(View.VISIBLE);
                            pb_loading.setVisibility(View.GONE);
                            if (!(throwable instanceof IllegalStateException)) {
                                Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        },
                        () -> pb_loading.setVisibility(View.GONE));

        mDisposables = new CompositeDisposable();
        mDisposables.add(mRxBus.asFlowable()
                .subscribe(event -> {
                    if (event instanceof Integer) {
                        int msg = (Integer) event;
                        if (msg != 0) {
                            tv_sub_process_state.setText(msg);
                        }
                    }
                }));
    }

    @Override
    public void onDestroyView() {
        if (mDisposables != null) {
            mDisposables.dispose();
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mUnBinder.unbind();
        super.onDestroyView();
    }
}
