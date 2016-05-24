package com.cdelgado.mimartillocom;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.widget.TextView;

import java.util.ArrayList;


public class VentanaResetPassword extends VentanaBase
{
    private GestorSesiones gestorSesion;    // solo se usará aquí para consultar el protocolo de conexión en las preferencias generales

    // Referencia a los controles de la interfaz de esta activity
    private TextView txtTipoAcceso;
    private EditText txtEmail;
    private Button btnRecuperar;

    protected ProgressDialog pDialog;
    private VentanaResetPassword estaVentana;

    // Tipo de usuario para el que se quiere recuperar la contraseña (particular o profesional)
    GestorSesiones.TipoUsuario tipoUsuario;

    // Información a mostrar al cargar el formulario de login (si es necesario)
    String motivo, emailSugerido;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_reset_password);

        // Referencia al propio objeto activity
        estaVentana = this;

        // Referencia a los objetos de la interfaz
        txtTipoAcceso   = (TextView)findViewById(R.id.txtTipoAcceso);
        txtEmail        = (EditText)findViewById(R.id.txtEmail);
        btnRecuperar    = (Button)findViewById(R.id.btnRecuperar);

        // Cuadro de progreso
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        String tituloVentana = getResources().getString(R.string.VentanaResetPassword_txt_titulo);
        setTitle(tituloVentana);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Recuperar la información pasada en el intent, si es que se pasó alguna
        Bundle info = this.getIntent().getExtras();

        if ( info!=null )
        {
            // Tipo de usuario que inicia la sesión
            if ( info.getSerializable("tipo_usuario") != null )
            {
                tipoUsuario = (GestorSesiones.TipoUsuario) info.getSerializable("tipo_usuario");
            }
            else
            {
                Log.e("VentanaResetPassword","No se ha indicado el tipo de usuario en el Intent");
                this.finish();
                return;
            }

            // Texto de la ventana indicando el tipo de acceso
            String txtTipo = "";

            switch (tipoUsuario)
            {
                case PARTICULAR:    txtTipo = getResources().getString(R.string.general_txt_tipoAcceso_particular);
                    gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PARTICULAR);
                    break;

                case PROFESIONAL:   txtTipo = getResources().getString(R.string.general_txt_tipoAcceso_profesional);
                    gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PROFESIONAL);
                    break;

                default:            break;
            }

            txtTipoAcceso.setText(txtTipo);


            // Si hay una sugerencia para la dirección de correo, mostrarla en el formulario
            emailSugerido = info.getString("emailSugerido");
            if (emailSugerido != null )
                txtEmail.setText(emailSugerido);
        }



        // Comportamiento del botón de recuperar password del usuario
        btnRecuperar.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Recoger el valor introducido por el usuario en el formulario
                    String email = txtEmail.getText().toString();

                    // Administrador de conexiones del sistema
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


                    // Si el campo email está vacío, indicar al usuario que lo rellene
                    if ( email.length()==0 )
                    {
                        String msg = getResources().getString(R.string.VentanaResetPassword_txt_faltanDatos);
                        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
                    }

                    // Si el texto de email no es una dirección de correo válida, indicarlo al usuario
                    else if ( !Utils.direccionEmailValida(email) )
                    {
                        String msg = getResources().getString(R.string.general_txt_emailInvalido);
                        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
                    }

                    // Si el formulario se rellenó correctamente,
                    // intentar conectar con el servidor para solicitar el reseteo de la contraseña.
                    else
                    {
                        resetPassword(email);
                    }
                }
            }
        );

    }


    // Llamar al servicio web que inicia el proceso de reiniciar el password del usuario
    // (NOTA: este método solo lanza la petición por red, la carga de los datos recibidos se realiza en procesarResultado)
    // (método de clase privado)
    private void resetPassword(String email_usuario)
    {
        // Administrador de conexiones del sistema
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
        if ( !Utils.dispositivoConectado(cm) )
        {
            String titulo = getResources().getString(R.string.VentanaResetPassword_txt_titulo);
            String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
        }

        // Si el dispositivo está conectado a la red, conectar con el servidor para solicitar el reseteo de la contraseña.
        else
        {
            // Construir el servicio web con los parámetros de entrada
            ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.PASSWORD_RESET, estaVentana);

            miServicioWeb.addParam("correo",email_usuario);

            String tipo = "";
            if ( tipoUsuario == GestorSesiones.TipoUsuario.PARTICULAR )         tipo = "particular";
            else if ( tipoUsuario == GestorSesiones.TipoUsuario.PROFESIONAL )   tipo = "profesional";

            miServicioWeb.addParam("tipo_usuario",tipo);


            // Construir el cliente para la consulta Http(s)
            ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


            // Crear una tarea asíncrona para conectar con el servicio web
            TareaSegundoPlano tareaResetPassword = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

            // Ejecutar la tarea de inicio de sesión (en un hilo aparte del principal)
            Log.d("VentanaResetPassword","Solicitando renicio de contraseña...");
            tareaResetPassword.execute();
        }
    }


    // Procesar la respuesta del servicio web
    // (se invoca cuando la tarea en segundo plano ha finalizado)
    @Override
    public void procesarResultado(TareaSegundoPlano tarea)
    {
        // Título del cuadro de diálogo de error (por si hubo errores en la tarea)
        int idTituloOperacion = tarea.getIdTituloOperacion();

        // Lista de posibles respuestas de error que puede recibir esta ventana y el tratamiento que debe dárse a cada una
        // (sin incluir las respuestas de sesion expirada, sesión inválida o usuario deshabilitado, que ya se tratan por defecto)
        ArrayList<ErrorServicioWeb> listaErrores = new ArrayList();

        // Posibles errores del servicio PASSWORD_RESET a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.PASSWORD_RESET, "ERR_UsuarioInexistente", false, R.string.msg_ResetPassword_ERR_UsuarioInexistente) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.PASSWORD_RESET, "ERR_EnviarCorreo", false, R.string.msg_ResetPassword_ERR_EnviarCorreo, idTituloOperacion) );

        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana,tarea,idTituloOperacion,"",listaErrores,tipoUsuario);


        // Si la respuesta se recibió correctamente,
        // mostrar un diálogo de confirmación al usuario y cerrar esta ventana
        if ( respuesta_OK  )
        {
            String msg = getResources().getString( R.string.msg_ResetPassword_OK_CorreoEnviado );
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Esta ventna no tiene opciones de menú
        //getMenuInflater().inflate(R.menu.menu_ventana_login, menu);

        return true;
    }

    // Opciones del menú de la Action Bar de esta ventana
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Botón home de la action bar (volver atras)
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
