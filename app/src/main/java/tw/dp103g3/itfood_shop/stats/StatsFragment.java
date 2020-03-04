package tw.dp103g3.itfood_shop.stats;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment {
    private static final String TAG = "TAG_StatsFragment";
    private Activity activity;
    private ListView lvStats;
    private NavController navController;
    private int[] navigationArray = {R.id.action_statsFragment_to_monthStatsFragment,
            R.id.action_statsFragment_to_timeStatsFragment,
            R.id.action_statsFragment_to_saleStatsFragment};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Common.disconnectOrderServer();
        navController = Navigation.findNavController(view);
        lvStats = view.findViewById(R.id.lvStats);
        String[] textStats = getResources().getStringArray(R.array.textStats);
        ArrayAdapter statsAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_1, textStats);
        lvStats.setAdapter(statsAdapter);
        lvStats.setOnItemClickListener((parent, view1, position, id) ->
                navController.navigate(navigationArray[position]));
    }
}
