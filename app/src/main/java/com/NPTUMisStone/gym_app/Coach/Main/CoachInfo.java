package com.NPTUMisStone.gym_app.Coach.Main;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.PasswordReset;
import com.NPTUMisStone.gym_app.User_And_Coach.Validator;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CoachInfo extends AppCompatActivity {
    Connection MyConnection;
    ImageView coachInfo_image;
    AutoCompleteTextView[] coachInfo_tv;
    TextInputLayout[] coachInfo_layout;
    Uri uri;
    private boolean isEditMode = false; // 判斷是否進入編輯模式
    private int tempSex = -1; // 臨時存儲選擇的性別，-1 表示未改變



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_main_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }

    private void init() {
        findViewById(R.id.CoachInfo_return).setOnClickListener(v -> finish());
        findViewById(R.id.CoachInfo_logout).setOnClickListener(v -> logout());
        findViewById(R.id.CoachInfo_upload).setOnClickListener(v -> changeImage());
        findViewById(R.id.CoachInfo_resetButton).setOnClickListener(v -> new PasswordReset(this, false, MyConnection).showPasswordResetDialog());
        findViewById(R.id.CoachInfo_saveButton).setOnClickListener(v -> {
            saveUpdatedInfo();
        });
        findViewById(R.id.CoachInfo_cancelButton).setOnClickListener(v -> {
            toggleEditMode(false); // 取消時退出編輯模式
            refreshFields(); // 取消時刷新回原資料
        });

        // 宣告並設置 coachEdit 按鈕
        findViewById(R.id.coachEdit).setOnClickListener(v -> toggleEditMode(true)); // 點擊進入編輯模式

        findViewById(R.id.CoachInfo_delete).setOnClickListener(v -> deleteAccount());

        coachInfo_tv = new AutoCompleteTextView[]{
                findViewById(R.id.CoachInfo_nameText),
                findViewById(R.id.CoachInfo_accountText),
                findViewById(R.id.CoachInfo_phoneText),
                findViewById(R.id.CoachInfo_emailText),
                findViewById(R.id.CoachInfo_sexText),
                findViewById(R.id.CoachInfo_introText)
        };
        coachInfo_layout = new TextInputLayout[]{
                findViewById(R.id.CoachInfo_nameLayout),
                findViewById(R.id.CoachInfo_accountLayout),
                findViewById(R.id.CoachInfo_phoneLayout),
                findViewById(R.id.CoachInfo_emailLayout),
                findViewById(R.id.CoachInfo_identifyLayout),
                findViewById(R.id.CoachInfo_introLayout)
        };

        // 初始化各欄位的內容
        for (int i = 0; i < coachInfo_tv.length; i++) {
            coachInfo_tv[i].setText(get_info(i));
            coachInfo_tv[i].setEnabled(false); // 初始為不可編輯
        }

        // 為性別欄位添加點擊事件，顯示性別選擇對話框
        coachInfo_tv[4].setInputType(0); // 禁用鍵盤輸入
        coachInfo_tv[4].setKeyListener(null); // 禁用按鍵事件
        coachInfo_tv[4].setCursorVisible(false); // 隱藏光標
        coachInfo_tv[4].setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showSexDialog(); // 顯示性別選擇對話框
            }
            return true; // 阻止默認點擊行為
        });

        // 禁用帳號欄位
        coachInfo_tv[1].setFocusable(false);
        coachInfo_tv[1].setFocusableInTouchMode(false);

        init_image();
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
    }

    private void refreshFields() {
        for (int i = 0; i < coachInfo_tv.length; i++) {
            coachInfo_tv[i].setText(get_info(i)); // 使用原始資料刷新字段
        }
        refreshImage(); // 刷新圖片
    }
    private void refreshImage() {
        byte[] image = Coach.getInstance().getCoachImage(); // 從本地緩存獲取圖片
        if (image != null) {
            coachInfo_image.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image))); // 更新圖片顯示
        }
        uri = null; // 重置 URI
    }

    private void toggleEditMode(boolean isCancel) {
        isEditMode = !isEditMode;

        // 如果是取消操作，刷新資料
        if (isCancel) {
            refreshCoachInfo(); // 刷新文字欄位
            refreshImage(); // 刷新圖片
        }

        // 切換所有欄位的編輯狀態
        for (int i = 0; i < coachInfo_tv.length; i++) {
            if (i == 1) continue; // 跳過帳號欄位
            AutoCompleteTextView textView = coachInfo_tv[i];
            textView.setEnabled(isEditMode); // 切換編輯模式
            textView.setFocusable(isEditMode);
            textView.setFocusableInTouchMode(isEditMode);
        }

        // 隱藏或顯示編輯圖示
        findViewById(R.id.coachEdit).setVisibility(isEditMode ? View.GONE : View.VISIBLE);

        // 顯示或隱藏儲存與取消按鈕
        findViewById(R.id.saveCancelButtons).setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        // 顯示或隱藏 CoachInfo_upload
        findViewById(R.id.CoachInfo_upload).setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }

    private void refreshCoachInfo() {
        for (int i = 0; i < coachInfo_tv.length; i++) {
            coachInfo_tv[i].setText(get_info(i));
        }
        tempSex = -1; // 重置臨時性別
    }

    private void saveUpdatedInfo() {
        // 日誌
        Log.d("CoachInfo", "saveUpdatedInfo() called");
        Validator validator = new Validator(MyConnection);

        // 驗證並更新每個欄位，跳過索引 4 (性別)
        for (int i = 0; i < coachInfo_tv.length; i++) {
            if (i == 4) continue; // 跳過性別，稍後單獨處理
            AutoCompleteTextView textView = coachInfo_tv[i];
            String errorMessage = validateInput(i, textView, validator);

            if (errorMessage != null) {
                editHint(textView, errorMessage);
                return; // 終止保存操作
            }

            try {
                String updateQuery = getUpdateQuery(i);
                PreparedStatement ps = MyConnection.prepareStatement(updateQuery);
                ps.setString(1, textView.getText().toString());
                ps.setInt(2, Coach.getInstance().getCoachId());
                ps.executeUpdate();
                updateUserInstance(i, textView.getText().toString());
            } catch (SQLException e) {
                Log.e("SQL", "Error saving updated info", e);
                Toast.makeText(this, "更新失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 處理性別更新
        if (tempSex != -1 && tempSex != Coach.getInstance().getCoachSex()) {
            try {
                String updateQuery = "UPDATE 健身教練資料 SET 健身教練性別 = ? WHERE 健身教練編號 = ?";
                PreparedStatement ps = MyConnection.prepareStatement(updateQuery);
                ps.setInt(1, tempSex);
                ps.setInt(2, Coach.getInstance().getCoachId());
                ps.executeUpdate();

                // 更新本地 Coach 實例數據
                Coach.getInstance().setCoachSex(tempSex);
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

                String updateImageQuery = "UPDATE 健身教練資料 SET 健身教練圖片 = ? WHERE 健身教練編號 = ?";
                PreparedStatement ps = MyConnection.prepareStatement(updateImageQuery);
                ps.setBytes(1, imageBytes);
                ps.setInt(2, Coach.getInstance().getCoachId());
                ps.executeUpdate();

                // 更新本地 Coach 實例的圖片
                Coach.getInstance().setCoachImage(imageBytes);

            } catch (IOException | SQLException e) {
                Log.e("SQL", "更新圖片失敗", e);
                Toast.makeText(this, "圖片更新失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // 處理個人介紹欄位
        if (coachInfo_tv[5].isEnabled()) { // 如果可編輯
            String introText = coachInfo_tv[5].getText().toString().trim();
            try {
                String updateIntroQuery = "UPDATE 健身教練資料 SET 健身教練介紹 = ? WHERE 健身教練編號 = ?";
                PreparedStatement ps = MyConnection.prepareStatement(updateIntroQuery);
                ps.setString(1, introText);
                ps.setInt(2, Coach.getInstance().getCoachId());
                ps.executeUpdate();

                // 更新本地 Coach 實例
                Coach.getInstance().setCoachIntro(introText);
            } catch (SQLException e) {
                Log.e("SQL", "更新個人介紹失敗", e);
                Toast.makeText(this, "個人介紹更新失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        // 關閉編輯模式
        toggleEditMode(false); // 確保此方法被正確執行
        Log.d("CoachInfo", "toggleEditMode(false) called"); // 日誌
        Toast.makeText(this, "資料已成功更新", Toast.LENGTH_SHORT).show();
    }



    private void showSexDialog() {
        // 性別選項
        String[] sexOptions = {"男性", "女性", "不願透露"};
        int currentSex = (tempSex != -1) ? tempSex - 1 : Coach.getInstance().getCoachSex() - 1; // 當前性別索引

        new AlertDialog.Builder(this)
                .setTitle("選擇性別")
                .setSingleChoiceItems(sexOptions, currentSex, (dialog, which) -> {
                    // 設置臨時性別索引
                    tempSex = which + 1;
                })
                .setPositiveButton("確認", (dialog, which) -> {
                    if (tempSex != -1) {
                        coachInfo_tv[4].setText(sexOptions[tempSex - 1]); // 更新顯示的性別
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
                    errorMessage = validator.checkAccount(autoCompleteTextView1, Coach.getInstance().getCoachAccount(), null);
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
            case 0 -> Coach.getInstance().setCoachName(newValue);
            case 1 -> Coach.getInstance().setCoachAccount(newValue);
            case 2 -> Coach.getInstance().setCoachPhone(newValue);
            case 3 -> Coach.getInstance().setCoachMail(newValue);
        }
    }

    @NonNull
    private static String getUpdateQuery(int index) {
        String columnName = switch (index) {
            case 0 -> "健身教練姓名";
            case 1 -> "健身教練帳號";
            case 2 -> "健身教練電話";
            case 3 -> "健身教練郵件";
            case 4 -> "健身教練性別"; // 性別對應資料庫中的欄位名稱
            case 5 -> "健身教練介紹"; // 個人介紹的欄位名稱
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
        return "UPDATE 健身教練資料 SET " + columnName + " = ? WHERE 健身教練編號 = ?";
    }



    private String get_info(int index) {
        return switch (index) {
            case 0 -> Coach.getInstance().getCoachName();
            case 1 -> Coach.getInstance().getCoachAccount();
            case 2 -> Coach.getInstance().getCoachPhone();
            case 3 -> Coach.getInstance().getCoachMail();
            case 4 -> switch (Coach.getInstance().getCoachSex()) {
                case 1 -> "男性";
                case 2 -> "女性";
                case 3 -> "不願透露";
                default -> "未設定";
            };
            case 5 -> Coach.getInstance().getCoachIntro(); // 新增個人介紹
            default -> "";
        };
    }


    private void init_image() {
        coachInfo_image = findViewById(R.id.CoachInfo_image);
        //將byte[]轉換成Bitmap：https://stackoverflow.com/questions/3520019/display-image-from-bytearray
        byte[] image = Coach.getInstance().getCoachImage();
        if (image != null)
            coachInfo_image.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
    }

    private void logout() {
        Coach.setInstance(0, "", "", "", 0, "", null,"");
        //Delete SharedPreferences Data：https://stackoverflow.com/questions/3687315/how-to-delete-shared-preferences-data-from-app-in-android
        getSharedPreferences("remember", MODE_PRIVATE).edit().remove("last_login").apply();
        sendBroadcast(new Intent("com.NPTUMisStone.gym_app.LOGOUT"));
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void changeImage() {
        coachInfo_image = findViewById(R.id.CoachInfo_image);
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
                        coachInfo_image.setImageURI(uri); // 顯示新圖片
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
            String query = "SELECT 健身教練密碼 FROM 健身教練資料 WHERE 健身教練編號 = ?";
            PreparedStatement ps = MyConnection.prepareStatement(query);
            ps.setInt(1, Coach.getInstance().getCoachId()); // 使用當前教練的 ID
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("健身教練密碼");
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
                .setPositiveButton("確定", (dialog, which) -> {
                    performDeleteAccount();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void performDeleteAccount() {
        try {
            String deleteQuery = "DELETE FROM 健身教練資料 WHERE 健身教練編號 = ?";
            PreparedStatement ps = MyConnection.prepareStatement(deleteQuery);
            ps.setInt(1, Coach.getInstance().getCoachId());
            ps.executeUpdate();

            // 清除本地 Coach 實例
            Coach.setInstance(0, "", "", "", 0, "", null,"");

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