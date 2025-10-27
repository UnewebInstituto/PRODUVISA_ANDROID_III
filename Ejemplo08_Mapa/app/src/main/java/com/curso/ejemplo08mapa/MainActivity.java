package com.curso.ejemplo08mapa;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import android.preference.PreferenceManager;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_CODE = 1;
    private MapView map;

    // Coordenadas para centrar el mapa (Caracas, Venezuela)
    private final double CARACAS_LAT = 10.4806;
    private final double CARACAS_LON = -66.9036;

    // Array de permisos que necesitamos
    private final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1. Configuración de OSMDroid (IMPORTANTE: Debe ser antes de setContentView)
        // Inicializa la configuración global para caché, User-Agent, etc.
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        // 2. Solicitar permisos de forma dinámica
        requestPermissionsIfNecessary(REQUIRED_PERMISSIONS);

        // 3. Obtener referencia al MapView
        map = findViewById(R.id.map_osm);

        // 4. Configurar el MapView
        map.setTileSource(TileSourceFactory.MAPNIK); // Usar el proveedor estándar de OSM
        map.setBuiltInZoomControls(true); // Habilitar controles de zoom
        map.setMultiTouchControls(true); // Habilitar zoom con dos dedos

        // 5. Centrar el mapa en Caracas
        GeoPoint startPoint = new GeoPoint(CARACAS_LAT, CARACAS_LON);
        map.getController().setZoom(10.0); // Nivel de zoom 10
        map.getController().setCenter(startPoint);

        // 6. Añadir un marcador
        addMarkerToMap(startPoint, "Ubicación Centrada (Caracas)");

        // 7. Añadir un overlay de brújula (opcional)
        CompassOverlay compassOverlay = new CompassOverlay(ctx, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
    }

    /**
     * Añade un marcador en un GeoPoint específico.
     */
    private void addMarkerToMap(GeoPoint point, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        // Anclar el marcador al centro inferior de su posición (para que la punta apunte a la ubicación)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        map.getOverlays().add(marker);
        map.invalidate(); // Forzar la actualización del mapa
    }

    // ---------------------- GESTIÓN DEL CICLO DE VIDA ----------------------

    @Override
    protected void onResume() {
        super.onResume();
        // Cargar tiles, refrescar el estado del mapa y reanudar la brújula, etc.
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar el mapa y guardar el estado
        map.onPause();
    }

    // ---------------------- GESTIÓN DE PERMISOS ----------------------

    /**
     * Verifica y solicita permisos necesarios en tiempo de ejecución.
     */
    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            // Solo necesitamos solicitar WRITE_EXTERNAL_STORAGE si la API es baja.
            if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                continue; // Saltar si es WRITE_EXTERNAL_STORAGE en Android 10+
            }

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            // Refrescar la configuración del mapa después de que se hayan otorgado los permisos
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
            map.invalidate();
        }
    }
}