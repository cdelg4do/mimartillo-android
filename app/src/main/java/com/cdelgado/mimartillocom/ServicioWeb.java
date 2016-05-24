package com.cdelgado.mimartillocom;


import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;





public class ServicioWeb
{
    public static enum Tipo
    {
        LOGIN,
        CHECK_SESSION,
        REGISTER,
        NEW_WORK,
        INITIAL_PARAMETERS_NEW_WORK,
        MY_WORKS,
        WORK_INFO,
        PROFESSIONAL_SEARCH,      // para usar en las búsquedas que hacen los particularess (WORK_INFO también devuelve una lista de Info_CabeceraProfesional)
        PROFESSIONAL_INFO,
        MODIFY_FAVORITES,
        MY_FAVORITES,
        SPINNER_VALUES,
        USER_PROFILE,
        WORK_FOLLOWERS,
        CLOSE_WORK,
        VOTE_WORK,
        REMOVE_WORK,
        MY_WORKS_PRO,
        WORK_SEARCH,
        MY_VOTES,
        PREMIUM_ACCESS,
        FOLLOW_WORK,
        UNFOLLOW_WORK,
        PROFESSIONAL_PROFILE,
        PASSWORD_RESET
    }

    public static enum Metodo
    {
        GET,
        POST
    }


    private boolean                 servicioDePrueba;   // Indica si se trata de un servicio web que simula la respuesta de un servidor remoto

    private boolean                 conexionSegura;     // Indica si se usará un protocolo seguro para la consulta al servicio web
    private ServicioWeb.Tipo        tipo;               // Indicador del tipo de servicio web a que corresponde este objeto
    private String                  url;                // URL asociada a ese servicio web
    private Metodo                  metodo;             // Método HTTP utilizado por este servicio web
    private List<NameValuePair>     parametros;         // Lista de parámetros (de tipo String) a enviar al servidor a través de la red
    private RespuestaServicioWeb    respuesta;          // Objeto con la respuesta obtenida del servidor

    private Object                  parametroLocal;     // Parámetro genérico que se pasa a través del servicio web para ser manipulado
                                                        // durante el post-procesado de la tarea asíncrona correspondiente.
                                                        // Este objeto NO se envía a través de la red

    private Context contexto;                           // Contexto en que se ejecuta la consulta al servicio web


    // Constructor sin parámetro local
    public ServicioWeb(boolean s, ServicioWeb.Tipo t, Context c)
    {
        servicioDePrueba = false;

        conexionSegura = s;
        tipo = t;
        contexto = c;

        respuesta = null;
        parametros = new ArrayList<>();

        parametroLocal = null;

        setUrl(t);
        setMetodo(t);
    }

    // Constructor con parámetro local
    public ServicioWeb(boolean s, ServicioWeb.Tipo t, Context c, Object p)
    {
        servicioDePrueba = false;

        conexionSegura = s;
        tipo = t;
        contexto = c;

        respuesta = null;
        parametros = new ArrayList<>();

        parametroLocal = p;

        setUrl(t);
        setMetodo(t);
    }

    // Constructor privado: construye un ServicioWeb que ya incluye un objeto RespuestaServicioWeb previamente existente
    // Si no se quiere incluir un parámetro local, p debe ser null.
    // (este constructor debe ser invocado a través del método de clase público crearServicioDePrueba)
    private ServicioWeb(ServicioWeb.Tipo t, Context c, Object p, RespuestaServicioWeb r)
    {
        servicioDePrueba = true;

        conexionSegura = true;
        tipo = t;
        contexto = c;

        respuesta = r;
        parametros = new ArrayList<>();

        parametroLocal = p;

        setUrl(t);
        setMetodo(t);
    }


    // Devuelve un servicio web de prueba, que ya incluye una respuesta simulada del servidor
    // (método de clase)
    public static ServicioWeb crearServicioDePrueba(Context ctx, ServicioWeb.Tipo tipoServicio, Object paramLocal, RespuestaServicioWeb respuestaSimulada)
    {
        return new ServicioWeb(tipoServicio, ctx, paramLocal, respuestaSimulada);
    }


