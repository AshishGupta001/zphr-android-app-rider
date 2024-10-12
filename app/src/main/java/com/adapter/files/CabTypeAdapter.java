package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.general.files.GeneralFunctions;
import com.general.files.ToolTipDialog;
import com.zphr.user.R;
import com.utils.CommonUtilities;
import com.utils.LoadImage;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Admin on 04-07-2016.
 */
public class CabTypeAdapter extends RecyclerView.Adapter<CabTypeAdapter.ViewHolder> {

    private final GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list_item;
    Context mContext;
    String vehicleIconPath = CommonUtilities.SERVER_URL + "webimages/icons/VehicleType/";
    String vehicleDefaultIconPath = CommonUtilities.SERVER_URL + "webimages/icons/DefaultImg/";
    String selectedVehicleTypeId = "", userProfileJson;

    OnItemClickList onItemClickList;
    ViewHolder viewHolder;

    boolean isMultiDelivery = false, isVertical;
    private final int whiteColor;
    View toolTipView;


    public CabTypeAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
        whiteColor = mContext.getResources().getColor(R.color.white);
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        isVertical = generalFunc.getJsonValue("VEHICLE_TYPE_SHOW_METHOD", userProfileJson) != null && generalFunc.getJsonValue("VEHICLE_TYPE_SHOW_METHOD", userProfileJson).equalsIgnoreCase("Vertical");
    }

    public void setRentalItem(ArrayList<HashMap<String, String>> list_item) {
        this.list_item = list_item;
    }

    @NonNull
    @Override
    public CabTypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (isVertical) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_design_vertical_cab_type, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_design_cab_type, parent, false);
        }
        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public void setSelectedVehicleTypeId(String selectedVehicleTypeId) {
        this.selectedVehicleTypeId = selectedVehicleTypeId;
    }

    public void isMultiDelivery(boolean isMultiDelivery) {
        this.isMultiDelivery = isMultiDelivery;
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, final int position) {
        setData(viewHolder, position);
    }

    private void setData(CabTypeAdapter.ViewHolder viewHolder, final int position) {
        HashMap<String, String> item = list_item.get(position);

        String vVehicleType = item.get("vVehicleType");
        String iVehicleTypeId = item.get("iVehicleTypeId");


        String eRental = item.get("eRental");
        if (eRental != null && !eRental.equals("") && eRental.equalsIgnoreCase("Yes")) {
            viewHolder.carTypeTitle.setText(item.get("vRentalVehicleTypeName"));
        } else {
            viewHolder.carTypeTitle.setText(vVehicleType);
        }


        String iPersonSize = item.get("iPersonSize");
        if (isVertical) {
            if (iPersonSize != null && !eRental.equals("")) {
                viewHolder.personsizeTxt.setVisibility(View.VISIBLE);
                viewHolder.personsizeTxt.setText(item.get("iPersonSize"));
            } else {
                viewHolder.personsizeTxt.setVisibility(View.GONE);
            }
        }


        boolean isHover = selectedVehicleTypeId.equals(iVehicleTypeId);


        String total_fare = item.get("total_fare");
        if (Utils.checkText(total_fare)) {
            viewHolder.totalfare.setText(generalFunc.convertNumberWithRTL(total_fare));
        } else {
            viewHolder.infoimage.setVisibility(View.GONE);
            viewHolder.totalfare.setText("");
        }


        String imgUrl, imgName;
        if (isHover) {
            imgName = getImageName(Objects.requireNonNull(item.get("vLogo1")));
        } else {
            imgName = getImageName(Objects.requireNonNull(item.get("vLogo")));
        }
        if (imgName.equals("")) {
            if (isHover) {
                imgUrl = vehicleDefaultIconPath + "hover_ic_car.png";
            } else {
                imgUrl = vehicleDefaultIconPath + "ic_car.png";
            }
        } else {
            imgUrl = vehicleIconPath + item.get("iVehicleTypeId") + "/android/" + imgName;
        }
        loadImage(viewHolder, imgUrl);


        viewHolder.contentArea.setOnClickListener(view -> {
            if (onItemClickList != null) {
                onItemClickList.onItemClick(position);
                toolTipView = viewHolder.contentArea;
            }
        });

        if (isMultiDelivery) {
            viewHolder.llArea.setBackgroundColor(whiteColor);
        }

        if (isHover) {
            toolTipView = viewHolder.contentArea;
            if (!isMultiDelivery) {
                if (viewHolder.totalfare.getText().toString().length() > 0) {
                    viewHolder.infoimage.setVisibility(View.VISIBLE);
                }
                //  viewHolder.totalfare.setTextColor(Color.parseColor("#777b82"));
            }

            viewHolder.imagareaselcted.setVisibility(View.VISIBLE);
            viewHolder.imagarea.setVisibility(View.GONE);

            int color = mContext.getResources().getColor(R.color.appThemeColor_2);
            viewHolder.carTypeTitle.setTextColor(color);

            if (isVertical) {
                viewHolder.carTypeTitle.setTextColor(mContext.getResources().getColor(R.color.black));
                viewHolder.totalfare.setTextColor(mContext.getResources().getColor(R.color.black));
                // viewHolder.carTypeDesc.setTextColor(Color.parseColor("#f5fbfc"));
                //  viewHolder.totalfare.setTextColor(Color.parseColor("#f2f2f3"));
                viewHolder.infoimage.setVisibility(View.GONE);
            }
            if (!isVertical) {
                new CreateRoundedView(mContext.getResources().getColor(R.color.white), (int) mContext.getResources().getDimension(R.dimen._30sdp), 2,
                        color, viewHolder.carTypeImgViewselcted);
                viewHolder.carTypeImgViewselcted.setBorderColor(color);
            }

//            if (item.get("eDeliveryHelper") != null && Objects.requireNonNull(item.get("eDeliveryHelper")).equalsIgnoreCase("Yes")) {
//                if (lastPosition != position) {
//                    lastPosition = position;
//                    isClick = false;
//                }
//                if (!isClick) {
//                    new Handler(Looper.myLooper()).postDelayed(() -> {
//                        manageToolTip();
//                        if (MyApp.getInstance().mainAct != null) {
//                            MyApp.getInstance().mainAct.transperntView.setVisibility(View.VISIBLE);
//                        }
//                        isClick = true;
//                    }, 200);
//                }
//            }

            if (isVertical) {
                // viewHolder.contentArea.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_sharp_rounded_shadow_type_selected));
                viewHolder.contentArea.setBackgroundColor(Color.parseColor("#ededed"));
                // viewHolder.llArea.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                viewHolder.carTypeDesc.setText(item.get("tInfoText"));
                viewHolder.carTypeDesc.setVisibility(View.VISIBLE);
            }


        } else {
            if (!isMultiDelivery) {
                viewHolder.infoimage.setVisibility(View.GONE);
            }

            if (isVertical) {
                //  viewHolder.contentArea.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_sharp_rounded_shadow_type_traspernt));
                viewHolder.contentArea.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                viewHolder.llArea.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                viewHolder.totalfare.setTextColor(mContext.getResources().getColor(R.color.black));
                viewHolder.carTypeDesc.setText(item.get("tInfoText"));
                viewHolder.carTypeDesc.setVisibility(View.VISIBLE);
                viewHolder.infoimage.setVisibility(View.GONE);
            }


            viewHolder.imagareaselcted.setVisibility(View.GONE);
            viewHolder.imagarea.setVisibility(View.VISIBLE);
            viewHolder.carTypeTitle.setTextColor(mContext.getResources().getColor(R.color.black));


            int color = Color.parseColor("#cbcbcb");
            if (!isVertical) {
                new CreateRoundedView(Color.parseColor("#ffffff"), (int) mContext.getResources().getDimension(R.dimen._30sdp), 2,
                        color, viewHolder.carTypeImgView);
                //   viewHolder.carTypeImgView.setColorFilter(Color.parseColor("#999fa2"));
                viewHolder.carTypeImgView.setBorderColor(color);
            }


        }

        if (isVertical) {

            if (position == list_item.size() - 1) {
                viewHolder.lastView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.lastView.setVisibility(View.GONE);
            }
        }

        if (isMultiDelivery) {
            viewHolder.totalFareArea.setVisibility(View.GONE);
            if (isVertical) {
                viewHolder.personsizeTxt.setVisibility(View.GONE);
                if (item.get("eDeliveryHelper").equalsIgnoreCase("Yes")) {
                    viewHolder.DeliveryHelper.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.DeliveryHelper.setVisibility(View.GONE);
                }
            }
        } else {
            viewHolder.totalFareArea.setVisibility(View.VISIBLE);
            if (isVertical) {
                viewHolder.personsizeTxt.setVisibility(View.VISIBLE);
                viewHolder.DeliveryHelper.setVisibility(View.GONE);
            }
        }

        if (isVertical) {
            viewHolder.DeliveryHelper.setOnClickListener(v -> {
                new ToolTipDialog(mContext, generalFunc, generalFunc.retrieveLangLBl("", "LBL_DEL_HELPER"), item.get("tDeliveryHelperNoteUser") + "");
            });
        }
    }


    private String getImageName(String vLogo) {
        String imageName;

        if (vLogo.equals("")) {
            return vLogo;
        }

        DisplayMetrics metrics = (mContext.getResources().getDisplayMetrics());
        int densityDpi = (int) (metrics.density * 160f);
        switch (densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                imageName = "mdpi_" + vLogo;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                imageName = "mdpi_" + vLogo;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                imageName = "hdpi_" + vLogo;
                break;

            case DisplayMetrics.DENSITY_TV:
                imageName = "hdpi_" + vLogo;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                imageName = "xhdpi_" + vLogo;
                break;

            case DisplayMetrics.DENSITY_280:
                imageName = "xhdpi_" + vLogo;
                break;

            case DisplayMetrics.DENSITY_400:
                imageName = "xxhdpi_" + vLogo;
                break;

            case DisplayMetrics.DENSITY_360:
                imageName = "xxhdpi_" + vLogo;
                break;
            case DisplayMetrics.DENSITY_420:
                imageName = "xxhdpi_" + vLogo;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                imageName = "xxhdpi_" + vLogo;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                imageName = "xxxhdpi_" + vLogo;
                break;

            case DisplayMetrics.DENSITY_560:
                imageName = "xxxhdpi_" + vLogo;
                break;

            default:
                imageName = "xxhdpi_" + vLogo;
                break;
        }

        return imageName;
    }

    private void loadImage(final CabTypeAdapter.ViewHolder holder, String imageUrl) {

        new LoadImage.builder(LoadImage.bind(imageUrl), holder.carTypeImgView).setPicassoListener(new LoadImage.PicassoListener() {
            @Override
            public void onSuccess() {
                holder.loaderView.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                holder.loaderView.setVisibility(View.VISIBLE);
            }
        }).build();
        new LoadImage.builder(LoadImage.bind(imageUrl), holder.carTypeImgViewselcted).setPicassoListener(new LoadImage.PicassoListener() {
            @Override
            public void onSuccess() {
                holder.loaderView.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                holder.loaderView.setVisibility(View.VISIBLE);
            }
        }).build();


    }

    @Override
    public int getItemCount() {
        if (list_item == null) {
            return 0;
        }
        return list_item.size();
    }

    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public void clickOnItem(int position) {
        if (onItemClickList != null) {
            onItemClickList.onItemClick(position);
        }
    }

    public interface OnItemClickList {
        void onItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        SelectableRoundedImageView carTypeImgView, carTypeImgViewselcted;
        MTextView carTypeTitle, totalfare, carTypeDesc, personsizeTxt;
        ImageView DeliveryHelper;
        View llArea, lastView, leftSeperationLine, rightSeperationLine, leftSeperationLine2, rightSeperationLine2;
        RelativeLayout contentArea;
        AVLoadingIndicatorView loaderView, loaderViewselected;
        ImageView infoimage;
        LinearLayout totalFareArea;
        FrameLayout imagarea, imagareaselcted;

        private ViewHolder(View view) {
            super(view);

            carTypeImgView = view.findViewById(R.id.carTypeImgView);
            carTypeImgViewselcted = view.findViewById(R.id.carTypeImgViewselcted);
            carTypeTitle = view.findViewById(R.id.carTypeTitle);
            personsizeTxt = view.findViewById(R.id.personsizeTxt);
            DeliveryHelper = view.findViewById(R.id.DeliveryHelper);
            totalFareArea = view.findViewById(R.id.totalFareArea);
            leftSeperationLine = view.findViewById(R.id.leftSeperationLine);
            rightSeperationLine = view.findViewById(R.id.rightSeperationLine);
            leftSeperationLine2 = view.findViewById(R.id.leftSeperationLine2);
            rightSeperationLine2 = view.findViewById(R.id.rightSeperationLine2);
            contentArea = view.findViewById(R.id.contentArea);
            llArea = view.findViewById(R.id.llArea);
            loaderView = view.findViewById(R.id.loaderView);
            loaderViewselected = view.findViewById(R.id.loaderViewselected);
            totalfare = view.findViewById(R.id.totalfare);
            imagarea = view.findViewById(R.id.imagarea);
            imagareaselcted = view.findViewById(R.id.imagareaselcted);
            infoimage = view.findViewById(R.id.infoimage);
            carTypeDesc = view.findViewById(R.id.carTypeDesc);
            lastView = view.findViewById(R.id.lastView);
        }
    }
}