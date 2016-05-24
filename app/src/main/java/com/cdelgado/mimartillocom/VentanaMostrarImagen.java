package com.cdelgado.mimartillocom;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;


public class VentanaMostrarImagen extends ActionBarActivity
{
    private ImageView imagen;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_mostrar_imagen);

        // Referencia a los objetos de la interfaz
        imagen = (ImageView) findViewById(R.id.img_pantallaCompleta);

        // Recuperar la información pasada en el bundle
        // (mensaje a mostrar cuando se carga la ventana)
        Log.d("VentanaMostrarImagen","Recuperando informacion de imagen...");

        Bundle extras = getIntent().getExtras();
        String msg = extras.getString("mensaje");

        // No mostrar la action bar para esta ventana
        getSupportActionBar().hide();


        // Recuperar los datos de la imagen y construir un bitmap con ellos
        // (están almacenados como una cadena temporal en las preferencias generales)
        Bitmap bitmap = null;
        GestorSesiones gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PARTICULAR);  // No importa el tipo indicado

        boolean errorImagen = false;

        // Obtener la cadena almacenada en las preferencias generales (si existe)
        String img_base64 = gestorSesion.getDatosTemporales();

        // Si no había datos temporales, error
        if ( img_base64 == null )
        {
            Log.e("VentanaMostrarImagen","No pudo cargarse ningún dato temporal de las preferencias");
            errorImagen = true;
        }
        else
        {
            bitmap = Utils.descodifica_imagen_base64( img_base64 );

            if ( bitmap == null )
            {
                Log.e("VentanaMostrarImagen","Fallo al decodificar los datos temporales como un bitmap");
                errorImagen = true;
            }
        }

        // Si hubo algún error que impide mostrar la imagen, mostrar mensaje de error y cerrar la ventana
        if (errorImagen)
        {
            msg = getResources().getString( R.string.VentanaMostrarImagen_txt_msgFalloImagen );
            Utils.mostrarMensaje(this,msg, Utils.TipoMensaje.TOAST,null,null);

            finish();
        }

        // Si no, mostrar la imagen y el mensaje original
        else
        {
            Log.d("VentanaMostrarImagen","Mostrando la imagen...");

            imagen.setImageBitmap(bitmap);

            // Agregar al objeto imagen la funcionalidad de ampliable mediante gestos
            // (el objeto mAttacher devuelto no es necesario utilizarlo)
            PhotoViewAttacher mAttacher = new PhotoViewAttacher(imagen);

            Utils.mostrarMensaje(this,msg, Utils.TipoMensaje.TOAST,null,null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_ventana_mostrar_imagen, menu);
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
