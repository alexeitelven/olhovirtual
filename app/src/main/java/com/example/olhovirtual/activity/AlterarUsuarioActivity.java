package com.example.olhovirtual.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olhovirtual.R;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.helper.UsuarioFirebase;
import com.example.olhovirtual.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AlterarUsuarioActivity extends AppCompatActivity {

    private EditText campoNome,  campoSenha, campoSenhaAntiga, campoConfirmarSenha;
    private TextView campoEmail;
    private Button botaoAlterarNome, botaoAlterarSenha;
    private ProgressBar progressBar;

    private Usuario usuario;
    private FirebaseAuth autenticacao;

    private DatabaseReference usuarioRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_usuario);

        inicializarComponentes();

        //Inicializa o usuário
        usuario = new Usuario();
        //Busca informações do usuário no autentication
        usuario = UsuarioFirebase.getDadosUsuarioLogado();

        //Pesquisa Dados do usuário no banco de dados
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
                usuario.setNome(temp.getNome());
                //Preenche dados da Tela
                campoNome.setText(temp.getNome());
                campoEmail.setText(temp.getEmail());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        progressBar.setVisibility(View.GONE);

        botaoAlterarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();


                //Verifica Cadastro em branco.
                if(!textoNome.isEmpty()){
                    if(!textoEmail.isEmpty()){
                        //usuario = new Usuario();
                        usuario.setId(usuario.getId());
                        usuario.setNome(textoNome);
                        //Altera o nome do usuário
                        alterarNome(usuario);
                    }else{
                        Toast.makeText(AlterarUsuarioActivity.this,"Campo E-mail em branco!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AlterarUsuarioActivity.this,"Campo nome em branco!",Toast.LENGTH_SHORT).show();
                }

            }
        });

        botaoAlterarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoSenha = campoSenha.getText().toString();
                String textoSenhaAntiga = campoSenhaAntiga.getText().toString();
                String textoConfirmarSenha = campoConfirmarSenha.getText().toString();


                if(!textoSenha.isEmpty()){
                    if(!textoConfirmarSenha.isEmpty()){
                        if(textoSenha.equals(textoConfirmarSenha)){
                            if(textoSenha.length() >= 6){
                                //usuario = new Usuario();
                                usuario.setSenha(textoSenha);
                                alterarSenha(usuario);
                            }else{
                                Toast.makeText(AlterarUsuarioActivity.this,"A senha deve conter no mínimo 6 caracteres",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(AlterarUsuarioActivity.this,"Os Campos senhas não estão iguais!",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(AlterarUsuarioActivity.this,"Campo confirmar senha em branco!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AlterarUsuarioActivity.this,"Campo senha em branco!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public void alterarNome(Usuario usuario){
        progressBar.setVisibility(View.VISIBLE);
        usuario.alterar();
        progressBar.setVisibility(View.GONE);
        Toast.makeText(AlterarUsuarioActivity.this,
                "Alterado com sucesso!",
                Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(),ListaEventoActivity.class));
        finish();

    };

    //Cadastra usuário e senha no Firebase e valida
    public void alterarSenha(Usuario usuario){
        progressBar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), campoSenhaAntiga.toString());

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("Usuario","Reautenticado");
                        Log.i("Usuario","E-mail: " + user.getEmail());
                        String newPassword = usuario.getSenha();
                        Log.i("Usuario",newPassword);


                        user.updatePassword(newPassword)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            try{
                                                progressBar.setVisibility(View.GONE);

                                                //String idUsuario = task.getResult().getUser().getUid();
                                                //usuario.setId(idUsuario);
                                                //usuario.alterar();
                                                Toast.makeText(AlterarUsuarioActivity.this,
                                                        "Alterado com sucesso!",
                                                        Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                                finish();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                        }else{
                                            progressBar.setVisibility(View.GONE);
                                            String erroExcecao = "";
                                            try{
                                                throw task.getException();
                                            }catch (FirebaseAuthWeakPasswordException e) {
                                                erroExcecao = "Digite uma senha mais forte!";
                                            //}catch (Fire){
                                            } catch (Exception e) {
                                                erroExcecao = "ao cadastrar usuário: " + e.getMessage();
                                                e.printStackTrace();
                                            }
                                            Toast.makeText(AlterarUsuarioActivity.this,
                                                    "Erro: "+ erroExcecao,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
    }



    public void inicializarComponentes(){

        campoNome = findViewById(R.id.editAltNomeUsuario);
        campoEmail = findViewById(R.id.textAltEmailCadastro);
        campoSenha = findViewById(R.id.editAltSenha);
        campoSenhaAntiga = findViewById(R.id.editAltSenhaAntiga);
        campoConfirmarSenha = findViewById(R.id.editAltConfirmarSenha);
        botaoAlterarNome = findViewById(R.id.buttonAltNomeUsuario);
        botaoAlterarSenha = findViewById(R.id.buttonAltSenhaUsuario);
        progressBar = findViewById(R.id.progressAltUsuario);


    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,ListaEventoActivity.class);
        startActivity(intent);
        finish();
    }


}