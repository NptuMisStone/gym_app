package com.NPTUMisStone.gym_app.Coach.Class;

import static com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle.convertImageToBytes;

import android.app.Activity;
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ClassMain extends AppCompatActivity {

    List<DateInfo> dateInfoList;
    boolean isDialogShow = false;
    Connection MyConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_class_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        findViewById(R.id.ClassMain_backButton).setOnClickListener(v -> finish());
        findViewById(R.id.ClassMain_addButton).setOnClickListener(v -> addClass(null));
        setRecyclerView();
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
        View dialogView = getLayoutInflater().inflate(R.layout.coach_class_add, findViewById(R.id.main), false);
        showImage = dialogView.findViewById(R.id.ClassAdd_uploadImage);
        dialogView.findViewById(R.id.ClassAdd_uploadButton).setOnClickListener(v -> changeImage());
        setFocusChangeListeners(dialogView);
        return dialogView;
    }
    private void setFocusChangeListeners(View dialogView) {
        setFocusChangeListener(dialogView.findViewById(R.id.ClassAdd_timeEdit), "分鐘");
        setFocusChangeListener(dialogView.findViewById(R.id.ClassAdd_limitEdit), "人");
        setFocusChangeListener(dialogView.findViewById(R.id.ClassAdd_feeEdit), "$");
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
        ((EditText) dialogView.findViewById(R.id.ClassAdd_nameEdit)).setText(searchResult.getString("課程名稱"));
        ((EditText) dialogView.findViewById(R.id.ClassAdd_typeEdit)).setText(searchResult.getString("課程類型"));
        ((EditText) dialogView.findViewById(R.id.ClassAdd_descriptionEdit)).setText(searchResult.getString("課程內容介紹"));
        ((EditText) dialogView.findViewById(R.id.ClassAdd_timeEdit)).setText(getString(R.string.ScheduledSet_timeHint, searchResult.getInt("課程時間長度")));
        ((EditText) dialogView.findViewById(R.id.ClassAdd_limitEdit)).setText(getString(R.string.ScheduledSet_limitHint, searchResult.getInt("上課人數")));
        ((EditText) dialogView.findViewById(R.id.ClassAdd_locationEdit)).setText(searchResult.getString("上課地點"));
        ((EditText) dialogView.findViewById(R.id.ClassAdd_feeEdit)).setText(getString(R.string.ScheduledSet_feeHint, searchResult.getInt("課程費用")));
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
        EditText[] editTexts = {dialogView.findViewById(R.id.ClassAdd_feeEdit), dialogView.findViewById(R.id.ClassAdd_locationEdit), dialogView.findViewById(R.id.ClassAdd_limitEdit), dialogView.findViewById(R.id.ClassAdd_timeEdit), dialogView.findViewById(R.id.ClassAdd_descriptionEdit), dialogView.findViewById(R.id.ClassAdd_typeEdit), dialogView.findViewById(R.id.ClassAdd_nameEdit)};
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
        insertStatement.setString(2, ((EditText) dialogView.findViewById(R.id.ClassAdd_nameEdit)).getText().toString());
        insertStatement.setString(3, ((EditText) dialogView.findViewById(R.id.ClassAdd_typeEdit)).getText().toString());
        insertStatement.setString(4, ((EditText) dialogView.findViewById(R.id.ClassAdd_descriptionEdit)).getText().toString());
        insertStatement.setInt(5, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ClassAdd_timeEdit)).getText().toString().replace("分鐘", "")));
        insertStatement.setInt(6, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ClassAdd_limitEdit)).getText().toString().replace("人", "")));
        insertStatement.setString(7, ((EditText) dialogView.findViewById(R.id.ClassAdd_locationEdit)).getText().toString());
        insertStatement.setInt(8, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ClassAdd_feeEdit)).getText().toString().replace("$", "")));
        insertStatement.setBytes(9, convertImageToBytes(this, uri == null ? Uri.parse("android.resource://com.example.gym_app/" + R.drawable.coach_scheduled_add_null_image) : uri));
        //URI of resource:https://stackoverflow.com/questions/4896223/how-to-get-an-uri-of-an-image-resource-in-android
        insertStatement.executeUpdate();
    }
    private boolean updateClass(Integer classId,byte[] image,View dialogView){
        EditText[] editTexts = {dialogView.findViewById(R.id.ClassAdd_feeEdit), dialogView.findViewById(R.id.ClassAdd_locationEdit), dialogView.findViewById(R.id.ClassAdd_limitEdit), dialogView.findViewById(R.id.ClassAdd_timeEdit), dialogView.findViewById(R.id.ClassAdd_descriptionEdit), dialogView.findViewById(R.id.ClassAdd_typeEdit), dialogView.findViewById(R.id.ClassAdd_nameEdit)};
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
        statement.setString(1, ((EditText) dialogView.findViewById(R.id.ClassAdd_nameEdit)).getText().toString());
        statement.setString(2, ((EditText) dialogView.findViewById(R.id.ClassAdd_typeEdit)).getText().toString());
        statement.setString(3, ((EditText) dialogView.findViewById(R.id.ClassAdd_descriptionEdit)).getText().toString());
        statement.setInt(4, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ClassAdd_timeEdit)).getText().toString().replace("分鐘", "")));
        statement.setInt(5, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ClassAdd_limitEdit)).getText().toString().replace("人", "")));
        statement.setString(6, ((EditText) dialogView.findViewById(R.id.ClassAdd_locationEdit)).getText().toString());
        statement.setInt(7, Integer.parseInt(((EditText) dialogView.findViewById(R.id.ClassAdd_feeEdit)).getText().toString().replace("$", "")));
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
    static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ItemViewHolder> {
        List<Integer> idList;
        List<byte[]> imageList;
        List<String> nameList;
        boolean isLoading = false;
        int selectedPosition = RecyclerView.NO_POSITION;

        CustomAdapter(List<Integer> ids, List<byte[]> images, List<String> names) {
            idList = ids;
            imageList = images;
            nameList = names;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 創建 ViewHolder，這裡使用 R.layout.coach_class_item 作為項目佈局
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.coach_class_item, parent, false);
            return new ItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            // 綁定資料邏輯
            if (isLoading) {
                // 顯示 loading.gif 或其他加載動畫
                holder.imageView.setImageResource(R.drawable.loading);  // 使用 loading.gif
                holder.textView.setVisibility(View.GONE);  // 隱藏文字
            } else {
                // 顯示實際的數據
                holder.imageView.setVisibility(View.VISIBLE);
                holder.textView.setVisibility(View.VISIBLE);
                holder.imageView.setImageBitmap(ImageHandle.getBitmap(imageList.get(position)));
                holder.textView.setText(nameList.get(position));

                holder.selectButton.setOnClickListener(v -> {
                    int previousSelectedPosition = selectedPosition;
                    selectedPosition = holder.getBindingAdapterPosition();
                    notifyItemChanged(previousSelectedPosition);  // 通知更新先前選擇的項目
                    notifyItemChanged(selectedPosition);  // 通知更新新的選擇項目
                });

                if (selectedPosition == position) {
                    holder.selectButton.setBackgroundColor(
                            ContextCompat.getColor(holder.selectButton.getContext(), R.color.light_gray));  // 選中效果
                } else {
                    holder.selectButton.setBackgroundColor(
                            ContextCompat.getColor(holder.selectButton.getContext(), R.color.transparent));  // 未選中效果
                }
            }
        }

        @Override
        public int getItemCount() {
            return isLoading ? 5 : nameList.size();  // 根據 isLoading 返回項目數量
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;
            ConstraintLayout selectButton;

            ItemViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.Class_Item_imageView);
                textView = view.findViewById(R.id.Class_Item_nameText);
                selectButton = view.findViewById(R.id.SetItem_allLayout);
            }
        }
    }

    private void setRecyclerView() {
        CustomAdapter adapter = new CustomAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        RecyclerView recyclerView = findViewById(R.id.ClassMain_classRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Integer> idList = new ArrayList<>();
                List<byte[]> imageList = new ArrayList<>();
                List<String> nameList = new ArrayList<>();
                fetchClassData(idList, imageList, nameList);

                // 更新 UI 和資料
                new Handler(Looper.getMainLooper()).post(() -> {
                    adapter.idList = idList;
                    adapter.imageList = imageList;
                    adapter.nameList = nameList;

                    // 通知 RecyclerView 資料已變更
                    adapter.notifyDataSetChanged();  // 這裡非常重要
                    Log.d("RecyclerView", "RecyclerView set successfully");
                });
            } catch (Exception e) {
                Log.e("SQL", "Error in search SQL", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "更新課程列表失敗", Toast.LENGTH_SHORT).show()
                );
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

                // 檢查是否有資料返回
                if (!resultSet.isBeforeFirst()) {
                    Log.e("SQL", "No data found");
                }

                while (resultSet.next()) {
                    idList.add(resultSet.getInt("課程編號"));
                    imageList.add(resultSet.getBytes("課程圖片"));
                    nameList.add(resultSet.getString("課程名稱"));

                    // 日誌輸出以確認資料
                    Log.d("SQL", "課程名稱: " + resultSet.getString("課程名稱"));
                }
                break;
            } catch (SQLException e) {
                Log.e("SQL", "Error in search SQL"+resultCount, e);
                if (resultCount == 0) throw new RuntimeException(e);
            }
        }
    }

}