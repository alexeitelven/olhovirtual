package com.example.olhovirtual.model;

import android.net.Uri;

import com.example.olhovirtual.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.net.URI;

public class Evento implements Serializable {

    private String id;
    private String nomeEvento;
    private String descricao;
    private String horarioAtendimento;
    private String valores;
    private String idAdm;
    private String urlImagem;
    private double coordenadaX,coordenadaY,raio;


    public Evento() {
    }

    public void salvar(){
        /*
        + eventos
            +id_evento
                dados_eventos
         */

        /* OLD
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebase();
        firebase.child("eventos")
                .push()
                .setValue(this);
        */


        DatabaseReference eventoRef = ConfiguracaoFirebase.getFirebase()
                .child("eventos");

        //Retorna a chave do comentarios atual recem criado
        String chaveEvento = eventoRef.push().getKey();
        setId(chaveEvento);
        eventoRef.child(getId()).setValue(this);


    }


    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getIdAdm() {
        return idAdm;
    }

    public void setIdAdm(String idAdm) {
        this.idAdm = idAdm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeEvento() {
        return nomeEvento;
    }

    public void setNomeEvento(String nomeEvento) {
        this.nomeEvento = nomeEvento;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getHorarioAtendimento() {
        return horarioAtendimento;
    }

    public void setHorarioAtendimento(String horarioAtendimento) {
        this.horarioAtendimento = horarioAtendimento;
    }

    public String getValores() {
        return valores;
    }

    public void setValores(String valores) {
        this.valores = valores;
    }

    public double getCoordenadaX() {
        return coordenadaX;
    }

    public void setCoordenadaX(double coordenadaX) {
        this.coordenadaX = coordenadaX;
    }

    public double getCoordenadaY() {
        return coordenadaY;
    }

    public void setCoordenadaY(double coordenadaY) {
        this.coordenadaY = coordenadaY;
    }

    public double getRaio() {
        return raio;
    }

    public void setRaio(double raio) {
        this.raio = raio;
    }
}
