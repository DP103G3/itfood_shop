package tw.dp103g3.itfood_shop.person;


import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;


public class InformationFragment extends Fragment {
    private Activity activity;
    private Toolbar toolbarInformation;

    public InformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_information, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

        toolbarInformation = view.findViewById(R.id.toolbarInformation);

        toolbarInformation.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

}
