package cniao5.com.cniao5shop.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cniao5.com.cniao5shop.R;


/**
 * Created by Ivan on 15/9/22.
 */
public class MineFragment extends BaseFragment{



    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine,container,false);
    }

    @Override
    public void init() {

    }
}
