package com.example.olhovirtual.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.olhovirtual.R;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.helper.UsuarioFirebase;
import com.example.olhovirtual.model.Evento;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class AlterarEventoActivity extends AppCompatActivity {

    private EditText campoNomeEvento, campoDescricao, campoHorarioAtendimento, campoValores,campoRaio,campoCoordenadaY,campoCoordenadaX;
    private double CoordenadaX= 0.0, CoordenadaY= 0.0,Raio=0.0;
    private Button botaoAlterarCadastro,botaoAlterarFoto;
    private ProgressBar progressBar;

    //Para autenticar no banco
    private FirebaseAuth autenticacao;
    private UsuarioFirebase usuarioFirebase;

    private Evento evento;
    private ImageView imagemEvento;
    private Evento eventoDestinatario;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageRef;
    private String identificadorEvento;
    private byte[] dadosImagem;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_evento);

        //Configurações iniciais

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        inicializarComponentes();

        //Recuperar dados do Evento selecionado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            eventoDestinatario =(Evento) bundle.getSerializable("eventoSelecionado");
            campoNomeEvento.setText(eventoDestinatario.getNomeEvento());
            campoDescricao.setText(eventoDestinatario.getDescricao());
            campoHorarioAtendimento.setText(eventoDestinatario.getHorarioAtendimento());
            campoValores.setText(eventoDestinatario.getValores());
            campoCoordenadaX.setText(Double.toString(eventoDestinatario.getCoordenadaX()));
            campoCoordenadaY.setText(Double.toString(eventoDestinatario.getCoordenadaY()));
            campoRaio.setText(Double.toString(eventoDestinatario.getRaio()));

            String fotoEvento = eventoDestinatario.getUrlImagem();
            if( fotoEvento != null){
                Uri url = Uri.parse(fotoEvento);
                Glide.with(AlterarEventoActivity.this)
                        .load(url)
                        .into(imagemEvento);
            }else{
                imagemEvento.setImageResource(R.drawable.logo4);
            }

        }


        progressBar.setVisibility(View.GONE);

        botaoAlterarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoNomeEvento = campoNomeEvento.getText().toString();
                String textoDescricao = campoDescricao.getText().toString();
                String textoHorarioAtendimento = campoHorarioAtendimento.getText().toString();
                String textoValores = campoValores.getText().toString();
                String textoCoordenadaX= campoCoordenadaX.getText().toString();
                String textoCoordenadaY= campoCoordenadaY.getText().toString();
                String textoRaio=  campoRaio.getText().toString();
                //String userAdm = autenticacao.getCurrentUser().getUid();
                String userAdm = usuarioFirebase.getUsuarioAtual().getUid();


                //Valida os campos
                if(!textoNomeEvento.isEmpty()){
                    //Valida os campos
                    if(!textoDescricao.isEmpty()){
                        //Valida os campos
                        if(!textoCoordenadaX.isEmpty()){
                            if(!textoCoordenadaY.isEmpty()){
                                if(!textoRaio.isEmpty()){

                                    evento = new Evento();
                                    evento.setNomeEvento(textoNomeEvento);
                                    evento.setDescricao(textoDescricao);
                                    evento.setHorarioAtendimento(textoHorarioAtendimento);
                                    evento.setValores(textoValores);
                                    evento.setCoordenadaX(Double.valueOf(textoCoordenadaX).doubleValue());
                                    evento.setCoordenadaY(Double.valueOf(textoCoordenadaY).doubleValue());
                                    evento.setRaio(Double.valueOf(textoRaio).doubleValue());
                                    evento.setIdAdm(userAdm);
                                    evento.setId(eventoDestinatario.getId());
                                    evento.setUrlImagem(eventoDestinatario.getUrlImagem());
                                    //Envia para função do cadastro
                                    alterarEvento(evento);


                                }else{
                                    Toast.makeText(AlterarEventoActivity.this,"Campo Raio em branco!",Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(AlterarEventoActivity.this,"Campo Coordenada Y em branco!",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(AlterarEventoActivity.this,"Campo Coordenada X em branco!",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(AlterarEventoActivity.this,"Campo descrição do evento em branco!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AlterarEventoActivity.this,"Campo nome do evento em branco!",Toast.LENGTH_SHORT).show();
                }


            }
        });

        botaoAlterarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                botaoAlterarFoto.setVisibility(View.GONE);
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if( i.resolveActivity(getPackageManager()) != null ){
                    startActivityForResult(i, SELECAO_GALERIA);

                }
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Variaveis para criar Chave da imagem
        String textoNomeEvento = campoNomeEvento.getText().toString();
        String userAdm = usuarioFirebase.getUsuarioAtual().getUid();

        evento = new Evento();
        evento.setNomeEvento(textoNomeEvento);
        evento.setIdAdm(userAdm);


        if( resultCode == RESULT_OK){
            Bitmap imagem = null;

            try{
                //Selecao apenas da galeria
                switch ( requestCode){
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);
                        break;
                }

                //Recuperar dados da imagem para o firebase
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagem.compress(Bitmap.CompressFormat.JPEG, 80,baos);
                dadosImagem = baos.toByteArray();

                //Chave: nomeEvento-useradm
                //Salvar imagem no firebase
                final StorageReference imageRef = storageRef
                        .child("imagens")
                        .child("eventos")
                        .child(evento.getNomeEvento()+"-"+evento.getIdAdm()+".jpeg");
                UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AlterarEventoActivity.this,
                                "Erro ao fazer upload da imagem!",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                url = task.getResult().toString();

                            }
                        });
                        Toast.makeText(AlterarEventoActivity.this,
                                "Upload da imagem realizado com sucesso!",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        botaoAlterarCadastro.setVisibility(View.VISIBLE);
                    }
                });


            }catch(Exception e){

            }
        }

    }



    public void alterarEvento(Evento evento){
        progressBar.setVisibility(View.VISIBLE);
        //autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        evento.alterar();

        progressBar.setVisibility(View.GONE);
        Toast.makeText(AlterarEventoActivity.this,
                "Alterado com sucesso!",
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();

    }


    public void inicializarComponentes() {

        campoNomeEvento = findViewById(R.id.editAltNomeEvento);
        campoDescricao = findViewById(R.id.editAltDescricaoEvento);
        campoHorarioAtendimento = findViewById(R.id.editAltHorarioAtendimento);
        campoValores = findViewById(R.id.editAltValores);
        campoCoordenadaX= findViewById(R.id.editAltEventoX);
        campoCoordenadaY= findViewById(R.id.editAltEventoY);
        campoRaio= findViewById(R.id.editAltEventoRaio);
        botaoAlterarCadastro = findViewById(R.id.buttonAltEvento);
        botaoAlterarFoto = findViewById(R.id.buttonAltFotoEvento);
        progressBar= findViewById(R.id.progressBarAltEvento);
        imagemEvento = findViewById(R.id.imageAltEvento);



    }

}