package com.example.aulago;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class IntroActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Configura para tela cheia (Esconde barra de status e navegação)
        hideSystemBars();

        videoView = findViewById(R.id.videoView);
        Button btnSkip = findViewById(R.id.btnSkip);

        // 1. Caminho do vídeo (substitua 'video_intro' pelo nome do seu arquivo)
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video_intro;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // 2. O que acontece quando o vídeo acaba
        videoView.setOnCompletionListener(mp -> irParaLogin());

        // 3. Botão Pular
        btnSkip.setOnClickListener(v -> irParaLogin());

        // 4. Inicia o vídeo
        videoView.start();
    }

    private void irParaLogin() {
        // Impede que o método seja chamado múltiplas vezes (ex: clique duplo no pular)
        if (isFinishing()) return;

        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Mata a IntroActivity para o usuário não voltar para o vídeo ao apertar "Voltar"
    }

    private void hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}