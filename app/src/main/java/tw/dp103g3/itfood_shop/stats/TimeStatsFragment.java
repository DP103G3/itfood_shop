package tw.dp103g3.itfood_shop.stats;


import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.DateTimePickerDialog;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.order.Order;
import tw.dp103g3.itfood_shop.task.CommonTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimeStatsFragment extends Fragment {
    private static final String TAG = "TAG_TimeStatsFragment";
    private Activity activity;
    private Button btTime;
    private ImageButton ibBack;
    private NavController navController;
    private Calendar setDate = Calendar.getInstance();
    private CommonTask getOrdersTask;
    private List<Order> orders;
    private int shopId;
    private TextView tvTitle, tvTotal, tvAmount, tvAverage;
    private BarChart barChart;
    private RadioGroup radioGroup;
    private int checkedId = R.id.rbTotal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        shopId = Common.getShopId(activity);
        navController = Navigation.findNavController(view);
        tvTitle = view.findViewById(R.id.tvTitle);
        ibBack = view.findViewById(R.id.ibBack);
        btTime = view.findViewById(R.id.btTime);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvAmount = view.findViewById(R.id.tvAmount);
        tvAverage = view.findViewById(R.id.tvAverage);
        barChart = view.findViewById(R.id.barChart);
        radioGroup = view.findViewById(R.id.radioGroup);

        tvTitle.setText(getResources().getTextArray(R.array.textStats)[1]);
        ibBack.setOnClickListener(v -> navController.popBackStack());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        setDate.add(Calendar.DAY_OF_MONTH, -1);
        setDate.set(Calendar.HOUR_OF_DAY, 0);
        setDate.set(Calendar.MINUTE, 0);
        setDate.set(Calendar.SECOND, 0);
        setDate.set(Calendar.MILLISECOND, 0);
        btTime.setText(simpleDateFormat.format(setDate.getTime()));
        btTime.setOnClickListener(v -> {
            DateTimePickerDialog dialog = new DateTimePickerDialog(activity, setDate, true);
            dialog.setOnDateTimeSetListener((dialog1, date) -> {
                btTime.setText(simpleDateFormat.format(date.getTime()));
                setDate = date;
                orders = getOrders(shopId);
                calAndShow();
            });
            dialog.show();
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            TimeStatsFragment.this.checkedId = checkedId;
            calAndShow();
        });

        Description description = new Description();
        description.setText("");
        description.setTextSize(20);
        description.setTextColor(getResources().getColor(R.color.colorTextOnP, activity.getTheme()));
        barChart.setDescription(description);
        orders = getOrders(shopId);
        calAndShow();
    }

    private void calAndShow() {
        if (orders.size() == 0) {
            barChart.setVisibility(View.GONE);
            Common.showToast(activity, R.string.textNoOrderFound);
            return;
        }
        barChart.setVisibility(View.VISIBLE);
        int total = orders.stream().mapToInt(Order::getOrder_ttprice).sum();
        int amount = orders.size();
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        };
        tvTotal.setText(String.valueOf(total));
        tvAmount.setText(String.valueOf(amount));
        tvAverage.setText(amount == 0 ? "--" : String.format(Locale.getDefault(),
                "%.2f",(float) total / amount));

        List<BarEntry> entries = getEntries();

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(entries.size() + 1);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setAxisMaximum((
                float) (entries.stream().mapToDouble(BarEntry::getY).max().orElse(0) * 1.2));
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setValueFormatter(formatter);

        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);

        BarDataSet barDataSet = new BarDataSet(entries, getString(R.string.textTotal));
        barDataSet.setColor(getResources().getColor(R.color.colorSecondaryLight, activity.getTheme()));
        barDataSet.setValueTextColor(getResources().getColor(R.color.colorTextOnP, activity.getTheme()));
        barDataSet.setValueTextSize(10);
        barDataSet.setValueFormatter(formatter);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.animateX(500, Easing.Linear);
        barChart.animateY(1600, Easing.Linear);
        barChart.invalidate();
    }

    private List<BarEntry> getEntries() {
        List<BarEntry> entries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(setDate.getTimeInMillis());
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int index = 1;
        while (calendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
            long minTime = calendar.getTimeInMillis();
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            long maxTime = calendar.getTimeInMillis();
            IntStream intStream = orders.stream().filter(v ->
                    v.getOrder_delivery().getTime() >= minTime && v.getOrder_delivery().getTime() < maxTime)
                    .mapToInt(Order::getOrder_ttprice);
            int yValue;
            if (checkedId == R.id.rbTotal) {
                yValue = intStream.sum();
            } else {
                yValue = (int) intStream.count();
            }
            entries.add(new BarEntry(index, yValue));
            index++;
        }
        entries.forEach(v -> Log.d(TAG, String.format("%f, %f", v.getX(), v.getY())));
        return entries;
    }

    private List<Order> getOrders(int shopId) {
        List<Order> orders = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/OrderServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findByCase");
            jsonObject.addProperty("id", shopId);
            jsonObject.addProperty("type", "shop");
            jsonObject.addProperty("state", 4);
            jsonObject.addProperty("dateMili", setDate.getTimeInMillis());
            jsonObject.addProperty("containDay", true);
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
        }
        return orders;
    }
}
