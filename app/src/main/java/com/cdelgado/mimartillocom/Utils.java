package com.cdelgado.mimartillocom;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import im.delight.android.location.SimpleLocation;


public abstract class Utils
{
    public static enum CategoriaDialogo
    {
        EXITO,
        ERROR,
        ADVERTENCIA,
        INFO,
        NINGUNO
    }

    public static enum TipoMensaje
    {
        DIALOGO,
        TOAST,
        NINGUNO
    }

    public static enum AppExterna
    {
        CORREO,
        MAPA_COORDENADAS,
        MAPA_DIRECCION,
        LLAMADA,
        NAVEGADOR
    }


    public static final String TEMP_FILENAME = "mimartillo_temporary.tmp";



   // Muestra un mensaje Toast, solo en configuración de debug
    public static void debug(Context ctx, String msg)
    {
        if (BuildConfig.DEBUG)
            Toast.makeText(ctx,msg,Toast.LENGTH_LONG).show();
    }


    // Muestra un mensaje al usuario del tipo indicado (Toast o Dialogo)
    // En el caso de ser dialogo, se indica la categoría del mensaje y el título
    public static void mostrarMensaje(Context ctx, String msg, TipoMensaje tipoMensaje, CategoriaDialogo cat, String titulo)
    {
        if ( tipoMensaje==TipoMensaje.NINGUNO )
        {
            return;
        }

        else if ( tipoMensaje==TipoMensaje.TOAST )
        {
            Toast.makeText(ctx,msg,Toast.LENGTH_LONG).show();
        }

        else if ( tipoMensaje==TipoMensaje.DIALOGO )
        {
            AlertDialog dialogo = new AlertDialog.Builder(ctx).create();
            dialogo.setTitle(titulo);
            dialogo.setMessage(msg);

            // Icono del cuádro de diálogo
            switch (cat)
            {
                case EXITO:         dialogo.setIcon(R.drawable.ic_exito);
                                    break;
                case ERROR:         dialogo.setIcon(R.drawable.ic_fallo);
                                    break;
                case ADVERTENCIA:   dialogo.setIcon(R.drawable.ic_advertencia);
                                    break;
                case INFO:          dialogo.setIcon(R.drawable.ic_info);
                                    break;
                case NINGUNO:       break;

                default:            break;
            }

            // Botón de OK
            dialogo.setButton
            (
                ctx.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int cual)
                    {
                    }
                }
            );

