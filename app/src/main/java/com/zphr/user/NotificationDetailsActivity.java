package com.zphr.user;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.activity.ParentActivity;
import com.utils.LoadImage;
import com.utils.Utils;
import com.view.MTextView;

import java.util.HashMap;

public class NotificationDetailsActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        HashMap<String, String> list = (HashMap<String, String>) getIntent().getSerializableExtra("data");

        MTextView titleTxt = findViewById(R.id.titleTxt);
        ImageView backImgView = findViewById(R.id.backImgView);
        backImgView.setOnClickListener(v -> onBackPressed());
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        MTextView notificationtitleTxt = findViewById(R.id.notificationtitleTxt);
        MTextView dateTxt = findViewById(R.id.dateTxt);
        MTextView detailsTxt = findViewById(R.id.detailsTxt);
        ImageView newsImage = findViewById(R.id.newsImage);

        String vImage = list.get("vImage");
        if (vImage != null && !vImage.isEmpty()) {
            findViewById(R.id.imagArea).setVisibility(View.VISIBLE);

            new LoadImage.builder(LoadImage.bind(Utils.getResizeImgURL(getActContext(), vImage, (int) (Utils.getScreenPixelWidth(getActContext()) - Utils.dipToPixels(getActContext(), 20)), 0)), newsImage).build();

        }
        String eType = list.get("eType");
        String label = eType.equalsIgnoreCase("Notification") ? "LBL_NOTIFICATIONS" : "LBL_NEWS";
        titleTxt.setText(generalFunc.retrieveLangLBl(eType, label));

        notificationtitleTxt.setText(list.get("vTitle"));
        dateTxt.setText(list.get("dDateTime"));
        detailsTxt.setText(list.get("tDescription"));
    }

    private Context getActContext() {
        return NotificationDetailsActivity.this;
    }
}