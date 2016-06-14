package com.bpk.filescanner.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.bpk.filescanner.R;
import com.bpk.filescanner.parcelable.Update;

public class ExtDataFragment extends Fragment {
    private ArrayAdapter<String> extAdapter;
    private ListView listView;


    public ExtDataFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ext_data, container, false);


        listView = (ListView)v.findViewById(R.id.extDataList);
        extAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1);
        listView.setAdapter(extAdapter);

        return v;
    }

    public void updateData(Update update){
        extAdapter.clear();
        for(int i = 0;i < 5 ; i++){
            extAdapter.add(update.mostFrequentFiveExtensions[i]);
        }
        extAdapter.notifyDataSetChanged();
    }


}
