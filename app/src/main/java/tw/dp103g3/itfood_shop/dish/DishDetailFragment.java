package tw.dp103g3.itfood_shop.dish;


import android.annotation.SuppressLint;
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

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.shop.Dish;
import tw.dp103g3.itfood_shop.task.CommonTask;
import tw.dp103g3.itfood_shop.task.ImageTask;


public class DishDetailFragment extends Fragment {
    private final static String TAG = "TAG_ShopDishDetailsFragment";
    private Activity activity;
    private Dish dish, dishDetail, account;
    private ImageView ivDish, ivButton;
    private TextView tvDishId, tvDishState, tvDishName,tvDishInfo, tvDishPrice;
    private Switch swDishState;
    private Button btUpdate;
    private Toolbar toolbarShopDishDetail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("dish") == null) {
            return;
        }
        dish = (Dish) bundle.getSerializable("dish");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textShopDishDetailPageTitle);
        return inflater.inflate(R.layout.fragment_dish_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivDish = view.findViewById(R.id.ivDish);
        //tvDishId = view.findViewById(R.id.tvDishId);
        tvDishState = view.findViewById(R.id.tvDishState);
        tvDishName = view.findViewById(R.id.tvDishName);
        tvDishInfo = view.findViewById(R.id.tvDishInfo);
        tvDishPrice = view.findViewById(R.id.tvDishPrice);
        swDishState = view.findViewById(R.id.swDishState);
        ivButton = view.findViewById(R.id.ivButton);
        btUpdate = view.findViewById(R.id.btUpdate);
        toolbarShopDishDetail = view.findViewById(R.id.toolbarShopDishDetail);

        toolbarShopDishDetail.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("dish", dish);
                Navigation.findNavController(view)
                        .navigate(R.id.action_dishDetailFragment_to_dishUpdateFragment, bundle);
            }
        });

        swDishState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showDish();
            }
        });
        showDish();
    }

    @SuppressLint("LongLogTag")
    private void showDish() {
        String url = Url.URL + "/DishServlet";
        int id = dish.getId();
        dishDetail = null;
        int imageSize = getResources().getDisplayMetrics().widthPixels / 2;
        Bitmap bitmap = null;
        try {
            bitmap = new ImageTask(url, id, imageSize).execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivDish.setImageBitmap(bitmap);
        } else {
            ivDish.setImageResource(R.drawable.no_image);
        }
        if (Common.networkConnected(activity)) {
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "getDishById");
            jsonObject.addProperty("id", id);
            String jsonOut = jsonObject.toString();
            CommonTask getShopTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getShopTask.execute().get();
                dishDetail = gson.fromJson(jsonIn, Dish.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }

        account = null;
        if (Common.networkConnected(activity)) {
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "getAccount");
            jsonObject.addProperty("id", id);
            String jsonOut = jsonObject.toString();
            CommonTask getShopTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getShopTask.execute().get();
                account = gson.fromJson(jsonIn, Dish.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }


        //tvDishId.setText(String.valueOf(dishDetail.getId()));
        if(account.getState() == 0){
            tvDishState.setText("未上架");
            tvDishState.setTextColor(Color.RED);
            swDishState.setChecked(false);
        }else if(account.getState() == 1){
            tvDishState.setText("已上架");
            swDishState.setChecked(true);
            tvDishState.setTextColor(Color.BLUE);
        }
        tvDishName.setText(dishDetail.getName());
        tvDishInfo.setText(dishDetail.getInfo());
        tvDishPrice.setText(String.valueOf(dishDetail.getPrice()));
        //設定蓋在Switch上的透明ImageView被按時,判斷Switch狀態並跳出對話框給使用這確認動作
        ivButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (account.getState() == 1) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要將餐點【" + dishDetail.getName() + "】下架 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //店家是有效狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //店家是有效狀態時,對話框按確定的話要做的事情

                                    int id = dishDetail.getId();
                                    byte state = 0;

                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/DishServlet";
                                        account.Account(id, state);
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("dish", new Gson().toJson(account));

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
                                            Common.showToast(activity, R.string.textSuspendedDish);
                                            swDishState.setChecked(false);
                                        }
                                    } else {
                                        Common.showToast(activity, R.string.textNoNetwork);
                                    }

                                }
                            })

                            .show();
                } else if (account.getState() == 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要將餐點 【" + dishDetail.getName() + "】 上架嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //店家是停權狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //店家是停權狀態時,對話框按確定的話要做的事情
                                    int id = dishDetail.getId();
                                    byte state = 1;
                                    account.Account(id, state);
                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/DishServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("dish", new Gson().toJson(account));

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
                                            Common.showToast(activity, R.string.textStartDish);
                                            swDishState.setChecked(true);
                                        }
                                    } else {
                                        Common.showToast(activity, R.string.textNoNetwork);
                                    }

                                }
                            })
                            .show();
                }
            }
        });



    }


}
