package com.NPTUMisStone.gym_app.User_And_Coach.Helper;

import static com.NPTUMisStone.gym_app.User_And_Coach.Helper.ErrorHints.textHint;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.NPTUMisStone.gym_app.Main.Identify.JavaMailAPI;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PasswordReset {
    Context context;
    Connection MyConnection;
    boolean isDialogShow = false;
    Validator validator;
    CountDownTimer countDownTimer;
    Handler handler;
    Runnable runnable;

    public PasswordReset(Context context, Connection MyConnection) {
        this.context = context;
        this.MyConnection = MyConnection;
        this.validator = new Validator(MyConnection);
    }

    public void showPasswordResetDialog() {
        if (isDialogShow) return;
        isDialogShow = true;
        View dialogView = ((Activity) context).getLayoutInflater().inflate(R.layout.main_login_forget, null);
        AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).create();
        dialogView.findViewById(R.id.forget_getButton).setOnClickListener(v -> handleGetCodeClick(dialogView, dialog));
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show();
    }

    private void handleGetCodeClick(View dialogView, AlertDialog dialog) {
        EditText editAccount = dialogView.findViewById(R.id.forget_accountEdit);
        EditText editEmail = dialogView.findViewById(R.id.forget_emailEdit);
        TextView statusHint = dialogView.findViewById(R.id.forget_statusHint);
        Button getCode = dialogView.findViewById(R.id.forget_getButton);

        String validationError = validator.validateEmailAndAccount(editAccount, editEmail, statusHint);
        if (validationError == null) {
            if (isUserOrCoachExists(editAccount.getText().toString(), editEmail.getText().toString())) {
                sendVerificationEmail(editEmail.getText().toString(), statusHint, getCode, dialogView, dialog);
            } else {
                textHint(statusHint, "❌ 帳號或 Email 不存在，請確認後再試。");
            }
        } else {
            textHint(statusHint, validationError);
        }
    }
    private boolean isUserOrCoachExists(String account, String email) {
        try {
            String query = "SELECT 1 FROM [使用者資料] WHERE [使用者帳號] = ? OR [使用者郵件] = ? " +
                    "UNION SELECT 1 FROM [健身教練資料] WHERE [健身教練帳號] = ? OR [健身教練郵件] = ?";
            PreparedStatement statement = MyConnection.prepareStatement(query);
            statement.setString(1, account);
            statement.setString(2, email);
            statement.setString(3, account);
            statement.setString(4, email);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // 如果有結果，說明存在
        } catch (SQLException e) {
            Log.e("PasswordReset", "查詢帳號或 Email 發生錯誤", e);
            return false;
        }
    }

    private void sendVerificationEmail(String userEmail, TextView statusHint, Button getCode, View dialogView, AlertDialog dialog) {
        animateTextHint(statusHint);
        String randomCode = generateRandomNumber();
        Log.d("randomCode", randomCode);
        String mSubject = "【屏大Fit-健身預約系統】密碼重設通知信";
        String mMessage = "我們收到您重設密碼的請求。\n\n" +
                "您的驗證碼：" + randomCode + "\n\n" +
                "有效期限為3分鐘，請妥善保管驗證碼，勿將其告知他人。\n\n" +
                "如果這不是您的操作，請忽略這封郵件。\n\n" +
                "（本郵件是由系統自動寄發，請勿直接回覆，謝謝。）\n\n" +
                "屏大Fit 團隊\n" +
                "NptuMisStone@gmail.com";
        new JavaMailAPI(context, userEmail, mSubject, mMessage).sendMail(new JavaMailAPI.EmailSendResultCallback() {
            @Override
            public void onSuccess() {
                ((Activity) context).runOnUiThread(() -> {
                    stopAnimateTextHint();
                    textHint(statusHint, "✔ 驗證碼已成功寄送，請前往您的信箱查看");
                    LocalTime sendTime = LocalTime.now(ZoneId.of("Asia/Taipei"));
                    if (countDownTimer != null) countDownTimer.cancel();
                    countDownTimer = startCountdown(getCode);
                    setupCodeValidation(dialogView, randomCode, sendTime, dialog);
                });
            }

            @Override
            public void onFailure(Exception e) {
                stopAnimateTextHint();
                ((Activity) context).runOnUiThread(() -> textHint(statusHint, "❌ 郵件發送失敗，請再試一次。"));
                Log.e("EmailSendError", Objects.requireNonNull(e.getMessage()));
            }
        });
    }

    private void animateTextHint(TextView statusHint1) {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            private boolean toggle = true;
            @Override
            public void run() {
                statusHint1.setVisibility(View.VISIBLE);
                statusHint1.setText(toggle ? "⏳確認中..." : "⌛確認中...");
                toggle = !toggle;
                handler.postDelayed(this, 500);
            }
        };
        handler.post(runnable);
    }

    private void stopAnimateTextHint() {
        if (handler != null && runnable != null) handler.removeCallbacks(runnable);
    }

    private void setupCodeValidation(View dialogView, String randomCode, LocalTime sendTime, AlertDialog dialog) {
        Button checkButton = dialogView.findViewById(R.id.forget_checkButton);
        EditText editAccount = dialogView.findViewById(R.id.forget_accountEdit);
        EditText editCode = dialogView.findViewById(R.id.forget_codeEdit);
        checkButton.setVisibility(View.VISIBLE);
        checkButton.setOnClickListener(v -> {
            if (validateCode(randomCode, editCode.getText().toString(), dialogView.findViewById(R.id.forget_statusHint), sendTime)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
        if (duration.toMinutes() > 3 || duration.toMinutes() <= 0 && !inputCode.equals(randomCode)) {
            textHint(statusHint1, duration.toMinutes() > 3 ? "❌ 驗證碼已過期，請再獲取一次。" : "❌ 驗證碼有誤，請再輸入一次。");
            return false;
        }
        textHint(statusHint1, "✔ 驗證碼驗證成功");
        return true;
    }

    private String generateRandomNumber() {
        int length = 6;
        return ThreadLocalRandom.current().ints(length, 0, 10)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining());
    }

    private CountDownTimer startCountdown(Button getCode) {
        return new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                getCode.setText(String.format(Locale.TAIWAN, "重新獲取\n(%d 秒)", millisUntilFinished / 1000 + 1));
                getCode.setEnabled(false);
                getCode.setTextSize(12);
            }

            public void onFinish() {
                getCode.setText("獲取");
                getCode.setEnabled(true);
                getCode.setTextSize(14);
            }
        }.start();
    }

    private void handleResetPasswordClick(EditText newPass, EditText checkPass, String account, TextView statusHint2, AlertDialog dialog) {
        String validationError = validator.validateAgainPasswords(newPass, checkPass, statusHint2);
        if (validationError != null) {
            textHint(statusHint2, validationError);
            return;
        }
        validationError = validator.validatePasswords(newPass, statusHint2);
        if (validationError != null) {
            textHint(statusHint2, validationError);
            return;
        }
        if (updatePassword(account, newPass.getText().toString(), statusHint2))
            new Handler(Looper.getMainLooper()).postDelayed(dialog::dismiss, 1000);
    }

    private boolean updatePassword(String account, String newPassword, TextView statusHint) {
        try {
            String query = "UPDATE [使用者資料] SET [使用者密碼] = ? WHERE [使用者帳號] = ? OR [使用者郵件] = ? " +
                    "UNION ALL " +
                    "UPDATE [健身教練資料] SET [健身教練密碼] = ? WHERE [健身教練帳號] = ? OR [健身教練郵件] = ?";
            PreparedStatement statement = MyConnection.prepareStatement(query);
            statement.setString(1, newPassword);
            statement.setString(2, account);
            statement.setString(3, account);
            statement.setString(4, newPassword);
            statement.setString(5, account);
            statement.setString(6, account);
            if (statement.executeUpdate() > 0) {
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