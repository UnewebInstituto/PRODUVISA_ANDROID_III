package com.curso.ejemplo04basesdedatos;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText et_id, et_nombre, et_apellido, et_telefono, et_email;
    private AdminSQLiteOpenHelper admin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */

        et_id = findViewById(R.id.et_id);
        et_nombre = findViewById(R.id.et_nombre);
        et_apellido = findViewById(R.id.et_apellido);
        et_telefono = findViewById(R.id.et_telefono);
        et_email = findViewById(R.id.et_email);

        admin = new AdminSQLiteOpenHelper(this);

    }

    // Método para Inserción (Botón "Guardar")
    public void registrar(View view) {
        SQLiteDatabase db = admin.getWritableDatabase();
        String nombre = et_nombre.getText().toString();
        String apellido = et_apellido.getText().toString();
        String telefono = et_telefono.getText().toString();
        String email = et_email.getText().toString();

        if (!nombre.isEmpty() && !apellido.isEmpty() && !telefono.isEmpty() && !email.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("nombre", nombre);
            registro.put("apellido", apellido);
            registro.put("telefono", telefono);
            registro.put("email", email);

            db.insert("contactos", null, registro);
            //db.close();

            limpiarCampos();
            Toast.makeText(this, "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Debes llenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para Consulta (Botón "Buscar")
    public void buscar(View view) {
        SQLiteDatabase db = admin.getReadableDatabase();
        String id = et_id.getText().toString();

        if (!id.isEmpty()) {
            Cursor fila = db.rawQuery(
                    "SELECT nombre, apellido, telefono, email FROM contactos WHERE id =" + id, null);

            if (fila.moveToFirst()) {
                et_nombre.setText(fila.getString(0));
                et_apellido.setText(fila.getString(1));
                et_telefono.setText(fila.getString(2));
                et_email.setText(fila.getString(3));
            } else {
                Toast.makeText(this, "El contacto no existe", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            }
            //db.close();
        } else {
            Toast.makeText(this, "Debes ingresar un ID para buscar", Toast.LENGTH_SHORT).show();
        }
    }


    // Método para Actualización (Botón "Modificar")
    public void modificar(View view) {
        SQLiteDatabase db = admin.getWritableDatabase();
        String id = et_id.getText().toString();
        String nombre = et_nombre.getText().toString();
        String apellido = et_apellido.getText().toString();
        String telefono = et_telefono.getText().toString();
        String email = et_email.getText().toString();

        if (!id.isEmpty() && !nombre.isEmpty() && !apellido.isEmpty() && !telefono.isEmpty() && !email.isEmpty()) {
            ContentValues registro = new ContentValues();
            registro.put("nombre", nombre);
            registro.put("apellido", apellido);
            registro.put("telefono", telefono);
            registro.put("email", email);

            int cantidad = db.update("contactos", registro, "id=" + id, null);
            //db.close();

            if (cantidad == 1) {
                Toast.makeText(this, "Contacto modificado exitosamente", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            } else {
                Toast.makeText(this, "El contacto no existe", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Debes llenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para Eliminación (Botón "Eliminar")
    public void eliminar(View view) {
        SQLiteDatabase db = admin.getWritableDatabase();
        String id = et_id.getText().toString();

        if (!id.isEmpty()) {
            int cantidad = db.delete("contactos", "id=" + id, null);
            //db.close();

            if (cantidad == 1) {
                Toast.makeText(this, "Contacto eliminado exitosamente", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            } else {
                Toast.makeText(this, "El contacto no existe", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Debes ingresar un ID para eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    // Método auxiliar para limpiar los campos de texto
    private void limpiarCampos() {
        et_id.setText("");
        et_nombre.setText("");
        et_apellido.setText("");
        et_telefono.setText("");
        et_email.setText("");
    }


}