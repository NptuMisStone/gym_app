package com.NPTUMisStone.gym_app.Coach.Main;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;
import static com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle.convertImageToBytes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Identify.Login;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CoachInfo extends AppCompatActivity {
    Connection MyConnection;
    ImageView CoachInfo_image;
    Button edit,upload,cancel,save;
    TextView[] CoachInfo_tv;
    EditText[] CoachInfo_et;
    String[] CoachInfo_name = new String[]{"姓名", "帳號", "電話", "信箱"};
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_main_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }
    private void init() {
        TextView title = findViewById(R.id.CoachInfo_coachId);
        title.setText(getString(R.string.Coach_CoachInfo, Integer.toString(Coach.getInstance().getCoachId())));
        findViewById(R.id.CoachInfo_return).setOnClickListener(v -> finish());
        findViewById(R.id.CoachInfo_logout).setOnClickListener(v -> logout());
        edit = findViewById(R.id.CoachInfo_edit);
        edit.setOnClickListener(v -> changeInfo());
        upload = findViewById(R.id.CoachInfo_upload);
        upload.setOnClickListener(v -> changeImage());
        cancel = findViewById(R.id.CoachInfo_cancel);
        cancel.setOnClickListener(v -> cancelEdit());
        save = findViewById(R.id.CoachInfo_save);
        save.setOnClickListener(v -> saveInfo());
        CoachInfo_tv = new TextView[]{findViewById(R.id.CoachInfo_et_showName), findViewById(R.id.CoachInfo_et_showAccount), findViewById(R.id.CoachInfo_et_showPhone), findViewById(R.id.CoachInfo_et_showMail)};
        CoachInfo_et = new EditText[]{findViewById(R.id.CoachInfo_et_editName), findViewById(R.id.CoachInfo_et_editAccount), findViewById(R.id.CoachInfo_et_editPhone), findViewById(R.id.CoachInfo_et_editMail)};
        for (int i = 0; i < CoachInfo_tv.length; i++) {
            CoachInfo_tv[i].setText(get_info(i));
            CoachInfo_et[i].setText(get_info(i));
        }
        init_image();
    }
    private String get_info(int index){
        return switch (index) {
            case 0 -> Coach.getInstance().getCoachName();
            case 1 -> Coach.getInstance().getCoachAccount();
            case 2 -> Coach.getInstance().getCoachPhone();
            case 3 -> Coach.getInstance().getCoachMail();
            default -> "";
        };
    }
    private void init_image() {
        CoachInfo_image = findViewById(R.id.CoachInfo_image);
        //將byte[]轉換成Bitmap：https://stackoverflow.com/questions/3520019/display-image-from-bytearray
        byte[] image = Coach.getInstance().getCoachImage();
        if (image != null) CoachInfo_image.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
    }
    private void logout() {
        Coach.setInstance(0,"","", "", true,"",null);
        //Delete SharedPreferences Data：https://stackoverflow.com/questions/3687315/how-to-delete-shared-preferences-data-from-app-in-android
        getSharedPreferences("remember", MODE_PRIVATE).edit().remove("last_login").apply();
        sendBroadcast(new Intent("com.NPTUMisStone.gym_app.LOGOUT"));
        startActivity(new Intent(this, Login.class));
        finish();
    }
    private void changeInfo() {
        edit.setEnabled(false);
        upload.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        for (int i = 0; i < CoachInfo_tv.length; i++) {
            CoachInfo_tv[i].setVisibility(View.GONE);
            CoachInfo_et[i].setVisibility(View.VISIBLE);
        }
        imageShift();
    }
    private void imageShift() {
        ConstraintLayout layout = findViewById(R.id.main);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        // 將CoachInfo_image對齊到parent的左邊
        constraintSet.connect(R.id.CoachInfo_image, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
        constraintSet.connect(R.id.CoachInfo_image, ConstraintSet.END, R.id.CoachInfo_upload, ConstraintSet.START, 0);
        // 將CoachInfo_upload對齊到parent的右邊
        constraintSet.connect(R.id.CoachInfo_upload, ConstraintSet.START, R.id.CoachInfo_image, ConstraintSet.END, 0);
        constraintSet.connect(R.id.CoachInfo_upload, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        // 應用新的約束
        constraintSet.applyTo(layout);
    }
    private void imageReturn() {
        ConstraintLayout layout = findViewById(R.id.main);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        // 將CoachInfo_image對齊到parent的左邊
        constraintSet.connect(R.id.CoachInfo_image, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
        constraintSet.connect(R.id.CoachInfo_image, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        // 將CoachInfo_upload對齊到parent的右邊
        constraintSet.connect(R.id.CoachInfo_upload, ConstraintSet.START, R.id.CoachInfo_image, ConstraintSet.END, 0);
        constraintSet.connect(R.id.CoachInfo_upload, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        // 應用新的約束
        constraintSet.applyTo(layout);
    }
    private void cancelEdit() {
        edit.setEnabled(true);
        upload.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        save.setVisibility(View.GONE);
        for (int i = 0; i < CoachInfo_tv.length; i++) {
            CoachInfo_tv[i].setVisibility(View.VISIBLE);
            CoachInfo_et[i].setText(get_info(i));
            CoachInfo_et[i].setVisibility(View.GONE);
        }
        imageReturn();
    }
    private void saveInfo() {
        if(!checkInput()) return;
        try {
            String[] inputData = new String[]{CoachInfo_et[0].getText().toString(), CoachInfo_et[1].getText().toString(), CoachInfo_et[2].getText().toString(), CoachInfo_et[3].getText().toString()};
            byte[] inputImage = uri == null ? Coach.getInstance().getCoachImage(): convertImageToBytes(this,uri);
            updateCoachInfoInDatabase(inputData, inputImage);
            updateCoachInstance(inputData, inputImage);
            Toast.makeText(this, "資料更新成功", Toast.LENGTH_SHORT).show();
            edit.setEnabled(true);
            upload.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            for (int i = 0; i < CoachInfo_tv.length; i++) {
                CoachInfo_tv[i].setText(get_info(i));
                CoachInfo_tv[i].setVisibility(View.VISIBLE);
                CoachInfo_et[i].setVisibility(View.GONE);
            }
            imageReturn();
        }catch (Exception e){
            Log.e("SQL", "Error in SQL", e);
        }
    }
    private boolean checkInput() {
        boolean isValid = true;
        int max = CoachInfo_et.length-1;
        EditText editText;
        int[] max_length = new int[]{20, 20, 10, 30};
        for (int i = max-1; i >= 0; i--) { //從後面開始檢查(跳過信箱)，讓最上面的EditText可以focus
            editText = CoachInfo_et[i];
            if (editText.getText().toString().trim().isEmpty()) {
                editHint(editText, "請輸入"+ CoachInfo_name[i]);
                isValid = false;
            }else if (editText.getText().toString().length() > max_length[i]) {
                editHint(editText, CoachInfo_name[i] + "過長");
                isValid = false;
            }
        }
        editText = CoachInfo_et[max];
        if (!isEmailValid(editText.getText().toString())) {
            editHint(editText, "請輸入有效的電子郵件");
            isValid = false;
        }
        editText = CoachInfo_et[max-1];
        if (editText.getText().toString().length() != 10) {
            editHint(editText, "請輸入正確的聯絡電話");
            isValid = false;
        }
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            editText = CoachInfo_et[1];
            if (!editText.getText().toString().equals(Coach.getInstance().getCoachAccount())) {
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
    private void updateCoachInfoInDatabase(String[] coachInfo, byte[] imageData) throws SQLException {
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        PreparedStatement ps = MyConnection.prepareStatement("UPDATE 健身教練資料 SET 健身教練姓名 = ?, 健身教練帳號 = ?, 健身教練電話 = ?, 健身教練郵件 = ?, 健身教練圖片 = ? WHERE 健身教練編號 = ?");
        for (int i = 0; i < coachInfo.length; i++) ps.setString(i + 1, coachInfo[i]);
        ps.setBytes(5, imageData);
        ps.setInt(6, Coach.getInstance().getCoachId());
        ps.executeUpdate();
    }
    private void updateCoachInstance(String[] coachInfo, byte[] imageData) {
        Coach.setInstance(Coach.getInstance().getCoachId(), coachInfo[1], coachInfo[0], coachInfo[2], Coach.getInstance().getCoachSex(), coachInfo[3], imageData);
    }
    private void changeImage() {
        CoachInfo_image = findViewById(R.id.CoachInfo_image);
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
                    if (data == null)
                        return;
                    uri = data.getData();
                    CoachInfo_image.setImageURI(uri);
                }
                else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "取消圖片上傳", Toast.LENGTH_SHORT).show();
                }
            }
    );
}