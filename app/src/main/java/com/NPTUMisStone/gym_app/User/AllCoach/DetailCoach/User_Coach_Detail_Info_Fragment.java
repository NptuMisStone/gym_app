package com.NPTUMisStone.gym_app.User.AllCoach.DetailCoach;

import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.databinding.UserCoachDetailInfoFragmentBinding;
import com.NPTUMisStone.gym_app.databinding.UserDetailClassFragmentBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User_Coach_Detail_Info_Fragment extends Fragment {

    private UserCoachDetailInfoFragmentBinding binding;
    Connection MyConnection;
    private int coachID;
    TextView coachname,coachintro,coachgender,coachphone,coachmail,coachtype,storename,storeaddress,storephone,storemail;
    LinearLayout storeview;
    public static User_Coach_Detail_Info_Fragment newInstance() {
        return new User_Coach_Detail_Info_Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding= UserCoachDetailInfoFragmentBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        if (getArguments() != null) {
            coachID = getArguments().getInt("coachID");
        }
        bindID();
        try {
            MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
            String query = "SELECT * FROM [健身教練審核合併] WHERE 健身教練編號 = ? " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,coachID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
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
                coachmail.setText(rs.getString("健身教練郵件"));
                coachtype.setText(rs.getString("註冊類型"));
                if(rs.getString("註冊類型").equals("私人健身教練")){
                    storeview.setVisibility(View.GONE);
                }
                storename.setText(rs.getString("服務地點名稱"));
                storeaddress.setText(rs.getString("服務地點地址"));
                storephone.setText(rs.getString("服務地點電話"));
                storemail.setText(rs.getString("服務地點郵件"));
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return root;
    }
    private void bindID(){
        coachname=binding.userDetailCoachname;
        coachintro=binding.userDetailCoachintro;
        coachgender=binding.userDetailCoachgender;
        coachphone=binding.userDetailCoachphone;
        coachmail=binding.userDetailCoachmail;
        coachtype=binding.userDetailCoachtype;
        storename=binding.userDetailCoachStoreName;
        storeaddress=binding.userDetailCoachStoreAddress;
        storephone=binding.userDetailCoachStorePhone;
        storemail=binding.userDetailCoachStoreMail;
        storeview=binding.userDetailCoachStoreView;
    }


}