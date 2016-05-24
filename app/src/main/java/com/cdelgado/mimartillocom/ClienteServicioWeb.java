package com.cdelgado.mimartillocom;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.util.ArrayList;


public class ClienteServicioWeb
{
    // Password para acceder al almacén de certificados
    private static final String KEYSTORE_PWD    = "passKeyStore2015";

    // Codificación de caracteres de los datos recibidos del servidor
    private static final String JUEGO_CARACTERES = "UTF-8";

    // Etiquetas para discriminar los campos de los objetos json recibidos
    private static final String TAG_RESULTADO   = "resultado";
    private static final String TAG_DETALLE     = "detalle";
    private static final String TAG_CONTENIDO   = "contenido";


    private Context ctx;             // Necesario para acceder a los recursos de la aplicación (acceso al recurso keystore para https)
    private ServicioWeb servicioWeb; // Servicio web asociado a este cliente

    private boolean timeout;         // Indicador de si la operación de red produjo algún timeout

    // Tiempos de timeout, en ms
    private int timeoutConexion;     // ---> org.apache.http.conn.ConnectTimeoutException
    private int timeoutSocket;       // ---> java.net.SocketTimeoutException



    // Constructor con tiempos de timeout de red por defecto (fichero de recursos network.xml)
    public ClienteServicioWeb(Context cont, ServicioWeb serv)
    {
        ctx             = cont;
        servicioWeb     = serv;

        timeoutConexion = ctx.getResources().getInteger( R.integer.DEFAULT_CONNECTION_TIMEOUT );
        timeoutSocket   = ctx.getResources().getInteger( R.integer.DEFAULT_SOCKET_TIMEOUT );
    }


    // Constructor con tiempos de timeout de red personalizados (en ms.)
    public ClienteServicioWeb(Context cont, ServicioWeb serv, int tmtCon, int tmtSoc)
    {
        ctx             = cont;
        servicioWeb     = serv;

        timeoutConexion = tmtCon;
        timeoutSocket   = tmtSoc;
    }


    public boolean timeout()
    {
        return timeout;
    }


    public ServicioWeb getServicioWeb()
    {
        return servicioWeb;
    }


