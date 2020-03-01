package tw.dp103g3.itfood_shop.dish;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.shop.Dish;
import tw.dp103g3.itfood_shop.task.CommonTask;
import tw.dp103g3.itfood_shop.task.ImageTask;

import static android.app.Activity.RESULT_OK;


public class DishUpdateFragment extends Fragment {
    private static final String TAG = "TAG_ShopDishUpdateFragment";
    private Activity activity;
    private Dish dish, dishDetail;
    private ImageView ivDish;
    private TextView tvDishId, tvDishState, tvNameWarning, tvInfoWarning, tvPriceWarning;
    private Button btTakePicture, btPickPicture, btFinishUpdate, btCancel;
    private EditText etDishName, etDishInfo, etDishPrice;
    private Uri contentUri, croppedImageUri;
    private byte[] image;
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_IMAGE = 1;
    private static final int REQ_CROP_PICTURE = 2;;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        activity.setTitle(R.string.textShopUpdateDishhTitle);
        return inflater.inflate(R.layout.fragment_dish_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivDish = view.findViewById(R.id.ivDish);
        //tvDishId = view.findViewById(R.id.tvDishId);
        tvDishState = view.findViewById(R.id.tvDishState);
        btTakePicture = view.findViewById(R.id.btTakePicture);
        btPickPicture = view.findViewById(R.id.btPickPicture);
        btFinishUpdate = view.findViewById(R.id.btFinishUpdate);
        btCancel = view.findViewById(R.id.btCancel);
        etDishName = view.findViewById(R.id.etDishName);
        etDishInfo = view.findViewById(R.id.etDishInfo);
        etDishPrice = view.findViewById(R.id.etDishPrice);
        tvNameWarning = view.findViewById(R.id.tvNameWarning);
        tvInfoWarning = view.findViewById(R.id.tvInfoWarning);
        tvPriceWarning = view.findViewById(R.id.tvPriceWarning);


        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("dish") == null) {
            Common.showToast(activity, R.string.textNoDishsFound);
            navController.popBackStack();
            return;
        }
        dish = (Dish) bundle.getSerializable("dish");

        showShop();

        btTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定存檔路徑
                File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                file = new File(file, "picture.jpg");
                contentUri = FileProvider.getUriForFile(
                        activity, activity.getPackageName() + ".provider", file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivityForResult(intent, REQ_TAKE_PICTURE);
                } else {
                    Common.showToast(activity, R.string.textNoCameraApp);
                }
            }
        });

        btPickPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_PICK_IMAGE);
            }
        });

        btFinishUpdate.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                String dishName = etDishName.getText().toString();
                if (dishName.length() <= 0) {
                    tvNameWarning.setText(R.string.textDishNameIsInvalid);
                    Common.showToast(activity, R.string.textDishNameIsInvalid);
                    return;
                }
                String dishPrice = etDishPrice.getText().toString().trim();
                if (dishPrice.length() <= 0) {
                    tvPriceWarning.setText(R.string.textDishPriceIsInvalid);
                    Common.showToast(getActivity(), R.string.textDishPriceIsInvalid);
                    return;
                }else {
                    tvPriceWarning.setText("");
                }

                String dishInfo = etDishInfo.getText().toString().trim();

                if (Common.networkConnected(activity)) {
                    String url = Url.URL + "/DishServlet";
                    dish.setFields(dishName, dishInfo, Integer.parseInt(dishPrice));
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "update");
                    jsonObject.addProperty("dish", new Gson().toJson(dish));
                    // 有圖才上傳
                    if (image != null) {
                        jsonObject.addProperty("imageBase64", Base64.encodeToString(image, Base64.DEFAULT));
                    }
                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        count = Integer.valueOf(result);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (count == 0) {
                        Common.showToast(activity, R.string.textUpdateFail);
                    } else {
                        Common.showToast(activity, R.string.textUpdateSuccess);
                        /* 回前一個Fragment */
                        navController.popBackStack();
                    }
                } else {
                    Common.showToast(activity, R.string.textNoNetwork);
                }

            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 回前一個Fragment */
                navController.popBackStack();
            }
        });
    }


    @SuppressLint("LongLogTag")
    private void showShop() {
        String url = Url.URL + "/DishServlet";
        int id = dish.getId();
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        Bitmap bitmap = null;
        try {
            bitmap = new ImageTask(url, id, imageSize).execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivDish.setImageBitmap(bitmap);
        } else {
            ivDish.setImageResource(R.drawable.no_image);
        }
        if (Common.networkConnected(activity)) {
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "getDishById");
            jsonObject.addProperty("id", id);
            String jsonOut = jsonObject.toString();
            CommonTask getShopTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getShopTask.execute().get();
                dishDetail = gson.fromJson(jsonIn, Dish.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }

        //tvDishId.setText(String.valueOf(dishDetail.getId()));
        if(dish.getState() == 0){
            tvDishState.setText("未上架");
            tvDishState.setTextColor(Color.RED);
        }else if(dish.getState() == 1){
            tvDishState.setText("已上架");
            tvDishState.setTextColor(Color.BLUE);
        }
        etDishName.setText(dishDetail.getName());
        etDishInfo.setText(dishDetail.getInfo());
        etDishPrice.setText(String.valueOf(dishDetail.getPrice()));
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_TAKE_PICTURE:
                    crop(contentUri);
                    break;
                case REQ_PICK_IMAGE:
                    Uri uri = intent.getData();
                    crop(uri);
                    break;
                case REQ_CROP_PICTURE:
                    Log.d(TAG, "REQ_CROP_PICTURE: " + croppedImageUri.toString());
                    try {
                        Bitmap picture = BitmapFactory.decodeStream(
                                activity.getContentResolver().openInputStream(croppedImageUri));
                        ivDish.setImageBitmap(picture);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        image = out.toByteArray();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, e.toString());
                    }
                    break;
            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        croppedImageUri = Uri.fromFile(file);
        // take care of exceptions
        try {
            // call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // the recipient of this Intent can read soruceImageUri's data
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // set image source Uri and type
            cropIntent.setDataAndType(sourceImageUri, "image/*");
            // send crop message
            cropIntent.putExtra("crop", "true");
            // aspect ratio of the cropped area, 0 means user define
            cropIntent.putExtra("aspectX", 0); // this sets the max width
            cropIntent.putExtra("aspectY", 0); // this sets the max height
            // output with and height, 0 keeps original size
            cropIntent.putExtra("outputX", 0);
            cropIntent.putExtra("outputY", 0);
            // whether keep original aspect ratio
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, croppedImageUri);
            // whether return data by the intent
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQ_CROP_PICTURE);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            Common.showToast(activity, "This device doesn't support the crop action!");
        }
    }




}
