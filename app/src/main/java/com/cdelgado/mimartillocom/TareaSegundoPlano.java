package com.cdelgado.mimartillocom;


import android.app.ProgressDialog;
import android.os.AsyncTask;


// Tarea asíncrona para realizar la consulta a un servicio web
// Tipos de parametros:
//      Params ---> Void: parámetros de doInBackground()
//      Progress -> Integer: parámetros de onProgressUpdate() y de la llamada a publishProgress() (no usados aqui)
//      Result ---> Boolean:   parámetro de entrada de onPostExecute() y devuelto por doInBackground()

public class TareaSegundoPlano extends AsyncTask<Void,Integer,Boolean>
{
    boolean timeoutCliente;  // Variable que registra si el cliente web superó el tiempo máximo de espera
    boolean haFallado;       // Variable de estado de la tarea: indica si ha habido fallos

    private ClienteServicioWeb miCliente;   // Cliente web para realizar la consulta http
    private VentanaBase ventana;            // Referencia a la activity que invoca esta tarea asíncrona
    private ProgressDialog pDialog;         // Dialogo de progreso para mostrar durante la tarea

    private String tituloProgreso;  // Título para el diálogo de progreso
    private String msgProgreso;     // Mensaje para el diálogo de progreso



    // Constructor de la clase: la interfaz de la aplicación se detendrá mientras se procesa el hilo en segundo plano
    // (se mostrará un diálogo de progeso, con el título de la operación que se realiza y un mensaje de progreso)
    // Si los parámetros t(ítulo) o m(ensaje) son null, se mostrarán unos valores por defecto.
    public TareaSegundoPlano(ClienteServicioWeb c, VentanaBase v, ProgressDialog p, String t, String m)
    {
        super();

        miCliente   = c;
        ventana     = v;
        pDialog     = p;

        timeoutCliente  = false;
        haFallado       = false;

        // Si el título indicado es null, se mostrará un título genérico (que viene dado por el servicio web que se invoca)
        if ( t != null )    tituloProgreso  = t;
        else                tituloProgreso  = v.getResources().getString( c.getServicioWeb().getIdTituloOperacion() );

        // Si el mensaje indicado es null, se mostrará un mensaje de progreso genérico
        if ( m != null )    msgProgreso = m;
        else                msgProgreso     = v.getResources().getString( R.string.txt_progresoSegundoPlano );
    }



    // Constructor de la clase: el usuario podrá seguir interactuando con la aplicación mientras se procesa el hilo en segundo plano
    // (no se mostrará ningún diálogo de progeso)
    public TareaSegundoPlano(ClienteServicioWeb c, VentanaBase v)
    {
        super();

        miCliente   = c;
        ventana     = v;
        pDialog     = null;

        timeoutCliente  = false;
        haFallado       = false;

        tituloProgreso  = null;
        msgProgreso     = null;
    }



    // Indica si el cliente web superó el tiempo máximo de espera
    public boolean timeoutCliente()
    {
        return timeoutCliente;
    }


    // Indica si hubo fallos en la ejecución de la tarea
    public boolean haFallado()
    {
        return haFallado;
    }


    // Devuelve una referencia al cliente que se ejecuta en esta tarea
    public ClienteServicioWeb getCliente ()
    {
        return miCliente;
    }


    // Devuelve un int con la referencia al título de la operación que realiza esta tarea
    public int getIdTituloOperacion()
    {
        return miCliente.getServicioWeb().getIdTituloOperacion();
    }


    // Tarea a realizar ANTES de la consulta, en el mismo hilo que la Interfaz de usuario
    // (crear y mostrar el diálogo de progreso)
    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        if ( pDialog != null )
        {
            pDialog = new ProgressDialog(ventana);

            if (tituloProgreso != null)
                pDialog.setTitle(tituloProgreso);

            pDialog.setMessage(msgProgreso);
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }


    // Tarea a realizar en segundo plano, en un hilo distinto al de la Interfaz de usuario
    @Override
    protected Boolean doInBackground(Void... params)
    {
        // Hacer la petición Http y esperar a recibir respuesta
        boolean sinErrores = miCliente.peticionServicioWeb();

        // Registrar si el cliente web superó el tiempo máximo de espera
        timeoutCliente = miCliente.timeout();

        // Si se produjo un timeout o algo falló mientras se esperaba por la respuesta, cancelar la operación
        if ( timeoutCliente || !sinErrores )    {   cancel(true);   }

        return true;
    }


    // Tarea a realizar DESPUÉS de completar el proceso en segundo plano
    // (desechar el cuadro de progreso y devolver el control a la activity para analizar la respuesta recibida)
    @Override
    protected void onPostExecute(Boolean result)
    {
        if ( pDialog != null )
            pDialog.dismiss();

        ventana.procesarResultado(this);
    }


    // Tarea a realizar DESPUÉS de cancelar el proceso en segundo plano (desechar el cuadro de progreso,
    // levantar el flag de fallo y devolver el control a la activity para analizar la respuesta recibida)
    @Override
    protected void onCancelled()
    {
        haFallado = true;

        if ( pDialog != null )
            pDialog.dismiss();

        ventana.procesarResultado(this);
    }

}
