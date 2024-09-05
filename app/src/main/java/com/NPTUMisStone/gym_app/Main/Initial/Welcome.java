package com.NPTUMisStone.gym_app.Main.Initial;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Identify.Login;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.util.Objects;

public class Welcome extends AppCompatActivity {
    Connection MyConnection;
    boolean isTrying = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_initial_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        new Handler(Looper.getMainLooper()).postDelayed(this::Connect, 500);
    }
    private void Connect() {    //資料庫連線：https://blog.csdn.net/jacobywu/article/details/125614398
        isTrying = true;
        ProgressBar progressBar = findViewById(R.id.welcome_progressBar);
        ImageView welcome_state = findViewById(R.id.welcome_state);
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            if (MyConnection != null) {
                SnackBarUtils.makeShort(findViewById(R.id.main), "連線成功").confirm(); //SnackBar：別用Toast了，來試試Snack bar：https://blog.csdn.net/g984160547/article/details/121269520 //一行代码搞定Snack bar：https://www.jianshu.com/p/f4ba05d7bbda //比系统自带的更好用的SnackBar：https://www.jianshu.com/p/e3c82b98f151
                progressBar.setVisibility(View.GONE);
                welcome_state.setVisibility(View.VISIBLE);
                welcome_state.setImageResource(R.drawable.main_welcome_ic_correct);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startActivity(new Intent(this, Login.class));
                    finish();
                }, 500);
            } else {
                isTrying = false;
                progressBar.setVisibility(View.GONE);
                welcome_state.setVisibility(View.VISIBLE);
                findViewById(R.id.logoImage).setOnClickListener(v -> {  if(!isTrying) retry();});
            }
        } catch (Exception e) {
            Log.e("拿不到連線狀態回傳", Objects.requireNonNull(e.getMessage()));
            isTrying = false;
        }
    }

    private void retry() {
        findViewById(R.id.welcome_progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.welcome_state).setVisibility(View.GONE);
        new Handler(Looper.getMainLooper()).postDelayed(this::Connect, 500);
    }
}