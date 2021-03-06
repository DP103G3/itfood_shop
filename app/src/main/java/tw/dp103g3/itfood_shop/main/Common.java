package tw.dp103g3.itfood_shop.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import tw.dp103g3.itfood_shop.order.DeliveryOrderWebSocketClient;
import tw.dp103g3.itfood_shop.order.OrderWebSocketClient;
import tw.dp103g3.itfood_shop.person.LoginDialogFragment;

public class Common {

    public enum Tab {
        UNCONFIRMED, UNCOMPLETE, PICKUP
    }

    public enum OrderState {
        UNCONFIRMED, MAKING, PICKUP, DELIVERING, DONE, CANCEL
    }

    public enum OrderType {
        SELFPICK, DELIVERY
    }

    public static final String TAG = "TAG_Common";
    public static final String PREFERENCES_SHOP = "shop";
    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    public static OrderWebSocketClient orderWebSocketClient;
    public static DeliveryOrderWebSocketClient deliveryOrderWebSocketClient;
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static void connectOrderServer(Context context, int shopId) {
        URI uri = null;
        try {
            uri = new URI(Url.ORDER_SOCKET_URI + shopId);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.toString());
        }
        if (orderWebSocketClient == null) {
            orderWebSocketClient = new OrderWebSocketClient(uri, context);
            orderWebSocketClient.connect();
        }
    }

    public static void disconnectOrderServer() {
        if (orderWebSocketClient != null) {
            orderWebSocketClient.close();
            orderWebSocketClient = null;
        }
    }

    public static void connectDeliveryServer(Context context, int shopId) {
        URI uri = null;
        try {
            uri = new URI(Url.DELIVERY_SOCKET_URI + shopId);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.toString());
        }
        if (deliveryOrderWebSocketClient == null) {
            deliveryOrderWebSocketClient = new DeliveryOrderWebSocketClient(uri, context);
            deliveryOrderWebSocketClient.connect();
        }
    }

    public static void disconnectDeliveryServer() {
        if (deliveryOrderWebSocketClient != null) {
            deliveryOrderWebSocketClient.close();
            deliveryOrderWebSocketClient = null;
        }
    }

    public static int getShopId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_SHOP, Context.MODE_PRIVATE);
        return pref.getInt("shopId", 0);
    }

    public static boolean networkConnected(Activity activity) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities actNetWork =
                connectivityManager.getNetworkCapabilities(network);
        if (actNetWork != null) {
            return actNetWork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNetWork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        } else {
            return false;
        }
    }

    public static void showLoginDialog(Fragment fragment) {
        LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.setTargetFragment(fragment, 0);
        loginDialogFragment.show(fragment.getParentFragmentManager(), "LoginDialogFragment");
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }

    public static void setDialogUi(Dialog dialog, Activity activity) {
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility());
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                wm.updateViewLayout(dialog.getWindow().getDecorView(), dialog.getWindow().getAttributes());
            }
        });
    }
}
