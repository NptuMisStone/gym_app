package com.NPTUMisStone.gym_app.User.Class;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class ClassList extends AppCompatActivity {
    private ClassListAdapter cityadapter;
    private BottomSheetDialog filterDialog;
    Connection MyConnection;
    ArrayList<User_All_Class_Data> class_data = new ArrayList<>();
    private ProgressBar progressBar;
    ImageView filterbtn, searchbtn;
    TextView searchtext;
    ExpandableListView expandableListView;
    List<String> parentList; // 父節點縣市
    HashMap<String, List<String>> childMap; // 子節點：行政區
    EditText minmoney, maxmoney;
    ArrayList<String> selectedCities, selectedAreas;
    String searchtxt;

    public static class ClassListAdapter extends BaseExpandableListAdapter {
        private Context context;
        private List<String> parentList;
        private HashMap<String, List<String>> childMap;
        private HashMap<String, Boolean> groupCheckState; // 保存父節點的 CheckBox 狀態
        private HashMap<String, HashMap<String, Boolean>> childCheckState; // 保存子節點的 CheckBox 狀態

        private ArrayList<String> selectedCities; // 選中的縣市
        private ArrayList<String> selectedAreas;  // 選中的行政區

        public ClassListAdapter(Context context, List<String> parentList, HashMap<String, List<String>> childMap) {
            this.context = context;
            this.parentList = parentList;
            this.childMap = childMap;

            // 初始化狀態保存
            groupCheckState = new HashMap<>();
            childCheckState = new HashMap<>();
            selectedCities = new ArrayList<>();
            selectedAreas = new ArrayList<>();

            for (String group : parentList) {
                groupCheckState.put(group, false);
                HashMap<String, Boolean> childStates = new HashMap<>();
                for (String child : childMap.get(group)) {
                    childStates.put(child, false);
                }
                childCheckState.put(group, childStates);
            }
        }

        // Getter for selected cities and areas
        public ArrayList<String> getSelectedCities() {
            return selectedCities;
        }

        public ArrayList<String> getSelectedAreas() {
            return selectedAreas;
        }

        @Override
        public int getGroupCount() {
            return parentList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childMap.get(parentList.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return parentList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childMap.get(parentList.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String cityName = (String) getGroup(groupPosition);
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.expandable_list_group, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.group_name);
            CheckBox checkBox = convertView.findViewById(R.id.group_checkbox);
            ImageView indicator = convertView.findViewById(R.id.group_indicator);

            textView.setText(cityName);

            // 下拉箭頭的旋轉效果
            if (isExpanded) {
                indicator.setRotation(180);
            } else {
                indicator.setRotation(0);
            }

            // 設置 CheckBox 的狀態
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(groupCheckState.get(cityName));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                groupCheckState.put(cityName, isChecked);

                if (isChecked) {
                    if (!selectedCities.contains(cityName)) {
                        selectedCities.add(cityName);
                    }
                } else {
                    selectedCities.remove(cityName);
                }

                // 同步子節點的狀態
                HashMap<String, Boolean> childStates = childCheckState.get(cityName);
                if (childStates != null) {
                    for (String key : childStates.keySet()) {
                        childStates.put(key, isChecked);

                        if (isChecked) {
                            if (!selectedAreas.contains(key)) {
                                selectedAreas.add(key);
                            }
                        } else {
                            selectedAreas.remove(key);
                        }
                    }
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String areaName = (String) getChild(groupPosition, childPosition);
            String groupName = (String) getGroup(groupPosition);

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.expandable_list_child, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.child_name);
            CheckBox checkBox = convertView.findViewById(R.id.child_checkbox);

            textView.setText(areaName);
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(childCheckState.get(groupName).get(areaName));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                childCheckState.get(groupName).put(areaName, isChecked);

                if (isChecked) {
                    if (!selectedAreas.contains(areaName)) {
                        selectedAreas.add(areaName);
                    }
                } else {
                    selectedAreas.remove(areaName);
                }
            });

            return convertView;
        }

        public void resetCheckStates() {
            // 重置父節點的勾選狀態
            for (String group : groupCheckState.keySet()) {
                groupCheckState.put(group, false);
            }

            // 重置子節點的勾選狀態
            for (String group : childCheckState.keySet()) {
                HashMap<String, Boolean> childStates = childCheckState.get(group);
                if (childStates != null) {
                    for (String child : childStates.keySet()) {
                        childStates.put(child, false);
                    }
                }
            }

            // 清空已選擇的城市和行政區
            selectedCities.clear();
            selectedAreas.clear();

            // 通知數據已改變，更新 UI
            notifyDataSetChanged();
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_class_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fetchClass();
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        progressBar = findViewById(R.id.progressBar_allclass);
        progressBar.setVisibility(View.VISIBLE);
        filterbtn = findViewById(R.id.class_filter_btn);
        filterbtn.setOnClickListener(view -> showFilterDialog());
        searchtext = findViewById(R.id.class_filter_searchtext);
        searchbtn = findViewById(R.id.class_search_btn);
        searchbtn.setOnClickListener(view -> {
            // 顯示進度條
            progressBar.setVisibility(View.VISIBLE);

            // 將所有過濾條件初始化
            selectedCities = cityadapter != null ? cityadapter.getSelectedCities() : new ArrayList<>();
            selectedAreas = cityadapter != null ? cityadapter.getSelectedAreas() : new ArrayList<>();
            searchtxt = searchtext.getText().toString();
            gendercheck = ""; // 根據你的需求預設為空或某一值
            peoplecheck = "0"; // 預設為 0 表示全部人數
            minmoney = findViewById(R.id.filter_class_price_min);
            maxmoney = findViewById(R.id.filter_class_price_max);

            // 呼叫篩選方法
            fetchfilter();
        });


    }

    private void fetchClass() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                class_data.clear();
                String sql = "SELECT * FROM [健身教練課程-有排課的]";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next()) {
                    class_data.add(new User_All_Class_Data(
                            rs.getInt("課程編號"),
                            rs.getBytes("課程圖片"),
                            rs.getString("課程名稱"),
                            rs.getString("課程費用"),
                            rs.getString("健身教練姓名"),
                            rs.getString("課程內容介紹"),
                            rs.getString("上課人數")
                    ));
                }
                rs.close();
                searchStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });
    }

    private void fetchfilter() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();

                searchtxt = (searchtxt == null || searchtxt.trim().isEmpty()) ? "%" : "%" + searchtxt + "%";
                selectedTypeID = (selectedTypeID == null || selectedTypeID.trim().isEmpty()) ? "%" : selectedTypeID;
                selectedTypeID = (selectedTypeID.equals("0")) ? "%" : selectedTypeID;
                gendercheck = (gendercheck == null || gendercheck.trim().isEmpty()) ? "%" : gendercheck;
                gendercheck = (gendercheck.equals("0")) ? "%" : gendercheck;
                peoplecheck = (peoplecheck == null || peoplecheck.trim().isEmpty()) ? "0" : peoplecheck;

                // 獲取最大和最小費用
                String minMoneyText = (minmoney != null && minmoney.getText() != null && !minmoney.getText().toString().isEmpty())
                        ? minmoney.getText().toString() : "0";
                String maxMoneyText = (maxmoney != null && maxmoney.getText() != null && !maxmoney.getText().toString().isEmpty())
                        ? maxmoney.getText().toString() : "9999";

                int minMoney = Integer.parseInt(minMoneyText);
                int maxMoney = Integer.parseInt(maxMoneyText);
                // 檢查最大值是否小於最小值
                if (minMoney > maxMoney) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(ClassList.this, "最小值不能大於最大值，請重新輸入！", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // 隱藏進度條
                    });
                    return; // 結束篩選
                }
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append(
                        "SELECT  * FROM [健身教練課程-有排課的] " +
                                "WHERE [課程名稱] LIKE ? " +
                                "AND [分類編號] LIKE ? " +
                                "AND [健身教練性別] LIKE ? " +
                                "AND ([課程費用] >= ? AND [課程費用] <= ?) ");

                switch (peoplecheck) {
                    case "0":
                        sqlBuilder.append("AND [上課人數] LIKE ? ");
                        break;
                    case "1":
                        sqlBuilder.append("AND [上課人數] = ? ");
                        break;
                    case "2":
                        sqlBuilder.append("AND [上課人數] > ? ");
                        break;
                }

                if (selectedAreas != null && !selectedAreas.isEmpty()) {
                    sqlBuilder.append("AND ( ");
                    for (int i = 0; i < selectedAreas.size(); i++) {
                        if (i > 0) sqlBuilder.append(" OR ");
                        sqlBuilder.append("[行政區] LIKE ?");
                    }
                    sqlBuilder.append(" ) ");
                } else {
                    sqlBuilder.append("AND [行政區] LIKE ? ");
                }


                PreparedStatement statement = MyConnection.prepareStatement(sqlBuilder.toString());
                int index = 1;
                statement.setString(index++, searchtxt);
                statement.setString(index++, selectedTypeID);
                statement.setString(index++, gendercheck);
                statement.setInt(index++, minMoney);
                statement.setInt(index++, maxMoney);

                switch (peoplecheck) {
                    case "0":
                        statement.setString(index++, "%");
                        break;
                    case "1":
                        statement.setString(index++, "1");
                        break;
                    case "2":
                        statement.setString(index++, "1");
                        break;
                }
                if (selectedAreas != null && !selectedAreas.isEmpty()) {
                    for (String area : selectedAreas) {
                        statement.setString(index++, "%" + area + "%");
                    }
                } else {
                    statement.setString(index++, "%");
                }

                ResultSet rs = statement.executeQuery();
                class_data.clear();
                while (rs.next()) {
                    class_data.add(new User_All_Class_Data(
                            rs.getInt("課程編號"),
                            rs.getBytes("課程圖片"),
                            rs.getString("課程名稱"),
                            rs.getString("課程費用"),
                            rs.getString("健身教練姓名"),
                            rs.getString("課程內容介紹"),
                            rs.getString("上課人數")
                    ));
                }
                rs.close();
                statement.close();
            } catch (SQLException e) {
                Log.e("SQL Error", Objects.requireNonNull(e.getMessage()));
            }
            // 更新 UI
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });
    }
    private void updateUI() {
        All_ClassAdapter classAdapter = new All_ClassAdapter(this, class_data, findViewById(R.id.main));
        RecyclerView classRecyclerView = findViewById(R.id.userClassRecycleview);
        classRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        classRecyclerView.setAdapter(classAdapter);
        progressBar.setVisibility(View.GONE);
    }

    static class User_All_Class_Data {
        int classID;
        byte[] classimage;
        String className, classPrice, coachName, classIntro, classPeople;

        static ArrayList<User_All_Class_Data> classData = new ArrayList<>();

        public User_All_Class_Data(int classID, byte[] classimage, String className, String classPrice, String coachName, String classIntro, String classPeople) {
            this.classID = classID;
            this.classimage = classimage;
            this.className = className;
            this.classPrice = classPrice;
            this.coachName = coachName;
            this.classIntro = classIntro;
            this.classPeople = classPeople;
        }

        public static ArrayList<User_All_Class_Data> getClassData() {
            if (classData == null) {
                return null;
            }
            return classData;
        }

        private int getClassID() {
            return classID;
        }

        private byte[] getClassImage() {
            return classimage;
        }

        private String getClassName() {
            return className;
        }

        private String getClassPrice() {
            return classPrice;
        }

        private String getCoachName() {
            return coachName;
        }

        private String getClassIntro() {
            return classIntro;
        }

        private String getClassPeople() {
            return classPeople;
        }
    }

    class All_ClassAdapter extends RecyclerView.Adapter<ClassList.All_ClassAdapter.ViewHolder> {

        List<User_All_Class_Data> class_dataList;
        Context context;
        View view;

        public All_ClassAdapter(Context context, List<User_All_Class_Data> class_dataList, View view) {
            this.context = context;
            this.class_dataList = class_dataList;
            this.view = view;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView class_image;
            TextView class_people_sign, class_name, class_price, coach_name, class_intro, class_people;
            ImageButton like_class_btn, moreindo_btn;

            public ViewHolder(View itemView) {
                super(itemView);
                class_image = itemView.findViewById(R.id.user_all_class_img);
                class_people_sign = itemView.findViewById(R.id.user_all_class_people_sign);
                class_name = itemView.findViewById(R.id.user_all_class_classname);
                class_price = itemView.findViewById(R.id.user_all_class_price);
                coach_name = itemView.findViewById(R.id.user_all_class_coachname);
                class_intro = itemView.findViewById(R.id.user_all_class_intro);
                class_people = itemView.findViewById(R.id.user_all_class_people);

                like_class_btn = itemView.findViewById(R.id.user_like_all_class_btn);
                moreindo_btn = itemView.findViewById(R.id.user_all_class_info);
            }
        }

        @NonNull
        @Override
        public ClassList.All_ClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_class_list_item, parent, false);
            return new ClassList.All_ClassAdapter.ViewHolder(view);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void onBindViewHolder(@NonNull ClassList.All_ClassAdapter.ViewHolder holder, int position) {
            User_All_Class_Data item = class_dataList.get(position);

            if (item.getClassImage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getClassImage());
                holder.class_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.class_name.setText(item.getClassName());
            holder.class_price.setText("$" + item.getClassPrice().split("\\.")[0] + "/堂");
            holder.coach_name.setText(item.getCoachName());
            holder.class_intro.setText(item.getClassIntro());
            holder.class_people.setText("人數：" + item.getClassPeople() + "人");
            holder.moreindo_btn.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("看更多課程ID", item.getClassID());
                editor.apply(); // 保存
                Intent intent = new Intent(context, ClassDetail.class);
                startActivity(intent);
            });
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                String sql = "SELECT COUNT(*) FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?";
                PreparedStatement Statement = MyConnection.prepareStatement(sql);
                Statement.setInt(1, item.getClassID());
                Statement.setInt(2, User.getInstance().getUserId());
                ResultSet rs = Statement.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        holder.like_class_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user_like_ic_love));
                    } else {
                        holder.like_class_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user_like_ic_not_love));
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                Log.e("SQL", "Error checking like status", e);
            }
            holder.like_class_btn.setOnClickListener(v -> {
                Drawable currentDrawable = holder.like_class_btn.getDrawable();
                if (Objects.equals(currentDrawable.getConstantState(), Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.user_like_ic_not_love)).getConstantState())) {
                    // 如果當前是 dislike 狀態，切換到 like
                    holder.like_class_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user_like_ic_love));

                    // 更新資料庫
                    try {
                        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                        String insertSql = "INSERT INTO 課程被收藏 (使用者編號, 課程編號) VALUES (?, ?)";
                        PreparedStatement insertStatement = MyConnection.prepareStatement(insertSql);
                        insertStatement.setInt(1, User.getInstance().getUserId());
                        insertStatement.setInt(2, item.getClassID());
                        insertStatement.executeUpdate();
                        insertStatement.close();
                    } catch (SQLException e) {
                        Log.e("SQL", "Error inserting like status", e);
                    }
                } else {
                    // 如果當前是 like 狀態，切換到 dislike
                    holder.like_class_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user_like_ic_not_love));
                    try {
                        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                        String deleteSql = "DELETE FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?";
                        PreparedStatement deleteStatement = MyConnection.prepareStatement(deleteSql);
                        deleteStatement.setInt(1, item.getClassID());
                        deleteStatement.setInt(2, User.getInstance().getUserId());
                        deleteStatement.executeUpdate();
                        deleteStatement.close();
                    } catch (SQLException e) {
                        Log.e("SQL", "Error deleting like status", e);
                    }
                }

            });
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                String SQL = "SELECT 上課人數 FROM 健身教練課程 WHERE 課程編號 = ? ";
                PreparedStatement Statement = MyConnection.prepareStatement(SQL);
                Statement.setInt(1, item.getClassID());
                ResultSet rs = Statement.executeQuery();
                while (rs.next()) {
                    if (item.getClassPeople().equals("1")) {
                        holder.class_people_sign.setText("一對一");
                    } else {
                        holder.class_people_sign.setText("團體");
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                Log.e("SQL", "Error checking like status", e);
            }

        }


        @Override
        public int getItemCount() {
            return class_dataList.size();
        }
    }

    public void user_All_Class_goback(View view) {
        finish();
    }

    // 数据模型
    static class City { //縣市
        int cityId;
        String cityName;
        List<Area> areas = new ArrayList<>();

        public City(int cityId, String cityName) {
            this.cityId = cityId;
            this.cityName = cityName;
        }
    }

    static class Area { //行政區
        int areaId;
        String areaName;

        public Area(int areaId, String areaName) {
            this.areaId = areaId;
            this.areaName = areaName;
        }
    }

    static class ClassType {
        int classTypeId; // 分类编号
        String classTypeName; // 分类名称

        public ClassType(int classTypeId, String classTypeName) {
            this.classTypeId = classTypeId;
            this.classTypeName = classTypeName;
        }
    }

    // 查询分类数据
    private List<ClassType> getClassTypeData() {
        List<ClassType> classTypes = new ArrayList<>();
        // 数据库操作
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            String sql = "SELECT 分類編號, 分類名稱 FROM [健身教練課程-有排課的]";
            PreparedStatement statement = MyConnection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            // 添加默认项 "全部"
            classTypes.add(new ClassType(0, "全部")); // 0 表示全部分类
            while (resultSet.next()) {
                int classTypeId = resultSet.getInt("分類編號");
                String classTypeName = resultSet.getString("分類名稱");
                classTypes.add(new ClassType(classTypeId, classTypeName));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            Log.e("SQL", "Error fetching class type data", e);
        }
        return classTypes;
    }

    // 查询县市和行政区数据
    private List<City> getCityAndAreaData() {
        List<City> cities = new ArrayList<>();
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            String citySql = "SELECT 縣市id, 縣市 FROM 縣市";
            PreparedStatement cityStmt = MyConnection.prepareStatement(citySql);
            ResultSet cityRs = cityStmt.executeQuery();
            while (cityRs.next()) {
                int cityId = cityRs.getInt("縣市id");
                String cityName = cityRs.getString("縣市");
                City city = new City(cityId, cityName);

                String areaSql = "SELECT 行政區id, 行政區 FROM 行政區 WHERE 縣市id = ?";
                PreparedStatement areaStmt = MyConnection.prepareStatement(areaSql);
                areaStmt.setInt(1, cityId);
                ResultSet areaRs = areaStmt.executeQuery();
                while (areaRs.next()) {
                    int areaId = areaRs.getInt("行政區id");
                    String areaName = areaRs.getString("行政區");
                    city.areas.add(new Area(areaId, areaName));
                }
                cities.add(city);
            }
        } catch (SQLException e) {
            Log.e("SQL", "Error fetching city and area data", e);
        }
        // 缓存数据
        return cities;
    }

    private void setupExpandableListView(List<City> cityList) {
        parentList = new ArrayList<>();
        childMap = new HashMap<>();

        for (City city : cityList) {
            parentList.add(city.cityName);
            List<String> areas = new ArrayList<>();
            for (Area area : city.areas) {
                areas.add(area.areaName);
            }
            childMap.put(city.cityName, areas);
        }
        cityadapter = new ClassListAdapter(this, parentList, childMap);
        expandableListView.setAdapter(cityadapter);


    }

    String gendercheck, peoplecheck;

    private void showFilterDialog() {
        if (isFinishing() || isDestroyed()) {
            return; // 避免 Activity 已销毁时尝试显示
        }
        if (filterDialog != null) {
            filterDialog.show();
            return;
        }

        filterDialog = new BottomSheetDialog(this);
        View filterView = LayoutInflater.from(this).inflate(R.layout.user_class_list_filter, null);
        filterDialog.setContentView(filterView);

        View bottomSheet = filterDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        expandableListView = filterView.findViewById(R.id.filter_class_area);
        Spinner typeSpinner = filterView.findViewById(R.id.filter_class_type_spinner);

        // 分別加載數據
        loadCityAndAreaData();
        loadClassTypeData(typeSpinner);

        RadioGroup gendergroup = filterView.findViewById(R.id.filter_class_coachgender_radiobuttongroup);
        RadioGroup peoplegroup = filterView.findViewById(R.id.filter_class_people_radiobuttongroup);
        Button resetButton = filterView.findViewById(R.id.btn_reset);
        resetButton.setOnClickListener(v -> {
            // 重置篩選條件
            gendergroup.check(R.id.filter_class_coachgender_all);
            peoplegroup.check(R.id.filter_class_people_all);

            // 重置價格範圍
            minmoney = filterView.findViewById(R.id.filter_class_price_min);
            maxmoney = filterView.findViewById(R.id.filter_class_price_max);
            if (minmoney != null) minmoney.setText("");
            if (maxmoney != null) maxmoney.setText("");

            // 重置城市和區域選擇
            if (cityadapter != null) {
                cityadapter.resetCheckStates();
                cityadapter.notifyDataSetChanged();
            }

            // 重置分類
            if (typeSpinner != null && typeSpinner.getAdapter() != null) {
                typeSpinner.setSelection(0);
            }

            // 顯示提示
            Toast.makeText(this, "篩選條件已重置", Toast.LENGTH_SHORT).show();
        });
        Button applyButton = filterView.findViewById(R.id.btn_apply);
        applyButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            // 取得選中的 RadioButton ID
            int selectedGenderId = gendergroup.getCheckedRadioButtonId();

            // 判斷選中的 RadioButton
            if (selectedGenderId == R.id.filter_class_coachgender_boy) {
                // 男性被選中
                gendercheck = "1";
            } else if (selectedGenderId == R.id.filter_class_coachgender_girl) {
                // 女性被選中
                gendercheck = "2";
            } else if (selectedGenderId == R.id.filter_class_coachgender_nogender) {
                // 全部被選中
                gendercheck = "3";
            } else {
                gendercheck = "";
            }
            // 取得選中的 RadioButton ID
            int selectedPeopleId = peoplegroup.getCheckedRadioButtonId();

            // 判斷選中的 RadioButton
            if (selectedPeopleId == R.id.filter_class_people_all) {
                // 全部人數
                peoplecheck = "0";
            } else if (selectedPeopleId == R.id.filter_class_people_one) {
                // 1對1
                peoplecheck = "1";
            } else if (selectedPeopleId == R.id.filter_class_people_many) {
                // 團體
                peoplecheck = "2";
            }
            selectedCities = cityadapter.getSelectedCities();
            selectedAreas = cityadapter.getSelectedAreas();
            searchtxt = searchtext.getText().toString();
            minmoney = filterView.findViewById(R.id.filter_class_price_min);
            maxmoney = filterView.findViewById(R.id.filter_class_price_max);
            fetchfilter();

            filterDialog.dismiss();
        });

        filterDialog.show();
    }

    // 加載縣市和行政區的數據
    private void loadCityAndAreaData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<City> cities = getCityAndAreaData();
            new Handler(Looper.getMainLooper()).post(() -> setupExpandableListView(cities));
        });
    }

    // 加載分類數據
    private void loadClassTypeData(Spinner spinner) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ClassType> classTypes = getClassTypeData();
            new Handler(Looper.getMainLooper()).post(() -> setupTypeSpinner(spinner, classTypes));
        });
    }

    String selectedTypeID;

    // 设置 Spinner 数据
    private void setupTypeSpinner(Spinner spinner, List<ClassType> classTypes) {
        List<String> typeNames = new ArrayList<>();
        for (ClassType type : classTypes) {
            typeNames.add(type.classTypeName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                typeNames
        );
        spinner.setAdapter(adapter);

        // 設置監聽器
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTypeID = String.valueOf(classTypes.get(position).classTypeId); // 獲取選擇的項目 ID
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 用戶未選擇任何項目時的處理（可以忽略或進行提示）
            }
        });
    }

}