            dialogo.show();
        }

    }


    // Muestra un mensaje al usuario del tipo indicado (Toast o Dialogo)
    // En el caso de ser dialogo, se indica la categoría del mensaje y el título
    // Si tipoMensaje == DIALOGO y cerrarVentana== true, al pulsar el botón de Aceptar se cerrará la ventana actual
    public static void mostrarMensaje(final Context ctx, String msg, TipoMensaje tipoMensaje, CategoriaDialogo cat, String titulo, final boolean cerrarVentana)
    {
        if ( tipoMensaje==TipoMensaje.NINGUNO )
        {
            return;
        }

        else if ( tipoMensaje==TipoMensaje.TOAST )
        {
            Toast.makeText(ctx,msg,Toast.LENGTH_LONG).show();
        }

        else if ( tipoMensaje==TipoMensaje.DIALOGO )
        {
            AlertDialog dialogo = new AlertDialog.Builder(ctx).create();
            dialogo.setTitle(titulo);
            dialogo.setMessage(msg);

            // Icono del cuádro de diálogo
            switch (cat)
            {
                case EXITO:         dialogo.setIcon(R.drawable.ic_exito);
                    break;
                case ERROR:         dialogo.setIcon(R.drawable.ic_fallo);
                    break;
                case ADVERTENCIA:   dialogo.setIcon(R.drawable.ic_advertencia);
                    break;
                case INFO:          dialogo.setIcon(R.drawable.ic_info);
                    break;
                case NINGUNO:       break;

                default:            break;
            }

            // Botón de OK
            dialogo.setButton
                    (
                            ctx.getResources().getString(android.R.string.ok),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int cual)
                                {
                                    if ( cerrarVentana )
                                        ((Activity) ctx).finish();
                                }
                            }
                    );

            dialogo.show();
        }

    }


    // Analiza el resultado de una tarea de consulta de un servicio web.
    // Si la tarea falló, o el resultado devolvió error, realizará el procesamiento adecuado y devolverá TRUE.
    // Si la tarea no fallo y su resultado fue "OK", devolverá FALSE
    public static boolean procesar_respuesta_erronea(Activity ventana, TareaSegundoPlano tarea, int idTitulo, String emailUsuario, ArrayList<ErrorServicioWeb> posiblesRespuestasErroneas, GestorSesiones.TipoUsuario tipoUsuario)
    {
        String msg = "";
        String titulo = ventana.getResources().getString(idTitulo);

        RespuestaServicioWeb respuesta_servidor = tarea.getCliente().getServicioWeb().getRespuesta();


        // Si se superó el tiempo límite de espera para la conexión, mostrar un diálogo de error
        if ( tarea.timeoutCliente() )
        {
            msg = ventana.getResources().getString(R.string.msg_generico_ErrorTimeoutTarea);
            Utils.mostrarMensaje(ventana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ERROR, titulo);

            return true;
        }

        // Si la tarea en segundo plano falló (ej. servidor inaccesible), mostrar un diálogo de error
        else if ( tarea.haFallado() )
        {
            msg = ventana.getResources().getString(R.string.msg_generico_ErrorFalloTarea);
            Utils.mostrarMensaje(ventana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ERROR, titulo);

            return true;
        }

        // Si no se recibió una respuesta correcta (null), mostrar un diálogo de error
        else if ( respuesta_servidor == null )
        {
            msg = ventana.getResources().getString(R.string.msg_generico_ErrorRespuestaIncorrecta);
            Utils.mostrarMensaje(ventana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ERROR, titulo);

            return true;
        }

        // Si la respuesta se recibió correctamente, pero no fue satisfactoria (ERR)
        else if ( ! respuesta_servidor.getResultado().equals("OK") )
        {
            String detalle = respuesta_servidor.getDetalle();

            // Errores con la sesión actual (inválida, expirada, usuario bloqueado) --> redirigir a la pantalla de login
            if ( detalle.equals("ERR_SesionInvalida") || detalle.equals("ERR_SesionExpirada") || detalle.equals("ERR_UsuarioDeshabilitado")  )
            {
                if (detalle.equals("ERR_SesionInvalida"))
                    msg = ventana.getResources().getString(R.string.msg_generico_ErrorSesionInvalida);

                else if (detalle.equals("ERR_SesionExpirada"))
                    msg = ventana.getResources().getString(R.string.msg_generico_ErrorSesionExpirada);

                else if (detalle.equals("ERR_UsuarioDeshabilitado"))
                {
                    msg = ventana.getResources().getString(R.string.msg_generico_ErrorUsuarioDeshabilitado1);
                    msg += " '" + emailUsuario + "' ";
                    msg += ventana.getResources().getString(R.string.msg_generico_ErrorUsuarioDeshabilitado2);
                }

                Log.e("Fallo sesión almacenada","Cerrando ventanas y redirigiendo a VentanaInicio --> VentanaLogin...");

                // Redirigimos a la Ventana de Login a traves de la ventana de Inicio, cerrando todas las demás
                // (de este modo, garantizamos que la ventana de inicio siempre queda en el fondo de la pila
                // independientemente de cuántas ventanas hubiéramos abierto)
                Intent intent = new Intent(ventana, VentanaInicio.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Bundle info = new Bundle();
                info.putBoolean("ir_a_ventana_login",true);

                // Estos valores serán redirigidos a la ventana de Login
                info.putString("motivo", msg);
                info.putString("emailSugerido", emailUsuario);
                info.putSerializable("tipo_usuario", tipoUsuario);

                intent.putExtras(info);

                // Cerrar la ventana desde donde se invocó el servicio web que falló,
                // excepto si es la ventana de inicio, que permanecerá abierta
                if ( ventana instanceof VentanaInicio ) ;
                else
                    ventana.finish();

                ventana.startActivity(intent);
            }

            // Otros errores no relacionados con la sesión de usuario --> buscar coincidencias en la lista de posibles respuestas erróneas
            // (el error de la lista debe coincidir en tipo y en detalle)
            else
            {
                ServicioWeb.Tipo tipo = tarea.getCliente().getServicioWeb().getTipo();

                boolean sinCoincidencias = true;
                ErrorServicioWeb error = null;

                for (int i=0; i<posiblesRespuestasErroneas.size() && sinCoincidencias; i++)
                {
                    error = posiblesRespuestasErroneas.get(i);

                     if ( error.getTipo()==tipo && error.getDetalle().equals( detalle ) )
                         sinCoincidencias = false;
                }

                // Si el detalle devuelto por el servidor no coincide con ninguno de los indicados en la lista, acción por defecto
                // (mostrar un diálogo de error inesperado)
                if ( sinCoincidencias )
                {
                    msg = ventana.getResources().getString(R.string.msg_generico_ErrorInesperado) + "\n\n(" + detalle + ")";
                    Utils.mostrarMensaje(ventana,msg,Utils.TipoMensaje.DIALOGO,Utils.CategoriaDialogo.ERROR,titulo);
                }

                // Si el detalle devuelto por el servidor coincide con alguno de los indicados en la lista, procesarlo según corresponda
                else
                {
                    error.procesar(ventana);
                }
            }

            return true;
        }


        // Si hemos llegado hasta este punto, significa que el servidor devolvió una respuesta "OK"
        // y por lo tanto deberá ser tratada aparte de esta función
        return false;
    }




    // Mostrar diálogo de consulta al usuario para activar el servicio de ubicación (red/gps)
    public static void dialogo_AvisoUbicacion(final Context ctx, String titulo, String msg, CategoriaDialogo cat)
    {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(ctx);
        dialogo.setTitle(titulo);
        dialogo.setMessage(msg);

        // Icono del cuádro de diálogo
        switch (cat)
        {
            case EXITO:         dialogo.setIcon(R.drawable.ic_exito);
                break;
            case ERROR:         dialogo.setIcon(R.drawable.ic_fallo);
                break;
            case ADVERTENCIA:   dialogo.setIcon(R.drawable.ic_advertencia);
                break;
            case INFO:          dialogo.setIcon(R.drawable.ic_info);
                break;
            case NINGUNO:       break;

            default:            break;
        }


        // Botón de Aceptar
        dialogo.setPositiveButton
        (
            ctx.getResources().getString(android.R.string.ok),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SimpleLocation.openSettings(ctx);
                }
            }
        );


        // Botón de Cancelar
        dialogo.setNegativeButton
        (
            ctx.getResources().getString(android.R.string.cancel),
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    Activity a = (Activity) ctx;
                    a.finish();
                }
            }
        );


        dialogo.show();
    }


    // Mostrar diálogo de consulta al usuario para eliminar una obra
    public static void dialogo_EliminarObra(final Context ctx, String titulo, String msg, CategoriaDialogo cat, final String idObra)
    {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(ctx);
        dialogo.setTitle(titulo);
        dialogo.setMessage(msg);

        // Icono del cuádro de diálogo
        switch (cat)
        {
            case EXITO:         dialogo.setIcon(R.drawable.ic_exito);
                break;
            case ERROR:         dialogo.setIcon(R.drawable.ic_fallo);
                break;
            case ADVERTENCIA:   dialogo.setIcon(R.drawable.ic_advertencia);
                break;
            case INFO:          dialogo.setIcon(R.drawable.ic_info);
                break;
            case NINGUNO:       break;

            default:            break;
        }


        // Botón de Aceptar
        dialogo.setPositiveButton
        (
            ctx.getResources().getString(android.R.string.ok),
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    // Invocar al método EliminarObra de la ventana padre
                    // (este cuadro de diálogo puede ser invocado desde varias clases de ventana diferentes)
                    if ( ctx instanceof VentanaDatosObra )
                        ((VentanaDatosObra)ctx).eliminarObra(idObra);

                    else if ( ctx instanceof VentanaParticular )
                        ((VentanaParticular)ctx).eliminarObra(idObra);
                }
            }
        );


        // Botón de Cancelar
        dialogo.setNegativeButton
        (
            ctx.getResources().getString(android.R.string.cancel),
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    String msgCancelado = ctx.getResources().getString(R.string.Utils_eliminarObra_txt_msgCancelado);
                    Utils.mostrarMensaje(ctx, msgCancelado, TipoMensaje.TOAST, null, null);
                }
            }
        );


        dialogo.show();
    }


    public static int imagenActividadObra(int tipo)
    {
        if ( tipo>1000000 && tipo<2000000 )       return R.drawable.ic_work_construccion;
        else if ( tipo>2000000 && tipo<3000000 )  return R.drawable.ic_work_reforma;
        else if ( tipo>3000000 && tipo<4000000 )  return R.drawable.ic_work_mudanzas;
        else if ( tipo>4000000 && tipo<5000000 )  return R.drawable.ic_work_tecnicos;
        else if ( tipo>5000000 && tipo<6000000 )  return R.drawable.ic_work_obramenor;
        else if ( tipo>6000000 && tipo<7000000 )  return R.drawable.ic_work_instaladores;
        else if ( tipo>7000000 && tipo<8000000 )  return R.drawable.ic_work_mantenimiento;
        else                                      return R.drawable.ic_work_tiendas;
    }



    public static String codifica_jpeg_base64(Bitmap imagen)
    {
        // Comprimir el bitmap indicado en formato JPEG
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imagen.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        // Codificar la imagen JPEG en Base64 y volcar el resultado en un string
        byte[] bytesImagen = stream.toByteArray();
        String imagen_base64 = Base64.encodeToString(bytesImagen, Base64.DEFAULT);

        return imagen_base64;
    }


    public static Bitmap descodifica_imagen_base64(String imagen_base64)
    {
        // Descodificar la imagen JPEG en Base64 y volcar el resultado en un array de bytes
        // (devolver null si no se pudo decodificar la cadena)
        byte[] bytesImagen;

        try
        {
            bytesImagen = Base64.decode(imagen_base64, Base64.DEFAULT);
        }
        catch (Exception e)
        {
            return null;
        }

        // Construir un objeto Bitmap con los datos descodificados
        // (devuelve null si no se pudo construir el bitmap)
        Bitmap imagen = BitmapFactory.decodeByteArray(bytesImagen, 0, bytesImagen.length);

        return imagen;
    }


    public static String idiomaAplicacion()
    {
        return Locale.getDefault().getLanguage();
    }


    public static String distanciaUnidadesLocales(Context ctx, double distMillas)
    {
        if ( distMillas < 0 )   return "n/d";

        String idioma = idiomaAplicacion();
        String unidades = ctx.getResources().getString(R.string.general_txt_unidadDistancia) + "."; // km. , mi. , etc...

        int dist;

        // Si el idioma es español, pasar la distancia a kilómetros
        if ( idioma.equals("es") )
        {
            Double distKM = distMillas * 1.609344;
            dist = (int) Math.round(distKM);
        }

        // En caso contrario, mantenemos la distancia en millas
        else
        {
            unidades = "mi.";
            dist = (int) Math.round(distMillas);
        }

        // Si tras redondear la distancia sale cero, redondeamos a 1
        if ( dist == 0 )    dist = 1;

        String textoDistancia = "~" + Integer.toString(dist) + " " + unidades;
        return textoDistancia;
    }


    public static boolean direccionEmailValida(String email)
    {
        boolean valida = false;

        String expresion = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence secuencia = email;

        Pattern patron = Pattern.compile(expresion, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(secuencia);

        if ( matcher.matches() )
        {
            valida = true;
        }

        return valida;
    }


    public static boolean urlValida(String url)
    {
        return Patterns.WEB_URL.matcher( url.toLowerCase() ).matches();
    }


    public static boolean passwordValida(String pass)
    {
        boolean valida = (pass.length() >= 6) && (pass.length() <= 16);

        return valida;
    }


    public static boolean esEntero(String num)
    {
        try
        {
            Integer.parseInt(num);
        }
        catch(NumberFormatException e)
        {
            return false;
        }
        catch(NullPointerException e)
        {
            return false;
        }

        return true;
    }


    public static boolean esDoble(String num)
    {
        try
        {
            Double.parseDouble(num);
        }
        catch(NumberFormatException e)
        {
            return false;
        }
        catch(NullPointerException e)
        {
            return false;
        }

        return true;
    }


    public static boolean esBooleano(String b)
    {
        if ( b.equalsIgnoreCase("true") || b.equalsIgnoreCase("false") )
            return true;
        else
            return false;
    }


    public static String formatearFechaVisible(String fechaOriginal, String idioma)
    {
        String fechaFormateada;
        SimpleDateFormat formatoOriginal, formatoNuevo;

        formatoOriginal = new SimpleDateFormat("yyyy-MM-dd");

        if ( idioma.equalsIgnoreCase("es") )
            formatoNuevo = new SimpleDateFormat("dd/MM/yyyy");
        else
            formatoNuevo = new SimpleDateFormat("yyyy-MM-dd");

        try
        {
            fechaFormateada = formatoNuevo.format(formatoOriginal.parse(fechaOriginal));
        }
        catch (ParseException e)
        {
            fechaFormateada = "";
        }

        return fechaFormateada;
    }


    public static SimpleDateFormat nuevoSimpleDateFormat(String idioma)
    {
        String formatoFecha;

        if ( idioma.equalsIgnoreCase("es") )
            formatoFecha = "dd/MM/yyyy";
        else
            formatoFecha = "yyyy-MM-dd";

        return new SimpleDateFormat(formatoFecha);
    }


    public static String formatearFechaBBDD(String fechaOriginal, String idioma)
    {
        String fechaFormateada;
        SimpleDateFormat formatoOriginal, formatoBBDD;

        if ( idioma.equalsIgnoreCase("es") )
            formatoOriginal = new SimpleDateFormat("dd/MM/yyyy");
        else
            formatoOriginal = new SimpleDateFormat("yyyy-MM-dd");

        formatoBBDD = new SimpleDateFormat("yyyy-MM-dd");

        try
        {
            fechaFormateada = formatoBBDD.format(formatoOriginal.parse(fechaOriginal));
        }
        catch (ParseException e)
        {
            fechaFormateada = "";
        }

        return fechaFormateada;
    }


    public static boolean validarFecha(String fecha, String idioma)
    {
        Date date = null;
        String formato;

        if ( idioma.equalsIgnoreCase("es") )
            formato = "dd/MM/yyyy";
        else
            formato = "yyyy-MM-dd";

        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(formato);
            date = sdf.parse(fecha);

            if (!fecha.equals(sdf.format(date)))
                date = null;
        }
        catch (ParseException e)
        {
            Log.e("validarFecha", e.toString());
        }

        return (date != null);
    }


    public static BigDecimal parsearCantidadMonetaria(String cantidad, String idioma)
    {
        DecimalFormat formateador;
        String numeroFormateado;


        // Si se usa español, se supone un formato #.###,##
        // (eliminar los puntos de separación de miles y convertir a formato inglés)
        if (idioma.equalsIgnoreCase("es"))
        {
            cantidad.replaceAll("." , "");
            cantidad.replaceAll("," , ".");
        }


        // Si se usa otro idioma, se supone un formato #,###.##
        // (eliminar las comas de separación de miles)
        else
        {
            cantidad.replaceAll("," , "");
        }


        BigDecimal numero;

        try
        {
            numero = new BigDecimal(cantidad);
        }
        catch (NumberFormatException ex)
        {
            numero = null;
        }

        return numero;
    }


    public static String formatearReal(double num, String idioma)
    {
        NumberFormat formateador;
        String numeroFormateado;

        if ( idioma.equalsIgnoreCase("es") )
            formateador = NumberFormat.getNumberInstance( new Locale("es","ES") );
        else
            formateador = NumberFormat.getNumberInstance( Locale.US );

        numeroFormateado = formateador.format(num);

        return  numeroFormateado;
    }


    private static String comprobarDosDecimales(String num, String separador)
    {
        String res = num;
        String[] temp = num.split(separador);

        // Si el número tiene una parte decimal
        if ( temp.length == 2 )
        {
            // Si la parte decimal del número solo tiene una cifra, añadir un cero más a la derecha
            if (temp[1].length() == 1)
                res += "0";
        }

        return res;
    }


    public static String formatearCantidadMonetaria(BigDecimal num, String simboloMoneda, String idioma)
    {
        DecimalFormat formateador;
        String numeroFormateado;

        if (idioma.equalsIgnoreCase("es"))
        {
            //formateador = NumberFormat.getNumberInstance(new Locale("es", "ES"));
            DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(new Locale("es", "ES"));
            formateador = new DecimalFormat("#,###.##",simbolos);

            numeroFormateado = formateador.format(num);

            return comprobarDosDecimales( numeroFormateado , "," ) + simboloMoneda;
        }

        else
        {
            //formateador = NumberFormat.getNumberInstance(Locale.US);
            DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(Locale.US);
            formateador = new DecimalFormat("#,###.##",simbolos);

            numeroFormateado = formateador.format(num);

            return simboloMoneda + comprobarDosDecimales( numeroFormateado , "\\." );
        }

    }


    public static int redondearReal(double num)
    {
        double numAbsoluto = Math.abs(num);

        int i = (int) numAbsoluto;
        double dif = numAbsoluto - (double) i;

        // Si la parte decimal del número (en valor absoluto) era menor que 0,5 redondeamos al valor de la parte entera
        // y devolvemos ese entero con el signo correspondiente
        if ( dif < 0.5 )
        {
            return (num<0)? -i : i;
        }

        // Si la parte decimal del número (en valor absoluto) es mayor o igual que 0,5 redondeamos al valor de la parte entera + 1
        // y devolvemos ese entero con el signo correspondiente
        else
        {
            return (num<0)? -(i+1) : i+1;
        }
    }


    public static String crearUrl(String protocolo, String dominio, String puerto, String carpeta, String servicio)
    {
        String url = protocolo+"://"+dominio+":"+puerto+"/"+carpeta+"/"+servicio;
        return url;
    }


    // Determina si el dispositivo está conectado a alguna red
    public static boolean dispositivoConectado(ConnectivityManager cm)
    {
        boolean conectado;

        NetworkInfo infoRedMobil    = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo infoRedWifi     = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected() )
        if ( infoRedMobil != null && infoRedMobil.isConnectedOrConnecting() || infoRedWifi != null && infoRedWifi.isConnectedOrConnecting() )
        {
            conectado = true;
        }
        else
        {
            conectado = false;
        }

        return conectado;
    }


    // Intenta abrir una aplicación externa con los parámetros indicados en la lista, según este órden de parámetros:
    //
    //      LLAMADA -----------> 0: número a marcar
    //      CORREO ------------> 0: destinatario del correo, 1: asunto del correo
    //      NAVEGADOR ---------> 0: url a abrir
    //      MAPA_COORDENADAS --> 0: latitud, 1: longitud, 2: etiqueta a mostrar en el mapa (si no es la cadena vacía)
    //      MAPA_DIRECCION ----> 0: dirección a buscar
    //
    // Si el tipo de aplicación indicado no existe, o si no hay ninguna aplicación instalada en el dispositivo para ese tipo,
    // muestra un mensaje de error al usuario y finaliza sin hacer nada
    public static void aplicacionExterna(Context ctx, AppExterna app, ArrayList<String> params)
    {
        // Comprobar que la lista tenga al menos un parámetro
        if ( params.size()<1 )
            return;


        Intent appExterna = null;
        String msgError = "";


        // Preparar el intent de la aplicación externa correspondiente
        switch (app)
        {
            // Aplicación para envío de llamadas
            case LLAMADA:

                String numero = params.get(0).trim();

                appExterna = new Intent(Intent.ACTION_DIAL);
                appExterna.setData(Uri.parse("tel:" + numero));

                msgError = ctx.getResources().getString( R.string.Utils_aplicacionExterna_txt_msgSinLlamadas);
                break;


            // Aplicación para envío de correos
            case CORREO:

                if ( params.size()<2 )
                    return;

                String destinatario = params.get(0).trim();
                String asunto       = params.get(1).trim();

                appExterna = new Intent(Intent.ACTION_SENDTO);
                appExterna.setData( Uri.parse("mailto:" + destinatario + "?subject=" + asunto));

                msgError = ctx.getResources().getString( R.string.Utils_aplicacionExterna_txt_msgSinCorreos);
                break;


            // Aplicación para URLs
            case NAVEGADOR:

                String url = params.get(0).trim();

                if ( !url.startsWith("http://" ) && !url.startsWith("https://") )
                    url = "http://" + url;

                appExterna = new Intent(Intent.ACTION_VIEW);
                appExterna.setData(Uri.parse(url));

                msgError = ctx.getResources().getString( R.string.Utils_aplicacionExterna_txt_msgSinNavegador);
                break;


            // Aplicación para mapas (latitud,longitud)
            case MAPA_COORDENADAS:

                if ( params.size()<3 )
                    return;

                String latitud  = params.get(0);
                String longitud = params.get(1);
                String etiqueta = params.get(2).trim();

                Uri uri;

                // Inicialmente, intentaremos abrir el mapa con alguna aplicación de mapas (Google Maps o similar)
                if ( etiqueta.equals("") )
                    uri = Uri.parse("geo:0,0?q=" + latitud + "," + longitud);
                else
                    uri = Uri.parse("geo:0,0?q=" + latitud + "," + longitud + "(" + Uri.encode(etiqueta) + ")");

                appExterna = new Intent( Intent.ACTION_VIEW, uri );

                // Si se quiere forzar el uso de Google Maps, descomentar la siguiente línea
                //appExterna.setPackage("com.google.android.apps.maps");

                // Si el sistema no puede identificar una app que abra este intent,
                // cambiar el intent para intentar abrir el mapa en el navegador (o similar)
                // (En este caso, la etiqueta no está soportada)
                if ( appExterna.resolveActivity(ctx.getPackageManager()) == null )
                {
                    appExterna = new Intent( android.content.Intent.ACTION_VIEW , Uri.parse("http://maps.google.com/maps?q=loc:"+ latitud +","+ longitud) );
                }

                msgError = ctx.getResources().getString( R.string.Utils_aplicacionExterna_txt_msgSinMapas);
                break;


            // Aplicación para mapas GMAPS (direccion: calle, número, población, etc)
            case MAPA_DIRECCION:

                String direccion  = params.get(0).trim();
                //direccion = direccion.replaceAll("[(|)]", " ");

                // Inicialmente, intentaremos abrir el mapa con alguna aplicación de mapas (Google Maps o similar)
                uri = Uri.parse( "geo:0,0?q="+ Uri.encode(direccion) );

                appExterna = new Intent( Intent.ACTION_VIEW, uri );

                // Si se quiere forzar el uso de Google Maps, descomentar la siguiente línea
                //appExterna.setPackage("com.google.android.apps.maps");

                // Si el sistema no puede identificar una app que abra este intent,
                // cambiar el intent para intentar abrir el mapa en el navegador (o similar)
                // (En este caso, la etiqueta no está soportada)
                if ( appExterna.resolveActivity(ctx.getPackageManager()) == null )
                {
                    appExterna = new Intent( android.content.Intent.ACTION_VIEW , Uri.parse("http://www.google.com/maps/search/"+ Uri.encode(direccion)) );
                }

                msgError = ctx.getResources().getString( R.string.Utils_aplicacionExterna_txt_msgSinMapas);
                break;


            // Otra aplicación no contemplada
            default:
                break;
        }


        // Si se preparó el intent para un tipo de aplicación conocida, intentar ejecutarlo
        // (si no hay ninguna aplicación en el sistema que responda a esa llamada, capturar la excepción y mostrar mensaje de error)
        if (appExterna != null)
        {
            try
            {
                ctx.startActivity(appExterna);
            }
            catch (ActivityNotFoundException ex)
            {
                Utils.mostrarMensaje(ctx, msgError, Utils.TipoMensaje.TOAST, null, null);
            }
        }

        // Si el tipo de aplicación externa no es conocido, mostrar un mensaje de error
        else
            Utils.mostrarMensaje(ctx, msgError, Utils.TipoMensaje.TOAST, null, null);


        return;
    }


    // Construye un objeto LatLngBounds para centrar la vista cuando se use el widget Places Picker
    public static LatLngBounds limitesMapa(LatLng centro, double radio)
    {
        LatLng esquinaSurOeste = SphericalUtil.computeOffset(centro, radio * Math.sqrt(2.0), 225);
        LatLng esquinaNorEste  = SphericalUtil.computeOffset(centro, radio * Math.sqrt(2.0), 45);

        return new LatLngBounds(esquinaSurOeste,esquinaNorEste);
    }


    // Devuelve un string "latitud,longitud" con las coordenadas actuales del dispositivo
    public static String ubicacionActual(Context ctx, SimpleLocation ubicacion)
    {
        final double latitud  = ubicacion.getLatitude();
        final double longitud = ubicacion.getLongitude();

        String coordenadas = Double.toString(latitud) +","+ Double.toString(longitud);

        Log.d("Utils.ubicacionActual", "Coordenadas: " + coordenadas);

        // Para debug con el emulador, es posible que no detecte la ubicación correctamente
        // devolvemos unas coordenadas predeterminadas
        if ( coordenadas.equals("0.0,0.0") && BuildConfig.DEBUG )
            coordenadas="43.523502,-5.630577";

        return coordenadas;
    }


    public static String obtenerDireccion(double lat, double lon, Context ctx)
    {
        String direccion;
        Geocoder miGeocoder = new Geocoder(ctx);

        try
        {
            List<Address> direcciones = miGeocoder.getFromLocation(lat,lon, 1);

            if( direcciones != null && direcciones.size() > 0 )
            {
                Address fetchedAddress = direcciones.get(0);

                StringBuilder strAddress = new StringBuilder();
                for(int i=0; i<fetchedAddress.getMaxAddressLineIndex(); i++)
                {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append("\n");
                }

                direccion = strAddress.toString();
            }

            else
                direccion = null;
        }
        catch (IOException e)
        {
            direccion = null;
        }
        catch (IndexOutOfBoundsException e)
        {
            direccion = null;
        }

        return direccion;
    }


    public static int construirTablaEspecialidades(VentanaBase ctx, LinearLayout tabla, ArrayList<ActividadObra> actividades, boolean opcionEliminar)
    {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Comprobar si la lista contiene algún tipo de obra
        int total = Utils.contarEspecialidades(actividades);

        // Si no contiene ninguno (o si la lista no está bien construída), devolver 0 y salir sin hacer nada
        if ( total == 0 )
            return total;

        // Vaciar todos los elementos del LinearLayout (por si ya tuviera alguno previamente)
        tabla.removeAllViews();

        // Para mostrar el botón de eliminar tipo, el contexto tiene que ser una ventana del tipo VentanaPerfilProfesional
        final VentanaPerfilProfesional ventana;
        if( ctx instanceof VentanaPerfilProfesional )
            ventana = (VentanaPerfilProfesional) ctx;
        else
            ventana = null;

        // Recorrer las listas de actividades, categorías y tipos e ir construyendo la tabla
        for (int i=0; i<actividades.size(); i++)
        {
            ActividadObra act = actividades.get(i);

            View filaActividad = inflater.inflate(R.layout.elemento_especialidad_actividad, null, false);
            TextView nombreActividad = (TextView) filaActividad.findViewById(R.id.txt_nombreActividad);
            nombreActividad.setText(act.getNombre());

            tabla.addView(filaActividad);

            ArrayList<CategoriaObra> categorias = act.getCategorias();
            for (int j = 0; j < categorias.size(); j++)
            {
                CategoriaObra cat = categorias.get(j);

                View filaCategoria = inflater.inflate(R.layout.elemento_especialidad_categoria, null, false);
                TextView nombreCategoria = (TextView) filaCategoria.findViewById(R.id.txt_nombreCategoria);
                nombreCategoria.setText(cat.getNombre());

                tabla.addView(filaCategoria);

                ArrayList<TipoObra> tipos = cat.getTipos();
                for (int k = 0; k < tipos.size(); k++)
                {
                    TipoObra tip = tipos.get(k);

                    View filaTipo = inflater.inflate(R.layout.elemento_especialidad_tipo, null, false);
                    TextView nombreTipo = (TextView) filaTipo.findViewById(R.id.txt_nombreTipo);
                    nombreTipo.setText(tip.getNombre());

                    // Referencia al botón de eliminar tipo de obra
                    ImageButton btnEliminarTipo = (ImageButton) filaTipo.findViewById(R.id.btn_eliminarTipo);

                    // Si hay que mostrar el botón de eliminar tipo (y el contexto es una ventana del tipo adecuado),
                    // entonces guardar en el propio objeto botón las posiciones en la lista de Actividad/Categoría/Tipo
                    // y definir el comportamiento del botón al pulsarlo
                    if (opcionEliminar && ventana != null)
                    {
                        btnEliminarTipo.setVisibility(View.VISIBLE);

                        btnEliminarTipo.setTag(R.id.TAG_POS_ACTIVIDAD, new Integer(i) );
                        btnEliminarTipo.setTag(R.id.TAG_POS_CATEGORIA, new Integer(j) );
                        btnEliminarTipo.setTag(R.id.TAG_POS_TIPO, new Integer(k) );

                        btnEliminarTipo.setOnClickListener
                        (
                            new View.OnClickListener()
                            {
                                public void onClick(View v)
                                {
                                    int posActividad = ((Integer) v.getTag(R.id.TAG_POS_ACTIVIDAD)).intValue();
                                    int posCategoria = ((Integer) v.getTag(R.id.TAG_POS_CATEGORIA)).intValue();
                                    int posTipo = ((Integer) v.getTag(R.id.TAG_POS_TIPO)).intValue();

                                    ventana.eliminarEspecialidad(posActividad, posCategoria, posTipo);

                                    // Mostrar un mensaje de confirmación al usuario
                                    String msg = ventana.getResources().getString(R.string.Utils_eliminarEspecialidad_txt_msgConfirmacion);
                                    Utils.mostrarMensaje(ventana,msg,TipoMensaje.TOAST,null,null);
                                }
                            }
                        );
                    }
                    // En caso contrario, el botón de eliminar tipo de obra ni se utiliza ni se muestra
                    else
                        btnEliminarTipo.setVisibility(View.GONE);


                    tabla.addView(filaTipo);
                }
            }

        }

        // Por ultimo, devolver la cantidad de tipos de obra que contiene la lista
        return total;
    }


    public static int contarEspecialidades(ArrayList<ActividadObra> actividades)
    {
        int total = 0;
        boolean error = false;

        if ( actividades==null )
            error = true;

        for (int i=0; !error && i<actividades.size(); i++)
        {
            ActividadObra act = actividades.get(i);

            ArrayList<CategoriaObra> categorias = act.getCategorias();
            if ( categorias==null )
                error = true;

            for (int j = 0; !error && j < categorias.size(); j++)
            {
                CategoriaObra cat = categorias.get(j);

                ArrayList<TipoObra> tipos = cat.getTipos();
                if ( tipos==null )
                    error = true;
                else
                    total += tipos.size();
            }
        }

        if ( error )
            return 0;
        else
            return total;
    }


    public static void agregarEspecialidad(ArrayList<ActividadObra> actividades, Info_ElementoSpinner actividad, Info_ElementoSpinner categoria, ArrayList<Info_ElementoSpinner> tipos)
    {
        int i,j;

        // Comprobar si la actividad indicada ya existe en la lista
        boolean esActividadNueva = true;

        for (i=0; esActividadNueva && i<actividades.size(); i++)
        {
            if ( actividad.getClave().equals(actividades.get(i).getId()) )
                esActividadNueva = false;
        }

        // Si la actividad es nueva, la categoría y el/los tipo/s de obra también lo serán
        if (esActividadNueva)
        {
            // Crear la lista con el/los nuevo/s tipo/s de obra
            ArrayList<TipoObra> nuevaListaTipos = new ArrayList();

            for (int n=0; n<tipos.size(); n++)
            {
                TipoObra t = new TipoObra( tipos.get(n).getClave(), tipos.get(n).getValor() );
                nuevaListaTipos.add(t);
            }

            // Crear la nueva categoría conteniendo la lista de tipos nuevos
            CategoriaObra c = new CategoriaObra( categoria.getClave() , categoria.getValor() , nuevaListaTipos );

            // Crear la nueva actividad conteniendo a la categoría nueva
            ArrayList<CategoriaObra> nuevaListaCategorias = new ArrayList();
            nuevaListaCategorias.add(c);

            ActividadObra a = new ActividadObra( actividad.getClave() , actividad.getValor() , nuevaListaCategorias );

            // Añadir la nueva Actividad a las actividades ya seleccinadas previamente y salir
            actividades.add(a);

            Log.d("agregarEspecialidad()","Añadida nueva actividad a la lista: " + a.getId() + " '" + a.getNombre() + "'");
        }

        // si la actividad no es nueva
        else
        {
            boolean esCategoriaNueva = true;

            ActividadObra a = actividades.get(i-1);

            // Comprobar si la categoría indicada ya existe dentro de esa actividad
            for (j=0; esCategoriaNueva && j<a.getCategorias().size(); j++)
            {
                if ( categoria.getClave().equals( a.getCategorias().get(j).getId() ) )
                    esCategoriaNueva = false;
            }

            // Si la categoría es nueva, el/los tipo/s de obra también lo serán
            if (esCategoriaNueva)
            {
                // Crear la lista con el/los nuevo/s tipo/s de obra
                ArrayList<TipoObra> nuevaListaTipos = new ArrayList();

                for (int n=0; n<tipos.size(); n++)
                {
                    TipoObra t = new TipoObra( tipos.get(n).getClave(), tipos.get(n).getValor() );
                    nuevaListaTipos.add(t);
                }

                // Crear la nueva categoría conteniendo la lista de tipos nuevos
                CategoriaObra c = new CategoriaObra( categoria.getClave() , categoria.getValor() , nuevaListaTipos );

                // Añadir la nueva categoría a la actividad ya seleccionada previamente y salir
                a.getCategorias().add(c);

                Log.d("agregarEspecialidad()", "Añadida nueva categoría: " + c.getId() + " '" + c.getNombre() + "' a la actividad existente " + actividades.get(i-1).getId() + " '"  + actividades.get(i-1).getNombre() + "'" );
            }

            // Si la categoría no es nueva, añadir el/los nuevo/s tipo/s de obra a la categoría
            // (siempre que no estuvieran ya contenidos en dicha categoría) y salir
            else
            {
                CategoriaObra c = a.getCategorias().get(j-1);

                for (int n=0; n<tipos.size(); n++)
                {
                    String idTipo = tipos.get(n).getClave();
                    String nombreTipo = tipos.get(n).getValor();

                    if ( !existeTipoEnCategoria(idTipo,c) )
                    {
                        TipoObra t = new TipoObra(idTipo,nombreTipo);
                        c.getTipos().add(t);

                        Log.d("agregarEspecialidad()", "Añadido nuevo tipo: " + t.getId() + " '" + t.getNombre() + "' a la categoría existente " + c.getId() + " '" + c.getNombre() + "'");
                    }
                }
            }
        }

    }


    // Devuelve un string con los id de tipos de obra seleccionados como especialidades en la lista indicada
    //
    // Posibles valores devueltos:
    //  null    --->    si hay algún error en la lista
    //  ""    --->    si la lista está vacía (no se seleccionó ninguna especialidad)
    //  cadena de la forma "idTipoObra1,idTipoObra2,idTipoObra3,..." en el resto de casos
    public static String listadoEspecialidades(ArrayList<ActividadObra> actividades)
    {
        ArrayList<String> tiposSeleccionados = new ArrayList();
        boolean error = false;

        if ( actividades==null )
            error = true;

        for (int i=0; !error && i<actividades.size(); i++)
        {
            ActividadObra act = actividades.get(i);

            ArrayList<CategoriaObra> categorias = act.getCategorias();
            if ( categorias==null )
                error = true;

            for (int j=0; !error && j<categorias.size(); j++)
            {
                CategoriaObra cat = categorias.get(j);

                ArrayList<TipoObra> tipos = cat.getTipos();
                if ( tipos==null )
                    error = true;

                for (int k=0; !error && k<tipos.size(); k++)
                {
                    TipoObra tip = tipos.get(k);

                    tiposSeleccionados.add( tip.getId() );
                }
            }
        }

        if ( error )
            return null;
        else if ( tiposSeleccionados.size()==0 )
            return "";
        else
        {
            String res = tiposSeleccionados.get(0);

            for (int n=1; n<tiposSeleccionados.size(); n++)
                res += "," + tiposSeleccionados.get(n);

            return res;
        }
    }


    private static boolean existeTipoEnCategoria(String idTipo, CategoriaObra cat)
    {
        boolean existe = false;

        for (int i=0; !existe && i<cat.getTipos().size(); i++)
        {
            if ( idTipo.equals(cat.getTipos().get(i).getId()) )
                existe = true;
        }

        return existe;
    }


    private static File getTempFile()
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            File file = new File(Environment.getExternalStorageDirectory(), TEMP_FILENAME);
            return file;
        }
        else
        {
            return null;
        }
    }


    public static Uri getTempUri()
    {
        File tmpFile = getTempFile();

        if ( tmpFile == null )
            return null;
        else
            return Uri.fromFile( tmpFile );
    }


    public static boolean removeTempFile()
    {
        return getTempFile().delete();
    }




}
