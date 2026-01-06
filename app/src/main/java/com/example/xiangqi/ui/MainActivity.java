package com.example.xiangqi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.xiangqi.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_pvp).setOnClickListener(v -> startGame(false));
        findViewById(R.id.btn_pve).setOnClickListener(v -> startGame(true));
    }

    private void startGame(boolean isPve) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("IS_PVE", isPve);
        startActivity(intent);
    }
}
