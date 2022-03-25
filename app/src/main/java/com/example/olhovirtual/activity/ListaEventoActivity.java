package com.example.olhovirtual.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.olhovirtual.R;
import com.example.olhovirtual.adapter.AdapterListaEventos;
import com.example.olhovirtual.databinding.ActivityMainBinding;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.helper.RecyclerItemClickListener;
import com.example.olhovirtual.model.Evento;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListaEventoActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private FloatingActionButton floatingRA, floatingLocalizacao;
    private RecyclerView recyclerEventos;

    private List<Evento> listaEventos;
    private AdapterListaEventos adapterListaEventos;
    private DatabaseReference eventosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_evento);

        inicializarComponentes();

        //QRY RESULTADO EVENTOS
        listaEventos = new ArrayList<>();
        eventosRef = ConfiguracaoFirebase.getFirebase()
                .child("eventos");

        //Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarTurista);
        //toolbar.setTitle("Olho Virtual");
        setSupportActionBar( toolbar);

        //Autenticação firebase
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //Lista de eventos


        //Configuração RecyclerView
        recyclerEventos.setHasFixedSize(true);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

        adapterListaEventos = new AdapterListaEventos(listaEventos,getApplicationContext());
        recyclerEventos.setAdapter(adapterListaEventos);

        //pesquisa eventos
        pesquisarEventos();

        recyclerEventos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerEventos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent i = new Intent(getApplicationContext(),VisualizarEventoActivity.class );
                                startActivity(i);
                                //finish();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );




        floatingRA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testeCamera();
            }
        });
        floatingLocalizacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_sair:
                deslogarUsuario();
                startActivity( new Intent(getApplicationContext(),LoginActivity.class));
                finish();
                break;
            case R.id.menu_CadastrarEvento:
                startActivity( new Intent(getApplicationContext(),CadastrarEventoActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario(){
        try{
            autenticacao.signOut();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void inicializarComponentes(){

        floatingRA = findViewById(R.id.floatingRA);
        floatingLocalizacao = findViewById(R.id.floatingLocalizacao);
        recyclerEventos = findViewById(R.id.recyclerListaEventos);


    }
    public void testeCamera(){
        Intent clickPhoto = new Intent(this, CameraActivity.class);
        startActivity(clickPhoto);
    }

    private void pesquisarEventos(){

        //limpar lista
        listaEventos.clear();

        Query query = eventosRef.orderByChild("id");
                //.startAt(texto)
                //.endAt(texto + "\uf8ff" );

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //limpar lista
                listaEventos.clear();

                for( DataSnapshot ds : dataSnapshot.getChildren() ){

                    listaEventos.add( ds.getValue(Evento.class) );

                }

                adapterListaEventos.notifyDataSetChanged();

                /*
                int total = listaUsuarios.size();
                Log.i("totalUsuarios", "total: " + total );
                */
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }


}