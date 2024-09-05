package com.NPTUMisStone.gym_app.Main.Identify;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Register extends AppCompatActivity {
    EditText et_register_name,et_register_passwd,et_register_passwd_again, et_register_phone, et_register_email ,et_register_account;
    Connection MyConnection;
    Boolean isUser = false;
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
        change_status();
        et_register_name = findViewById(R.id.et_name);
        et_register_phone = findViewById(R.id.et_phone);
        et_register_email = findViewById(R.id.et_email);
        et_register_account = findViewById(R.id.et_account);
        et_register_passwd = findViewById(R.id.et_password);
        et_register_passwd_again =findViewById(R.id.et_passwd_again);
        findViewById(R.id.register_ok).setOnClickListener(v -> register_input());
        findViewById(R.id.toLogin).setOnClickListener(v -> finish());
        ((SwitchCompat)findViewById(R.id.register_change_status)).setOnCheckedChangeListener((buttonView, isChecked) -> change_status());
    }
    private void change_status(){
        isUser = !isUser;
        ((TextView)findViewById(R.id.register_title)).setText(isUser ? "用戶註冊" : "教練註冊");
    }
    private boolean ValidateInput() {   //為了在最後Focus在最上面的錯誤輸入，所以將順序倒過來
        boolean isValid = true;
        try {
            if(!et_register_email.getText().toString().isEmpty()){
                if (!isEmailValid(et_register_email.getText().toString())) {
                    editHint(et_register_email, "請輸入有效的電子郵件");
                    isValid = false;
                }
            }
            if (et_register_phone.getText().toString().isEmpty()) {
                editHint(et_register_phone, "請輸入聯絡電話");
                isValid = false;
            }else if (et_register_phone.getText().toString().length() != 10) {
                editHint(et_register_phone, "請輸入正確的聯絡電話");
                isValid = false;
            }
            if (et_register_passwd_again.getText().toString().isEmpty()) {
                editHint(et_register_passwd_again, "請輸入確認密碼");
                isValid = false;
            }
            if (et_register_passwd.getText().toString().isEmpty()) {
                editHint(et_register_passwd, "請輸入密碼");
                isValid = false;
            }else if (et_register_passwd.getText().toString().length() < 6) {
                editHint(et_register_passwd, "密碼長度至少6個字元");
                isValid = false;
            } else if (et_register_passwd.getText().toString().length()==et_register_account.getText().toString().length()) {
                editHint(et_register_passwd, "密碼不可與帳號相同");
                isValid = false;
            }
            if (!et_register_passwd.getText().toString().equals(et_register_passwd_again.getText().toString())) {
                editHint(et_register_passwd_again, "確認密碼不一致");
                isValid = false;
            }
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            if (et_register_account.getText().toString().isEmpty()) {
                editHint(et_register_account, "請輸入帳號");
                isValid = false;
            }else{
                String searchQuery = isUser ? "SELECT * FROM 使用者資料 WHERE 使用者帳號 ='" + et_register_account.getText().toString()+ "'" : "SELECT * FROM 健身教練資料 WHERE 健身教練帳號 ='" + et_register_account.getText().toString()+ "'";
                ResultSet account_result = MyConnection.createStatement().executeQuery(searchQuery);
                if (account_result.next()) {
                    editHint(et_register_account, "此帳號已被使用");
                    isValid = false;
                }
            }
            if (et_register_name.getText().toString().isEmpty()) {
                editHint(et_register_name, "請輸入姓名");
                isValid = false;
            }
            return isValid;
        } catch (SQLException e) {
            Log.e("SQL", "Error in SQL", e);
            return false;
        }
    }
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void register_input() {
        if (ValidateInput())
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                String email = et_register_email.getText().toString().isEmpty()? "NULL" : "'"+et_register_email.getText().toString()+"'";// SQL語法需要加上單引號
                String insertQuery = isUser ? "INSERT INTO 使用者資料 (使用者帳號,使用者密碼,使用者姓名,使用者電話,使用者郵件) VALUES ('" + et_register_account.getText().toString() + "','" + et_register_passwd.getText().toString() + "','" + et_register_name.getText().toString() + "', '" + et_register_phone.getText().toString() + "'," + email + ")" : "INSERT INTO [健身教練資料] ([健身教練帳號],[健身教練密碼],[健身教練姓名],[健身教練電話],[健身教練郵件]) VALUES ('" + et_register_account.getText().toString() + "','" + et_register_passwd.getText().toString() + "','" + et_register_name.getText().toString() + "', '" + et_register_phone.getText().toString() + "'," + email + ")";
                MyConnection.createStatement().executeUpdate(insertQuery);
                setResult(RESULT_OK,getIntent());   //ActivityResultLauncher：https://litotom.com/2017/06/04/ch5-7-activity-result/
                finish();
            } catch (SQLException e) {
                Log.e("SQL", "Error in SQL", e);
                Toast.makeText(this, "資料庫連線錯誤", Toast.LENGTH_SHORT).show();
            }
    }
}