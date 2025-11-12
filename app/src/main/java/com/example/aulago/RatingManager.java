package com.example.aulago;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RatingManager {

    private static final String TAG = "RatingManager";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface RatingCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void submitRating(String avaliadoId, String avaliadorId, double nota, String comentario, RatingCallback callback) {
        // Passo 1: Primeiro, buscamos todas as avaliações existentes para o usuário.
        // Isso é feito FORA da transação.
        db.collection("avaliacoes")
                .whereEqualTo("avaliadoId", avaliadoId)
                .get()
                .addOnSuccessListener(existingRatings -> {
                    // Passo 2: Com a lista de avaliações em mãos, calculamos a nova média.
                    double novoTotalNotas = nota; // Começa com a nota da nova avaliação
                    int novoTotalAvaliacoes = 1;  // Começa com 1 (a nova avaliação)

                    for (DocumentSnapshot doc : existingRatings) {
                        if (doc.contains("nota")) {
                            novoTotalNotas += doc.getDouble("nota");
                            novoTotalAvaliacoes++;
                        }
                    }

                    double novaMedia = (novoTotalAvaliacoes > 0) ? novoTotalNotas / novoTotalAvaliacoes : 0.0;
                    double mediaArredondada = Math.round(novaMedia * 10.0) / 10.0;

                    Log.d(TAG, "Cálculo: Nova Média = " + mediaArredondada + " de " + novoTotalAvaliacoes + " avaliações.");

                    // Passo 3: Usamos um "WriteBatch" para garantir que as duas escritas aconteçam juntas.
                    // É como uma "mini-transação" apenas para operações de escrita.
                    WriteBatch batch = db.batch();

                    // 3.1. Prepara a escrita do novo documento de avaliação.
                    DocumentReference newRatingRef = db.collection("avaliacoes").document(); // <-- Corrigido para .document()
                    Map<String, Object> novaAvaliacao = new HashMap<>();
                    novaAvaliacao.put("avaliadoId", avaliadoId);
                    novaAvaliacao.put("avaliadorId", avaliadorId);
                    novaAvaliacao.put("nota", nota);
                    novaAvaliacao.put("comentario", comentario);
                    novaAvaliacao.put("timestamp", new Date());
                    batch.set(newRatingRef, novaAvaliacao);

                    // 3.2. Prepara a atualização no perfil do usuário.
                    DocumentReference userRef = db.collection("users").document(avaliadoId); // <-- Corrigido para .document()
                    batch.update(userRef, "ratingMedia", mediaArredondada);
                    batch.update(userRef, "ratingCount", novoTotalAvaliacoes);

                    // 3.3. Executa todas as operações de escrita de uma só vez.
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Batch de escrita concluído com sucesso!");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Falha no batch de escrita!", e);
                                callback.onError(e);
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Falha ao buscar avaliações existentes!", e);
                    callback.onError(e);
                });
    }
}