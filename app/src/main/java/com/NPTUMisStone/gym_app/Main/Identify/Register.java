package com.NPTUMisStone.gym_app.Main.Identify;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Register extends AppCompatActivity {
    EditText et_register_name, et_register_passwd, et_register_passwd_again, et_register_phone, et_register_email, et_register_account, et_register_gender;
    Connection MyConnection;
    private int tempSex = -1; // 用於臨時保存選擇的性別

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }

    private void init() {
        et_register_name = findViewById(R.id.et_name);
        et_register_phone = findViewById(R.id.et_phone);
        et_register_email = findViewById(R.id.et_email);
        et_register_account = findViewById(R.id.et_account);
        et_register_passwd = findViewById(R.id.et_password);
        et_register_passwd_again = findViewById(R.id.et_passwd_again);
        et_register_gender = findViewById(R.id.et_gender);

        et_register_gender.setFocusable(false); // 禁止手動輸入，只允許點擊選擇
        et_register_gender.setOnClickListener(v -> showSexDialog());

        findViewById(R.id.register_ok).setOnClickListener(v -> register_input());
        findViewById(R.id.toLogin).setOnClickListener(v -> finish());
    }

    private void showSexDialog() {
        // 性別選項
        String[] sexOptions = {"男性", "女性", "不願透露"};
        int currentSex = (tempSex != -1) ? tempSex - 1 : -1; // 當前性別索引，預設為 -1 表示沒有選擇過

        new AlertDialog.Builder(this)
                .setTitle("選擇性別")
                .setSingleChoiceItems(sexOptions, currentSex, (dialog, which) -> {
                    // 設置臨時性別索引
                    tempSex = which + 1;
                })
                .setPositiveButton("確認", (dialog, which) -> {
                    if (tempSex != -1) {
                        et_register_gender.setText(sexOptions[tempSex - 1]); // 更新顯示的性別
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private boolean ValidateInput() {
        boolean isValid = true;
        Validator validator = new Validator(MyConnection);

        // 檢查是否選擇性別
        if (tempSex == -1) {
            et_register_gender.setError("請選擇性別");
            isValid = false;
        }

        String resultValidation = validator.checkEmail(et_register_email, null);
        if (resultValidation != null) {
            editHint(et_register_email, resultValidation);
            isValid = false;
        }

        resultValidation = validator.checkPhone(et_register_phone, null);
        if (resultValidation != null) {
            editHint(et_register_phone, resultValidation);
            isValid = false;
        }

        resultValidation = validator.validateAgainPasswords(et_register_passwd, et_register_passwd_again, null);
        if (resultValidation != null) {
            editHint(et_register_passwd_again, resultValidation);
            isValid = false;
        }

        resultValidation = validator.validatePasswords(et_register_passwd, null);
        if (resultValidation != null) {
            editHint(et_register_passwd, resultValidation);
            isValid = false;
        }

        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        resultValidation = validator.checkAccount(et_register_account, et_register_account.getText().toString(), null);
        if (resultValidation != null) {
            editHint(et_register_account, resultValidation);
            isValid = false;
        }

        resultValidation = validator.checkInput(et_register_account, "帳號", 20, null);
        if (resultValidation != null) {
            editHint(et_register_account, resultValidation);
            isValid = false;
        }

        resultValidation = validator.checkInput(et_register_name, "姓名", 20, null);
        if (resultValidation != null) {
            editHint(et_register_name, resultValidation);
            isValid = false;
        }

        return isValid;
    }

    private void register_input() {
        if (ValidateInput()) {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();

                String account = et_register_account.getText().toString().trim();
                String email = et_register_email.getText().toString().trim();
                String phone = et_register_phone.getText().toString().trim();

                // 檢查帳號是否已經存在
                if (isAccountRegistered(account)) {
                    Toast.makeText(this, "帳號已存在，請使用其他帳號", Toast.LENGTH_SHORT).show();
                    et_register_account.setText(""); // 清空帳號輸入框
                    return;
                }

                // 檢查電話是否已經存在
                if (isPhoneRegistered(phone)) {
                    Toast.makeText(this, "電話號碼已存在，請使用其他號碼", Toast.LENGTH_SHORT).show();
                    et_register_phone.setText(""); // 清空電話輸入框
                    return;
                }

                // 檢查郵件是否已經存在
                if (isEmailRegistered(email)) {
                    Toast.makeText(this, "電子郵件已存在，請使用其他郵件", Toast.LENGTH_SHORT).show();
                    et_register_email.setText(""); // 清空郵件輸入框
                    return;
                }

                // 將性別從文本轉換成對應的數字
                String genderText = et_register_gender.getText().toString().trim();
                int genderValue;
                switch (genderText) {
                    case "男性":
                        genderValue = 1;
                        break;
                    case "女性":
                        genderValue = 2;
                        break;
                    case "不願透露":
                        genderValue = 3;
                        break;
                    default:
                        genderValue = 0; // 未設定或意外情況
                        break;
                }

                // 插入新使用者資料
                String insertQuery = "INSERT INTO 使用者資料 (使用者帳號, 使用者密碼, 使用者姓名, 使用者電話, 使用者郵件, 使用者性別) " +
                        "VALUES ('" + account + "','" + et_register_passwd.getText().toString() + "','" + et_register_name.getText().toString() + "', '" + phone + "', '" + email + "', " + genderValue + ")";
                MyConnection.createStatement().executeUpdate(insertQuery);

                setResult(RESULT_OK, getIntent());
                finish();

            } catch (SQLException e) {
                Log.e("SQL", "Error in SQL", e);
                Toast.makeText(this, "資料庫連線錯誤", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isAccountRegistered(String acc) {
        boolean isRegistered = false;
        try {
            String sqlCoach = "SELECT COUNT(*) FROM 健身教練資料 WHERE 健身教練帳號 = ?";
            String sqlUser = "SELECT COUNT(*) FROM 使用者資料 WHERE 使用者帳號 = ?";

            PreparedStatement coachStatement = MyConnection.prepareStatement(sqlCoach);
            coachStatement.setString(1, acc);
            ResultSet coachResult = coachStatement.executeQuery();

            if (coachResult.next() && coachResult.getInt(1) > 0) {
                return true; // 教練資料中已經存在該帳號
            }

            PreparedStatement userStatement = MyConnection.prepareStatement(sqlUser);
            userStatement.setString(1, acc);
            ResultSet userResult = userStatement.executeQuery();

            if (userResult.next() && userResult.getInt(1) > 0) {
                isRegistered = true; // 使用者資料中已經存在該帳號
            }

        } catch (SQLException e) {
            Log.e("SQL", "Error checking account uniqueness", e);
        }
        return isRegistered;
    }

    private boolean isPhoneRegistered(String phone) {
        boolean isRegistered = false;
        try {
            String sqlCoach = "SELECT COUNT(*) FROM 健身教練資料 WHERE 健身教練電話 = ?";
            String sqlUser = "SELECT COUNT(*) FROM 使用者資料 WHERE 使用者電話 = ?";

            PreparedStatement coachStatement = MyConnection.prepareStatement(sqlCoach);
            coachStatement.setString(1, phone);
            ResultSet coachResult = coachStatement.executeQuery();

            if (coachResult.next() && coachResult.getInt(1) > 0) {
                return true; // 教練資料中已經存在該電話
            }

            PreparedStatement userStatement = MyConnection.prepareStatement(sqlUser);
            userStatement.setString(1, phone);
            ResultSet userResult = userStatement.executeQuery();

            if (userResult.next() && userResult.getInt(1) > 0) {
                isRegistered = true; // 使用者資料中已經存在該電話
            }

        } catch (SQLException e) {
            Log.e("SQL", "Error checking phone uniqueness", e);
        }
        return isRegistered;
    }

    private boolean isEmailRegistered(String email) {
        boolean isRegistered = false;
        try {
            String sqlCoach = "SELECT COUNT(*) FROM 健身教練資料 WHERE 健身教練郵件 = ?";
            String sqlUser = "SELECT COUNT(*) FROM 使用者資料 WHERE 使用者郵件 = ?";

            PreparedStatement coachStatement = MyConnection.prepareStatement(sqlCoach);
            coachStatement.setString(1, email);
            ResultSet coachResult = coachStatement.executeQuery();

            if (coachResult.next() && coachResult.getInt(1) > 0) {
                return true; // 教練資料中已經存在該郵件
            }

            PreparedStatement userStatement = MyConnection.prepareStatement(sqlUser);
            userStatement.setString(1, email);
            ResultSet userResult = userStatement.executeQuery();

            if (userResult.next() && userResult.getInt(1) > 0) {
                isRegistered = true; // 使用者資料中已經存在該郵件
            }

        } catch (SQLException e) {
            Log.e("SQL", "Error checking email uniqueness", e);
        }
        return isRegistered;
    }
}
