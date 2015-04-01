package hoverpuck.anomalousmaker.com.hoverpuckcontroller;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ControllerFragment extends Fragment {
    public static ControllerFragment newInstance() {
        return new ControllerFragment();
    }

    public ControllerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return new ControllerUI(inflater.getContext());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

}
