package tw.dp103g3.itfood_shop.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;

import java.util.Calendar;

public class DateTimePickerDialog extends AlertDialog {
    private DateTimePicker dateTimePicker;
    private Calendar date = Calendar.getInstance();
    private OnDateTimeSetListener onDateTimeSetListener;

    public DateTimePickerDialog(Context context, long date) {
        super(context);
        this.date.setTimeInMillis(date);
        dateTimePicker = new DateTimePicker(context, this.date);
        setView(dateTimePicker);
        dateTimePicker.setOnDateTimeChangedListener((view, year, month) -> {
            this.date.set(Calendar.YEAR, year);
            this.date.set(Calendar.MONTH, month);
            updateTitle(this.date.getTimeInMillis());
        });
        setButton(BUTTON_POSITIVE, "設置", this::onClick);
        setButton(BUTTON_NEGATIVE, "取消", (OnClickListener) null);
    }

    public interface OnDateTimeSetListener {
        void onDateTimeSet(AlertDialog dialog, Long date);
    }

    private void updateTitle(long date) {
        int flag = DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE |
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME;
        setTitle(DateUtils.formatDateTime(this.getContext(), date, flag));
    }

    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        onDateTimeSetListener = callBack;
    }

    public void onClick(DialogInterface arg0, int arg1) {
        if (onDateTimeSetListener != null) {
            onDateTimeSetListener.onDateTimeSet(this, date.getTimeInMillis());
        }
    }
}
