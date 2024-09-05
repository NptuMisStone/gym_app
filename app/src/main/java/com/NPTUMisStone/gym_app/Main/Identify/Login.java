package com.NPTUMisStone.gym_app.Main.Identify;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;
import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.textHint;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import com.NPTUMisStone.gym_app.User_And_Coach.ProgressBarHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
        Executors.newSingleThreadExecutor().execute(() -> {
            ProgressBarHandler progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));
            progressBarHandler.showProgressBar();
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
                progressBarHandler.hideProgressBar();
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
        findViewById(R.id.login_forgot).setOnClickListener(v -> forgotPassword());
        Button login_user = findViewById(R.id.login_user);
        Button login_coach = findViewById(R.id.login_coach);
        login_user.setOnClickListener(v -> change_toUser(login_user, login_coach));
        login_coach.setOnClickListener(v -> change_toCoach(login_user, login_coach));
        set_status(login_user, login_coach);
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
                    Toast.makeText(this, "快用新帳號登入吧", Toast.LENGTH_SHORT).show();    //LENGTH_SHORT and LENGTH_LONG：https://stackoverflow.com/questions/50422727/what-is-different-between-toast-lengthlong-and-lengthshort?noredirect=1&lq=1
                else if (result.getResultCode() == Activity.RESULT_CANCELED)
                    Toast.makeText(this, "取消註冊", Toast.LENGTH_SHORT).show();
            }
    );

    private void set_last_login() {
        user.edit().putInt("last_login", isUser ? User.getInstance().getUserId() : Coach.getInstance().getCoachId())
                .putBoolean("isUser", isUser).apply();
    }

    private void get_last_login(int last_login, ProgressBarHandler progressBarHandler) throws SQLException {
        if (isUser) {
            ResultSet resultSet = MyConnection.createStatement().executeQuery("SELECT * FROM [使用者資料] WHERE [使用者編號] = " + last_login);
            if (resultSet.next()){
                User.setInstance(resultSet.getInt("使用者編號"), resultSet.getString("使用者帳號"), resultSet.getString("使用者姓名"), resultSet.getString("使用者電話"), resultSet.getBoolean("使用者性別"), resultSet.getString("使用者郵件"), resultSet.getBytes("使用者圖片"));
                startActivity(new Intent(this, UserHome.class));
                finish();
            }
        } else {
            ResultSet resultSet = MyConnection.createStatement().executeQuery("SELECT * FROM [健身教練資料] WHERE [健身教練編號] = " + last_login);
            if (resultSet.next()) {
                Coach.setInstance(resultSet.getInt("健身教練編號"), resultSet.getString("健身教練帳號"), resultSet.getString("健身教練姓名"), resultSet.getString("健身教練電話"), resultSet.getBoolean("健身教練性別"), resultSet.getString("健身教練郵件"), resultSet.getBytes("健身教練圖片"));
                startActivity(new Intent(this, CoachHome.class));
                finish();
            }
        }
        progressBarHandler.hideProgressBar();
    }

    private void checkLogin(String account, String password) {
        if(isLoginIn) return;
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
                    boolean isValid = validateCredentials(account, password);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressBarHandler.hideProgressBar();
                        isLoginIn = false;
                        if (isValid) goHome();
                        else Toast.makeText(this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                    });
                } catch (SQLException e) {
                    progressBarHandler.hideProgressBar();
                    isLoginIn = false;
                    throw new RuntimeException(e);
                }
            });
        });
    }
    private void goHome() {
        remember_input();
        set_last_login();
        startActivity(isUser ? new Intent(this, UserHome.class) : new Intent(this, CoachHome.class));
        finish();
    }
    private boolean validateCredentials(String account, String password) throws SQLException {
        String searchQuery = isUser ? "SELECT * FROM [使用者資料] WHERE ([使用者帳號] = ? OR [使用者郵件] = ?) AND [使用者密碼] = ?"
                :"SELECT * FROM [健身教練資料] WHERE ([健身教練帳號] = ? OR [健身教練郵件] = ?) AND [健身教練密碼] = ?";
        PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery);
        searchStatement.setString(1, account);
        searchStatement.setString(2, account);
        searchStatement.setString(3, password);
        ResultSet rs = searchStatement.executeQuery();
        if (rs.next()) {
            setUserOrCoachInstance(rs);
            return true;
        }
        return false;
    }

    private void setUserOrCoachInstance(ResultSet rs) throws SQLException {
        if (isUser)
            User.setInstance(rs.getInt("使用者編號"), rs.getString("使用者帳號"), rs.getString("使用者姓名"), rs.getString("使用者電話"), rs.getBoolean("使用者性別"), rs.getString("使用者郵件"), rs.getBytes("使用者圖片"));
        else
            Coach.setInstance(rs.getInt("健身教練編號"), rs.getString("健身教練帳號"), rs.getString("健身教練姓名"), rs.getString("健身教練電話"), rs.getBoolean("健身教練性別"), rs.getString("健身教練郵件"), rs.getBytes("健身教練圖片"));
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

    private void change_toUser(Button login_user, Button login_coach) {
        isUser = true;
        set_status(login_user, login_coach);
    }
    private void change_toCoach(Button login_user, Button login_coach) {
        isUser = false;
        set_status(login_user, login_coach);
    }

    private void set_status(Button login_user, Button login_coach) {
        login_user.setSelected(isUser);
        login_coach.setSelected(!isUser);
    }

    boolean isDialogShow = false;

    private void forgotPassword() {
        if (isDialogShow) return;
        isDialogShow = true;
        View dialogView = getLayoutInflater().inflate(R.layout.main_login_forget, null);
        ((TextView) dialogView.findViewById(R.id.forget_title)).setText(isUser ? "用戶密碼重設" : "教練密碼重設");
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        dialogView.findViewById(R.id.forget_getButton).setOnClickListener(v -> handleGetCodeClick(dialogView, dialog));
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
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show();
    }
    CountDownTimer countDownTimer;
    private void handleGetCodeClick(View dialogView, AlertDialog dialog) {
        EditText editAccount = dialogView.findViewById(R.id.forget_accountEdit);
        EditText editEmail = dialogView.findViewById(R.id.forget_emailEdit);
        String inputAccount = editAccount.getText().toString();
        String userEmail = editEmail.getText().toString();
        TextView statusHint1 = dialogView.findViewById(R.id.forget_statusHint1);
        Button getCode = dialogView.findViewById(R.id.forget_getButton);
        if (validateEmailAndAccount(inputAccount, userEmail, statusHint1))
            sendVerificationEmail(userEmail, statusHint1, getCode, dialogView, dialog);
    }

    private boolean validateEmailAndAccount(String account, String email, TextView statusHint1) {
        if (account.isEmpty()) {
            textHint(statusHint1, "❌ 請輸入帳號");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textHint(statusHint1, "❌ 信箱格式不正確");
            return false;
        }
        return checkAccountExists(account, email, statusHint1);
    }
    private void sendVerificationEmail(String userEmail, TextView statusHint1, Button getCode, View dialogView, AlertDialog dialog) {
        String randomCode = generateRandomNumber();
        String mSubject = "【益身GYM】驗證碼";
        String mMessage = "您的驗證碼為：" + randomCode + "\n\n有效期限為10分鐘，請務必妥善保管驗證碼，勿將其告知他人。逾時請返回APP重新發送驗證信，感謝您。";
        new JavaMailAPI(this, userEmail, mSubject, mMessage).sendMail(new JavaMailAPI.EmailSendResultCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {   //runOnUiThread：https://blog.csdn.net/gao511147456/article/details/120881181
                    textHint(statusHint1, "✔ 驗證碼已成功寄送，請前往您的信箱查看");
                    LocalTime sendTime = LocalTime.now(ZoneId.of("Asia/Taipei"));
                    if (countDownTimer != null) countDownTimer.cancel();
                    countDownTimer = startCountdown(getCode);
                    setupCodeValidation(dialogView, randomCode, sendTime, dialog);
                });
            }
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    textHint(statusHint1, "❌ 郵件發送失敗，請再試一次。");
                    Log.e("Email：", "無法發送郵件！", e);
                });
            }
        });
    }
    private void setupCodeValidation(View dialogView, String randomCode, LocalTime sendTime, AlertDialog dialog) {
        Button checkButton = dialogView.findViewById(R.id.forget_checkButton);
        EditText editAccount = dialogView.findViewById(R.id.forget_accountEdit);
        EditText editCode = dialogView.findViewById(R.id.forget_codeEdit);
        checkButton.setVisibility(View.VISIBLE);
        checkButton.setOnClickListener(v -> {
            if (validateCode(randomCode, editCode.getText().toString(), dialogView.findViewById(R.id.forget_statusHint1), sendTime)) {
                new Handler(getMainLooper()).postDelayed(() -> {
                    dialogView.findViewById(R.id.forget_linear1).setVisibility(View.GONE);
                    dialogView.findViewById(R.id.forget_linear2).setVisibility(View.VISIBLE);
                }, 1000);
                dialogView.findViewById(R.id.forget_returnButton).setOnClickListener(view -> handleResetPasswordClick(dialogView.findViewById(R.id.forget_newEdit), dialogView.findViewById(R.id.forget_checkEdit), editAccount.getText().toString(), dialogView.findViewById(R.id.forget_statusHint2), dialog));
            }
        });
    }
    private boolean validateCode(String randomCode, String inputCode, TextView statusHint1, LocalTime sendTime) {
        LocalTime nowTime = LocalTime.now(ZoneId.of("Asia/Taipei"));
        Duration duration = Duration.between(sendTime, nowTime);
        if (duration.toMinutes() > 10 && duration.toMinutes() <= 0 && inputCode.equals(randomCode)) {
            textHint(statusHint1, "❌ 驗證碼已過期，請再獲取一次。");
            return false;
        } else if (!inputCode.equals(randomCode)) {
            textHint(statusHint1, "❌ 驗證碼有誤，請再輸入一次。");
            return false;
        } else
            textHint(statusHint1, "✔ 驗證碼驗證成功");
        return true;
    }
    private boolean checkAccountExists(String account, String email, TextView statusHint1) {
        String searchQuery = isUser ? "SELECT * FROM [使用者資料] WHERE [使用者帳號] = '" + account + "' AND [使用者郵件] = '" + email + "'" : "SELECT * FROM [健身教練資料] WHERE [健身教練帳號] = '" + account + "' AND [健身教練郵件] = '" + email + "'";
        try {
            ResultSet rs = MyConnection.createStatement().executeQuery(searchQuery);
            if (rs.next()) return true;
            else textHint(statusHint1, "❌ 找不到此帳號。請確認您輸入的帳號是否正確。");
        } catch (SQLException e) {
            textHint(statusHint1, "❌ 資料庫錯誤");
        }
        return false;
    }

    private String generateRandomNumber() {
        int length = 6; // 驗證碼長度    //ThreadLocalRandom 產生隨機數方法：https://www.cnblogs.com/txmfz/p/14756355.html
        return ThreadLocalRandom.current().ints(length, 0, 10)
                .mapToObj(Integer::toString)    //Random、ThreadLocalRandom、SecureRandom：https://www.cnblogs.com/shoufeng/p/14853376.html
                .collect(Collectors.joining()); //比較 ThreadLocalRandom 和 Random：https://www.twle.cn/c/yufei/javatm/javatm-basic-threadLocalrandom.html
    }

    private CountDownTimer startCountdown(Button getCode) {
        return new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                getCode.setText(String.format(Locale.TAIWAN, "重新獲取\n(%d 秒)", millisUntilFinished / 1000 + 1));
                getCode.setEnabled(false); // 禁用按钮
                getCode.setTextSize(12);
            }
            public void onFinish() {
                getCode.setText("獲取");
                getCode.setEnabled(true); // 啟用按钮
                getCode.setTextSize(14);
            }
        }.start();
    }

    private void handleResetPasswordClick(EditText newPass, EditText checkPass, String account, TextView statusHint2, AlertDialog dialog) {
        if (!validatePasswords(newPass, checkPass, statusHint2)) return;
        if (updatePassword(account, newPass.getText().toString(), statusHint2))
            new Handler(getMainLooper()).postDelayed(dialog::dismiss, 1000);
    }

    private boolean validatePasswords(EditText newPass, EditText checkPass, TextView statusHint) {
        String newPassword = newPass.getText().toString();
        String checkPassword = checkPass.getText().toString();
        if (newPassword.isEmpty()) {
            textHint(statusHint, "❌ 請輸入新密碼");
            return false;
        }
        if (checkPassword.isEmpty()) {
            textHint(statusHint, "❌ 請再次輸入新密碼");
            return false;
        }
        if (newPassword.length() < 6) {
            textHint(statusHint, "❌ 密碼長度需大於六個字元");
            return false;
        }
        if (!checkPassword.equals(newPassword)) {
            textHint(statusHint, "❌ 兩次輸入的密碼不同");
            return false;
        }
        return true;
    }

    private boolean updatePassword(String account, String newPassword, TextView statusHint) {
        try {
            String updateQuery = isUser ? "UPDATE [使用者資料] SET [使用者密碼] = ? WHERE [使用者帳號] = ?" : "UPDATE [健身教練資料] SET [健身教練密碼] = ? WHERE [健身教練帳號] = ?";
            PreparedStatement updateStatement = MyConnection.prepareStatement(updateQuery);
            updateStatement.setString(1, newPassword);
            updateStatement.setString(2, account);
            if (updateStatement.executeUpdate() > 0) {
                textHint(statusHint, "✔ 密碼已成功重置");
                return true;
            } else {
                textHint(statusHint, "❌ 密碼重置失敗");
                return false;
            }
        } catch (SQLException e) {
            textHint(statusHint, "❌ 資料庫錯誤，無法更新密碼");
            return false;
        }
    }
}