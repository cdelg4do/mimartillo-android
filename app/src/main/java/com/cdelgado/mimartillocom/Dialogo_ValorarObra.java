package com.cdelgado.mimartillocom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

public class Dialogo_ValorarObra extends Dialog
{
    private Context contexto;

    private Comportamiento comportamiento;

    private String tituloDialogo, adjudicatario, tituloParaMostrar, infoParaMostrar;


    public interface Comportamiento
    {
        public void dialogoAceptar(String id_adjudicatario, String coste, float valorCalidad, float valorPrecio, String comentario);
        public void dialogoCancelar();
    }

    // Constructor de la clase (el diálogo mostrará el nombre del profesional al que se adjudicó la obra)
    public Dialogo_ValorarObra(Context ctx, String titulo, String idObra, String idAdjudicatario, String nombreAdjudicatario, Comportamiento comp)
    {
        super(ctx);

        comportamiento = comp;
        contexto = ctx;

        tituloParaMostrar = contexto.getResources().getString(R.string.dialogo_valorarObra_txt_adjudicatario);

        adjudicatario = idAdjudicatario;
        infoParaMostrar = nombreAdjudicatario;

        tituloDialogo = titulo;
    }

    // Constructor de la clase (el diálogo mostrará el título de la obra que se está valorando)
    public Dialogo_ValorarObra(Context ctx, String titulo, String idObra, String idAdjudicatario, Comportamiento comp, String tituloObra)
    {
        super(ctx);

        comportamiento = comp;
        contexto = ctx;

        tituloParaMostrar = contexto.getResources().getString(R.string.dialogo_valorarObra_txt_obra);

        adjudicatario = idAdjudicatario;
        infoParaMostrar = tituloObra;

        tituloDialogo = titulo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogo_valorar_obra);

        // Establecer el ancho del cuadro de dialogo al maximo
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


        // Referencia a los objetos de la interfaz del cuadro de diálogo
        TextView tituloInfoParaMostrar = (TextView) findViewById(R.id.dialog_tituloInfoParaMostrar);
        TextView txtInfoParaMostrar = (TextView) findViewById(R.id.dialog_txtInfoParaMostrar);
        final EditText txtCoste = (EditText) findViewById(R.id.dialog_txtCoste);
        final RatingBar rtnCalidad = (RatingBar) findViewById(R.id.dialog_rtnCalidad);
        final RatingBar rtnPrecio = (RatingBar) findViewById(R.id.dialog_rtnPrecio);
        final EditText txtComentario = (EditText) findViewById(R.id.dialog_txtComentario);
        Button btnAceptar  = (Button) findViewById(R.id.dialog_btnAceptar);
        Button btnCancelar = (Button) findViewById(R.id.dialog_btnCancelar);


        // Según el idioma utilizado, el símbolo de moneda irá antes o después del recuadro para el coste
        TextView simboloMoneda_delante = (TextView) findViewById(R.id.dialog_simboloDelante);
        TextView simboloMoneda_detras  = (TextView) findViewById(R.id.dialog_simboloDetras);

        if ( Utils.idiomaAplicacion().equalsIgnoreCase("es") )
        {
            simboloMoneda_delante.setVisibility(View.GONE);
            simboloMoneda_detras.setVisibility(View.VISIBLE);
        }
        else
        {
            simboloMoneda_delante.setVisibility(View.VISIBLE);
            simboloMoneda_detras.setVisibility(View.GONE);
        }



        // Título del cuadro de diálogo
        setTitle(tituloDialogo);

        // Mostrar el nombre del adjudicatario de la obra o el título de la obra (según corresponda)
        tituloInfoParaMostrar.setText(tituloParaMostrar);
        txtInfoParaMostrar.setText(infoParaMostrar);


        // Llamada al pulsar el botón de Aceptar del diálogo, si los datos del formulario son incorrectos, se muestra un mensaje de error
        // Si los datos son correctos, se invoca al método dialogoAceptar (que se implementará desde otra ubicación) y se cierra el cuadro de diálogo
        btnAceptar.setOnClickListener
        (
                new android.view.View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        String res = validarFormulario();

                        if (res == null)
                        {
                            comportamiento.dialogoAceptar(adjudicatario, txtCoste.getText().toString(), rtnCalidad.getRating(), rtnPrecio.getRating(), txtComentario.getText().toString());
                            Dialogo_ValorarObra.this.dismiss();
                        }
                        else
                            Utils.mostrarMensaje(contexto, res, Utils.TipoMensaje.TOAST,null,null);
                    }
                }
        );


        // Llamada al pulsar el botón de Cancelar del diálogo
        // Se muestra un mensaje de cancelación y se cierra el cuadro de diálogo
        btnCancelar.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    comportamiento.dialogoCancelar();
                    Dialogo_ValorarObra.this.dismiss();
                }
            }
        );

    }


    // Comprobación de los datos introducidos en el formulario
    // Devuelve null si los datos son correcto, o una cadena con el mensaje de error en caso contrario
    // (método privado)
    private String validarFormulario()
    {
        // Referencia a los objetos de la interfaz del cuadro de diálogo
        final EditText txtCoste = (EditText) findViewById(R.id.dialog_txtCoste);
        final RatingBar rtnCalidad = (RatingBar) findViewById(R.id.dialog_rtnCalidad);
        final RatingBar rtnPrecio = (RatingBar) findViewById(R.id.dialog_rtnPrecio);
        final EditText txtComentario = (EditText) findViewById(R.id.dialog_txtComentario);


        String msg = null;

        if ( Math.round(rtnCalidad.getRating()) < 1 )
        {
            msg = contexto.getResources().getString( R.string.dialogo_valorarObra_txt_msgFormulario1);
        }

        else if ( Math.round(rtnPrecio.getRating()) < 1 )
        {
            msg = contexto.getResources().getString( R.string.dialogo_valorarObra_txt_msgFormulario2);
        }

        else if ( txtComentario.getText().toString().equals("") )
        {
            msg = contexto.getResources().getString( R.string.dialogo_valorarObra_txt_msgFormulario3);
        }

        return msg;
    }

}
