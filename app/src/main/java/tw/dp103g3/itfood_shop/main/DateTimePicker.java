package tw.dp103g3.itfood_shop.main;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import java.util.Calendar;

import tw.dp103g3.itfood_shop.R;

public class DateTimePicker extends FrameLayout {
    private NumberPicker yearSpinner;
    private NumberPicker monthSpinner;
    private Calendar date;
    private int year, month, oYear, oMonth;
    private OnDateTimeChangedListener onDateTimeChangedListener;

    public DateTimePicker(Context context, Calendar date) {
        super(context);
        this.date = date;
        year = date.get(Calendar.YEAR);
        month = date.get(Calendar.MONTH);

        inflate(context, R.layout.date_time_piker, this);

        yearSpinner = this.findViewById(R.id.npYear);
        initYear();
        yearSpinner.setWrapSelectorWheel(false);
        yearSpinner.dispatchSetSelected(false);
        yearSpinner.setOnValueChangedListener(onYearChangedLintener);

        monthSpinner = this.findViewById(R.id.npMonth);
        initMonth();
        monthSpinner.dispatchSetSelected(false);
        monthSpinner.setOnValueChangedListener(onMonthChangedLintener);
        oYear = yearSpinner.getValue();
        oMonth = monthSpinner.getValue();
        onDateTimeChanged();
    }

    private NumberPicker.OnValueChangeListener onYearChangedLintener = (picker, oldVal, newVal) -> {
        oYear = yearSpinner.getValue();
        initMonth();
        onDateTimeChanged();
    };

    private NumberPicker.OnValueChangeListener onMonthChangedLintener = (picker, oldVal, newVal) -> {
        oMonth = monthSpinner.getValue();
        onDateTimeChanged();
    };

    private void initYear() {
        int yearMaxVal = date.get(Calendar.YEAR);
        yearSpinner.setMaxValue(yearMaxVal);
        yearSpinner.setMinValue(2000);
        yearSpinner.setValue(yearMaxVal);
        oYear = yearSpinner.getValue();
    }

    private void initMonth() {
        int monthMaxVal = yearSpinner.getValue() == year ? month : 11;
        monthSpinner.setMaxValue(monthMaxVal);
        monthSpinner.setMinValue(0);
        monthSpinner.setFormatter(value -> String.valueOf(value + 1));
        monthSpinner.setValue(yearSpinner.getValue() == year ? month : 0);
        oMonth = monthSpinner.getValue();
    }

    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(DateTimePicker view, int year, int month);
    }

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callBack) {
        onDateTimeChangedListener = callBack;
    }

    private void onDateTimeChanged() {
        if (onDateTimeChangedListener != null) {
            onDateTimeChangedListener.onDateTimeChanged(this, oYear,
                    oMonth);
        }
    }

}
