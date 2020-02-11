package tw.dp103g3.itfood_shop.stats;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tw.dp103g3.itfood_shop.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SaleStatsFragment extends Fragment {


    public SaleStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sale_stats, container, false);
    }

}