    // Indica si se trata de un servicio web de prueba o no
    public boolean esDePrueba()
    {
        return servicioDePrueba;
    }


    // Añade un parametro a la peticion de la forma nombre/valor
    public void addParam(String parametro, String valor)
    {
        BasicNameValuePair par = new BasicNameValuePair(parametro,valor);
        parametros.add(par);
    }


    // Añade los parámetros de un ArrayList<BasicNameValuePair> a los parámetros de la petición
    public void addParamList (ArrayList<BasicNameValuePair> lista)
    {
        for (int i=0; i<lista.size(); i++)
            parametros.add( lista.get(i) );
    }


    // Devuelve la lista de parametros de la peticion a enviar por red
    public List<NameValuePair> getParametros()
    {
        return parametros;
    }


    // Devuelve el parámetro local (si lo hay) para ser tratado en el método post-AsyncTask correspondiente
    public Object getParametroLocal()
    {
        return parametroLocal;
    }


    // Almacena un objeto RespuestaServicioWeb indicado
    public void setRespuesta(RespuestaServicioWeb r)
    {
        respuesta = r;
    }


    // Devuelve la respuesta recibida a la peticion al servicio web
    public RespuestaServicioWeb getRespuesta()
    {
        return respuesta;
    }


    // Asigna el metodo de peticion http correspondiente a ese tipo de servicio web
    // (por defecto GET) (metodo de clase privado)
    private void setMetodo(ServicioWeb.Tipo t)
    {
        String m = "";

        switch (t)
        {
            case LOGIN:
                m = contexto.getResources().getString(R.string.METHOD_webservice_login);
                break;

            case CHECK_SESSION:
                m = contexto.getResources().getString(R.string.METHOD_webservice_checkSession);
                break;

            case REGISTER:
                m = contexto.getResources().getString(R.string.METHOD_webservice_register);
                break;

            case NEW_WORK:
                m = contexto.getResources().getString(R.string.METHOD_webservice_newWork);
                break;

            case INITIAL_PARAMETERS_NEW_WORK:
                m = contexto.getResources().getString(R.string.METHOD_webservice_initialParametersNewWork);
                break;

            case MY_WORKS:
                m = contexto.getResources().getString(R.string.METHOD_webservice_myWorks);
                break;

            case WORK_INFO:
                m = contexto.getResources().getString(R.string.METHOD_webservice_workInfo);
                break;

            case PROFESSIONAL_INFO:
                m = contexto.getResources().getString(R.string.METHOD_webservice_professionalInfo);
                break;

            case MODIFY_FAVORITES:
                m = contexto.getResources().getString(R.string.METHOD_webservice_modifyFavorites);
                break;

            case MY_FAVORITES:
                m = contexto.getResources().getString(R.string.METHOD_webservice_myFavorites);
                break;

            case SPINNER_VALUES:
                m = contexto.getResources().getString(R.string.METHOD_webservice_searchParameters);
                break;

            case PROFESSIONAL_SEARCH:
                m = contexto.getResources().getString(R.string.METHOD_webservice_professionalSearch);
                break;

            case USER_PROFILE:
                m = contexto.getResources().getString(R.string.METHOD_webservice_userProfile);
                break;

            case WORK_FOLLOWERS:
                m = contexto.getResources().getString(R.string.METHOD_webservice_workFollowers);
                break;

            case CLOSE_WORK:
                m = contexto.getResources().getString(R.string.METHOD_webservice_closeWork);
                break;

            case VOTE_WORK:
                m = contexto.getResources().getString(R.string.METHOD_webservice_voteWork);
                break;

            case REMOVE_WORK:
                m = contexto.getResources().getString(R.string.METHOD_webservice_removeWork);
                break;


            case MY_WORKS_PRO:
                m = contexto.getResources().getString(R.string.METHOD_webservice_myWorksPro);
                break;

            case WORK_SEARCH:
                m = contexto.getResources().getString(R.string.METHOD_webservice_workSearch);
                break;

            case MY_VOTES:
                m = contexto.getResources().getString(R.string.METHOD_webservice_myVotes);
                break;

            case PREMIUM_ACCESS:
                m = contexto.getResources().getString(R.string.METHOD_webservice_premiumAccess);
                break;

            case FOLLOW_WORK:
                m = contexto.getResources().getString(R.string.METHOD_webservice_followWork);
                break;

            case UNFOLLOW_WORK:
                m = contexto.getResources().getString(R.string.METHOD_webservice_unfollowWork);
                break;

            case PROFESSIONAL_PROFILE:
                m = contexto.getResources().getString(R.string.METHOD_webservice_professionalProfile);
                break;

            case PASSWORD_RESET:
                m = contexto.getResources().getString(R.string.METHOD_webservice_passwordReset);
                break;

            default:
                break;
        }

        if ( m.equalsIgnoreCase("POST") )
            metodo = Metodo.POST;
        else
            metodo = Metodo.GET;
    }


