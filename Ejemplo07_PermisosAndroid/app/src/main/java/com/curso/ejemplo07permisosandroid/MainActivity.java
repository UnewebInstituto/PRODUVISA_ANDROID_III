package com.curso.ejemplo07permisosandroid;

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

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PermissionHandler";

    // Lista de permisos que queremos gestionar
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    // Componentes de la UI
    private TextView tvCameraStatus, tvAudioStatus;
    private Button btnRequestPermissions;

    // ActivityResultLauncher para manejar la solicitud de permisos
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvCameraStatus = findViewById(R.id.textView_camera_status);
        tvAudioStatus = findViewById(R.id.textView_audio_status);
        btnRequestPermissions = findViewById(R.id.button_request_permissions);

        // 1. Inicializar el ActivityResultLauncher
        setupPermissionsLauncher();

        // 2. Revisar el estado inicial de los permisos
        checkAndDisplayPermissions();

        // 3. Configurar el Listener del botón
        btnRequestPermissions.setOnClickListener(v -> handlePermissionRequest());

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
    }

    /**
     * Configura el lanzador de resultados para manejar la respuesta del diálogo de permisos.
     */
    private void setupPermissionsLauncher() {
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> results) -> {
                    // El Map 'results' contiene el resultado (true/false) para cada permiso solicitado

                    // Recorremos los resultados y actualizamos la UI
                    for (Map.Entry<String, Boolean> entry : results.entrySet()) {
                        String permission = entry.getKey();
                        boolean isGranted = entry.getValue();

                        if (permission.equals(Manifest.permission.CAMERA)) {
                            updateStatus(tvCameraStatus, "CÁMARA", isGranted);
                        } else if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
                            updateStatus(tvAudioStatus, "AUDIO", isGranted);
                        }
                    }

                    // Mensaje final de resumen
                    boolean allGranted = areAllPermissionsGranted();
                    if (allGranted) {
                        Toast.makeText(this, "Todos los permisos concedidos. ✅", Toast.LENGTH_SHORT).show();
                        btnRequestPermissions.setEnabled(false);
                    } else {
                        Toast.makeText(this, "Algunos permisos fueron denegados. ⚠️", Toast.LENGTH_LONG).show();
                        btnRequestPermissions.setEnabled(true);
                    }
                }
        );
    }

    /**
     * Identifica los permisos faltantes y lanza el diálogo de solicitud.
     */
    private void handlePermissionRequest() {
        // 1. Identificar qué permisos faltan
        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // 2. Si hay permisos faltantes, lanzamos la solicitud
        if (!permissionsToRequest.isEmpty()) {
            // Convertir la lista a un array para el launcher
            String[] arrayToRequest = permissionsToRequest.toArray(new String[0]);

            // Lanzar la solicitud de permisos
            requestPermissionsLauncher.launch(arrayToRequest);
        } else {
            Toast.makeText(this, "Todos los permisos ya están concedidos. 👍", Toast.LENGTH_SHORT).show();
            btnRequestPermissions.setEnabled(false);
        }
    }

    /**
     * Revisa el estado actual de todos los permisos y actualiza la UI al inicio.
     */
    private void checkAndDisplayPermissions() {
        boolean cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean audioGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        updateStatus(tvCameraStatus, "CÁMARA", cameraGranted);
        updateStatus(tvAudioStatus, "AUDIO", audioGranted);

        if (cameraGranted && audioGranted) {
            btnRequestPermissions.setEnabled(false);
            Log.d(TAG, "Todos los permisos ya concedidos.");
        } else {
            btnRequestPermissions.setEnabled(true);
        }
    }

    /**
     * Helper: Actualiza el texto del TextView según el estado del permiso.
     */
    private void updateStatus(TextView textView, String name, boolean isGranted) {
        if (isGranted) {
            textView.setText(name + ": CONCEDIDO ✅");
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            textView.setText(name + ": DENEGADO ❌");
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

    /**
     * Helper: Verifica si todos los permisos requeridos han sido concedidos.
     */
    private boolean areAllPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}