package com.zphr.user;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import com.general.files.MyApp;
import com.general.files.OpenNoLocationView;
import com.utils.Logger;

/**
 * Created by Admin on 31-08-2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Activity currentActivity = MyApp.getInstance().getCurrentAct();
        Logger.e("NetworkChangeReceiver", "NetworkStauts ::stcheck::"+ currentActivity);
        checkNetworkSettings(currentActivity);
    }

    private void checkNetworkSettings(Activity currentActivity) {
        if (currentActivity != null) {
            if (currentActivity instanceof UberXActivity || currentActivity instanceof MainActivity || currentActivity instanceof UberXHomeActivity) {
                ViewGroup viewGroup;
                if (currentActivity instanceof UberXHomeActivity) {
                    viewGroup = currentActivity.findViewById(R.id.MainArea);

                } else {
                    viewGroup = currentActivity.findViewById(android.R.id.content);
                }
                handleNetworkView(currentActivity, viewGroup);
            }
        }
    }

    private void handleNetworkView(Activity activity, ViewGroup viewGroup) {
        try {
            OpenNoLocationView.getInstance(activity, viewGroup).configView(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
