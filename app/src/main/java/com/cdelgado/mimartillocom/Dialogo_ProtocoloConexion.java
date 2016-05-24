package com.cdelgado.mimartillocom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


public class Dialogo_ProtocoloConexion extends Dialog
{
    private Context ctx;
    private GestorSesiones gestorSesion;    // Solo se usará para consultar el protocolo de conexión en las preferencias generales

    private Switch swSeguridad;
    private Button btnOk;
    private TextView txtEstadoSeguridad;



    public Dialogo_ProtocoloConexion(Context c)
    {
        super(c);
        ctx = c;

        gestorSesion = new GestorSesiones(ctx, GestorSesiones.TipoUsuario.PARTICULAR);  // en este caso el tipo de usuario es irrelevante

        String titulo = ctx.getResources().getString(R.string.VentanaInicio_txt_problemasConexion);
        this.setTitle(titulo);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogo_protocolo_conexion);

        // Referencia a los objetos de la interfaz
        swSeguridad         = (Switch) findViewById(R.id.swSeguridad);
        btnOk               = (Button) findViewById(R.id.btnOk);
        txtEstadoSeguridad  = (TextView) findViewById(R.id.txtEstadoSeguridad);


        // Consultar en las preferencias generales del cliente el protocolo de conexión que se usa actualmente
        // y actualizar la interfaz
        if ( gestorSesion.usarConexionSegura() )
        {
            swSeguridad.setChecked(true);
            txtEstadoSeguridad.setText( ctx.getResources().getString(R.string.VentanaPerfil_txt_seguridadConexionActivada) );
            txtEstadoSeguridad.setTextColor( ctx.getResources().getColor(R.color.verde) );
        }
        else
        {
            swSeguridad.setChecked(false);
            txtEstadoSeguridad.setText( ctx.getResources().getString(R.string.VentanaPerfil_txt_seguridadConexionDesactivada) );
            txtEstadoSeguridad.setTextColor( ctx.getResources().getColor(R.color.rojo) );
        }

        // Comportamiento del switch del protocolo de conexión al cambiar de estado
        // (registrar el cambio en las preferencias generales de la aplicación y actualizar la interfaz)
        swSeguridad.setOnCheckedChangeListener
        (
            new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean activarSeguridad)
                {
                    gestorSesion.setConexionSegura(activarSeguridad);

                    if (activarSeguridad)
                    {
                        txtEstadoSeguridad.setText( ctx.getResources().getString(R.string.dialogo_protocoloConexion_txt_conexion_activada) );
                        txtEstadoSeguridad.setTextColor( ctx.getResources().getColor(R.color.verde) );
                    }
                    else
                    {
                        txtEstadoSeguridad.setText( ctx.getResources().getString(R.string.dialogo_protocoloConexion_txt_conexion_desactivada) );
                        txtEstadoSeguridad.setTextColor( ctx.getResources().getColor(R.color.rojo));
                    }
                }
            }
        );


        // Comportamiento del botón de Aceptar
        // (cerrar este cuadro de diálogo)
        btnOk.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Dialogo_ProtocoloConexion.this.dismiss();
                }
            }
        );
    }

}