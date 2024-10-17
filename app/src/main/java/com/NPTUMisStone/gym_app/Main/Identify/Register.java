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
import com.NPTUMisStone.gym_app.User_And_Coach.Validator;

import java.sql.Connection;
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
    private boolean ValidateInput() {
        boolean isValid = true;
        Validator validator = new Validator(MyConnection);
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