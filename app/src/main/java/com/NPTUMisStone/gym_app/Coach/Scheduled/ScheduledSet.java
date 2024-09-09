package com.NPTUMisStone.gym_app.Coach.Scheduled;

import static com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle.convertImageToBytes;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ErrorHints;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.hdev.calendar.bean.DateInfo;
import com.hdev.calendar.view.MultiCalendarView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class ScheduledSet extends AppCompatActivity {

    List<DateInfo> dateInfoList;
    boolean isDialogShow = false;
    Connection MyConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_scheduled_set);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        findViewById(R.id.ScheduledSet_backButton).setOnClickListener(v -> finish());
        findViewById(R.id.ScheduledSet_addButton).setOnClickListener(v -> addClass(null));
        setRecyclerView();
    }

    private void showDatePicker() {
        if (isDialogShow) return;    //避免重複開啟
        isDialogShow = true;
        View dialogView = getLayoutInflater().inflate(R.layout.coach_scheduled_set_calendar, null); //自定義日歷元件：https://blog.csdn.net/coffee_shop/article/details/130709029
        MultiCalendarView mCalendarView = dialogView.findViewById(R.id.ScheduledSet_calendarView);
        mCalendarView.setDateRange(System.currentTimeMillis(), System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000, System.currentTimeMillis());
        if (dateInfoList != null) mCalendarView.setSelectedDateList(dateInfoList);
        mCalendarView.setOnMultiDateSelectedListener((view, selectedDays, selectedDates) -> {
            ((AlertDialog) view.getTag()).setMessage(selectedDates.isEmpty() ? "請選擇日期" : "已選擇" + selectedDates.size() + "天");
            return null;
        });
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(dateInfoList == null || dateInfoList.isEmpty() ? "請選擇日期" : "已選擇" + dateInfoList.size() + "天")
                .setTitle("選擇日期").setView(dialogView).setPositiveButton("確定", (dialog1, which) -> {
                    dateInfoList = mCalendarView.getSelectedDateList();
                    List<String> dateList = new ArrayList<>();
                    for (DateInfo dateInfo : dateInfoList)
                        dateList.add(dateInfo.getYear() + "/" + dateInfo.getMonth() + "/" + dateInfo.getDay());
                    ((TextView) findViewById(R.id.ScheduledSet_dateEdit)).setText(dateList.isEmpty() ? "" : dateList.toString());
                }).create();
        mCalendarView.setTag(dialog);
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show();
    }

    private void showTimePicker(int classPeriod, boolean isStart) {
        if (isDialogShow) return;
        isDialogShow = true;
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            if (isStart && hourOfDay < 7) {
                Toast.makeText(this, "不得在七點前開始課程", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isStart && hourOfDay > 23) {
                Toast.makeText(this, "不得在十一點後結束課程", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isStart && hourOfDay + classPeriod / 60 > 23) {
                Toast.makeText(this, "不得在十一點後結束課程", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isStart && hourOfDay - classPeriod / 60 < 7) {
                Toast.makeText(this, "不得在七點前開始課程", Toast.LENGTH_SHORT).show();
                return;
            }
            //((EditText) findViewById(isStart ? R.id.ScheduledSet_startEdit : R.id.ScheduledSet_endEdit)).setText(String.format("%02d:%02d", hourOfDay, minute));
            ((EditText) findViewById(isStart ? R.id.ScheduledSet_endEdit : R.id.ScheduledSet_startEdit)).setText(getString(R.string.ScheduledSet_timeText, hourOfDay, minute));
            //((EditText) findViewById(isStart ? R.id.ScheduledSet_endEdit : R.id.ScheduledSet_startEdit)).setText(String.format("%02d:%02d", (hourOfDay + (isStart ? classPeriod : -classPeriod) / 60) % 24, (minute + (isStart ? classPeriod : -classPeriod) % 60) % 60));
            ((EditText) findViewById(isStart ? R.id.ScheduledSet_endEdit : R.id.ScheduledSet_startEdit)).setText(getString(R.string.ScheduledSet_timeText, (hourOfDay + (isStart ? classPeriod : -classPeriod) / 60) % 24, (minute + (isStart ? classPeriod : -classPeriod) % 60) % 60));
        }, 0, 0, false);
        timePickerDialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        timePickerDialog.show();
    }

    private List<DateInfo> stringToDateInfoList(String str) {
        List<DateInfo> dateInfoList = new ArrayList<>();
        if (str == null || str.isEmpty() || str.equals("[]")) return dateInfoList;
        str = str.substring(1, str.length() - 1);       // 移除兩端的方括號
        String[] dateList = str.split(", ");      // 分割並轉換為 List<String>
        for (String dateStr : dateList) {               // 將 List<String> 轉換為 List<DateInfo>
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                dateInfoList.add(new DateInfo(year, month, day));
            }
        }
        return dateInfoList;
    }
    private boolean compareTime(int originalHour, int originalMinute, String compareTime, boolean isStart) {  //originalHour 和 originalMinute 是原始時間
        if (compareTime.isEmpty()) return true;
        String[] parts = compareTime.split(":");    // 將時間字串轉換為小時和分鐘
        int compareHour = Integer.parseInt(parts[0]);
        int compareMinute = Integer.parseInt(parts[1]);
        if (compareHour == originalHour && compareMinute == originalMinute)
            Toast.makeText(this, "時間重複", Toast.LENGTH_SHORT).show();
        else if (isStart && (compareHour < originalHour || (compareHour == originalHour && compareMinute <= originalMinute)))
            Toast.makeText(this, "結束時間需大於開始時間", Toast.LENGTH_SHORT).show();
        else if (!isStart && (compareHour > originalHour || (compareHour == originalHour && compareMinute >= originalMinute)))
            Toast.makeText(this, "結束時間需大於開始時間", Toast.LENGTH_SHORT).show();
        else
            return true;
        return false;
    }
    private void saveScheduled(int classId) {
        EditText[] editTexts = {findViewById(R.id.ScheduledSet_endEdit), findViewById(R.id.ScheduledSet_startEdit), findViewById(R.id.ScheduledSet_dateEdit)};
        String[] errorMessages = {"請選擇結束時間", "請選擇開始時間", "請選擇日期"};
        String[] suffixes = {"", "", ""};
        if (!checkFields(editTexts, errorMessages, suffixes)) {
            Toast.makeText(this, "請填寫完整", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            List<String> dateList = new ArrayList<>();
            List<String> weekList = new ArrayList<>();
            for (DateInfo dateInfo : dateInfoList) {
                if (isTimeConflict(MyConnection, dateInfo, editTexts[1].getText().toString(), editTexts[0].getText().toString())) {
                    Toast.makeText(this, "時間重複", Toast.LENGTH_SHORT).show();
                    return;
                }
                dateList.add(dateInfo.getYear() + "-" + dateInfo.getMonth() + "-" + dateInfo.getDay());
                weekList.add(getDayOfWeek(dateInfo));
            }
            insertScheduled(MyConnection, dateList, weekList, editTexts[1].getText().toString(), editTexts[0].getText().toString(), classId);
            Toast.makeText(this, "儲存成功", Toast.LENGTH_SHORT).show();
            finish();
        } catch (SQLException e) {
            Log.e("SQL", "Error in check SQL", e);
            Toast.makeText(this, "資料庫連線錯誤", Toast.LENGTH_SHORT).show();
        }
    }
    private String getDayOfWeek(DateInfo dateInfo) { //get the weekday of the date:https://stackoverflow.com/questions/18600257/how-to-get-the-weekday-of-a-date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateInfo.toCalendar().getTime());
        return switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY -> "星期日";
            case Calendar.MONDAY -> "星期一";
            case Calendar.TUESDAY -> "星期二";
            case Calendar.WEDNESDAY -> "星期三";
            case Calendar.THURSDAY -> "星期四";
            case Calendar.FRIDAY -> "星期五";
            case Calendar.SATURDAY -> "星期六";
            default -> "";
        };
    }
    //***！！！注意邏輯正確性未知！！！***
    private boolean isTimeConflict(Connection connection, DateInfo dateInfo, String start, String end) throws SQLException {
        String searchQuery = "SELECT * FROM [健身教練班表] WHERE [健身教練編號] = ? AND [日期] = ? AND (" +
                "([開始時間] < ? AND [結束時間] > ?) OR " + // New start time is within an existing range
                "([開始時間] >= ? AND [結束時間] <= ?))";   // New time range completely overlaps an existing range
        try (PreparedStatement searchStatement = connection.prepareStatement(searchQuery)) {
            searchStatement.setInt(1, Coach.getInstance().getCoachId());
            searchStatement.setString(2, dateInfo.getYear() + "/" + dateInfo.getMonth() + "/" + dateInfo.getDay());
            searchStatement.setString(3, end);
            searchStatement.setString(4, start);
            searchStatement.setString(5, start);
            searchStatement.setString(6, end);
            return searchStatement.executeQuery().next();
        }
    }

    private void insertScheduled(Connection connection, List<String> dateList, List<String> weekList, String start, String end, int classId) throws SQLException {
        String insertQuery = "INSERT INTO [健身教練課表] ([課程編號],[日期],[開始時間],[結束時間],[星期幾]) VALUES (?,?,?,?,?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setInt(1, classId);
            insertStatement.setString(3, start);
            insertStatement.setString(4, end);
            for (int i = 0; i < dateList.size(); i++) {
                insertStatement.setString(2, dateList.get(i));
                insertStatement.setString(5, weekList.get(i));
                insertStatement.executeUpdate();
            }
        }
    }
    ImageView showImage;
    Uri uri;
    private void addClass(Integer classId) {
        if (isDialogShow) return;    //避免重複開啟
        isDialogShow = true;
        AlertDialog dialog = createDialog(classId, initializeDialogViews());
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show(); //阻止dialog關閉避免錯誤內容被傳入
    }
    private View initializeDialogViews() {
        View dialogView = getLayoutInflater().inflate(R.layout.coach_scheduled_class_add, findViewById(R.id.main), false);
        showImage = dialogView.findViewById(R.id.ScheduledAdd_uploadImage);
        dialogView.findViewById(R.id.ScheduledAdd_uploadButton).setOnClickListener(v -> changeImage());
        setFocusChangeListeners(dialogView);
        return dialogView;
    }
    private void setFocusChangeListeners(View dialogView) {
        setFocusChangeListener(dialogView.findViewById(R.id.ScheduledAdd_timeEdit), "分鐘");
        setFocusChangeListener(dialogView.findViewById(R.id.ScheduledAdd_limitEdit), "人");
        setFocusChangeListener(dialogView.findViewById(R.id.ScheduledAdd_feeEdit), "$");
    }
    private void setFocusChangeListener(EditText editText, String suffix) {
        editText.setOnFocusChangeListener((v, hasFocus) -> editText.setText(hasFocus ? editText.getText().toString().replace(suffix, "") : editText.getText().toString() + suffix));
    }
    private AlertDialog createDialog(Integer classId, View dialogView) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView).setTitle(classId == null ? "新增課程" : "課程修改")
                .setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss())
                .setPositiveButton("確定", null).create();
        //當classId為null時，表示新增課程，否則表示修改課程
        if (classId == null) {
            dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                if (saveClass(dialogView)) {
                    dialog.dismiss();
                    setRecyclerView();
                } else
                    Toast.makeText(this, "輸入錯誤1", Toast.LENGTH_SHORT).show();
            }));
        } else {
            byte[] finalImage = populateClassDetails(classId, dialogView);
            dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                if (updateClass(classId, finalImage, dialogView)) {
                    dialog.dismiss();
                    setRecyclerView();
                } else
                    Toast.makeText(this, "輸入錯誤2", Toast.LENGTH_SHORT).show();
            }));
        }
        return dialog;
    }
    //將課程資料取出並填入dialogView
    private byte[] populateClassDetails(Integer classId,View dialogView) {
        try {
            String searchQuery = "SELECT * FROM [健身教練課程] WHERE [課程編號] = ?";
            PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery);
            searchStatement.setInt(1, classId);
            ResultSet searchResult = searchStatement.executeQuery();
            if (searchResult.next()) {
                setDialogViewData(dialogView, searchResult);
                byte[] image = searchResult.getBytes("課程圖片");
                showImage.setImageBitmap(ImageHandle.getBitmap(image));
                return image;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(this, "填充課程資訊失敗", Toast.LENGTH_SHORT).show();
        return null;
    }
    private void setDialogViewData(View dialogView, ResultSet searchResult) throws SQLException {
        ((EditText) dialogView.findViewById(R.id.ScheduledAdd_nameEdit)).setText(searchResult.getString("課程名稱"));
        ((EditText) dialogView.findViewById(R.id.ScheduledAdd_typeEdit)).setText(searchResult.getString("課程類型"));
        ((EditText) dialogView.findViewById(R.id.ScheduledAdd_descriptionEdit)).setText(searchResult.getString("課程內容介紹"));
        ((EditText) dialogView.findViewById(R.id.ScheduledAdd_timeEdit)).setText(getString(R.string.ScheduledSet_timeHint, searchResult.getInt("課程時間長度")));
        ((EditText) dialogView.findViewById(R.id.ScheduledAdd_limitEdit)).setText(getString(R.string.ScheduledSet_limitHint, searchResult.getInt("上課人數")));
        ((EditText) dialogView.findViewById(R.id.ScheduledAdd_locationEdit)).setText(searchResult.getString("上課地點"));
        ((EditText) dialogView.findViewById(R.id.ScheduledAdd_feeEdit)).setText(getString(R.string.ScheduledSet_feeHint, searchResult.getInt("課程費用")));
    }
    private void changeImage() {
        Intent intent = new Intent();   //上傳圖片：https://www.youtube.com/watch?v=9oNujFx_ZaI&ab_channel=ShihFinChen
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        uploadImage_ActivityResult.launch(intent);
    }
    ActivityResultLauncher<Intent> uploadImage_ActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) return;
                    showImage.setImageURI(data.getData());
                } else if (result.getResultCode() == Activity.RESULT_CANCELED)
                    Toast.makeText(this, "取消圖片上傳", Toast.LENGTH_SHORT).show();
            }
    );
    private boolean saveClass(View dialogView) {
        EditText[] editTexts = {dialogView.findViewById(R.id.ScheduledAdd_feeEdit), dialogView.findViewById(R.id.ScheduledAdd_locationEdit), dialogView.findViewById(R.id.ScheduledAdd_limitEdit), dialogView.findViewById(R.id.ScheduledAdd_timeEdit), dialogView.findViewById(R.id.ScheduledAdd_descriptionEdit), dialogView.findViewById(R.id.ScheduledAdd_typeEdit), dialogView.findViewById(R.id.ScheduledAdd_nameEdit)};
        String[] errorMessages = {"請輸入費用", "請輸入地點", "請輸入人數上限", "請輸入課程時間", "請輸入課程描述", "請輸入課程類型", "請輸入課程名稱"};
        String[] suffixes = {"$", "", "人", "分鐘", "", "", ""};
        if(checkFields(editTexts, errorMessages, suffixes)){
            try {
                InsertStatement(dialogView);
                Toast.makeText(this, "新增成功", Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception e) {
                Log.e("SQL", "Error in insert SQL", e);
                Toast.makeText(this, "資料庫新增失敗", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
    private void InsertStatement(View dialogView) throws SQLException, IOException {
        String insertQuery = "INSERT INTO [健身教練課程] ([健身教練編號],[課程名稱],[課程類型],[課程內容介紹],[課程時間長度],[上課人數],[上課地點],[課程費用],[課程圖片]) VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement insertStatement = MyConnection.prepareStatement(insertQuery);
        insertStatement.setInt(1, Coach.getInstance().getCoachId());
        insertStatement.setString(2, ((EditText) dialogView.findViewById(R.id.ScheduledAdd_nameEdit)).getText().toString());
        insertStatement.setString(3, ((EditText) dialogView.findViewById(R.id.ScheduledAdd_typeEdit)).getText().toString());
        insertStatement.setString(4, ((EditText) dialogView.findViewById(R.id.ScheduledAdd_descriptionEdit)).getText().toString());
        insertStatement.setInt(5, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ScheduledAdd_timeEdit)).getText().toString().replace("分鐘", "")));
        insertStatement.setInt(6, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ScheduledAdd_limitEdit)).getText().toString().replace("人", "")));
        insertStatement.setString(7, ((EditText) dialogView.findViewById(R.id.ScheduledAdd_locationEdit)).getText().toString());
        insertStatement.setInt(8, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ScheduledAdd_feeEdit)).getText().toString().replace("$", "")));
        insertStatement.setBytes(9, convertImageToBytes(this, uri == null ? Uri.parse("android.resource://com.example.gym_app/" + R.drawable.coach_scheduled_add_null_image) : uri));
        //URI of resource:https://stackoverflow.com/questions/4896223/how-to-get-an-uri-of-an-image-resource-in-android
        insertStatement.executeUpdate();
    }
    private boolean updateClass(Integer classId,byte[] image,View dialogView){
        EditText[] editTexts = {dialogView.findViewById(R.id.ScheduledAdd_feeEdit), dialogView.findViewById(R.id.ScheduledAdd_locationEdit), dialogView.findViewById(R.id.ScheduledAdd_limitEdit), dialogView.findViewById(R.id.ScheduledAdd_timeEdit), dialogView.findViewById(R.id.ScheduledAdd_descriptionEdit), dialogView.findViewById(R.id.ScheduledAdd_typeEdit), dialogView.findViewById(R.id.ScheduledAdd_nameEdit)};
        String[] errorMessages = {"請輸入費用", "請輸入地點", "請輸入人數上限", "請輸入課程時間", "請輸入課程描述", "請輸入課程類型", "請輸入課程名稱"};
        String[] suffixes = {"$", "", "人", "分鐘", "", "", ""};
        if(checkFields(editTexts, errorMessages, suffixes)){
            try {
                String updateQuery = "UPDATE [健身教練課程] SET [課程名稱] = ?, [課程類型] = ?, [課程內容介紹] = ?, [課程時間長度] = ?, [上課人數] = ?, [上課地點] = ?, [課程費用] = ?, [課程圖片] = ? WHERE [健身教練編號] = ? AND [課程編號] = ?";
                PreparedStatement updateStatement = MyConnection.prepareStatement(updateQuery);
                setUpdateStatementParameters(updateStatement, classId, image, dialogView);
                updateStatement.executeUpdate();
                return true;
            } catch (Exception e) {
                Log.e("SQL", "Error in update SQL", e);
            }
        }
        return false;
    }
    private void setUpdateStatementParameters(PreparedStatement statement, Integer classId, byte[] image, View dialogView) throws SQLException, IOException {
        statement.setString(1, ((EditText) dialogView.findViewById(R.id.ScheduledAdd_nameEdit)).getText().toString());
        statement.setString(2, ((EditText) dialogView.findViewById(R.id.ScheduledAdd_typeEdit)).getText().toString());
        statement.setString(3, ((EditText) dialogView.findViewById(R.id.ScheduledAdd_descriptionEdit)).getText().toString());
        statement.setInt(4, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ScheduledAdd_timeEdit)).getText().toString().replace("分鐘", "")));
        statement.setInt(5, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ScheduledAdd_limitEdit)).getText().toString().replace("人", "")));
        statement.setString(6, ((EditText) dialogView.findViewById(R.id.ScheduledAdd_locationEdit)).getText().toString());
        statement.setInt(7, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ScheduledAdd_feeEdit)).getText().toString().replace("$", "")));
        statement.setBytes(8, uri == null ? image : convertImageToBytes(this, uri));
        statement.setInt(9, Coach.getInstance().getCoachId());
        statement.setInt(10, classId);
    }
    private boolean checkFields(EditText[] editTexts, String[] errorMessages, String[] suffixes) {
        boolean check = true;   //當check & check & ... & check = false時，check = false
        for (int i = 0; i < editTexts.length; i++) check &= validateField(editTexts[i], errorMessages[i], suffixes[i]);
        Log.d("checkFields", "checkFields: " + check);
        return check;
    }

    private boolean validateField(EditText editText, String errorMessage, String suffix) {
        if (editText.getText().toString().replace(suffix, "").isEmpty()) {
            ErrorHints.editHint(editText, errorMessage);
            return false;
        }
        return true;
    }
    static class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<Integer> idList;
        List<byte[]> imageList;
        List<String> nameList;
        static int VIEW_TYPE_SHIMMER = 0, VIEW_TYPE_ITEM = 1;
        int selectedPosition = RecyclerView.NO_POSITION;
        boolean isLoading = false;
        CustomAdapter(List<Integer> ids,List<byte[]> images,List<String> names) {
            idList = ids;
            imageList = images;
            nameList = names;
        }
        @Override
        public int getItemViewType(int position) {
            return isLoading ? VIEW_TYPE_SHIMMER : VIEW_TYPE_ITEM;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            if (viewType == VIEW_TYPE_SHIMMER) {
                View shimmerView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_shimmer_layout, viewGroup, false);
                setShimmerLayoutHeight(shimmerView, R.layout.coach_scheduled_set_item, viewGroup);
                return new ShimmerViewHolder(shimmerView);
            } else {
                View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.coach_scheduled_set_item, viewGroup, false);
                return new ItemViewHolder(itemView);
            }
        }
        private void setShimmerLayoutHeight(View shimmerView, int itemLayoutResId, ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId, parent, false);
            itemView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int itemHeight = itemView.getMeasuredHeight();
            int itemWidth = itemView.getMeasuredWidth();
            ViewGroup.LayoutParams layoutParams = shimmerView.getLayoutParams();
            layoutParams.height = itemHeight;
            layoutParams.width = itemWidth;
            shimmerView.setLayoutParams(layoutParams);
        }
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_ITEM) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
                itemViewHolder.imageView.setImageBitmap(ImageHandle.getBitmap(imageList.get(position)));
                itemViewHolder.textView.setText(nameList.get(position));
                itemViewHolder.selectButton.setOnClickListener(v -> {
                    int previousSelectedPosition = selectedPosition;
                    selectedPosition = viewHolder.getBindingAdapterPosition();
                    notifyItemChanged(previousSelectedPosition); // Notify previous selected item
                    notifyItemChanged(selectedPosition); // Notify new selected item
                    ((ScheduledSet) itemViewHolder.selectButton.getContext()).selectClass(idList.get(selectedPosition));
                });
                if (selectedPosition == position) itemViewHolder.selectButton.setBackgroundColor(ContextCompat.getColor(itemViewHolder.selectButton.getContext(),R.color.light_gray)); // 選中效果
                else itemViewHolder.selectButton.setBackgroundColor(ContextCompat.getColor(itemViewHolder.selectButton.getContext(),R.color.transparent)); // 未選中效果
            }
        }
        @Override
        public int getItemCount() { // Show 5 shimmer items while loading
            return isLoading ? 5 : nameList.size();
        }
        static class ShimmerViewHolder extends RecyclerView.ViewHolder {
            ShimmerViewHolder(View view) {
                super(view);
            }
        }
        static class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;
            ConstraintLayout selectButton;

            ItemViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.SetItem_imageView);
                textView = view.findViewById(R.id.SetItem_nameText);
                selectButton = view.findViewById(R.id.SetItem_allLayout);
            }

        }
        public void setLoading(boolean loading) {
            isLoading = loading;
            notifyItemRangeChanged(0, getItemCount());
        }
    }
    private void selectClass(Integer classId){
        findViewById(R.id.ScheduledSet_constraintLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.ScheduledSet_setButton).setOnClickListener(v -> saveScheduled(classId));
        findViewById(R.id.ScheduledSet_dateEdit).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.ScheduledSet_editButton).setOnClickListener(v -> addClass(classId));
        int classPeriod = getClassPeriod(classId);
        findViewById(R.id.ScheduledSet_startEdit).setOnClickListener(v -> showTimePicker(classPeriod, true));
        findViewById(R.id.ScheduledSet_endEdit).setOnClickListener(v -> showTimePicker(classPeriod, false));
    }
    //取得課程時間長度
    private int getClassPeriod(Integer classId){
        Callable<Integer> task = () -> {
            int classPeriod = 0;
            try {
                String searchQuery = "SELECT [課程時間長度] FROM [健身教練課程] WHERE [課程編號] = ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery);
                searchStatement.setInt(1, classId);
                ResultSet searchResult = searchStatement.executeQuery();
                if (searchResult.next()) classPeriod = searchResult.getInt("課程時間長度");
            } catch (SQLException e) {
                Log.e("SQL", "Error in search SQL", e);
            }
            if (classPeriod == 0) new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, "取得課程時間長度失敗", Toast.LENGTH_SHORT).show());
            return classPeriod;
        };
        try {
            return Executors.newSingleThreadExecutor().submit(task).get();
        } catch (Exception e) {
            Log.e("FutureTask", "Error getting class period", e);
            return 0;
        }
    }
    private void setRecyclerView() {
        CustomAdapter adapter = new CustomAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        RecyclerView recyclerView = findViewById(R.id.ScheduledSet_classRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        adapter.setLoading(true);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Integer> idList = new ArrayList<>();
                List<byte[]> imageList = new ArrayList<>();
                List<String> nameList = new ArrayList<>();
                fetchClassData(idList, imageList, nameList);
                new Handler(Looper.getMainLooper()).post(() -> {
                    adapter.idList = idList;
                    adapter.imageList = imageList;
                    adapter.nameList = nameList;
                    adapter.setLoading(false);
                    Log.d("RecyclerView", "RecyclerView set successfully");
                });
            } catch (Exception e) {
                Log.e("SQL", "Error in search SQL", e);
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, "更新課程列表失敗", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void fetchClassData(List<Integer> idList, List<byte[]> imageList, List<String> nameList) {
        String searchQuery = "SELECT * FROM [健身教練課程] WHERE [健身教練編號] = ?";
        int resultCount = 3; //避免過快失敗
        while (resultCount-- > 0){
            try {
                PreparedStatement statement = MyConnection.prepareStatement(searchQuery);
                statement.setInt(1, Coach.getInstance().getCoachId());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    idList.add(resultSet.getInt("課程編號"));
                    imageList.add(resultSet.getBytes("課程圖片"));
                    nameList.add(resultSet.getString("課程名稱"));
                }
                break;
            } catch (SQLException e) {
                Log.e("SQL", "Error in search SQL"+resultCount, e);
                if (resultCount == 0) throw new RuntimeException(e);
            }
        }
    }
}