    // Realiza una consulta Http(s) POST/GET al servicio web correspondiente
    // (si hubo algún error o se produjo un timeout, devuelve false. En caso contrario, devuelve true)
    public boolean peticionServicioWeb()
    {
        // Si se trata de un servicio de prueba (que ya incluye una respuesta)
        // entonces no se envía ninguna petición por red, sino que directamente devolvemos true.
        // ---------------------------------------------------------------------------------------
        if ( servicioWeb.esDePrueba() )
        {
            // Información de debug sobre el servicio web de prueba
            String httpMethod = "";
            if( servicioWeb.getMetodo() == ServicioWeb.Metodo.POST )     httpMethod = "POST";
            else if( servicioWeb.getMetodo() == ServicioWeb.Metodo.GET ) httpMethod = "GET";
            Log.d("Cliente Servicio Web", "Simulando petición " + httpMethod + " a: "+servicioWeb.getUrl());
            Log.d("Cliente Servicio Web", "Nº parámetros petición simulada: " + Integer.toString(servicioWeb.getParametros().size()) );

            if ( servicioWeb.getParametros().size() > 0 )
                for (int i=0; i<servicioWeb.getParametros().size(); i++)
                {
                    NameValuePair p = servicioWeb.getParametros().get(i);
                    Log.d("Cliente Servicio Web", "'" + p.getName() + "' : '" + p.getValue() + "'");
                }

            // Información de debug sobre la respuesta simulada
            Log.d("Cliente Servicio Web","Respuesta simulada: " + servicioWeb.getRespuesta().toString());


            // Devolvemos el control indicando que la operación fue correctamente, y sin haber hecho ninguna petición por red
            return true;
        }


        // Si no se trata de un servicio de prueba, procedemos con la petición por red
        // ---------------------------------------------------------------------------------------

        InputStream is  = null;  // Flujo de bytes recibido del servidor
        JSONObject jObj = null;  // Objeto JSON parseado a partir de la respuesta del servidor


        // Hacer la consulta y obtener un objeto InputStream
        try
        {
            timeout = false;

            // Parametros de la conexión (tiempos de timeout)
            HttpParams parametrosConexion = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(parametrosConexion, timeoutConexion);
            HttpConnectionParams.setSoTimeout(parametrosConexion, timeoutSocket);


            // SSLsocketFactory personalizada que usaremos para verificar el/los certificado/s del servidor (para conexiones HTTPS)
            SSLSocketFactory miSSLsocketFactory;

            try
            {
                // Objeto keystore que manejaremos, con el formato Bouncy Castle KeyStore
                KeyStore trusted = KeyStore.getInstance("BKS");

                // Acceso al fichero que contiene el keystore con el/los certificado/s del servidor
                InputStream file = ctx.getResources().openRawResource(R.raw.mykeystore);

                // Cargar el objeto keystore con el/los certificado/s del servidor (usando la passowrd con la que se creó el fichero)
                try
                {
                    trusted.load(file,KEYSTORE_PWD.toCharArray());
                }
                finally
                {
                    file.close();
                }

                // Pasar el objeto keystore a nuestro SSLfactory personalizado
                miSSLsocketFactory = new SSLSocketFactory(trusted);

                // Verificación del hostname con el certificado (http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506)
                miSSLsocketFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            }
            catch (Exception e)
            {
                Log.e("Cliente Servicio Web", "Error al construir SSLsocketFactory - " + e.toString());
                return false;
            }

            // Esquemas de conexión HTTP y HTTPS (usando la SSLsocketFactory personalizada) para el cliente
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register( new Scheme("http",PlainSocketFactory.getSocketFactory(),80) );
            schemeRegistry.register( new Scheme("https",miSSLsocketFactory,443) );

            ClientConnectionManager connManager = new SingleClientConnManager(parametrosConexion,schemeRegistry);


            // Si el método de consulta es POST
            if( servicioWeb.getMetodo() == ServicioWeb.Metodo.POST )
            {
                Log.d("Cliente Servicio Web", "Haciendo petición POST a: " + servicioWeb.getUrl());

                // Construir el cliente y la petición http - post
                DefaultHttpClient   httpClient  = new DefaultHttpClient(connManager,parametrosConexion);
                HttpPost            httpPost    = new HttpPost( servicioWeb.getUrl() );

                // Añadir a la petición los parámetros POST que se van a enviar (codificados en UTF-8)
                httpPost.setEntity( new UrlEncodedFormEntity( servicioWeb.getParametros() , HTTP.UTF_8 ) );

                // Ejecutar la petición http (post) y almacenar la respuesta
                HttpResponse httpResponse = httpClient.execute(httpPost);

                // Extraer los datos de la respuesta y abrir un InputStream con el contenido de la respuesta
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

            // Si el método de consulta es GET
            else if( servicioWeb.getMetodo() == ServicioWeb.Metodo.GET )
            {
                Log.d("Cliente Servicio Web", "Haciendo petición GET a: "+servicioWeb.getUrl());

                // Construir el cliente http
                DefaultHttpClient httpClient = new DefaultHttpClient(connManager,parametrosConexion);    //para HTTPS

                // Construir una cadena con la lista de parámetros formateada para usar en una url (codificados en UTF-8)
                String paramString = URLEncodedUtils.format( servicioWeb.getParametros(), HTTP.UTF_8 );

                // Añadir a la url base el ? y la lista de parámetros (clave=valor)
                String urlCompleta = servicioWeb.getUrl() + "?" + paramString;

                // Construir la petición http - get (incluidos los parametros que se enviarán)
                HttpGet httpGet = new HttpGet(urlCompleta);

                // Ejecutar la petición http (get) y almacenar la respuesta
                HttpResponse httpResponse = httpClient.execute(httpGet);

                // Extraer los datos de la respuesta y abrir un InputStream con el contenido de la respuesta
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

            // Si es cualquier otro método no contemplado
            else
            {
                Log.e("Cliente Servicio Web", "Error en la consulta - Método desconocido");
                return false;
            }
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("Cliente Servicio Web", "Error en la consulta - " + e.toString());
            return false;
        }
        catch (ClientProtocolException e)
        {
            Log.e("Cliente Servicio Web", "Error en la consulta - " + e.toString());
            return false;
        }
        catch (ConnectTimeoutException e)
        {
            Log.e("Cliente Servicio Web", "Error en la consulta - " + e.toString());
            timeout = true;
            return false;
        }
        catch (SocketTimeoutException e)
        {
            Log.e("Cliente Servicio Web", "Error en la consulta - " + e.toString());
            timeout = true;
            return false;
        }
        catch (SocketException e)
        {
            Log.e("Cliente Servicio Web", "Error en la consulta - " + e.toString());
            timeout = true;
            return false;
        }
        catch (IOException e)
        {
            Log.e("Cliente Servicio Web", "Error en la consulta - " + e.toString());
            return false;
        }

        // Construir un String con los datos del InputStream obtenido
        String jsonString = "";

        try
        {
            // BufferedReader para parsear el InputStream obtenido
            //BufferedReader reader = new BufferedReader( new InputStreamReader(is,"iso-8859-1"),8 );
            BufferedReader reader = new BufferedReader( new InputStreamReader(is,JUEGO_CARACTERES) , 256 );

            // StringBuilder para usar durante el parseo
            StringBuilder sb = new StringBuilder();

            // String para guardar los datos del objeto JSON en formato cadena
            String line = null;

            // Leer todas las líneas del InputStream y añadirlas al StringBuilder
            // (separadas por un salto de línea)
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }

            // Cerrar el InputStream
            is.close();

            // // Convertir los datos del StringBuilder a String
            jsonString = sb.toString();
        }
        catch (Exception e)
        {
            Log.e("Cliente Servicio Web", "Error al construir el objeto JSON - " + e.toString());
            jObj = null;
        }

        // Intentar parsear el String a un objeto JSON
        // Si el string no está correctamente construido, se producirá la excepción JSONObject
        try
        {
            jObj = new JSONObject( jsonString );
        }
        catch (JSONException e)
        {
            Log.e("Cliente Servicio Web", "Error al parsear los datos JSON: " + e.toString());
            jObj = null;
        }

        // Transformar el objeto JSON obtenido del servidor en un objeto RespuestaServicioWeb
        // del tipo correspondiente al servicio web consultado
        // (si la respuesta del servidor no es correcta/está malformada, devolverá null)
        RespuestaServicioWeb respuestaObtenida = transformarJSON(jObj);


        // Añadir la respuesta obtenida al servicio web para que sea manipulada por la aplicación
        servicioWeb.setRespuesta(respuestaObtenida);
        return true;
    }


    // Convierte un objeto JSON recibido en un objeto del tipo RespuestaServicioWeb
    // con el contenido correspondiente al servicio web que se consultó.
    // Si el objeto JSON indicado es null o el tipo indicado no es correcto, devuelve null.
    // (método de clase privado)
    private RespuestaServicioWeb transformarJSON(JSONObject json)
    {
        if (json==null)
            return null;


        RespuestaServicioWeb resp;

        try
        {
            // Obtener los valores de los campos "resultado" y "detalle" del objeto JSON
            String resultado = json.getString(TAG_RESULTADO);
            String detalle   = json.getString(TAG_DETALLE);

            // El campo CONTENIDO del objeto JSON es siempre es un array de otros objetos JSON
            JSONArray contenido = new JSONArray ( json.getString(TAG_CONTENIDO) );

            // Lista para almacenar los objetos del array de CONTENIDO
            ArrayList<ContenidoServicioWeb> listaContenido = new ArrayList<>();


            // Si el array de CONTENIDO contiene elementos, transformar cada uno en el correspondiente objeto ContenidoServicioWeb
            // (será diferente dependiendo del tipo de respuesta) y añadirlo a la lista
            switch ( servicioWeb.getTipo() )
            {
                // Respuesta para una consulta al servicio de inicio de sesión
                // ------------------------------------------------------------------------------------------------------------
                case LOGIN:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO contiene solo un elemento
                    else if ( contenido.length() == 1 )
                    {
                        // Datos del único elemento del array JSON
                        JSONObject temp = contenido.getJSONObject(0);
                        String nombre = temp.getString("nombre");
                        String id_usuario = temp.getString("id");
                        String id_sesion = temp.getString("sesion");

                        String imagen_base64 = temp.getString("avatar");

                        // Añadir a la lista un objeto Info_Login con esos datos
                        listaContenido.add( new Info_Login(nombre,id_usuario,id_sesion,imagen_base64) );

                        // Construir el objeto respuesta con todos los datos
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene más de un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }

                    break;


                // Respuesta para una consulta al servicio de validar sesión
                // ------------------------------------------------------------------------------------------------------------
                case CHECK_SESSION:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }

                    break;


                // Respuesta para una consulta al servicio de registro de nuevo usuario (particular o profesional)
                // ------------------------------------------------------------------------------------------------------------
                case REGISTER:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }
                    break;


                // Respuesta para una consulta al servicio de registro de nueva obra
                // ------------------------------------------------------------------------------------------------------------
                case NEW_WORK:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }
                    break;


                // Respuesta para una consulta al servicio de obtener/actualizar la información del perfil de un usurario particular
                // ------------------------------------------------------------------------------------------------------------
                case INITIAL_PARAMETERS_NEW_WORK:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO mas de un elemento, es un error
                    else if (contenido.length() > 1)
                    {
                        resp = null;
                    }

                    // Si el array de CONTENIDO tiene un elemento, procesarlo
                    else
                    {
                        JSONObject temp = contenido.getJSONObject(0);

                        String nombre = temp.getString("nombre");
                        String email = temp.getString("email");
                        String tel1 = temp.getString("tel1");
                        String tel2 = temp.getString("tel2");
                        String idProvincia = temp.getString("provincia");
                        String idPoblacion = temp.getString("poblacion");

                        ArrayList<Info_ElementoSpinner> actividades = new ArrayList<>();
                        ArrayList<Info_ElementoSpinner> provincias = new ArrayList<>();
                        ArrayList<Info_ElementoSpinner> poblaciones = new ArrayList<>();


                        // El campo actividades del objeto JSON temp es siempre es un array de otros objetos JSON
                        // Cada elemento de este array se corresponderá con un objeto del tipo Info_ElementoSpinner
                        JSONArray jArrayActividades = new JSONArray ( temp.getString("actividades") );

                        Log.d("Cliente Servicio Web", "Objeto JSON con las actividades: " + jArrayActividades.toString());

                        for (int i=0; (i < jArrayActividades.length()); i++)
                        {
                            JSONObject temp2 = jArrayActividades.getJSONObject(i);

                            String clave = temp2.getString("i");
                            String valor = temp2.getString("n");

                            // Añadir a la lista de actividades un objeto Info_ElementoSpinner con esos datos
                            actividades.add( new Info_ElementoSpinner(clave,valor) );
                        }


                        // El campo provincias del objeto JSON temp es siempre es un array de otros objetos JSON
                        // Cada elemento de este array se corresponderá con un objeto del tipo Info_ElementoSpinner
                        JSONArray jArrayProvincias = new JSONArray ( temp.getString("provincias") );

                        Log.d("Cliente Servicio Web", "Objeto JSON con las provincias: " + jArrayProvincias.toString());

                        for (int i=0; (i < jArrayProvincias.length()); i++)
                        {
                            JSONObject temp2 = jArrayProvincias.getJSONObject(i);

                            String clave = temp2.getString("i");
                            String valor = temp2.getString("n");

                            // Añadir a la lista de provincias un objeto Info_ElementoSpinner con esos datos
                            provincias.add( new Info_ElementoSpinner(clave,valor) );
                        }


                        // El campo poblaciones del objeto JSON temp es siempre es un array de otros objetos JSON
                        // Cada elemento de este array se corresponderá con un objeto del tipo Info_ElementoSpinner
                        JSONArray jArrayPoblaciones = new JSONArray ( temp.getString("poblaciones") );

                        Log.d("Cliente Servicio Web", "Objeto JSON con las poblaciones: " + jArrayPoblaciones.toString());

                        for (int i=0; (i < jArrayPoblaciones.length()); i++)
                        {
                            JSONObject temp2 = jArrayPoblaciones.getJSONObject(i);

                            String clave = temp2.getString("i");
                            String valor = temp2.getString("n");

                            // Añadir a la lista de poblaciones un objeto Info_ElementoSpinner con esos datos
                            poblaciones.add( new Info_ElementoSpinner(clave,valor) );
                        }


                        Info_ParametrosNuevaObra infoParNuevaObra = new Info_ParametrosNuevaObra(nombre,email,tel1,tel2,idProvincia,idPoblacion,actividades,provincias,poblaciones);

                        listaContenido.add(infoParNuevaObra);
                        Log.d("Cliente Servicio Web", "Objeto Info_ParametrosNuevaObra construido: " + infoParNuevaObra.toString());

                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                        Log.d("Cliente Servicio Web", "Respuesta construida: " + resp.toString());
                    }

                    break;


                // Respuesta para una consulta al servicio de obtener las cabeceras de las obras de un usuario particular o profesional,
                // o de las obras resultado de una busqueda hecha por un profesional
                // (el formato de la respuesta JSON es el mismo en los tres casos, por tanto se les da el mismo tratamiento)
                // ------------------------------------------------------------------------------------------------------------
                case MY_WORKS:
                case MY_WORKS_PRO:
                case WORK_SEARCH:

                    boolean error = false;

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene uno o más elementos, añadirlos a la lista
                    else
                    {
                        for (int i=0; (i < contenido.length()) && (!error); i++)
                        {
                            JSONObject temp = contenido.getJSONObject(i);
                            String id                = temp.getString("id");
                            String titulo            = temp.getString("titulo");
                            String id_tipoObra       = temp.getString("id_tipo");
                            String tipo_obra         = temp.getString("tipo_obra");
                            String fecha_realizacion = temp.getString("fecha_realizacion");
                            String visitas           = temp.getString("visitas");
                            String adjudicatario     = temp.getString("adjudicatario");
                            String interesados       = temp.getString("interesados");
                            String distancia         = temp.getString("distancia");
                            String falta_valoracion  = temp.getString("falta_valoracion");

                            boolean seguidor = false;
                            if ( (temp.getString("seguidor").equals("true")) || (temp.getString("seguidor").equals("false")) )
                            {
                                seguidor = temp.getString("seguidor").equals("true")?true:false;
                            }
                            else
                            {
                                error = true;
                                Log.e("Cliente Servicio Web", "Error al calcular variable: boolean seguidor");
                            }

                            // Si el valor del campo "visitas" o el del campo "interesados" no es un entero,
                            // o el campo "falta_valoracion" no es un booleano,
                            // o si el campo distancia no es un doble, error y salir
                            if ( !Utils.esEntero(visitas) || !Utils.esEntero(interesados) || !Utils.esBooleano(falta_valoracion) || !Utils.esDoble(distancia) )
                                error = true;

                            // Si no hay error, añadir a la lista un objeto Info_CabeceraObra con esos datos
                            if ( !error )
                                listaContenido.add( new Info_CabeceraObra(id,titulo,Integer.parseInt(id_tipoObra),tipo_obra,fecha_realizacion,Integer.parseInt(visitas),adjudicatario,Integer.parseInt(interesados),seguidor,Double.parseDouble(distancia),Boolean.parseBoolean(falta_valoracion)) );
                        }

                        // Si hubo errores en los datos recibidos, devolver null
                        if ( error )
                            resp = null;

                        // Si no hubo errores, construir el objeto respuesta con todos los datos
                        else
                            resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    break;


                // Respuesta para una consulta al servicio de obtener la información detallada de una obra
                // ------------------------------------------------------------------------------------------------------------
                case WORK_INFO:

                    error = false;

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene más de un elemento, es un error
                    else if (contenido.length() > 1)
                    {
                        resp = null;
                    }

                    // Si el array de CONTENIDO tiene un elemento, añadirlo a la lista
                    else
                    {
                        JSONObject temp = contenido.getJSONObject(0);

                        Log.d("Cliente Servicio Web","Contenido JSON recibido: "+temp.toString() );

                        String solicitante = temp.getString("solicitante");
                        String titulo = temp.getString("titulo");
                        String detalleObra = temp.getString("detalle");
                        String actividad = temp.getString("actividad");
                        String categoria = temp.getString("categoria");
                        String tipo = temp.getString("tipo");
                        String fecha_solicitud = temp.getString("fecha_solicitud");
                        String fecha_realizacion = temp.getString("fecha_realizacion");
                        String poblacion = temp.getString("poblacion");
                        String nombre_contacto = temp.getString("nombre_contacto");
                        String email_contacto = temp.getString("email_contacto");
                        String telef_contacto = temp.getString("telef_contacto");
                        String adjudicatario = temp.getString("adjudicatario");
                        String imagen_base64 = temp.getString("avatar_solicitante");

                        boolean cerrada = false;
                        if ( (temp.getString("cerrada").equals("0")) || (temp.getString("cerrada").equals("1")) )
                        {
                            cerrada = temp.getString("cerrada").equals("0")?false:true;
                        }
                        else
                        {
                            error = true;
                            Log.e("Cliente Servicio Web", "Error al calcular variable: boolean cerrada");
                        }

                        boolean falta_valoracion = false;
                        if ( (temp.getString("falta_valoracion").equalsIgnoreCase("true")) || (temp.getString("falta_valoracion").equalsIgnoreCase("false")) )
                        {
                            falta_valoracion = temp.getString("falta_valoracion").equalsIgnoreCase("true")?true:false;
                        }
                        else
                        {
                            error = true;
                            Log.e("Cliente Servicio Web", "Error al calcular variable: boolean falta_valoracion");
                        }


                        int visitas = 0;
                        double latitud = 0;
                        double longitud = 0;
                        BigDecimal presupuesto = null;
                        try
                        {
                            visitas  = Integer.parseInt( temp.getString("visitas") );
                            latitud  = Double.parseDouble( temp.getString("latitud") );
                            longitud = Double.parseDouble( temp.getString("longitud") );
                            presupuesto = new BigDecimal( temp.getString("presupuesto") );
                        }
                        catch (NumberFormatException ex)
                        {
                            error = true;
                            Log.e("Cliente Servicio Web", "Error al calcular varialbes: int visitas, double latitud,longitud");
                        }

                        ArrayList<String> imagenes = new ArrayList<>();
                        if ( !error )
                        {
                            // El campo imagenes del objeto JSON temp es siempre es un array de otros objetos JSON
                            // Cada elemento de este array se corresponderá con un objeto String que representa una imagen JPEG en Base64
                            JSONArray jArrayImagenes = new JSONArray ( temp.getString("imagenes") );

                            for (int i=0; (i < jArrayImagenes.length()) && (!error); i++)
                            {
                                JSONObject temp2 = jArrayImagenes.getJSONObject(i);
                                Log.d("Cliente Servicio Web", "Objeto JSON con las imágenes: " + temp2.toString());

                                imagenes.add(temp2.getString("jpeg64") );
                            }
                        }

                        ArrayList<Info_CabeceraProfesional> seguidores = new ArrayList<>();
                        if ( !error )
                        {
                            // El campo seguidores del objeto JSON temp es siempre es un array de otros objetos JSON
                            // Cada elemento de este array se corresponderá con un objeto del tipo Info_CabeceraProfesional
                            JSONArray jArraySeguidores = new JSONArray ( temp.getString("seguidores") );

                            for (int i=0; (i < jArraySeguidores.length()) && (!error); i++)
                            {
                                JSONObject temp2 = jArraySeguidores.getJSONObject(i);

                                Log.d("Cliente Servicio Web","Objeto JSON con los seguidores: "+temp2.toString() );

                                String id            = temp2.getString("id");
                                String nombre        = temp2.getString("nombre");
                                String email         = temp2.getString("email");
                                String telefono1     = temp2.getString("telefono1");
                                String telefono2     = temp2.getString("telefono2");
                                String direccion     = temp2.getString("direccion");
                                String poblacionProf = temp2.getString("poblacion");
                                String provinciaProf = temp2.getString("provincia");
                                String img_base64    = temp2.getString("avatar");
                                boolean favorito     = Boolean.parseBoolean(temp2.getString("esFavorito"));

                                int votos = 0;
                                double media_calidad = 0;
                                double media_precio = 0;
                                double distancia = 0;
                                try
                                {
                                    votos = Integer.parseInt( temp2.getString("votos") );
                                    media_calidad = Double.parseDouble( temp2.getString("calidad") );
                                    media_precio = Double.parseDouble( temp2.getString("precio") );
                                    distancia = Double.parseDouble( temp2.getString("distancia") );
                                }
                                catch (NumberFormatException ex)
                                {
                                    error = true;
                                    Log.d("Cliente Servicio Web","Error al calcular variables: int votos, double media_calidad,media_precio,distancia" );
                                }

                                // Si no hubo errores, crear el objeto Info_CabeceraProfesional correspondiente y añadirlo a la lista de seguidores
                                if ( !error )
                                {
                                    Info_CabeceraProfesional infoPro = new Info_CabeceraProfesional(id, nombre, email, telefono1, telefono2, direccion, poblacionProf, provinciaProf, img_base64, votos, media_calidad, media_precio, distancia, favorito);

                                    // Si el id de este profesional seguidor coincide con el del adjudicatario de la obra, guardar esta info
                                    if ( id.equals(adjudicatario) )
                                        infoPro.setAdjudicatario(true);

                                    seguidores.add(infoPro);
                                    Log.d("Cliente Servicio Web", "Objeto Info_CabeceraProfesional construido: "+infoPro.toString() );
                                }
                            }
                        }

                        // Si hubo errores en los datos recibidos, devolver null
                        if ( error )
                            resp = null;

                        // Si no hubo errores, construir el objeto respuesta con todos los datos
                        else
                        {
                            Info_Obra infoObra = new Info_Obra(solicitante,titulo,detalleObra,actividad,categoria,tipo,fecha_solicitud,fecha_realizacion,poblacion,latitud,longitud,nombre_contacto,email_contacto,telef_contacto,visitas,cerrada,adjudicatario,presupuesto,imagen_base64,seguidores,falta_valoracion,imagenes);
                            listaContenido.add( infoObra );

                            resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);

                            Log.d("Cliente Servicio Web", "Objecto Info_Obra construido: "+infoObra.toString() );

                            Log.d("Cliente Servicio Web","Respuesta construida: "+resp.toString() );
                        }
                    }

                    break;


                // Respuesta para una consulta al servicio de obtener la información detallada de un profesional
                // ------------------------------------------------------------------------------------------------------------
                case PROFESSIONAL_INFO:

                    error = false;

                    Log.d("Cliente Servicio Web","Tam. Array: "+Integer.toString( contenido.length() ) );

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene más de un elemento, es un error
                    else if (contenido.length() > 1)
                    {
                        resp = null;
                    }

                    // Si el array de CONTENIDO tiene un elemento, procesarlo
                    else
                    {
                        JSONObject temp = contenido.getJSONObject(0);

                        Log.d("Cliente Servicio Web","Contenido JSON recibido: "+temp.toString() );

                        String id             = temp.getString("id");
                        String nombre         = temp.getString("nombre");
                        String descripcion    = temp.getString("descripcion");
                        String web            = temp.getString("web");
                        String email_contacto = temp.getString("email_contacto");
                        String telefono1      = temp.getString("telefono1");
                        String telefono2      = temp.getString("telefono2");
                        String direccion      = temp.getString("direccion");
                        String id_poblacion   = temp.getString("id_poblacion");
                        String poblacion      = temp.getString("poblacion");
                        String provincia      = temp.getString("provincia");
                        String imagen_base64  = temp.getString("avatar_profesional");
                        boolean favorito      = Boolean.parseBoolean(temp.getString("favorito"));

                        ArrayList <ActividadObra> actividades = new ArrayList<>();
                        ArrayList <Info_Valoracion> valoraciones = new ArrayList<>();




                        // El campo actividades del objeto JSON temp es siempre es un array de otros objetos JSON
                        // Cada elemento de este array se corresponderá con un objeto del tipo ActividadObra
                        JSONArray jArrayActividades = new JSONArray ( temp.getString("actividades") );

                        for (int i=0; (i < jArrayActividades.length()) && (!error); i++)
                        {
                            JSONObject temp2 = jArrayActividades.getJSONObject(i);

                            Log.d("Cliente Servicio Web", "Objeto JSON con las actividades: " + temp2.toString());

                            String act_id = temp2.getString("act_id");
                            String act_nom = temp2.getString("act_nom");
                            ArrayList<CategoriaObra> categorias = new ArrayList<>();


                            // El campo categorias del objeto JSON temp2 es siempre es un array de otros objetos JSON
                            // Cada elemento de este array se corresponderá con un objeto del tipo CategoriaObra
                            JSONArray jArrayCategorias = new JSONArray(temp2.getString("categorias"));

                            for (int j = 0; (j < jArrayCategorias.length()) && (!error); j++)
                            {
                                JSONObject temp3 = jArrayCategorias.getJSONObject(j);

                                Log.d("Cliente Servicio Web", "Objeto JSON con las categorias: " + temp3.toString());

                                String cat_id = temp3.getString("cat_id");
                                String cat_nom = temp3.getString("cat_nom");
                                ArrayList<TipoObra> tipos = new ArrayList<>();


                                // El campo tipos del objeto JSON temp3 es siempre es un array de otros objetos JSON
                                // Cada elemento de este array se corresponderá con un objeto del tipo TipoObra
                                JSONArray jArrayTipos = new JSONArray(temp3.getString("tipos"));

                                for (int k = 0; (k < jArrayTipos.length()) && (!error); k++)
                                {
                                    JSONObject temp4 = jArrayTipos.getJSONObject(k);

                                    Log.d("Cliente Servicio Web", "Objeto JSON con los tipos: " + temp4.toString());

                                    String tip_id = temp4.getString("tip_id");
                                    String tip_nom = temp4.getString("tip_nom");

                                    TipoObra tipoObra = new TipoObra(tip_id, tip_nom);
                                    tipos.add(tipoObra);
                                }

                                CategoriaObra categoriaObra = new CategoriaObra(cat_id, cat_nom, tipos);
                                categorias.add(categoriaObra);
                            }

                            ActividadObra actividadObra = new ActividadObra(act_id, act_nom, categorias);
                            actividades.add(actividadObra);
                        }


                        // El campo valoraciones del objeto JSON temp es siempre es un array de otros objetos JSON
                        // Cada elemento de este array se corresponderá con un objeto del tipo Info_Valoracion
                        JSONArray jArrayValoraciones = new JSONArray(temp.getString("valoraciones"));

                        for (int i = 0; (i < jArrayValoraciones.length()) && (!error); i++)
                        {
                            JSONObject temp2 = jArrayValoraciones.getJSONObject(i);

                            Log.d("Cliente Servicio Web", "Objeto JSON con las valoraciones: " + temp2.toString());

                            String id_usuario       = temp2.getString("id_usuario");
                            String nombre_usuario   = temp2.getString("nombre_usuario");
                            String email_usuario    = temp2.getString("email_usuario");
                            String img_base64       = temp2.getString("avatar");
                            String comentario       = temp2.getString("comentario");

                            String fecha_valoracion = temp2.getString("fecha_valoracion");
                            String id_obra          = temp2.getString("id_obra");
                            String titulo_obra      = temp2.getString("titulo_obra");
                            String id_tipo          = temp2.getString("id_tipo");
                            String tipo_obra        = temp2.getString("tipo_obra");


                            int nota_calidad = 0;
                            int nota_precio = 0;
                            BigDecimal presupuesto_obra = null;
                            try
                            {
                                nota_calidad     = Integer.parseInt( temp2.getString("nota_calidad") );
                                nota_precio      = Integer.parseInt( temp2.getString("nota_precio") );
                                presupuesto_obra = new BigDecimal( temp2.getString("presupuesto_obra") );
                            }
                            catch (NumberFormatException ex)
                            {
                                error = true;
                                Log.e("Cliente Servicio Web", "Error al calcular varialbes: int nota_calidad, int nota_precio, BigDecimal presupuesto_obra");
                            }

                            // Si no hubo errores, crear el objeto Info_Valoracion correspondiente y añadirlo a la lista de valoraciones
                            if ( !error )
                            {
                                Info_Valoracion valoracion = new Info_Valoracion(id_usuario,nombre_usuario,email_usuario,comentario,nota_calidad,
                                        nota_precio,fecha_valoracion,id_obra,titulo_obra,id_tipo,tipo_obra,presupuesto_obra,img_base64);

                                valoraciones.add(valoracion);
                            }

                        }


                        // Si hubo errores en los datos recibidos, devolver null
                        if ( error )
                            resp = null;

                        // Si no hubo errores, construir el objeto respuesta con todos los datos
                        else
                        {
                            Info_Profesional infoProfesional = new Info_Profesional(id,nombre,descripcion,web,email_contacto,telefono1,telefono2,
                                    direccion,id_poblacion,poblacion,provincia,imagen_base64,favorito,actividades,valoraciones);

                            listaContenido.add( infoProfesional );

                            resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);

                            Log.d("Cliente Servicio Web", "Objecto Info_Profesional construido: " + infoProfesional.toString());

                            Log.d("Cliente Servicio Web", "Respuesta construida: "+ resp.toString() );
                        }
                    }

