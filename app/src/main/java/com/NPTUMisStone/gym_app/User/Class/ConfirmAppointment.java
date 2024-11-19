package com.NPTUMisStone.gym_app.User.Class;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User.Records.AppointmentAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

public class ConfirmAppointment extends AppCompatActivity {
    Connection MyConnection;
    Intent intent;
    int scheduleID,classID,coachID,appeople;
    TextView classname,classtimelong,classplace,classaddress,classdatetime,homeplace,title16,title18,title34;
    EditText note,homeaddress;
    ImageButton gobackbtn;
    String Ap_date, Ap_starttime, Ap_endtime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_confirm_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        intent = getIntent();
        scheduleID = intent.getIntExtra("預約課表ID", 0);
        Bind();
        GetID();
        SearchApPeople();

        try {
            String query = "SELECT * FROM 健身教練課表課程合併 WHERE 課表編號 = ? " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,scheduleID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                classname.setText(rs.getString("課程名稱"));
                classtimelong.setText(rs.getString("課程時間長度")+"分鐘");
                classplace.setText(rs.getString("地點名稱"));
                classaddress.setText(rs.getString("縣市")+rs.getString("行政區")+rs.getString("地點地址"));
                classdatetime.setText(rs.getString("日期")+"("+rs.getString("開始時間")+"~"+rs.getString("結束時間")+")");
                homeplace.setText(rs.getString("縣市")+rs.getString("行政區"));
                String checkplace;
                checkplace=rs.getString("地點類型");
                if(checkplace.equals("2")){
                    classplace.setVisibility(View.GONE);
                    classaddress.setVisibility(View.GONE);
                    homeplace.setVisibility(View.VISIBLE);
                    homeaddress.setVisibility(View.VISIBLE);
                    title16.setVisibility(View.GONE);
                    title18.setVisibility(View.GONE);
                    title34.setVisibility(View.VISIBLE);
                }
                else {
                    classplace.setVisibility(View.VISIBLE);
                    classaddress.setVisibility(View.VISIBLE);
                    homeplace.setVisibility(View.GONE);
                    homeaddress.setVisibility(View.GONE);
                    title34.setVisibility(View.GONE);
                    title16.setVisibility(View.VISIBLE);
                    title18.setVisibility(View.VISIBLE);
                }
                Ap_date=rs.getString("日期");
                Ap_starttime=rs.getString("開始時間");
                Ap_endtime=rs.getString("結束時間");
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void GetID(){
        try {
            String query = "SELECT 課程編號,健身教練編號 FROM 健身教練課表課程合併 WHERE 課表編號 = ? " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,scheduleID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                classID=rs.getInt("課程編號");
                coachID=rs.getInt("健身教練編號");
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public  void UserConfirmAP(View view){
        if(CheckAP()){
            try {
                String query = "Insert Into 使用者預約 (使用者編號,健身教練編號,課程編號,課表編號,預約狀態,備註,客戶到府地址)values(?,?,?,?,?,?,?)" ;
                PreparedStatement Statement = MyConnection.prepareStatement(query);
                Statement.setInt(1,User.getInstance().getUserId());
                Statement.setInt(2,coachID);
                Statement.setInt(3,classID);
                Statement.setInt(4,scheduleID);
                Statement.setInt(5,1);
                Statement.setString(6,note.getText().toString());
                Statement.setString(7,homeaddress.getText().toString());
                Statement.executeQuery();
                Statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            UpdateApPeople();
            Intent it=new Intent(this,AppointmentAll.class);
            startActivity(it);
            finish();
        }
    }
    private void SearchApPeople(){
        try {
            String query = "SELECT 預約人數 FROM 健身教練課表 WHERE 課表編號 = ? " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,scheduleID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                appeople=rs.getInt("預約人數");
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void UpdateApPeople(){
        try {
            String query = "UPDATE 健身教練課表 SET 預約人數 = ? WHERE 課表編號 = ?" ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,(appeople + 1));
            Statement.setInt(2,scheduleID);
            Statement.executeQuery();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private boolean CheckAP(){
        try {
            String query = "SELECT 開始時間, 結束時間 FROM [使用者預約-有預約的] WHERE 日期 = ? AND 使用者編號 = ? AND 預約狀態 = 1 " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setString(1,Ap_date);
            Statement.setInt(2,User.getInstance().getUserId());
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                LocalTime scheduledStartTime = LocalTime.parse(rs.getString("開始時間"));
                LocalTime scheduledEndTime = LocalTime.parse(rs.getString("結束時間"));

                LocalTime selectedStartTime = LocalTime.parse(Ap_starttime);
                LocalTime selectedEndTime = LocalTime.parse(Ap_endtime);

                if ((selectedStartTime.isAfter(scheduledStartTime) || selectedStartTime.equals(scheduledStartTime)) && selectedStartTime.isBefore(scheduledEndTime) ||
                        (selectedEndTime.isAfter(scheduledStartTime) && (selectedEndTime.isBefore(scheduledEndTime) || selectedEndTime.equals(scheduledEndTime))) ||
                        (selectedStartTime.isBefore(scheduledStartTime) || selectedStartTime.equals(scheduledStartTime)) && (selectedEndTime.isAfter(scheduledEndTime) || selectedEndTime.equals(scheduledEndTime)))
                {
                    new AlertDialog.Builder(this)
                            .setTitle("時間衝突")
                            .setMessage("預約時段衝突")
                            .setIcon(R.drawable.warning)
                            .show();
                    return false;
                }
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
    private void Bind(){
        classname=findViewById(R.id.user_confirmAP_classname);
        classtimelong=findViewById(R.id.user_confirmAP_timelong);
        classplace=findViewById(R.id.user_confirmAP_place);
        classaddress=findViewById(R.id.user_confirmAP_address);
        classdatetime=findViewById(R.id.user_confirmAP_datetime);
        note=findViewById(R.id.user_confirmAP_note);
        homeplace=findViewById(R.id.user_confirmAP_homeplace);
        homeaddress=findViewById(R.id.user_confirmAP_homeAddress);
        title16=findViewById(R.id.textView16);
        title18=findViewById(R.id.textView18);
        title34=findViewById(R.id.textView34);
        gobackbtn=findViewById(R.id.user_confirm_back);
    }
    public  void user_confirm_goback(View view){
        finish();
    }
}