package com.example.aulago;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // Layout com container

        // Adiciona o fragmento - apenas se não estiver já adicionado
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_chat, new ChatFragment())
                    .commit();
        }
    }
}