    // Indica el tipo de metodo de peticion que utiliza este servicio
    public ServicioWeb.Metodo getMetodo()
    {
        return metodo;
    }


    // Indica de que tipo de servicio web se trata
    public ServicioWeb.Tipo getTipo()
    {
        return tipo;
    }


    // Asigna la url correspondiente a ese tipo de servicio web
    // (metodo de clase privado)
    private void setUrl(ServicioWeb.Tipo t)
    {
        String protocolo, dominio, puerto, carpeta;

        // El protocolo y el puerto de conexion serán diferentes,
        // dependiendo de si el servicio web utiliza una conexión segura o no
        if (conexionSegura)
        {
            protocolo    = contexto.getResources().getString(R.string.URL_secure_protocol);
            puerto       = contexto.getResources().getString(R.string.URL_secure_port);
        }
        else
        {
            protocolo    = contexto.getResources().getString(R.string.URL_nonSecure_protocol);
            puerto       = contexto.getResources().getString(R.string.URL_nonSecure_port);
        }

        // El resto de elementos de la URL no varían, se use una conexión segura o no
        dominio      = contexto.getResources().getString(R.string.URL_domain);
        carpeta      = contexto.getResources().getString(R.string.URL_webservices_folder);

        // Dependiendo del tipo de servicio web, el nombre del servicio invocado será distinto
        String servicio = "";

        switch (t)
        {
            case LOGIN:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_login);
                break;

            case CHECK_SESSION:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_checkSession);
                break;

