package com.cdelgado.mimartillocom;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;


public class VentanaErrores extends Activity
{
    // Referencia a los controles de la interfaz de esta activity
    private TextView txtInfoDebug;
    private Button btnCopiarPortapapeles;
    private Button btnAceptarError;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_errores);

        // Obtener la referencia a los controles al crear el objeto
        txtInfoDebug = (TextView)findViewById(R.id.txtInfoDebug);
        btnCopiarPortapapeles = (Button) findViewById(R.id.btnCopiarPortapapeles);
        btnAceptarError = (Button) findViewById(R.id.btnAceptarError);

        // Recuperar la información pasada en el intent
        Bundle info = this.getIntent().getExtras();
        String cadena = info.getString("debugInfo");

        // Mostrar la información del error en la activity
        txtInfoDebug.setText(cadena);


        // Comportamiento del botón de Copiar al portapapeles al pulsarlo
        btnCopiarPortapapeles.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Copiar la informacion del error al portapapeles (válido para APIs viejas y nuevas)
                    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
                    {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText( txtInfoDebug.getText().toString() );
                    }
                    else
                    {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText( "Texto copiado", txtInfoDebug.getText().toString() );
                        clipboard.setPrimaryClip(clip);
                    }

                    // Mostrar toast confirmando la operación
                    String txtConfirmacion = getResources().getString(R.string.VentanaErrores_txt_infoCopiada);
                    Toast.makeText(getApplicationContext(),txtConfirmacion,Toast.LENGTH_SHORT).show();
                }
            }
        );


        // Comportamiento del botón de Aceptar al pulsarlo
        btnAceptarError.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            }
        );

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ventana_errores, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
