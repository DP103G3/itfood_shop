package tw.dp103g3.itfood_shop;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.task.CommonTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = "TAG_LoginFragment";
    private static final String reEmail = "^\\w+((-\\w+)|(.\\w+))*@[A-Za-z0-9]+((\\.|\\-)[A-Za-z0-9]+)*\\.[A-Za-z]+$";
    private Activity activity;
    private BottomNavigationView bottomNavigationView;
    private EditText etEmail, etPassword;
    private Button btLogin;
    private String textEmail, textPassword;
    private CommonTask loginTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        handledViews(view);
        bottomNavigationView.setVisibility(View.GONE);
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                textEmail = etEmail.getText().toString().trim().toLowerCase();
                etEmail.setText(textEmail);
                if (textEmail.isEmpty()) {
                    etEmail.setError(getString(R.string.textInputEmail));
                } else if (!textEmail.matches(reEmail)) {
                    etEmail.setError(getString(R.string.textEmailFormateError));
                }
            }
        });
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            textPassword = etPassword.getText().toString().trim();
            if (!hasFocus) {
                if (textPassword.isEmpty()) {
                    etPassword.setError(getString(R.string.textInputPassword));
                }
            }
        });
        btLogin.setOnClickListener(this::onClick);
    }

    private void handledViews(View view) {
        bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btLogin = view.findViewById(R.id.btLogin);
    }

    private void onClick(View view) {
        String url = Url.URL + "/ShopServlet";
        textEmail = etEmail.getText().toString().trim().toLowerCase();
        textPassword = etPassword.getText().toString().trim();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "login");
        jsonObject.addProperty("email", textEmail);
        jsonObject.addProperty("password", textPassword);
        loginTask = new CommonTask(url, jsonObject.toString());
        int shopId = 0;
        try {
            String result = loginTask.execute().get();
            shopId = Integer.parseInt(result);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (shopId != 0) {
            SharedPreferences pref =
                    activity.getSharedPreferences(Common.PREFERENCES_SHOP, Context.MODE_PRIVATE);
            pref.edit().putString("email", textEmail)
                    .putString("password", textPassword).putInt("shopId", shopId).apply();
            Navigation.findNavController(view).popBackStack();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        bottomNavigationView.setVisibility(View.VISIBLE);
        if (loginTask != null) {
            loginTask.cancel(true);
            loginTask = null;
        }
    }
}
