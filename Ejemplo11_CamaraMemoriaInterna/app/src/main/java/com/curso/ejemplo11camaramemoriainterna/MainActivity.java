package com.curso.ejemplo11camaramemoriainterna;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment; // Aún se usa para el tipo de directorio
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CameraInternal";

    private ImageView imageViewFoto;
    private Button buttonTomarFoto;
    private TextView textViewPath;

    private String currentPhotoPath; // Ruta del archivo en la memoria interna
    private Uri photoURI; // URI del FileProvider

    // Launcher para la solicitud del permiso de la Cámara
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // Launcher para la actividad de la Cámara
    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        imageViewFoto = findViewById(R.id.imageView_foto);
        buttonTomarFoto = findViewById(R.id.button_tomar_foto);
        textViewPath = findViewById(R.id.textView_path);

        // 1. Inicializar los ActivityResultLaunchers
        setupLaunchers();

        // 2. Manejar el click del botón
        buttonTomarFoto.setOnClickListener(v -> handleCameraAction());

        // 3. Revisar el estado inicial para actualizar el texto del botón
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

        // A. Launcher para solicitar el permiso de CÁMARA
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        buttonTomarFoto.setText("Tomar Foto (Memoria Interna)");
                        Toast.makeText(this, "Permisos concedidos. Abriendo cámara... ✅", Toast.LENGTH_SHORT).show();
                        dispatchTakePictureIntent();
                    } else {
                        buttonTomarFoto.setText("Permiso Denegado: Reintentar");
                        Toast.makeText(this, "Permiso de Cámara denegado. ❌", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // B. Launcher para la actividad de la CÁMARA
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentPhotoPath != null) {
                            setPic();
                            textViewPath.setText("Ruta Guardada: " + currentPhotoPath);
                            Toast.makeText(this, "Foto guardada en Memoria Interna.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Captura de foto cancelada o fallida.", Toast.LENGTH_SHORT).show();
                        currentPhotoPath = null;
                        photoURI = null;
                    }
                }
        );
    }

    // ---------------------- GESTIÓN DEL FLUJO DE PERMISOS ----------------------

    private void updateButtonTextAndToast() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            buttonTomarFoto.setText("Solicitar Permiso de Cámara");
        } else {
            buttonTomarFoto.setText("Tomar Foto (Memoria Interna)");
        }
    }

    private void handleCameraAction() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // ---------------------- LÓGICA DE LA CÁMARA Y ARCHIVOS ----------------------

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                // 1. Crea el archivo de destino en la memoria INTERNA (privada de la app)
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error al crear el archivo de imagen: " + ex.getMessage());
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                // 2. Obtener un URI seguro usando FileProvider
                photoURI = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile
                );

                // 3. Conceder permisos temporales de lectura/escritura a la app de cámara
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // 4. Pasar la URI a la app de cámara para que guarde la foto
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // 5. Lanzar la cámara
                takePictureLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crea un archivo JPEG con timestamp en el directorio /data/data/tu.paquete/files/images/
     * (Memoria Interna de la Aplicación).
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // CAMBIO CLAVE: Usamos getFilesDir() para la memoria interna
        File storageDir = new File(getFilesDir(), "images");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // ---------------------- REDIMENSIONAMIENTO DE IMAGEN ----------------------

    /**
     * Carga la imagen desde la ruta interna, la redimensiona para la miniatura y la muestra.
     */
    private void setPic() {
        if (currentPhotoPath == null) return;

        int targetW = imageViewFoto.getWidth() > 0 ? imageViewFoto.getWidth() : 250;
        int targetH = imageViewFoto.getHeight() > 0 ? imageViewFoto.getHeight() : 250;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageViewFoto.setImageBitmap(bitmap);
    }

}