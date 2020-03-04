package tw.dp103g3.itfood_shop.person;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;


public class AboutFragment extends Fragment {
    private Activity activity;
    private Toolbar toolbarAbout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        Common.connectOrderServer(activity, Common.getShopId(activity));
        toolbarAbout = view.findViewById(R.id.toolbarAbout);

        toolbarAbout.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

}
