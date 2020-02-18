package tw.dp103g3.itfood_shop.main;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import java.util.Calendar;

public class DateTimePickerDialog extends AlertDialog {
    private DateTimePicker dateTimePicker;
    private Calendar setDate;
    private OnDateTimeSetListener onDateTimeSetListener;

    public DateTimePickerDialog(Context context, Calendar setDate, boolean containDay) {
        super(context);
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(containDay ? Calendar.DAY_OF_MONTH : Calendar.MONTH, -1);
        this.setDate = setDate;
        dateTimePicker = new DateTimePicker(context, maxDate, setDate, containDay);
        setView(dateTimePicker);
        dateTimePicker.setOnDateTimeChangedListener((view, year, month, day) -> {
            this.setDate.set(Calendar.YEAR, year);
            this.setDate.set(Calendar.MONTH, month);
            this.setDate.set(Calendar.DAY_OF_MONTH, day);
            this.setDate.set(Calendar.HOUR_OF_DAY, 0);
            this.setDate.set(Calendar.MINUTE, 0);
            this.setDate.set(Calendar.SECOND, 0);
            this.setDate.set(Calendar.MILLISECOND, 0);
        });
        setButton(BUTTON_POSITIVE, "設置", this::onClick);
        setButton(BUTTON_NEGATIVE, "取消", (OnClickListener) null);
    }

    public interface OnDateTimeSetListener {
        void onDateTimeSet(AlertDialog dialog, Calendar date);
    }

    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        onDateTimeSetListener = callBack;
    }

    public void onClick(DialogInterface arg0, int arg1) {
        if (onDateTimeSetListener != null) {
            onDateTimeSetListener.onDateTimeSet(this, setDate);
        }
    }

    private void fullScreenImmersive(View view) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }
}
