package com.navigation.vibration.adaptors;

import android.content.Context;
import android.graphics.Movie;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.ImageView;
import android.widget.TextView;

import com.navigation.vibration.R;
import com.navigation.vibration.models.VibrationPattern;

import java.util.ArrayList;
import java.util.List;

public class VibrationPatternAdaptor extends ArrayAdapter<VibrationPattern> {

    private Context mContext;
    private List<VibrationPattern> patternList = new ArrayList<>();

    public VibrationPatternAdaptor(@NonNull Context context, ArrayList<VibrationPattern> list) {
        super(context, 0 , list);
        mContext = context;
        this.patternList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_activity,parent,false);

        VibrationPattern currentPattern = patternList.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.label);
        name.setText(currentPattern.getName());


        return listItem;
    }
}

