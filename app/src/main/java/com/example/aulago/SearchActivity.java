package com.example.aulagoagoravai;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private CardView card1, card2, card3, card4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Inicializar componentes
        etSearch = findViewById(R.id.etSearch);
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);

        // Configurar botões
        Button btnContact1 = findViewById(R.id.btnContact1);
        Button btnContact2 = findViewById(R.id.btnContact2);
        Button btnContact3 = findViewById(R.id.btnContact3);
        Button btnContact4 = findViewById(R.id.btnContact4);

        // Listeners dos botões
        btnContact1.setOnClickListener(v -> openProfile("João Martins", "24 anos", R.drawable.joao));
        btnContact2.setOnClickListener(v -> openProfile("Maria Silva", "31 anos", R.drawable.mariasilva));
        btnContact3.setOnClickListener(v -> openProfile("Pedro Santos", "19 anos", R.drawable.pedrocosta));
        btnContact4.setOnClickListener(v -> openProfile("Ana Costa", "27 anos", R.drawable.abacosta));

        // Filtro de busca
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void openProfile(String name, String age, int imageResource) {
        Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
        intent.putExtra("STUDENT_NAME", name);
        intent.putExtra("STUDENT_AGE", age);
        intent.putExtra("STUDENT_IMAGE", imageResource);
        startActivity(intent);
    }

    private void filterStudents(String query) {
        // João Martins
        if ("joão martins".contains(query) || "joao martins".contains(query)) {
            card1.setVisibility(View.VISIBLE);
        } else {
            card1.setVisibility(View.GONE);
        }

        // Maria Silva
        if ("maria silva".contains(query)) {
            card2.setVisibility(View.VISIBLE);
        } else {
            card2.setVisibility(View.GONE);
        }

        // Pedro Santos
        if ("pedro santos".contains(query)) {
            card3.setVisibility(View.VISIBLE);
        } else {
            card3.setVisibility(View.GONE);
        }

        // Ana Costa
        if ("ana costa".contains(query)) {
            card4.setVisibility(View.VISIBLE);
        } else {
            card4.setVisibility(View.GONE);
        }
    }
}
