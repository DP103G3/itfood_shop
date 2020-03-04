package tw.dp103g3.itfood_shop.person;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.shop.Shop;
import tw.dp103g3.itfood_shop.task.CommonTask;

import static tw.dp103g3.itfood_shop.main.Common.TAG;


public class ChangePasswordFragment extends Fragment {
    private Activity activity;
    private SharedPreferences preferences;
    private NavController navController;
    private View view;
    private EditText etObsoletePassword, etPassword, etCheckPassword;
    private TextView tvObsoletePasswordWarning, tvPasswordWarning, tvCheckPasswordWarning;
    private Button btSend;
    private CommonTask getOrderTask, editOrderTask, loginTask;
    private Shop shop;
    private Toolbar toolbarChangPassword;
    int shopId, counts;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        Common.connectOrderServer(activity, Common.getShopId(activity));
        etObsoletePassword = view.findViewById(R.id.etObsoletePassword);
        etPassword = view.findViewById(R.id.etPassword);
        etCheckPassword = view.findViewById(R.id.etCheckPassword);
        tvObsoletePasswordWarning = view.findViewById(R.id.tvObsoletePasswordWarning);
        tvPasswordWarning = view.findViewById(R.id.tvPasswordWarning);
        tvCheckPasswordWarning = view.findViewById(R.id.tvCheckPasswordWarning);
        btSend = view.findViewById(R.id.btSend);
        toolbarChangPassword = view.findViewById(R.id.toolbarChangPassword);

        toolbarChangPassword.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        navController = Navigation.findNavController(view);


        btSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                counts = 0;
                SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCES_SHOP, Context.MODE_PRIVATE);
                String password = pref.getString("password", null);
                String obsoletePassword = etObsoletePassword.getText().toString().trim();
                String newPassword = etPassword.getText().toString().trim();
                String checkPassword = etCheckPassword.getText().toString().trim();
                if (password != null){
                    //檢查新密碼
                    if (newPassword.length() <= 0){
                        tvPasswordWarning.setText("請輸入新密碼！");
                        counts++;
                    }else {
                        tvPasswordWarning.setText("");
                    }

                    //檢查確認密碼
                    if (checkPassword.length() <= 0){
                        tvCheckPasswordWarning.setText("請再輸入新密碼！");
                        counts++;
                    }else if (checkPassword.equals(newPassword)) {
                        tvCheckPasswordWarning.setText("");
                    }else {
                        tvCheckPasswordWarning.setText("密碼不相符！請確認後再試一次。");
                        counts++;
                    }

                    //檢查舊密碼
                    if (obsoletePassword.length() <= 0){
                        tvObsoletePasswordWarning.setText("請輸入舊密碼！");
                        counts++;
                    }else if (password.equals(obsoletePassword)) {
                        tvObsoletePasswordWarning.setText("");
                    }else {
                        tvObsoletePasswordWarning.setText("密碼錯誤！請確認後再試一次。");
                        counts++;
                    }

                    if(counts != 0){
                        counts = 0;
                        return;
                    }else {

                        if (Common.networkConnected(activity)) {

                            int id = shopId;
                            String url = Url.URL + "/ShopServlet";
                            if (Common.networkConnected(activity)) {
                                JsonObject jsonObject = new JsonObject();
                                Gson gson = new GsonBuilder().create();
                                jsonObject.addProperty("action", "setShopUpDateById");
                                jsonObject.addProperty("id", id);
                                String jsonOut = jsonObject.toString();
                                CommonTask getShopTask = new CommonTask(url, jsonOut);
                                try {
                                    String jsonIn = getShopTask.execute().get();
                                    shop = gson.fromJson(jsonIn, Shop.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Common.showToast(activity, R.string.textNoNetwork);
                            }

                            shop.updatePassword(id, newPassword);
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("action", "updatePassword");
                            jsonObject.addProperty("shop", new Gson().toJson(shop));


                            int count = 0;
                            try {
                                String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                count = Integer.valueOf(result);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                            if (count == 0) {
                                Common.showToast(activity, R.string.textUpdateFail);
                            } else {
                                Common.showToast(activity, R.string.textUpdateSuccess);
                                /* 回前一個Fragment */
                                navController.popBackStack();
                            }
                        } else {
                            Common.showToast(activity, R.string.textNoNetwork);
                        }
                    }



                }else {
                    navController.popBackStack(R.id.mainOrderFragment, false);
                    Common.showToast(activity, "請登入後再試。");
                }


            }
        });



    }
}
