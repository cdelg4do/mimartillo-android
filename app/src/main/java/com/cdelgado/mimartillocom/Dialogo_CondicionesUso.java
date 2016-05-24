package com.cdelgado.mimartillocom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


public class Dialogo_CondicionesUso extends Dialog
{
    private Context ctx;

    private TextView txt_contenido;
    private Button btnOk;


    public Dialogo_CondicionesUso(Context c)
    {
        super(c);
        ctx = c;

        String titulo = ctx.getResources().getString(R.string.dialogo_condicionesUso_txt_titulo);
        this.setTitle(titulo);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogo_condiciones_uso);

        // Esto es necesario para que el cuadro de diálogo ocupe toda la ventana
        // (debe invocarse después de la llamada a setContentView() )
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.FILL_PARENT;
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

        // Referencia a los objetos de la interfaz
        txt_contenido = (TextView) findViewById(R.id.txt_contenido);
        btnOk = (Button) findViewById(R.id.btnOk);

        // Hacer que el cuadro de texto sea scrollable
        // (debe tener las propiedades android:maxLines y android:scrollbars="vertical")
        txt_contenido.setMovementMethod( new ScrollingMovementMethod() );

        // Comportamiento del botón de Aceptar
        // (cerrar este cuadro de diálogo)
        btnOk.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Dialogo_CondicionesUso.this.dismiss();
                }
            }
        );
    }

}