                    break;


                // Respuesta para una consulta al servicio de validar sesión
                // ------------------------------------------------------------------------------------------------------------
                case MODIFY_FAVORITES:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }

                    break;


                // Respuesta para una consulta al servicio de listar los profesionales favoritos del usuario
                // ------------------------------------------------------------------------------------------------------------
                case MY_FAVORITES:

                    error = false;

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún elemento, añadirlo a la lista
                    else
                    {
                        for (int i=0; (i < contenido.length()) && (!error); i++)
                        {
                            JSONObject temp = contenido.getJSONObject(i);

                            String id            = temp.getString("id");
                            String nombre        = temp.getString("nombre");
                            String email         = temp.getString("email");
                            String telefono1     = temp.getString("telefono1");
                            String telefono2     = temp.getString("telefono2");
                            String direccion     = temp.getString("direccion");
                            String poblacionProf = temp.getString("poblacion");
                            String provinciaProf = temp.getString("provincia");
                            String imagen_base64 = temp.getString("avatar");;
                            boolean favorito     = Boolean.parseBoolean(temp.getString("esFavorito"));

                            int votos = 0;
                            double media_calidad = 0;
                            double media_precio = 0;
                            double distancia = 0;
                            try
                            {
                                votos = Integer.parseInt( temp.getString("votos") );
                                media_calidad = Double.parseDouble(temp.getString("calidad"));
                                media_precio = Double.parseDouble( temp.getString("precio") );
                                distancia = Double.parseDouble( temp.getString("distancia") );
                            }
                            catch (NumberFormatException ex)
                            {
                                error = true;
                                Log.d("Cliente Servicio Web","Error al calcular variables: int votos, double media_calidad,media_precio,distancia" );
                            }

                            // Si no hubo errores, crear el objeto Info_CabeceraProfesional correspondiente y añadirlo a la lista de seguidores
                            if ( !error )
                            {
                                Info_CabeceraProfesional infoPro = new Info_CabeceraProfesional(id, nombre, email, telefono1, telefono2, direccion, poblacionProf, provinciaProf, imagen_base64, votos, media_calidad, media_precio, distancia, favorito);

                                listaContenido.add(infoPro);
                                Log.d("Cliente Servicio Web", "Objeto Info_CabeceraProfesional construido: "+infoPro.toString() );
                            }
                        }

                        // Si hubo errores en los datos recibidos, devolver null
                        if ( error )
                            resp = null;

                        // Si no hubo errores, construir el objeto respuesta con todos los datos
                        else
                        {
                            resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);

                            Log.d("Cliente Servicio Web","Respuesta construida: "+resp.toString() );
                        }

                    }

                    break;


                // Respuesta para una consulta al servicio de obtener los parámetros para las búsquedas
                // ------------------------------------------------------------------------------------------------------------
                case SPINNER_VALUES:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene uno o más elementos, añadirlos a la lista
                    else
                    {
                        for (int i=0; i < contenido.length(); i++)
                        {
                            JSONObject temp = contenido.getJSONObject(i);
                            String clave = temp.getString("i");
                            String valor = temp.getString("n");

                            // Añadir a la lista un objeto Info_ElementoSpinner con esos datos
                            listaContenido.add( new Info_ElementoSpinner(clave,valor) );
                        }

                        // Construir el objeto respuesta con todos los datos
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    break;


                // Respuesta para una consulta al servicio de obtener la información detallada de una obra
                // ------------------------------------------------------------------------------------------------------------
                case PROFESSIONAL_SEARCH:

                    error = false;

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene uno o más elementos, añadirlos a la lista
                    else
                    {
                        for (int i=0; (i<contenido.length()) && (!error); i++)
                        {
                            JSONObject temp = contenido.getJSONObject(i);

                            String id = temp.getString("id");
                            String nombre = temp.getString("nombre");
                            String email = temp.getString("email");
                            String telefono1 = temp.getString("telefono1");
                            String telefono2 = temp.getString("telefono2");
                            String direccion = temp.getString("direccion");
                            String poblacionProf = temp.getString("poblacion");
                            String provinciaProf = temp.getString("provincia");
                            String imagen_base64 = temp.getString("avatar");
                            boolean favorito = Boolean.parseBoolean(temp.getString("esFavorito"));

                            int votos = 0;
                            double media_calidad = 0;
                            double media_precio = 0;
                            double distancia = 0;
                            try
                            {
                                votos = Integer.parseInt(temp.getString("votos"));
                                media_calidad = Double.parseDouble(temp.getString("calidad"));
                                media_precio = Double.parseDouble(temp.getString("precio"));
                                distancia = Double.parseDouble( temp.getString("distancia") );
                            }
                            catch (NumberFormatException ex)
                            {
                                error = true;
                                Log.d("Cliente Servicio Web", "Error al calcular variables: int votos, double media_calidad,media_precio");
                            }

                            // Si no hubo errores, crear el objeto Info_CabeceraProfesional correspondiente y añadirlo a la lista
                            if (!error)
                            {
                                Info_CabeceraProfesional infoPro = new Info_CabeceraProfesional(id, nombre, email, telefono1, telefono2, direccion, poblacionProf, provinciaProf, imagen_base64, votos, media_calidad, media_precio, distancia, favorito);

                                listaContenido.add(infoPro);
                                Log.d("Cliente Servicio Web", "Objeto Info_CabeceraProfesional construido: " + infoPro.toString());
                            }
                        }


                        // Si hubo errores en los datos recibidos, devolver null
                        if ( error )
                            resp = null;

                        // Si no hubo errores, construir el objeto respuesta con todos los datos
                        else
                        {
                            resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);

                            Log.d("Cliente Servicio Web","Respuesta construida: "+resp.toString() );
                        }
                    }

                    break;


                // Respuesta para una consulta al servicio de obtener/actualizar la información del perfil de un usuario particular
                // ------------------------------------------------------------------------------------------------------------
                case USER_PROFILE:

                    error = false;

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene mas de un elemento, es un error
                    else if (contenido.length() > 1)
                    {
                        resp = null;
                    }

                    // Si el array de CONTENIDO tiene un elemento, añadirlo a la lista
                    else
                    {
                        JSONObject temp = contenido.getJSONObject(0);

                        String nombre = temp.getString("nombre");
                        String email = temp.getString("email");
                        String tel1 = temp.getString("tel1");
                        String tel2 = temp.getString("tel2");
                        String idProvincia = temp.getString("provincia");
                        String idPoblacion = temp.getString("poblacion");
                        String nacimiento = temp.getString("nacimiento");
                        String sexo = temp.getString("genero");
                        String idioma = temp.getString("idioma");
                        String avatar_base64 = temp.getString("avatar");

                        boolean publicidad;
                        if ( temp.getString("publicidad").equals("0") )
                            publicidad = false;
                        else
                            publicidad = true;

                        Bitmap avatar;

                        if ( avatar_base64.equals("default_profile_pic") )
                            avatar = null;
                        else
                            avatar = Utils.descodifica_imagen_base64(avatar_base64);

                        Info_PerfilUsuario infoPerfil = new Info_PerfilUsuario(nombre,email,tel1,tel2,idProvincia,idPoblacion,nacimiento,sexo,publicidad,idioma,avatar);

                        listaContenido.add(infoPerfil);
                        Log.d("Cliente Servicio Web", "Objeto Info_PerfilUsuario construido: " + infoPerfil.toString());

                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                        Log.d("Cliente Servicio Web","Respuesta construida: "+resp.toString() );
                    }

                    break;


                // Respuesta para una consulta al servicio de obtener datos básicos de los profesionales seguidores de una obra
                // ------------------------------------------------------------------------------------------------------------
                case WORK_FOLLOWERS:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene uno o más elementos, añadirlos a la lista
                    else
                    {
                        for (int i=0; i < contenido.length(); i++)
                        {
                            JSONObject temp = contenido.getJSONObject(i);
                            String id = temp.getString("id");
                            String nombre = temp.getString("nombre");

                            listaContenido.add( new Info_ProfesionalInteresado(id,nombre) );
                        }

                        // Construir el objeto respuesta con todos los datos
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    break;


                // Respuesta para una consulta al servicio de cerrar una obra
                // ------------------------------------------------------------------------------------------------------------
                case CLOSE_WORK:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }

                    break;


                // Respuesta para una consulta al servicio de valorar una obra
                // ------------------------------------------------------------------------------------------------------------
                case VOTE_WORK:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }

                    break;


                // Respuesta para una consulta al servicio de eliminar una obra
                // ------------------------------------------------------------------------------------------------------------
                case REMOVE_WORK:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }

                    break;


                // Respuesta para una consulta al servicio de obtener las valoraciones de un usuario (particular o profesional)
                // ------------------------------------------------------------------------------------------------------------
                case MY_VOTES:

                    error = false;

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene uno o más elementos, añadirlos a la lista
                    else
                    {
                        for (int i=0; (i<contenido.length()) && (!error); i++)
                        {
                            JSONObject temp = contenido.getJSONObject(i);

                            String id_usuario = temp.getString("id_usuario");
                            String nombre_usuario = temp.getString("nombre_usuario");
                            String email_usuario = temp.getString("email_usuario");
                            String img_base64 = temp.getString("avatar");
                            String comentario = temp.getString("comentario");

                            String fecha_valoracion = temp.getString("fecha_valoracion");
                            String id_obra = temp.getString("id_obra");
                            String titulo_obra = temp.getString("titulo_obra");
                            String id_tipo = temp.getString("id_tipo");
                            String tipo_obra = temp.getString("tipo_obra");


                            int nota_calidad = 0;
                            int nota_precio = 0;
                            BigDecimal presupuesto_obra = null;
                            try
                            {
                                nota_calidad = Integer.parseInt(temp.getString("nota_calidad"));
                                nota_precio = Integer.parseInt(temp.getString("nota_precio"));
                                presupuesto_obra = new BigDecimal(temp.getString("presupuesto_obra"));
                            }
                            catch (NumberFormatException ex)
                            {
                                error = true;
                                Log.e("Cliente Servicio Web", "Error al calcular varialbes: int nota_calidad, int nota_precio, BigDecimal presupuesto_obra");
                            }

                            // Si no hubo errores, crear el objeto Info_Valoracion correspondiente y añadirlo a la lista
                            if (!error)
                            {
                                Info_Valoracion valoracion = new Info_Valoracion(id_usuario, nombre_usuario, email_usuario, comentario, nota_calidad,
                                        nota_precio, fecha_valoracion, id_obra, titulo_obra, id_tipo, tipo_obra, presupuesto_obra, img_base64);

                                listaContenido.add(valoracion);
                            }
                        }


                        // Si hubo errores en los datos recibidos, devolver null
                        if ( error )
                            resp = null;

                        // Si no hubo errores, construir el objeto respuesta con todos los datos
                        else
                        {
                            resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);

                            Log.d("Cliente Servicio Web","Respuesta construida: "+resp.toString() );
                        }

                    }

                    break;


                // Respuesta para una consulta al servicio de consultar/modificar el acceso premium de un usuario profesional
                // ------------------------------------------------------------------------------------------------------------
                case PREMIUM_ACCESS:

                    error = false;

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene mas de un elemento, es un error
                    else if (contenido.length() > 1)
                    {
                        resp = null;
                    }

                    // Si el array de CONTENIDO tiene un elemento, añadirlo a la lista
                    else
                    {
                        JSONObject temp = contenido.getJSONObject(0);

                        String premium = temp.getString("premium");

                        boolean esPremium = false;
                        if ( premium.equalsIgnoreCase("true") )
                            esPremium = true;

                        Info_AccesoPremium infoPremium = new Info_AccesoPremium(esPremium);

                        listaContenido.add(infoPremium);
                        Log.d("Cliente Servicio Web", "Objeto infoPremium construido: " + infoPremium.toString());

                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                        Log.d("Cliente Servicio Web","Respuesta construida: "+resp.toString() );
                    }

                    break;


                // Respuesta para una consulta a los servicios de seguir/olvidar una obra
                // (el tratamiento de la respuesta es el mismo para ambos casos)
                // ------------------------------------------------------------------------------------------------------------
                case FOLLOW_WORK:
                case UNFOLLOW_WORK:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }

                    break;


                // Respuesta para una consulta al servicio de obtener/actualizar la información del perfil de un usuario profesional
                // ------------------------------------------------------------------------------------------------------------
                case PROFESSIONAL_PROFILE:

                    error = false;

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene mas de un elemento, es un error
                    else if (contenido.length() > 1)
                    {
                        resp = null;
                    }

                    // Si el array de CONTENIDO tiene un elemento, procesarlo
                    else
                    {
                        JSONObject temp = contenido.getJSONObject(0);

                        String usuario = temp.getString("email_usuario");
                        String nombre = temp.getString("nombre");
                        String descripcion = temp.getString("descripcion");
                        String direccion = temp.getString("direccion");
                        String idPoblacion = temp.getString("poblacion");
                        String idProvincia = temp.getString("provincia");
                        String tel1 = temp.getString("telefono1");
                        String tel2 = temp.getString("telefono2");
                        String web = temp.getString("web");
                        String email_contacto = temp.getString("email_contacto");
                        String idioma = temp.getString("idioma");

                        boolean premium;
                        if ( temp.getString("premium").equals("0") )
                            premium = false;
                        else
                            premium = true;

                        boolean publicidad;
                        if ( temp.getString("publicidad").equals("0") )
                            publicidad = false;
                        else
                            publicidad = true;

                        Bitmap avatar;
                        String avatar_base64 = temp.getString("avatar");

                        if ( avatar_base64.equals("default_profile_pic") )
                            avatar = null;
                        else
                            avatar = Utils.descodifica_imagen_base64(avatar_base64);


                        int votos = 0;
                        double media_calidad = 0;
                        double media_precio = 0;
                        try
                        {
                            votos = Integer.parseInt(temp.getString("votos"));
                            media_calidad = Double.parseDouble(temp.getString("calidad"));
                            media_precio = Double.parseDouble(temp.getString("precio"));
                        }
                        catch (NumberFormatException ex)
                        {
                            error = true;
                            Log.d("Cliente Servicio Web", "Error al calcular variables: int votos, double media_calidad,media_precio");
                        }


                        // Listado de especialidades del profesional
                        ArrayList <ActividadObra> especialidades = new ArrayList<>();

                        // El campo actividades del objeto JSON temp es siempre es un array de otros objetos JSON
                        // Cada elemento de este array se corresponderá con un objeto del tipo ActividadObra
                        JSONArray jArrayActividades = new JSONArray ( temp.getString("actividades") );

                        for (int i=0; (i < jArrayActividades.length()) && (!error); i++)
                        {
                            JSONObject temp2 = jArrayActividades.getJSONObject(i);

                            Log.d("Cliente Servicio Web", "Objeto JSON con las actividades: " + temp2.toString());

                            String act_id = temp2.getString("act_id");
                            String act_nom = temp2.getString("act_nom");
                            ArrayList<CategoriaObra> categorias = new ArrayList<>();


                            // El campo categorias del objeto JSON temp2 es siempre es un array de otros objetos JSON
                            // Cada elemento de este array se corresponderá con un objeto del tipo CategoriaObra
                            JSONArray jArrayCategorias = new JSONArray(temp2.getString("categorias"));

                            for (int j = 0; (j < jArrayCategorias.length()) && (!error); j++)
                            {
                                JSONObject temp3 = jArrayCategorias.getJSONObject(j);

                                Log.d("Cliente Servicio Web", "Objeto JSON con las categorias: " + temp3.toString());

                                String cat_id = temp3.getString("cat_id");
                                String cat_nom = temp3.getString("cat_nom");
                                ArrayList<TipoObra> tipos = new ArrayList<>();


                                // El campo tipos del objeto JSON temp3 es siempre es un array de otros objetos JSON
                                // Cada elemento de este array se corresponderá con un objeto del tipo TipoObra
                                JSONArray jArrayTipos = new JSONArray(temp3.getString("tipos"));

                                for (int k = 0; (k < jArrayTipos.length()) && (!error); k++)
                                {
                                    JSONObject temp4 = jArrayTipos.getJSONObject(k);

                                    Log.d("Cliente Servicio Web", "Objeto JSON con los tipos: " + temp4.toString());

                                    String tip_id = temp4.getString("tip_id");
                                    String tip_nom = temp4.getString("tip_nom");

                                    TipoObra tipoObra = new TipoObra(tip_id, tip_nom);
                                    tipos.add(tipoObra);
                                }

                                CategoriaObra categoriaObra = new CategoriaObra(cat_id, cat_nom, tipos);
                                categorias.add(categoriaObra);
                            }

                            ActividadObra actividadObra = new ActividadObra(act_id, act_nom, categorias);
                            especialidades.add(actividadObra);
                        }


                        // Si hubo errores en los datos recibidos, devolver null
                        if ( error )
                            resp = null;

                        // Si no hubo errores, construir el objeto respuesta con todos los datos
                        else
                        {
                            // boolean prem, String usuario, String nom, String desc, String dir, String idPob, String idProv,
                            //    String t1, String t2, String www, String email, String lang, boolean pub, int vot, double cal,
                            // double pre, ArrayList<ActividadObra> esp, Bitmap img)

                            Info_PerfilProfesional infoPerfil = new Info_PerfilProfesional(premium,usuario,nombre,descripcion,direccion,
                                                                                            idPoblacion,idProvincia,tel1,tel2,web,
                                                                                            email_contacto,idioma,publicidad,votos,
                                                                                            media_calidad,media_precio,especialidades,
                                                                                            avatar);

                            listaContenido.add(infoPerfil);
                            Log.d("Cliente Servicio Web", "Objeto Info_PerfilUsuario construido: " + infoPerfil.toString());

                            resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                            Log.d("Cliente Servicio Web","Respuesta construida: "+resp.toString() );
                        }
                    }

                    break;


                // Respuesta para una consulta al servicio de iniciar le proceso de reiniciar una contraseña de usuario
                // ------------------------------------------------------------------------------------------------------------
                case PASSWORD_RESET:

                    // Si el array de CONTENIDO está vacío, construir un objeto Respuesta sin contenido (una lista vacía)
                    if (contenido.length() == 0)
                    {
                        resp = new RespuestaServicioWeb(resultado, detalle, listaContenido);
                    }

                    // Si el array de CONTENIDO tiene algún un elemento, no es correcto
                    else
                    {
                        resp = null;
                    }

                    break;


                // Respuesta para una consulta a otro servicio no implementado
                // ------------------------------------------------------------------------------------------------------------
                default:
                    Log.e("Cliente Servicio Web","Tipo servicio web no implementado" );
                    resp = null;
                    break;
            }

        }

        // Si hubo errores al manipular el objeto JSON, devolver null
        catch (JSONException ex)
        {
            resp = null;
            Log.e("Cliente Servicio Web", "Se produjo una JSONException: " + ex.toString());
            Log.e("Cliente Servicio Web", Log.getStackTraceString(ex));
        }


        return resp;
    }

}
