package com.example.olhovirtual.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.example.olhovirtual.R;

public class VisualizarEventoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_evento);

        inicializarComponentes();


        //Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarVisualizarEvento);
        toolbar.setTitle("Evento Teste");
        toolbar.setSubtitle("Sub título");
        setSupportActionBar(toolbar);

    }

    public void inicializarComponentes() {



    }
}