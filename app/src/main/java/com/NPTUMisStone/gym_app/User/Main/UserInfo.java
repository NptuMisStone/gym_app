package com.NPTUMisStone.gym_app.User.Main;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;
import static com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle.convertImageToBytes;
import static com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle.getBitmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Identify.Login;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserInfo extends AppCompatActivity {
    Connection MyConnection;
    ImageView userinfo_image;
    Button edit,upload,cancel,save;
    TextView[] userinfo_tv;
    EditText[] userinfo_et;
    String[] userinfo_name = new String[]{"姓名", "帳號", "電話", "信箱"};
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_main_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }
    private void init() {
        TextView title = findViewById(R.id.userinfo_userid);
        title.setText(getString(R.string.User_UserInfo, Integer.toString(User.getInstance().getUserId())));
        findViewById(R.id.userinfo_return).setOnClickListener(v -> finish());
        findViewById(R.id.userinfo_logout).setOnClickListener(v -> logout());
        edit = findViewById(R.id.userinfo_edit);
        edit.setOnClickListener(v -> changeInfo());
        upload = findViewById(R.id.userinfo_upload);
        upload.setOnClickListener(v -> changeImage());
        cancel = findViewById(R.id.userinfo_cancel);
        cancel.setOnClickListener(v -> cancelEdit());
        save = findViewById(R.id.userinfo_save);
        save.setOnClickListener(v -> saveInfo());
        Button change_password = findViewById(R.id.userinfo_change);
        change_password.setOnClickListener(v -> change_password());
        userinfo_tv = new TextView[]{findViewById(R.id.userinfo_et_showName), findViewById(R.id.userinfo_et_showAccount), findViewById(R.id.userinfo_et_showPhone), findViewById(R.id.userinfo_et_showMail)};
        userinfo_et = new EditText[]{findViewById(R.id.userinfo_et_editName), findViewById(R.id.userinfo_et_editAccount), findViewById(R.id.userinfo_et_editPhone), findViewById(R.id.userinfo_et_editMail)};
        for (int i = 0; i < userinfo_tv.length; i++) {
            userinfo_tv[i].setText(get_info(i));
            userinfo_et[i].setText(get_info(i));
        }
        init_image();
    }
    private String get_info(int index){
        return switch (index) {
            case 0 -> User.getInstance().getUserName();
            case 1 -> User.getInstance().getUserAccount();
            case 2 -> User.getInstance().getUserPhone();
            case 3 -> User.getInstance().getUserMail();
            default -> "";
        };
    }
    private void init_image() {
        userinfo_image = findViewById(R.id.userinfo_image);
        //將byte[]轉換成Bitmap：https://stackoverflow.com/questions/3520019/display-image-from-bytearray
        byte[] image = User.getInstance().getUserImage();
        if (image != null)
            userinfo_image.setImageBitmap(getBitmap(image));
    }
    private void logout() {
        User.setInstance(0,"","", "", true,"",null);
        //Delete SharedPreferences Data：https://stackoverflow.com/questions/3687315/how-to-delete-shared-preferences-data-from-app-in-android
        getSharedPreferences("remember", MODE_PRIVATE).edit().remove("last_login").apply();
        startActivity(new Intent(this, Login.class));
        finish();
    }
    private void changeInfo() {
        edit.setEnabled(false);
        upload.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        for (int i = 0; i < userinfo_tv.length; i++) {
            userinfo_tv[i].setVisibility(View.GONE);
            userinfo_et[i].setVisibility(View.VISIBLE);
        }
        imageShift();
    }
    private void imageShift() {
        ConstraintLayout layout = findViewById(R.id.main);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        // 將userinfo_image對齊到parent的左邊
        constraintSet.connect(R.id.userinfo_image, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
        constraintSet.connect(R.id.userinfo_image, ConstraintSet.END, R.id.userinfo_upload, ConstraintSet.START, 0);
        // 將userinfo_upload對齊到parent的右邊
        constraintSet.connect(R.id.userinfo_upload, ConstraintSet.START, R.id.userinfo_image, ConstraintSet.END, 0);
        constraintSet.connect(R.id.userinfo_upload, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        // 應用新的約束
        constraintSet.applyTo(layout);
    }
    private void imageReturn() {
        ConstraintLayout layout = findViewById(R.id.main);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        // 將userinfo_image對齊到parent的左邊
        constraintSet.connect(R.id.userinfo_image, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
        constraintSet.connect(R.id.userinfo_image, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        // 將userinfo_upload對齊到parent的右邊
        constraintSet.connect(R.id.userinfo_upload, ConstraintSet.START, R.id.userinfo_image, ConstraintSet.END, 0);
        constraintSet.connect(R.id.userinfo_upload, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        // 應用新的約束
        constraintSet.applyTo(layout);
    }
    private void cancelEdit() {
        edit.setEnabled(true);
        upload.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        save.setVisibility(View.GONE);
        for (int i = 0; i < userinfo_tv.length; i++) {
            userinfo_tv[i].setVisibility(View.VISIBLE);
            userinfo_et[i].setText(get_info(i));
            userinfo_et[i].setVisibility(View.GONE);
        }
        imageReturn();
    }
    private void saveInfo() {
        if(!checkInput()) return;
        try {
            String[] inputData = new String[]{userinfo_et[0].getText().toString(), userinfo_et[1].getText().toString(), userinfo_et[2].getText().toString(), userinfo_et[3].getText().toString()};
            byte[] inputImage = uri == null ? User.getInstance().getUserImage(): convertImageToBytes(this,uri);
            updateUserInfoInDatabase(inputData, inputImage);
            updateUserInstance(inputData, inputImage);
            Toast.makeText(this, "資料更新成功", Toast.LENGTH_SHORT).show();
            edit.setEnabled(true);
            upload.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            for (int i = 0; i < userinfo_tv.length; i++) {
                userinfo_tv[i].setText(get_info(i));
                userinfo_tv[i].setVisibility(View.VISIBLE);
                userinfo_et[i].setVisibility(View.GONE);
            }
            imageReturn();
        }catch (Exception e){
            Log.e("SQL", "Error in SQL", e);
            Toast.makeText(this, "資料更新失敗", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkInput() {
        boolean isValid = true;
        int max = userinfo_et.length-1;
        EditText editText;
        int[] max_length = new int[]{20, 20, 10, 30};
        for (int i = max-1; i >= 0; i--) { //從後面開始檢查(跳過信箱)，讓最上面的EditText可以focus
            editText = userinfo_et[i];
            if (editText.getText().toString().trim().isEmpty()) {
                editHint(editText, "請輸入"+ userinfo_name[i]);
                isValid = false;
            }else if (editText.getText().toString().length() > max_length[i]) {
                editHint(editText, userinfo_name[i] + "過長");
                isValid = false;
            }
        }
        editText = userinfo_et[max];
        if (!isEmailValid(editText.getText().toString())) {
            editHint(editText, "請輸入有效的電子郵件");
            isValid = false;
        }
        editText = userinfo_et[max-1];
        if (editText.getText().toString().length() != 10) {
            editHint(editText, "請輸入正確的聯絡電話");
            isValid = false;
        }
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            editText = userinfo_et[1];
            if (!editText.getText().toString().equals(User.getInstance().getUserAccount())) {
                String check_account = "SELECT * FROM 使用者資料 WHERE 使用者帳號 ='" + editText.getText().toString()+ "'";
                ResultSet account_result = MyConnection.createStatement().executeQuery(check_account);
                if (account_result.next()) {
                    editHint(editText, "此帳號已被使用");
                    isValid = false;
                }
            }
        } catch (Exception e) {
            Log.e("SQL", "Error in SQL", e);
            isValid = false;
        }
        return isValid;
    }
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void updateUserInfoInDatabase(String[] userInfo, byte[] imageData) throws SQLException {
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        PreparedStatement ps = MyConnection.prepareStatement("UPDATE 使用者資料 SET 使用者姓名 = ?, 使用者帳號 = ?, 使用者電話 = ?, 使用者郵件 = ?, 使用者圖片 = ? WHERE 使用者編號 = ?");
        for (int i = 0; i < userInfo.length; i++) ps.setString(i + 1, userInfo[i]);
        ps.setBytes(5, imageData);
        ps.setInt(6, User.getInstance().getUserId());
        ps.executeUpdate();
    }
    private void updateUserInstance(String[] userInfo, byte[] imageData) {
        User.setInstance(User.getInstance().getUserId(), userInfo[1], userInfo[0], userInfo[2], User.getInstance().getUserSex(), userInfo[3], imageData);
    }
    private void changeImage() {
        userinfo_image = findViewById(R.id.userinfo_image);
        Intent intent = new Intent();   //上傳圖片：https://www.youtube.com/watch?v=9oNujFx_ZaI&ab_channel=ShihFinChen
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
                    if (data == null)   return;
                    uri = data.getData();
                    userinfo_image.setImageURI(uri);
                }
                else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "取消圖片上傳", Toast.LENGTH_SHORT).show();
                }
            }
    );
    boolean isDialogShow = false;
    private void change_password() {
        if (isDialogShow) return;
        View dialogView = getLayoutInflater().inflate(R.layout.main_login_forget, null);
        ((TextView) dialogView.findViewById(R.id.forget_title)).setText("用戶密碼重設");
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        dialogView.findViewById(R.id.forget_getButton).setOnClickListener(v -> handleGetCodeClick(dialogView, dialog));
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show();
        isDialogShow = true;
    }
    private void handleGetCodeClick(View dialogView, AlertDialog dialog) {

    }

}