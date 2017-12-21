package com.prajwal.firebasesos;

/**
 * Created by PRAJWAL on 26-09-2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MyAdapter extends ArrayAdapter<Item> {

    ArrayList<Item> personList = new ArrayList<>();

    public MyAdapter(Context context, int textViewResourceId, ArrayList<Item> objects) {
        super(context, textViewResourceId, objects);
        personList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.list_view_items, null);
        TextView personName = (TextView) v.findViewById(R.id.personName);
        TextView distance = (TextView) v.findViewById(R.id.distance);
        TextView time = (TextView) v.findViewById(R.id.time);
        TextView address = (TextView) v.findViewById(R.id.address);
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        personName.setText(personList.get(position).getPersonName());
        distance.setText(personList.get(position).getDistance());
        time.setText(personList.get(position).getTime().toString());
        address.setText(personList.get(position).getAddress().toString());
        imageView.setImageResource(personList.get(position).getPersonImage());
        return v;

    }

}