package com.adapter.files;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.zphr.user.R;
import com.general.files.GeneralFunctions;
import com.model.TransactionTypesModel;
import com.utils.LoadImageGlide;
import com.view.MTextView;

import java.util.ArrayList;

/**
 * Created by Admin on 02-03-2017.
 */
public class RecentTransactionsTypeRecycleAdapter extends RecyclerView.Adapter<RecentTransactionsTypeRecycleAdapter.ViewHolder> {

    private ArrayList<TransactionTypesModel> list_item = new ArrayList<>();
    private Context context;
    OnTypeClickList onItemClickList;
    GeneralFunctions generalFunctions;

    public RecentTransactionsTypeRecycleAdapter(Context context, ArrayList<TransactionTypesModel> list_item) {
        this.context = context;
        this.list_item.addAll(list_item);
        generalFunctions = new GeneralFunctions(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_type, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TransactionTypesModel mapData = list_item.get(position);

        int vImage = mapData.getId();
        String vName = mapData.getName();
        boolean isSelected = mapData.getSelected();
        if (vImage != 0) {
            holder.typeImgView.setVisibility(View.VISIBLE);
            new LoadImageGlide.builder(context, LoadImageGlide.bind(vImage), holder.typeImgView).setErrorImagePath(R.mipmap.ic_no_icon).setPlaceholderImagePath(R.mipmap.ic_no_icon).build();


        } else {
            holder.typeImgView.setVisibility(View.GONE);

        }

        holder.typeTxt.setText(vName);

        holder.typeContainerView.setOnClickListener(v -> {
            onItemClickList.onTypeClick(mapData);
        });

        if (generalFunctions.isRTLmode()) {
            holder.typeImgView.setScaleX(-1);
        }

        if (isSelected) {
            holder.containerView.setBackground(getActContext().getResources().getDrawable(R.drawable.ic_square_shadow_type_selected));
            holder.typeTxt.setTextColor(getActContext().getResources().getColor(R.color.white));
        } else {
            holder.containerView.setBackground(getActContext().getResources().getDrawable(R.drawable.ic_square_shadow_type_unseleted));
            holder.typeTxt.setTextColor(getActContext().getResources().getColor(R.color.black));
        }

    }

    private Context getActContext() {

        return context;
    }


    @Override
    public int getItemCount() {
        if (list_item == null) {
            return 0;
        } else {
            return list_item.size();
        }
    }

    public void updatelist(ArrayList<TransactionTypesModel> updatedTypeList) {
        list_item.clear();
        list_item.addAll(updatedTypeList);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout typeContainerView;
        public LinearLayout containerView;
        public ImageView typeImgView;
        public MTextView typeTxt;

        public ViewHolder(View view) {
            super(view);

            typeImgView = (ImageView) view.findViewById(R.id.typeImgView);
            typeTxt = (MTextView) view.findViewById(R.id.typeTxt);
            typeContainerView = (LinearLayout) view.findViewById(R.id.typeContainerView);
            containerView = (LinearLayout) view.findViewById(R.id.containerView);
        }
    }

    public void setOnItemClickList(OnTypeClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public interface OnTypeClickList {
        void onTypeClick(TransactionTypesModel transactionTypesModel);
    }

}