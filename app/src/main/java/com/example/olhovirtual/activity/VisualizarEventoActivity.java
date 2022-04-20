package com.example.olhovirtual.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.olhovirtual.MapsActivity;
import com.example.olhovirtual.R;
import com.example.olhovirtual.model.Evento;

public class VisualizarEventoActivity extends AppCompatActivity {

    private TextView campoDescricao,campoHorarioAtendimento,campoValores;
    private ImageView imagemEvento;
    private Button botaoLocalizacao,botaoComentarios;
    private Evento eventoDestinatario;
    private Context context;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_evento);

        inicializarComponentes();


        //Recuperar dados do Evento selecionado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            eventoDestinatario =(Evento) bundle.getSerializable("eventoSelecionado");
            campoDescricao.setText(eventoDestinatario.getDescricao());
            campoHorarioAtendimento.setText(eventoDestinatario.getHorarioAtendimento());
            campoValores.setText(eventoDestinatario.getValores());

            String fotoEvento = eventoDestinatario.getUrlImagem();
            /* ANTIGO
            if( fotoEvento != null){
                Uri url = Uri.parse(fotoEvento);
                Glide.with(VisualizarEventoActivity.this)
                        .load(url)
                        .into(imagemEvento);
            }else{
                imagemEvento.setImageResource(R.drawable.logo4);
            }
            */
            if( fotoEvento != null){
                Uri url = Uri.parse(fotoEvento);
                Glide.with(VisualizarEventoActivity.this)
                        .load(url)
                        .transform(
                                new MultiTransformation(
                                        new CenterCrop(),
                                        new RoundedCorners(25)))
                        .into(imagemEvento);
            }else{
                imagemEvento.setImageResource(R.drawable.logo4);
            }


        }

        //Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarVisualizarEvento);
        toolbar.setTitle(eventoDestinatario.getNomeEvento());
        //toolbar.setSubtitle("Sub título");
        setSupportActionBar(toolbar);

        botaoComentarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisualizarEventoActivity.this, ComentariosActivity.class);
                intent.putExtra("idEvento",eventoDestinatario.getId());
                intent.putExtra("Activity","visualizarEventoActivity");
                intent.putExtra("EventoDestinatario",eventoDestinatario);
                startActivity(intent);
            }
        });


        botaoLocalizacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("Activity","visualizarEventoActivity");
                intent.putExtra("EventoDestinatario",eventoDestinatario);
                startActivity(intent);
            }
        });





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_evento,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_alterarEvento:
                Evento eventoAlterar =  eventoDestinatario;
                Intent i = new Intent(getApplicationContext(), AlterarEventoActivity.class);
                i.putExtra("eventoSelecionado", eventoAlterar);
                startActivity(i);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void inicializarComponentes() {

        campoDescricao = findViewById(R.id.textVEDescricao);
        campoHorarioAtendimento = findViewById(R.id.textVEHorarios);
        campoValores = findViewById(R.id.textVEValores);
        imagemEvento = findViewById(R.id.imageVEEvento);
        botaoComentarios = findViewById(R.id.buttonComentarios);
        botaoLocalizacao = findViewById(R.id.buttonLocalizacao);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,CameraActivity.class);
        startActivity(intent);
        finish();
    }
}