package com.example.olhovirtual.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.olhovirtual.R;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastrarUsuarioActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha, campoConfirmarSenha;
    private Button botaoCadastrar;
    private ProgressBar progressBar;

    private Usuario usuario;

    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);

        inicializarComponentes();

        progressBar.setVisibility(View.GONE);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();
                String textoConfirmarSenha = campoConfirmarSenha.getText().toString();


                //Verifica Cadastro em branco.
                if(!textoNome.isEmpty()){
                    if(!textoEmail.isEmpty()){
                        if(!textoSenha.isEmpty()){
                            if(!textoConfirmarSenha.isEmpty()){
                                if(textoSenha.equals(textoConfirmarSenha)){
                                    usuario = new Usuario();
                                    usuario.setNome(textoNome);
                                    usuario.setEmail(textoEmail);
                                    usuario.setSenha(textoSenha);
                                    cadastrar(usuario);

                                }else{
                                    Toast.makeText(CadastrarUsuarioActivity.this,"Os Campos senhas não estão iguais!",Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(CadastrarUsuarioActivity.this,"Campo confirmar senha em branco!",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(CadastrarUsuarioActivity.this,"Campo senha em branco!",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(CadastrarUsuarioActivity.this,"Campo E-mail em branco!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CadastrarUsuarioActivity.this,"Campo nome em branco!",Toast.LENGTH_SHORT).show();
                }


            }
        });

    }


    //Cadastra usuário e senha no Firebase e valida
    public void cadastrar(Usuario usuario){
        progressBar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()

        ).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            try{
                                progressBar.setVisibility(View.GONE);

                                //Salvar usuário no firebase
                                String idUsuario = task.getResult().getUser().getUid();
                                usuario.setId(idUsuario);
                                usuario.salvar();
                                Toast.makeText(CadastrarUsuarioActivity.this,
                                        "Cadastrado com sucesso!",
                                        Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),ListaEventoActivity.class));
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
                            }catch (FirebaseAuthInvalidCredentialsException e) {
                                erroExcecao = "Por favor digite um e-mail válido";
                            }catch (FirebaseAuthUserCollisionException e){
                                erroExcecao = "Esta conta já está cadastrada";
                            } catch (Exception e) {
                                erroExcecao = "ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastrarUsuarioActivity.this,
                                    "Erro: "+ erroExcecao,
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                }
        );

    }


    public void inicializarComponentes(){

        campoNome = findViewById(R.id.editCadastroNomeUsuario);
        campoEmail = findViewById(R.id.editEmailCadastro);
        campoSenha = findViewById(R.id.editCadastroSenha);
        campoConfirmarSenha = findViewById(R.id.editConfirmarSenha);
        botaoCadastrar = findViewById(R.id.buttonCadastrarUsuario);
        progressBar = findViewById(R.id.progressCadastroUsuario);

        campoNome.requestFocus();

    }

}