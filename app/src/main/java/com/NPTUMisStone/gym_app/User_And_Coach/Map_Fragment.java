package com.NPTUMisStone.gym_app.User_And_Coach;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Class.ClassDetail;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Map_Fragment extends Fragment implements OnMapReadyCallback, OnMapsSdkInitializedCallback {
    Connection Myconnection;
    private GoogleMap map;
    Handler mainHandler = new Handler();
    LatLng[] myLocation = new LatLng[1];
    private AutoCompleteTextView editText;
    Button submit;
    List<String> tagStoreName = new ArrayList<>();
    private MapLikeQueryAdapter adapter;
    List<Marker> salonMarkers = new ArrayList<>();
    private RecyclerView recyclerDesigner;
    private DesignersAdapter designersAdapter;
    private List<DeItem> designerList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        MapsInitializer.initialize(getContext(), MapsInitializer.Renderer.LEGACY, this);
        View view = inflater.inflate(R.layout.user_and_coach_map_fragment, container, false);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        Objects.requireNonNull(supportMapFragment).getMapAsync(this);
        Myconnection = new SQLConnection(view).IWantToConnection();

        editText = view.findViewById(R.id.AutoCompleteTextView);
        submit = view.findViewById(R.id.submit);
        recyclerDesigner = view.findViewById(R.id.designers);
        setupAutoCompleteTextView();
//        MapsInitializer.initialize(getContext(), MapsInitializer.Renderer.LATEST, (OnMapsSdkInitializedCallback) this);
        submit.setOnClickListener(v -> {
            String locationName = editText.getText().toString();
            if (!locationName.isEmpty()) {
                // 從店家去找地址
                SalonItem targetSalon = findSalonByName(locationName);

                if (targetSalon != null) {
                    Geocoder geocoder = new Geocoder(requireActivity(), Locale.TRADITIONAL_CHINESE);
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(targetSalon.getAddress(), 1);
                        if (addressList != null && !addressList.isEmpty()) {
                            Address address = addressList.get(0);
                            LatLng targetLocation = new LatLng(address.getLatitude(), address.getLongitude());

                            // 移動地圖
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 13));

                            designerList = getDesignerList(locationName);
                            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                            Log.e("designerList", String.valueOf(designerList.size()));
                            recyclerDesigner.setLayoutManager(layoutManager1);
                            designersAdapter = new DesignersAdapter(designerList);
                            recyclerDesigner.setAdapter(designersAdapter);

                            // 顯示marker
                            for (Marker marker : salonMarkers) {
                                if (marker.getPosition().equals(targetLocation)) {
                                    marker.showInfoWindow();
                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(getActivity(), "抱歉，抓不到該地址，請回報讓我們知道！非常感謝您", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e("MapFragment", "Geocoding failed", e);
                    }
                } else {
                    Toast.makeText(getActivity(), "抱歉，找不到相應的理髮店", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*startOtherMap.setOnClickListener {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + myLocation[0] +"&destination=22.5428600000,114.0595600000"));
            startActivity(intent);
        }*/

        return view;
    }

    static class SalonItem {
        String name;
        String address;
        private static ArrayList<SalonItem> salonItems = new ArrayList<>();

        public SalonItem(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public static void addSalon(SalonItem salonItem) {
            salonItems.add(salonItem);
            setSalon(salonItems);
        }

        // SalonItem
        public static void clearSalon() {
            salonItems.clear();
        }

        public static ArrayList<SalonItem> getSalonObjects() {
            if (salonItems == null) {
                return null;
            }
            return salonItems;
        }

        private static void setSalon(ArrayList<SalonItem> salonItems) {
            SalonItem.salonItems = salonItems;
        }

        public String getAddress() {
            return address != null ? address : "";
        }

        public String getName() {
            return name != null ? name : "";
        }

    }

    private SalonItem findSalonByName(String name) {
        for (SalonItem salonItem : Objects.requireNonNull(SalonItem.getSalonObjects())) {
            if (salonItem.getName().equalsIgnoreCase(name)) {
                return salonItem;
            }
        }
        return null;
    }

    private void setupAutoCompleteTextView() {
        // 設置 AutoCompleteTextView 的適配器
        adapter = new MapLikeQueryAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, tagStoreName);
        editText.setAdapter(adapter);
        // 設定建議的最小字符數
        editText.setThreshold(0);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Get Last Location,and catch the success or failed event.
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        myLocation[0] = new LatLng(location.getLatitude(), location.getLongitude());
                        //In thw same time，move camera to the current location.
                    } else {
                        //Taipei 101 LatLng
                        myLocation[0] = new LatLng(25.033964, 121.564468);
                        Toast.makeText(getActivity(), "Failed to get you location.", Toast.LENGTH_SHORT).show();
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation[0], 13));
                    new Add_Salon_Marker(SalonItem.getSalonObjects()).start();

                })
                .addOnCanceledListener(() -> Toast.makeText(getActivity(), "Failed to get currnt last location.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d("MapsDemo", "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d("MapsDemo", "The legacy version of the renderer is used.");
                break;
        }
    }

    class Add_Salon_Marker extends Thread {

         ArrayList<SalonItem> salonItems;

        public Add_Salon_Marker(ArrayList<SalonItem> salonItems) {
            this.salonItems = salonItems;
        }

        @Override
        public void run() {
            Geocoder geocoder = new Geocoder(requireActivity(), Locale.TRADITIONAL_CHINESE);
            for (int i = 0; i < salonItems.size(); i++) {
                try {
//                    Log.e("Add_Salon_Marker", salonItems.get(i).getAddress());
                    tagStoreName.add(salonItems.get(i).getName());
                    List<Address> address_LatLon = geocoder.getFromLocationName(salonItems.get(i).getAddress(), 2);
                    int Index = i;
                    try {
                        Address location_LatLon = Objects.requireNonNull(address_LatLon).get(0);
                        mainHandler.post(() -> {
                            Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(location_LatLon.getLatitude(),
                                    location_LatLon.getLongitude())).title(salonItems.get(Index).getName()));
                            salonMarkers.add(marker);
                            adapter.notifyDataSetChanged();
                            // 當標記訊息視窗被點擊時會呼叫此方法
                            // 點擊infoWindow
                            map.setOnInfoWindowClickListener(marker1 -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + myLocation[0].latitude + "," +
                                        myLocation[0].longitude + "&destination=" + marker1.getPosition().latitude + "," + marker1.getPosition().longitude));
                                startActivity(intent);
                            });
                            // 當標記被點擊時會呼叫此方法
                            map.setOnMarkerClickListener(marker2 -> {
                                recyclerDesigner.setVisibility(View.VISIBLE);
                                designerList = getDesignerList(marker2.getTitle());
                                RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                                Log.e("designerList", String.valueOf(designerList.size()));
                                recyclerDesigner.setLayoutManager(layoutManager1);
                                designersAdapter = new DesignersAdapter(designerList);
                                recyclerDesigner.setAdapter(designersAdapter);
                                return false;
                            });
                            map.setOnMapClickListener(latLng -> recyclerDesigner.setVisibility(View.GONE));
                        });
                    } catch (Exception e) {
                        mainHandler.post(() -> Toast.makeText(getActivity(), "The " + salonItems.get(Index).getName() +
                                " can not display the marker.", Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    Log.e("MapFragment", "Geocoding failed", e);
                }
            }
        }
    }

    private List<DeItem> getDesignerList(String storeName) {
        List<DeItem> designerList = new ArrayList<>();
        try {
            String query = "SELECT * FROM filter_designer " +
                    "WHERE 服務店家 = '" + storeName + "' " +
                    "ORDER BY 評論數量 DESC";
            Statement stmt = Myconnection.createStatement();
            ResultSet rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                int DesignerId = rs1.getInt("設計師編號");
                byte[] DesignerHead = rs1.getBytes("大頭貼");
                String DesignerName = rs1.getString("姓名");
                String rating = rs1.getString("平均評分");
                DeItem designerItem = new DeItem(DesignerId, DesignerHead, DesignerName, rating);
                designerList.add(designerItem);
            }
            stmt.close();
            rs1.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return designerList;
    }

    private class MapLikeQueryAdapter extends ArrayAdapter<String> {
        List<String> originalData;
        List<String> filteredData = new ArrayList<>();
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    // Trim the input to remove leading and trailing spaces
                    String trimmedConstraint = constraint.toString().trim();

                    // Check if the trimmed input is not empty
                    if (!trimmedConstraint.isEmpty()) {
                        List<String> suggestions = new ArrayList<>();
                        for (String item : tagStoreName) {
                            if (item != null && !item.isEmpty()) {
                                String[] words = trimmedConstraint.toLowerCase().split(" ");
                                boolean allWordsFound = true;
                                for (String word : words) {
                                    if (!item.toLowerCase().contains(word)) {
                                        allWordsFound = false;
                                        break;
                                    }
                                }
                                if (allWordsFound) {
                                    suggestions.add(item);
                                }
                            }
                        }

                        results.values = suggestions;
                        results.count = suggestions.size();
                        Log.e("originalData", originalData.toString());
                    }
                }
                return results;
            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                filteredData.clear();
                if (filterResults != null && filterResults.count > 0) {
                    filteredData.addAll((List<String>) filterResults.values);
                    notifyDataSetChanged();
                    Log.e("filteredData", filteredData.toString());
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        MapLikeQueryAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            this.originalData = objects;
        }

        @Override
        public int getCount() {
            return filteredData.size();
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return filter;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            // 添加点击事件监听器
            view.setOnClickListener(v -> {
                editText.setText(filteredData.get(position));
                //關閉提示窗、游標
                editText.dismissDropDown();
                editText.clearFocus();
                // 關閉鍵盤
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            });


            ((TextView) view).setText(filteredData.get(position));
            return view;
        }
    }

    private static class DesignersAdapter extends RecyclerView.Adapter<DesignersAdapter.DesignerViewHolder> {
         List<DeItem> designerList;

        public DesignersAdapter(List<DeItem> designerList) {
            this.designerList = designerList;
        }

        @NonNull
        @Override
        public DesignerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_and_coach_map_item_salon_designer, parent, false);
            return new DesignerViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull DesignerViewHolder holder, int position) {
            DeItem item = designerList.get(position);

            // Designer Head
            byte[] imageData = item.getDesignerHead();
            if (imageData != null && imageData.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                holder.designer_head.setImageBitmap(bitmap);
            } else {
                holder.designer_head.setImageResource(R.drawable.all_ic_null_image_account);
            }
            holder.designer_name.setText(item.getDesignerName());
            holder.rating.setText(item.getRating());
        }

        @Override
        public int getItemCount() {
            return designerList.size();
        }

        public class DesignerViewHolder extends RecyclerView.ViewHolder {
            ImageView designer_head;
            TextView designer_name;
            TextView rating;

            public DesignerViewHolder(@NonNull View itemView) {
                super(itemView);
                designer_head = itemView.findViewById(R.id.designer_head);
                designer_name = itemView.findViewById(R.id.designer_name);
                rating = itemView.findViewById(R.id.textView_rating);

                itemView.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        DeItem item = designerList.get(position);
                        Intent intent = new Intent(itemView.getContext(), ClassDetail.class);
                        intent.putExtra("designer_id", item.getDesignerId());
                        itemView.getContext().startActivity(intent);
                    }
                });
            }

        }
    }

    private static class DeItem {
        int DesignerId;
        byte[] DesignerHead;
        String DesignerName;
        String rating;

        private DeItem(int DesignerId, byte[] DesignerHead, String DesignerName, String rating) {
            this.DesignerId = DesignerId;
            this.DesignerHead = DesignerHead;
            this.DesignerName = DesignerName;
            this.rating = rating;
        }

        private int getDesignerId() {
            return DesignerId;
        }

        private byte[] getDesignerHead() {
            return DesignerHead;
        }

        private String getDesignerName() {
            return DesignerName;
        }

        private String getRating() {
            return rating;
        }
    }
}