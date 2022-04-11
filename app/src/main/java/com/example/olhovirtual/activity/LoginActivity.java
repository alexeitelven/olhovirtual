package com.example.olhovirtual.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.olhovirtual.R;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.helper.Permissoes;
import com.example.olhovirtual.helper.Util;
import com.example.olhovirtual.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private Button botaoEntrar;
    private ProgressBar progressBar;

    private Usuario usuario;

    private FirebaseAuth autenticacao;

    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Util util = new Util();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Validar Permissões
        //Localização do Usuário
        Permissoes.validarPermissoes(permissoes, this, 1);

        //Verifica se o GPS está Ativo, caso não esteja solicita a Utilização
        try {
            checkGps();
        }
        catch (Exception e) {
            createNoGpsDialog(); }
        //-----------------------------------

        verificarUsuarioLogado();

        inicializarComponentes();

        progressBar.setVisibility(View.GONE);
        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if(!textoEmail.isEmpty()){
                    if(!textoSenha.isEmpty()){
                        usuario = new Usuario();
                        usuario.setEmail(textoEmail);
                        usuario.setSenha(textoSenha);
                        validarLogin(usuario);

                    }else{
                        Toast.makeText(LoginActivity.this,"Campo senha vazio!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this,"Campo e-mail vazio!",Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    public void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if(autenticacao.getCurrentUser() != null){
            startActivity( new Intent(getApplicationContext(),ListaEventoActivity.class));
            finish();
        }
    }

    public void validarLogin(Usuario usuario){

        progressBar.setVisibility(View.VISIBLE);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    startActivity( new Intent(getApplicationContext(),ListaEventoActivity.class));
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this,"Login inválido, verifique sua senha ou realize seu cadastro!",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });



    }

    public void abrirCadastro(View View){
        Intent i = new Intent(LoginActivity.this, CadastrarUsuarioActivity.class);
        startActivity(i);
    }

    public void inicializarComponentes(){

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
        botaoEntrar = findViewById(R.id.buttonEntrar);
        progressBar = findViewById(R.id.progressLogin);

        campoEmail.requestFocus();

    }

    //Valida permissões para acessar a localização do Usuário e configura retornos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                //Alerta permissão NEGADA
                alertaValidarPermissão();
            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {
                //Recupera  a localização do usuárip
                /*
                    1) Provedor da Localização
                    2) Tempo mínimo entre atualizações de localização (milesegundos)
                    3) Distancia mínima entre atualização de Localizações
                    4) Locarion listener (para receber as atulizações)
                 */
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            5000, //Tempo das atualizações em milisegundos
                            1, //distÂncia em metros para receber atualizações
                            locationListener


                    );
                }

            }
        }

    }

    //Mensagem para confirmação de acesso a localização do usuário.
    private void alertaValidarPermissão(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o App é necessário aceitar as permissões de localização");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Cria mensagem para Ativação do GPS
    private void createNoGpsDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent callGPSSettingIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor ative seu GPS para usar esse aplicativo.")
                .setPositiveButton("Ativar", dialogClickListener)
                .create();
        builder.show();

    }
    //Checa se GPS está ATIVO
    public void checkGps() throws Exception{
        LocationManager manager;
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            throw new Exception("gps off");
        }
    }


}

