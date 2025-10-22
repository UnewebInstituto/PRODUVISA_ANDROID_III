package com.curso.ejemplo05ficherosmamoriainterna;

public class Registro {
    public String nombre;
    public String apellido;
    public String email; // Usaremos el email como ID único para buscar y actualizar.
    public String telefono;

    public Registro(String nombre, String apellido, String email, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
    }

    /**
     * Convierte el objeto a una línea de texto para guardarla en el archivo.
     * Usamos '|' como delimitador.
     */
    public String toFileString() {
        return nombre + "|" + apellido + "|" + email + "|" + telefono;
    }

    /**
     * Crea un objeto Registro a partir de una línea de texto del archivo.
     */
    public static Registro fromFileString(String line) {
        String[] parts = line.split("\\|"); // Usamos \\| para escapar el caracter '|'
        if (parts.length == 4) {
            return new Registro(parts[0], parts[1], parts[2], parts[3]);
        }
        return null;
    }
}
