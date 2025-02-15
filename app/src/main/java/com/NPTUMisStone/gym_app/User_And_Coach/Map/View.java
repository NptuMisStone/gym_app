package com.NPTUMisStone.gym_app.User_And_Coach.Map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.R;

public class View extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_and_coach_map_class);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.MapClass_backButton).setOnClickListener(v -> finish());
        //權限請求
        PermissionGET();
    }
    private void PermissionGET() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ViewFragment()).commit();
        else new AlertDialog.Builder(this).setTitle("權限請求")
                    .setPositiveButton("確定", (dialog, which) -> ActivityCompat.requestPermissions(View.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1))
                    .setNegativeButton("取消", (dialog, which) -> finish())
                    .setMessage("地圖需使用定位權限，請允許請求").show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ViewFragment()).commit();
            else new AlertDialog.Builder(this).setTitle("權限請求").setMessage("已曾被拒絕權限，請自行手動開啟定位權限")
                        .setPositiveButton("確定", (dialog, which) -> startActivity(
                                new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.fromParts("package", getPackageName(), null))))
                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
        }
    }
}