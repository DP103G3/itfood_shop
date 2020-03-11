package tw.dp103g3.itfood_shop.stats;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.DateTimePickerDialog;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.order.Order;
import tw.dp103g3.itfood_shop.order.OrderDetail;
import tw.dp103g3.itfood_shop.shop.Dish;
import tw.dp103g3.itfood_shop.task.CommonTask;
import tw.dp103g3.itfood_shop.task.ImageTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class SaleStatsFragment extends Fragment {
    private static final String TAG = "TAG_SaleStatsFragment";
    private Activity activity;
    private ImageView ivBack;
    private RadioGroup rgContainDay;
    private Button btTime;
    private RecyclerView rvDish;
    private List<Dish> dishes;
    private List<Order> orders;
    private CommonTask getDishesTask, getOrdersTask;
    private ImageTask getImageTask;
    private Calendar setDate = Calendar.getInstance();
    private boolean containDay;
    private int dayOfMonth;
    private SimpleDateFormat simpleDateFormat;
    private int shopId;
    private static final String dishIdKey = "dishId";
    private static final String totalKey = "total";
    private static final String amountKey = "amount";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        shopId = Common.getShopId(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        handledViews(view);
        Optional<List<Dish>> optionalShops = getDishes(shopId);
        if (optionalShops.isPresent()) {
            dishes = optionalShops.get();
            if (dishes.size() == 0) {
                Common.showToast(activity, R.string.textNoDishesFound);
                Navigation.findNavController(view).popBackStack();
                return;
            }
        } else {
            Common.showToast(activity, R.string.textServerError);
            Navigation.findNavController(view).popBackStack();
            return;
        }
        ivBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        containDay = true;
        setDate.add(Calendar.DAY_OF_MONTH, -1);
        dayOfMonth = setDate.get(Calendar.DAY_OF_MONTH);
        setDate.set(Calendar.HOUR_OF_DAY, 0);
        setDate.set(Calendar.MINUTE, 0);
        setDate.set(Calendar.SECOND, 0);
        setDate.set(Calendar.MILLISECOND, 0);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        rgContainDay.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDay) {
                containDay = true;
                dayOfMonth = setDate.getActualMaximum(Calendar.DAY_OF_MONTH) < dayOfMonth ?
                        setDate.getActualMaximum(Calendar.DAY_OF_MONTH) : dayOfMonth;
                setDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                btTime.setText(simpleDateFormat.format(setDate.getTime()));
                setOrders();
            } else {
                containDay = false;
                simpleDateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                Calendar judgeDate = Calendar.getInstance();
                judgeDate.add(Calendar.MONTH, -1);
                if (setDate.get(Calendar.MONTH) > judgeDate.get(Calendar.MONTH)) {
                    setDate.add(Calendar.MONTH, -1);
                }
                setDate.set(Calendar.DAY_OF_MONTH, 1);
                btTime.setText(simpleDateFormat.format(setDate.getTime()));
                setOrders();
            }
        });
        btTime.setText(simpleDateFormat.format(setDate.getTime()));
        btTime.setOnClickListener(v -> {
            DateTimePickerDialog dialog = new DateTimePickerDialog(activity, setDate, containDay);
            dialog.setOnDateTimeSetListener((dialog1, date) -> {
                btTime.setText(simpleDateFormat.format(date.getTime()));
                setDate = date;
                if (containDay) {
                    dayOfMonth = setDate.get(Calendar.DAY_OF_MONTH);
                }
                setOrders();
            });
            dialog.show();
        });
        setOrders();
    }

    private void handledViews(View view) {
        ivBack = view.findViewById(R.id.ivBack);
        rgContainDay = view.findViewById(R.id.rgContainDay);
        btTime = view.findViewById(R.id.btTime);
        rvDish = view.findViewById(R.id.rvDish);
    }

    private void setOrders() {
        Optional<List<Order>> optionalOrders = getOrders(shopId);
        optionalOrders.ifPresent(orders -> {
            SaleStatsFragment.this.orders = orders;
            calAndShow();
        });
        orders = optionalOrders.orElseGet(() -> {
            Common.showToast(activity, R.string.textServerError);
            return new ArrayList<>();
        });
    }

    private void calAndShow() {
        List<OrderDetail> orderDetails = new ArrayList<>();
        List<Map<String, Integer>> dishStatses = new ArrayList<>();
        List<Dish> showDishes = new ArrayList<>();
        orders.forEach(v -> orderDetails.addAll(v.getOrderDetails()));
        dishes.forEach(v -> {
            List<OrderDetail> filtedOrderDetails =
                    orderDetails.stream().filter(u -> u.getDish().getId() == v.getId())
                            .collect(Collectors.toList());
            Map<String, Integer> dishStatas = new HashMap<>();
            dishStatas.put(dishIdKey, v.getId());
            int amount = filtedOrderDetails.stream().mapToInt(OrderDetail::getOd_count).sum();
            if (amount != 0) {
                dishStatas.put(amountKey, amount);
                dishStatas.put(totalKey, filtedOrderDetails.stream().mapToInt(OrderDetail::getOd_price).sum());
                dishStatses.add(dishStatas);
                showDishes.add(filtedOrderDetails.get(0).getDish());
            }
        });
        Log.d(TAG, String.valueOf(dishStatses.size()));
        if (dishStatses.size() == 0) {
            Common.showToast(activity, rgContainDay.getCheckedRadioButtonId() == R.id.rbDay ?
                    R.string.textNoOrderFound : R.string.textMonthNoOrderFound);
        }
        rvDish.setLayoutManager(new LinearLayoutManager(activity));
        DishSaleAdapter adapter = (DishSaleAdapter) rvDish.getAdapter();
        if (adapter == null) {
            rvDish.setAdapter(new DishSaleAdapter(activity, dishes, dishStatses));
        } else {
            adapter.setAdapter(dishes, dishStatses);
        }
    }

    private class DishSaleAdapter extends RecyclerView.Adapter<DishSaleAdapter.DishSaleViewHolder> {
            private Context context;
            private List<Dish> dishes;
            private List<Map<String, Integer>> dishStatses;

        DishSaleAdapter(Context context, List<Dish> dishes, List<Map<String, Integer>> dishStatses) {
            this.context = context;
            this.dishes = dishes;
            this.dishStatses = dishStatses;
        }

        void setAdapter(List<Dish> dishes, List<Map<String, Integer>> dishStatses) {
            this.dishes = dishes;
            this.dishStatses = dishStatses;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return dishStatses.size();
        }

        @NonNull
        @Override
        public DishSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_view_dish_stats, parent, false);
            return new DishSaleViewHolder(itemView);
        }

        class DishSaleViewHolder extends RecyclerView.ViewHolder {
            ImageView ivDish;
            TextView tvName, tvInfo, tvPrice, tvTotal, tvAmount;
            DishSaleViewHolder(View itemView) {
                super(itemView);
                ivDish = itemView.findViewById(R.id.ivDish);
                tvName = itemView.findViewById(R.id.tvName);
                tvInfo = itemView.findViewById(R.id.tvInfo);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvTotal = itemView.findViewById(R.id.tvTotal);
                tvAmount = itemView.findViewById(R.id.tvAmount);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull DishSaleViewHolder holder, int position) {
            Map<String, Integer> dishstats = dishStatses.get(position);
            Dish dish = dishes.get(position);

            holder.tvName.setText(dish.getName());
            holder.tvInfo.setText(dish.getInfo());
            holder.tvPrice.setText(String.format(Locale.getDefault(), "$%d", dish.getPrice()));
            holder.tvTotal.setText(String.valueOf(dishstats.getOrDefault(totalKey, 0)));
            holder.tvAmount.setText(String.valueOf(dishstats.getOrDefault(amountKey, 0)));
        }
    }

    private Optional<List<Dish>> getDishes(int shopId) {
        List<Dish> dishes = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/DishServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getDishByShopId");
            jsonObject.addProperty("shop_id", shopId);
            getDishesTask = new CommonTask(url, jsonObject.toString());
            try {
                String dishesJson = getDishesTask.execute().get();
                Type listType = new TypeToken<List<Dish>>(){}.getType();
                dishes = Common.gson.fromJson(dishesJson, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetWork);
            dishes = new ArrayList<>();
        }
        return Optional.ofNullable(dishes);
    }

    private Optional<List<Order>> getOrders(int shopId) {
        List<Order> orders = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/OrderServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findByCase");
            jsonObject.addProperty("id", shopId);
            jsonObject.addProperty("type", "shop");
            jsonObject.addProperty("state", 4);
            jsonObject.addProperty("dateMili", setDate.getTimeInMillis());
            jsonObject.addProperty("containDay", containDay);
            getOrdersTask = new CommonTask(url, jsonObject.toString());
            try {
                String jsonIn = getOrdersTask.execute().get();
                Type listType = new TypeToken<List<Order>>(){}.getType();
                orders = Common.gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetWork);
            orders = new ArrayList<>();
        }
        return Optional.ofNullable(orders);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getDishesTask != null) {
            getDishesTask.cancel(true);
            getDishesTask = null;
        }
        if (getOrdersTask != null) {
            getOrdersTask.cancel(true);
            getOrdersTask = null;
        }
        if (getImageTask != null) {
            getImageTask.cancel(true);
            getImageTask = null;
        }
    }
}
