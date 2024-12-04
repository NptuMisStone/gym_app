package com.NPTUMisStone.gym_app.User.Coach.Fragment;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Class.ClassDetail;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.databinding.UserCoachDetailClassFragmentBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CoachDetail_ClassFragment extends Fragment {

    private UserCoachDetailClassFragmentBinding binding;
    Connection MyConnection;
    int coachID;
    ArrayList<CoachDetail_ClassFragment.User_Coach_Detail_Class_Data> classData = new ArrayList<>();
    private ProgressBar progressBar;
    TextView nodata;

    public static CoachDetail_ClassFragment newInstance() {
        return new CoachDetail_ClassFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding= UserCoachDetailClassFragmentBinding.inflate(inflater,container,false);
        nodata=binding.userCoachDetailClassNodata;
        nodata.setVisibility(View.GONE);
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        View root = binding.getRoot();
        if (getArguments() != null) {
            coachID = getArguments().getInt("coachID");
        }
        fetchClass();
        return root;
    }
    private void fetchClass() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                classData.clear();
                String sql = "SELECT * FROM [健身教練課程-有排課的] WHERE [健身教練編號] = ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, coachID);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    classData.add(new CoachDetail_ClassFragment.User_Coach_Detail_Class_Data(
                            rs.getInt("課程編號"),
                            rs.getBytes("課程圖片"),
                            rs.getString("課程名稱"),
                            rs.getString("課程費用"),
                            rs.getString("課程內容介紹"),
                            rs.getString("上課人數"),
                            rs.getString("課程時間長度"),
                            rs.getString("顯示地點名稱"),
                            rs.getString("所需設備")
                    ));
                rs.close();
                searchStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });
    }
    private void updateUI() {
        if (getActivity() != null && isAdded()) {
            CoachDetail_ClassFragment.Detail_Coach_ClassAdapter detailCoachClassAdapter = new CoachDetail_ClassFragment.Detail_Coach_ClassAdapter(getActivity(),classData,binding.getRoot());
            RecyclerView coachDetailClassRecyclerView= binding.userCoachDetailClassRecyclerview;
            coachDetailClassRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            coachDetailClassRecyclerView.setAdapter(detailCoachClassAdapter);
            progressBar.setVisibility(View.GONE);
            if (classData.isEmpty()) {
                nodata.setVisibility(View.VISIBLE);
            } else {
                nodata.setVisibility(View.GONE);
            }

        }
    }
    static class User_Coach_Detail_Class_Data {
        int classID;
        byte[] classimage;
        String className,classPrice,classIntro,classPeople,classTimelong,classPlace,classItem;

        static ArrayList<CoachDetail_ClassFragment.User_Coach_Detail_Class_Data> classData = new ArrayList<>();

        public User_Coach_Detail_Class_Data(int classID,byte[] classimage,String className,String classPrice,String classIntro,String classPeople,String classTimelong,String classPlace,String classItem) {
            this.classID=classID;
            this.classimage=classimage;
            this.className=className;
            this.classPrice=classPrice;
            this.classIntro=classIntro;
            this.classPeople=classPeople;
            this.classTimelong=classTimelong;
            this.classPlace=classPlace;
            this.classItem=classItem;
        }
        public static ArrayList<CoachDetail_ClassFragment.User_Coach_Detail_Class_Data> getClassData() {
            if (classData == null) {
                return null;
            }
            return classData;
        }


        private int getClassID() {
            return classID;
        }

        private byte[] getClassimage() {
            return classimage;
        }
        private String getClassName(){return className;}
        private String getClassPrice(){return  classPrice;}

        private String getClassIntro() {
            return classIntro;
        }

        private String getClassPeople() {
            return classPeople;
        }
        private String getClassTimelong(){return classTimelong;}
        private String getClassPlace(){return classPlace;}
        private String getClassItem(){return classItem;}
    }
    class Detail_Coach_ClassAdapter extends RecyclerView.Adapter<CoachDetail_ClassFragment.Detail_Coach_ClassAdapter.ViewHolder>
    {

        List<CoachDetail_ClassFragment.User_Coach_Detail_Class_Data> class_dataList;
        Context context;
        View view;
        public Detail_Coach_ClassAdapter(Context context, List<CoachDetail_ClassFragment.User_Coach_Detail_Class_Data> class_dataList , View view) {
            this.context = context;
            this.class_dataList = class_dataList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView class_image;
            TextView class_people_sign,class_name,class_price,class_intro,class_people,class_timelong,class_place,class_item;
            ConstraintLayout seeclassdetail;
            public ViewHolder(View itemView) {
                super(itemView);
                class_image=itemView.findViewById(R.id.user_detail_coach_class_img);
                class_people_sign=itemView.findViewById(R.id.user_detail_coach_class_people_sign);
                class_name=itemView.findViewById(R.id.user_detail_coach_class_classname);
                class_price=itemView.findViewById(R.id.user_detail_coach_class_price);
                class_intro=itemView.findViewById(R.id.user_detail_coach_class_intro);
                class_people=itemView.findViewById(R.id.user_detail_coach_class_people);
                class_timelong=itemView.findViewById(R.id.user_detail_coach_class_timelong);
                class_place=itemView.findViewById(R.id.user_detail_coach_class_place);
                class_item=itemView.findViewById(R.id.user_detail_coach_class_item);
                seeclassdetail=itemView.findViewById(R.id.user_coach_detail_class_seemore);

            }
        }
        @NonNull
        @Override
        public CoachDetail_ClassFragment.Detail_Coach_ClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_coach_detail_class_fragment_item, parent, false);
            return new CoachDetail_ClassFragment.Detail_Coach_ClassAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CoachDetail_ClassFragment.Detail_Coach_ClassAdapter.ViewHolder holder, int position) {
            CoachDetail_ClassFragment.User_Coach_Detail_Class_Data item = class_dataList.get(position);

            if (item.getClassimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getClassimage());
                holder.class_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.class_name.setText(item.getClassName());
            holder.class_price.setText("$"+item.getClassPrice().split("\\.")[0]+"/堂");
            holder.class_intro.setText(item.getClassIntro());
            holder.class_people.setText("人數："+item.getClassPeople()+"人");
            holder.class_timelong.setText("時間："+item.getClassTimelong()+"分鐘");
            holder.class_place.setText("地點"+item.getClassPlace());
            holder.class_item.setText("所需設備："+item.getClassItem());
            holder.seeclassdetail.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("看更多課程ID", item.getClassID());
                editor.apply(); // 保存
                Intent intent = new Intent(requireActivity(), ClassDetail.class);
                startActivity(intent);
            });
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                String SQL = "SELECT 上課人數 FROM 健身教練課程 WHERE 課程編號 = ? ";
                PreparedStatement Statement = MyConnection.prepareStatement(SQL);
                Statement.setInt(1,item.getClassID());
                ResultSet rs = Statement.executeQuery();
                while (rs.next()) {
                    if(item.getClassPeople().equals("1")){
                        holder.class_people_sign.setText("一對一");
                    }
                    else {
                        holder.class_people_sign.setText("團體");
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }




        @Override
        public int getItemCount() {
            return class_dataList.size();
        }
    }




}