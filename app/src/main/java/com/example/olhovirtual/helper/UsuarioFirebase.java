package com.example.olhovirtual.helper;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.olhovirtual.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.Exclude;



public class UsuarioFirebase {

    public static FirebaseUser getUsuarioAtual(){

        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();

    }

    public static String getIdentificadorUsuario(){
        return getUsuarioAtual().getUid();
    }

    public static Usuario getDadosUsuarioLogado(){

        FirebaseUser firebaseUser = getUsuarioAtual();

        Usuario usuario = new Usuario();
        usuario.setEmail( firebaseUser.getEmail() );
        //usuario.setNome( firebaseUser.getDisplayName() );
        usuario.setId( firebaseUser.getUid() );


        return usuario;

    }


    public static void atualizarNomeUsuario(String nome){

        try {

            //Usuario logado no App
            FirebaseUser usuarioLogado = getUsuarioAtual();

            //Configurar objeto para alteração do perfil
            UserProfileChangeRequest profile = new UserProfileChangeRequest
                    .Builder()
                    .setDisplayName( nome )
                    .build();
            usuarioLogado.updateProfile( profile ).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if( !task.isSuccessful() ){
                        Log.d("Perfil","Erro ao atualizar nome de perfil." );
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}