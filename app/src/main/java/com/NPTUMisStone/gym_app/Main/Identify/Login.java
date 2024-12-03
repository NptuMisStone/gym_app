package com.NPTUMisStone.gym_app.Main.Identify;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Coach.Main.CoachHome;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User.Main.UserHome;
import com.NPTUMisStone.gym_app.User_And_Coach.PasswordReset;
import com.NPTUMisStone.gym_app.User_And_Coach.ProgressBarHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity {
    Connection MyConnection;
    Boolean isUser;
    CheckBox cb_remember;
    EditText et_account, et_password;
    SharedPreferences user;
    boolean isLoginIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initFirst();
    }
    private void initFirst() {
        ProgressBarHandler progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));
        runOnUiThread(progressBarHandler::showProgressBar);
        Executors.newSingleThreadExecutor().execute(() -> {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            user = getSharedPreferences("remember", MODE_PRIVATE);
            isUser = user.getBoolean("isUser", false);
            int lastLogin = user.getInt("last_login", -1);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (lastLogin != -1) {
                    try {
                        get_last_login(lastLogin, progressBarHandler);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                initSecond();
                runOnUiThread(progressBarHandler::hideProgressBar);
            });
        });
    }
    private void initSecond() {
        et_account = findViewById(R.id.login_account);
        et_password = findViewById(R.id.login_password);
        cb_remember = findViewById(R.id.login_remember);
        ImageView ivShowPassword = findViewById(R.id.login_show_password);
        ivShowPassword.setOnClickListener(v -> change_password_visibility(ivShowPassword));
        remember_output();
        findViewById(R.id.login_register).setOnClickListener(v -> register());
        findViewById(R.id.login_button).setOnClickListener(v -> checkLogin(et_account.getText().toString(), et_password.getText().toString()));
        findViewById(R.id.login_forgot).setOnClickListener(v -> new PasswordReset(this,  MyConnection).showPasswordResetDialog());

    }

    private void change_password_visibility(ImageView ivShowPassword) {
        ivShowPassword.bringToFront();  //bringToFront：https://stackoverflow.com/questions/44351354/android-constraintlayout-put-one-view-on-top-of-another-view
        if (et_password.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivShowPassword.setImageResource(R.drawable.main_login_ic_password_invisible);
        } else { //Change Password Visibility：https://stackoverflow.com/questions/3685790/how-to-switch-between-hide-and-view-password
            et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivShowPassword.setImageResource(R.drawable.main_login_ic_password_visible);
        }
    }
    private void register() {
        registerActivityResult.launch(new Intent(this, Register.class));
    }
    ActivityResultLauncher<Intent> registerActivityResult = registerForActivityResult(  //ActivityResultLauncher：https://litotom.com/2017/06/04/ch5-7-activity-result/
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK)
                    Toast.makeText(this, "註冊成功！快用新帳號登入吧", Toast.LENGTH_SHORT).show();    //LENGTH_SHORT and LENGTH_LONG：https://stackoverflow.com/questions/50422727/what-is-different-between-toast-lengthlong-and-lengthshort?noredirect=1&lq=1
            }
    );

    private void get_last_login(int last_login, ProgressBarHandler progressBarHandler) throws SQLException {
        if (isUser) {
            ResultSet resultSet = MyConnection.createStatement().executeQuery("SELECT * FROM [使用者資料] WHERE [使用者編號] = " + last_login);
            if (resultSet.next()){
                User.setInstance(resultSet.getInt("使用者編號"), resultSet.getString("使用者帳號"), resultSet.getString("使用者姓名"), resultSet.getString("使用者電話"), resultSet.getInt("使用者性別"), resultSet.getString("使用者郵件"), resultSet.getBytes("使用者圖片"));
                startActivity(new Intent(this, UserHome.class));
                finish();
            }
        } else {
            ResultSet resultSet = MyConnection.createStatement().executeQuery("SELECT * FROM [健身教練資料] WHERE [健身教練編號] = " + last_login);
            if (resultSet.next()) {
                Coach.setInstance(resultSet.getInt("健身教練編號"), resultSet.getString("健身教練帳號"), resultSet.getString("健身教練姓名"), resultSet.getString("健身教練電話"), resultSet.getInt("健身教練性別"), resultSet.getString("健身教練郵件"), resultSet.getBytes("健身教練圖片"),resultSet.getString("健身教練介紹"));
                startActivity(new Intent(this, CoachHome.class));
                finish();
            }
        }
        progressBarHandler.hideProgressBar();
    }

    private void checkLogin(String account, String password) {
        if (isLoginIn) return;
        isLoginIn = true;

        if (account.isEmpty() || password.isEmpty()) {
            if (password.isEmpty()) editHint(et_password, "請輸入密碼");
            if (account.isEmpty()) editHint(et_account, "請輸入帳號");
            isLoginIn = false;
            return;
        }

        runOnUiThread(() -> {
            ProgressBarHandler progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));
            progressBarHandler.showProgressBar();
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // 驗證帳號密碼
                    boolean isValid = validateCredentials(account, password);

                    if (isValid && !isUser) {
                        // 如果是教練，執行合約檢查
                        int coachId = Coach.getInstance().getCoachId();
                        boolean isContractValid = checkCoachDateDuringLogin(coachId);

                        // 如果合約檢查失敗，退出
                        if (!isContractValid) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                progressBarHandler.hideProgressBar();
                                isLoginIn = false;
                            });
                            return;
                        }
                    }

                    // 登入完成，進入主頁面
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressBarHandler.hideProgressBar();
                        isLoginIn = false;
                        if (isValid) goHome();
                        else Toast.makeText(this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                    });
                } catch (SQLException e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressBarHandler.hideProgressBar();
                        isLoginIn = false;
                        Toast.makeText(this, "❌ 資料庫錯誤", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
    private boolean checkCoachDateDuringLogin(int coachId) {
        try {
            if (MyConnection == null || MyConnection.isClosed()) {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            }

            String query = "SELECT 合約到期日, 審核狀態 FROM 健身教練審核 WHERE 健身教練編號 = ?";
            PreparedStatement checkStatement = MyConnection.prepareStatement(query);
            checkStatement.setInt(1, coachId);

            ResultSet rs = checkStatement.executeQuery();
            if (rs.next()) {
                java.sql.Date contractEndDate = rs.getDate("合約到期日");
                int currentStatus = rs.getInt("審核狀態");
                rs.close();
                checkStatement.close();

                if (contractEndDate == null || currentStatus==0) {
                    Log.d("CheckCoachDate", "合約到期日為空，代表有資料未審核");
                    showDialog("審核中", "我們正在處理您的教練身分審核，這可能需要一些時間，審核通過將會發送電子郵件至您的電子郵件信箱！感謝您的耐心等候！如有任何問題，請聯繫我們。");
                    return false;
                }

                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                if (contractEndDate.before(today) || contractEndDate.equals(today)) {
                    if (currentStatus == 1) {
                        String updateQuery = "UPDATE 健身教練審核 SET 審核狀態 = ? WHERE 健身教練編號 = ?";
                        PreparedStatement updateStatement = MyConnection.prepareStatement(updateQuery);
                        updateStatement.setInt(1, 3);
                        updateStatement.setInt(2, coachId);
                        updateStatement.executeUpdate();
                        updateStatement.close();
                        showDialog("合約已過期", "您的合約已過期，請使用電腦網頁版重新提出審核！");
                        return false;
                    } else {
                        Log.d("CheckCoachDate", "合約已過期但審核狀態不變");
                        showDialog("合約已過期", "您的合約已過期，請使用電腦網頁版重新提出審核！");
                        return false;
                    }
                } else {
                    Log.d("CheckCoachDate", "合約有效");
                    return true; // 合約有效
                }
            } else {
                rs.close();
                checkStatement.close();
                showDialog("立即驗證健身教練身分！", "提醒您，為了保障平台使用者的安全與信任，教練必須先完成身分驗證才能在平台上被用戶搜尋及預約。如未驗證，您的資訊將不會被上架顯示。\n\n請使用電腦網頁版進行審核！");
                return false;
            }
        } catch (SQLException e) {
            Log.e("CheckCoachDate", "資料庫查詢失敗", e);
            showDialog("錯誤", "無法檢查您的合約狀態，請稍後再試或聯絡管理員。");
            return false;
        }
    }
    private void showDialog(String title, String message) {
        runOnUiThread(() -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("確定", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
    private void goHome() {
        remember_input();
        //設定最後登入的使用者或教練
        user.edit().putInt("last_login", isUser ? User.getInstance().getUserId() : Coach.getInstance().getCoachId()).putBoolean("isUser", isUser).apply();
        //開啟使用者或教練的主頁面
        startActivity(isUser ? new Intent(this, UserHome.class) : new Intent(this, CoachHome.class));
        finish();
    }
    private boolean validateCredentials(String account, String password) throws SQLException {
        if (MyConnection == null || MyConnection.isClosed())
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();

        // 檢查是否為使用者
        String userQuery = "SELECT * FROM [使用者資料] WHERE ([使用者帳號] = ? OR [使用者郵件] = ?) AND [使用者密碼] = ?";
        PreparedStatement userStatement = MyConnection.prepareStatement(userQuery);
        userStatement.setString(1, account);
        userStatement.setString(2, account);
        userStatement.setString(3, password);
        ResultSet userResultSet = userStatement.executeQuery();

        if (userResultSet.next()) {
            isUser = true;
            setUserOrCoachInstance(userResultSet);
            return true;
        }

        // 檢查是否為教練
        String coachQuery = "SELECT * FROM [健身教練資料] WHERE ([健身教練帳號] = ? OR [健身教練郵件] = ?) AND [健身教練密碼] = ?";
        PreparedStatement coachStatement = MyConnection.prepareStatement(coachQuery);
        coachStatement.setString(1, account);
        coachStatement.setString(2, account);
        coachStatement.setString(3, password);
        ResultSet coachResultSet = coachStatement.executeQuery();

        if (coachResultSet.next()) {
            isUser = false;
            setUserOrCoachInstance(coachResultSet);
            return true;
        }

        return false; // 帳號密碼都不正確
    }


    private void setUserOrCoachInstance(ResultSet rs) throws SQLException {
        if (isUser)
            User.setInstance(rs.getInt("使用者編號"), rs.getString("使用者帳號"), rs.getString("使用者姓名"), rs.getString("使用者電話"), rs.getInt("使用者性別"), rs.getString("使用者郵件"), rs.getBytes("使用者圖片"));
        else
            Coach.setInstance(rs.getInt("健身教練編號"), rs.getString("健身教練帳號"), rs.getString("健身教練姓名"), rs.getString("健身教練電話"), rs.getInt("健身教練性別"), rs.getString("健身教練郵件"), rs.getBytes("健身教練圖片"),rs.getString("健身教練介紹"));
    }

    private void remember_input() {
        if (cb_remember.isChecked()) //取得checkbox的狀態 //存入帳號資料 // 存入Remember狀態
            user.edit().putString("Remember_account", et_account.getText().toString()).putBoolean("isCheck", true).apply();
        else   // 清除記住的資料  // 存入Remember狀態
            user.edit().remove("Remember_account").putBoolean("isCheck", false).apply();
    }

    private void remember_output() {
        et_account.setText(user.getString("Remember_account", "")); //若沒取得就是沒任何東西 ，有取到則顯示在帳號上
        cb_remember.setChecked(user.getBoolean("isCheck", false));  //取得上次Input的Remember狀態   //顯示上次Remember狀態
    }

        //Android 驗證碼輸入框的實現：https://www.jianshu.com/p/3238a5afc21c
        /*VerifyCodeView verifyCodeView = dialogView.findViewById(R.id.verify_code_view);
        verifyCodeView.setInputCompleteListener(new VerifyCodeView.InputCompleteListener() {
            @Override
            public void inputComplete() {
                Toast.makeText(LoginActivity.this, "inputComplete: " + verifyCodeView.getEditContent(), Toast.LENGTH_SHORT).show();
                Log.d("VerifyCodeView", "inputComplete: " + verifyCodeView.getEditContent());
            }

            @Override
            public void invalidContent() {
                Toast.makeText(LoginActivity.this, "invalidContent: " + verifyCodeView.getEditContent(), Toast.LENGTH_SHORT).show();
                Log.d("VerifyCodeView", "invalidContent: " + verifyCodeView.getEditContent());
            }
        });*/
}