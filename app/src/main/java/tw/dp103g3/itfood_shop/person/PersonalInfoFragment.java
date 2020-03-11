package tw.dp103g3.itfood_shop.person;


import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.shop.Shop;
import tw.dp103g3.itfood_shop.task.CommonTask;
import tw.dp103g3.itfood_shop.task.ImageTask;


public class PersonalInfoFragment extends Fragment {
    private final static String TAG = "TAG_ShopDetailsFragment";
    private Activity activity;
    private ImageView ivShop;
    private TextView tvShopId, tvShopState, tvShopName,tvShopPhone, tvShopEmail, tvShopJoinDate
            , tvShopAddress, tvShopTax, tvShopArea, tvShopInfo;
    private Button btUpdate;
    private Shop shop, shopDetail;
    private int shopId;
    private Toolbar toolbarPersonInfo;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        shopId = Common.getShopId(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_info, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivShop = view.findViewById(R.id.ivShop);
        tvShopId = view.findViewById(R.id.tvShopId);
        tvShopState = view.findViewById(R.id.tvShopState);
        tvShopName = view.findViewById(R.id.tvShopName);
        tvShopPhone = view.findViewById(R.id.tvShopPhone);
        tvShopEmail = view.findViewById(R.id.tvShopEmail);
        tvShopJoinDate = view.findViewById(R.id.tvShopJoinDate);
        tvShopAddress = view.findViewById(R.id.tvShopAddress);
        tvShopTax = view.findViewById(R.id.tvShopTax);
        //tvShopArea = view.findViewById(R.id.tvShopArea);
        tvShopInfo = view.findViewById(R.id.tvShopInfo);
        btUpdate = view.findViewById(R.id.btUpdate);
        toolbarPersonInfo = view.findViewById(R.id.toolbarPersonInfo);

        toolbarPersonInfo.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());


        showShop();

    }
    private void showShop() {
        String url = Url.URL + "/ShopServlet";
        int id = shopId;
        shopDetail = null;
        int imageSize = getResources().getDisplayMetrics().widthPixels / 2;
        Bitmap bitmap = null;
        try {
            bitmap = new ImageTask(url, id, imageSize).execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivShop.setImageBitmap(bitmap);
        } else {
            ivShop.setImageResource(R.drawable.no_image);
        }
        if (Common.networkConnected(activity)) {
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "getShopAllById");
            jsonObject.addProperty("id", id);
            String jsonOut = jsonObject.toString();
            CommonTask getShopTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getShopTask.execute().get();
                shopDetail = Common.gson.fromJson(jsonIn, Shop.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }


        tvShopId.setText(String.valueOf(id));
        if(shopDetail.getState() == 0){
            tvShopState.setText("未上架/下架店家");
            tvShopState.setTextColor(Color.RED);
        }else if(shopDetail.getState() == 1){
            tvShopState.setText("已上架店家");
            tvShopState.setTextColor(Color.BLUE);
        }
        tvShopName.setText(shopDetail.getName());
        tvShopPhone.setText(shopDetail.getPhone());
        tvShopEmail.setText(shopDetail.getEmail());
        shopId = Common.getShopId(activity);
        Date jointime = shopDetail.getJointime();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
        tvShopJoinDate.setText(sdFormat.format(jointime));
        tvShopAddress.setText(shopDetail.getAddress());
        tvShopTax.setText(shopDetail.getTax());
        tvShopArea.setText(String.valueOf(shopDetail.getArea()));
        tvShopInfo.setText(shopDetail.getInfo());

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shopDetail);
                Navigation.findNavController(view)
                        .navigate(R.id.action_personalInfoFragment_to_personalInfoUpdateFragment, bundle);
            }
        });



    }

}
