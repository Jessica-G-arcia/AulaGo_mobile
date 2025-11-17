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

    /**
     * MÉTODO submitRating ATUALIZADO
     * Agora recebe o ReviewModel completo e calcula o MELHOR comentário.
     */
    public void submitRating(ReviewModel review, String avaliadoId, RatingCallback callback) {

        double notaNovaAvaliacao = review.getRating();
        String autorDoNovoComentario = "aluno".equals(review.getEscritoPor()) ? review.getAlunoNome() : review.getProfessorNome();

        // Define qual campo usar para a busca (se o avaliado é um professor, buscamos pelo 'professorId', etc.)
        String idFieldQuery = "aluno".equals(review.getEscritoPor()) ? "professorId" : "alunoId";

        db.collection("avaliacoes")
                .whereEqualTo(idFieldQuery, avaliadoId) // Busca todas as avaliações DO usuário que está sendo AVALIADO
                .get()
                .addOnSuccessListener(existingRatings -> {
                    double novoTotalNotas = notaNovaAvaliacao;
                    int novoTotalAvaliacoes = 1;

                    // Começamos assumindo que a NOVA avaliação é a melhor.
                    double melhorNota = notaNovaAvaliacao;
                    String melhorComentario = review.getComentario();
                    String autorMelhorComentario = autorDoNovoComentario;

                    // Itera sobre as avaliações JÁ EXISTENTES no banco
                    for (DocumentSnapshot doc : existingRatings) {
                        if (doc.contains("rating")) {
                            double notaExistente = doc.getDouble("rating");
                            novoTotalNotas += notaExistente;
                            novoTotalAvaliacoes++;

                            // Compara a nota existente com a melhor nota encontrada até agora
                            if (notaExistente >= melhorNota) {
                                melhorNota = notaExistente;
                                melhorComentario = doc.getString("comentario");

                                // Determina o autor do comentário com base na avaliação existente
                                String escritoPor = doc.getString("escritoPor");
                                if ("aluno".equals(escritoPor)) {
                                    autorMelhorComentario = doc.getString("alunoNome");
                                } else {
                                    autorMelhorComentario = doc.getString("professorNome");
                                }
                            }
                        }
                    }

                    double novaMedia = (novoTotalAvaliacoes > 0) ? novoTotalNotas / novoTotalAvaliacoes : 0.0;
                    double mediaArredondada = Math.round(novaMedia * 10.0) / 10.0;
                    Log.d(TAG, "Cálculo Final: Média=" + mediaArredondada + ", Contagem=" + novoTotalAvaliacoes);
                    Log.d(TAG, "Melhor Comentário: \"" + melhorComentario + "\" por " + autorMelhorComentario);


                    // --- ATUALIZAÇÃO NO FIREBASE ---
                    WriteBatch batch = db.batch();

                    // 1. Salva a nova avaliação na coleção "avaliacoes"
                    DocumentReference newRatingRef = db.collection("avaliacoes").document();
                    batch.set(newRatingRef, review);

                    // 2. Prepara a atualização no perfil do usuário na coleção "users"
                    DocumentReference userRef = db.collection("users").document(avaliadoId);

                    // Atualiza TODOS os campos necessários
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ratingMedia", mediaArredondada);
                    updates.put("ratingCount", novoTotalAvaliacoes);
                    updates.put("comentarioMelhorAvaliado", melhorComentario);
                    updates.put("autorComentarioMelhorAvaliado", autorMelhorComentario);

                    batch.update(userRef, updates);

                    // 3. Executa a transação
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Sucesso! Campos de avaliação atualizados para o usuário " + avaliadoId);
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erro ao atualizar campos de avaliação para o usuário " + avaliadoId, e);
                                callback.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar avaliações existentes para o usuário " + avaliadoId, e);
                    callback.onError(e);
                });
    }
}