package com.example.olhovirtual.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.olhovirtual.R;
import com.example.olhovirtual.adapter.AdapterComentario;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.helper.UsuarioFirebase;
import com.example.olhovirtual.model.Comentario;
import com.example.olhovirtual.model.Evento;
import com.example.olhovirtual.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ComentariosActivity extends AppCompatActivity {

    private EditText editComentario;
    private FloatingActionButton  botaoEnviar;
    private RecyclerView reclyclerComentarios;
    private AdapterComentario adapterComentario;
    private List<Comentario> listaComentarios = new ArrayList<>();
    private List<Usuario> usuarioQry = new ArrayList<>();

    private String idEvento;
    private Usuario usuario;

    private DatabaseReference firebaseRef;
    private DatabaseReference comentariosRef;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerComentarios;
    private String activityAnterior;
    private Evento eventoDestinatario = new Evento();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        inicializarComponentes();

        //Configurações iniciais
        usuario = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase();

        //Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarComentarios);
        //toolbar.setTitle("Olho Virtual");
        setSupportActionBar( toolbar);

        //Configuração Recyclerview
        adapterComentario = new AdapterComentario(listaComentarios,getApplicationContext());
        reclyclerComentarios.setHasFixedSize(true);
        reclyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        reclyclerComentarios.setAdapter(adapterComentario);


        //Recuperar ID do evento
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            idEvento = bundle.getString("idEvento");
            eventoDestinatario = (Evento) bundle.getSerializable("EventoDestinatario");
        }


        //Adiciona o nome do usuário logado ao objeto usuário
        pesquisaUsuario();

        //Foca no Edit para carregar comentários automaticamente
        //editComentario.requestFocus();

        botaoEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarComentario();
            }
        });


    }

    private void recuperarComentarios(){
        comentariosRef = firebaseRef.child("comentarios")
                .child(idEvento);

        valueEventListenerComentarios = comentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaComentarios.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    listaComentarios.add(ds.getValue(Comentario.class));
                }
                adapterComentario.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    @Override
    public void onBackPressed() {

        //Recuperar Local Onde foi pressionado o botão
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            activityAnterior = bundle.getString("Activity");
        }

        if (activityAnterior.equals("cameraActivity")) {
            Intent intent = new Intent(this,CameraActivity.class);
            startActivity(intent);
            finish();
        }else if (activityAnterior.equals("visualizarEventoActivity")) {
            Intent intent = new Intent(this, VisualizarEventoActivity.class);
            intent.putExtra("eventoSelecionado",eventoDestinatario);
            startActivity(intent);
            finish();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarComentarios();
    }

    @Override
    protected void onStop() {
        super.onStop();
        comentariosRef.removeEventListener(valueEventListenerComentarios);
    }

    public void salvarComentario(){
        String textoComentario = editComentario.getText().toString();

        if (textoComentario != null && !textoComentario.equals("")){
            Comentario comentario = new Comentario();

            comentario.setIdEvento(idEvento);
            comentario.setIdUsuario(usuario.getId());

            //Buscar nome do usuário
            pesquisaUsuario();

            //Toast.makeText(this,usuario.getNome(),Toast.LENGTH_SHORT).show();
            comentario.setNomeUsuario(usuario.getNome());
            //comentario.setNomeUsuario("Alex");
            comentario.setComentario(textoComentario);
            if(comentario.salvar()){
                Toast.makeText(this,"Comentario salvo com sucesso!!",Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(this,"Insira um comentário antes de enviar!",Toast.LENGTH_SHORT).show();
        }

        //Limpa texto do comentário
        editComentario.setText("");

    }

    public void inicializarComponentes(){
        editComentario = findViewById(R.id.editComentario);
        botaoEnviar = findViewById(R.id.floatingEnviarComentario);
        reclyclerComentarios = findViewById(R.id.recyclerComentarios);

    }

    private void pesquisaUsuario(){

        usuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("usuarios");

        Query query = usuarioRef.orderByKey().equalTo(usuario.getId());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            Usuario temp = new Usuario();
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()){
                    temp = ds.getValue(Usuario.class);
                    //Log.i("Usuario",ds.getValue().toString());
                }
                //Log.i("Usuario",temp.getNome());
                usuario.setNome(temp.getNome());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }



}