package tw.dp103g3.itfood_shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tw.dp103g3.itfood_shop.shop.Shop;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG_MainActivity";
    private BottomNavigationView bottomNavigationView;
    private static Shop shop;
    private File shopFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        shopFile = new File(this.getFilesDir(), "shop");
        if (!shopFile.exists()) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(shopFile));
                out.writeObject(new Shop(0, null, null, -1, -1,
                        0, (byte) 0, null, null, 0, 0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(shopFile));
            shop = (Shop) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public static Shop getShop() {
        return shop;
    }
}
