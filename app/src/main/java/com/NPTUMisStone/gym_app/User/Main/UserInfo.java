package com.NPTUMisStone.gym_app.User.Main;

import static com.NPTUMisStone.gym_app.User_And_Coach.Helper.ErrorHints.editHint;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.PasswordReset;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.Validator;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserInfo extends AppCompatActivity {
    Connection MyConnection;
    ImageView userinfo_image;
    AutoCompleteTextView[] userinfo_tv;
    TextInputLayout[] userinfo_layout;
    Uri uri;

    private boolean isEditMode = false;
    private int tempSex = -1;

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
        findViewById(R.id.UserInfo_return).setOnClickListener(v -> finish());
        findViewById(R.id.UserInfo_logout).setOnClickListener(v -> logout());
        findViewById(R.id.UserInfo_upload).setOnClickListener(v -> changeImage());
        findViewById(R.id.UserInfo_resetButton).setOnClickListener(v -> new PasswordReset(this, MyConnection).showPasswordResetDialog());
        findViewById(R.id.UserInfo_saveButton).setOnClickListener(v -> saveUpdatedInfo());
        findViewById(R.id.UserInfo_cancelButton).setOnClickListener(v -> {
            toggleEditMode(false); // 取消時退出編輯模式
            refreshFields(); // 取消時刷新回原資料
        });

        // 宣告並設置 UserEdit 按鈕
        findViewById(R.id.UserInfo_edit).setOnClickListener(v -> toggleEditMode(true)); // 點擊進入編輯模式

        findViewById(R.id.UserInfo_delete).setOnClickListener(v -> deleteAccount());

        userinfo_tv = new AutoCompleteTextView[]{
                findViewById(R.id.UserInfo_nameText),
                findViewById(R.id.UserInfo_accountText),
                findViewById(R.id.UserInfo_phoneText),
                findViewById(R.id.UserInfo_emailText),
                findViewById(R.id.UserInfo_sexText)
        };
        userinfo_layout = new TextInputLayout[]{
                findViewById(R.id.UserInfo_nameLayout),
                findViewById(R.id.UserInfo_accountLayout),
                findViewById(R.id.UserInfo_phoneLayout),
                findViewById(R.id.UserInfo_emailLayout),
                findViewById(R.id.UserInfo_identifyLayout)
        };

        // 初始化各欄位的內容
        for (int i = 0; i < userinfo_tv.length; i++) {
            userinfo_tv[i].setText(get_info(i));
            userinfo_tv[i].setEnabled(false); // 初始為不可編輯
        }

        // 為性別欄位添加點擊事件，顯示性別選擇對話框
        userinfo_tv[4].setInputType(0); // 禁用鍵盤輸入
        userinfo_tv[4].setKeyListener(null); // 禁用按鍵事件
        userinfo_tv[4].setCursorVisible(false); // 隱藏光標
        userinfo_tv[4].setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showSexDialog(); // 顯示性別選擇對話框
            }
            return true; // 阻止默認點擊行為
        });

        // 禁用帳號欄位
        userinfo_tv[1].setFocusable(false);
        userinfo_tv[1].setFocusableInTouchMode(false);

        init_image();
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
    }
    private void init_image() {
        userinfo_image = findViewById(R.id.UserInfo_image);
        byte[] image = User.getInstance().getUserImage();
        if (image != null && image.length > 0) {
            // 將圖片轉換為 Bitmap 並設置
            userinfo_image.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
        } else {
            // 設置默認圖片
            userinfo_image.setImageResource(R.drawable.user_main_ic_default);
        }
    }


    private void refreshFields() {
        for (int i = 0; i < userinfo_tv.length; i++) {
            userinfo_tv[i].setText(get_info(i)); // 使用原始資料刷新字段
        }
        refreshImage(); // 刷新圖片
    }
    private void refreshImage() {
        byte[] image = User.getInstance().getUserImage();
        if (image != null && image.length > 0) {
            userinfo_image.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
        } else {
            userinfo_image.setImageResource(R.drawable.user_main_ic_default);
        }
        uri = null; // 重置 URI
    }


    private void toggleEditMode(boolean isCancel) {
        isEditMode = !isEditMode;

        // 如果是取消操作，刷新資料
        if (isCancel) {
            refreshUserInfo(); // 刷新文字欄位
            refreshImage(); // 刷新圖片
        }

        // 根據模式切換文字顏色
        int editModeColor = getResources().getColor(R.color.dark_black); // 編輯模式顏色
        int viewModeColor = getResources().getColor(R.color.dark_gray); // 查看模式顏色
        int textColor = isEditMode ? editModeColor : viewModeColor;

        // 切換所有欄位的編輯狀態
        for (int i = 0; i < userinfo_tv.length; i++) {
            if (i == 1) continue; // 跳過帳號欄位
            AutoCompleteTextView textView = userinfo_tv[i];
            textView.setEnabled(isEditMode); // 切換編輯模式
            textView.setFocusable(isEditMode);
            textView.setFocusableInTouchMode(isEditMode);
            textView.setTextColor(textColor); // 動態設置文字顏色
        }

        // 隱藏或顯示編輯圖示
        findViewById(R.id.UserInfo_edit).setVisibility(isEditMode ? View.GONE : View.VISIBLE);

        // 顯示或隱藏儲存與取消按鈕
        findViewById(R.id.saveCancelButtons).setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        // 顯示或隱藏 UserInfo_upload
        findViewById(R.id.UserInfo_upload).setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }


    private void refreshUserInfo() {
        for (int i = 0; i < userinfo_tv.length; i++) {
            userinfo_tv[i].setText(get_info(i));
        }
        tempSex = -1; // 重置臨時性別
    }

    private void saveUpdatedInfo() {
        // 日誌
        Log.d("UserInfo", "saveUpdatedInfo() called");
        Validator validator = new Validator(MyConnection);

        // 驗證並更新每個欄位，跳過索引 4 (性別)
        for (int i = 0; i < userinfo_tv.length; i++) {
            if (i == 4) continue; // 跳過性別，稍後單獨處理
            AutoCompleteTextView textView = userinfo_tv[i];
            String errorMessage = validateInput(i, textView, validator);

            if (errorMessage != null) {
                editHint(textView, errorMessage);
                return; // 終止保存操作
            }

            try {
                String updateQuery = getUpdateQuery(i);
                PreparedStatement ps = MyConnection.prepareStatement(updateQuery);
                ps.setString(1, textView.getText().toString());
                ps.setInt(2, User.getInstance().getUserId());
                ps.executeUpdate();
                updateUserInstance(i, textView.getText().toString());
            } catch (SQLException e) {
                Log.e("SQL", "Error saving updated info", e);
                Toast.makeText(this, "更新失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 處理性別更新
        if (tempSex != -1 && tempSex != User.getInstance().getUserSex()) {
            try {
                String updateQuery = "UPDATE 使用者資料 SET 使用者性別 = ? WHERE 使用者編號 = ?";
                PreparedStatement ps = MyConnection.prepareStatement(updateQuery);
                ps.setInt(1, tempSex);
                ps.setInt(2, User.getInstance().getUserId());
                ps.executeUpdate();

                // 更新本地 User 實例數據
                User.getInstance().setUserSex(tempSex);
            } catch (SQLException e) {
                Log.e("SQL", "更新性別失敗", e);
                Toast.makeText(this, "性別更新失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // 處理圖片更新
        if (uri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageBytes = stream.toByteArray();

                String updateImageQuery = "UPDATE 使用者資料 SET 使用者圖片 = ? WHERE 使用者編號 = ?";
                PreparedStatement ps = MyConnection.prepareStatement(updateImageQuery);
                ps.setBytes(1, imageBytes);
                ps.setInt(2, User.getInstance().getUserId());
                ps.executeUpdate();

                // 更新本地 User 實例的圖片
                User.getInstance().setUserImage(imageBytes);

            } catch (IOException | SQLException e) {
                Log.e("SQL", "更新圖片失敗", e);
                Toast.makeText(this, "圖片更新失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (User.getInstance().getUserImage() == null || User.getInstance().getUserImage().length == 0) {
            // 如果沒有新圖片且當前圖片為空，設置默認圖片
            userinfo_image.setImageResource(R.drawable.user_main_ic_default);
        }


        // 關閉編輯模式
        toggleEditMode(false); // 確保此方法被正確執行
        Log.d("UserInfo", "toggleEditMode(false) called"); // 日誌
        Toast.makeText(this, "資料已成功更新", Toast.LENGTH_SHORT).show();
    }



    private void showSexDialog() {
        // 性別選項
        String[] sexOptions = {"男性", "女性", "不願透露"};
        int currentSex = (tempSex != -1) ? tempSex - 1 : User.getInstance().getUserSex() - 1; // 當前性別索引

        new AlertDialog.Builder(this)
                .setTitle("選擇性別")
                .setSingleChoiceItems(sexOptions, currentSex, (dialog, which) -> {
                    // 設置臨時性別索引
                    tempSex = which + 1;
                })
                .setPositiveButton("確認", (dialog, which) -> {
                    if (tempSex != -1) {
                        userinfo_tv[4].setText(sexOptions[tempSex - 1]); // 更新顯示的性別
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }


    private String validateInput(int index, AutoCompleteTextView autoCompleteTextView1, Validator validator) {
        return switch (index) {
            case 0 -> validator.checkInput(autoCompleteTextView1, "姓名", 20, null);
            case 1 -> {
                String errorMessage = validator.checkInput(autoCompleteTextView1, "帳號", 20, null);
                if (errorMessage == null) {
                    errorMessage = validator.checkAccount(autoCompleteTextView1, User.getInstance().getUserAccount(), null);
                }
                yield errorMessage;
            }
            case 2 -> {
                String errorMessage = validator.checkInput(autoCompleteTextView1, "電話", 10, null);
                if (errorMessage == null) {
                    errorMessage = validator.checkPhone(autoCompleteTextView1, null);
                }
                yield errorMessage;
            }
            case 3 -> {
                String errorMessage = validator.checkInput(autoCompleteTextView1, "信箱", 30, null);
                if (errorMessage == null) {
                    errorMessage = validator.checkEmail(autoCompleteTextView1, null);
                }
                yield errorMessage;
            }
            default -> null;
        };
    }

    private void updateUserInstance(int index, String newValue) {
        switch (index) {
            case 0 -> User.getInstance().setUserName(newValue);
            case 1 -> User.getInstance().setUserAccount(newValue);
            case 2 -> User.getInstance().setUserPhone(newValue);
            case 3 -> User.getInstance().setUserMail(newValue);
        }
    }

    @NonNull
    private static String getUpdateQuery(int index) {
        String columnName = switch (index) {
            case 0 -> "使用者姓名";
            case 1 -> "使用者帳號";
            case 2 -> "使用者電話";
            case 3 -> "使用者郵件";
            case 4 -> "使用者性別"; // 性別對應資料庫中的欄位名稱
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
                case 1 -> "男性";
                case 2 -> "女性";
                case 3 -> "不願透露";
                default -> "未設定";
            };
            default -> "";
        };
    }
    private void logout() {
        User.setInstance(0, "", "", "", 0, "", null);
        //Delete SharedPreferences Data：https://stackoverflow.com/questions/3687315/how-to-delete-shared-preferences-data-from-app-in-android
        getSharedPreferences("remember", MODE_PRIVATE).edit().remove("last_login").apply();
        sendBroadcast(new Intent("com.NPTUMisStone.gym_app.LOGOUT"));
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void changeImage() {
        userinfo_image = findViewById(R.id.UserInfo_image);
        Intent intent = new Intent();
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
                    if (data != null) {
                        uri = data.getData(); // 設定新的 URI
                        userinfo_image.setImageURI(uri); // 顯示新圖片
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "取消圖片選擇", Toast.LENGTH_SHORT).show();
                    refreshImage(); // 恢復原始圖片
                }
            }
    );
    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("刪除帳號")
                .setMessage("是否確定要刪除帳號？刪除後無法復原。")
                .setPositiveButton("確定", (dialog, which) -> {
                    // 第一步：請輸入密碼驗證
                    showPasswordInputDialog();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showPasswordInputDialog() {
        // 創建輸入框
        EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("驗證密碼")
                .setMessage("請輸入您的密碼以驗證身份：")
                .setView(passwordInput)
                .setPositiveButton("確定", (dialog, which) -> {
                    String inputPassword = passwordInput.getText().toString();
                    if (validatePasswordFromDatabase(inputPassword)) {
                        // 第二步：再次確認刪除
                        confirmFinalDelete();
                    } else {
                        Toast.makeText(this, "密碼錯誤，無法刪除帳號。", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private boolean validatePasswordFromDatabase(String inputPassword) {
        boolean isValid = false;
        try {
            // 使用 SQL 查詢從資料庫驗證密碼
            String query = "SELECT 使用者密碼 FROM 使用者資料 WHERE 使用者編號 = ?";
            PreparedStatement ps = MyConnection.prepareStatement(query);
            ps.setInt(1, User.getInstance().getUserId()); // 使用當前教練的 ID
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("使用者密碼");
                isValid = inputPassword.equals(storedPassword); // 比較輸入的密碼與資料庫中的密碼
            }
        } catch (SQLException e) {
            Log.e("SQL", "密碼驗證失敗", e);
        }
        return isValid;
    }

    private void confirmFinalDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最終確認")
                .setMessage("帳號刪除後無法復原，是否真的要刪除？")
                .setPositiveButton("確定", (dialog, which) -> performDeleteAccount())
                .setNegativeButton("取消", null)
                .show();
    }

    private void performDeleteAccount() {
        try {
            // 刪除使用者主記錄
            String deleteUserQuery = "DELETE FROM 使用者資料 WHERE 使用者編號 = ?";
            PreparedStatement userStatement = MyConnection.prepareStatement(deleteUserQuery);
            userStatement.setInt(1, User.getInstance().getUserId());
            userStatement.executeUpdate();

            // 清除本地 User 實例
            User.setInstance(0, "", "", "", 0, "", null);

            // 跳轉到登錄頁面
            Toast.makeText(this, "帳號已刪除。", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
        } catch (SQLException e) {
            Log.e("SQL", "刪除帳號失敗", e);
            Toast.makeText(this, "刪除帳號時發生錯誤，請稍後再試。", Toast.LENGTH_SHORT).show();
        }
    }
}