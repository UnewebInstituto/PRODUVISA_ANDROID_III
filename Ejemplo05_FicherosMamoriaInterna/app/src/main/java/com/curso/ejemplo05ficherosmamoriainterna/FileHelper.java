package com.curso.ejemplo05ficherosmamoriainterna;

import android.content.Context;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
public class FileHelper {
    private static final String FILENAME = "datos_registro.txt";
    private final Context context;

    public FileHelper(Context context) {
        this.context = context;
    }

    // =================================================================
    // LECTURA (READ)
    // =================================================================

    /**
     * Lee todos los registros del archivo y los devuelve como una lista de objetos.
     */
    public List<Registro> leerTodosLosRegistros() {
        List<Registro> registros = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput(FILENAME);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                Registro reg = Registro.fromFileString(line);
                if (reg != null) {
                    registros.add(reg);
                }
            }
        } catch (FileNotFoundException e) {
            // No pasa nada, el archivo aún no existe. Devolvemos lista vacía.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return registros;
    }

    // =================================================================
    // ESCRITURA Y CRUD
    // =================================================================

    /**
     * Escribe la lista completa de registros de vuelta al archivo, sobrescribiendo el contenido.
     */
    private void sobrescribirArchivo(List<Registro> registros) throws IOException {
        // Usamos Context.MODE_PRIVATE, que sobrescribe el archivo existente.
        try (FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
            for (Registro reg : registros) {
                String line = reg.toFileString() + "\n";
                fos.write(line.getBytes());
            }
        }
    }

    /**
     * CREAR (CREATE): Agrega un nuevo registro.
     */
    public boolean agregarRegistro(Registro nuevoRegistro) {
        List<Registro> registros = leerTodosLosRegistros();
        // Evitamos duplicados: usamos el email como ID único.
        for (Registro reg : registros) {
            if (reg.email.equalsIgnoreCase(nuevoRegistro.email)) {
                return false; // El registro ya existe
            }
        }

        registros.add(nuevoRegistro);
        try {
            sobrescribirArchivo(registros);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * CONSULTAR/LEER (READ - Específico): Busca un registro por email.
     */
    public Registro buscarRegistro(String email) {
        List<Registro> registros = leerTodosLosRegistros();
        for (Registro reg : registros) {
            if (reg.email.equalsIgnoreCase(email)) {
                return reg;
            }
        }
        return null; // No encontrado
    }

    /**
     * ACTUALIZAR (UPDATE): Busca un registro por email y lo reemplaza con el nuevo.
     */
    public boolean actualizarRegistro(Registro registroActualizado) {
        List<Registro> registros = leerTodosLosRegistros();
        boolean encontrado = false;

        for (int i = 0; i < registros.size(); i++) {
            if (registros.get(i).email.equalsIgnoreCase(registroActualizado.email)) {
                registros.set(i, registroActualizado); // Reemplazar
                encontrado = true;
                break;
            }
        }

        if (encontrado) {
            try {
                sobrescribirArchivo(registros);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false; // No encontrado para actualizar
    }

    /**
     * ELIMINAR (DELETE): Busca un registro por email y lo elimina.
     */
    public boolean eliminarRegistro(String email) {
        List<Registro> registros = leerTodosLosRegistros();
        boolean eliminado = registros.removeIf(reg -> reg.email.equalsIgnoreCase(email));

        if (eliminado) {
            try {
                sobrescribirArchivo(registros);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false; // No encontrado para eliminar
    }




}
