package com.NPTUMisStone.mapview;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {
    private static Connection INSTANCE = null; //儲存已成功的連線
    int MAX_RETRY = 3; //最大重試次數
    String ip = "nptumisstone.database.windows.net"; //"203.64.129.17"; //專題電腦 //"192.168.197.209";  //錯誤ip演示
    String JtdsClass = "net.sourceforge.jtds.jdbc.Driver"; // 設定JDBC 驅動程式類別
    String dbname = "gym", username = "NptuMisStone@nptumisstone", password = "-MisStoneNptu"; //username = "sqlserver",password = "sqlserver";
    View view;  //提供View讓Snack-bar能夠顯示

    public SQLConnection(View view) {
        this.view = view;
    }

    public synchronized Connection IWantToConnection() { //連接資料庫 //加上synchronized，避免重複連線
        int retry_count = 0;
        while (retry_count < MAX_RETRY) {
            if (INSTANCE != null)
                try {
                    if (INSTANCE.isClosed()) {
                        INSTANCE = null; // 重設 INSTANCE 強迫重新連線
                    } else {
                        return INSTANCE;
                    }
                } catch (SQLException e) {
                    Log.e("SQLConnection", "檢查連接狀態時出錯: " + e.getMessage());
                    INSTANCE = null; // Reset INSTANCE to force reconnection
                }
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
            try {
                Class.forName(JtdsClass);
                String ConnURL = "jdbc:jtds:sqlserver://" + ip + ":1433;ssl=require;databaseName=" + dbname + ";user=" + username + ";password=" + password + ";loginTimeout=5;socketTimeout=30";
                INSTANCE = DriverManager.getConnection(ConnURL);
                return INSTANCE;
            } catch (SQLException e) {
                Log.e("SQLConnection", "SQL 錯誤: " + e.getMessage());
                SnackBarUtils.makeShort(view, "無法連接到資料庫！").danger();
            } catch (ClassNotFoundException e) {
                Log.e("SQLConnection", "找不到驅動程式類別: " + e.getMessage());
                SnackBarUtils.makeShort(view, "找不到驅動程式類別！").danger();
            } catch (Exception e) {
                Log.e("SQLConnection", "錯誤: " + e.getMessage());
                SnackBarUtils.makeShort(view, "SQL額外錯誤！").danger();
            }
        }
        return null;
    }
}
