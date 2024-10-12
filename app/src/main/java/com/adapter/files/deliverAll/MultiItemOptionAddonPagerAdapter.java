package com.adapter.files.deliverAll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.general.files.EnhancedWrapContentViewPager;
import com.general.files.GeneralFunctions;
import com.utils.Utils;
import com.zphr.user.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmList;

public class MultiItemOptionAddonPagerAdapter extends PagerAdapter {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final GeneralFunctions generalFunc;
    private final MultiItemOptionAddonListener multiItemOptionAddonListener;
    private JSONArray mCategoryArray = new JSONArray();


    private final List<Double> mOptionPriceList = new ArrayList<>();
    private ArrayList<String> mOptionIdList = new ArrayList<>();

    private final List<Double> mToppingPriceList = new ArrayList<>();
    private ArrayList<String> mToppingListId = new ArrayList<>();
    private boolean mIsEdit = false;
    private int mCurrentPosition = -1;

    public MultiItemOptionAddonPagerAdapter(Context context, GeneralFunctions generalFunc, MultiItemOptionAddonListener multiItemOptionAddonListener) {

        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.generalFunc = generalFunc;
        this.multiItemOptionAddonListener = multiItemOptionAddonListener;
    }


    @Override
    public int getCount() {
        return mCategoryArray.length();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        View itemView = mLayoutInflater.inflate(R.layout.item_basket_option_addon, container, false);

        JSONObject categoryObject = generalFunc.getJsonObject(mCategoryArray, position);

        TextView txtCategoryTitle = (TextView) itemView.findViewById(R.id.txtCategoryTitle);
        String categoryName = generalFunc.getJsonValueStr("vCatName", categoryObject);
        txtCategoryTitle.setText("" + categoryName);

        LinearLayout optionArea = (LinearLayout) itemView.findViewById(R.id.optionArea);
        TextView optionTitleTxt = (TextView) itemView.findViewById(R.id.optionTitleTxt);
        LinearLayout optionContainer = (LinearLayout) itemView.findViewById(R.id.optionContainer);

        LinearLayout topingArea = (LinearLayout) itemView.findViewById(R.id.topingArea);

        TextView topingTitleTxt = (TextView) itemView.findViewById(R.id.topingTitleTxt);
        LinearLayout topingContainer = (LinearLayout) itemView.findViewById(R.id.topingContainer);
        String categoryTitleName = generalFunc.getJsonValueStr("tOptionTitle", categoryObject);
        String topingTitleName = generalFunc.getJsonValueStr("tAddonTitle", categoryObject);

        if (Utils.checkText(categoryTitleName)) {
            optionTitleTxt.setText(categoryTitleName);
        } else {
            optionTitleTxt.setVisibility(View.GONE);
        }
        if (Utils.checkText(topingTitleName)) {
            topingTitleTxt.setText(topingTitleName);
        } else {
            topingTitleTxt.setVisibility(View.GONE);
        }

        JSONArray optionArray = generalFunc.getJsonArray("options", categoryObject);
        JSONArray addonArray = generalFunc.getJsonArray("addon", categoryObject);

        final RadioButton[] lastCheckedRB = {null};
        ArrayList<HashMap<String, String>> optionList = new ArrayList<>();
        ArrayList<HashMap<String, String>> topingList = new ArrayList<>();


        optionTitleTxt.setVisibility(View.GONE);
        if (mOptionIdList.get(position).equalsIgnoreCase("0")) {
            mOptionIdList.set(position, "-1");
        }

        topingTitleTxt.setVisibility(View.GONE);

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void setPrimaryItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            EnhancedWrapContentViewPager pager = (EnhancedWrapContentViewPager) container;
            mCurrentPosition = position;
            pager.measureCurrentView((LinearLayout) object);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

    public void setCategoryArrayList(JSONArray categoryArray, boolean isEdit) {
        this.mIsEdit = isEdit;
        this.mCategoryArray = categoryArray;
        if (isEdit) {
            for (int i = 0; i < mCategoryArray.length(); i++) {
                mOptionPriceList.add(i, 0.0);
                mToppingPriceList.add(i, 0.0);
            }
        } else {
            for (int i = 0; i < mCategoryArray.length(); i++) {
                mOptionPriceList.add(i, 0.0);
                mOptionIdList.add(i, "0");
                mToppingPriceList.add(i, 0.0);
            }
        }
        notifyDataSetChanged();
    }


    public String getCategoryArray() {
        return mCategoryArray.toString();
    }

    public void setSelectedData(ArrayList<String> optionIdList, ArrayList<String> toppingListId) {
        this.mOptionIdList = optionIdList;
        this.mToppingListId = toppingListId;
    }

    public interface MultiItemOptionAddonListener {
        void radioButtonPressed(String iOptionsCategoryId, ArrayList<String> mOptionIdList, List<Double> mTotalAmountList);

        void checkBoxPressed(String iOptionsCategoryId, ArrayList<String> mToppingListId, List<Double> mToppingPriceAmountList);
    }
}