            case REGISTER:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_register);
                break;

            case NEW_WORK:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_newWork);
                break;

            case INITIAL_PARAMETERS_NEW_WORK:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_initialParametersNewWork);
                break;

            case MY_WORKS:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_myWorks);
                break;

            case WORK_INFO:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_workInfo);
                break;

            case PROFESSIONAL_INFO:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_professionalInfo);
                break;

            case MODIFY_FAVORITES:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_modifyFavorites);
                break;

            case MY_FAVORITES:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_myFavorites);
                break;

            case SPINNER_VALUES:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_searchParameters);
                break;

            case PROFESSIONAL_SEARCH:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_professionalSearch);
                break;

            case USER_PROFILE:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_userProfile);
                break;

            case WORK_FOLLOWERS:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_workFollowers);
                break;

            case CLOSE_WORK:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_closeWork);
                break;

            case VOTE_WORK:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_voteWork);
                break;

            case REMOVE_WORK:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_removeWork);
                break;


            case MY_WORKS_PRO:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_myWorksPro);
                break;

            case WORK_SEARCH:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_workSearch);
                break;

            case MY_VOTES:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_myVotes);
                break;

            case PREMIUM_ACCESS:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_premiumAccess);
                break;

            case FOLLOW_WORK:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_followWork);
                break;

            case UNFOLLOW_WORK:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_unfollowWork);
                break;

            case PROFESSIONAL_PROFILE:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_professionalProfile);
                break;

            case PASSWORD_RESET:
                servicio = contexto.getResources().getString(R.string.NAME_webservice_passwordReset);
                break;

            default:
                break;
        }

        url = Utils.crearUrl(protocolo, dominio, puerto, carpeta, servicio);
    }


    // Devuelve un String con la url del servicio web
    public String getUrl()
    {
        return url;
    }


    // Devuelve un int que representa el recurso String que describe la operación que realiza este servicio web
    public int getIdTituloOperacion()
    {
        int idTitulo;

        switch (tipo)
        {
            case LOGIN:
                idTitulo = R.string.titulo_servicio_LOGIN;
                break;

            case CHECK_SESSION:
                idTitulo = R.string.titulo_servicio_CHECK_SESSION;
                break;

            case REGISTER:
                idTitulo = R.string.titulo_servicio_REGISTER;
                break;

            case NEW_WORK:
                idTitulo = R.string.titulo_servicio_NEW_WORK;
                break;

            case INITIAL_PARAMETERS_NEW_WORK:
                idTitulo = R.string.titulo_servicio_INITIAL_PARAMETERS_NEW_WORK;
                break;

            case MY_WORKS:
                idTitulo = R.string.titulo_servicio_MY_WORKS;
                break;

            case WORK_INFO:
                idTitulo = R.string.titulo_servicio_WORK_INFO;
                break;

            case PROFESSIONAL_SEARCH:
                idTitulo = R.string.titulo_servicio_PROFESSIONAL_SEARCH;
                break;

            case PROFESSIONAL_INFO:
                idTitulo = R.string.titulo_servicio_PROFESSIONAL_INFO;
                break;

            case MODIFY_FAVORITES:
                idTitulo = R.string.titulo_servicio_MODIFY_FAVORITES;
                break;

            case MY_FAVORITES:
                idTitulo = R.string.titulo_servicio_MY_FAVORITES;
                break;

            case SPINNER_VALUES:
                idTitulo = R.string.titulo_servicio_SPINNER_VALUES;
                break;

            case USER_PROFILE:
                idTitulo = R.string.titulo_servicio_USER_PROFILE;
                break;

            case WORK_FOLLOWERS:
                idTitulo = R.string.titulo_servicio_WORK_FOLLOWERS;
                break;

            case CLOSE_WORK:
                idTitulo = R.string.titulo_servicio_CLOSE_WORK;
                break;

            case VOTE_WORK:
                idTitulo = R.string.titulo_servicio_VOTE_WORK;
                break;

            case REMOVE_WORK:
                idTitulo = R.string.titulo_servicio_REMOVE_WORK;
                break;

            case MY_WORKS_PRO:
                idTitulo = R.string.titulo_servicio_MY_WORKS_PRO;
                break;

            case WORK_SEARCH:
                idTitulo = R.string.titulo_servicio_WORK_SEARCH;
                break;

            case MY_VOTES:
                idTitulo = R.string.titulo_servicio_MY_VOTES;
                break;

            case PREMIUM_ACCESS:
                idTitulo = R.string.titulo_servicio_PREMIUM_ACCESS;
                break;

            case FOLLOW_WORK:
                idTitulo = R.string.titulo_servicio_FOLLOW_WORK;
                break;

            case UNFOLLOW_WORK:
                idTitulo = R.string.titulo_servicio_UNFOLLOW_WORK;
                break;

            case PROFESSIONAL_PROFILE:
                idTitulo = R.string.titulo_servicio_PROFESSIONAL_PROFILE;
                break;

            case PASSWORD_RESET:
                idTitulo = R.string.titulo_servicio_PASSWORD_RESET;
                break;

            default:
                idTitulo = R.string.titulo_servicio_desconocido;
                break;
        }

        return idTitulo;
    }


}
