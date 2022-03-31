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
import android.widget.ProgressBar;
import android.widget.Toast;

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

public class CadastrarEventoActivity extends AppCompatActivity {

    private EditText campoNomeEvento, campoDescricao, campoHorarioAtendimento, campoValores,campoRaio,campoCoordenadaY,campoCoordenadaX;
    private double CoordenadaX= 0.0, CoordenadaY= 0.0,Raio=0.0;
    private Button botaoCadastrar,botaoAdicionarFoto;
    private ProgressBar progressBar;

    //Para autenticar no banco
    private FirebaseAuth autenticacao;
    private UsuarioFirebase usuarioFirebase;

    private Evento evento;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageRef;
    private String identificadorEvento;
    private byte[] dadosImagem;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_evento);

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        inicializarComponentes();


        progressBar.setVisibility(View.GONE);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
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
                                    evento.setUrlImagem(url);
                                    //Envia para função do cadastro
                                    cadastrar(evento);


                                }else{
                                    Toast.makeText(CadastrarEventoActivity.this,"Campo Raio em branco!",Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(CadastrarEventoActivity.this,"Campo Coordenada Y em branco!",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(CadastrarEventoActivity.this,"Campo Coordenada X em branco!",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(CadastrarEventoActivity.this,"Campo descrição do evento em branco!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CadastrarEventoActivity.this,"Campo nome do evento em branco!",Toast.LENGTH_SHORT).show();
                }


            }
        });

        botaoAdicionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                botaoCadastrar.setVisibility(View.GONE);
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

                /* Mostra a imagem no cadastro
                //Ajustar layout
                if (imagem !=null){
                    //Adiicona imagem na tela
                    imageEvento.setImageBitmap(imagem);
                }
                */


                //
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
                        Toast.makeText(CadastrarEventoActivity.this,
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
                        Toast.makeText(CadastrarEventoActivity.this,
                                "Upload da imagem realizado com sucesso!",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        botaoCadastrar.setVisibility(View.VISIBLE);
                    }
                });


            }catch(Exception e){

            }
        }

    }



    public void cadastrar(Evento evento){
        progressBar.setVisibility(View.VISIBLE);
        //autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        evento.salvar();

        progressBar.setVisibility(View.GONE);
        Toast.makeText(CadastrarEventoActivity.this,
                "Cadastrado com sucesso!",
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();

    }


    public void inicializarComponentes() {

        campoNomeEvento = findViewById(R.id.editCadastroNomeEvento);
        campoDescricao = findViewById(R.id.editDescricaoEvento);
        campoHorarioAtendimento = findViewById(R.id.editHorarioAtendimento);
        campoValores = findViewById(R.id.editValores);
        campoCoordenadaX= findViewById(R.id.editCadastroEventoX);
        campoCoordenadaY= findViewById(R.id.editCadastroEventoY);
        campoRaio= findViewById(R.id.editCadastroEventoRaio);
        botaoCadastrar = findViewById(R.id.buttonCadastrarEvento);
        botaoAdicionarFoto = findViewById(R.id.buttonAdicionarFotos);
        progressBar= findViewById(R.id.progressBarCadastroEvento);

        campoNomeEvento.requestFocus();

    }

}

