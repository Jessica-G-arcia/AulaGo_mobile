package com.example.aulago;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Aluno implements Parcelable {

    // 1. Variável para o ID do aluno
    private String id;

    // Variáveis do seu documento no Firestore (Mantenha o mesmo nome)
    private String nome;
    private String email;
    private String urlFotoPerfil;
    private String bio;
    private String userType;
    private String dataNascimento;
    private double ratingMedia;
    private long ratingCount;
    private String objetivos;
    private String preferenciaModalidade;
    private String idioma;
    private String nivel;
    @Exclude
    private float distanciaCalculada;
    private GeoPoint localizacao;

    @Exclude
    private boolean expanded = false;

    // 2. Construtor vazio (OBRIGATÓRIO para o Firebase)
    public Aluno() {
    }

    // 3. Getters (Usados pelo Adapter e Fragment)
    public String getId() {
        return this.id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }

    public String getBio() {
        return bio;
    }

    public String getUserType() {
        return userType;
    }


    /**
     * Calcula a idade com base na dataNascimento.
     * Retorna null se a data for inválida ou não existir.
     * A anotação @Exclude impede o Firestore de tentar salvar este método.
     */
    @Exclude
    public Integer getIdade() {
        if (dataNascimento == null || dataNascimento.isEmpty()) {
            return null; // Não há data de nascimento
        }

        try {
            // 1. Define o formato da data
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // 2. Converte a String (agora dtNasc é String)
            java.util.Date dataNasc = sdf.parse(dataNascimento); // <--- ISTO VAI FUNCIONAR

            // 3. Cria um calendário para a data de nascimento
            Calendar calNasc = Calendar.getInstance();
            calNasc.setTime(dataNasc);

            // 4. Cria um calendário para HOJE
            Calendar hoje = Calendar.getInstance();

            // 5. Calcula a diferença de anos
            int idade = hoje.get(Calendar.YEAR) - calNasc.get(Calendar.YEAR);

            // 6. Verifica se o aniversário deste ano já passou
            if (hoje.get(Calendar.DAY_OF_YEAR) < calNasc.get(Calendar.DAY_OF_YEAR)) {
                idade--; // Se não passou, diminui 1 ano
            }

            return idade;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // 4. Setter (USADO PARA DEFINIR O ID MANUALMENTE)
    // Este é o método que você chama no SearchFragment
    public void setId(String id) {
        this.id = id;
    }


    // --- Lógica do Parcelable (Usada para enviar o objeto entre telas) ---

    protected Aluno(Parcel in) {
        id = in.readString();
        nome = in.readString();
        email = in.readString();
        urlFotoPerfil = in.readString();
        bio = in.readString();
        userType = in.readString();
        dataNascimento = in.readString();
        ratingCount = in.readLong();
        ratingMedia = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nome);
        dest.writeString(email);
        dest.writeString(urlFotoPerfil);
        dest.writeString(bio);
        dest.writeString(userType);
        dest.writeString(dataNascimento);
        dest.writeLong(ratingCount);
        dest.writeDouble(ratingMedia);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Aluno> CREATOR = new Creator<Aluno>() {
        @Override
        public Aluno createFromParcel(Parcel in) {
            return new Aluno(in);
        }

        @Override
        public Aluno[] newArray(int size) {
            return new Aluno[size];
        }
    };

    public double getRatingMedia() {
        return ratingMedia;
    }

    public void setRatingMedia(double ratingMedia) {
        this.ratingMedia = ratingMedia;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getObjetivos() {
        return objetivos;
    }

    public void setObjetivos(String objetivos) {
        this.objetivos = objetivos;
    }


    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public float getDistanciaCalculada() {
        return distanciaCalculada;
    }

    @Exclude
    public void setDistanciaCalculada(float distanciaCalculada) {
        this.distanciaCalculada = distanciaCalculada;
    }

    @Exclude
    public GeoPoint getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(GeoPoint localizacao) {
        this.localizacao = localizacao;
    }

    public String getPreferenciaModalidade() {
        return preferenciaModalidade;
    }

    public void setPreferenciaModalidade(String preferenciaModalidade) {
        this.preferenciaModalidade = preferenciaModalidade;
    }

    @Exclude
    public boolean isExpanded() {
        return expanded;
    }

    @Exclude
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}