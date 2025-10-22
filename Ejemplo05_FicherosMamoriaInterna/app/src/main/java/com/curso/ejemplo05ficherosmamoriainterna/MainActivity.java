package com.curso.ejemplo05ficherosmamoriainterna;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etEmail, etTelefono;
    private TextView tvResultado;
    private FileHelper fileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar FileHelper
        fileHelper = new FileHelper(this);

        // Referencias a la UI
        etNombre = findViewById(R.id.et_nombre);
        etApellido = findViewById(R.id.et_apellido);
        etEmail = findViewById(R.id.et_email);
        etTelefono = findViewById(R.id.et_telefono);q
        tvResultado = findViewById(R.id.tv_resultado);

        // Configuración de los botones
        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardarRegistro());
        findViewById(R.id.btn_buscar).setOnClickListener(v -> buscarRegistro());
        findViewById(R.id.btn_actualizar).setOnClickListener(v -> actualizarRegistro());
        findViewById(R.id.btn_eliminar).setOnClickListener(v -> eliminarRegistro());

        mostrarTodosLosRegistros(); // Muestra el estado inicial al cargar.


        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
    }

    /** Recolecta los datos de los EditText para crear un objeto Registro. */
    private Registro getRegistroFromFields() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "El Correo Electrónico es obligatorio (es el ID)", Toast.LENGTH_SHORT).show();
            return null;
        }
        return new Registro(nombre, apellido, email, telefono);
    }

    // ================= CRUD IMPLEMENTATION =================

    // CREATE
    private void guardarRegistro() {
        Registro nuevoReg = getRegistroFromFields();
        if (nuevoReg == null) return;

        if (fileHelper.agregarRegistro(nuevoReg)) {
            Toast.makeText(this, "✅ Registro Guardado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
            mostrarTodosLosRegistros();
        } else {
            Toast.makeText(this, "❌ Error: El Email ya existe o falló la escritura.", Toast.LENGTH_LONG).show();
        }
    }

    // READ
    private void buscarRegistro() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese el Email para buscar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Registro reg = fileHelper.buscarRegistro(email);
        if (reg != null) {
            // Rellenar campos con el registro encontrado
            etNombre.setText(reg.nombre);
            etApellido.setText(reg.apellido);
            etTelefono.setText(reg.telefono);
            tvResultado.setText("Resultado: Registro de " + reg.nombre + " encontrado.");
        } else {
            Toast.makeText(this, "❌ Registro no encontrado.", Toast.LENGTH_SHORT).show();
            tvResultado.setText("Resultado: Registro no encontrado.");
            // Opcional: limpiar campos no-ID
            etNombre.setText("");
            etApellido.setText("");
            etTelefono.setText("");
        }
    }

    // UPDATE
    private void actualizarRegistro() {
        Registro regActualizado = getRegistroFromFields();
        if (regActualizado == null) return;

        if (fileHelper.actualizarRegistro(regActualizado)) {
            Toast.makeText(this, "🔃 Registro Actualizado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
            mostrarTodosLosRegistros();
        } else {
            Toast.makeText(this, "❌ Error: Registro no encontrado para actualizar.", Toast.LENGTH_LONG).show();
        }
    }

    // DELETE
    private void eliminarRegistro() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese el Email para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fileHelper.eliminarRegistro(email)) {
            Toast.makeText(this, "🗑️ Registro Eliminado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
            mostrarTodosLosRegistros();
        } else {
            Toast.makeText(this, "❌ Error: Registro no encontrado para eliminar.", Toast.LENGTH_LONG).show();
        }
    }

    // ================= UTILITIES =================

    /** Muestra todos los registros actuales en el TextView de resultado. */
    private void mostrarTodosLosRegistros() {
        List<Registro> registros = fileHelper.leerTodosLosRegistros();
        StringBuilder sb = new StringBuilder("Registros Actuales:\n");

        if (registros.isEmpty()) {
            sb.append("No hay registros almacenados.");
        } else {
            for (Registro reg : registros) {
                sb.append(" -> ").append(reg.email).append(" (").append(reg.nombre).append(")\n");
            }
        }
        tvResultado.setText(sb.toString());
    }

    /** Limpia todos los campos de texto de la UI. */
    private void limpiarCampos() {
        etNombre.setText("");
        etApellido.setText("");
        etEmail.setText("");
        etTelefono.setText("");
    }

}