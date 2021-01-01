package com.dacho.darkmoon.layoutFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dacho.darkmoon.R;

/**
 * Created by hirofumi on 15/02/27.
 */
public class LoadFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.load_activity,container,false);
    }
}
