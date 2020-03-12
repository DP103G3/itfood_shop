package tw.dp103g3.itfood_shop.order;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.shop.Dish;
import tw.dp103g3.itfood_shop.shop.Shop;
import tw.dp103g3.itfood_shop.task.CommonTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class OrderFragment extends Fragment {
    private static final String TAG = "TAG_OrderFragment";
    private Activity activity;
    private RecyclerView rvOrder;
    private Common.Tab tab;
    private CommonTask editOrderTask, getOrderTask;
    private List<Order> sortedOrders;
    private LinearLayout llNoItem;
    private Comparator<Order> cmp;
    private LocalBroadcastManager broadcastManager;

    OrderFragment(Common.Tab tab) {
        this.tab = tab;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        broadcastManager = LocalBroadcastManager.getInstance(activity);
        registerOrderReceiver();
        llNoItem = view.findViewById(R.id.llNoItem);
        rvOrder = view.findViewById(R.id.rvOrder);
    }

    @Override
    public void onResume() {
        super.onResume();
        cmp = Comparator.<Order, Long>comparing(v -> v.getOrder_time()
                .getTime()).reversed();
        sortedOrders = MainOrderFragment.getOrders().stream().filter(v -> v.getOrder_state() == tab.ordinal())
                .sorted(cmp).collect(Collectors.toList());
        if (sortedOrders.isEmpty()) {
            llNoItem.setVisibility(View.VISIBLE);
        } else {
            llNoItem.setVisibility(View.GONE);
        }
        rvOrder.setLayoutManager(new LinearLayoutManager(activity));
        showOrders(sortedOrders);
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        if (nextAnim != 0X0) {
            Animator animator = AnimatorInflater.loadAnimator(activity, nextAnim);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (enter) {
                        showOrders(sortedOrders);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            return animator;
        }
        return null;
    }

    private void showOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            if (!Common.networkConnected(activity)) {
                Common.showToast(activity, R.string.textNoNetWork);
            }
        }
        OrderAdapter orderAdapter = (OrderAdapter) rvOrder.getAdapter();
        if (orderAdapter == null) {
            rvOrder.setAdapter(new OrderAdapter(activity, orders));
        } else {
            orderAdapter.setOrders(orders);
            orderAdapter.notifyDataSetChanged();
        }
    }

    private void registerOrderReceiver() {
        IntentFilter orderFilter = new IntentFilter("order");
        IntentFilter deliveryFilter = new IntentFilter("delivery");
        broadcastManager.registerReceiver(orderReceiver, orderFilter);
        broadcastManager.registerReceiver(deliveryReceiver, deliveryFilter);
    }

    private BroadcastReceiver orderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Order order = Common.gson.fromJson(message, Order.class);
            Set<Order> orders = MainOrderFragment.getOrders();
            orders.remove(order);
            orders.add(order);
            MainOrderFragment.setOrders(orders);
            onResume();
            Log.d(TAG, message);
        }
    };

    private BroadcastReceiver deliveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Type orderSetType = new TypeToken<Set<Order>>(){}.getType();
            Set<Order> oldOrders = MainOrderFragment.getOrders();
            Set<Order> newOrders = Common.gson.fromJson(message, orderSetType);
            for (Order order : newOrders) {
                oldOrders.remove(order);
                oldOrders.add(order);
            }
            MainOrderFragment.setOrders(oldOrders);
            onResume();
            Log.d(TAG, message);
        }
    };

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder>{
        private Context context;
        private List<Order> orders;
        DecimalFormatSymbols symbols;
        DecimalFormat decimalFormat;

        OrderAdapter(Context context, List<Order> orders) {
            this.context = context;
            this.orders = orders;
            symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            decimalFormat = new DecimalFormat("$ ###,###,###,###", symbols);
        }

        void setOrders(List<Order> orders) {
            this.orders = orders;
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        private class OrderViewHolder extends RecyclerView.ViewHolder{
            TextView tvMemberName, tvMemberPhone, tvType, tvTotal;
            Button btAction, btCancel;
            RecyclerView rvOrderDetail;
            View divider6, divider7;
            Order order;

            OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMemberName = itemView.findViewById(R.id.tvMemberName);
                tvMemberPhone = itemView.findViewById(R.id.tvMemberPhone);
                tvType = itemView.findViewById(R.id.tvType);
                tvTotal = itemView.findViewById(R.id.tvTotal);
                rvOrderDetail = itemView.findViewById(R.id.rvOrderDetail);
                divider6 = itemView.findViewById(R.id.divider6);
                divider7 = itemView.findViewById(R.id.divider7);
                btAction = itemView.findViewById(R.id.btAction);
                btCancel = itemView.findViewById(R.id.btCancel);
                btAction.setOnClickListener(this::onClick);
                btCancel.setOnClickListener(this::onClick);
            }

            private void setOrder(Order order) {
                this.order = order;
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

            private void onClick(View view) {
                String url = Url.URL + "/OrderServlet";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "orderUpdate");
                if (view.getId() == R.id.btCancel) {
                    order.setOrder_state(5);
                } else {
                    order.setOrder_state(order.getOrder_state() + 1);
                }
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
                    Set<Order> orders = MainOrderFragment.getOrders();
                    orders.remove(order);
                    orders.add(order);
                    MainOrderFragment.setOrders(orders);
                    Common.OrderType orderType = Common.OrderType.values()[order.getOrder_type()];
                    OrderMessage orderMessageMem = new OrderMessage(order, "mem" + order.getMem_id());
                    switch (orderType) {
                        case DELIVERY:
                            DeliveryMessage deliveryMessage = null;
                            if (order.getOrder_state() == 1) {
                                 deliveryMessage = new DeliveryMessage("shopPublishOrder"
                                         , order, order.getOrder_area()
                                         , "shop" + order.getShop().getId()
                                         , "");
                            } else if (order.getOrder_state() == 2) {
                                deliveryMessage =  new DeliveryMessage("shopDishDone"
                                        , order, order.getOrder_area()
                                        , "shop" + order.getShop().getId()
                                        , "del" + order.getDel_id());
                            }
                            String delMessage = Common.gson.toJson(deliveryMessage, DeliveryMessage.class);
                            Common.deliveryOrderWebSocketClient.send(delMessage);
                        case SELFPICK:
                            String memMessage = Common.gson.toJson(orderMessageMem);
                            Common.orderWebSocketClient.send(memMessage);
                            break;
                    }
                    onResume();
                }
            }
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.order_item_view, parent, false);
            return new OrderViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            final Order order = orders.get(position);
            Shop shop = order.getShop();
            List<OrderDetail> orderDetails = order.getOrderDetails();
            int orderTtprice = order.getOrder_ttprice();
            Common.OrderState orderState = Common.OrderState.values()[order.getOrder_state()];
            Common.OrderType orderType = Common.OrderType.values()[order.getOrder_type()];
            String[] textOrderType = getResources().getStringArray(R.array.textOrderType);
            String textOrderTtprice = decimalFormat.format(orderTtprice);
            int del_id = order.getDel_id();
            switch (orderState) {
                case UNCONFIRMED:
                    holder.divider6.setVisibility(View.VISIBLE);
                    holder.divider7.setVisibility(View.VISIBLE);
                    holder.btAction.setVisibility(View.VISIBLE);
                    holder.btAction.setText(getString(R.string.textConfirmed));
                    holder.btCancel.setVisibility(View.VISIBLE);
                    holder.btCancel.setText(getString(R.string.textCancel));
                    break;
                case MAKING:
                    holder.divider6.setVisibility(View.VISIBLE);
                    holder.divider7.setVisibility(View.VISIBLE);
                    holder.btAction.setVisibility(View.VISIBLE);
                    holder.btAction.setText("製作完成");
                    holder.btCancel.setVisibility(View.GONE);
                    if ((del_id == -1) && order.getOrder_type() != 0) {
                        holder.btAction.setEnabled(false);
                        holder.btAction.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBackground, activity.getTheme())));
                    } else {
                        holder.btAction.setEnabled(true);
                        holder.btAction.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, activity.getTheme())));
                    }
                    break;
                case PICKUP:
                    holder.divider6.setVisibility(View.GONE);
                    holder.divider7.setVisibility(View.GONE);
                    holder.btAction.setVisibility(View.GONE);
                    holder.btCancel.setVisibility(View.GONE);
            }
            holder.tvMemberName.setText(order.getOrder_name());
            holder.tvMemberPhone.setText(order.getOrder_phone());
            holder.tvType.setText(textOrderType[orderType.ordinal()]);
            holder.tvTotal.setText(textOrderTtprice);
            holder.rvOrderDetail.setLayoutManager(new LinearLayoutManager(context));
            holder.rvOrderDetail.setAdapter(new OrderDetailAdapter(context,
                    orderDetails == null ? new ArrayList<>() : orderDetails));
            holder.setOrder(order);
        }

        private class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
            private Context context;
            private List<OrderDetail> orderDetails;

            OrderDetailAdapter(Context context, List<OrderDetail> orderDetails) {
                this.context = context;
                this.orderDetails = orderDetails;
            }

            @Override
            public int getItemCount() {
                return orderDetails.size() + 1;
            }

            private class OrderDetailViewHolder extends RecyclerView.ViewHolder {
                TextView tvDishName, tvDishInfo, tvDishPrice, tvDishCount;
                OrderDetailViewHolder(@NonNull View itemView) {
                    super(itemView);
                    tvDishName = itemView.findViewById(R.id.tvDishName);
                    tvDishInfo = itemView.findViewById(R.id.tvDishInfo);
                    tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
                    tvDishCount = itemView.findViewById(R.id.tvDishCount);
                }
            }

            @NonNull
            @Override
            public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.order_detail_item_view,
                        parent, false);
                return new OrderDetailViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
                if (position != orderDetails.size()) {
                    final OrderDetail orderDetail = orderDetails.get(position);
                    Dish dish = orderDetail.getDish();
                    int price = orderDetail.getOd_price();
                    String textDishCount = "x" + orderDetail.getOd_count();
                    holder.tvDishName.setText(dish.getName());
                    holder.tvDishInfo.setText((dish.getInfo() == null || dish.getInfo().isEmpty() ?
                            "" : dish.getInfo()));
                    holder.tvDishCount.setText(textDishCount);
                    holder.tvDishPrice.setText(decimalFormat.format(price));
                } else {
                    holder.tvDishName.setText("外送費");
                    holder.tvDishPrice.setText(decimalFormat.format(70));
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (editOrderTask != null) {
            editOrderTask.cancel(true);
            editOrderTask = null;
        }
        if (getOrderTask != null) {
            getOrderTask.cancel(true);
            getOrderTask = null;
        }
    }
}
