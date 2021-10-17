package ai.nextbillion;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nbmap.nbmapsdk.annotations.Marker;
import com.nbmap.nbmapsdk.annotations.MarkerOptions;
import com.nbmap.nbmapsdk.camera.CameraPosition;
import com.nbmap.nbmapsdk.camera.CameraUpdateFactory;
import com.nbmap.nbmapsdk.geometry.LatLng;
import com.nbmap.nbmapsdk.maps.MapView;
import com.nbmap.nbmapsdk.maps.NbmapMap;
import com.nbmap.nbmapsdk.maps.OnMapReadyCallback;
import com.nbmap.nbmapsdk.maps.Style;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GeocodeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NbmapMap mMap;
    private EditText editText = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocode);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        editText = (EditText) findViewById(R.id.query);

        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    fwdGeocode(editText.getText().toString());
                    editText.setText("");
                    return true;
                }
                return false;
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    @Override
    public void onMapReady(@NonNull NbmapMap nbmapMap) {
        mMap = nbmapMap;

        mMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                addMarker();
            }
        });

        mMap.addOnMapClickListener(new NbmapMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng latLng)  {
                // reverse geocode
                revGeocode(latLng);
                return true;
            }
        });
    }


    private void fwdGeocode(@NonNull String query) {
        OkHttpClient client2 = new OkHttpClient();
        JSONArray items = null;
        Request request = new Request.Builder()
                .url("https://api.nextbillion.io/h/geocode?q="+
                        query +
                        "&limit=1&key=" + BuildConfig.NBAI_API_KEY)
                .build();
        try (Response response = client2.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            Headers responseHeaders = response.headers();
            ResponseBody responseBody = response.body();
            try {
                String sResponse = responseBody.string();
                JSONObject jobj = new JSONObject(sResponse);
                items = jobj.getJSONArray("items");
                System.out.println(items.getJSONObject(0).getString("title"));
            } catch (JSONException | IOException e) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            JSONObject position = items.getJSONObject(0).getJSONObject("position");
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(position.getString("lat")),
                    Double.valueOf(position.getString("lng"))))
                    .snippet(String.format("%s", items.getJSONObject(0).getString("title"))));

            CameraPosition camposition = (new CameraPosition.Builder()).target(new LatLng(Double.valueOf(position.getString("lat")), Double.valueOf(position.getString("lng")))).zoom(16.0D).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camposition), 600);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void revGeocode(@NonNull LatLng latLng) {
        OkHttpClient client2 = new OkHttpClient();
        JSONArray items = null;
        Request request = new Request.Builder()
                .url("https://api.nextbillion.io/h/revgeocode?at="+
                        String.valueOf(latLng.getLatitude()) + "," + String.valueOf(latLng.getLongitude()) +
                        "&limit=1&key=727f7df8765b4433b2194cd023879aee")
                .build();
        try (Response response = client2.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            Headers responseHeaders = response.headers();
            ResponseBody responseBody = response.body();
            try {
                String sResponse = responseBody.string();
                JSONObject jobj = new JSONObject(sResponse);
                items = jobj.getJSONArray("items");
                System.out.println(items.getJSONObject(0).getString("title"));
            } catch (JSONException | IOException e) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .snippet(String.format("%s", items.getJSONObject(0).getString("title"))));
            //marker.showInfoWindow(mMap, mapView);
            TextView tvId = (TextView) findViewById(R.id.addr);
            tvId.setText(items.getJSONObject(0).getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private void addMarker() {
        //Marker marker = mMap.addMarker(new LatLng(34.038058398414215, -118.26978793837996 ));
        //mMap.removeMarker(marker);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(34.038058398414215, -118.26978793837996)).title("Title"));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(34.038058398414215, -118.268)).snippet("Snippet"));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
