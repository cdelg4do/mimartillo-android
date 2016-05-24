package com.cdelgado.mimartillocom;


import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class GestorSesiones
{
    public static enum TipoUsuario
    {
        PARTICULAR,
        PROFESIONAL
    }

/*
    public static enum estadoCredenciales
    {
        OK,
        INVALID,
        EXPIRED,
        DISABLED,
        NONEXISTENT,
        UNKNOWN
    }
*/

    private static final int acceso_preferencias = Context.MODE_PRIVATE;    // Modo de Shared Preferences

    private SharedPreferences prefUsuario;      // Shared Preferences del usuario (particular o profesional)
    private Editor editorUsuario;               // Editor para las Shared Preferences del usuario
    private SharedPreferences prefGeneral;      // Shared Preferences generales (configuración del cliente, independiente del tipo de usuario)
    private Editor editorGeneral;               // Editor para las Shared Preferences generales
    private Context contexto;                   // Contexto

    private static String PREFERENCIAS_USUARIO; // Indica el nombre de la colección de preferencias a utilizar por el usuario
    public static final String PREFERENCIAS_GENERAL = "Preferencias_general"; // Indica el nombre de la colección de preferencias generales

    // Posibles valores para el nombre de la colección de preferencias de usuario
    public static final String PREF_PARTICULAR  = "Preferencias_particular";
    public static final String PREF_PROFESIONAL = "Preferencias_profesional";

    // Claves para acceder a los valores de las preferencias de usuario
    public static final String KEY_NOMBRE = "nombre";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USUARIO = "id_usuario";
    public static final String KEY_SESION = "id_sesion";
    public static final String KEY_AVATAR = "avatar";
    private static final String SESION_GUARDADA = "hay_sesion";

    // Claves para acceder a los valores de las preferencias generales
    public static final String KEY_HTTPS = "usar_https";
    public static final String TEMP_DATA = "temp";



    // Constructor
    public GestorSesiones(Context c, TipoUsuario tipo)
    {
        this.contexto = c;

        // Determinar el tipo de usuario para las preferencias de usuario
        switch (tipo)
        {
            case PARTICULAR:    PREFERENCIAS_USUARIO = PREF_PARTICULAR;
                                break;

            case PROFESIONAL:   PREFERENCIAS_USUARIO = PREF_PROFESIONAL;
                                break;
        }

        prefUsuario = contexto.getSharedPreferences(PREFERENCIAS_USUARIO, acceso_preferencias);
        editorUsuario = prefUsuario.edit();


        // Preferencias generales
        prefGeneral = contexto.getSharedPreferences(PREFERENCIAS_GENERAL, acceso_preferencias);
        editorGeneral = prefGeneral.edit();
    }


    // Guardar un inicio de sesion
    public void guardarSesion(String nombre, String email, String usuario, String sesion, String imagen_base64)
    {
        editorUsuario.putString(KEY_NOMBRE, nombre);
        editorUsuario.putString(KEY_EMAIL, email);
        editorUsuario.putString(KEY_USUARIO, usuario);
        editorUsuario.putString(KEY_SESION, sesion);
        editorUsuario.putString(KEY_AVATAR, imagen_base64);

        editorUsuario.putBoolean(SESION_GUARDADA, true);

        editorUsuario.commit();
    }


    // Actualizar los datos de una sesión (nombre)
    // siempre y cuando ya existiera una sesión que actualizar
    public void actualizarDatos(String nombre)
    {
        if ( prefUsuario.getBoolean(SESION_GUARDADA,false) )
        {
            editorUsuario.putString(KEY_NOMBRE, nombre);

            editorUsuario.commit();
        }
    }


    // Actualizar los datos de una sesión (nombre y email)
    // siempre y cuando ya existiera una sesión que actualizar
    public void actualizarDatos(String nombre, String email)
    {
        if ( prefUsuario.getBoolean(SESION_GUARDADA,false) )
        {
            editorUsuario.putString(KEY_NOMBRE, nombre);
            editorUsuario.putString(KEY_EMAIL, email);

            editorUsuario.commit();
        }
    }


    // Actualizar los datos de una sesión (nombre, email y avatar)
    // siempre y cuando ya existiera una sesión que actualizar
    public void actualizarDatos(String nombre, String email, String imagen_base64)
    {
        if ( prefUsuario.getBoolean(SESION_GUARDADA,false) )
        {
            editorUsuario.putString(KEY_NOMBRE, nombre);
            editorUsuario.putString(KEY_EMAIL, email);
            editorUsuario.putString(KEY_AVATAR, imagen_base64);

            editorUsuario.commit();
        }
    }


    // Actualizar la preferencia del protocolo de comunicación
    // (es un ajuste general, se actualiza aunque no haya una sesión guardada)
    public void setConexionSegura(boolean conexionSegura)
    {
        editorGeneral.putBoolean(KEY_HTTPS, conexionSegura);
        editorGeneral.commit();
    }


    // Guardar un string como dato temporal en los ajustes generales
    // (útil para pasar información que no puede ser pasada a través de un intent por ser demasiado grande)
    public void setDatosTemporales(String datos)
    {
        editorGeneral.putString(TEMP_DATA, datos);
        editorGeneral.commit();
    }


    // Eliminar los datos temporales (si los hubiera) de las preferencias generales
    public void removeDatosTemporales()
    {
        editorGeneral.remove(TEMP_DATA);
        editorGeneral.commit();
    }


    // Obtener los datos temporales (si los hubiera) de las preferencias generales
    // Si no hay datos temporales almacenados, devuelve null
    public String getDatosTemporales()
    {
        return prefGeneral.getString(TEMP_DATA, null);
    }


    // Obtener los datos de la sesion almacenados
    // (incluye tanto preferencias de usuario como preferencas generales)
    public HashMap<String, String> getDatosSesion()
    {
        HashMap<String, String> datos = new HashMap<String, String>();

        datos.put(KEY_NOMBRE, prefUsuario.getString(KEY_NOMBRE, null));
        datos.put(KEY_EMAIL, prefUsuario.getString(KEY_EMAIL, null));
        datos.put(KEY_USUARIO, prefUsuario.getString(KEY_USUARIO, null));
        datos.put(KEY_SESION, prefUsuario.getString(KEY_SESION, null));
        datos.put(KEY_AVATAR, prefUsuario.getString(KEY_AVATAR, null));

        datos.put(KEY_HTTPS, Boolean.toString( usarConexionSegura() ));

        return datos;
    }


    // Cerrar una sesion (elimina todos los datos de la sesión del usuario de las Shared Preferences)
    public void destruirSesion()
    {
        editorUsuario.clear();
        editorUsuario.commit();
    }


    // Indica si hay almacenados datos de una sesion en las preferencias de usuario
    public boolean hayDatosSesion()
    {
        return prefUsuario.getBoolean(SESION_GUARDADA,false);
    }


    // Indica si se debe usar una conexión segura para las conexiones con el servidor
    // Será false solo si el campo KEY_HTTPS existe en las pref. generales y su valor es false.
    // En caso contrario (el campo KEY_HTTPS aún no existe, o existe y es true), será true.
    // (de este modo, en la primera ejecución de la aplicación se usará una conexión segura por defecto)
    public boolean usarConexionSegura()
    {
        return prefGeneral.getBoolean(KEY_HTTPS, true);
    }

}

