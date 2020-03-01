package tw.dp103g3.itfood_shop.dish;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.shop.Dish;
import tw.dp103g3.itfood_shop.shop.Shop;
import tw.dp103g3.itfood_shop.task.CommonTask;

import static android.app.Activity.RESULT_OK;
import static java.lang.Integer.parseInt;


public class AddDishFragment extends Fragment {
    private Activity activity;
    private final static String TAG = "TAG_AddBookFragment";
    private Shop shop;
    private ImageView ivDish;
    private EditText etDishName, etDishInfo, etDishPrice;
    private TextView tvNameWarning, tvInfoWarning, tvPriceWarning;
    private Button btTakePicture, btPickPicture, btFinishInsert, btCancel;
    private Uri contentUri, croppedImageUri;
    private byte[] image;
    private Toolbar toolbarAddAddress;
    int shopId;
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_PICTURE = 1;
    private static final int REQ_CROP_PICTURE = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        shopId = Common.getShopId(activity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_add_dish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);


        ivDish = view.findViewById(R.id.ivDish);
        btTakePicture = view.findViewById(R.id.btTakePicture);
        btPickPicture = view.findViewById(R.id.btPickPicture);
        btFinishInsert = view.findViewById(R.id.btFinishInsert);
        btCancel = view.findViewById(R.id.btCancel);
        etDishName = view.findViewById(R.id.etDishName);
        etDishInfo = view.findViewById(R.id.etDishInfo);
        etDishPrice = view.findViewById(R.id.etDishPrice);
        tvNameWarning = view.findViewById(R.id.tvNameWarning);
        tvInfoWarning = view.findViewById(R.id.tvInfoWarning);
        tvPriceWarning = view.findViewById(R.id.tvPriceWarning);

        toolbarAddAddress = view.findViewById(R.id.toolbarAddAddress);

        toolbarAddAddress.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

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
                    Common.showToast(getActivity(), R.string.textNoCameraApp);
                }
            }
        });

        btPickPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_PICK_PICTURE);
            }
        });

        btFinishInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dishName = etDishName.getText().toString().trim();
                if (dishName.length() <= 0) {
                    tvNameWarning.setText(R.string.textDishNameIsInvalid);
                    Common.showToast(getActivity(), R.string.textDishNameIsInvalid);
                    return;
                }
                String dishInfo = etDishInfo.getText().toString().trim();

                String dishPrice = etDishPrice.getText().toString().trim();
                if (dishPrice.length() <= 0) {
                    tvPriceWarning.setText(R.string.textDishPriceIsInvalid);
                    Common.showToast(getActivity(), R.string.textDishPriceIsInvalid);
                    return;
                }

                byte dishState = 0;

                if (Common.networkConnected(activity)) {
                    String url = Url.URL + "/DishServlet";
                    Dish dish = new Dish(0,dishName, dishInfo, dishState, shopId, parseInt(dishPrice));
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "insert");
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
                        Common.showToast(getActivity(), R.string.textDishInsertFail);
                    } else {
                        Common.showToast(getActivity(), R.string.textDishInsertSuccess);
                        navController.popBackStack();
                    }
                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }
                /* 回前一個Fragment */

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_TAKE_PICTURE:
                    crop(contentUri);
                    break;
                case REQ_PICK_PICTURE:
                    crop(intent.getData());
                    break;
                case REQ_CROP_PICTURE:
                    Uri uri = intent.getData();
                    Bitmap bitmap = null;
                    if (uri != null) {
                        try {
                            bitmap = BitmapFactory.decodeStream(
                                    activity.getContentResolver().openInputStream(uri));
                            ivDish.setImageBitmap(bitmap);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            image = out.toByteArray();
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                    if (bitmap != null) {
                        ivDish.setImageBitmap(bitmap);
                    } else {
                        ivDish.setImageResource(R.drawable.no_image);
                    }
                    break;
            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        Uri uri = Uri.fromFile(file);
        // 開啟截圖功能
        Intent intent = new Intent("com.android.camera.action.CROP");
        // 授權讓截圖程式可以讀取資料
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 設定圖片來源與類型
        intent.setDataAndType(sourceImageUri, "image/*");
        // 設定要截圖
        intent.putExtra("crop", "true");
        // 設定截圖框大小，0代表user任意調整大小
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        // 設定圖片輸出寬高，0代表維持原尺寸
        intent.putExtra("outputX", 0);
        intent.putExtra("outputY", 0);
        // 是否保持原圖比例
        intent.putExtra("scale", true);
        // 設定截圖後圖片位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        // 設定是否要回傳值
        intent.putExtra("return-data", true);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            // 開啟截圖activity
            startActivityForResult(intent, REQ_CROP_PICTURE);
        } else {
            Toast.makeText(activity, R.string.textNoImageCropAppFound,
                    Toast.LENGTH_SHORT).show();
        }
    }


}
