package com.curso.ejemplo06ficheromemoriaexternasd;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FileStorage";
    private static final String FILE_NAME = "mi_data_externa.txt";

    private EditText editTextData;
    private TextView textViewLoadedData;
    private Button buttonSave, buttonLoad;

    private File externalFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextData = findViewById(R.id.editText_data);
        textViewLoadedData = findViewById(R.id.textView_loaded_data);
        buttonSave = findViewById(R.id.button_save);
        buttonLoad = findViewById(R.id.button_load);

        // 1. Inicializar el objeto File que apunta a la ruta externa
        externalFile = getExternalStorageFile(FILE_NAME);

        buttonSave.setOnClickListener(v -> saveData());
        buttonLoad.setOnClickListener(v -> loadData());

        // Mostrar la ruta del archivo para depuración
        Toast.makeText(this, "Ruta: " + externalFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
    }

    /**
     * Define la ruta del archivo en el almacenamiento externo (SD o memoria principal).
     * Utilizamos getExternalFilesDir() que NO requiere permisos de tiempo de ejecución.
     */
    private File getExternalStorageFile(String fileName) {
        // Obtenemos el directorio base para documentos en el almacenamiento externo privado de la app.
        // Ruta típica: /storage/emulated/0/Android/data/tu.paquete/files/Documents/
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if (storageDir == null) {
            Log.e(TAG, "El almacenamiento externo no está disponible o montado.");
            Toast.makeText(this, "Error: No se puede acceder al almacenamiento externo.", Toast.LENGTH_LONG).show();
            return null;
        }

        // Asegurarse de que el directorio exista
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return new File(storageDir, fileName);
    }

    // ---------------------- GUARDAR DATOS ----------------------

    private void saveData() {
        if (externalFile == null) return;

        String data = editTextData.getText().toString();

        try (FileOutputStream fos = new FileOutputStream(externalFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos)) {

            osw.write(data);
            Toast.makeText(this, "Datos guardados en memoria externa.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error al guardar datos: " + e.getMessage());
            Toast.makeText(this, "Fallo al guardar en la SD Externa.", Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------- CARGAR DATOS ----------------------

    private void loadData() {
        if (externalFile == null) return;

        if (!externalFile.exists()) {
            textViewLoadedData.setText("[Archivo no existe]");
            Toast.makeText(this, "El archivo no existe en la SD Externa.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(externalFile);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }

            // Eliminar el último salto de línea
            if (stringBuilder.length() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }

            textViewLoadedData.setText(stringBuilder.toString());
            Toast.makeText(this, "Datos cargados exitosamente.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error al cargar datos: " + e.getMessage());
            textViewLoadedData.setText("[ERROR AL CARGAR DATOS]");
            Toast.makeText(this, "Fallo al cargar de la SD Externa.", Toast.LENGTH_LONG).show();
        }
    }

}