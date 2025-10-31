package com.curso.ejemplo12videomemoriainterna;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.MediaController; // Importar MediaController
import android.widget.Toast;
import android.widget.VideoView; // Importar VideoView

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CameraVideoInternal";
    private Button buttonGrabarVideo;
    private TextView textViewPath;
    private VideoView videoView;

    private String currentVideoPath; // Ruta absoluta del archivo en la memoria interna
    private Uri videoURI; // URI seguro de FileProvider

    // Cambiamos a RequestMultiplePermissions para solicitar CÁMARA y ESCRITURA (compatibilidad)
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> takeVideoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        buttonGrabarVideo = findViewById(R.id.button_grabar_video);
        textViewPath = findViewById(R.id.textView_path);
        videoView = findViewById(R.id.video_view);

        // 1. Inicializar los ActivityResultLaunchers
        setupLaunchers();

        // 2. Manejar el click del botón
        buttonGrabarVideo.setOnClickListener(v -> handleCameraAction());

        // 3. Revisar el estado inicial
        updateButtonTextAndToast();

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */

    }

    // ---------------------- CONFIGURACIÓN DE LAUNCHERS ----------------------

    private void setupLaunchers() {

        // A. Launcher para solicitar PERMISOS MÚLTIPLES
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    // Verificamos si el permiso de CÁMARA fue concedido
                    boolean cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);

                    if (cameraGranted) {
                        buttonGrabarVideo.setText("Grabar Video (Memoria Interna)");
                        Toast.makeText(this, "Permisos concedidos. Abriendo cámara... ✅", Toast.LENGTH_SHORT).show();
                        dispatchTakeVideoIntent();
                    } else {
                        buttonGrabarVideo.setText("Permiso Denegado: Reintentar");
                        Toast.makeText(this, "Permiso de Cámara denegado. ❌", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // B. Launcher para la actividad de la CÁMARA (sin cambios)
        takeVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentVideoPath != null) {
                            playVideo(); // Muestra el video
                            textViewPath.setText("Ruta Guardada: " + currentVideoPath);
                            Toast.makeText(this, "Video guardado y listo para reproducir.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Grabación de video cancelada o fallida.", Toast.LENGTH_SHORT).show();
                        // Importante: Limpiar las rutas si la grabación falla o se cancela
                        currentVideoPath = null;
                        videoURI = null;
                    }
                }
        );
    }

    // ---------------------- GESTIÓN DEL FLUJO DE PERMISOS ----------------------

    private void updateButtonTextAndToast() {
        // Solo verificamos el permiso de CÁMARA para el estado del botón
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            buttonGrabarVideo.setText("Solicitar Permiso de Cámara");
        } else {
            buttonGrabarVideo.setText("Grabar Video (Memoria Interna)");
        }
    }

    private void handleCameraAction() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakeVideoIntent();
        } else {
            // ✅ CORRECCIÓN SUGERIDA: Pedir CAMERA y WRITE_EXTERNAL_STORAGE (compatibilidad)
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        }
    }

    // ---------------------- LÓGICA DE LA CÁMARA Y ARCHIVOS ----------------------

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {

            File videoFile = null;
            try {
                // 1. Crea el archivo de destino en la memoria INTERNA (privada de la app)
                videoFile = createVideoFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error al crear el archivo de video: " + ex.getMessage());
                Toast.makeText(this, "Error al crear archivo de video", Toast.LENGTH_SHORT).show();
                return;
            }

            if (videoFile != null) {
                // 2. Obtener un URI seguro usando FileProvider
                videoURI = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        videoFile
                );

                // 3. Conceder permisos temporales a la app de cámara
                takeVideoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // 4. Pasar la URI para que la cámara guarde el video
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);

                // OPCIONAL: Limitar la duración o calidad (por ejemplo, 10 segundos)
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);

                // 5. Lanzar la cámara
                takeVideoLauncher.launch(takeVideoIntent);
            }
        } else {
            Toast.makeText(this, "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crea un archivo MP4 con timestamp en el directorio /data/data/tu.paquete/files/videos/
     * (Memoria Interna de la Aplicación).
     */
    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";

        // CAMBIO CLAVE: Usamos getFilesDir() para la memoria interna y el subdirectorio "videos"
        File storageDir = new File(getFilesDir(), "videos");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File video = File.createTempFile(
                videoFileName,
                ".mp4",
                storageDir
        );

        currentVideoPath = video.getAbsolutePath();
        return video;
    }

    // ---------------------- REPRODUCCIÓN DE VIDEO ----------------------

    /**
     * Carga el video desde la ruta interna y lo reproduce en el VideoView.
     */
    private void playVideo() {
        if (videoURI == null) return;

        // 1. Establecer la URI segura del FileProvider en el VideoView
        videoView.setVideoURI(videoURI);

        // 2. Añadir controles de reproducción (opcional pero recomendado)
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // 3. Iniciar la reproducción
        videoView.start();

        // Para detener el video cuando la actividad se detenga
        videoView.setOnCompletionListener(mp -> {
            Log.d(TAG, "Reproducción de video finalizada.");
        });
    }
}