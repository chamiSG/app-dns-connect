/* Copyright (C) 2016-2019 Julian Andres Klode <jak@jak-linux.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package org.jak_linux.dns66.main;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textview.MaterialTextView;

import org.jak_linux.dns66.Configuration;
import org.jak_linux.dns66.FileHelper;
import org.jak_linux.dns66.MainActivity;
import org.jak_linux.dns66.R;
import org.jak_linux.dns66.vpn.AdVpnService;
import org.jak_linux.dns66.vpn.Command;

import java.io.IOException;
import java.io.InputStreamReader;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class StartFragment extends Fragment {
    public static final int REQUEST_START_VPN = 1;
    private static final String TAG = "StartFragment";

    public StartFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);

        ImageView view = (ImageView) rootView.findViewById(R.id.state_image);

        MaterialTextView mDnsView = rootView.findViewById(R.id.dns_view);
        mDnsView.setText(MainActivity.config.dnsServers.items.get(0).location);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return startStopService();
            }
        });

        Button startButton = (Button) rootView.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopService();
            }
        });

        updateStatus(rootView, AdVpnService.vpnStatus);

        return rootView;
    }

    private boolean startStopService() {
        if (AdVpnService.vpnStatus != AdVpnService.VPN_STATUS_STOPPED) {
            Log.i(TAG, "Attempting to disconnect");

            Intent intent = new Intent(getActivity(), AdVpnService.class);
            intent.putExtra("COMMAND", Command.STOP.ordinal());
            getActivity().startService(intent);
        } else {
            startService();
        }
        return true;
    }

    public static void updateStatus(View rootView, int status) {
        Context context = rootView.getContext();
        ImageView stateImage = (ImageView) rootView.findViewById(R.id.state_image);
        Button startButton = (Button) rootView.findViewById(R.id.start_button);

        if (stateImage == null)
            return;

        stateImage.setContentDescription(rootView.getContext().getString(AdVpnService.vpnStatusToTextId(status)));
        stateImage.setImageAlpha(255);
        stateImage.setImageTintList(ContextCompat.getColorStateList(context, R.color.colorStateImage));
        switch(status) {
            case AdVpnService.VPN_STATUS_RECONNECTING:
            case AdVpnService.VPN_STATUS_STARTING:
            case AdVpnService.VPN_STATUS_STOPPING:
                stateImage.setImageDrawable(context.getDrawable(R.drawable.ic_settings_black_24dp));
                startButton.setText(R.string.action_stop);
                break;
            case AdVpnService.VPN_STATUS_STOPPED:
                stateImage.setImageAlpha(32);
                stateImage.setImageTintList(null);
                stateImage.setImageDrawable(context.getDrawable(R.mipmap.app_icon_large));
                startButton.setText(R.string.action_start);
                break;
            case AdVpnService.VPN_STATUS_RUNNING:
                stateImage.setImageDrawable(context.getDrawable(R.drawable.ic_verified_user_black_24dp));
                startButton.setText(R.string.action_stop);
                break;
            case AdVpnService.VPN_STATUS_RECONNECTING_NETWORK_ERROR:
                stateImage.setImageDrawable(context.getDrawable(R.drawable.ic_error_black_24dp));
                startButton.setText(R.string.action_stop);
                break;
        }
    }


    private void startService() {
        Log.i(TAG, "Attempting to connect");
        Intent intent = VpnService.prepare(getContext());
        if (intent != null) {
            startActivityForResult(intent, REQUEST_START_VPN);
        } else {
            onActivityResult(REQUEST_START_VPN, RESULT_OK, null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: Received result=" + resultCode + " for request=" + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_START_VPN && resultCode == RESULT_CANCELED) {
            Toast.makeText(getContext(), R.string.could_not_configure_vpn_service, Toast.LENGTH_LONG).show();
        }
        if (requestCode == REQUEST_START_VPN && resultCode == RESULT_OK) {
            Log.d("MainActivity", "onActivityResult: Starting service");
            Intent intent = new Intent(getContext(), AdVpnService.class);
            intent.putExtra("COMMAND", Command.START.ordinal());
            intent.putExtra("NOTIFICATION_INTENT",
                    PendingIntent.getActivity(getContext(), 0,
                            new Intent(getContext(), MainActivity.class), 0));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(intent);
            } else {
                getContext().startService(intent);
            }

        }
    }
}
