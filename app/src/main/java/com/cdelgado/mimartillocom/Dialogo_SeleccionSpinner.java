package com.cdelgado.mimartillocom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class Dialogo_SeleccionSpinner extends Dialog
{
    private Context mContext;
    private Spinner mSpinner;

    private Comportamiento comportamiento;

    private ArrayList<Info_ElementoSpinner> items;
    private String opcionDefecto;



    public interface Comportamiento
    {
        public void elementoSeleccionado(int pos, long id, String item);
        public void seleccionCancelada();
    }


    public Dialogo_SeleccionSpinner(Context context, String titulo, ArrayList<Info_ElementoSpinner> list, String defecto, Comportamiento comp)
    {
        super(context);
        comportamiento = comp;
        mContext = context;

        items = list;

        this.setTitle(titulo);

        opcionDefecto = defecto;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialogo_seleccion_spinner);
        mSpinner = (Spinner) findViewById (R.id.dialog_spinner);

        Adapter_Spinner miAdapter = new Adapter_Spinner(mContext,R.layout.elemento_spinner,items,opcionDefecto);
        mSpinner.setAdapter(miAdapter);

        Button buttonOK = (Button) findViewById(R.id.dialogOK);
        Button buttonCancel = (Button) findViewById(R.id.dialogCancel);

        buttonOK.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int pos = mSpinner.getSelectedItemPosition();

                    long id  = mSpinner.getItemIdAtPosition(pos);

                    String item = (String) mSpinner.getSelectedItem();

                    comportamiento.elementoSeleccionado(pos, id, item);
                    Dialogo_SeleccionSpinner.this.dismiss();
                }
            }
        );

        buttonCancel.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    comportamiento.seleccionCancelada();
                    Dialogo_SeleccionSpinner.this.dismiss();
                }
            }
        );

    }
}