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
        findViewById(R.id.ClassMain_addButton).setOnClickListener(v -> {
            Intent intent = new Intent(ClassMain.this, ClassAdd.class);
            startActivity(intent);
        });
        setRecyclerView();
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
        while (resultCount-- > 0) {
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
                Log.e("SQL", "Error in search SQL" + resultCount, e);
                if (resultCount == 0) throw new RuntimeException(e);
            }
        }
    }

    // 保留 CustomAdapter 以顯示資料
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
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.coach_class_item, parent, false);
            return new ItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            if (isLoading) {
                holder.imageView.setImageResource(R.drawable.loading);
                holder.textView.setVisibility(View.GONE);
            } else {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.textView.setVisibility(View.VISIBLE);
                holder.imageView.setImageBitmap(ImageHandle.getBitmap(imageList.get(position)));
                holder.textView.setText(nameList.get(position));

                holder.selectButton.setOnClickListener(v -> {
                    int previousSelectedPosition = selectedPosition;
                    selectedPosition = holder.getBindingAdapterPosition();
                    notifyItemChanged(previousSelectedPosition);
                    notifyItemChanged(selectedPosition);
                });

                holder.selectButton.setBackgroundColor(selectedPosition == position ?
                        ContextCompat.getColor(holder.selectButton.getContext(), R.color.light_gray) :
                        ContextCompat.getColor(holder.selectButton.getContext(), R.color.transparent));
            }
        }

        @Override
        public int getItemCount() {
            return isLoading ? 5 : nameList.size();
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

}