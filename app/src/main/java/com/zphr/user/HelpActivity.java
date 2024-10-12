package com.zphr.user;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;

import com.activity.ParentActivity;
import com.adapter.files.HelpCategoryRecycleAdapter;
import com.general.files.ActUtils;
import com.general.files.GeneralFunctions;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HelpActivity extends ParentActivity implements HelpCategoryRecycleAdapter.OnItemClickList {

    MTextView titleTxt;
    ProgressBar loading;
    MTextView noHelpTxt;

    RecyclerView helpCategoryRecyclerView;
    HelpCategoryRecycleAdapter adapter;
    ErrorView errorView;

    ArrayList<HashMap<String, String>> list;
    ArrayList<String> answer = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ImageView backImgView = findViewById(R.id.backImgView);
        backImgView.setOnClickListener(v -> onBackPressed());
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        titleTxt = findViewById(R.id.titleTxt);
        loading = findViewById(R.id.loading);
        noHelpTxt = findViewById(R.id.noHelpTxt);
        helpCategoryRecyclerView = findViewById(R.id.helpCategoryRecyclerView);
        errorView = findViewById(R.id.errorView);

        list = new ArrayList<>();
        adapter = new HelpCategoryRecycleAdapter(getActContext(), list, generalFunc);
        helpCategoryRecyclerView.setAdapter(adapter);

        getHelpCategory();
        setLabels();

        adapter.setOnItemClickList(this);
    }

    private void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FAQ_TXT"));
    }

    @Override
    public void onItemClick(int position, String question, String answer) {
        Bundle bn = new Bundle();
        bn.putString("QUESTION", question);
        bn.putString("ANSWER", answer);
        new ActUtils(getActContext()).startActWithData(QuestionAnswerActivity.class, bn);
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void getHelpCategory() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        if (list.size() > 0) {
            list.clear();
        }

        final HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getFAQ");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("appType", Utils.app_type);

        noHelpTxt.setVisibility(View.GONE);

        ApiHandler.execute(getActContext(), parameters, responseString -> {

            noHelpTxt.setVisibility(View.GONE);
            if (responseString != null && !responseString.equals("")) {

                closeLoader();

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {

                    JSONArray obj_arr = generalFunc.getJsonArray(Utils.message_str, responseString);

                    for (int i = 0; i < obj_arr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(obj_arr, i);

                        HashMap<String, String> map = new HashMap<>();

                        map.put("vTitle", generalFunc.getJsonValueStr("vTitle", obj_temp));
                        //map.put("QUESTION_LIST", obj_temp.toString());
                        list.add(map);

                        answer.add(generalFunc.getJsonValueStr("vTitle", obj_temp));
                        JSONArray obj_ques = generalFunc.getJsonArray("Questions", obj_temp.toString());
                        if (obj_ques != null && obj_ques.length() > 0) {
                            for (int i1 = 0; i1 < obj_ques.length(); i1++) {
                                JSONObject obj_temp1 = generalFunc.getJsonObject(obj_ques, i1);


                                HashMap<String, String> map1 = new HashMap<>();
                                map1.put("vSubTitle", generalFunc.getJsonValueStr("vTitle", obj_temp1));
                                map1.put("tAnswer", generalFunc.getJsonValueStr("tAnswer", obj_temp1));

                                list.add(map1);

                                answer.add(generalFunc.getJsonValueStr("vTitle", obj_temp1) + "," + generalFunc.getJsonValueStr("tAnswer", obj_temp1));

                                //list.put(listDataHeader.get(i), answer);
                            }
                        }


                    }

                    adapter.notifyDataSetChanged();

                } else {
                    noHelpTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    noHelpTxt.setVisibility(View.VISIBLE);
                }
            } else {
                generateErrorView();
            }
        });

    }

    private void generateErrorView() {
        closeLoader();
        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");
        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(this::getHelpCategory);
    }

    private Context getActContext() {
        return HelpActivity.this;
    }
}
