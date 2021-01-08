/* Copyright (C) 2016-2019 Julian Andres Klode <jak@jak-linux.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package org.conn.dns.main;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.conn.dns.Configuration;
import org.conn.dns.FileHelper;
import org.conn.dns.ItemChangedListener;
import org.conn.dns.MainActivity;
import org.conn.dns.R;

public class DNSFragment extends Fragment implements FloatingActionButtonFragment {

    private ItemRecyclerViewAdapter mAdapter;

    public DNSFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dns, container, false);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.dns_entries);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ItemRecyclerViewAdapter(MainActivity.config.dnsServers.items, 2);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);




        return rootView;
    }

    @Override
    public void setupFloatingActionButton(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity main = (MainActivity) getActivity();
                main.editItem(2, null, new ItemChangedListener() {
                    @Override
                    public void onItemChanged(Configuration.Item item) {
                        MainActivity.config.dnsServers.items.add(item);
                        mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
                        FileHelper.writeSettings(getContext(), MainActivity.config);
                    }
                });
            }
        });
    }
}
