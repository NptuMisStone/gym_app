package com.NPTUMisStone.gym_app.User.Search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SportList extends AppCompatActivity {
    Connection MyConnection;
    //碼農日常-『Android studio』SearchView+RecyclerView 列表搜尋之基礎用法：https://thumbb13555.pixnet.net/blog/post/328072571
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_search_sport_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if(MyConnection == null) MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        RecyclerView recyclerView = findViewById(R.id.SearchSport_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<String> arrayList = new ArrayList<>();
        String[] strings = {"CRUISER MK. I","CRUISER MK. II","VALENTINE","CRUISER MK. III"
                ,"CRUISER MK. IV","COVENANTED","CRUSADER","GSR 3301 SETTER","MTV","GSON3301 AVR FS"
                ,"MANTICORE","MATILDA","CHURCHILL I","CHURCHILL VII","BLACK PRINCE","CARNATION"
                ,"CONQUEROR","SUPER CONQUEROR","CAVALIER","CROMWELL","COMET","CENTURION MK. I"
                ,"CENTURION MK. 7/1","CENTURION ACTION X","VALENTINE AT","BISHOP","FV304"
                ,"CRUSADER 5.5-IN. SP","FV207","FV3805","CONQUEROR GUN CARRIAGE"
                ,"AT 2","ACHILLES","CHALLENGER","CHARIOTEER","FV4004 CONWAY","FV4005 STAGE II"
                ,"AT 8","AT 7","AT 15","TORTOISE","FV217 BADGER"};

        Collections.addAll(arrayList, getScheduledCourses());
        mAdapter = new RecyclerViewAdapter(arrayList);
        recyclerView.setAdapter(mAdapter);
        setSearchView();
    }
    private String[] getScheduledCourses() {
        String searchQuery = "SELECT [健身教練資料].健身教練姓名, [健身教練課表].日期, [健身教練課程].課程名稱 " +
                "FROM [健身教練課表] " +
                "LEFT JOIN [健身教練課程] ON [健身教練課表].課程編號 = [健身教練課程].課程編號 " +
                "LEFT JOIN [健身教練資料] ON [健身教練課程].健身教練編號 = [健身教練資料].健身教練編號";
        try(PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery)){
            ResultSet resultSet = searchStatement.executeQuery();
            List<String> coursesList = new ArrayList<>();
            while (resultSet.next()) {
                String coachName = resultSet.getString("健身教練姓名");
                String date = resultSet.getString("日期");
                String courseName = resultSet.getString("課程名稱");
                coursesList.add(coachName + " " + date + " " + courseName);
            }
            return coursesList.toArray(new String[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void goBack(View view) {
        finish();
    }
    public void goChange(View view) {
        Button changeButton1 = findViewById(R.id.SearchSport_changeButton1), changeButton2 = findViewById(R.id.SearchSport_changeButton2), changeButton3 = findViewById(R.id.SearchSport_changeButton3);
        ConstraintLayout constraintLayout1 = findViewById(R.id.SearchSport_constraintLayout1), constraintLayout2 = findViewById(R.id.SearchSport_constraintLayout2), constraintLayout3 = findViewById(R.id.SearchSport_constraintLayout3);
        if (view == changeButton1) {
            setConstraint(constraintLayout1, constraintLayout2, constraintLayout3);
        } else if (view == changeButton2) {
            setConstraint(constraintLayout2, constraintLayout1, constraintLayout3);
        } else {
            setConstraint(constraintLayout3, constraintLayout1, constraintLayout2);
        }
    }
    private void setConstraint(ConstraintLayout constraintLayout1, ConstraintLayout constraintLayout2, ConstraintLayout constraintLayout3) {
        constraintLayout1.setVisibility(View.VISIBLE);
        constraintLayout2.setVisibility(View.GONE);
        constraintLayout3.setVisibility(View.GONE);
    }
    RecyclerViewAdapter mAdapter;
    /**初始化Toolbar內SearchView的設置*/
    private void setSearchView() {
        /**SearchView設置，以及輸入內容後的行動*/
        ((SearchView)findViewById(R.id.SearchSport_searchView)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                /**調用RecyclerView內的Filter方法*/
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }
    static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {

        /**上方的arrayList為RecyclerView所綁定的ArrayList*/
        ArrayList<String> arrayList;
        /**儲存最原先ArrayList的狀態(也就是充當回複RecyclerView最原先狀態的陣列)*/
        ArrayList<String> arrayListFilter;

        public RecyclerViewAdapter(ArrayList<String> arrayList) {
            this.arrayList = arrayList;
            arrayListFilter = new ArrayList<>();
            /**這裡把初始陣列複製進去了*/
            arrayListFilter.addAll(arrayList);

        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView tvItem;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItem = itemView.findViewById(android.R.id.text1);
                /**點擊事件*/
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Toast.makeText(itemView.getContext(), arrayList.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvItem.setText(arrayList.get(position));
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }
        /**使用Filter濾除方法*/
        Filter mFilter = new Filter() {
            /**此處為正在濾除字串時所做的事*/
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                /**先將完整陣列複製過去*/
                ArrayList<String> filteredList = new ArrayList<>();
                /**如果沒有輸入，則將原本的陣列帶入*/
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(arrayListFilter);
                } else {
                    /**如果有輸入，則照順序濾除相關字串
                     * 如果你有更好的搜尋演算法，就是寫在這邊*/
                    for (String movie: arrayListFilter) {
                        if (movie.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredList.add(movie);
                        }
                    }
                }
                /**回傳濾除結果*/
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }
            /**將濾除結果推給原先RecyclerView綁定的陣列，並通知RecyclerView刷新*/
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<String> oldList = new ArrayList<>(arrayList);
                arrayList.clear();
                if (results.values instanceof Collection<?> resultsCollection) {
                    for (Object item : resultsCollection) {
                        if (item instanceof String) {
                            arrayList.add((String) item);
                        }
                    }
                }
                // Calculate the differences between oldList and arrayList
                int oldSize = oldList.size();
                int newSize = arrayList.size();
                int minSize = Math.min(oldSize, newSize);

                for (int i = 0; i < minSize; i++) {
                    if (!oldList.get(i).equals(arrayList.get(i))) {
                        notifyItemChanged(i);
                    }
                }

                if (oldSize > newSize) {
                    notifyItemRangeRemoved(newSize, oldSize - newSize);
                } else if (newSize > oldSize) {
                    notifyItemRangeInserted(oldSize, newSize - oldSize);
                }
            }
        };
    }
}