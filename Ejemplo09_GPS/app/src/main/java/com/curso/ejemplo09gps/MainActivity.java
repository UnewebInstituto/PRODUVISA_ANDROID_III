package com.curso.ejemplo09gps;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Map;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GPSHandler";
    private TextView textStatus, textLatitude, textLongitude;
    private Button btnToggleUpdates;

    // Clases principales de la API de ubicación
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private boolean requestingLocationUpdates = false;

    // Permisos requeridos
    private final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private ActivityResultLauncher<String[]> requestPermissionsLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicialización de Views
        textStatus = findViewById(R.id.text_status);
        textLatitude = findViewById(R.id.text_latitude);
        textLongitude = findViewById(R.id.text_longitude);
        btnToggleUpdates = findViewById(R.id.btn_toggle_updates);

        // Inicialización del cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 1. Configurar el LocationRequest (Define cómo queremos las actualizaciones)
        createLocationRequest();

        // 2. Configurar el LocationCallback (Maneja las actualizaciones recibidas)
        createLocationCallback();

        // 3. Configurar el Launcher de Permisos
        setupPermissionLauncher();

        // 4. Configurar el botón
        btnToggleUpdates.setOnClickListener(v -> toggleLocationUpdates());

        // 5. Verificar permisos al inicio
        checkPermissionsAndSetup();

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
    }

    // ---------------------- CONFIGURACIÓN DE UBICACIÓN ----------------------

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // Alta precisión, intervalo de 5 segundos
                .setWaitForAccurateLocation(true) // Esperar hasta que haya una ubicación precisa
                .setMinUpdateIntervalMillis(3000) // Intervalo mínimo de 3 segundos
                .setMaxUpdateAgeMillis(10000) // No usar ubicaciones con más de 10 segundos
                .build();
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // Manejar cada nueva ubicación
                for (Location location : locationResult.getLocations()) {
                    updateUI(location);
                }
            }
        };
    }

    // ---------------------- GESTIÓN DE PERMISOS ----------------------

    private void setupPermissionLauncher() {
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if (fineLocationGranted || coarseLocationGranted) {
                        // Permiso concedido
                        textStatus.setText("Estado: Permiso Concedido. Listo.");
                        btnToggleUpdates.setEnabled(true);
                        Toast.makeText(this, "Permisos de ubicación concedidos. ✅", Toast.LENGTH_SHORT).show();
                    } else {
                        // Permiso denegado
                        textStatus.setText("Estado: Permiso Denegado.");
                        btnToggleUpdates.setEnabled(false);
                        btnToggleUpdates.setText("Permiso Requerido");
                        Toast.makeText(this, "Permiso de ubicación denegado. ❌", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private boolean checkLocationPermissions() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return fine || coarse;
    }

    private void checkPermissionsAndSetup() {
        if (checkLocationPermissions()) {
            textStatus.setText("Estado: Permiso Concedido. Listo.");
            btnToggleUpdates.setEnabled(true);
        } else {
            requestPermissionsLauncher.launch(LOCATION_PERMISSIONS);
        }
    }

    // ---------------------- INICIO/PARADA DE ACTUALIZACIONES ----------------------

    private void toggleLocationUpdates() {
        if (requestingLocationUpdates) {
            stopLocationUpdates();
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (!checkLocationPermissions()) {
            Toast.makeText(this, "Debe conceder permisos de ubicación.", Toast.LENGTH_SHORT).show();
            requestPermissionsLauncher.launch(LOCATION_PERMISSIONS);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper())
                .addOnSuccessListener(aVoid -> {
                    requestingLocationUpdates = true;
                    btnToggleUpdates.setText("Detener Monitoreo GPS");
                    textStatus.setText("Estado: Monitoreando...");
                    Log.d(TAG, "Monitoreo iniciado.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al iniciar monitoreo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al iniciar monitoreo", e);
                });
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnSuccessListener(aVoid -> {
                    requestingLocationUpdates = false;
                    btnToggleUpdates.setText("Iniciar Monitoreo GPS");
                    textStatus.setText("Estado: Monitoreo Detenido.");
                    Log.d(TAG, "Monitoreo detenido.");
                });
    }

    // ---------------------- ACTUALIZACIÓN DE LA INTERFAZ ----------------------

    private void updateUI(Location location) {
        if (location != null) {
            textLatitude.setText(String.format(Locale.getDefault(), "%.6f", location.getLatitude()));
            textLongitude.setText(String.format(Locale.getDefault(), "%.6f", location.getLongitude()));
        }
    }

    // ---------------------- CICLO DE VIDA ----------------------

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermissions() && requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (requestingLocationUpdates) {
            // Detener las actualizaciones para ahorrar batería cuando la app no está en primer plano
            stopLocationUpdates();
        }
    }
}