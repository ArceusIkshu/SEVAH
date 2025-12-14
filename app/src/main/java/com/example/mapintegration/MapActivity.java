package com.example.mapintegration;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_LOCATION = 2;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private static final String PLACES_API_KEY = "AIzaSyA1qKK9d5HmY51GQuAbmXC89ftCkmnZ2EI";
    private String currentLatitude = "";
    private String currentLongitude = "";
    private LocationManager locationManager;
    private DatabaseHandler myDB;

    // UI components
    private EditText searchEditText;
    private ImageButton searchButton;
    private Button sosButton, contactsButton;
    private ArrayList<Marker> hospitalMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Database
        myDB = new DatabaseHandler(this);

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize RequestQueue for API calls
        requestQueue = Volley.newRequestQueue(this);

        // Initialize UI components
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        sosButton = findViewById(R.id.danger);
        contactsButton = findViewById(R.id.button);

        // Set up search functionality
        setupSearchBar();

        // Set up SOS and Contacts buttons
        setupButtons();

        // Check and request permissions
        checkAndRequestPermissions();

        // Check if GPS is enabled
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGPS();
        }

        // Get the SupportMapFragment and request notification when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupButtons() {
        // SOS Button functionality
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                danger();
            }
        });

        // Contacts Button functionality
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Optional: close this activity when returning to contacts
            }
        });
    }

    // Rest of the MapActivity implementation remains the same as in your MainActivity
    // Copy the remaining methods from your MainActivity class

    // Add implementation for these methods:
    private void setupSearchBar() {

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLocation();
            }
        });

        // Set up keyboard action listener for search field
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchLocation();
                    return true;
                }
                return false;
            }
        });
    }

    private void checkAndRequestPermissions() {
        // Implementation as in your MainActivity
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.INTERNET);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_LOCATION);
        }
    }

    private void onGPS() {
        // Implementation as in your MainActivity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void searchLocation() {
        String searchQuery = searchEditText.getText().toString();

        if (!searchQuery.isEmpty()) {
            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocationName(searchQuery, 1);
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    LatLng searchLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                    // Clear previous markers
                    clearHospitalMarkers();

                    // Add marker for searched location
                    mMap.addMarker(new MarkerOptions()
                            .position(searchLatLng)
                            .title(address.getAddressLine(0))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    // Move camera to search location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 14));

                    // Find hospitals near this location
                    findNearbyHospitals(searchLatLng);
                } else {
                    Toast.makeText(MapActivity.this, "Location not found!", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoder error: " + e.getMessage());
                Toast.makeText(MapActivity.this, "Geocoding service not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MapActivity.this, "Please enter a location to search", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearHospitalMarkers() {
        for (Marker marker : hospitalMarkers) {
            marker.remove();
        }
        hospitalMarkers.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Get current location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLatitude = String.valueOf(location.getLatitude());
                                currentLongitude = String.valueOf(location.getLongitude());
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));

                                // Find nearby hospitals for initial location
                                findNearbyHospitals(currentLatLng);
                            } else {
                                // Fallback to default location if current location is not available
                                LatLng sydney = new LatLng(-34, 151);
                                mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));

                                Toast.makeText(MapActivity.this,
                                        "Location not available. Using default location.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void findNearbyHospitals(LatLng location) {
        // Show loading indicator
        Toast.makeText(MapActivity.this, "Searching for hospitals...", Toast.LENGTH_SHORT).show();

        // Places API request URL
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + location.latitude + "," + location.longitude +
                "&radius=5000" + // 5km radius
                "&type=hospital" +
                "&key=" + PLACES_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject place = results.getJSONObject(i);
                                JSONObject geometry = place.getJSONObject("geometry");
                                JSONObject locationObj = geometry.getJSONObject("location");

                                double lat = locationObj.getDouble("lat");
                                double lng = locationObj.getDouble("lng");
                                String name = place.getString("name");
                                String vicinity = place.getString("vicinity");

                                LatLng hospitalLatLng = new LatLng(lat, lng);
                                Marker hospitalMarker = mMap.addMarker(new MarkerOptions()
                                        .position(hospitalLatLng)
                                        .title(name)
                                        .snippet(vicinity)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                // Add to the list to track markers
                                if (hospitalMarker != null) {
                                    hospitalMarkers.add(hospitalMarker);
                                }
                            }
                            Toast.makeText(MapActivity.this,
                                    "Found " + results.length() + " hospitals nearby",
                                    Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing Places API response", e);
                            Toast.makeText(MapActivity.this,
                                    "Error finding hospitals",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error making Places API request", error);
                        Toast.makeText(MapActivity.this,
                                "Error connecting to Places API",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE || requestCode == REQUEST_LOCATION) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, permissions[i] + " granted");
                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        enableMyLocation();
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        Toast.makeText(this, "Permission required for proper functionality", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permission permanently denied. Enable in settings.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    // SOS Functionality
    private void danger() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Update current location before sending SOS
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLatitude = String.valueOf(location.getLatitude());
                        currentLongitude = String.valueOf(location.getLongitude());
                        loadData();
                    } else {
                        Toast.makeText(MapActivity.this, "Cannot get current location for SOS", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Required permissions not granted for SOS", Toast.LENGTH_SHORT).show();
            checkAndRequestPermissions();
        }
    }

    private void loadData() {
        ArrayList<String> theList = new ArrayList<>();
        Cursor data = myDB.getListContents();

        if (data.getCount() == 0) {
            Toast.makeText(MapActivity.this, "No emergency contacts to show. Add contacts first.", Toast.LENGTH_SHORT).show();
            // Direct user to add contacts
//            Intent intent = new Intent(getApplicationContext(), Register.class);
//            startActivity(intent);
        } else {
            String msg = "I need help! My location: https://www.google.com/maps?q=" + currentLatitude + "," + currentLongitude;
            String number = "";

            while (data.moveToNext()) {
                theList.add(data.getString(1));
                number = number + data.getString(1) + (data.isLast() ? "" : ";");
            }

            if (!theList.isEmpty()) {
                Log.d(TAG, "Numbers to contact: " + number);
                call(theList.get(0)); // Call the first number
                sendSMS(number, msg);
            }
        }
    }

    private void call(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSMS(String number, String msg) {
        SmsManager sms = SmsManager.getDefault();
        String[] numbers = number.split(";");

        for (String contact : numbers) {
            sms.sendTextMessage(contact, null, msg, null, null);
            Toast.makeText(this, "SOS message sent to " + contact, Toast.LENGTH_SHORT).show();
        }
    }

    // All remaining methods from MainActivity
}