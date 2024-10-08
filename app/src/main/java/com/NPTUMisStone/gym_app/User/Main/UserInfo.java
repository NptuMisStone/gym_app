package com.NPTUMisStone.gym_app.User.Main;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;
import static com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle.getBitmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Identify.Login;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.PasswordReset;
import com.NPTUMisStone.gym_app.User_And_Coach.Validator;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserInfo extends AppCompatActivity {
    Connection MyConnection;
    ImageView userinfo_image;
    Button upload;
    AutoCompleteTextView[] userinfo_tv;
    TextInputLayout[] userinfo_layout;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_main_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }
    private void init() {
        TextView title = findViewById(R.id.UserInfo_idText);
        title.setText(getString(R.string.User_UserInfo, Integer.toString(User.getInstance().getUserId())));
        findViewById(R.id.userinfo_return).setOnClickListener(v -> finish());
        findViewById(R.id.userinfo_logout).setOnClickListener(v -> logout());
        upload = findViewById(R.id.userinfo_upload);
        upload.setOnClickListener(v -> changeImage());
        Button change_password = findViewById(R.id.userinfo_change);
        change_password.setOnClickListener(v -> new PasswordReset(this, true, MyConnection).showPasswordResetDialog());
        userinfo_tv = new AutoCompleteTextView[]{
                findViewById(R.id.UserInfo_nameText),
                findViewById(R.id.UserInfo_accountText),
                findViewById(R.id.UserInfo_phoneText),
                findViewById(R.id.UserInfo_emailText),
                findViewById(R.id.UserInfo_identifyText)
        };
        userinfo_layout = new TextInputLayout[]{
                findViewById(R.id.UserInfo_nameLayout),
                findViewById(R.id.UserInfo_accountLayout),
                findViewById(R.id.UserInfo_phoneLayout),
                findViewById(R.id.UserInfo_emailLayout),
                findViewById(R.id.UserInfo_identifyLayout)
        };
        for (int i = 0; i < userinfo_tv.length; i++) {
            userinfo_tv[i].setText(get_info(i));
            int finalI = i;
            userinfo_tv[i].setOnClickListener(v -> changeData(finalI));
        }
        init_image();
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
    }
    private void changeData(int index) {
        Log.d("ChangeData", "ChangeData: " + index);
        AutoCompleteTextView autoCompleteTextView = userinfo_tv[index];
        TextInputLayout textInputLayout = userinfo_layout[index];
        View dialogView;
        Validator validator = new Validator(MyConnection);
        ViewGroup parent = findViewById(android.R.id.content); // Get the parent view group

        switch (index) {
            case 0, 1, 2, 3 -> {
                dialogView = getLayoutInflater().inflate(R.layout.info_edit_layout, parent, false);
                TextInputLayout textInputLayout1 = dialogView.findViewById(R.id.InfoEdit_Layout);
                textInputLayout1.setHint(textInputLayout.getHint());
                AutoCompleteTextView autoCompleteTextView1 = dialogView.findViewById(R.id.InfoEdit_Text);
                autoCompleteTextView1.setText(autoCompleteTextView.getText().toString());
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("修改資料")
                        .setMessage("是否要修改資料？")
                        .setView(dialogView)
                        .setPositiveButton("確定", null)
                        .setNegativeButton("否", null)
                        .create();
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String errorMessage = null;
                    switch (index) {
                        case 0 -> errorMessage = validator.checkInput(autoCompleteTextView1, "姓名", 20,null);
                        case 1 -> {
                            errorMessage = validator.checkInput(autoCompleteTextView1, "帳號", 20,null);
                            if (errorMessage == null) {
                                errorMessage = validator.checkAccount(autoCompleteTextView1, User.getInstance().getUserAccount(), null);
                            }
                        }
                        case 2 -> {
                            errorMessage = validator.checkInput(autoCompleteTextView1, "電話", 10,null);
                            if (errorMessage == null) {
                                errorMessage = validator.checkPhone(autoCompleteTextView1, null);
                            }
                        }
                        case 3 -> {
                            errorMessage = validator.checkInput(autoCompleteTextView1, "信箱", 30,null);
                            if (errorMessage == null) {
                                errorMessage = validator.checkEmail(autoCompleteTextView1, null);
                            }
                        }
                    }
                    if (errorMessage == null) {
                        autoCompleteTextView.setText(autoCompleteTextView1.getText().toString());
                        try {
                            String updateQuery = getUpdateQuery(index);
                            PreparedStatement ps = MyConnection.prepareStatement(updateQuery);
                            ps.setString(1, autoCompleteTextView1.getText().toString());
                            ps.setInt(2, User.getInstance().getUserId());
                            ps.executeUpdate();
                            switch (index) {
                                case 0 -> User.getInstance().setUserName(autoCompleteTextView1.getText().toString());
                                case 1 -> User.getInstance().setUserAccount(autoCompleteTextView1.getText().toString());
                                case 2 -> User.getInstance().setUserPhone(autoCompleteTextView1.getText().toString());
                                case 3 -> User.getInstance().setUserMail(autoCompleteTextView1.getText().toString());
                            }
                            alertDialog.dismiss();
                        } catch (SQLException e) {
                            Log.e("SQL", "Error updating user info", e);
                        }
                    } else {
                        editHint(autoCompleteTextView1, errorMessage);
                    }
                });
            }
            case 4 -> {
                dialogView = getLayoutInflater().inflate(R.layout.info_sex_layout, parent, false);
                RadioButton[] radioButtons = new RadioButton[]{dialogView.findViewById(R.id.InfoSex_sexRadio1), dialogView.findViewById(R.id.InfoSex_sexRadio2), dialogView.findViewById(R.id.InfoSex_sexRadio3)};
                for (int i = 0; i < radioButtons.length; i++)
                    if (i == User.getInstance().getUserSex() - 1) radioButtons[i].setChecked(true);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("修改資料")
                        .setMessage("是否要修改資料？")
                        .setView(dialogView)
                        .setPositiveButton("是", (dialog, which) -> {
                            for (int i = 0; i < radioButtons.length; i++) {
                                if (radioButtons[i].isChecked()) {
                                    try {
                                        MyConnection.prepareStatement("UPDATE 使用者資料 SET 使用者性別 = " + (i + 1) + " WHERE 使用者編號 = " + User.getInstance().getUserId()).executeUpdate();
                                        User.getInstance().setUserSex(i + 1);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    autoCompleteTextView.setText(radioButtons[i].getText().toString());
                                    break;
                                }
                            }
                        })
                        .setNegativeButton("否", null)
                        .create();
                alertDialog.show();
            }
        }
    }

    @NonNull
    private static String getUpdateQuery(int index) {
        String columnName = switch (index) {
            case 0 -> "使用者姓名";
            case 1 -> "使用者帳號";
            case 2 -> "使用者電話";
            case 3 -> "使用者郵件";
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
        return "UPDATE 使用者資料 SET " + columnName + " = ? WHERE 使用者編號 = ?";
    }

    private String get_info(int index) {
        return switch (index) {
            case 0 -> User.getInstance().getUserName();
            case 1 -> User.getInstance().getUserAccount();
            case 2 -> User.getInstance().getUserPhone();
            case 3 -> User.getInstance().getUserMail();
            case 4 -> switch (User.getInstance().getUserSex()) {
                case 1 -> "男♂";
                case 2 -> "女♀";
                case 3 -> "保密⚲";
                default -> "未設定";
            };
            default -> "";
        };
    }
    private void init_image() {
        userinfo_image = findViewById(R.id.userinfo_image);
        //將byte[]轉換成Bitmap：https://stackoverflow.com/questions/3520019/display-image-from-bytearray
        byte[] image = User.getInstance().getUserImage();
        if (image != null)
            userinfo_image.setImageBitmap(getBitmap(image));
    }
    private void logout() {
        User.setInstance(0,"","", "", 0,"",null);
        //Delete SharedPreferences Data：https://stackoverflow.com/questions/3687315/how-to-delete-shared-preferences-data-from-app-in-android
        getSharedPreferences("remember", MODE_PRIVATE).edit().remove("last_login").apply();
        startActivity(new Intent(this, Login.class));
        finish();
    }
    private void changeImage() {
        userinfo_image = findViewById(R.id.userinfo_image);
        Intent intent = new Intent();   //上傳圖片：https://www.youtube.com/watch?v=9oNujFx_ZaI&ab_channel=ShihFinChen
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        uploadImage_ActivityResult.launch(intent);
    }
    ActivityResultLauncher<Intent> uploadImage_ActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null)   return;
                    uri = data.getData();
                    userinfo_image.setImageURI(uri);
                }
                else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "取消圖片上傳", Toast.LENGTH_SHORT).show();
                }
            }
    );
}