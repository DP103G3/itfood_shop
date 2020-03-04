package tw.dp103g3.itfood_shop.order;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.shop.Shop;
import tw.dp103g3.itfood_shop.task.CommonTask;

public class MainOrderFragment extends Fragment {
    private static final String TAG = "TAG_MainOrderFragment";
    private FragmentActivity activity;
    private Shop shop;
    private NavController navController;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private static int shopId;
    private static Set<Order> orders;
    private CommonTask getOrderTask, editOrderTask, loginTask;
    private ImageButton ibScanQRCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Common.connectOrderServer(activity, Common.getShopId(activity));
        Common.connectDeliveryServer(activity, Common.getShopId(activity));
//        broadcastManager = LocalBroadcastManager.getInstance(activity);
//        registerOrderReceiver();
        navController = Navigation.findNavController(view);
        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCES_SHOP, Context.MODE_PRIVATE);
        String email = pref.getString("email", null);
        String password = pref.getString("password", null);
        if (email != null && password != null) {
            String url = Url.URL + "/ShopServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "login");
            jsonObject.addProperty("email", email);
            jsonObject.addProperty("password", password);
            loginTask = new CommonTask(url, jsonObject.toString());
            shopId = 0;
            try {
                String result = loginTask.execute().get();
                shopId = Integer.parseInt(result);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (shopId == 0) {
                navController.navigate(R.id.action_mainOrderFragment_to_loginFragment);
                return;
            }
        } else {
            navController.navigate(R.id.action_mainOrderFragment_to_loginFragment);
            return;
        }
        ibScanQRCode = view.findViewById(R.id.ibScanQRCode);
        ibScanQRCode.setOnClickListener(this::scanQRCode);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager2 = view.findViewById(R.id.viewPager);
        orders = getOrders(shopId);
        viewPager2.setAdapter(new ViewPagerAdapter(activity));
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(getResources().getTextArray(R.array.textTab)[position])).attach();
        viewPager2.setOffscreenPageLimit(1);
    }

    public void resumeViewAdapter() {
        viewPager2.setAdapter(new ViewPagerAdapter(activity));
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(getResources().getTextArray(R.array.textTab)[position])).attach();
        viewPager2.setOffscreenPageLimit(1);
    }

    static void setOrders(Set<Order> orders) {
        MainOrderFragment.orders = orders;
    }

    static Set<Order> getOrders() {
        return MainOrderFragment.orders;
    }

    private Set<Order> getOrders(int shopId) {
        Set<Order> orders = new HashSet<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/OrderServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findByCase");
            jsonObject.addProperty("type", "shop");
            jsonObject.addProperty("id", shopId);
            getOrderTask = new CommonTask(url, jsonObject.toString());
            try {
                String jsonIn = getOrderTask.execute().get();
                Type listType = new TypeToken<Set<Order>>(){}.getType();
                orders = Common.gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetWork);
        }
        return orders;
    }

//    private void registerOrderReceiver() {
//        IntentFilter orderFilter = new IntentFilter("order");
//        broadcastManager.registerReceiver(orderReceiver, orderFilter);
//    }
//
//    private BroadcastReceiver orderReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String message = intent.getStringExtra("message");
//            Order order = Common.gson.fromJson(message, Order.class);
//            Set<Order> orders = MainOrderFragment.getOrders();
//            orders.remove(order);
//            orders.add(order);
//            MainOrderFragment.setOrders(orders);
//            viewPager2.getAdapter().notifyDataSetChanged();
//            Log.d(TAG, message);
//        }
//    };

    private class ViewPagerAdapter extends FragmentStateAdapter {

        ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public int getItemCount() {
            return Common.Tab.values().length;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new OrderFragment(Common.Tab.values()[position]);
        }
    }

    private void scanQRCode(View view) {
        if (orders.stream().anyMatch(v -> v.getOrder_state() == 2)) {
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(MainOrderFragment.this);
            integrator.setBarcodeImageEnabled(true);
            integrator.setBeepEnabled(false);
            integrator.setCameraId(0);
            integrator.setOrientationLocked(false);
            integrator.setPrompt(getString(R.string.textPickHint));
            integrator.initiateScan();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator
                .parseActivityResult(requestCode, resultCode, data);
        if (Common.networkConnected(activity) && intentResult != null && intentResult.getContents() != null) {
            String orderStr = intentResult.getContents();
            Order order = Common.gson.fromJson(orderStr, Order.class);
            if (order.getOrder_type() == 0) {
                order.setOrder_state(4);
                order.setOrder_delivery(new Date());
            } else {
                order.setOrder_state(3);
            }
            String url = Url.URL + "/OrderServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "orderUpdate");
            jsonObject.addProperty("order", Common.gson.toJson(order));
            editOrderTask = new CommonTask(url, jsonObject.toString());
            int count = 0;
            try {
                String result = editOrderTask.execute().get();
                count = Integer.parseInt(result);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (count == 1) {
                orders.remove(order);
                orders.add(order);
                orders.forEach(v -> Log.d(TAG, v.toString()));
                Common.OrderType orderType = Common.OrderType.values()[order.getOrder_type()];
                OrderMessage orderMessageMem = new OrderMessage(order, "mem" + order.getMem_id());
                OrderMessage orderMessageDel = new OrderMessage(order, "del" + order.getDel_id());
                switch (orderType) {
                    case DELIVERY:
                        String delMessage = Common.gson.toJson(orderMessageDel);
                        Common.orderWebSocketClient.send(delMessage);
                    case SELFPICK:
                        String memMessage = Common.gson.toJson(orderMessageMem);
                        Common.orderWebSocketClient.send(memMessage);
                        break;
                }
                Common.showToast(activity, R.string.textPickSuccess);
            } else {
                Common.showToast(activity, R.string.textPickFail);
            }
        } else if (!Common.networkConnected(activity)) {
            Common.showToast(activity, R.string.textNoNetWork);
        }
        resumeViewAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
//        broadcastManager.unregisterReceiver(orderReceiver);
        if (getOrderTask != null) {
            getOrderTask.cancel(true);
            getOrderTask = null;
        }
        if (editOrderTask != null) {
            editOrderTask.cancel(true);
            editOrderTask = null;
        }
        if (loginTask != null) {
            loginTask.cancel(true);
            loginTask = null;
        }
        if (viewPager2 != null && viewPager2.getAdapter() != null) {
            viewPager2.setAdapter(null);
        }
        Common.disconnectDeliveryServer();
    }
}
