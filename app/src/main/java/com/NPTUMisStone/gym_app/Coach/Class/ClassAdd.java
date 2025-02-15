package com.NPTUMisStone.gym_app.Coach.Class;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClassAdd extends AppCompatActivity {

    // 圖片相關控件
    private ImageView showImage; // 用於顯示上傳的圖片
    private Uri uri;             // 圖片的 URI

    // 資料庫連接
    private Connection MyConnection; // SQL 資料庫連接

    // 課程相關控件
    private Spinner classTypeSpinner;     // 課程類型 Spinner
    private Spinner classDurationSpinner; // 課程時長 Spinner

    // 上課地點選擇相關控件
    private RadioGroup locationRadioGroup;   // 上課地點選擇的 RadioGroup
    private RadioButton trainerStoreRadio;   // 教練的店家選項
    private RadioButton clientLocationRadio; // 到府（客戶指定地點）選項
    private RadioButton otherLocationRadio;  // 其他（教練指定地點）選項
    private EditText locationNameEdit;       // 地點名稱輸入框
    private Spinner citySpinner;             // 縣市選擇 Spinner
    private Spinner areaSpinner;             // 地區選擇 Spinner
    private EditText addressEdit;            // 詳細地址輸入框

    // 上課人數相關控件
    private RadioGroup classSizeRadioGroup; // 上課人數選擇的 RadioGroup
    private RadioButton oneToOneRadio;      // 一對一課程選項
    private RadioButton groupRadio;         // 團體課程選項
    private EditText groupSizeEdit;         // 團體課程人數輸入框

    // 表示縣市的類
    public class City {
        public int cityId; // 縣市 ID
        public String cityName; // 縣市名稱

        public City(int cityId, String cityName) {
            this.cityId = cityId;
            this.cityName = cityName;
        }

        @Override
        public String toString() {
            return cityName; // Spinner 顯示縣市名稱
        }
    }

    // 表示行政區的類
    public class Area {
        public int areaId; // 行政區 ID
        public String areaName; // 行政區名稱

        public Area(int areaId, String areaName) {
            this.areaId = areaId;
            this.areaName = areaName;
        }

        @Override
        public String toString() {
            return areaName; // Spinner 顯示行政區名稱
        }
    }
    class ClassType {
        int classTypeId;
        String classTypeName;

        public ClassType(int classTypeId, String classTypeName) {
            this.classTypeId = classTypeId;
            this.classTypeName = classTypeName;
        }

        @Override
        public String toString() {
            return classTypeName; // Spinner會顯示名稱
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_class_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 初始化控件
        initializeControls();

        // 加載數據
        loadInitialData();

        // 設置監聽器
        setupListeners();

        // 設置初始選項
        initializeDefaultSelections();
    }

    /**
     * 初始化控件
     */
    private void initializeControls() {
        // 上課地點選擇相關控件
        locationRadioGroup = findViewById(R.id.ClassAdd_locationRadioGroup);
        trainerStoreRadio = findViewById(R.id.ClassAdd_trainerStoreRadio);
        clientLocationRadio = findViewById(R.id.ClassAdd_clientLocationRadio);
        otherLocationRadio = findViewById(R.id.ClassAdd_otherLocationRadio);
        locationNameEdit = findViewById(R.id.ClassAdd_locationNameEdit);
        citySpinner = findViewById(R.id.ClassAdd_citySpinner);
        areaSpinner = findViewById(R.id.ClassAdd_areaSpinner);
        addressEdit = findViewById(R.id.ClassAdd_addressEdit);

        // 上課人數選擇相關控件
        classSizeRadioGroup = findViewById(R.id.ClassAdd_classSizeRadioGroup);
        oneToOneRadio = findViewById(R.id.ClassAdd_oneToOneRadio);
        groupRadio = findViewById(R.id.ClassAdd_groupRadio);
        groupSizeEdit = findViewById(R.id.ClassAdd_groupSizeEdit);

        // 其他控件
        classTypeSpinner = findViewById(R.id.ClassAdd_typeSpinner);
        classDurationSpinner = findViewById(R.id.ClassAdd_durationSpinner);
        showImage = findViewById(R.id.ClassAdd_uploadImage);
        Button uploadButton = findViewById(R.id.ClassAdd_uploadButton);
        uploadButton.setOnClickListener(v -> changeImage());

        // 返回按鈕
        findViewById(R.id.ClassMain_backButton).setOnClickListener(v -> finish());

        findViewById(R.id.ClassAdd_saveButton).setOnClickListener(v -> saveCourse());
    }

    /**
     * 加載初始數據
     */
    private void loadInitialData() {
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        loadDurationData();
        loadClassTypeData();
        loadCityData(citySpinner, areaSpinner);
        bindLocationRadioButtons();
        updateLocationInputs(); // 確保數據加載後更新顯示邏輯
    }

    /**
     * 設置初始選項
     */
    private void initializeDefaultSelections() {
        locationRadioGroup.check(trainerStoreRadio.getVisibility() == View.VISIBLE
                ? R.id.ClassAdd_trainerStoreRadio
                : R.id.ClassAdd_clientLocationRadio);
        classSizeRadioGroup.check(R.id.ClassAdd_oneToOneRadio);
        groupSizeEdit.setVisibility(View.GONE);
        updateLocationInputs();
    }

    /**
     * 設置監聽器
     */
    private void setupListeners() {
        locationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateLocationInputs();
        });

        classSizeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            groupSizeEdit.setVisibility(checkedId == R.id.ClassAdd_groupRadio ? View.VISIBLE : View.GONE);
            clientLocationRadio.setEnabled(checkedId != R.id.ClassAdd_groupRadio);

            // 如果是團體課程且當前地點為 "到府"，自動切換為 "其他地點"
            if (checkedId == R.id.ClassAdd_groupRadio &&
                    locationRadioGroup.getCheckedRadioButtonId() == R.id.ClassAdd_clientLocationRadio) {
                locationRadioGroup.check(R.id.ClassAdd_otherLocationRadio);
            }
            updateLocationInputs();
        });
    }

    /**
     * 更新地點相關的顯示邏輯
     */
    private void updateLocationInputs() {
        int selectedLocationId = locationRadioGroup.getCheckedRadioButtonId();

        // 顯示邏輯
        if (selectedLocationId == R.id.ClassAdd_trainerStoreRadio) {
            showLocationInputs(false);
            showCityAreaInputs(false);
        } else if (selectedLocationId == R.id.ClassAdd_clientLocationRadio) {
            showCityAreaInputs(true);
            showLocationInputs(false);
        } else if (selectedLocationId == R.id.ClassAdd_otherLocationRadio) {
            showCityAreaInputs(true);
            showLocationInputs(true);
        }
    }

    /**
     * 顯示或隱藏地點相關輸入框
     */
    private void showLocationInputs(boolean show) {
        locationNameEdit.setVisibility(show ? View.VISIBLE : View.GONE);
        addressEdit.setVisibility(show ? View.VISIBLE : View.GONE);

        if (!show) {
            locationNameEdit.setText("");
            addressEdit.setText("");
        }
    }

    /**
     * 顯示或隱藏縣市和地區
     */
    private void showCityAreaInputs(boolean show) {
        citySpinner.setVisibility(show ? View.VISIBLE : View.GONE);
        areaSpinner.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    private void loadDurationData() {
        List<String> durationOptions = new ArrayList<>();
        durationOptions.add("請選擇課程時長"); // 添加提示選項
        durationOptions.add("30 分鐘");
        durationOptions.add("60 分鐘");
        durationOptions.add("90 分鐘");
        durationOptions.add("120 分鐘");
        durationOptions.add("150 分鐘");
        durationOptions.add("180 分鐘");
        durationOptions.add("210 分鐘");
        durationOptions.add("240 分鐘");

        // 使用 ArrayAdapter 綁定資料到 Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, durationOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classDurationSpinner.setAdapter(adapter);
    }

    private void loadClassTypeData() {
        List<ClassType> classTypes = getClassTypeData();

        // 使用 ArrayAdapter 綁定資料到 Spinner
        ArrayAdapter<ClassType> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classTypeSpinner.setAdapter(adapter);
    }
    private List<ClassType> getClassTypeData() {
        List<ClassType> classTypes = new ArrayList<>();
        // 添加提示選項作為第一個選項
        classTypes.add(new ClassType(-1, "請選擇課程類型"));
        try {
            String sql = "SELECT 分類編號, 分類名稱 FROM [運動分類清單]";
            PreparedStatement statement = MyConnection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int classTypeId = resultSet.getInt("分類編號");
                String classTypeName = resultSet.getString("分類名稱");
                classTypes.add(new ClassType(classTypeId, classTypeName));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classTypes;
    }
    private void bindLocationRadioButtons() {
        String query = "SELECT [註冊類型], [服務地點名稱] FROM [健身教練審核合併] WHERE [健身教練編號] = ?";

        try (PreparedStatement statement = MyConnection.prepareStatement(query)) {
            // 使用 Coach 單例獲取教練 ID
            statement.setInt(1, Coach.getInstance().getCoachId());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String registrationType = resultSet.getString("註冊類型");
                String serviceLocationName = resultSet.getString("服務地點名稱");

                if ("店家健身教練".equals(registrationType)) {
                    // 如果是店家教練，顯示“教練的店家” RadioButton
                    trainerStoreRadio.setVisibility(View.VISIBLE);
                    trainerStoreRadio.setText(serviceLocationName); // 設置服務地點名稱為按鈕文字
                } else {
                    // 如果不是店家教練，隱藏“教練的店家” RadioButton
                    trainerStoreRadio.setVisibility(View.GONE);
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadCityData(Spinner citySpinner, Spinner areaSpinner) {
        List<City> cityList = new ArrayList<>();
        String cityQuery = "SELECT 縣市id, 縣市 FROM [縣市]";

        try {
            if (MyConnection == null) {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            }

            cityList.add(new City(-1, "請選擇縣市")); // 添加提示選項

            PreparedStatement cityStatement = MyConnection.prepareStatement(cityQuery);
            ResultSet cityResultSet = cityStatement.executeQuery();

            while (cityResultSet.next()) {
                int cityId = cityResultSet.getInt("縣市id");
                String cityName = cityResultSet.getString("縣市");
                cityList.add(new City(cityId, cityName));
            }

            cityResultSet.close();
            cityStatement.close();

            ArrayAdapter<City> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityList);
            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setAdapter(cityAdapter);

            // 監聽縣市選擇
            citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    City selectedCity = (City) citySpinner.getSelectedItem();
                    if (selectedCity.cityId == -1) {
                        // 如果選擇的是 "請選擇縣市"，清空地區 Spinner
                        clearAreaSpinner(areaSpinner);
                    } else {
                        // 加載對應的鄉鎮區
                        loadAreaData(selectedCity.cityId, areaSpinner);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    clearAreaSpinner(areaSpinner);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "加載縣市失敗", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 加載鄉鎮區數據
     */
    private void loadAreaData(int cityId, Spinner areaSpinner) {
        List<Area> areaList = new ArrayList<>();
        String areaQuery = "SELECT 行政區id, 行政區 FROM [行政區] WHERE 縣市id = ?";

        try {
            if (MyConnection == null) {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            }

            areaList.add(new Area(-1, "請選擇鄉鎮區")); // 添加提示選項

            PreparedStatement areaStatement = MyConnection.prepareStatement(areaQuery);
            areaStatement.setInt(1, cityId);
            ResultSet areaResultSet = areaStatement.executeQuery();

            while (areaResultSet.next()) {
                int areaId = areaResultSet.getInt("行政區id");
                String areaName = areaResultSet.getString("行政區");
                areaList.add(new Area(areaId, areaName));
            }

            areaResultSet.close();
            areaStatement.close();

            ArrayAdapter<Area> areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, areaList);
            areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            areaSpinner.setAdapter(areaAdapter);

        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "加載鄉鎮區失敗", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 清空地區 Spinner
     */
    private void clearAreaSpinner(Spinner areaSpinner) {
        List<Area> emptyAreaList = new ArrayList<>();
        emptyAreaList.add(new Area(-1, "請選擇鄉鎮區")); // 添加提示選項

        ArrayAdapter<Area> areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, emptyAreaList);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaAdapter);
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

    private void saveCourse() {
        // 檢查欄位
        if (!validateFields()) {
            return; // 檢查失敗時直接返回
        }

        // 表單提交邏輯
        String courseName = ((EditText) findViewById(R.id.ClassAdd_nameEdit)).getText().toString().trim();
        int courseType = ((ClassType) classTypeSpinner.getSelectedItem()).classTypeId;
        String courseDescription = ((EditText) findViewById(R.id.ClassAdd_descriptionEdit)).getText().toString().trim();
        int courseDuration = Integer.parseInt(classDurationSpinner.getSelectedItem().toString().replace(" 分鐘", ""));
        double courseFee = Double.parseDouble(((EditText) findViewById(R.id.ClassAdd_feeEdit)).getText().toString());
        int classSize = classSizeRadioGroup.getCheckedRadioButtonId() == R.id.ClassAdd_oneToOneRadio ? 1 : Integer.parseInt(groupSizeEdit.getText().toString());
        String requiredEquipment = ((EditText) findViewById(R.id.ClassAdd_equipmentEdit)).getText().toString().trim();
        int locationType = locationRadioGroup.getCheckedRadioButtonId() == R.id.ClassAdd_trainerStoreRadio ? 1
                : locationRadioGroup.getCheckedRadioButtonId() == R.id.ClassAdd_clientLocationRadio ? 2 : 3;

        String locationName = null;
        String locationAddress = null;

        int cityId = -1;
        int areaId = -1;

        if (locationType == 1) {
            // 從資料庫獲取服務地點資訊
            try (PreparedStatement stmt = MyConnection.prepareStatement(
                    "SELECT [服務地點名稱], [縣市id], [行政區id], [服務地點地址] FROM [健身教練審核合併] WHERE [健身教練編號] = ?")) {
                stmt.setInt(1, Coach.getInstance().getCoachId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    locationName = rs.getString("服務地點名稱");
                    cityId = rs.getInt("縣市id");
                    areaId = rs.getInt("行政區id");
                    locationAddress = rs.getString("服務地點地址");
                } else {
                    Toast.makeText(this, "未找到教練的服務地點資訊", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "讀取服務地點資訊失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // 檢查 Spinner 是否有值
            if (citySpinner.getSelectedItem() == null || areaSpinner.getSelectedItem() == null) {
                Toast.makeText(this, "請選擇有效的縣市和行政區", Toast.LENGTH_SHORT).show();
                return;
            }

            City selectedCity = (City) citySpinner.getSelectedItem();
            Area selectedArea = (Area) areaSpinner.getSelectedItem();

            cityId = selectedCity.cityId;
            areaId = selectedArea.areaId;

            if (locationType == 3) {
                locationName = locationNameEdit.getText().toString().trim();
                locationAddress = addressEdit.getText().toString().trim();
            }
        }

        byte[] image = null; // 圖片處理邏輯（可從 ImageView 中獲取）
        if (uri != null) {
            image = getBytesFromUri(uri);
        }

        // 插入課程數據
        String query = "INSERT INTO [健身教練課程] ([課程名稱], [分類編號], [課程內容介紹], [課程時間長度], [上課人數], [地點類型], [地點名稱], [縣市id], [行政區id], [地點地址], [課程費用], [所需設備], [課程圖片], [健身教練編號]) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = MyConnection.prepareStatement(query)) {
            stmt.setString(1, courseName);
            stmt.setInt(2, courseType);
            stmt.setString(3, courseDescription);
            stmt.setInt(4, courseDuration);
            stmt.setInt(5, classSize);
            stmt.setInt(6, locationType);
            stmt.setString(7, locationName);
            stmt.setInt(8, cityId);
            stmt.setInt(9, areaId);
            stmt.setString(10, locationAddress);
            stmt.setDouble(11, courseFee);
            stmt.setString(12, requiredEquipment);
            if (image != null) {
                stmt.setBytes(13, image);
            } else {
                stmt.setNull(13, java.sql.Types.BLOB); // 如果沒有圖片，設置為 NULL
            }
            stmt.setInt(14, Coach.getInstance().getCoachId());
            stmt.executeUpdate();
            Toast.makeText(this, "課程已儲存成功！", Toast.LENGTH_SHORT).show();
            // 使用 Handler 延遲 2 秒後跳轉到 ClassMain
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                navigateToClassMain(); // 跳轉到主頁面
            }, 3000); // 3000 毫秒即 3 秒
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "儲存課程失敗！", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToClassMain() {
        Intent intent = new Intent(this, ClassMain.class); // ClassMain 是您的主界面 Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 清空當前的 Activity 堆棧
        startActivity(intent);
        finish(); // 關閉當前 Activity
    }


    /**
     * 將 Uri 轉為 byte[]
     */
    private byte[] getBytesFromUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 檢查欄位
     */
    private boolean validateFields() {
        // 檢查主要的 EditText 欄位
        if (!validateEditTexts(
                new int[]{R.id.ClassAdd_nameEdit, R.id.ClassAdd_descriptionEdit, R.id.ClassAdd_equipmentEdit, R.id.ClassAdd_feeEdit},
                new String[]{"請輸入課程名稱", "請輸入課程描述", "請輸入所需設備", "請輸入課程費用"})) {
            return false;
        }

        // 檢查主要的 Spinner
        if (!validateSpinners(
                new Spinner[]{classTypeSpinner, classDurationSpinner},
                new String[]{"請選擇課程類型", "請選擇課程時長"})) {
            return false;
        }

        // 驗證上課地點相關欄位
        int locationType = locationRadioGroup.getCheckedRadioButtonId();

        if (locationType == R.id.ClassAdd_clientLocationRadio) {
            // 如果選擇 "到府(客戶指定地點)"
            if (!validateSpinner(citySpinner, "請選擇縣市") || !validateSpinner(areaSpinner, "請選擇鄉鎮區")) {
                return false;
            }
        } else if (locationType == R.id.ClassAdd_otherLocationRadio) {
            // 如果選擇 "其他(教練指定地點)"
            if (!validateEditText(R.id.ClassAdd_locationNameEdit, "請輸入地點名稱") ||
                    !validateSpinner(citySpinner, "請選擇縣市") ||
                    !validateSpinner(areaSpinner, "請選擇鄉鎮區") ||
                    !validateEditText(R.id.ClassAdd_addressEdit, "請輸入詳細地址")) {
                return false;
            }
        }

        // 驗證群組人數欄位（如果選擇了群組課程）
        if (classSizeRadioGroup.getCheckedRadioButtonId() == R.id.ClassAdd_groupRadio) {
            EditText groupSizeEdit = findViewById(R.id.ClassAdd_groupSizeEdit);
            String groupSize = groupSizeEdit.getText().toString().trim();

            if (groupSize.isEmpty()) {
                groupSizeEdit.setError("請輸入群組人數");
                groupSizeEdit.requestFocus();
                return false;
            }

            try {
                int size = Integer.parseInt(groupSize);
                if (size <= 1) {
                    groupSizeEdit.setError("群組人數必須大於 1");
                    groupSizeEdit.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                groupSizeEdit.setError("請輸入有效的數字");
                groupSizeEdit.requestFocus();
                return false;
            }
        }

        return true; // 所有驗證通過
    }
    private boolean validateEditTexts(int[] editTextIds, String[] errorMessages) {
        for (int i = 0; i < editTextIds.length; i++) {
            if (!validateEditText(editTextIds[i], errorMessages[i])) {
                return false; // 返回第一個發現的錯誤
            }
        }
        return true;
    }
    private boolean validateEditText(int editTextId, String errorMessage) {
        EditText editText = findViewById(editTextId);
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError(errorMessage);
            editText.requestFocus();
            return false;
        } else {
            editText.setError(null);
        }
        return true;
    }
    private boolean validateSpinners(Spinner[] spinners, String[] errorMessages) {
        for (int i = 0; i < spinners.length; i++) {
            if (!validateSpinner(spinners[i], errorMessages[i])) {
                return false; // 返回第一個發現的錯誤
            }
        }
        return true;
    }
    private boolean validateSpinner(Spinner spinner, String errorMessage) {
        if (spinner.getSelectedItemPosition() == 0) { // 預設第一項為無效選項
            View selectedView = spinner.getSelectedView();
            if (selectedView != null && selectedView instanceof TextView) {
                ((TextView) selectedView).setError(errorMessage);
                ((TextView) selectedView).requestFocus();
            }
            return false;
        }
        return true;
    }

}
