package com.cdelgado.mimartillocom;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


public abstract class VentanaBase extends ActionBarActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Establecer el manejador de excepciones para las activities
        Thread.setDefaultUncaughtExceptionHandler( new ManejadorExcepciones(this) );

        // Por defecto, no se muestra la Action Bar
        getSupportActionBar().hide();
    }


    @Override
    public void onResume()
    {
        super.onResume();

    }


    @Override
    public void onPause()
    {
        super.onPause();

    }


    // Este método sera invocado por los objetos TareaSegundoPlano cuando finalicen su hilo de ejecución
    // y debe ser implementado por todas las subclases que extiendend VentanaBase
    public abstract void procesarResultado(TareaSegundoPlano tarea);


}
