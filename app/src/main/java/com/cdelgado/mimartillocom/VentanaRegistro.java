package com.cdelgado.mimartillocom;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class VentanaRegistro extends VentanaBase
{
    // Referencia a los controles de la interfaz de esta activity
    private TextView txtTipoCuenta;
    private Button btnCrearCuenta;
    private EditText txtEmail;
    private EditText txtNombre;
    private EditText txtPassword1;
    private EditText txtPassword2;
    private CheckBox chkCondiciones;
    private TextView txtVerCondiciones;

    private GestorSesiones.TipoUsuario tipoUsuario;     // Indica el tipo de usuario que se registra (particular o profesional)
    boolean vistaParticular;
    protected ProgressDialog pDialog;
    private VentanaRegistro estaVentana;
    private GestorSesiones gestorSesion;    // en esta ventana, el gestor de sesiones solo se usa para consultar el protocolo de conexión



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_registro);

        // Referencia al propio objeto activity
        estaVentana = this;

        // Obtener la referencia a los controles al crear el objeto
        txtTipoCuenta       = (TextView)findViewById(R.id.txtTipoCuenta);
        btnCrearCuenta      = (Button)findViewById(R.id.btnCrearCuenta);
        txtEmail            = (EditText)findViewById(R.id.txtEmail);
        txtNombre           = (EditText)findViewById(R.id.txtNombre);
        txtPassword1        = (EditText)findViewById(R.id.txtPassword1);
        txtPassword2        = (EditText)findViewById(R.id.txtPassword2);
        chkCondiciones      = (CheckBox)findViewById(R.id.chkCondiciones);
        txtVerCondiciones   = (TextView)findViewById(R.id.txtVerCondiciones);

        // Cuadro de progreso
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        String tituloVentana = getResources().getString(R.string.VentanaRegistro_txt_titulo);
        setTitle(tituloVentana);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Datos pasados en el intent
        Bundle info = this.getIntent().getExtras();

        // Recuperar la información de si el usuario es un particular o un profesional pasada en el intent, si es que se pasó alguna
        // (por defecto, se considera que es un particular)
        vistaParticular = true;
        if ( info!=null && (info.getSerializable("tipo_usuario"))!=null )
        {
            if ( (GestorSesiones.TipoUsuario) info.getSerializable("tipo_usuario") == GestorSesiones.TipoUsuario.PROFESIONAL )
                vistaParticular = false;
        }


        // Gestor de sesiones (para consultar el protocolo de conexión en las preferencias generales)
        // y texto informativo acordes al tipo de cuenta a crear
        if ( vistaParticular )
        {
            tipoUsuario = GestorSesiones.TipoUsuario.PARTICULAR;
            txtTipoCuenta.setText( getResources().getString(R.string.VentanaRegistro_txt_tipoParticular) );
        }
        else
        {
            tipoUsuario = GestorSesiones.TipoUsuario.PROFESIONAL;
            txtTipoCuenta.setText( getResources().getString(R.string.VentanaRegistro_txt_tipoProfesional) );
        }

        gestorSesion = new GestorSesiones(estaVentana, tipoUsuario);


        // Comportamiento del botón de ver condiciones de servicio al pulsarlo
        txtVerCondiciones.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Dialogo_CondicionesUso dialogo = new Dialogo_CondicionesUso(estaVentana);
                    dialogo.show();
                }
            }
        );


        // Comportamiento del botón de crear cuenta al pulsarlo
        btnCrearCuenta.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Tipo de usuario que se está creando
                    String tipo_usuario;
                    if ( vistaParticular )  tipo_usuario = "particular";
                    else                    tipo_usuario = "profesional";

                    // Valores introducidos en el formulario
                    String email = txtEmail.getText().toString();
                    String nombre = txtNombre.getText().toString();
                    String password1 = txtPassword1.getText().toString();
                    String password2 = txtPassword2.getText().toString();

                    // Idioma del cliente
                    String idioma = Utils.idiomaAplicacion();

                    // Administrador de conexiones del sistema
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


                    // Si algún campo está vacío, indicar al usuario que rellene ambos
                    if ( (email.length() == 0) || (nombre.length() == 0) || (password1.length() == 0) || (password2.length() == 0) )
                    {
                        String msg = getResources().getString(R.string.VentanaRegistro_txt_msgFaltanDatos);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    // Si el texto de email no es una dirección de correo válida, indicarlo al usuario
                    else if (!Utils.direccionEmailValida(email))
                    {
                        String msg = getResources().getString(R.string.VentanaRegistro_txt_msgEmailInvalido);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    // Si las passwords no coinciden, indicarlo al usuario
                    else if ( !password1.equals(password2) )
                    {
                        String msg = getResources().getString(R.string.VentanaRegistro_txt_msgPasswordsDistintas);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    // Si la password no cumple con los requisitos de longitud, indicarlo al usuario
                    else if (!Utils.passwordValida(password1))
                    {
                        String msg = getResources().getString(R.string.general_txt_passwordInvalido);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    // Si el checkbox de terminos y condiciones no está marcado, indicarlo al usuario
                    else if ( !chkCondiciones.isChecked() )
                    {
                        String msg = getResources().getString(R.string.VentanaRegistro_txt_msgAceptarCondiciones);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    // Si el dispositivo no esta conectado a la red, mostrar aviso al usuario
                    else if (!Utils.dispositivoConectado(cm))
                    {
                        String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    // Si los campos se rellenaron correctamente y el dispositivo está conectado a la red,
                    // conectar con el servidor para intentar iniciar sesión con esas credenciales.
                    else
                    {
                        // Construir el servicio web con los parámetros de entrada
                        ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.REGISTER, estaVentana);

                        miServicioWeb.addParam("nombre",nombre);
                        miServicioWeb.addParam("correo",email);
                        miServicioWeb.addParam("password",password1);
                        miServicioWeb.addParam("tipo_usuario",tipo_usuario);
                        miServicioWeb.addParam("idioma",idioma);


                        // Construir el cliente para la consulta Http (indicando timeouts para la conexiï¿½n y la recepciï¿½n de datos)
                        ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                        // Crear una tarea asíncrona para conectar con el servicio web
                        TareaSegundoPlano tareaRegistrar = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                        // Ejecutar la tarea de inicio de sesion (en un hilo aparte del principal)
                        Log.d("VentanaRegistro","Iniciando registro de usuario en segundo plano...");
                        tareaRegistrar.execute();
                    }

                }
            }
        );

    }



    @Override
    public void procesarResultado( TareaSegundoPlano tarea )
    {
        // Título del cuadro de diálogo de error (por si hubo errores en la tarea)
        int idTituloOperacion = tarea.getIdTituloOperacion();


        // Lista de posibles respuestas de error que puede recibir esta ventana y el tratamiento que debe dárse a cada una
        // (sin incluir las respuestas de sesion expirada, sesión inválida o usuario deshabilitado, que ya se tratan por defecto)
        ArrayList<ErrorServicioWeb> listaErrores = new ArrayList();

        // Posibles errores del servicio REGISTER a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.REGISTER, "ERR_UsuarioRepetido", false, R.string.msg_RegistroUsuario_ERR_UsuarioRepetido) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.REGISTER, "ERR_EnviarCorreo", false, R.string.msg_RegistroUsuario_ERR_EnviarCorreo, idTituloOperacion) );

        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, "", listaErrores, tipoUsuario);

        // Si la respuesta se recibió correctamente (OK)
        if ( respuesta_OK )
        {
            // Mostrar mensaje de confirmación al usuario
            String msg = getResources().getString( R.string.msg_RegistrarUsuario_OK_UsuarioCreado );

            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

            // Cerrar la ventana de registro y pasar a la ventana de inicio de sesión,
            // con el email del usuario recién creado ya introducido en el formulario
            Intent intent = new Intent(estaVentana,VentanaLogin.class);

            Bundle info = new Bundle();
            info.putString( "emailSugerido", txtEmail.getText().toString() );
            intent.putExtras(info);

            finish();
            startActivity(intent);
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
