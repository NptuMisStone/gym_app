package com.NPTUMisStone.gym_app.User.Search.Coach;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CoachDetail extends AppCompatActivity {
    Connection MyConnection;
    ArrayList<ClassListData> classList = new ArrayList<>();
    private ClassListAdapter classListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_search_coach_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView classRecyclerView = findViewById(R.id.CoachDetail_classListview);
        classRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        classListAdapter = new ClassListAdapter(this, classList);
        classRecyclerView.setAdapter(classListAdapter);
        getTheCoach();
        findViewById(R.id.CoachDetail_goBack).setOnClickListener(v -> finish());
    }
    private void getTheCoach() {
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            int coach_id = getIntent().getIntExtra("coach_id", 0);
            ResultSet resultSet = MyConnection.createStatement().executeQuery("SELECT * FROM 健身教練資料 WHERE 健身教練編號 = " + coach_id);
            if (resultSet.next()) {
                ((TextView)findViewById(R.id.CoachDetail_coachText)).setText(resultSet.getString("健身教練姓名"));
                Bitmap bitmap = ImageHandle.getBitmap(resultSet.getBytes("健身教練圖片"));
                Bitmap resizedBitmap = ImageHandle.resizeBitmap(bitmap); // Resize to a maximum of 1000x1000 pixels
                ((ImageView)findViewById(R.id.CoachDetail_coachImage)).setImageBitmap(resizedBitmap);
                getTheClassList(coach_id);
            }
        } catch (Exception e) {
            Log.e("CoachDetail", "getTheCoach: ", e);
        }
    }

    private void getTheClassList(int coach_id) {
        try {
            ResultSet resultSet = MyConnection.createStatement().executeQuery(
                    "SELECT [健身教練課表].日期, [健身教練課程].課程名稱, [健身教練課程].地點名稱, " +
                            "[健身教練課程].課程費用, [健身教練課程].上課人數, [運動分類清單].分類名稱, " +
                            "[健身教練課程].課程內容介紹 " +
                            "FROM [健身教練課表] " +
                            "JOIN [健身教練課程] ON [健身教練課程].課程編號 = [健身教練課表].課程編號 " +
                            "JOIN [運動分類清單] ON [健身教練課程].分類編號 = [運動分類清單].分類編號 " +
                            "WHERE [健身教練課程].健身教練編號 = " + coach_id
            );
            while (resultSet.next()) {
                classList.add(new ClassListData(
                        resultSet.getString("課程名稱"),
                        resultSet.getString("日期"),
                        resultSet.getString("地點名稱"),
                        resultSet.getInt("課程費用"),
                        resultSet.getInt("上課人數"),
                        resultSet.getString("分類名稱"),
                        resultSet.getString("課程內容介紹")
                ));
            }
            classListAdapter.notifyItemRangeChanged(0, classList.size());
        }catch (Exception e){
            Log.e("CoachDetail", "getTheClassList: ", e);
        }
    }
    static class ClassListData {
        String name;
        String time;
        String place;
        int price;
        int people;
        String type;
        String description;

        public ClassListData(String name, String time, String place, int price, int people, String type, String description) {
            this.name = name;
            this.time = time;
            this.place = place;
            this.price = price;
            this.people = people;
            this.type = type;
            this.description = description;
        }

        // Getters
        public String getName() { return name; }
        public String getTime() { return time; }
        public String getPlace() { return place; }
        public int getPrice() { return price; }
        public int getPeople() { return people; }
        public String getType() { return type; }
        public String getDescription() { return description; }
    }
    class ClassListAdapter extends RecyclerView.Adapter<ClassListAdapter.ViewHolder> {
        private final List<ClassListData> classList;
        private final LayoutInflater inflater;

        public ClassListAdapter(Context context, List<ClassListData> classList) {
            this.classList = classList;
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.user_search_coach_class_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ClassListData classData = classList.get(position);
            holder.name.setText(classData.getName());
            holder.time.setText(classData.getTime());
            holder.place.setText(classData.getPlace());
            holder.price.setText(getString(R.string.CoachClass_costText, classData.getPrice()));
            holder.people.setText(getString(R.string.CoachClass_numberText, classData.getPeople()));
            holder.type.setText(classData.getType());
            holder.description.setText(classData.getDescription());
            holder.itemView.setOnClickListener(v -> new AlertDialog.Builder(inflater.getContext())
                    .setTitle("Class Details")
                    .setMessage("Name: " + classData.getName() + "\n" +
                            "Time: " + classData.getTime() + "\n" +
                            "Place: " + classData.getPlace() + "\n" +
                            "Price: " + classData.getPrice() + "\n" +
                            "People: " + classData.getPeople() + "\n" +
                            "Type: " + classData.getType() + "\n" +
                            "Description: " + classData.getDescription())
                    .setPositiveButton("我要預約" , (dialog, which) -> {
                        try {
                            ResultSet updateResult = MyConnection.prepareStatement(
                                    "INSERT INTO [使用者預約] (使用者編號,健身教練編號,課程編號,預約狀態) " +
                                            "VALUES ((SELECT 課程編號 FROM [健身教練課表] WHERE 課程編號 = ?), ?)"
                            ).executeQuery();
                            updateResult.updateInt(1, position);
                            updateResult.updateInt(2, 1);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show());
        }

        @Override
        public int getItemCount() {
            return classList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, time, place, price, people, type, description;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.CoachClass_nameText);
                time = itemView.findViewById(R.id.CoachClass_timeText);
                place = itemView.findViewById(R.id.CoachClass_locationText);
                price = itemView.findViewById(R.id.CoachClass_costText);
                people = itemView.findViewById(R.id.CoachClass_peopleText);
                type = itemView.findViewById(R.id.CoachClass_typeText);
                description = itemView.findViewById(R.id.CoachClass_classDescription);
            }
        }
    }
}