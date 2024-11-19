package com.NPTUMisStone.gym_app.Coach.Scheduled;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.Main.Initial.SnackBarUtils;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.hdev.calendar.bean.DateInfo;
import com.hdev.calendar.view.SingleCalendarView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduledCheck extends AppCompatActivity {
    Connection MyConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_scheduled_check);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        findViewById(R.id.ScheduledCheck_backButton).setOnClickListener(v -> finish());
        setupCalendarView();
    }

    static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        List<Integer> idList;
        List<String> nameList;
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView imageView;
            ViewHolder(View view) {
                super(view);
                textView = view.findViewById(R.id.CheckItem_checkedText);
                imageView = view.findViewById(R.id.CheckItem_chooseImage);
            }
            TextView getTextView() {
                return textView;
            }
        }
        CustomAdapter(ArrayList<Integer> ids,ArrayList<String> names) {
            idList = ids;
            nameList = names;
        }
        @NonNull @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.coach_scheduled_check_item, viewGroup, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.getTextView().setText(nameList.get(position));
            viewHolder.imageView.setTag(idList.get(position)); // Ensure tag is set
            viewHolder.imageView.setOnClickListener(v -> {
                ((ScheduledCheck) v.getContext()).addClass((Integer) v.getTag());
            });
        }
        @Override
        public int getItemCount() { return nameList.size(); }
    }
    boolean isDialogShow = false;
    private void addClass(Integer classId) {
        if (isDialogShow) return;    //避免重複開啟
        isDialogShow = true;
        AlertDialog dialog = createDialog(classId, initializeDialogViews());
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show(); //阻止dialog關閉避免錯誤內容被傳入
    }
    private AlertDialog createDialog(Integer classId, View dialogView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("確定", (dialog, which) -> {
            // Do something when the user clicks the positive button
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            // Do something when the user clicks the negative button
        });
        byte[] finalImage = populateClassDetails(classId, dialogView);
        return builder.create();
    }
    private View initializeDialogViews() {
        View dialogView = getLayoutInflater().inflate(R.layout.coach_scheduled_class_check, findViewById(R.id.main), false);
        showImage = dialogView.findViewById(R.id.ClassCheck_uploadImage);
        return dialogView;
    }

    ImageView showImage;
    private byte[] populateClassDetails(Integer classId,View dialogView) {
        try {
            String searchQuery = "SELECT * FROM [健身教練課程] WHERE [課程編號] = ?";
            PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery);
            searchStatement.setInt(1, classId);
            ResultSet searchResult = searchStatement.executeQuery();
            if (searchResult.next()) {
                Log.d("ScheduledCheck", "Class details found for classId: " + classId);
                setDialogViewData(dialogView, searchResult);
                byte[] image = searchResult.getBytes("課程圖片");
                showImage.setImageBitmap(ImageHandle.getBitmap(image));
                return image;
            }else {
                Log.e("ScheduledCheck", "No class details found for classId: " + classId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(this, "填充課程資訊失敗", Toast.LENGTH_SHORT).show();
        return null;
    }
    private void setDialogViewData(View dialogView, ResultSet searchResult) throws SQLException {
        ((EditText) dialogView.findViewById(R.id.ClassCheck_nameEdit)).setText(searchResult.getString("課程名稱"));
        ((EditText) dialogView.findViewById(R.id.ClassCheck_typeEdit)).setText(searchResult.getString("課程類型"));
        ((EditText) dialogView.findViewById(R.id.ClassCheck_descriptionEdit)).setText(searchResult.getString("課程內容介紹"));
        ((EditText) dialogView.findViewById(R.id.ClassCheck_timeEdit)).setText(getString(R.string.ScheduledSet_timeHint, searchResult.getInt("課程時間長度")));
        ((EditText) dialogView.findViewById(R.id.ClassCheck_limitEdit)).setText(getString(R.string.ScheduledSet_limitHint, searchResult.getInt("上課人數")));
        ((EditText) dialogView.findViewById(R.id.ClassCheck_locationEdit)).setText(searchResult.getString("上課地點"));
        ((EditText) dialogView.findViewById(R.id.ClassCheck_feeEdit)).setText(getString(R.string.ScheduledSet_feeHint, searchResult.getInt("課程費用")));
    }
    private void setupCalendarView() {
        loadSchedule(new DateInfo(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
        SingleCalendarView singleCalendarView = findViewById(R.id.ScheduledCheck_calendarView);
        List<DateInfo> dateList = fetchDatesFromDB();
        if (!dateList.isEmpty()) {
            singleCalendarView.setClickableDateList(dateList);
            singleCalendarView.setDateRange(dateList.get(0).timeInMillis(), dateList.get(dateList.size() - 1).timeInMillis());
        }else{
            singleCalendarView.setClickableDateList(new ArrayList<>());
            singleCalendarView.setDateRange(Calendar.getInstance().getTimeInMillis(), Calendar.getInstance().getTimeInMillis());
            SnackBarUtils.makeShort(findViewById(R.id.main), "無課程").danger();
        }
        singleCalendarView.setOnSingleDateSelectedListener((calendar, date) -> {
            loadSchedule(date);
            return null;
        });
    }
    private List<DateInfo> fetchDatesFromDB() {
        List<DateInfo> dateList = new ArrayList<>();
        String searchQuery = "SELECT [健身教練課表].日期 FROM [健身教練課表] JOIN [健身教練課程] ON [健身教練課程].課程編號 = [健身教練課表].課程編號 WHERE [健身教練課程].健身教練編號 = ?";
        try (PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery)) {
            searchStatement.setInt(1, Coach.getInstance().getCoachId());
            ResultSet resultSet = searchStatement.executeQuery();
            while (resultSet.next()) {
                String[] date = resultSet.getString("日期").split("-");
                dateList.add(new DateInfo(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2])));
            }
        } catch (SQLException e) {
            Log.e("ScheduledCheck", "Error loading tasks from DB", e);
        }
        return dateList;
    }
    private void loadSchedule(DateInfo date) {
        RecyclerView recyclerView = findViewById(R.id.ScheduledCheck_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        CustomAdapter adapter = new CustomAdapter(new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        fetchScheduleFromDB(date, adapter);
    }
    private void fetchScheduleFromDB(DateInfo date, CustomAdapter adapter) {
        String searchQuery = "SELECT [健身教練課程].課程編號,[健身教練課程].課程名稱 FROM [健身教練課表] JOIN [健身教練課程] ON [健身教練課程].課程編號 = [健身教練課表].課程編號 WHERE [健身教練課表].日期 = ? AND [健身教練課程].健身教練編號 = ?";
        try (PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery)) {
            searchStatement.setString(1, date.getYear() + "/" + date.getMonth() + "/" + date.getDay());
            searchStatement.setInt(2, Coach.getInstance().getCoachId());
            ResultSet resultSet = searchStatement.executeQuery();
            while (resultSet.next()) {
                adapter.idList.add(resultSet.getInt("課程編號"));
                adapter.nameList.add(resultSet.getString("課程名稱"));
            }
        } catch (SQLException e) {
            Log.e("ScheduledCheck", "Error loading tasks from DB", e);
        }
    }
    /*private void fetchScheduleFromDB(DateInfo date, CustomAdapter adapter) {
        String url = "https://192.168.33.228:44312/api/schedule/" + Coach.getInstance().getCoachId() + "/" + date.getYear() + "-" + date.getMonth() + "-" + date.getDay();

        // Use an HTTP client library like OkHttp to make the request
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ScheduledCheck", "Error loading tasks from API", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : null;
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int courseId = jsonObject.getInt("courseId");
                            String courseName = jsonObject.getString("courseName");
                            adapter.idList.add(courseId);
                            adapter.nameList.add(courseName);
                        }
                        runOnUiThread(adapter::notifyDataSetChanged);
                    } catch (JSONException e) {
                        Log.e("ScheduledCheck", "Error parsing JSON response", e);
                    }
                } else {
                    Log.e("ScheduledCheck", "Unsuccessful response from API");
                }
            }
        });
    }*/
}