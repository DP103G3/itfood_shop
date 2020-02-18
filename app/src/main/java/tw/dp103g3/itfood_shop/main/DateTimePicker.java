package tw.dp103g3.itfood_shop.main;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Calendar;

import tw.dp103g3.itfood_shop.R;

public class DateTimePicker extends FrameLayout {
    private NumberPicker yearSpinner;
    private NumberPicker monthSpinner;
    private NumberPicker daySpinner;
    private TextView tvDay;
    private Calendar date;
    private int year, month , day, oYear, oMonth, oDay;
    private OnDateTimeChangedListener onDateTimeChangedListener;
    private boolean containDay;

    public DateTimePicker(Context context, Calendar maxDate, Calendar setDate, boolean containDay) {
        super(context);
        this.containDay = containDay;
        this.date = maxDate;
        year = maxDate.get(Calendar.YEAR);
        oYear = setDate.get(Calendar.YEAR);
        month = maxDate.get(Calendar.MONTH);
        oMonth = setDate.get(Calendar.MONTH);
        oDay = containDay ? setDate.get(Calendar.DAY_OF_MONTH) : 1;

        inflate(context, R.layout.date_time_piker, this);

        yearSpinner = this.findViewById(R.id.npYear);
        initYear();
        yearSpinner.setWrapSelectorWheel(false);
        yearSpinner.dispatchSetSelected(false);
        yearSpinner.setOnValueChangedListener(onYearChangedLintener);
        oYear = yearSpinner.getValue();

        monthSpinner = this.findViewById(R.id.npMonth);
        initMonth();
        monthSpinner.setWrapSelectorWheel(false);
        monthSpinner.dispatchSetSelected(false);
        monthSpinner.setOnValueChangedListener(onMonthChangedLintener);
        oMonth = monthSpinner.getValue();

        if (containDay) {
            day = date.get(Calendar.DAY_OF_MONTH);
            tvDay = this.findViewById(R.id.tvDay);
            daySpinner = this.findViewById(R.id.npDay);
            tvDay.setVisibility(VISIBLE);
            daySpinner.setVisibility(VISIBLE);
            initDay();
            daySpinner.setWrapSelectorWheel(false);
            daySpinner.dispatchSetSelected(false);
            daySpinner.setOnValueChangedListener(onDayChangedLintener);
            oDay = daySpinner.getValue();
        }
    }

    private NumberPicker.OnValueChangeListener onYearChangedLintener = (picker, oldVal, newVal) -> {
        oYear = yearSpinner.getValue();
        initMonth();
        if (containDay) {
            initDay();
        }
        onDateTimeChanged();
    };

    private NumberPicker.OnValueChangeListener onMonthChangedLintener = (picker, oldVal, newVal) -> {
        oMonth = monthSpinner.getValue();
        if (containDay) {
            initDay();
        }
        onDateTimeChanged();
    };

    private NumberPicker.OnValueChangeListener onDayChangedLintener = (picker, oldVal, newVal) -> {
        oDay = daySpinner.getValue();
        onDateTimeChanged();
    };

    private void initYear() {
        yearSpinner.setMaxValue(year);
        yearSpinner.setMinValue(2010);
        yearSpinner.setValue(oYear);
        oYear = yearSpinner.getValue();
    }

    private void initMonth() {
        int monthMaxVal = yearSpinner.getValue() == year ? month : 11;
        monthSpinner.setMaxValue(monthMaxVal);
        monthSpinner.setMinValue(0);
        monthSpinner.setFormatter(value -> String.valueOf(value + 1));
        monthSpinner.setValue(oMonth);
        oMonth = monthSpinner.getValue();
    }

    private void initDay() {
        int dayMaxVal = oYear == year && oMonth == month ? day : maxDayOfMonth(oYear, oMonth);
        daySpinner.setMaxValue(dayMaxVal);
        daySpinner.setMinValue(1);
        daySpinner.setValue(oDay);
        oDay = daySpinner.getValue();
    }

    private int maxDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(DateTimePicker view, int year, int month, int day);
    }

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callBack) {
        onDateTimeChangedListener = callBack;
    }

    private void onDateTimeChanged() {
        if (onDateTimeChangedListener != null) {
            onDateTimeChangedListener.onDateTimeChanged(this, oYear,
                    oMonth, oDay);
        }
    }

}
