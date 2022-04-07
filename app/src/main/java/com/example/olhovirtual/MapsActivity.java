package com.example.olhovirtual;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.example.olhovirtual.adapter.AdapterListaEventos;
import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.example.olhovirtual.helper.Util;
import com.example.olhovirtual.helper.Permissoes;
import com.example.olhovirtual.model.Evento;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
    private DatabaseReference eventosRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Validar Permissões
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

        //Objeto que gerecia a localização do usuário.
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //Objeto responsável por receber as atualizações do usuário
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Double latitudeUsr = location.getLatitude();
                Double longitudeUsr = location.getLongitude();

                //limpa mapa antes de adicionar o marcador do usuário
                mMap.clear();

                LatLng localUsuario = new LatLng(latitudeUsr, longitudeUsr);

                mMap.addMarker(new MarkerOptions().position(localUsuario).title("Meu Local"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(localUsuario));


                Log.i("LogDistancia", "Distância : " + util.distEntreCoordenadas(
                        -29.14070,-51.52270,-29.14039,-51.52271
                ) );
                



            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000, //Tempo das atualizações em milisegundos
                    1, //distÂncia em metros para receber atualizações
                    locationListener
            );
        }

    }

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
                            3000, //Tempo das atualizações em milisegundos
                            1, //distÂncia em metros para receber atualizações
                            locationListener


                    );
                }

            }
        }

    }

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


    private void pesquisarEventos(double latitudeUsr, double longitudeUsr){

        double latitudeUsuario = latitudeUsr;
        double longitudeUsuario = longitudeUsr;


        //QRY RESULTADO EVENTOS
        listaEventos = new ArrayList<>();
        eventosRef = ConfiguracaoFirebase.getFirebase()
                .child("eventos");

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