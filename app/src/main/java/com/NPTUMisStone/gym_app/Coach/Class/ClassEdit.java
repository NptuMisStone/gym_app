package com.NPTUMisStone.gym_app.Coach.Class;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
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

public class ClassEdit extends AppCompatActivity {

    // 圖片相關控件
    private ImageView showImage; // 用於顯示上傳的圖片
    private Uri uri;             // 圖片的 URI
    private byte[] currentImageBytes; // 用來保存加載的初始圖片
    private byte[] updatedImageBytes; // 用來保存用戶更新的圖片
    private Connection MyConnection;
    private int classId;

    // 上課地點相關控件
    private RadioGroup locationRadioGroup;
    private RadioButton trainerStoreRadio, clientLocationRadio, otherLocationRadio;
    private EditText locationNameEdit, addressEdit;
    private Spinner citySpinner, areaSpinner;

    // 上課人數相關控件
    private RadioGroup classSizeRadioGroup;
    private RadioButton oneToOneRadio, groupRadio;
    private EditText groupSizeEdit;

    // 其他控件
    private Spinner classTypeSpinner, classDurationSpinner;

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
        setContentView(R.layout.coach_class_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 接收課程 ID
        classId = getIntent().getIntExtra("classId", -1);
        if (classId == -1) {
            Toast.makeText(this, "課程資料載入失敗", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化控件
        initializeControls();

        // 加載數據
        loadInitialData();

        // 設置監聽器
        setupListeners();

        // 設置初始選項
        initializeDefaultSelections();


        // 加載課程資料
        loadClassData();
    }

    /**
     * 初始化控件
     */
    private void initializeControls() {
        // 上課地點選擇相關控件
        locationRadioGroup = findViewById(R.id.ClassEdit_locationRadioGroup);
        trainerStoreRadio = findViewById(R.id.ClassEdit_trainerStoreRadio);
        clientLocationRadio = findViewById(R.id.ClassEdit_clientLocationRadio);
        otherLocationRadio = findViewById(R.id.ClassEdit_otherLocationRadio);
        locationNameEdit = findViewById(R.id.ClassEdit_locationNameEdit);
        citySpinner = findViewById(R.id.ClassEdit_citySpinner);
        areaSpinner = findViewById(R.id.ClassEdit_areaSpinner);
        addressEdit = findViewById(R.id.ClassEdit_addressEdit);

        // 上課人數選擇相關控件
        classSizeRadioGroup = findViewById(R.id.ClassEdit_classSizeRadioGroup);
        oneToOneRadio = findViewById(R.id.ClassEdit_oneToOneRadio);
        groupRadio = findViewById(R.id.ClassEdit_groupRadio);
        groupSizeEdit = findViewById(R.id.ClassEdit_groupSizeEdit);

        // 其他控件
        classTypeSpinner = findViewById(R.id.ClassEdit_typeSpinner);
        classDurationSpinner = findViewById(R.id.ClassEdit_durationSpinner);
        showImage = findViewById(R.id.ClassEdit_uploadImage);

        // 圖片上傳按鈕
        Button uploadButton = findViewById(R.id.ClassEdit_uploadButton);
        uploadButton.setOnClickListener(v -> changeImage());

        // 返回按鈕
        findViewById(R.id.ClassEdit_backButton).setOnClickListener(v -> finish());

        // 保存按鈕
        findViewById(R.id.ClassEdit_saveButton).setOnClickListener(v -> saveCourse());

        // 刪除按鈕
        findViewById(R.id.ClassEdit_delButton).setOnClickListener(v -> deleteCourse());
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
                ? R.id.ClassEdit_trainerStoreRadio
                : R.id.ClassEdit_clientLocationRadio);
        classSizeRadioGroup.check(R.id.ClassEdit_oneToOneRadio);
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
            groupSizeEdit.setVisibility(checkedId == R.id.ClassEdit_groupRadio ? View.VISIBLE : View.GONE);
            clientLocationRadio.setEnabled(checkedId != R.id.ClassEdit_groupRadio);

            // 如果是團體課程且當前地點為 "到府"，自動切換為 "其他地點"
            if (checkedId == R.id.ClassEdit_groupRadio &&
                    locationRadioGroup.getCheckedRadioButtonId() == R.id.ClassEdit_clientLocationRadio) {
                locationRadioGroup.check(R.id.ClassEdit_otherLocationRadio);
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
        if (selectedLocationId == R.id.ClassEdit_trainerStoreRadio) {
            showLocationInputs(false);
            showCityAreaInputs(false);
        } else if (selectedLocationId == R.id.ClassEdit_clientLocationRadio) {
            showCityAreaInputs(true);
            showLocationInputs(false);
        } else if (selectedLocationId == R.id.ClassEdit_otherLocationRadio) {
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
    /**
     * 改變圖片
     */
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

                        // 將新圖片轉為 byte[] 並保存到 updatedImageBytes
                        updatedImageBytes = getBytesFromUri(uri);
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "取消圖片上傳", Toast.LENGTH_SHORT).show();
                }
            }
    );


