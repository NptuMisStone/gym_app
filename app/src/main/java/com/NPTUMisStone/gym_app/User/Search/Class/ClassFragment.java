package com.NPTUMisStone.gym_app.User.Search.Class;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.databinding.UserSearchFragmentClassBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClassFragment extends Fragment {
    private UserSearchFragmentClassBinding binding;
    private Connection MyConnection;
    private RecyclerViewAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = UserSearchFragmentClassBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.SearchClassBackButton.setOnClickListener(v -> requireActivity().finish());
        if (MyConnection == null) MyConnection = new SQLConnection(binding.main).IWantToConnection();
        RecyclerView recyclerView = binding.SearchClassRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<String> arrayList = new ArrayList<>();
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
        try (PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery)) {
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

    private void setSearchView() {
        binding.SearchClassSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {

        ArrayList<String> arrayList;
        ArrayList<String> arrayListFilter;

        public RecyclerViewAdapter(ArrayList<String> arrayList) {
            this.arrayList = arrayList;
            arrayListFilter = new ArrayList<>();
            arrayListFilter.addAll(arrayList);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView tvItem;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItem = itemView.findViewById(android.R.id.text1);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
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

        Filter mFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<String> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(arrayListFilter);
                } else {
                    for (String movie : arrayListFilter) {
                        if (movie.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredList.add(movie);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

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