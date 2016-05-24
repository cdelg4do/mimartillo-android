package com.cdelgado.mimartillocom;

import android.content.Intent;
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


public class VentanaLogin extends VentanaBase
{
    private GestorSesiones gestorSesion;    // solo se usará aquí para consultar el protocolo de conexión en las preferencias generales

    // Referencia a los controles de la interfaz de esta activity
    private Button btnOlvidado;
    private Button btnLogin;
    private Button btnRegistrar;
    private EditText txtEmail;
    private EditText txtPassword;
    private TextView txtTipoAcceso;

    protected ProgressDialog pDialog;
    private VentanaLogin estaVentana;

    // Tipo de usuario que inicia la sesion (particular o profesional)
    GestorSesiones.TipoUsuario tipoUsuario;

    // Usuario con el que se intentará iniciar la sesión (su dirección de correo)
    private String email;

    // Información a mostrar al cargar el formulario de login (si es necesario)
    String motivo, emailSugerido;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_login);

        // Referencia al propio objeto activity
        estaVentana = this;

        // Obtener la referencia a los controles al crear el objeto
        txtTipoAcceso   = (TextView)findViewById(R.id.txtTipoAcceso);
        btnOlvidado     = (Button)findViewById(R.id.btnOlvidado);
        btnLogin        = (Button)findViewById(R.id.btnLogin);
        btnRegistrar    = (Button)findViewById(R.id.btnRegistrar);
        txtEmail        = (EditText)findViewById(R.id.txtEmail);
        txtPassword     = (EditText)findViewById(R.id.txtPassword);

        // Cuadro de progreso
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        String tituloVentana = getResources().getString(R.string.VentanaLogin_txt_titulo);
        setTitle(tituloVentana);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Recuperar la información pasada en el intent, si es que se pasó alguna
        Bundle info = this.getIntent().getExtras();

        if ( info!=null )
        {
            String titulo = getResources().getString(R.string.titulo_servicio_LOGIN); // Para los mensajes de error

            // Tipo de usuario que inicia la sesión
            if ( info.getSerializable("tipo_usuario") != null )
            {
                tipoUsuario = (GestorSesiones.TipoUsuario) info.getSerializable("tipo_usuario");
            }
            else
            {
                Log.e("VentanaLogin","No se ha indicado el tipo de usuario en el Intent");
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


            // Mensaje informando del motivo por el que se muestra la ventana de login
            // (algún intento previo de inicio de sesión falló o las credenciales almacenadas caducaron)
            motivo = info.getString("motivo");

            if (motivo != null)
            {
                Utils.mostrarMensaje(this, motivo, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.INFO, titulo);
            }

            // Si hay una sugerencia para la dirección de correo, mostrarla en el formulario
            emailSugerido = info.getString("emailSugerido");
            if (emailSugerido != null )
                txtEmail.setText(emailSugerido);
        }



        // Comportamiento del botón de inicio de sesión al pulsarlo
        btnLogin.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Valores introducidos en el formulario
                    email           = txtEmail.getText().toString();    // Campo del objeto ventana
                    String password = txtPassword.getText().toString(); // Variable temporal, no se almacena

                    // Administrador de conexiones del sistema
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


                    // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
                    if ( !Utils.dispositivoConectado(cm) )
                    {
                        String titulo = getResources().getString(R.string.titulo_servicio_LOGIN);
                        String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
                    }

                    // Si algún campo está vacío, indicar al usuario que rellene ambos
                    else if ( (email.length()==0) || (password.length()==0))
                    {
                        String msg = getResources().getString(R.string.VentanaLogin_txt_faltanDatos);
                        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
                    }

                    // Si el texto de email no es una dirección de correo válida, indicarlo al usuario
                    else if ( !Utils.direccionEmailValida(email) )
                    {
                        String msg = getResources().getString(R.string.general_txt_emailInvalido);
                        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
                    }

                    // Si la password no cumple con los requisitos de longitud, indicarlo al usuario
                    else if ( !Utils.passwordValida(password) )
                    {
                        String msg = getResources().getString(R.string.general_txt_passwordInvalido);
                        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
                    }

                    // Si los campos se rellenaron correctamente y el dispositivo está conectado a la red,
                    // conectar con el servidor para intentar iniciar sesión con esas credenciales.
                    else
                    {
                      /*
                        // Prueba del servicio web (sin conexión al servidor)
                        final Info_Login datosSesionSumulados = new Info_Login("Nombre simulado","1","fakeSessionId","default_profile_pic");
                        ArrayList<ContenidoServicioWeb> contenidoRespuesta = new ArrayList() {{ add(datosSesionSumulados); }};
                        RespuestaServicioWeb respuestaSimulada = new RespuestaServicioWeb("OK", "OK_RespuestaSimulada", contenidoRespuesta);

                        ServicioWeb miServicioWeb = ServicioWeb.crearServicioDePrueba(estaVentana, ServicioWeb.Tipo.LOGIN, null, respuestaSimulada);
                      */


                        // Construir el servicio web con los parámetros de entrada
                        ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.LOGIN, estaVentana);

                        miServicioWeb.addParam("correo",email);
                        miServicioWeb.addParam("password",password);

                        String tipo = "";
                        if ( tipoUsuario == GestorSesiones.TipoUsuario.PARTICULAR )
                            tipo = "particular";
                        else if ( tipoUsuario == GestorSesiones.TipoUsuario.PROFESIONAL )
                            tipo = "profesional";

                        miServicioWeb.addParam("tipo_usuario",tipo);


                        // Construir el cliente para la consulta Http(s)
                        ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                        // Crear una tarea asíncrona para conectar con el servicio web
                        TareaSegundoPlano tareaIniciarSesion = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                        // Ejecutar la tarea de inicio de sesión (en un hilo aparte del principal)
                        Log.d("VentanaLogin","Iniciando sesión segundo plano...");
                        tareaIniciarSesion.execute();
                    }
                }
            }
        );



        // Comportamiento del botón de registrar al pulsarlo
        // (ir a la ventana de registro de nuevo usuario particularo de nuevo usuario profesional)
        btnRegistrar.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(estaVentana, VentanaRegistro.class);

                    Bundle b = new Bundle();
                    b.putSerializable("tipo_usuario", tipoUsuario);
                    intent.putExtras(b);

                    startActivity(intent);
                }
            }
        );



        // Comportamiento del botón de password olvidado
        // (ir a la ventana de reinicio de password)
        btnOlvidado.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(VentanaLogin.this, VentanaResetPassword.class);

                    Bundle b = new Bundle();
                    b.putSerializable("tipo_usuario", tipoUsuario);

                    if ( Utils.direccionEmailValida(txtEmail.getText().toString()) )
                        b.putString("emailSugerido", txtEmail.getText().toString() );

                    intent.putExtras(b);

                    startActivity(intent);
                }
            }
        );


    }


    // Procesar la respuesta del servicio web
    // (se invoca cuando la tarea en segundo plano ha finalizado)
    @Override
    public void procesarResultado(TareaSegundoPlano tarea)
    {
        // Título del cuadro de diálogo de error (por si hubo errores en la tarea)
        // y email del usuario (por si hubo problemas con la sesión y hay que volver a la ventana de Login)
        int idTituloOperacion = tarea.getIdTituloOperacion();

        String email = txtEmail.getText().toString();


        // Lista de posibles respuestas de error que puede recibir esta ventana y el tratamiento que debe dárse a cada una
        // (sin incluir las respuestas de sesion expirada, sesión inválida o usuario deshabilitado, que ya se tratan por defecto)
        ArrayList<ErrorServicioWeb> listaErrores = new ArrayList();

        // Posibles errores del servicio LOGIN a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.LOGIN, "ERR_PasswordIncorrecto", false, R.string.msg_Login_ERR_PasswordIncorrecto) );


        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana,tarea,idTituloOperacion,email,listaErrores,tipoUsuario);


        // Si la respuesta se recibió correctamente, almacenar los datos de la nueva sesión
        // y abrir la ventana principal correspondiente (usuario particular o profesional)
        if ( respuesta_OK  )
        {
            // Servicio web asociado a la tarea, que contiene la respuesta del servidor
            ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

            // Almacenar los datos de la sesión creada
            gestorSesion = new GestorSesiones(estaVentana,tipoUsuario);
            Info_Login info = (Info_Login) miServicioWeb.getRespuesta().getContenido().get(0);

            gestorSesion.guardarSesion(info.getNombre(), this.email, info.getId(), info.getSesion(), info.getAvatar());


            // Si hubo algún problema al cargar la imagen del avatar de usuario,
            // se informará al usuario al cargar la siguiente ventana
            String detalle = miServicioWeb.getRespuesta().getDetalle();

            boolean fallo_avatar = false;
            if ( detalle.equals("OK_FalloAvatar") )
                fallo_avatar = true;


            // Dependiendo del tipo de usuario (particular/profesional) se cargará una ventana u otra
            if ( tipoUsuario == GestorSesiones.TipoUsuario.PARTICULAR )
            {
                // Pasar a la ventana principal del usuario particular
                Intent intent = new Intent(estaVentana, VentanaParticular.class);

                Bundle bundle = new Bundle();

                if ( fallo_avatar ) bundle.putBoolean("fallo_avatar",true);
                else                bundle.putBoolean("fallo_avatar", false);

                intent.putExtras(bundle);

                finish();
                startActivity(intent);
            }
            else if ( tipoUsuario == GestorSesiones.TipoUsuario.PROFESIONAL )
            {
                // Pasar a la ventana principal del usuario profesional
                Intent intent = new Intent(estaVentana, VentanaProfesional.class);

                Bundle bundle = new Bundle();

                if ( fallo_avatar ) bundle.putBoolean("fallo_avatar",true);
                else                bundle.putBoolean("fallo_avatar", false);

                intent.putExtras(bundle);

                finish();
                startActivity(intent);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // No hay menú de opciones para esta ventana
        return true;
    }

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
