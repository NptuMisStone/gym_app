package com.NPTUMisStone.gym_app.User_And_Coach;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Validator {
    private final Connection MyConnection;

    public Validator(Connection MyConnection) {
        this.MyConnection = MyConnection;
    }

    // statusHint如果是TextView，則會在錯誤訊息前加上❌，如果是null則不會加上
    public String validateEmailAndAccount(String account, String email, boolean isUser, TextView statusHint) {
        String result = validateEmailAndAccount(account, email, isUser);
        return addPrefixIfTextView(result, statusHint);
    }

    public String validatePasswords(EditText newPass, EditText checkPass, TextView statusHint) {
        String result = validatePasswords(newPass, checkPass);
        return addPrefixIfTextView(result, statusHint);
    }

    private String validateEmailAndAccount(String account, String email, boolean isUser) {
        if (account.isEmpty()) {
            return "請輸入帳號";
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "信箱格式不正確";
        }
        return checkAccountExists(account, email, isUser);
    }

    private String validatePasswords(EditText newPass, EditText checkPass) {
        String newPassword = newPass.getText().toString();
        String checkPassword = checkPass.getText().toString();
        if (newPassword.isEmpty()) {
            return "請輸入新密碼";
        }
        if (checkPassword.isEmpty()) {
            return "請再次輸入新密碼";
        }
        if (newPassword.length() < 6) {
            return "密碼長度需大於六個字元";
        }
        if (!checkPassword.equals(newPassword)) {
            return "兩次輸入的密碼不同";
        }
        return null;
    }

    private String checkAccountExists(String account, String email, boolean isUser) {
        String searchQuery = isUser ? "SELECT * FROM [使用者資料] WHERE [使用者帳號] = '" + account + "' AND [使用者郵件] = '" + email + "'" : "SELECT * FROM [健身教練資料] WHERE [健身教練帳號] = '" + account + "' AND [健身教練郵件] = '" + email + "'";
        try {
            ResultSet rs = MyConnection.createStatement().executeQuery(searchQuery);
            if (rs.next()) {
                return null;
            } else {
                return "找不到此帳號。請確認您輸入的帳號是否正確。";
            }
        } catch (SQLException e) {
            return "資料庫錯誤";
        }
    }

    public String checkInput(EditText editText, String fieldName, int maxLength, TextView statusHint) {
        String result = checkInput(editText, fieldName, maxLength);
        return addPrefixIfTextView(result, statusHint);
    }

    private String checkInput(EditText editText, String fieldName, int maxLength) {
        if (editText.getText().toString().trim().isEmpty()) {
            return "請輸入" + fieldName;
        } else if (editText.getText().toString().length() > maxLength) {
            return fieldName + "過長";
        }
        return null;
    }

    public String checkEmail(EditText editText, TextView statusHint) {
        String result = checkEmail(editText);
        return addPrefixIfTextView(result, statusHint);
    }

    private String checkEmail(EditText editText) {
        if (!isEmailValid(editText.getText().toString())) {
            return "請輸入有效的電子郵件";
        }
        return null;
    }

    public String checkPhone(EditText editText, TextView statusHint) {
        String result = checkPhone(editText);
        return addPrefixIfTextView(result, statusHint);
    }

    private String checkPhone(EditText editText) {
        if (editText.getText().toString().length() != 10) {
            return "請輸入正確的聯絡電話";
        }
        return null;
    }

    public String checkAccount(EditText editText, String currentAccount, TextView statusHint) {
        String result = checkAccount(editText, currentAccount);
        return addPrefixIfTextView(result, statusHint);
    }

    private String checkAccount(EditText editText, String currentAccount) {
        try {
            if (!editText.getText().toString().equals(currentAccount)) {
                String check_account = "SELECT * FROM 使用者資料 WHERE 使用者帳號 ='" + editText.getText().toString() + "'";
                ResultSet account_result = MyConnection.createStatement().executeQuery(check_account);
                if (account_result.next()) {
                    return "此帳號已被使用";
                }
            }
        } catch (Exception e) {
            Log.e("SQL", "Error in SQL", e);
            return "SQL錯誤";
        }
        return null;
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private String addPrefixIfTextView(String result, TextView statusHint) {
        if (result != null && statusHint != null && !result.startsWith("❌")) {
            return "❌ " + result;
        }
        return result;
    }
}