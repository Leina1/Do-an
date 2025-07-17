package com.example.apptradeup.Fragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.apptradeup.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class PickLocationFragment extends Fragment {
    private MapView map;
    private EditText edtSearch;
    private Button btnSearch, btnSelect;
    private GeoPoint pickedPoint = null;
    private Marker marker;

    public PickLocationFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        // Cho phép network trên main thread (DEMO)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        map = view.findViewById(R.id.map);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnSelect = view.findViewById(R.id.btnSelectLocation);

        map.setMultiTouchControls(true);
        map.getController().setZoom(16.0);
        map.getController().setCenter(new GeoPoint(10.762622, 106.660172)); // Trung tâm TP.HCM

        // Sự kiện click map
        MapEventsOverlay overlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                setMarkerAt(p);
                String reverseAddress = reverseGeocode(p);
                edtSearch.setText(reverseAddress != null ? reverseAddress : "Lat: " + p.getLatitude() + ", Lng: " + p.getLongitude());
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        map.getOverlays().add(overlay);

        // Tìm địa chỉ
        btnSearch.setOnClickListener(v -> {
            String address = edtSearch.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(getContext(), "Nhập địa chỉ để tìm kiếm", Toast.LENGTH_SHORT).show();
                return;
            }

            GeoPoint geo = searchLocation(address);
            if (geo != null) {
                map.getController().setZoom(18.0);
                map.getController().setCenter(geo);
                setMarkerAt(geo);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy địa chỉ!", Toast.LENGTH_SHORT).show();
            }
        });

        // Gửi kết quả
        btnSelect.setOnClickListener(v -> {
            if (pickedPoint != null) {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                String address = "Lat: " + pickedPoint.getLatitude() + ", Lng: " + pickedPoint.getLongitude();

                try {
                    List<Address> addresses = geocoder.getFromLocation(pickedPoint.getLatitude(), pickedPoint.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        address = addresses.get(0).getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bundle result = new Bundle();
                result.putDouble("lat", pickedPoint.getLatitude());
                result.putDouble("lng", pickedPoint.getLongitude());
                result.putString("address", address);

                getParentFragmentManager().setFragmentResult("location_result", result);
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Hãy chọn vị trí trên bản đồ", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void setMarkerAt(GeoPoint point) {
        if (marker != null) map.getOverlays().remove(marker);

        marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Vị trí đã chọn");
        map.getOverlays().add(marker);
        map.invalidate();
        pickedPoint = point;
    }

    private GeoPoint searchLocation(String address) {
        try {
            String urlStr = "https://nominatim.openstreetmap.org/search?format=json&q=" + address.replace(" ", "%20");
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONArray arr = new JSONArray(response.toString());
            if (arr.length() > 0) {
                JSONObject obj = arr.getJSONObject(0);
                double lat = obj.getDouble("lat");
                double lon = obj.getDouble("lon");
                return new GeoPoint(lat, lon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String reverseGeocode(GeoPoint point) {
        try {
            String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" +
                    point.getLatitude() + "&lon=" + point.getLongitude();
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONObject obj = new JSONObject(response.toString());
            return obj.getString("display_name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
