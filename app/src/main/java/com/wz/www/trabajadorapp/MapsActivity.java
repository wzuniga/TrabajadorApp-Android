package com.wz.www.trabajadorapp;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Button btn_update;
    HashMap<String, String> hm = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        btn_update = (Button) findViewById(R.id.updateMarkers);
        btn_update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Actualizando" , Toast.LENGTH_LONG).show();
                BufferedReader in = null;
                StringBuffer sb = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    request.setURI(new URI("https://cityclear.herokuapp.com/reportEmployee/" + getIntent().getExtras().getString("usuario")));
                    HttpResponse response = client.execute(request);
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    sb = new StringBuffer("");
                    String line = "";
                    String NL = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line + NL);
                    }
                    in.close();
                }catch (Exception e){

                }

                String tipo = "", com = "", lat = "", lon = "", _id = "";
                try {
                    JSONArray array = new JSONArray(sb.toString());

                    for(int i = 0; i < array.length(); i++){
                        JSONObject jsonObj = array.getJSONObject(i);
                        _id = jsonObj.getString("_id");
                        tipo = jsonObj.getString("tipoReporte");
                        com = jsonObj.getString("comentario");
                        lat = jsonObj.getString("latitud");
                        lon = jsonObj.getString("longitud");

                        LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(location);
                        marker.title("Problema de " + tipo);
                        marker.snippet("Comentario: \n" + com);

                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                        mMap.addMarker(marker);
                        hm.put(_id, "Comentario: \n" + com);

                        String CHANNEL_ID = "my_channel_01";
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                                        .setContentTitle(tipo)
                                        .setContentText(com);
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(i, mBuilder.build());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Map-GET", "********"+sb.toString());


            }

        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d("Map-GET", "********");
                Toast.makeText(getApplicationContext(), "Actualizando" , Toast.LENGTH_LONG).show();
                Log.d("Map-GET", marker.getId());
                String key = "";
                for(String str:hm.keySet()){
                    if (hm.get(str).equals(marker.getSnippet())){
                        key = str;
                    }
                }

                BufferedReader in = null;
                StringBuffer sb = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPut request = new HttpPut();
                    request.setURI(new URI("https://cityclear.herokuapp.com/report/" + key));
                    List<NameValuePair> postParameters = new ArrayList<NameValuePair>(1);
                    postParameters.add(new BasicNameValuePair("status", "completed"));
                    request.setEntity(new UrlEncodedFormEntity(postParameters));
                    HttpResponse response = client.execute(request);
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    sb = new StringBuffer("");
                    String line = "";
                    String NL = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line + NL);
                    }
                    in.close();
                }catch (Exception e){

                }
                Log.d("Map-GET", "°°°°°°°°°°°°°°°°°°°°°°°°°°°"+sb.toString());
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                Button succ = new Button(getApplicationContext());
                succ.setText("Finalizar");
                succ.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                //image.setImageBitmap();

                info.addView(title);
                info.addView(snippet);
                info.addView(succ);

                return info;
            }
        });
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Tu estas Aquí"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Toast.makeText(getApplicationContext(), "Actualizando" + (latitude), Toast.LENGTH_LONG).show();
        mMap.setMyLocationEnabled(true);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
