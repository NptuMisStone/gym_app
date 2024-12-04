package com.NPTUMisStone.gym_app.User_And_Coach.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Class.ClassDetail;
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

public class ViewFragment extends androidx.fragment.app.Fragment implements OnMapReadyCallback, OnMapsSdkInitializedCallback {
    private Connection Myconnection;
    private GoogleMap map;
    private final Handler mainHandler = new Handler();
    private final LatLng[] myLocation = new LatLng[1];
    private AutoCompleteTextView editText;
    private final List<String> tagStoreName = new ArrayList<>();
    private MapLikeQueryAdapter adapter;
    private final List<Marker> salonMarkers = new ArrayList<>();
    private RecyclerView recyclerDesigner;
    private DesignersAdapter designersAdapter;
    private List<CourseItem> designerList = new ArrayList<>();
    class SalonAdd extends Thread {
        @Override
        public void run() {
            String sql = "SELECT DISTINCT 地點名稱, 課程編號 FROM [健身教練課程-有排課的]";
            try {
                Statement st = Myconnection.createStatement();
                ResultSet rs = st.executeQuery(sql);
                SalonItem.clearSalon();

                while (rs.next()) {
                    SalonItem salonItem = new SalonItem(rs.getString("地點名稱"), Redirecting.getLocationAddress(Myconnection,rs.getInt("課程編號")));
                    SalonItem.addSalon(salonItem);
                    Log.d("正在加入的Salon", salonItem.getName() + " " + salonItem.getAddress());
                }
                rs.close();
            } catch (SQLException exception) {
                Log.e("MapFragment", "Failed to add salon", exception);
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_and_coach_map_fragment, container, false);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        Objects.requireNonNull(supportMapFragment).getMapAsync(this);
        Myconnection = new SQLConnection(view).IWantToConnection();
        new SalonAdd().start();
        editText = view.findViewById(R.id.AutoCompleteTextView);
        recyclerDesigner = view.findViewById(R.id.designers);
        setupAutoCompleteTextView();

        (view.findViewById(R.id.submit)).setOnClickListener(v -> {
            String locationName = editText.getText().toString();
            if (!locationName.isEmpty()) {
                SalonItem targetSalon = findSalonByName(locationName);
                if (targetSalon != null) {
                    Geocoder geocoder = new Geocoder(requireActivity(), Locale.TRADITIONAL_CHINESE);
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(targetSalon.getAddress(), 1);
                        if (addressList != null && !addressList.isEmpty()) {
                            Address address = addressList.get(0);
                            LatLng targetLocation = new LatLng(address.getLatitude(), address.getLongitude());
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 13));

                            designerList = getDesignerList(locationName);
                            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                            recyclerDesigner.setLayoutManager(layoutManager1);
                            designersAdapter = new DesignersAdapter(designerList);
                            recyclerDesigner.setAdapter(designersAdapter);

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

        return view;
    }

    private void setupAutoCompleteTextView() {
        adapter = new MapLikeQueryAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, tagStoreName);
        editText.setAdapter(adapter);
        editText.setThreshold(0);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        // Enable zoom controls
        map.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Please enable location permission.", Toast.LENGTH_SHORT).show();
            return;
        }
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LocationServices.getFusedLocationProviderClient(requireActivity()).getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        myLocation[0] = new LatLng(location.getLatitude(), location.getLongitude());
                    } else {
                        myLocation[0] = new LatLng(25.033964, 121.564468);
                        Toast.makeText(getActivity(), "Failed to get your location.", Toast.LENGTH_SHORT).show();
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation[0], 13));
                    new Add_Salon_Marker(SalonItem.getSalonObjects()).start();
                })
                .addOnCanceledListener(() -> Toast.makeText(getActivity(), "Failed to get current last location.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        if (renderer == MapsInitializer.Renderer.LATEST) {
            Log.d("MapsDemo", "The latest version of the renderer is used.");
        } else {
            Log.d("MapsDemo", "A deprecated version of the renderer is used.");
        }
    }

    private class Add_Salon_Marker extends Thread {
        private final ArrayList<SalonItem> salonItems;

        public Add_Salon_Marker(ArrayList<SalonItem> salonItems) {
            this.salonItems = salonItems;
        }

        @SuppressLint("PotentialBehaviorOverride")  //TODO 暫時放棄處理
        @Override
        public void run() {
            Geocoder geocoder = new Geocoder(requireActivity(), Locale.TRADITIONAL_CHINESE);
            mainHandler.post(() -> Toast.makeText(getActivity(), "Salon Size: " + salonItems.size(), Toast.LENGTH_SHORT).show());
            for (int i = 0; i < salonItems.size(); i++) {
                try {
                    tagStoreName.add(salonItems.get(i).getName());

                    List<Address> address_LatLon = geocoder.getFromLocationName(salonItems.get(i).getAddress(), 2);
                    if (address_LatLon != null && !address_LatLon.isEmpty()) {
                        Address location_LatLon = address_LatLon.get(0);
                        int index = i;
                        mainHandler.post(() -> {
                            Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(location_LatLon.getLatitude(), location_LatLon.getLongitude())).title(salonItems.get(index).getName()));
                            salonMarkers.add(marker);
                            adapter.notifyDataSetChanged();
                            //TODO 改用 MarkerManager.Collection markerCollection;
                            map.setOnInfoWindowClickListener(marker1 -> {/*startActivity(new Intent(Intent.ACTION_VIEW)
                                    .setData(Uri.parse("https://www.google.com/maps/dir/?api=1&origin="
                                            + myLocation[0].latitude + "," + myLocation[0].longitude + "&destination="
                                            + marker1.getPosition().latitude + "," + marker1.getPosition().longitude)))*/
                                for (SalonItem salonItem : SalonItem.getSalonObjects()) {
                                    if (salonItem.getName().equals(marker1.getTitle())) {
                                        String address = salonItem.getAddress();
                                        Toast.makeText(getActivity(), "導航至" + marker1.getTitle(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), Direction.class).putExtra("address", address));
                                        break;
                                    }
                                }
                                //Toast.makeText(getActivity(), "導航至" + Objects.requireNonNull(marker).getTitle(), Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getActivity(), Maps.class).putExtra("address", 59));
                            });
                            map.setOnMarkerClickListener(marker2 -> {
                                recyclerDesigner.setVisibility(View.VISIBLE);
                                designerList = getDesignerList(marker2.getTitle());
                                RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                                recyclerDesigner.setLayoutManager(layoutManager1);
                                designersAdapter = new DesignersAdapter(designerList);
                                recyclerDesigner.setAdapter(designersAdapter);
                                return false;
                            });
                            map.setOnMapClickListener(latLng -> recyclerDesigner.setVisibility(View.GONE));
                        });
                    } else {
                        int finalI = i;
                        Log.e("MapFragment", "Failed to find location for address: " + salonItems.get(finalI).getAddress());
                        mainHandler.post(() -> Toast.makeText(getActivity(), "Failed to find location for address: " + salonItems.get(finalI).getAddress(), Toast.LENGTH_SHORT).show());
                        mainHandler.post(() -> Toast.makeText(getActivity(), "The " + salonItems.get(finalI).getName() + " can not display the marker.", Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    Log.e("MapFragment", "Geocoding failed", e);
                }
            }
        }
    }
    private List<CourseItem> getDesignerList(String storeName) {
        List<CourseItem> designerList = new ArrayList<>();
        String query = "SELECT * FROM [健身教練課程-有排課的] WHERE 地點名稱 = '" + storeName + "' ORDER BY 健身教練性別 DESC";
        Log.d("MapFragment", "Query: " + query); // Log the query
        try (Statement stmt = Myconnection.createStatement(); ResultSet rs1 = stmt.executeQuery(query)) {
            while (rs1.next()) {
                int designerId = rs1.getInt("課程編號");
                byte[] designerHead = rs1.getBytes("課程圖片");
                String designerName = rs1.getString("課程名稱");
                String rating = rs1.getString("健身教練性別");
                CourseItem designerItem = new CourseItem(designerId, designerHead, designerName, rating);
                designerList.add(designerItem);
            }
        } catch (SQLException e) {
            Log.e("MapFragment", "Failed to fetch designer list", e);
        }
        Log.d("MapFragment", "Designer list size: " + designerList.size()); // Log the size of the designer list
        return designerList;
    }

    private List<String> getSuggestions(String trimmedConstraint) {
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
        return suggestions;
    }

    private class MapLikeQueryAdapter extends ArrayAdapter<String> {
        private final List<String> filteredData = new ArrayList<>();
        private final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    String trimmedConstraint = constraint.toString().trim();
                    if (!trimmedConstraint.isEmpty()) {
                        List<String> suggestions = getSuggestions(trimmedConstraint);
                        results.values = suggestions;
                        results.count = suggestions.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                filteredData.clear();
                if (filterResults != null && filterResults.count > 0) {
                    if (filterResults.values instanceof List<?> resultsList) {
                        for (Object result : resultsList)
                            if (result instanceof String)
                                filteredData.add((String) result);
                    }
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        MapLikeQueryAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
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
            view.setOnClickListener(v -> {
                editText.setText(filteredData.get(position));
                editText.dismissDropDown();
                editText.clearFocus();
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editText.getWindowToken(), 0);
            });
            ((TextView) view).setText(filteredData.get(position));
            return view;
        }
    }

    private static class DesignersAdapter extends RecyclerView.Adapter<DesignersAdapter.DesignerViewHolder> {
        private final List<CourseItem> designerList;

        public DesignersAdapter(List<CourseItem> designerList) {
            this.designerList = designerList;
        }

        @NonNull
        @Override
        public DesignerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_and_coach_map_class_item, parent, false);
            return new DesignerViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull DesignerViewHolder holder, int position) {
            CourseItem item = designerList.get(position);
            byte[] imageData = item.getCourseImage();
            if (imageData != null && imageData.length > 0)
                holder.designer_head.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
            else
                holder.designer_head.setImageResource(R.drawable.all_ic_null_image_account);
            holder.designer_name.setText(item.getCoachName());
            holder.rating.setText(item.getCoachSex());
        }

        @Override
        public int getItemCount() {
            return designerList.size();
        }

        public class DesignerViewHolder extends RecyclerView.ViewHolder {
            ImageView designer_head;
            TextView designer_name, rating;

            public DesignerViewHolder(@NonNull View itemView) {
                super(itemView);
                designer_head = itemView.findViewById(R.id.designer_head);
                designer_name = itemView.findViewById(R.id.designer_name);
                rating = itemView.findViewById(R.id.textView_rating);
                itemView.setOnClickListener(view -> {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        CourseItem courseItem = designerList.get(position);
                        if (courseItem != null) {
                            Log.d("DesignersAdapter", "Course ID: " + courseItem.getCourseId());
                            Context context = itemView.getContext();
                            context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE).edit().putInt("看更多課程ID", courseItem.getCourseId()).apply();
                            context.startActivity(new Intent(context, ClassDetail.class));
                        } else {
                            Log.e("DesignersAdapter", "CourseItem is null at position: " + position);
                        }
                    } else {
                        Log.e("DesignersAdapter", "Invalid position: " + position);
                    }
                });
            }
        }
    }

    static class CourseItem {
        int courseId;
        byte[] courseImage;
        String coachName;
        String coachSex;

        public CourseItem(int courseId, byte[] courseImage, String coachName, String coachSex) {
            this.courseId = courseId;
            this.courseImage = courseImage;
            this.coachName = coachName;
            this.coachSex = coachSex;
        }

        public int getCourseId() {
            return courseId;
        }

        public byte[] getCourseImage() {
            return courseImage;
        }

        public String getCoachName() {
            return coachName;
        }

        public String getCoachSex() {
            return coachSex;
        }
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

        public static void clearSalon() {
            salonItems.clear();
        }

        public static ArrayList<SalonItem> getSalonObjects() {
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
        List<SalonItem> salonItems = SalonItem.getSalonObjects();
        if (salonItems != null) {
            for (SalonItem salonItem : salonItems) {
                if (salonItem.getName().equalsIgnoreCase(name)) {
                    return salonItem;
                }
            }
        }
        return null;
    }
    @Override
    public void onResume() {
        super.onResume();
        try {
            if (Myconnection == null || Myconnection.isClosed()) {
                Myconnection = new SQLConnection(getView()).IWantToConnection();
                new SalonAdd().start();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}