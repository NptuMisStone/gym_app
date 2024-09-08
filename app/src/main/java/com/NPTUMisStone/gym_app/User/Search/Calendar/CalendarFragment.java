package com.NPTUMisStone.gym_app.User.Search.Calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.Main.Initial.SnackBarUtils;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.databinding.UserSearchFragmentCalendarBinding;
import com.hdev.calendar.bean.DateInfo;
import com.hdev.calendar.view.SingleCalendarView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private UserSearchFragmentCalendarBinding binding;
    private Connection MyConnection;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CalendarViewModel calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);
        binding = UserSearchFragmentCalendarBinding.inflate(inflater, container, false);
        MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
        binding.SearchCalendarBackButton.setOnClickListener(v -> requireActivity().finish());
        setupCalendarView();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupCalendarView() {
        DateInfo today = new DateInfo(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        loadSchedule(today);
        SingleCalendarView singleCalendarView = binding.SearchCalendarCalendarView;
        singleCalendarView.setOnSingleDateSelectedListener((calendar, date) -> {
            loadSchedule(date);
            return null;
        });
        List<DateInfo> dateList = fetchDatesFromDB();
        if (!dateList.isEmpty()) {
            singleCalendarView.setClickableDateList(dateList);
            singleCalendarView.setDateRange(dateList.get(0).timeInMillis(), dateList.get(dateList.size() - 1).timeInMillis());
        } else {
            singleCalendarView.setClickableDateList(new ArrayList<>());
            singleCalendarView.setDateRange(Calendar.getInstance().getTimeInMillis(), Calendar.getInstance().getTimeInMillis());
            SnackBarUtils.makeShort(binding.getRoot(), "無課程").danger();
        }
    }

    private List<DateInfo> fetchDatesFromDB() {
        List<DateInfo> dateList = new ArrayList<>();
        String searchQuery = "SELECT [健身教練課表].日期 FROM [健身教練課表] JOIN [健身教練課程] ON [健身教練課程].課程編號 = [健身教練課表].課程編號";
        try (PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery)) {
            ResultSet resultSet = searchStatement.executeQuery();
            while (resultSet.next()) {
                String[] date = resultSet.getString("日期").split("-");
                dateList.add(new DateInfo(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2])));
            }
        } catch (SQLException e) {
            Log.e("CalendarFragment", "Error loading tasks from DB", e);
        }
        return dateList;
    }

    private void loadSchedule(DateInfo date) {
        RecyclerView recyclerView = binding.SearchCalendarRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        CustomAdapter adapter = new CustomAdapter(new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        fetchScheduleFromDB(date, adapter);
    }

    private void fetchScheduleFromDB(DateInfo date, CustomAdapter adapter) {
        String searchQuery = "SELECT [健身教練課程].課程編號,[健身教練課程].課程名稱 FROM [健身教練課表] JOIN [健身教練課程] ON [健身教練課程].課程編號 = [健身教練課表].課程編號 WHERE [健身教練課表].日期 = ?";
        try (PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery)) {
            searchStatement.setString(1, date.getYear() + "/" + date.getMonth() + "/" + date.getDay());
            ResultSet resultSet = searchStatement.executeQuery();
            while (resultSet.next()) {
                adapter.idList.add(resultSet.getInt("課程編號"));
                adapter.nameList.add(resultSet.getString("課程名稱"));
            }
        } catch (SQLException e) {
            Log.e("CalendarFragment", "Error loading tasks from DB", e);
        }
    }

    static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        List<Integer> idList;
        List<String> nameList;

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView imageView;

            ViewHolder(View view) {
                super(view);
                textView = view.findViewById(R.id.CheckItem_checkedText);
                imageView = view.findViewById(R.id.CheckItem_chooseImage);
            }

            TextView getTextView() {
                return textView;
            }
        }

        CustomAdapter(ArrayList<Integer> ids, ArrayList<String> names) {
            idList = ids;
            nameList = names;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_search_fragment_calendar_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.getTextView().setText(nameList.get(position));
        }

        @Override
        public int getItemCount() {
            return nameList.size();
        }
    }
}