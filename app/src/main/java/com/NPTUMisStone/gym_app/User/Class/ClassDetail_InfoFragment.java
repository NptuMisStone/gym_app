package com.NPTUMisStone.gym_app.User.Class;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.databinding.UserDetailClassFragmentBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClassDetail_InfoFragment extends Fragment {

    private UserDetailClassFragmentBinding binding;
    Connection MyConnection;
    private int classID;
    private TextView classname,classintro,classprice,classpeople,classtimelong,classplace,classitem,coachname,coachintro,coachgender,coachphone,coachemail;
    private ImageView coachImage;

    public static ClassDetail_InfoFragment newInstance() {
        return new ClassDetail_InfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding= UserDetailClassFragmentBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        if (getArguments() != null) {
            classID = getArguments().getInt("classID");
        }
        bindID();
        try {
            MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
            String query = "SELECT * FROM [健身教練課程-有排課的] WHERE 課程編號 = ? " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,classID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                classname.setText(rs.getString("課程名稱"));
                classintro.setText(rs.getString("課程內容介紹"));
                classprice.setText("$" + rs.getString("課程費用").split("\\.")[0] + "/堂");
                classpeople.setText(rs.getString("上課人數")+"人");
                classtimelong.setText(rs.getString("課程時間長度")+"分鐘");
                classplace.setText(rs.getString("顯示地點名稱"));
                classitem.setText(rs.getString("所需設備"));
                if (rs.getBytes("健身教練圖片") != null) {
                    Bitmap bitmap = ImageHandle.getBitmap(rs.getBytes("健身教練圖片"));
                    coachImage.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
                }
                coachname.setText(rs.getString("健身教練姓名"));
                coachintro.setText(rs.getString("健身教練介紹"));
                switch (rs.getString("健身教練性別")){
                    case "1":
                        coachgender.setText("男性");
                        break;
                    case "2":
                        coachgender.setText("女性");
                        break;
                    case "3":
                        coachgender.setText("不願透露性別");
                        break;
                }
                coachphone.setText(rs.getString("健身教練電話"));
                coachemail.setText(rs.getString("健身教練郵件"));
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            Log.e("SQL", "Error in SQL", e);
        }
        return root;
    }
    private void bindID(){
        classname=binding.userDetailClassname;
        classintro=binding.userDetailClassintro;
        classprice=binding.userDetailClassprice;
        classpeople=binding.userDetailClasspeople;
        classtimelong=binding.userDetailClasstimelong;
        classplace=binding.userDetailClassplace;
        classitem=binding.userDetailClassitem;

        coachImage =binding.userDetailCoachimg;
        coachname=binding.userDetailCoachname;
        coachintro=binding.userDetailCoachintro;
        coachgender=binding.userDetailCoachgender;
        coachphone=binding.userDetailCoachphone;
        coachemail=binding.userDetailCoachemail;
    }




}