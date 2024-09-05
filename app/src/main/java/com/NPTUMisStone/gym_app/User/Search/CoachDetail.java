package com.NPTUMisStone.gym_app.User.Search;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;

import java.sql.Connection;
import java.sql.ResultSet;

public class CoachDetail extends AppCompatActivity {
    Connection MyConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_search_coach_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getTheCoach();
        findViewById(R.id.CoachDetail_goBack).setOnClickListener(v -> finish());

    }
    private void getTheCoach() {
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            ResultSet resultSet = MyConnection.createStatement().executeQuery("SELECT * FROM 健身教練資料 WHERE 健身教練編號 = " + getIntent().getIntExtra("coach_id", 0));
            if (resultSet.next()) {
                TextView coach_name = findViewById(R.id.CoachDetail_coachText);
                coach_name.setText(resultSet.getString("健身教練姓名"));
                ImageView coach_image = findViewById(R.id.CoachDetail_coachImage);
                coach_image.setImageBitmap(ImageHandle.getBitmap(resultSet.getBytes("健身教練圖片")));
            }
        }catch (Exception e){
            Log.e("CoachDetail", "getTheCoach: ", e);
        }
    }

}