package com.example.magicalscript.facefactor;

import android.content.Context;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class tabfrag extends Fragment {
    public static int turn = 0;
    private FragmentTabHost mTabHost;

    public tabfrag(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(turn<=1) {
            // Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.tab_frag, container, false);
            mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
            mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

            mTabHost.addTab(mTabHost.newTabSpec("faces").setIndicator(getTabIndicator(mTabHost.getContext(),R.string.faces,R.drawable.comedy)),
                    caps.class, null);
            mTabHost.addTab(mTabHost.newTabSpec("glasses").setIndicator(getTabIndicator(mTabHost.getContext(),R.string.glasses,R.drawable.glasses)),
                    glasses.class, null);
            mTabHost.addTab(mTabHost.newTabSpec("caps").setIndicator(getTabIndicator(mTabHost.getContext(),R.string.caps,R.drawable.baseballcap)),
                    Faces.class, null);

            turn ++;
            return rootView;
        }
        return null;
    }
    private View getTabIndicator(Context context, int title, int icon) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.imageView);
        iv.setImageResource(icon);
        TextView tv = (TextView) view.findViewById(R.id.textView);
        tv.setText(title);
        return view;
    }

}