    /**
     * 加載課程資料
     */
    private void loadClassData() {
        String query = "SELECT * FROM [健身教練課程] WHERE [課程編號] = ?";
        try (PreparedStatement statement = MyConnection.prepareStatement(query)) {
            statement.setInt(1, classId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // 填充課程資訊到頁面元素
                ((EditText) findViewById(R.id.ClassEdit_nameEdit)).setText(resultSet.getString("課程名稱"));
                int fee = (int) Math.floor(resultSet.getDouble("課程費用"));
                ((EditText) findViewById(R.id.ClassEdit_feeEdit)).setText(String.valueOf(fee));
                ((EditText) findViewById(R.id.ClassEdit_descriptionEdit)).setText(resultSet.getString("課程內容介紹"));
                ((EditText) findViewById(R.id.ClassEdit_equipmentEdit)).setText(resultSet.getString("所需設備"));

                // 回綁課程分類
                int classTypeId = resultSet.getInt("分類編號");
                for (int i = 0; i < classTypeSpinner.getCount(); i++) {
                    ClassType item = (ClassType) classTypeSpinner.getItemAtPosition(i);
                    if (item.classTypeId == classTypeId) {
                        classTypeSpinner.setSelection(i);
                        break;
                    }
                }

                // 回綁課程時長
                int durationId = resultSet.getInt("課程時間長度");
                for (int i = 0; i < classDurationSpinner.getCount(); i++) {
                    String item = (String) classDurationSpinner.getItemAtPosition(i);
                    if (item.equals(durationId + " 分鐘")) {
                        classDurationSpinner.setSelection(i);
                        break;
                    }
                }

                // 回綁上課人數
                int people = resultSet.getInt("上課人數");
                if (people == 1) {
                    ((RadioButton) findViewById(R.id.ClassEdit_oneToOneRadio)).setChecked(true);
                } else if (people > 1) {
                    ((RadioButton) findViewById(R.id.ClassEdit_groupRadio)).setChecked(true);
                    EditText groupSizeEdit = findViewById(R.id.ClassEdit_groupSizeEdit);
                    groupSizeEdit.setVisibility(View.VISIBLE);
                    groupSizeEdit.setText(String.valueOf(people));
                }

                // 回綁上課地點
                int locationType = resultSet.getInt("地點類型");
                Spinner citySpinner = findViewById(R.id.ClassEdit_citySpinner);
                Spinner areaSpinner = findViewById(R.id.ClassEdit_areaSpinner);
                if (locationType == 1) {
                    ((RadioButton) findViewById(R.id.ClassEdit_trainerStoreRadio)).setChecked(true);
                } else {
                    int cityId = resultSet.getInt("縣市id");
                    int areaId = resultSet.getInt("行政區id");

                    if (locationType == 2) {
                        ((RadioButton) findViewById(R.id.ClassEdit_clientLocationRadio)).setChecked(true);
                    } else if (locationType == 3) {
                        ((RadioButton) findViewById(R.id.ClassEdit_otherLocationRadio)).setChecked(true);
                        EditText locationNameEdit = findViewById(R.id.ClassEdit_locationNameEdit);
                        locationNameEdit.setVisibility(View.VISIBLE);
                        locationNameEdit.setText(resultSet.getString("地點名稱"));

                        EditText addressEdit = findViewById(R.id.ClassEdit_addressEdit);
                        addressEdit.setVisibility(View.VISIBLE);
                        addressEdit.setText(resultSet.getString("地點地址"));
                    }

                    // 顯示縣市和行政區的下拉選單
                    citySpinner.setVisibility(View.VISIBLE);
                    areaSpinner.setVisibility(View.VISIBLE);

                    // 回綁縣市，並延遲回綁行政區
                    for (int i = 0; i < citySpinner.getCount(); i++) {
                        City city = (City) citySpinner.getItemAtPosition(i);
                        if (city.cityId == cityId) {
                            citySpinner.setSelection(i);
                            Log.d("DEBUG", "Selected city: " + city.cityName);

                            // 使用 Handler 延遲加載行政區
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                loadAreaData(cityId, areaSpinner, areaId);
                            }, 200); // 延遲 200 毫秒，確保 UI 完全更新
                            break;
                        }
                    }
                }

                // 加載課程圖片
                currentImageBytes = resultSet.getBytes("課程圖片");
                if (currentImageBytes != null && currentImageBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(currentImageBytes, 0, currentImageBytes.length);
                    showImage.setImageBitmap(bitmap);
                }
            } else {
                Toast.makeText(this, "課程資料未找到", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "加載課程失敗，請稍後再試", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAreaData(int cityId, Spinner areaSpinner, int selectedAreaId) {
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

            /// 延遲設置行政區選擇
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                for (int j = 0; j < areaList.size(); j++) {
                    if (areaList.get(j).areaId == selectedAreaId) {
                        areaSpinner.setSelection(j);
                        Log.d("DEBUG", "Selected area: " + areaList.get(j).areaName);
                        break;
                    }
                }
            }, 200); // 延遲 200 毫秒

        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "加載鄉鎮區失敗", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveCourse() {
        Log.d("SaveCourse", "開始執行課程儲存邏輯");

        // 驗證欄位
        if (!validateFields()) {
            Log.e("SaveCourse", "欄位驗證失敗");
            return; // 如果欄位驗證失敗，退出方法
        }

        Log.d("SaveCourse", "欄位驗證成功");

        // 優先使用 updatedImageBytes
        byte[] imageBytesToSave = updatedImageBytes != null ? updatedImageBytes : currentImageBytes;
        Log.d("SaveCourse", "準備使用的圖片數據是否存在: " + (imageBytesToSave != null));

        String query = "UPDATE [健身教練課程] " +
                "SET [課程名稱] = ?, " +
                "[分類編號] = ?, " +
                "[課程時間長度] = ?, " +
                "[課程內容介紹] = ?, " +
                "[所需設備] = ?, " +
                "[課程費用] = ?, " +
                "[上課人數] = ?, " +
                "[地點類型] = ?, " +
                "[地點名稱] = ?, " +
                "[地點地址] = ?, " +
                "[縣市id] = ?, " +
                "[行政區id] = ?" +
                (imageBytesToSave != null ? ", [課程圖片] = ?" : "") +
                " WHERE [課程編號] = ?";

        try (PreparedStatement statement = MyConnection.prepareStatement(query)) {
            // 收集用戶輸入資料
            Log.d("SaveCourse", "開始收集用戶輸入資料");

            String courseName = ((EditText) findViewById(R.id.ClassEdit_nameEdit)).getText().toString().trim();
            int courseType = ((ClassType) classTypeSpinner.getSelectedItem()).classTypeId;
            int courseTime = Integer.parseInt(classDurationSpinner.getSelectedItem().toString().replace(" 分鐘", ""));
            String courseDescription = ((EditText) findViewById(R.id.ClassEdit_descriptionEdit)).getText().toString().trim();
            String requiredEquipment = ((EditText) findViewById(R.id.ClassEdit_equipmentEdit)).getText().toString().trim();
            double courseFee = Double.parseDouble(((EditText) findViewById(R.id.ClassEdit_feeEdit)).getText().toString());
            int classSize = classSizeRadioGroup.getCheckedRadioButtonId() == R.id.ClassEdit_oneToOneRadio ? 1 : Integer.parseInt(groupSizeEdit.getText().toString());

            Log.d("SaveCourse", "收集完成: 課程名稱=" + courseName + ", 類型=" + courseType + ", 時間=" + courseTime + "分鐘");

            int locationType = locationRadioGroup.getCheckedRadioButtonId() == R.id.ClassEdit_trainerStoreRadio ? 1 :
                    locationRadioGroup.getCheckedRadioButtonId() == R.id.ClassEdit_clientLocationRadio ? 2 : 3;

            String locationName = null;
            String locationAddress = null;

            int cityId = -1;
            int areaId = -1;

            if (locationType == 1) {
                Log.d("SaveCourse", "服務地點為教練門市");
                try (PreparedStatement stmt = MyConnection.prepareStatement(
                        "SELECT [服務地點名稱], [縣市id], [行政區id], [服務地點地址] FROM [健身教練審核合併] WHERE [健身教練編號] = ?")) {
                    stmt.setInt(1, Coach.getInstance().getCoachId());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        locationName = rs.getString("服務地點名稱");
                        cityId = rs.getInt("縣市id");
                        areaId = rs.getInt("行政區id");
                        locationAddress = rs.getString("服務地點地址");
                        Log.d("SaveCourse", "服務地點資訊讀取成功: " + locationName + ", 地址: " + locationAddress);
                    } else {
                        Log.e("SaveCourse", "未找到教練的服務地點資訊");
                        Toast.makeText(this, "未找到教練的服務地點資訊", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                Log.d("SaveCourse", "服務地點為其他類型");
                if (citySpinner.getSelectedItem() == null || areaSpinner.getSelectedItem() == null) {
                    Log.e("SaveCourse", "縣市或行政區選擇無效");
                    Toast.makeText(this, "請選擇有效的縣市和行政區", Toast.LENGTH_SHORT).show();
                    return;
                }
                City selectedCity = (City) citySpinner.getSelectedItem();
                Area selectedArea = (Area) areaSpinner.getSelectedItem();

                cityId = selectedCity.cityId;
                areaId = selectedArea.areaId;

                if (locationType == 3) {
                    locationName = ((EditText) findViewById(R.id.ClassEdit_locationNameEdit)).getText().toString().trim();
                    locationAddress = ((EditText) findViewById(R.id.ClassEdit_addressEdit)).getText().toString().trim();
                }
            }

            Log.d("SaveCourse", "準備執行資料庫更新操作");

            // 設定參數
            statement.setString(1, courseName);
            statement.setInt(2, courseType);
            statement.setInt(3, courseTime);
            statement.setString(4, courseDescription);
            statement.setString(5, requiredEquipment);
            statement.setDouble(6, courseFee);
            statement.setInt(7, classSize);
            statement.setInt(8, locationType);
            statement.setString(9, locationType == 3 ? locationName : null);
            statement.setString(10, locationType == 3 ? locationAddress : null);
            statement.setInt(11, cityId);
            statement.setInt(12, areaId);

            if (imageBytesToSave != null) {
                statement.setBytes(13, imageBytesToSave);
                statement.setInt(14, classId);
            } else {
                statement.setInt(13, classId);
            }

            // 執行更新操作
            int rowsAffected = statement.executeUpdate();
            Log.d("SaveCourse", "更新影響的行數: " + rowsAffected);
            if (rowsAffected > 0) {
                Toast.makeText(this, "課程更新成功！", Toast.LENGTH_SHORT).show();
                navigateToClassMain();
            } else {
                Log.e("SaveCourse", "課程更新失敗");
                Toast.makeText(this, "課程更新失敗！", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            Log.e("SaveCourse", "SQL 錯誤", e);
            Toast.makeText(this, "儲存過程中發生錯誤", Toast.LENGTH_SHORT).show();
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
                new int[]{R.id.ClassEdit_nameEdit, R.id.ClassEdit_descriptionEdit, R.id.ClassEdit_equipmentEdit, R.id.ClassEdit_feeEdit},
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

        if (locationType == R.id.ClassEdit_clientLocationRadio) {
            // 如果選擇 "到府(客戶指定地點)"
            if (!validateSpinner(citySpinner, "請選擇縣市") || !validateSpinner(areaSpinner, "請選擇鄉鎮區")) {
                return false;
            }
        } else if (locationType == R.id.ClassEdit_otherLocationRadio) {
            // 如果選擇 "其他(教練指定地點)"
            if (!validateEditText(R.id.ClassEdit_locationNameEdit, "請輸入地點名稱") ||
                    !validateSpinner(citySpinner, "請選擇縣市") ||
                    !validateSpinner(areaSpinner, "請選擇鄉鎮區") ||
                    !validateEditText(R.id.ClassEdit_addressEdit, "請輸入詳細地址")) {
                return false;
            }
        }

        // 驗證群組人數欄位（如果選擇了群組課程）
        if (classSizeRadioGroup.getCheckedRadioButtonId() == R.id.ClassEdit_groupRadio) {
            EditText groupSizeEdit = findViewById(R.id.ClassEdit_groupSizeEdit);
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
    /**
     * 刪除課程
     */
    private void deleteCourse() {
        // 查詢是否有未結束的預約
        String queryCheck = "SELECT COUNT(*) AS count FROM [使用者預約-有預約的] " +
                "WHERE 課程編號 = ? AND (日期 > GETDATE() OR (日期 = GETDATE() AND 結束時間 > CONVERT(VARCHAR, GETDATE(), 108)))";

        try (PreparedStatement checkStmt = MyConnection.prepareStatement(queryCheck)) {
            checkStmt.setInt(1, classId); // 使用課程 ID 設置參數
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                // 如果有未結束的預約，彈出提示
                new AlertDialog.Builder(this)
                        .setTitle("無法刪除課程")
                        .setMessage("該課程仍有未結束的預約，請先於班表移除排班後再嘗試刪除。")
                        .setPositiveButton("確定", (dialog, which) -> dialog.dismiss())
                        .show();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "檢查未結束預約時發生錯誤，請稍後再試", Toast.LENGTH_SHORT).show();
            return;
        }

        // 創建一個確認對話框
        new AlertDialog.Builder(this)
                .setTitle("刪除課程")
                .setMessage("是否確定刪除課程？刪除後課程將無法復原！")
                .setPositiveButton("確定", (dialog, which) -> {
                    // 執行刪除課程邏輯
                    String qry = "DELETE FROM 健身教練課程 WHERE 課程編號 = ?";
                    try (PreparedStatement stmt = MyConnection.prepareStatement(qry)) {
                        stmt.setInt(1, classId); // 使用課程 ID 設置參數
                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            Toast.makeText(this, "課程已成功刪除", Toast.LENGTH_SHORT).show();
                            // 使用 Handler 延遲 2 秒後跳轉到 ClassMain
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                navigateToClassMain(); // 跳轉到主頁面
                            }, 2000); // 2000 毫秒即 2 秒
                        } else {
                            Toast.makeText(this, "刪除失敗，課程不存在", Toast.LENGTH_SHORT).show();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "刪除過程中發生錯誤，請稍後再試", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 點擊取消時，關閉對話框
                    dialog.dismiss();
                })
                .show();
    }


}