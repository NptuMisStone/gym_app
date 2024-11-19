package com.NPTUMisStone.gym_app.Coach.Class;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ClassMain extends AppCompatActivity {

    private Connection MyConnection;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isLoadingData = false;

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
        swipeRefreshLayout = findViewById(R.id.ClassMain_swipeRefreshLayout);

        findViewById(R.id.ClassMain_backButton).setOnClickListener(v -> finish());
        findViewById(R.id.ClassMain_addButton).setOnClickListener(v -> {
            Intent intent = new Intent(ClassMain.this, ClassAdd.class);
            startActivity(intent);
        });

        setRecyclerView();

        // 下拉刷新邏輯
        swipeRefreshLayout.setOnRefreshListener(this::reloadData);
    }

    private void setRecyclerView() {
        adapter = new CustomAdapter(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        recyclerView = findViewById(R.id.ClassMain_classRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        reloadData(); // 初始載入資料
    }


    private void reloadData() {
        if (isLoadingData) return; // 防止重複加載
        isLoadingData = true;

        swipeRefreshLayout.setRefreshing(true);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Integer> idList = new ArrayList<>();
                List<byte[]> imageList = new ArrayList<>();
                List<String> nameList = new ArrayList<>();
                List<Integer> durationList = new ArrayList<>();
                List<Integer> sizeList = new ArrayList<>();
                List<Double> feeList = new ArrayList<>();

                // 加載數據
                fetchClassData(idList, imageList, nameList, durationList, sizeList, feeList);

                // 更新 UI 和資料
                new Handler(Looper.getMainLooper()).post(() -> {
                    adapter.idList = idList;
                    adapter.imageList = imageList;
                    adapter.nameList = nameList;
                    adapter.durationList = durationList;
                    adapter.sizeList = sizeList;
                    adapter.feeList = feeList;
                    adapter.notifyDataSetChanged();

                    if (idList.isEmpty()) {
                        Toast.makeText(this, "目前無課程資料", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("SQL", "Error loading data", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "加載課程列表失敗，請稍後再試", Toast.LENGTH_SHORT).show()
                );
            } finally {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoadingData = false;
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }



    private void fetchClassData(List<Integer> idList, List<byte[]> imageList, List<String> nameList, List<Integer> durationList,
                                List<Integer> sizeList, List<Double> feeList) {
        String query = "SELECT [課程編號], [課程名稱], [課程費用], " +
                "[上課人數], [課程時間長度], [課程圖片] " +
                "FROM [健身教練課程] WHERE [健身教練編號] = ?";

        try (PreparedStatement statement = MyConnection.prepareStatement(query)) {
            statement.setInt(1, Coach.getInstance().getCoachId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                idList.add(resultSet.getInt("課程編號"));
                nameList.add(resultSet.getString("課程名稱"));
                feeList.add(resultSet.getDouble("課程費用"));
                sizeList.add(resultSet.getInt("上課人數"));
                durationList.add(resultSet.getInt("課程時間長度"));
                imageList.add(resultSet.getBytes("課程圖片"));
            }
        } catch (SQLException e) {
            Log.e("SQL", "Error fetching class data", e);
            throw new RuntimeException(e);
        }
    }





    // 保留 CustomAdapter 顯示資料
    static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ItemViewHolder> {
        List<Integer> idList;
        List<byte[]> imageList;
        List<String> nameList;
        List<Integer> durationList;
        List<Integer> sizeList;
        List<Double> feeList;

        CustomAdapter(List<Integer> ids, List<byte[]> images, List<String> names, List<Integer> durations,
                      List<Integer> sizes, List<Double> fees) {
            idList = ids;
            imageList = images;
            nameList = names;
            durationList = durations;
            sizeList = sizes;
            feeList = fees;
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
            byte[] imageData = imageList.get(position);
            if (imageData != null && imageData.length > 0) {
                holder.imageView.setImageBitmap(ImageHandle.getBitmap(imageData));
            } else {
                holder.imageView.setImageResource(R.drawable.null_class);
            }

            holder.nameTextView.setText(nameList.get(position));
            holder.priceTextView.setText("$ " + (int) Math.floor(feeList.get(position)) + " /堂");
            holder.sizeTextView.setText("人數: " + sizeList.get(position) + " 人");
            holder.timeTextView.setText("時長: " + durationList.get(position) + " 分鐘");

            // 設置點擊事件
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ClassEdit.class);
                intent.putExtra("classId", idList.get(position)); // 傳遞課程 ID
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return nameList.size();
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView nameTextView;
            TextView priceTextView;
            TextView sizeTextView;
            TextView timeTextView;

            ItemViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.Class_Item_imageView);
                nameTextView = view.findViewById(R.id.Class_Item_nameText);
                priceTextView = view.findViewById(R.id.Class_Item_price);
                sizeTextView = view.findViewById(R.id.Class_Item_size);
                timeTextView = view.findViewById(R.id.Class_Item_time);
            }
        }
    }

}