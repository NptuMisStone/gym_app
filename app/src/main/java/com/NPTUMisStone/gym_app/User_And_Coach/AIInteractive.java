package com.NPTUMisStone.gym_app.User_And_Coach;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.Main.Initial.SnackBarUtils;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class AIInteractive extends AppCompatActivity {
    static String BASE_URL = "https://g1014308research-f6hvaugmd2cxg3as.eastus-01.azurewebsites.net/";
    private boolean hasShownSuccess = false;
    private ProgressBarHandler progressBarHandler;
    Connection MyConnection;

    interface HealthCheckService {
        @GET("/health")
        Call<ResponseBody> checkHealth();
    }

    interface RagChainService {
        @POST("/rag/invoke")
        Call<ResponseBody> invokeRagChain(@Body RequestBody body);
    }

    private Retrofit retrofitInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_and_coach_ai_interactive);

        retrofitInstance = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        progressBarHandler = new ProgressBarHandler(this,findViewById(android.R.id.content));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkHealth();
        findViewById(R.id.AIInteractive_submitButton).setOnClickListener(v -> sendRequest(((EditText) findViewById(R.id.AIInteractive_inputEdit)).getText().toString()));
        findViewById(R.id.AIInteractive_backButton).setOnClickListener(v -> finish());
    }

    private void sendRequest(String inputText) {
        progressBarHandler.showProgressBar();
        checkHealth();
        invokeRagChain(inputText);
    }

    private void checkHealth() {
        HealthCheckService healthCheckService = retrofitInstance.create(HealthCheckService.class);
        healthCheckService.checkHealth().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (hasShownSuccess) return;
                    SnackBarUtils.makeShort(findViewById(R.id.main), "AI伺服器連接成功").confirm();
                    hasShownSuccess = true;
                } else {
                    SnackBarUtils.makeShort(findViewById(R.id.main), "AI伺服器連接失敗 " + response.code()).danger();
                }
                progressBarHandler.hideProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("Response", "Health check request failed", t);
                SnackBarUtils.makeShort(findViewById(R.id.main), "AI伺服器連接失敗 " + t.getMessage()).danger();
                progressBarHandler.hideProgressBar();
            }
        });
    }

    private void invokeRagChain(String inputText) {
        RagChainService ragChainService = retrofitInstance.create(RagChainService.class);
        RequestBody requestBody = RequestBody.create("{\"input\": \"" + inputText + "\"}", MediaType.parse("application/json"));
        ragChainService.invokeRagChain(requestBody).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                handleResponse(response);
                progressBarHandler.hideProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("Response", "Rag chain request failed", t);
                progressBarHandler.hideProgressBar();
            }
        });
    }

    private void handleResponse(Response<ResponseBody> response) {
        if (response.isSuccessful()) {
            try {
                String responseBody = Objects.requireNonNull(response.body()).string();
                new AlertDialog.Builder(this).setTitle("AI回應").setMessage(responseBody)
                        .setPositiveButton("紀錄歷史訊息", (dialog, which) -> {
                            // TODO: 紀錄歷史訊息
                            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                            //MyConnection.createStatement().executeUpdate("INSERT INTO AIInteractiveHistory (input, output) VALUES ('" + inputText + "', '" + responseBody + "')");
                        })
                        .setNegativeButton("確認", (dialog, which) -> dialog.dismiss()).show();
                Log.d("Response", responseBody);
            } catch (Exception e) {
                Log.e("Response", "Error parsing response", e);
            }
        } else {
            Log.e("Rag Chain", "Request failed with code: " + response.code());
        }
    }
}