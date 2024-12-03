package com.NPTUMisStone.gym_app.User_And_Coach;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

// statusHint如果是TextView，則會在錯誤訊息前加上❌，如果是null則不會加上
public class Validator {
    private final Connection MyConnection;

    public Validator(Connection MyConnection) {
        this.MyConnection = MyConnection;
    }

    public String validateEmailAndAccount(EditText account, EditText email, TextView statusHint) {
        String result = validateEmailAndAccount(account, email);
        return addPrefixIfTextView(result, statusHint);
    }

    private String validateEmailAndAccount(EditText account, EditText email) {
        String validateResult = checkInput(account, "帳號", 20);
        if (validateResult != null) return validateResult;
        validateResult = checkEmail(email);
        if (validateResult != null) return validateResult;

        // 更新查詢邏輯
        return checkAccountExists(account.getText().toString(), email.getText().toString());
    }

    private String checkAccountExists(String account, String email) {
        String searchQuery =
                "SELECT 1 FROM [使用者資料] WHERE [使用者帳號] = ? AND [使用者郵件] = ? " +
                        "UNION " +
                        "SELECT 1 FROM [健身教練資料] WHERE [健身教練帳號] = ? AND [健身教練郵件] = ?";
        try {
            PreparedStatement statement = MyConnection.prepareStatement(searchQuery);
            statement.setString(1, account);
            statement.setString(2, email);
            statement.setString(3, account);
            statement.setString(4, email);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return null; // 資料存在
            else return "找不到此帳號，請確認輸入是否正確。";
        } catch (SQLException e) {
            return "資料庫錯誤，請稍後再試。";
        }
    }
    public String validatePasswords(EditText newPass, TextView statusHint) {
        String result = validatePasswords(newPass);
        return addPrefixIfTextView(result, statusHint);
    }
    private String validatePasswords(EditText newPass){
        String newPassword = newPass.getText().toString();
        if (newPassword.length() < 6) return "密碼長度需大於六個字元";
        if (checkInput(newPass, "密碼", 20) != null) return checkInput(newPass, "密碼", 20);
        return checkPasswordSecurity(newPassword);
    }
    private String checkPasswordSecurity(String password) {
        try {
            // Hash the password using SHA-1
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : hashBytes) {
                hash.append(String.format("%02x", b));
            }
            String hashString = hash.toString().toUpperCase();
            String prefix = hashString.substring(0, 5);
            String suffix = hashString.substring(5);
            // Check the hash against the HIBP API
            URL url = new URL("https://api.pwnedpasswords.com/range/" + prefix);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            Scanner scanner = new Scanner(conn.getInputStream());
            boolean found = false;
            int count = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(suffix)) {
                    found = true;
                    count = Integer.parseInt(line.split(":")[1]);
                    break;
                }
            }
            scanner.close();
            if (found) return "警告!此密碼在 HIBP 資料庫出現 " + count + " 次，不建議使用";
            else {
                // Estimate brute-force time
                /*int charPool = 0;
                if (Pattern.compile("[0-9]").matcher(password).find()) charPool += 10;
                if (Pattern.compile("[a-z]").matcher(password).find()) charPool += 26;
                if (Pattern.compile("[A-Z]").matcher(password).find()) charPool += 26;
                if (Pattern.compile("[^0-9a-zA-Z]").matcher(password).find()) charPool += 32;
                int length = password.length();
                BigInteger countCombinations = BigInteger.valueOf(charPool).pow(length);
                BigInteger rtx4090Power = new BigInteger("21791700000"); // SHA2-256 21791.7 MH/s
                BigInteger seconds = countCombinations.divide(rtx4090Power);
                String time;
                if (seconds.compareTo(BigInteger.valueOf(60)) < 0) {
                    time = seconds + " 秒";
                } else if (seconds.compareTo(BigInteger.valueOf(3600)) < 0) {
                    time = seconds.divide(BigInteger.valueOf(60)) + " 分鐘";
                } else if (seconds.compareTo(BigInteger.valueOf(86400)) < 0) {
                    time = seconds.divide(BigInteger.valueOf(3600)) + " 小時";
                } else if (seconds.compareTo(BigInteger.valueOf(86400 * 365)) < 0) {
                    time = seconds.divide(BigInteger.valueOf(86400)) + " 天";
                } else {
                    time = seconds.divide(BigInteger.valueOf(86400 * 365)).toString() + " 年";
                }
                return "呼~ HIBP 資料庫未收錄此密碼。長度 " + length + " 預估暴力破解時間約：" + time + " (NVIDIA RTX-4090 / SHA256)";*/
                return null;
            }
        } catch (Exception e) {
            return "檢查密碼安全性時發生錯誤: " + e.getMessage();
        }
    }
    public String validateAgainPasswords(EditText newPass, EditText checkPass, TextView statusHint) {
        String result = validateAgainPasswords(newPass, checkPass);
        return addPrefixIfTextView(result, statusHint);
    }
    private String validateAgainPasswords(EditText newPass, EditText checkPass) {
        String newPassword = newPass.getText().toString();
        String checkPassword = checkPass.getText().toString();
        if (checkPassword.isEmpty()) return "請再次輸入新密碼";
        if (!checkPassword.equals(newPassword)) return "兩次輸入的密碼不同";
        return null;
    }
    /*private String validateEmailAndAccount(String account, String email, boolean isUser) {
        if (account.isEmpty())
            return "請輸入帳號";
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "信箱格式不正確";
        return checkAccountExists(account, email, isUser);
    }*/

    public String checkInput(EditText editText, String fieldName, int maxLength, TextView statusHint) {
        String result = checkInput(editText, fieldName, maxLength);
        return addPrefixIfTextView(result, statusHint);
    }

    private String checkInput(EditText editText, String fieldName, int maxLength) {
        if (editText.getText().toString().trim().isEmpty())
            return "請輸入" + fieldName;
        else if (editText.getText().toString().length() > maxLength)
            return fieldName + "過長";
        return null;
    }

    public String checkEmail(EditText editText, TextView statusHint) {
        String result = checkEmail(editText);
        return addPrefixIfTextView(result, statusHint);
    }

    private String checkEmail(EditText editText) {
        String validateResult = checkInput(editText, "電子郵件", 30);
        if (validateResult != null) return validateResult;
        boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches();
        if (!isEmailValid) return "請輸入有效的電子郵件";
        return null;
    }

    public String checkPhone(EditText editText, TextView statusHint) {
        String result = checkPhone(editText);
        return addPrefixIfTextView(result, statusHint);
    }

    private String checkPhone(EditText editText) {
        if (editText.getText().toString().length() != 10) return "請輸入正確的聯絡電話";
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
                if (account_result.next())
                    return "此帳號已被使用";
            }
        } catch (Exception e) {
            Log.e("SQL", "Error in SQL", e);
            return "SQL錯誤";
        }
        return null;
    }

    private String addPrefixIfTextView(String result, TextView statusHint) {
        if (result != null && statusHint != null && !result.startsWith("❌"))
            return "❌ " + result;
        return result;
    }
}