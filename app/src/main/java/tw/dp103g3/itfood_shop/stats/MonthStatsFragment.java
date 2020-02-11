package tw.dp103g3.itfood_shop.stats;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.DateTimePickerDialog;


public class MonthStatsFragment extends Fragment {
    private static final String TAG = "TAG_MonthStatsFragment";
    private Activity activity;
    private Button btTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_month_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btTime = view.findViewById(R.id.btTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
        btTime.setText(simpleDateFormat.format(new Date()));
        btTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DateTimePickerDialog dialog = new DateTimePickerDialog(activity, cal.getTimeInMillis());
            dialog.setOnDateTimeSetListener((dialog1, date) -> {
                btTime.setText(simpleDateFormat.format(date));
            });
            dialog.show();
        });
    }
}
