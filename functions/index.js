// Importa as funções da V2, que são mais modulares
const { onCreate } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions"); // Logger da V2
const admin = require("firebase-admin");

// Inicializa o Admin SDK
admin.initializeApp();
const db = admin.firestore();

/**
 * Cloud Function V2 que é acionada sempre que um NOVO documento é criado na coleção 'avaliacoes'.
 * A sintaxe é um pouco diferente da V1, mas o objetivo é o mesmo.
 */
exports.updateUserRating = onCreate("avaliacoes/{avaliacaoId}", async (event) => {
  // 1. Obter os dados da nova avaliação que foi criada.
  // Na V2, os dados do snapshot estão em event.data.
  const snap = event.data;
  if (!snap) {
    logger.error("Não foi possível obter os dados do evento.");
    return;
  }
  const novaAvaliacao = snap.data();
  const avaliadoId = novaAvaliacao.avaliadoId;

  if (!avaliadoId) {
    logger.warn("O documento de avaliação não possui 'avaliadoId'.");
    return;
  }

  logger.info(`Iniciando atualização de nota para o usuário: ${avaliadoId}`);

  // 2. Obter todos os documentos de avaliação para este usuário específico.
  const avaliacoesRef = db.collection("avaliacoes").where("avaliadoId", "==", avaliadoId);
  const avaliacoesSnapshot = await avaliacoesRef.get();

  // 3. Calcular a nova média.
  let totalNotas = 0;
  let totalAvaliacoes = 0;

  avaliacoesSnapshot.forEach((doc) => {
    const nota = doc.data().nota;
    if (typeof nota === "number") {
      totalNotas += nota;
      totalAvaliacoes++;
    }
  });

  const novaRatingMedia = totalAvaliacoes > 0 ? totalNotas / totalAvaliacoes : 0;
  const mediaArredondada = Math.round(novaRatingMedia * 10) / 10;

  logger.info(`Nova média calculada: ${mediaArredondada} de ${totalAvaliacoes} avaliações.`);

  // 4. Obter a referência para o documento do usuário.
  const userDocRef = db.collection("users").doc(avaliadoId);

  // 5. Atualizar o documento do usuário.
  try {
    await userDocRef.update({
      ratingMedia: mediaArredondada,
      ratingCount: totalAvaliacoes,
    });
    logger.info(`Sucesso! Usuário ${avaliadoId} atualizado com a nova nota.`);
  } catch (error) {
    logger.error(`Falha ao atualizar o usuário ${avaliadoId}:`, error);
  }
});