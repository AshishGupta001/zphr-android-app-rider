package com.zphr.user;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.activity.ParentActivity;
import com.adapter.files.ViewPagerAdapter;
import com.fragments.NotiFicationFragment;
import com.google.android.material.tabs.TabLayout;
import com.utils.Utils;
import com.view.MTextView;

import java.util.ArrayList;

public class NotificationActivity extends ParentActivity {

    MTextView titleTxt;
    CharSequence[] titles;
    ArrayList<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        titleTxt = findViewById(R.id.titleTxt);
        ImageView backImgView = findViewById(R.id.backImgView);
        backImgView.setOnClickListener(v -> onBackPressed());
        titleTxt.setText(generalFunc.retrieveLangLBl("Notifications", "LBL_NOTIFICATIONS"));

        ViewPager appLogin_view_pager = findViewById(R.id.appLogin_view_pager);
        TabLayout material_tabs = findViewById(R.id.material_tabs);

        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }

        if (generalFunc.getJsonValueStr("ENABLE_NEWS_SECTION", obj_userProfile).equalsIgnoreCase("Yes")) {

            if (generalFunc.isRTLmode()) {

                titles = new CharSequence[]{generalFunc.retrieveLangLBl("all", "LBL_NEWS"), generalFunc.retrieveLangLBl("", "LBL_NOTIFICATIONS"), generalFunc.retrieveLangLBl("news", "LBL_ALL")};
                material_tabs.setVisibility(View.VISIBLE);

                fragmentList.add(generateNotificationFrag(Utils.News));
                fragmentList.add(generateNotificationFrag(Utils.Notificatons));
                fragmentList.add(generateNotificationFrag(Utils.All));

            } else {

                titles = new CharSequence[]{generalFunc.retrieveLangLBl("all", "LBL_ALL"), generalFunc.retrieveLangLBl("", "LBL_NOTIFICATIONS"), generalFunc.retrieveLangLBl("news", "LBL_NEWS")};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateNotificationFrag(Utils.All));
                fragmentList.add(generateNotificationFrag(Utils.Notificatons));
                fragmentList.add(generateNotificationFrag(Utils.News));
            }

        } else {
            titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_NOTIFICATIONS")};
            material_tabs.setVisibility(View.GONE);
            fragmentList.add(generateNotificationFrag(Utils.Notificatons));


        }
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setupWithViewPager(appLogin_view_pager);
        appLogin_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                fragmentList.get(position).onResume();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public NotiFicationFragment generateNotificationFrag(String type) {
        NotiFicationFragment frag = new NotiFicationFragment();
        Bundle bn = new Bundle();
        bn.putString("type", type);
        frag.setArguments(bn);
        return frag;
    }

    public Context getActContext() {
        return NotificationActivity.this;
    }
}