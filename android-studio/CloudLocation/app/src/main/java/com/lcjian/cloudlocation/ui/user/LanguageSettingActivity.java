package com.lcjian.cloudlocation.ui.user;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.widget.ImageButton;
import android.widget.TextView;

import com.franmontiel.localechanger.LocaleChanger;
import com.franmontiel.localechanger.utils.ActivityRecreationHelper;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.ui.base.BaseActivity;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LanguageSettingActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_english)
    TextView tv_english;
    @BindView(R.id.tv_chinese)
    TextView tv_chinese;
    @BindView(R.id.tv_spanish)
    TextView tv_spanish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_setting);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.language_setting));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_english.setOnClickListener(v -> changeLocale(Locale.ENGLISH));
        tv_chinese.setOnClickListener(v -> changeLocale(Locale.SIMPLIFIED_CHINESE));
        tv_spanish.setOnClickListener(v -> changeLocale(new Locale("es")));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Locale.SIMPLIFIED_CHINESE.equals(LocaleChanger.getLocale())) {
            tv_chinese.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tick, 0);
            tv_english.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tv_spanish.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else if (Locale.ENGLISH.equals(LocaleChanger.getLocale())) {
            tv_chinese.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tv_english.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tick, 0);
            tv_spanish.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            tv_chinese.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tv_english.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tv_spanish.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tick, 0);
        }
        ActivityRecreationHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        ActivityRecreationHelper.onDestroy(this);
        super.onDestroy();
    }

    private void changeLocale(Locale locale) {
        LocaleChanger.setLocale(locale);
        changeAppLanguage(locale);
        ActivityRecreationHelper.recreate(this, true);
    }

    private void changeAppLanguage(Locale locale) {
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            configuration.setLocales(new LocaleList(locale));
            createConfigurationContext(configuration);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, metrics);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, metrics);
        }
    }
}
