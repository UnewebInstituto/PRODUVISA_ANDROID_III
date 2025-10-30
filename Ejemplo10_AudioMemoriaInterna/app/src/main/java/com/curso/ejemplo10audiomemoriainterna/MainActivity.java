package com.curso.ejemplo10audiomemoriainterna;

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
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build; // Importar la clase Build
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AudioInternal";

    private Button btnRecord, btnStopRecord, btnPlay, btnStopPlay;
    private TextView textStatus, textViewPath;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private String audioFilePath = null; // Ruta absoluta del archivo de audio en la memoria interna

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicialización de Views
        btnRecord = findViewById(R.id.btn_record);
        btnStopRecord = findViewById(R.id.btn_stop_record);
        btnPlay = findViewById(R.id.btn_play);
        btnStopPlay = findViewById(R.id.btn_stop_play);
        textStatus = findViewById(R.id.text_status);
        textViewPath = findViewById(R.id.textView_path);

        // 1. Inicializar Launcher de Permisos
        setupPermissionLauncher();

        // 2. Configurar Listeners de botones
        btnRecord.setOnClickListener(v -> handleRecordAction());
        btnStopRecord.setOnClickListener(v -> stopRecording());
        btnPlay.setOnClickListener(v -> playRecording());
        btnStopPlay.setOnClickListener(v -> stopPlaying());

        // 3. Revisar estado inicial del permiso
        checkInitialPermissions();
    }

    // ---------------------- GESTIÓN DE PERMISOS ----------------------

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        textStatus.setText("Estado: Permiso Concedido. ✅");
                        // 1. CORRECCIÓN: Si el usuario concede el permiso, iniciamos la grabación
                        // Esto soluciona el crash si el usuario toca "Grabar" e inmediatamente concede el permiso.
                        Toast.makeText(this, "Permiso de Micrófono concedido. Iniciando grabación.", Toast.LENGTH_SHORT).show();
                        startRecording();
                    } else {
                        textStatus.setText("Estado: Permiso Denegado.");
                        btnRecord.setEnabled(false);
                        Toast.makeText(this, "Permiso de Micrófono denegado. ❌", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void checkInitialPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            textStatus.setText("Estado: Solicitando Permiso...");
            btnRecord.setEnabled(false);
            // La solicitud se maneja aquí. El resultado y la habilitación del botón
            // se manejan en setupPermissionLauncher().
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            textStatus.setText("Estado: Listo para grabar.");
            btnRecord.setEnabled(true);
        }
    }


    private void handleRecordAction() {
        // Al tocar el botón, se verifica si el permiso ya fue dado
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            // Permiso OK: Inicia la grabación
            startRecording();
        } else {
            // Permiso NO OK: Lanza la solicitud. La grabación se iniciará
            // solo si el permiso se concede dentro del launcher.
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startRecording() {
        // 1. Crear el archivo de destino en la memoria interna
        try {
            createAudioFile();
        } catch (IOException e) {
            Log.e(TAG, "Error al crear el archivo de audio: " + e.getMessage());
            Toast.makeText(this, "Error al preparar el archivo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. CORRECCIÓN DE COMPATIBILIDAD API (MediaRecorder Constructor)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31 (Android 12) o superior: usar el constructor con Context
                mediaRecorder = new MediaRecorder(this);
            } else {
                // API 30 o inferior: usar el constructor sin argumentos
                mediaRecorder = new MediaRecorder();
            }

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // Formato 3GP
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.prepare();
            mediaRecorder.start();

            // Actualizar UI
            textStatus.setText("Estado: Grabando...");
            btnRecord.setEnabled(false);
            btnStopRecord.setEnabled(true);
            btnPlay.setEnabled(false);
            btnStopPlay.setEnabled(false);

            textViewPath.setText("Ruta: " + audioFilePath);
            Toast.makeText(this, "Iniciando grabación.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Fallo en prepare() o start() de MediaRecorder: " + e.getMessage());
            textStatus.setText("Estado: Error de Grabación (IO).");
            stopRecording(); // Intentar limpiar
        } catch (Exception e) {
            // Capturar SecurityException si el permiso falla, aunque la lógica del launcher debe evitar esto
            Log.e(TAG, "Error al iniciar MediaRecorder: " + e.getMessage());
            textStatus.setText("Estado: Error de Grabación.");
            stopRecording();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                // El log para detener la grabación se mantiene aquí
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                // Actualizar UI
                textStatus.setText("Estado: Grabación finalizada.");
                btnRecord.setEnabled(true);
                btnStopRecord.setEnabled(false);
                btnPlay.setEnabled(true); // Habilitar reproducción
                btnStopPlay.setEnabled(false);

                Toast.makeText(this, "Grabación detenida. Archivo guardado.", Toast.LENGTH_SHORT).show();

            } catch (RuntimeException e) {
                // Esto puede ocurrir si se llama a stop inmediatamente después de start.
                Log.e(TAG, "Error al detener grabación (Runtime): " + e.getMessage());
                // Asegurar liberación de recursos
                mediaRecorder.release();
                mediaRecorder = null;
                Toast.makeText(this, "Error al detener grabación. Intenta grabar más tiempo.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Crea un archivo .3gp con timestamp en el directorio /data/data/tu.paquete/files/audio/
     * (Memoria Interna de la Aplicación).
     */
    private void createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String audioFileName = "AUDIO_" + timeStamp + "_";

        // Usamos getFilesDir() para la memoria interna y el subdirectorio "audio"
        File storageDir = new File(getFilesDir(), "audio");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File audio = File.createTempFile(
                audioFileName,
                ".3gp",
                storageDir
        );

        audioFilePath = audio.getAbsolutePath();
    }

    // ---------------------- REPRODUCCIÓN (MediaPlayer) ----------------------
    // El código de reproducción se mantiene igual, ya que no tenía problemas de inicialización o API.

    private void playRecording() {
        if (audioFilePath == null) {
            Toast.makeText(this, "No hay audio grabado para reproducir.", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Actualizar UI
            textStatus.setText("Estado: Reproduciendo...");
            btnRecord.setEnabled(false);
            btnStopRecord.setEnabled(false);
            btnPlay.setEnabled(false);
            btnStopPlay.setEnabled(true);

            // Listener para cuando la reproducción termine
            mediaPlayer.setOnCompletionListener(mp -> stopPlaying());

        } catch (IOException e) {
            Log.e(TAG, "Fallo al reproducir audio: " + e.getMessage());
            textStatus.setText("Estado: Error de Reproducción.");
            stopPlaying(); // Limpiar el estado
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;

            // Actualizar UI
            textStatus.setText("Estado: Reproducción detenida.");
            btnRecord.setEnabled(true);
            btnStopRecord.setEnabled(false);
            btnPlay.setEnabled(true); // Re-habilitar reproducción
            btnStopPlay.setEnabled(false);
        }
    }

    // ---------------------- CICLO DE VIDA ----------------------

    @Override
    protected void onStop() {
        super.onStop();
        // Limpiar recursos al salir de la actividad
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}