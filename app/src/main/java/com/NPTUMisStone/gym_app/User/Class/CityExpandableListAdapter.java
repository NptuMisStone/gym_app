package com.NPTUMisStone.gym_app.User.Class;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.NPTUMisStone.gym_app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CityExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> parentList;
    private HashMap<String, List<String>> childMap;
    private HashMap<String, Boolean> groupCheckState; // 保存父節點的 CheckBox 狀態
    private HashMap<String, HashMap<String, Boolean>> childCheckState; // 保存子節點的 CheckBox 狀態

    private ArrayList<String> selectedCities; // 選中的縣市
    private ArrayList<String> selectedAreas;  // 選中的行政區

    public CityExpandableListAdapter(Context context, List<String> parentList, HashMap<String, List<String>> childMap) {
        this.context = context;
        this.parentList = parentList;
        this.childMap = childMap;

        // 初始化狀態保存
        groupCheckState = new HashMap<>();
        childCheckState = new HashMap<>();
        selectedCities = new ArrayList<>();
        selectedAreas = new ArrayList<>();

        for (String group : parentList) {
            groupCheckState.put(group, false);
            HashMap<String, Boolean> childStates = new HashMap<>();
            for (String child : childMap.get(group)) {
                childStates.put(child, false);
            }
            childCheckState.put(group, childStates);
        }
    }

    // Getter for selected cities and areas
    public ArrayList<String> getSelectedCities() {
        return selectedCities;
    }

    public ArrayList<String> getSelectedAreas() {
        return selectedAreas;
    }

    @Override
    public int getGroupCount() {
        return parentList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childMap.get(parentList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return parentList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childMap.get(parentList.get(groupPosition)).get(childPosition);
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
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String cityName = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expandable_list_group, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.group_name);
        CheckBox checkBox = convertView.findViewById(R.id.group_checkbox);
        ImageView indicator = convertView.findViewById(R.id.group_indicator);

        textView.setText(cityName);

        // 下拉箭頭的旋轉效果
        if (isExpanded) {
            indicator.setRotation(180);
        } else {
            indicator.setRotation(0);
        }

        // 設置 CheckBox 的狀態
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(groupCheckState.get(cityName));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            groupCheckState.put(cityName, isChecked);

            if (isChecked) {
                if (!selectedCities.contains(cityName)) {
                    selectedCities.add(cityName);
                }
            } else {
                selectedCities.remove(cityName);
            }

            // 同步子節點的狀態
            HashMap<String, Boolean> childStates = childCheckState.get(cityName);
            if (childStates != null) {
                for (String key : childStates.keySet()) {
                    childStates.put(key, isChecked);

                    if (isChecked) {
                        if (!selectedAreas.contains(key)) {
                            selectedAreas.add(key);
                        }
                    } else {
                        selectedAreas.remove(key);
                    }
                }
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String areaName = (String) getChild(groupPosition, childPosition);
        String groupName = (String) getGroup(groupPosition);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expandable_list_child, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.child_name);
        CheckBox checkBox = convertView.findViewById(R.id.child_checkbox);

        textView.setText(areaName);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(childCheckState.get(groupName).get(areaName));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            childCheckState.get(groupName).put(areaName, isChecked);

            if (isChecked) {
                if (!selectedAreas.contains(areaName)) {
                    selectedAreas.add(areaName);
                }
            } else {
                selectedAreas.remove(areaName);
            }
        });

        return convertView;
    }
    public void resetCheckStates() {
        // 重置父節點的勾選狀態
        for (String group : groupCheckState.keySet()) {
            groupCheckState.put(group, false);
        }

        // 重置子節點的勾選狀態
        for (String group : childCheckState.keySet()) {
            HashMap<String, Boolean> childStates = childCheckState.get(group);
            if (childStates != null) {
                for (String child : childStates.keySet()) {
                    childStates.put(child, false);
                }
            }
        }

        // 清空已選擇的城市和行政區
        selectedCities.clear();
        selectedAreas.clear();

        // 通知數據已改變，更新 UI
        notifyDataSetChanged();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

