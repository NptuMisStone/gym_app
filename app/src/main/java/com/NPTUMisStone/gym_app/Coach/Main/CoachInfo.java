package com.NPTUMisStone.gym_app.Coach.Main;

import static com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints.editHint;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Identify.Login;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.PasswordReset;
import com.NPTUMisStone.gym_app.User_And_Coach.Validator;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CoachInfo extends AppCompatActivity {
    Connection MyConnection;
    ImageView coachInfo_image;
    AutoCompleteTextView[] coachInfo_tv;
    TextInputLayout[] coachInfo_layout;
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
        ((TextView) findViewById(R.id.CoachInfo_idText)).setText(getString(R.string.All_idText, Coach.getInstance().getCoachId()));
        findViewById(R.id.CoachInfo_return).setOnClickListener(v -> finish());
        findViewById(R.id.CoachInfo_logout).setOnClickListener(v -> logout());
        findViewById(R.id.CoachInfo_upload).setOnClickListener(v -> changeImage());
        findViewById(R.id.CoachInfo_resetButton).setOnClickListener(v -> new PasswordReset(this, true, MyConnection).showPasswordResetDialog());
        coachInfo_tv = new AutoCompleteTextView[]{
                findViewById(R.id.CoachInfo_nameText),
                findViewById(R.id.CoachInfo_accountText),
                findViewById(R.id.CoachInfo_phoneText),
                findViewById(R.id.CoachInfo_emailText),
                findViewById(R.id.CoachInfo_sexText)
        };
        coachInfo_layout = new TextInputLayout[]{
                findViewById(R.id.CoachInfo_nameLayout),
                findViewById(R.id.CoachInfo_accountLayout),
                findViewById(R.id.CoachInfo_phoneLayout),
                findViewById(R.id.CoachInfo_emailLayout),
                findViewById(R.id.CoachInfo_identifyLayout)
        };
        for (int i = 0; i < coachInfo_tv.length; i++) {
            coachInfo_tv[i].setText(get_info(i));
            int finalI = i;
            coachInfo_tv[i].setOnClickListener(v -> changeData(finalI));
        }
        init_image();
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
    }

    private void changeData(int index) {
        Log.d("ChangeData", "ChangeData: " + index);
        AutoCompleteTextView autoCompleteTextView = coachInfo_tv[index];
        TextInputLayout textInputLayout = coachInfo_layout[index];
        View dialogView;
        Validator validator = new Validator(MyConnection);
        ViewGroup parent = findViewById(android.R.id.content); // Get the parent view group

        if (index < 4) {
            dialogView = getLayoutInflater().inflate(R.layout.info_edit_layout, parent, false);
            TextInputLayout textInputLayout1 = dialogView.findViewById(R.id.InfoEdit_Layout);
            textInputLayout1.setHint(textInputLayout.getHint());
            AutoCompleteTextView autoCompleteTextView1 = dialogView.findViewById(R.id.InfoEdit_Text);
            autoCompleteTextView1.setText(autoCompleteTextView.getText().toString());
            showEditDialog(index, autoCompleteTextView, autoCompleteTextView1, validator, dialogView);
        } else {
            dialogView = getLayoutInflater().inflate(R.layout.info_sex_layout, parent, false);
            RadioButton[] radioButtons = new RadioButton[]{
                    dialogView.findViewById(R.id.InfoSex_sexRadio1),
                    dialogView.findViewById(R.id.InfoSex_sexRadio2),
                    dialogView.findViewById(R.id.InfoSex_sexRadio3)
            };
            for (int i = 0; i < radioButtons.length; i++)
                if (i == Coach.getInstance().getCoachSex() - 1) radioButtons[i].setChecked(true);
            showSexDialog(autoCompleteTextView, radioButtons);
        }
    }

    private void showEditDialog(int index, AutoCompleteTextView autoCompleteTextView, AutoCompleteTextView autoCompleteTextView1, Validator validator, View dialogView) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("修改資料")
                .setMessage("是否要修改資料？")
                .setView(dialogView)
                .setPositiveButton("確定", null)
                .setNegativeButton("否", null)
                .create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String errorMessage = validateInput(index, autoCompleteTextView1, validator);
            if (errorMessage == null) {
                updateUserInfo(index, autoCompleteTextView, autoCompleteTextView1, alertDialog);
            } else {
                editHint(autoCompleteTextView1, errorMessage);
            }
        });
    }

    private void showSexDialog(AutoCompleteTextView autoCompleteTextView, RadioButton[] radioButtons) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("修改資料")
                .setMessage("是否要修改資料？")
                .setView((View) radioButtons[0].getParent())
                .setPositiveButton("是", (dialog, which) -> {
                    for (int i = 0; i < radioButtons.length; i++) {
                        if (radioButtons[i].isChecked()) {
                            try {
                                MyConnection.prepareStatement("UPDATE 健身教練資料 SET 健身教練性別 = " + (i + 1) + " WHERE 健身教練編號 = " + Coach.getInstance().getCoachId()).executeUpdate();
                                Coach.getInstance().setCoachSex(i + 1);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            autoCompleteTextView.setText(radioButtons[i].getText().toString());
                            break;
                        }
                    }
                })
                .setNegativeButton("否", null)
                .create();
        alertDialog.show();
    }

    private String validateInput(int index, AutoCompleteTextView autoCompleteTextView1, Validator validator) {
        return switch (index) {
            case 0 -> validator.checkInput(autoCompleteTextView1, "姓名", 20, null);
            case 1 -> {
                String errorMessage = validator.checkInput(autoCompleteTextView1, "帳號", 20, null);
                if (errorMessage == null) {
                    errorMessage = validator.checkAccount(autoCompleteTextView1, Coach.getInstance().getCoachAccount(), null);
                }
                yield errorMessage;
            }
            case 2 -> {
                String errorMessage = validator.checkInput(autoCompleteTextView1, "電話", 10, null);
                if (errorMessage == null) {
                    errorMessage = validator.checkPhone(autoCompleteTextView1, null);
                }
                yield errorMessage;
            }
            case 3 -> {
                String errorMessage = validator.checkInput(autoCompleteTextView1, "信箱", 30, null);
                if (errorMessage == null) {
                    errorMessage = validator.checkEmail(autoCompleteTextView1, null);
                }
                yield errorMessage;
            }
            default -> null;
        };
    }

    private void updateUserInfo(int index, AutoCompleteTextView autoCompleteTextView, AutoCompleteTextView autoCompleteTextView1, AlertDialog alertDialog) {
        autoCompleteTextView.setText(autoCompleteTextView1.getText().toString());
        try {
            String updateQuery = getUpdateQuery(index);
            PreparedStatement ps = MyConnection.prepareStatement(updateQuery);
            ps.setString(1, autoCompleteTextView1.getText().toString());
            ps.setInt(2, Coach.getInstance().getCoachId());
            ps.executeUpdate();
            updateUserInstance(index, autoCompleteTextView1.getText().toString());
            alertDialog.dismiss();
        } catch (SQLException e) {
            Log.e("SQL", "Error updating coach info", e);
        }
    }

    private void updateUserInstance(int index, String newValue) {
        switch (index) {
            case 0 -> Coach.getInstance().setCoachName(newValue);
            case 1 -> Coach.getInstance().setCoachAccount(newValue);
            case 2 -> Coach.getInstance().setCoachPhone(newValue);
            case 3 -> Coach.getInstance().setCoachMail(newValue);
        }
    }

    @NonNull
    private static String getUpdateQuery(int index) {
        String columnName = switch (index) {
            case 0 -> "健身教練姓名";
            case 1 -> "健身教練帳號";
            case 2 -> "健身教練電話";
            case 3 -> "健身教練郵件";
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
        return "UPDATE 健身教練資料 SET " + columnName + " = ? WHERE 健身教練編號 = ?";
    }

    private String get_info(int index) {
        return switch (index) {
            case 0 -> Coach.getInstance().getCoachName();
            case 1 -> Coach.getInstance().getCoachAccount();
            case 2 -> Coach.getInstance().getCoachPhone();
            case 3 -> Coach.getInstance().getCoachMail();
            case 4 -> switch (Coach.getInstance().getCoachSex()) {
                case 1 -> "男♂";
                case 2 -> "女♀";
                case 3 -> "保密⚲";
                default -> "未設定";
            };
            default -> "";
        };
    }

    private void init_image() {
        coachInfo_image = findViewById(R.id.CoachInfo_image);
        //將byte[]轉換成Bitmap：https://stackoverflow.com/questions/3520019/display-image-from-bytearray
        byte[] image = Coach.getInstance().getCoachImage();
        if (image != null)
            coachInfo_image.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
    }

    private void logout() {
        Coach.setInstance(0, "", "", "", 0, "", null);
        //Delete SharedPreferences Data：https://stackoverflow.com/questions/3687315/how-to-delete-shared-preferences-data-from-app-in-android
        getSharedPreferences("remember", MODE_PRIVATE).edit().remove("last_login").apply();
        sendBroadcast(new Intent("com.NPTUMisStone.gym_app.LOGOUT"));
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void changeImage() {
        coachInfo_image = findViewById(R.id.CoachInfo_image);
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
                    if (data == null) return;
                    uri = data.getData();
                    coachInfo_image.setImageURI(uri);
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "取消圖片上傳", Toast.LENGTH_SHORT).show();
                }
            }
    );
}