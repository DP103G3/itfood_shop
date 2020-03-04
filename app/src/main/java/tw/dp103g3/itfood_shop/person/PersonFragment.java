package tw.dp103g3.itfood_shop.person;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;



public class PersonFragment extends Fragment {
    private static final String TAG = "TAG_PersonFragment";
    private Activity activity;
    private SharedPreferences preferences;
    private ListAdapter shopAdapter, guestAdapter;
    private int[] shopAction, guestAction;
    private List<Map<String, Object>> shopList, guestList;
    private int shopId;
    private View view;
    private NavController navController;
    private ListView listView;
    private TextView tvShopId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        preferences =
                activity.getSharedPreferences(Common.PREFERENCES_SHOP, Context.MODE_PRIVATE);
        shopId = Common.getShopId(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        //tvShopId = view.findViewById(R.id.tvShopId);
        //tvShopId.setText(String.valueOf(shopId));
        navController = Navigation.findNavController(view);
        Common.disconnectOrderServer();
        initListMap();
        listView = view.findViewById(R.id.listView);
        int backgroundColor = getResources().getColor(R.color.colorItemBackground, activity.getTheme());
        listView.setBackgroundColor(backgroundColor);
        if (shopId == 0) {
            guestAdapter = new SimpleAdapter(activity, guestList, R.layout.basic_list_item,
                    new String[]{"icon", "title"}, new int[]{R.id.ivIcon, R.id.tvTitle});
            listView.setAdapter(guestAdapter);
            listView.setOnItemClickListener(((parent, v, position, id) ->
                    navController.popBackStack(R.id.mainOrderFragment, false)));
        } else {
            shopAdapter = new SimpleAdapter(activity, shopList, R.layout.basic_list_item,
                    new String[]{"icon", "title"}, new int[]{R.id.ivIcon, R.id.tvTitle});
            listView.setAdapter(shopAdapter);
            listView.setOnItemClickListener(((parent, v, position, id) -> {
                NavController navController = Navigation.findNavController(v);
                if (position != 5) {
                    navController.navigate(shopAction[position]);
                } else {
                    preferences.edit().putString("email", null)
                            .putString("password", null)
                            .putString("shopId", null)
                            .apply();
                    navController.popBackStack(R.id.mainOrderFragment, false);
                    Common.showToast(activity, "已登出。");
                }
            }));
        }
    }

    private void initListMap() {
        shopList = new ArrayList<>();
        int[] shopIcon = new int[]{R.drawable.person, R.drawable.lock, R.drawable.ic_local_dining_black_24dp, R.drawable.information, R.drawable.about, R.drawable.logout};
        String[] shopTitle = new String[]{getString(R.string.textPersonInfo), getString(R.string.textChangPassword), getString(R.string.textDish), getString(R.string.textInformation), getString(R.string.textAbout), getString(R.string.textLogout)};
        shopAction = new int[]{R.id.action_personFragment_to_personalInfoFragment,
                R.id.action_personFragment_to_changePasswordFragment,
                R.id.action_personFragment_to_dishFragment,
                R.id.action_personFragment_to_informationFragment,
                R.id.action_personFragment_to_aboutFragment,
                R.id.action_personFragment_to_mainOrderFragment};
        guestList = new ArrayList<>();
        int[] guestIcon = new int[]{R.drawable.login};
        String[] guestTitle = new String[]{getString(R.string.textLogin)};
        for (int i = 0; i < shopIcon.length; i++) {
            Map<String, Object> shopItem = new HashMap<>();
            shopItem.put("icon", shopIcon[i]);
            shopItem.put("title", shopTitle[i]);
            shopList.add(shopItem);
        }
        for (int i = 0; i < guestIcon.length; i++) {
            Map<String, Object> guestItem = new HashMap<>();
            guestItem.put("icon", guestIcon[i]);
            guestItem.put("title", guestTitle[i]);
            guestList.add(guestItem);
        }
    }


    public void sendLoginResult(boolean isSuccessful) {
        if (isSuccessful) {
            Navigation.findNavController(view).popBackStack(R.id.loginFragment, false);
        }

    }

}
