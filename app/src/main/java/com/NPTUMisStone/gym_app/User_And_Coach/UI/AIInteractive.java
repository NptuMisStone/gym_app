package com.NPTUMisStone.gym_app.User_And_Coach.UI;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

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
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ProgressBarHandler;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
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

    static class Question {
        String title;
        String answer;

        public Question(String title, String answer) {
            this.title = title;
            this.answer = answer;
        }

        public String getTitle() {
            return title;
        }

        public String getAnswer() {
            return answer;
        }
    }

    static class ExpandableQuestionAdapter extends BaseExpandableListAdapter {
        Context context;
        List<Question> questions;

        public ExpandableQuestionAdapter(Context context, List<Question> questions) {
            this.context = context;
            this.questions = questions;
        }

        @Override
        public int getGroupCount() {
            return questions.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1; // 每個問題只有一個解答
        }

        @Override
        public Object getGroup(int groupPosition) {
            return questions.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return questions.get(groupPosition).getAnswer();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String title = ((Question) getGroup(groupPosition)).getTitle();
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
            TextView textView = convertView.findViewById(android.R.id.text1);
            textView.setText(title);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String answer = (String) getChild(groupPosition, childPosition);
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.user_and_coach_ai_interactive_list_item_expandable_child, parent, false);
            TextView textView = convertView.findViewById(R.id.child_text_view);
            textView.setText(answer);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_and_coach_ai_interactive);

        retrofitInstance = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkHealth();

        findViewById(R.id.AIInteractive_submitButton).setOnClickListener(v -> sendRequest(((EditText) findViewById(R.id.AIInteractive_inputEdit)).getText().toString()));
        findViewById(R.id.AIInteractive_backButton).setOnClickListener(v -> finish());

        List<Question> questions = new ArrayList<>();
        questions.add(new Question("如何知道健身教練的詳細資訊及安排的課程？", "在健身教練頁面，點擊任何一個教練即可查看教練詳細資訊及他所有安排的課程。"));
        questions.add(new Question("想知道如何預約課程？", "在尋找課程中會有各個教練安排的課程供你們選擇，課程點擊後，即可依照課程日期、時間進行預約。"));
        questions.add(new Question("如何查找特定課程？", "在尋找課程中，搜尋列表旁，有個篩選按鈕，可進行選取想查詢的課程。"));
        questions.add(new Question("想知道如何取消預約？", "在預約紀錄中，可以查看所有預約紀錄，並進行取消。"));
        questions.add(new Question("如何查看目前所收藏的教練及課程？", "在我的收藏中，即可看到所收藏的教練及課程。"));

        ExpandableListView expandableListView = findViewById(R.id.AIInteractive_expandableList);

        ExpandableQuestionAdapter adapter = new ExpandableQuestionAdapter(this, questions);
        expandableListView.setAdapter(adapter);
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