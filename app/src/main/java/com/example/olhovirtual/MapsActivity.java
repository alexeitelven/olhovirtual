package com.example.olhovirtual;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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
import android.util.Log;

import com.example.olhovirtual.activity.CameraActivity;
import com.example.olhovirtual.activity.ListaEventoActivity;
import com.example.olhovirtual.activity.VisualizarEventoActivity;
import com.example.olhovirtual.adapter.AdapterListaEventos;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.helper.Util;
import com.example.olhovirtual.helper.Permissoes;
import com.example.olhovirtual.model.Evento;
import com.example.olhovirtual.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.olhovirtual.databinding.ActivityMapsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Util util = new Util();

    private List<Evento> listaEventos;
    private List<Evento> listaEventosProximos;
    private DatabaseReference eventosRef;

    private Evento eventoDestinatario = new Evento();

    private String activityAnterior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        listaEventos = new ArrayList<>();
        listaEventosProximos = new ArrayList<>();


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Validar Permiss??es
        Permissoes.validarPermissoes(permissoes, this, 1);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Objeto que gerecia a localiza????o do usu??rio.
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Objeto respons??vel por receber as atualiza????es do usu??rio
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //retorna localiza????o do usu??rio
                Double latitudeUsr = location.getLatitude();
                Double longitudeUsr = location.getLongitude();

                //limpa mapa antes de adicionar o marcador do usu??rio
                mMap.clear();

                LatLng localUsuario = new LatLng(latitudeUsr, longitudeUsr);

                mMap.addMarker(new MarkerOptions().position(localUsuario).title("Estou Aqui!!!").icon(
                        //BitmapDescriptorFactory.fromResource(R.drawable.icons_minha_localizacao_red_48)));
                        BitmapDescriptorFactory.fromResource(R.drawable.icons_minha_localizacao_red_48)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario,18));

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
                        for( DataSnapshot ds : dataSnapshot.getChildren() ){
                            listaEventos.add( ds.getValue(Evento.class) );
                        }

                        int total = listaEventos.size();
                        //Log.i("Eventos", "total no ondataSnapshot: " + total );

                        double distancia = 0.0;

                        for (Evento obj : listaEventos) {
                            //Log.i("Eventos", "calculando distancia" );

                            distancia = util.distEntreCoordenadas(latitudeUsr,longitudeUsr,obj.getCoordenadaX(), obj.getCoordenadaY());
                            //Log.i("Eventos", "Dist??ncia Calculada : " + distancia);
                            if(distancia < 5000 ){ //Raio dos eventos para aparecer na lista de eventos pr??ximos
                                listaEventosProximos.add(obj);
                            }

                        }

                        Log.i("Eventos", "total proximo: " + listaEventosProximos.size() );
                        for (Evento evt : listaEventosProximos) {
                            //Log.i("Eventos", "nome :" +evt.getNomeEvento());
                            //Log.i("Eventos", "coordenada X :" +evt.getCoordenadaX());
                            //Log.i("Eventos", "coordenada y :" +evt.getCoordenadaY());
                            //Retorna Coordenada do evento
                            double coordenadaX = evt.getCoordenadaX();
                            double coordenadaY = evt.getCoordenadaY();

                            LatLng localEvento = new LatLng(coordenadaX,coordenadaY);

                            //Adiciona Marcador com cor aleat??ria
                            Random numero = new Random();
                            switch (numero.nextInt(8)) {
                                case 0:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                                    break;
                                case 1:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    break;
                                case 2:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                    break;
                                case 3:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    break;
                                case 4:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                                    break;
                                case 5:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                    break;
                                case 6:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                                    break;
                                case 7:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                                    break;
                                case 8:
                                    mMap.addMarker(new MarkerOptions()
                                            .position(localEvento)
                                            .title(evt.getNomeEvento())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                    break;
                            }
                        }
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

    }

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


    @Override
    public void onBackPressed() {

        //Recuperar Local Onde foi pressionado o bot??o
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            activityAnterior = bundle.getString("Activity");
            eventoDestinatario = (Evento) bundle.getSerializable("EventoDestinatario");
        }

        if (activityAnterior.equals("cameraActivity")) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
            finish();
        }else if (activityAnterior.equals("listaEventoActivity")) {
            Intent intent = new Intent(this, ListaEventoActivity.class);
            startActivity(intent);
            finish();
        }else if (activityAnterior.equals("visualizarEventoActivity")) {
            Intent intent = new Intent(this, VisualizarEventoActivity.class);
            intent.putExtra("eventoSelecionado",eventoDestinatario);
            startActivity(intent);
            finish();
        }


    }


}