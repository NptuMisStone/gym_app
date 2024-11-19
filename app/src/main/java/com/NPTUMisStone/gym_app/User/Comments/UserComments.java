package com.NPTUMisStone.gym_app.User.Comments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class UserComments extends AppCompatActivity {
    EditText commenteditText;
    Intent intent;
    int reservationID;
    int myRating = 0;
    String wherefrom;
    Connection MyConnection;
    Button submitbtn;
    ImageView coachimg;
    TextView coachname,classname,date;
    RatingBar commentratingbar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_comments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();

        commenteditText=findViewById(R.id.user_comment_edittext);
        submitbtn=findViewById(R.id.user_comment_submit);
        coachimg=findViewById(R.id.user_comment_image);
        coachname=findViewById(R.id.user_comment_coach_name);
        classname=findViewById(R.id.user_comment_class_name);
        date=findViewById(R.id.user_comment_date);
        commentratingbar=findViewById(R.id.user_comment_ratingbar);

        intent = getIntent();
        reservationID = intent.getIntExtra("reservationID", 0);
        wherefrom=intent.getStringExtra("從哪裡來");
        commentratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {
                myRating = (int) rating;
            }
        });

        try {
            String query = "SELECT 健身教練圖片,課程名稱,健身教練姓名,日期 FROM [使用者預約-評論用] WHERE 預約編號 = ? AND 使用者編號 = ? " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,reservationID);
            Statement.setInt(2, User.getInstance().getUserId());
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                if (rs.getBytes("健身教練圖片") != null) {
                    Bitmap bitmap = ImageHandle.getBitmap(rs.getBytes("健身教練圖片"));
                    coachimg.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
                }
                coachname.setText(rs.getString("健身教練姓名"));
                classname.setText(rs.getString("課程名稱"));
                date.setText(String.valueOf( rs.getDate("日期")));
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            String query = "SELECT * FROM 完成預約評論表 WHERE 預約編號 = ? " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,reservationID);
            ResultSet rs = Statement.executeQuery();
            if (!rs.next()) {       //資料庫沒評論
                submitbtn.setText("評論");
                commentratingbar.setRating(0);

            }else {     //資料庫有評論
                submitbtn.setText("更改");
                commentratingbar.setRating(rs.getInt("評分"));
                commenteditText.setText(rs.getString("評論內容"));
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void commentsubmit(View view) {
        String buttonText = submitbtn.getText().toString();
        if ("評論".equals(buttonText)) {
            try {
                String insertQuery = "INSERT INTO 完成預約評論表 (預約編號, 評分, 評論內容 , 評論日期, 評論時間) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = MyConnection.prepareStatement(insertQuery);
                preparedStatement.setInt(1, reservationID); // 預約編號
                preparedStatement.setInt(2, myRating); // 評分

                if (commenteditText.getText().toString().isEmpty()) {
                    preparedStatement.setNull(3, java.sql.Types.VARCHAR); // 置為 null
                } else {
                    preparedStatement.setString(3, commenteditText.getText().toString()); // 非空則設置評論內容
                }

                preparedStatement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                // 获取当前的时间并格式化为 HH:mm:ss
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                String currentTimeString = timeFormat.format(new java.util.Date());
                java.sql.Time currentTime = java.sql.Time.valueOf(currentTimeString);
                preparedStatement.setTime(5, currentTime);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                if(wherefrom.equals("完成預約頁")){
                    Intent it = new Intent(this,AppointmentAll.class);
                    it.putExtra("是否是評論",1);
                    startActivity(it);
                    finish();
                } else if (wherefrom.equals("教練詳細頁")) {
                    setResult(RESULT_OK);
                    finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("更改".equals(buttonText)) {
            try {
                String updateQuery = "UPDATE 完成預約評論表 SET 評論內容 = ?, 評分 = ? WHERE 預約編號 = ?";
                PreparedStatement preparedStatement = MyConnection.prepareStatement(updateQuery);
                if (commenteditText.getText().toString().isEmpty()) {  //評論內容
                    // 置為 null
                    preparedStatement.setNull(1, java.sql.Types.VARCHAR);
                } else {
                    // 非空則設置評論內容
                    preparedStatement.setString(1, commenteditText.getText().toString());
                }
                preparedStatement.setInt(2, myRating); // 評分
                preparedStatement.setInt(3, reservationID); // 預約編號

                preparedStatement.executeUpdate();
                preparedStatement.close();
                if(wherefrom.equals("完成預約頁")){
                    Intent it = new Intent(this,AppointmentAll.class);
                    it.putExtra("是否是評論",1);
                    startActivity(it);
                    finish();
                } else if (wherefrom.equals("教練詳細頁")) {
                    setResult(RESULT_OK);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public  void user_Comment_goback(View view){
        if(wherefrom.equals("完成預約頁")){
            Intent it = new Intent(this,AppointmentAll.class);
            it.putExtra("是否是評論",1);
            startActivity(it);
            finish();
        } else if (wherefrom.equals("教練詳細頁")) {
            finish();
        }

    }
}