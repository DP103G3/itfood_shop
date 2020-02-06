package tw.dp103g3.itfood_shop.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import tw.dp103g3.itfood_shop.order.OrderWebSocketClient;

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

    public static void connectServer(Context context, int shopId) {
        URI uri = null;
        try {
            uri = new URI(Url.SOCKET_URI + shopId);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.toString());
        }
        if (orderWebSocketClient == null) {
            orderWebSocketClient = new OrderWebSocketClient(uri, context);
            orderWebSocketClient.connect();
        }
    }

    public static void disconnectServer() {
        if (orderWebSocketClient != null) {
            orderWebSocketClient.close();
            orderWebSocketClient = null;
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

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }
}
