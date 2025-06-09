package com.example.mysweetdiary;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private final String idUsuario = "usuario123";
    private PieChart pieChart;
    private TextView mainMoodText, mainIconText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_graph);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation();

        pieChart = findViewById(R.id.pieChart);
        mainMoodText = findViewById(R.id.mainMood);
        mainIconText = findViewById(R.id.mainIcon);
        db = FirebaseFirestore.getInstance();

        carregarDados();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_graph);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_diary) {
                startActivity(new Intent(this, DiaryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }

    private void carregarDados() {
        db.collection("entradas")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> contagemHumores = new HashMap<>();
                    Map<String, Integer> contagemEmojis = new HashMap<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        List<String> emocoes = (List<String>) doc.get("emocao");
                        if (emocoes != null) {
                            for (String e : emocoes) {
                                contagemHumores.put(e, contagemHumores.getOrDefault(e, 0) + 1);

                                String emoji = getEmojiFromTexto(e);
                                if (emoji != null) {
                                    contagemEmojis.put(emoji, contagemEmojis.getOrDefault(emoji, 0) + 1);
                                }
                            }
                        }
                    }

                    preencherGrafico(contagemHumores);
                    mostrarPrincipalHumor(contagemHumores);
                    mostrarEmojiPrincipal(contagemEmojis);
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Erro ao carregar dados", e));
    }

    private void preencherGrafico(Map<String, Integer> dados) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dados.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Humores");
        dataSet.setColors(Color.rgb(244, 67, 54), Color.rgb(33, 150, 243), Color.rgb(76, 175, 80),
                Color.rgb(255, 193, 7), Color.rgb(156, 39, 176));

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Distribuição de Humor");
        pieChart.setCenterTextSize(18f);
        pieChart.animateY(1000);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(14f);

        pieChart.invalidate();
    }

    private void mostrarPrincipalHumor(Map<String, Integer> dados) {
        if (!dados.isEmpty()) {
            String maisFrequente = Collections.max(dados.entrySet(), Map.Entry.comparingByValue()).getKey();
            mainMoodText.setText(maisFrequente);
        }
    }

    private void mostrarEmojiPrincipal(Map<String, Integer> dados) {
        if (!dados.isEmpty()) {
            String maisUsado = Collections.max(dados.entrySet(), Map.Entry.comparingByValue()).getKey();
            mainIconText.setText(maisUsado);
        }
    }

    private String getEmojiFromTexto(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            int type = Character.getType(texto.charAt(i));
            if (type == Character.SURROGATE || Character.isSurrogate(texto.charAt(i))) {
                return texto.substring(i);
            }
        }
        return null;
    }
}
