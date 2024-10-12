package com.adapter.files;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.zphr.user.R;
import com.general.files.MyApp;
import com.utils.Utils;
import com.utils.VectorUtils;
import com.view.anim.loader.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

public class MyImageAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<HashMap<String, String>> arrayList;
    private LayoutInflater layoutInflater;
    private OnActivePosition onActivePosition;

    public MyImageAdapter(Context context, ArrayList<HashMap<String, String>> arrayList, OnActivePosition onActivePosition) {
        this.context = context;
        this.arrayList = arrayList;
        this.onActivePosition = onActivePosition;
        this.layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == ((View) o);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.item_slider_images, container, false);
        int size230sdp = MyApp.getInstance().getCurrentAct().getResources().getDimensionPixelSize(R.dimen._230sdp);

        //VideoView videoView = view.findViewById(R.id.item_video);
        RelativeLayout rlMainSliderView = view.findViewById(R.id.rlMainSliderView);
        AVLoadingIndicatorView videoLoaderView = view.findViewById(R.id.videoLoaderView);
        AppCompatImageView imgPlayIcon = view.findViewById(R.id.imgPlayIcon);
        AppCompatImageView ivProductImage = view.findViewById(R.id.ivProductImage);
        AppCompatImageView thumbnailImage = view.findViewById(R.id.thumbnailImage);

        HashMap<String, String> data = arrayList.get(position);

        if (data.get("eFileType").equalsIgnoreCase("Video")) {
            videoLoaderView.setVisibility(View.GONE);
            ivProductImage.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(data.get("ThumbImage"))) {
                String imageUrl = Utils.getResizeImgURL(MyApp.getInstance().getCurrentAct(), data.get("ThumbImage"), (int) Utils.getScreenPixelWidth(context), size230sdp);
                Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_novideo__icon).error(R.drawable.ic_novideo__icon))
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {

                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(thumbnailImage);
            }
            thumbnailImage.setVisibility(View.VISIBLE);
            VectorUtils.manageVectorImage(context, imgPlayIcon, R.drawable.ic_play_button, R.drawable.ic_play_button_compat);
            imgPlayIcon.setVisibility(View.VISIBLE);


        } else {
            if (!TextUtils.isEmpty(data.get("vImage"))) {
                String imageUrl = Utils.getResizeImgURL(MyApp.getInstance().getCurrentAct(), data.get("vImage"), (int) Utils.getScreenPixelWidth(context), size230sdp);
                Glide.with(context)
                        .load(imageUrl)
                        .apply(new RequestOptions().placeholder(R.mipmap.ic_no_icon).error(R.mipmap.ic_no_icon))
                        .into(ivProductImage);

            }

            videoLoaderView.setVisibility(View.GONE);
            imgPlayIcon.setVisibility(View.GONE);
            ivProductImage.setVisibility(View.VISIBLE);

        }

        rlMainSliderView.setOnClickListener(v -> {
            if (onActivePosition != null) {
                onActivePosition.onActivePosition(position);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void showImage(String fileUrl) {
        Dialog builder = new Dialog(context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.WHITE));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(context);

        Glide.with(context)
                .load(fileUrl)
                .apply(new RequestOptions().placeholder(R.mipmap.ic_no_icon).error(R.mipmap.ic_no_icon))
                .into(imageView);

        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.show();
    }

    public interface OnActivePosition {
        void onActivePosition(int pos);
    }

}
