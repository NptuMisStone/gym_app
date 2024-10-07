package com.NPTUMisStone.gym_app.User_And_Coach;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.textHint;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.NPTUMisStone.gym_app.Main.Identify.JavaMailAPI;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PasswordReset {
    private final Context context;
    private final boolean isUser;
    private final Connection MyConnection;
    private final Validator validator;
    private boolean isDialogShow = false;
    private CountDownTimer countDownTimer;

    public PasswordReset(Context context, boolean isUser, Connection MyConnection) {
        this.context = context;
        this.isUser = isUser;
        this.MyConnection = MyConnection;
        this.validator = new Validator(MyConnection);
    }

    public void showPasswordResetDialog() {
        if (isDialogShow) return;
        isDialogShow = true;
        View dialogView = ((Activity) context).getLayoutInflater().inflate(R.layout.main_login_forget, null);
        ((TextView) dialogView.findViewById(R.id.forget_title)).setText(isUser ? "用戶密碼重設" : "教練密碼重設");
        AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).create();
        dialogView.findViewById(R.id.forget_getButton).setOnClickListener(v -> handleGetCodeClick(dialogView, dialog));
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show();
    }

    private void handleGetCodeClick(View dialogView, AlertDialog dialog) {
        EditText editAccount = dialogView.findViewById(R.id.forget_accountEdit);
        EditText editEmail = dialogView.findViewById(R.id.forget_emailEdit);
        String inputAccount = editAccount.getText().toString();
        String userEmail = editEmail.getText().toString();
        TextView statusHint1 = dialogView.findViewById(R.id.forget_statusHint1);
        Button getCode = dialogView.findViewById(R.id.forget_getButton);
        String validationError = validator.validateEmailAndAccount(inputAccount, userEmail, isUser,statusHint1);
        if (validationError == null) {
            sendVerificationEmail(userEmail, statusHint1, getCode, dialogView, dialog);
        } else {
            textHint(statusHint1, validationError);
        }
    }

    private void sendVerificationEmail(String userEmail, TextView statusHint1, Button getCode, View dialogView, AlertDialog dialog) {
        textHint(statusHint1, "確認中...");
        String randomCode = generateRandomNumber();
        String mSubject = "【益身GYM】驗證碼";
        String mMessage = "您的驗證碼為：" + randomCode + "\n\n有效期限為10分鐘，請務必妥善保管驗證碼，勿將其告知他人。逾時請返回APP重新發送驗證信，感謝您。";
        new JavaMailAPI(context, userEmail, mSubject, mMessage).sendMail(new JavaMailAPI.EmailSendResultCallback() {
            @Override
            public void onSuccess() {
                ((Activity) context).runOnUiThread(() -> {
                    textHint(statusHint1, "✔ 驗證碼已成功寄送，請前往您的信箱查看");
                    LocalTime sendTime = LocalTime.now(ZoneId.of("Asia/Taipei"));
                    if (countDownTimer != null) countDownTimer.cancel();
                    countDownTimer = startCountdown(getCode);
                    setupCodeValidation(dialogView, randomCode, sendTime, dialog);
                });
            }

            @Override
            public void onFailure(Exception e) {
                ((Activity) context).runOnUiThread(() -> textHint(statusHint1, "❌ 郵件發送失敗，請再試一次。"));
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
        String validationError = validator.validatePasswords(newPass, checkPass, statusHint2);
        if (validationError != null) {
            textHint(statusHint2, validationError);
            return;
        }
        if (updatePassword(account, newPass.getText().toString(), statusHint2))
            new Handler(Looper.getMainLooper()).postDelayed(dialog::dismiss, 1000);
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