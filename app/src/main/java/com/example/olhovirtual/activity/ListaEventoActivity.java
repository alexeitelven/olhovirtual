package com.example.olhovirtual.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.olhovirtual.MapsActivity;
import com.example.olhovirtual.R;
import com.example.olhovirtual.adapter.AdapterListaEventos;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.helper.Permissoes;
import com.example.olhovirtual.helper.RecyclerItemClickListener;
import com.example.olhovirtual.helper.Util;
import com.example.olhovirtual.model.Evento;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    //-----------------------------------------------
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Util util = new Util();

    //private List<Evento> listaEventos;
    private List<Evento> listaEventosProximos;
    //private DatabaseReference eventosRef;


    //-----------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_eventos);
        inicializarComponentes();

        //Localiza????o do Usu??rio
        Permissoes.validarPermissoes(permissoes, this, 1);


        //Verifica se o GPS est?? Ativo, caso n??o esteja solicita a Utiliza????o
        try {
            checkGps();
        } catch (Exception e) {
            createNoGpsDialog();

        }

        inicializarComponentes();

        //Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarTurista);
        //toolbar.setTitle("Olho Virtual");
        setSupportActionBar(toolbar);

        //Autentica????o firebase
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //pesquisa eventos

        //--------------------------------------------------------------------------------------
        //Objeto que gerecia a localiza????o do usu??rio.
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            //Configura????o RecyclerView
            recyclerEventos.setHasFixedSize(true);
            recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

            //adapterListaEventos = new AdapterListaEventos(listaEventos,getApplicationContext());
            adapterListaEventos = new AdapterListaEventos(listaEventosProximos, getApplicationContext());
            recyclerEventos.setAdapter(adapterListaEventos);

            //Objeto respons??vel por receber as atualiza????es do usu??rio
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    //retorna localiza????o do usu??rio
                    Double latitudeUsr = location.getLatitude();
                    Double longitudeUsr = location.getLongitude();

                    //----------------------------------------------------
                    //QRY RESULTADO EVENTOS
                    //listaEventos = new ArrayList<>();
                    eventosRef = ConfiguracaoFirebase.getFirebase()
                            .child("eventos");

                    //limpar lista
                    listaEventos.clear();
                    listaEventosProximos.clear();

                    Query query = eventosRef.orderByChild("id");

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //limpar lista
                            listaEventos.clear();
                            listaEventosProximos.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                listaEventos.add(ds.getValue(Evento.class));
                            }
                            double distancia = 0.0;
                            for (Evento obj : listaEventos) {
                                //Log.i("Eventos", "calculando distancia" );

                                distancia = util.distEntreCoordenadas(latitudeUsr, longitudeUsr, obj.getCoordenadaX(), obj.getCoordenadaY());
                                //Log.i("Eventos", "Dist??ncia Calculada : " + distancia);
                                if (distancia < 5000) {//Raio dos eventos para aparecer na lista de eventos pr??ximos
                                    listaEventosProximos.add(obj);
                                }
                            }
                            adapterListaEventos.notifyDataSetChanged();
                            /*
                            Log.i("Eventos", "total proximo: " + listaEventosProximos.size() );
                            for (Evento evt : listaEventosProximos) {
                                Log.i("Eventos", "nome :" +evt.getNomeEvento());
                                Log.i("Eventos", "coordenada X :" +evt.getCoordenadaX());
                                Log.i("Eventos", "coordenada y :" +evt.getCoordenadaY());
                            }
                            */
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000, //Tempo das atualiza????es em milisegundos
                        1, //dist??ncia em metros para receber atualiza????es
                        locationListener
                );
            }

            //--------------------------------------------------------------------------------------
            recyclerEventos.addOnItemTouchListener(
                    new RecyclerItemClickListener(
                            getApplicationContext(),
                            recyclerEventos,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    Evento eventoSelecionado = listaEventosProximos.get(position);
                                    Intent i = new Intent(getApplicationContext(), VisualizarEventoActivity.class);
                                    i.putExtra("eventoSelecionado", eventoSelecionado);
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
                    Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                    startActivity(i);
                }
            });
            floatingLocalizacao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("Activity","listaEventoActivity");
                    startActivity(intent);
                }
            });
        }
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
            case R.id.menu_AlterarPerfil:
                startActivity( new Intent(getApplicationContext(),AlterarUsuarioActivity.class));
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
        listaEventos = new ArrayList<>();
        listaEventosProximos = new ArrayList<>();


    }


    //Valida permiss??es para acessar a localiza????o do Usu??rio e configura retornos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                //Alerta permiss??o NEGADA
                alertaValidarPermiss??o();
            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {
                //Recupera  a localiza????o do usu??rip
                /*
                    1) Provedor da Localiza????o
                    2) Tempo m??nimo entre atualiza????es de localiza????o (milesegundos)
                    3) Distancia m??nima entre atualiza????o de Localiza????es
                    4) Locarion listener (para receber as atuliza????es)
                 */
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            5000, //Tempo das atualiza????es em milisegundos
                            1, //dist??ncia em metros para receber atualiza????es
                            locationListener


                    );
                }

            }
        }

    }

    //Mensagem para confirma????o de acesso a localiza????o do usu??rio.
    private void alertaValidarPermiss??o(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permiss??es Negadas");
        builder.setMessage("Para utilizar o App ?? necess??rio aceitar as permiss??es de localiza????o");
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

    //Cria mensagem para Ativa????o do GPS
    private void createNoGpsDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent callGPSSettingIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                        finish();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor ative seu GPS para usar esse aplicativo. E depois reabra o aplicativo.")
                .setPositiveButton("Ativar", dialogClickListener)
                .create();
        builder.show();

    }
    //Checa se GPS est?? ATIVO
    public void checkGps() throws Exception{
        LocationManager manager;
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            throw new Exception("gps off");
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,CameraActivity.class);
        startActivity(intent);
        finish();
    }




}