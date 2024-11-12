package com.NPTUMisStone.gym_app.Coach.Class;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClassAdd extends AppCompatActivity {

    private ImageView showImage;
    private Uri uri;
    private Connection MyConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_class_add);

        // 設置邊距調整
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        findViewById(R.id.ClassMain_backButton).setOnClickListener(v -> finish());

        // 初始化圖片上傳與按鈕事件
        showImage = findViewById(R.id.ClassAdd_uploadImage);
        Button uploadButton = findViewById(R.id.ClassAdd_uploadButton);
        uploadButton.setOnClickListener(v -> changeImage());

        Button saveButton = findViewById(R.id.ClassAdd_saveButton);
        //saveButton.setOnClickListener(v -> saveClass());

        // 設置輸入框的單位顯示
        setFocusChangeListeners();
    }

    private void setFocusChangeListeners() {
        //setFocusChangeListener(findViewById(R.id.ClassAdd_timeEdit), "分鐘");
        //setFocusChangeListener(findViewById(R.id.ClassAdd_limitEdit), "人");
        setFocusChangeListener(findViewById(R.id.ClassAdd_feeEdit), "$");
    }

    private void setFocusChangeListener(EditText editText, String suffix) {
        editText.setOnFocusChangeListener((v, hasFocus) -> editText.setText(hasFocus ? editText.getText().toString().replace(suffix, "") : editText.getText().toString() + suffix));
    }

    private void changeImage() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        uploadImage_ActivityResult.launch(intent);
    }

    ActivityResultLauncher<Intent> uploadImage_ActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        uri = data.getData();
                        showImage.setImageURI(uri);
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "取消圖片上傳", Toast.LENGTH_SHORT).show();
                }
            }
    );


//    private void saveClass() {
//        EditText nameEdit = findViewById(R.id.ClassAdd_nameEdit);
//        EditText typeEdit = findViewById(R.id.ClassAdd_typeEdit);
//        EditText descriptionEdit = findViewById(R.id.ClassAdd_descriptionEdit);
//        EditText timeEdit = findViewById(R.id.ClassAdd_timeEdit);
//        EditText limitEdit = findViewById(R.id.ClassAdd_limitEdit);
//        EditText locationEdit = findViewById(R.id.ClassAdd_locationEdit);
//        EditText feeEdit = findViewById(R.id.ClassAdd_feeEdit);
//
//        // 檢查欄位是否正確填寫
//        if (validateFields(new EditText[]{nameEdit, typeEdit, descriptionEdit, timeEdit, limitEdit, locationEdit, feeEdit})) {
//            try {
//                // 插入資料至資料庫
//                String insertQuery = "INSERT INTO [健身教練課程] ([健身教練編號],[課程名稱],[課程類型],[課程內容介紹],[課程時間長度],[上課人數],[上課地點],[課程費用],[課程圖片]) VALUES (?,?,?,?,?,?,?,?,?)";
//                PreparedStatement insertStatement = MyConnection.prepareStatement(insertQuery);
//                insertStatement.setInt(1, Coach.getInstance().getCoachId());
//                insertStatement.setString(2, nameEdit.getText().toString());
//                insertStatement.setString(3, typeEdit.getText().toString());
//                insertStatement.setString(4, descriptionEdit.getText().toString());
//                insertStatement.setInt(5, Integer.parseInt(timeEdit.getText().toString().replace("分鐘", "")));
//                insertStatement.setInt(6, Integer.parseInt(limitEdit.getText().toString().replace("人", "")));
//                insertStatement.setString(7, locationEdit.getText().toString());
//                insertStatement.setInt(8, Integer.parseInt(feeEdit.getText().toString().replace("$", "")));
//                insertStatement.setBytes(9, convertImageToBytes(uri));
//                insertStatement.executeUpdate();
//                Toast.makeText(this, "課程新增成功", Toast.LENGTH_SHORT).show();
//                finish();
//            } catch (SQLException e) {
//                Log.e("SQL", "資料庫新增失敗", e);
//                Toast.makeText(this, "資料庫新增失敗", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                Log.e("Error", "Unexpected error", e);
//            }
//        } else {
//            Toast.makeText(this, "請檢查必填欄位", Toast.LENGTH_SHORT).show();
//        }
//    }

    private boolean validateFields(EditText[] editTexts) {
        boolean isValid = true;
        for (EditText editText : editTexts) {
            if (editText.getText().toString().isEmpty()) {
                editText.setError("此欄位不可空白");
                isValid = false;
            }
        }
        return isValid;
    }

    private byte[] convertImageToBytes(Uri uri) {
        // 實現圖像轉字節數組的方法
        // 此方法可能使用 Bitmap 或其他方法來轉換 URI 到 byte[]
        return new byte[0]; // 這是佔位符，應用適合的轉換方法
    }
}
