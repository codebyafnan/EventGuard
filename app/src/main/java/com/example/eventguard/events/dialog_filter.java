package com.example.eventguard.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventguard.R;

public class dialog_filter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dialog_filter);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize CheckBoxes from activity_dialog_filter.xml
        CheckBox cbRegistered = findViewById(R.id.cbRegistered);
        CheckBox cbJoin = findViewById(R.id.cbJoin);
        CheckBox cbFull = findViewById(R.id.cbFull);

        CheckBox cbWorkshop = findViewById(R.id.cbWorkshop);
        CheckBox cbSeminar = findViewById(R.id.cbSeminar);
        CheckBox cbExhibition = findViewById(R.id.cbExhibition);
        CheckBox cbHackathon = findViewById(R.id.cbHackathon);

        CheckBox cbWeek = findViewById(R.id.cbWeek);
        CheckBox cbMonth = findViewById(R.id.cbMonth);
        CheckBox cbNewest = findViewById(R.id.cbNewest);
        CheckBox cbOldest = findViewById(R.id.cbOldest);

        Button btnApply = findViewById(R.id.btnApplyFilter);

        btnApply.setOnClickListener(v -> {
            // Collect filter states
            boolean isRegistered = cbRegistered.isChecked();
            boolean isJoin = cbJoin.isChecked();
            boolean isFull = cbFull.isChecked();

            boolean isWorkshop = cbWorkshop.isChecked();
            boolean isSeminar = cbSeminar.isChecked();
            boolean isExhibition = cbExhibition.isChecked();
            boolean isHackathon = cbHackathon.isChecked();

            boolean isWeek = cbWeek.isChecked();
            boolean isMonth = cbMonth.isChecked();
            boolean isNewest = cbNewest.isChecked();
            boolean isOldest = cbOldest.isChecked();

            // Apply filter logic
            applyFilters(isRegistered, isJoin, isFull, isWorkshop, isSeminar, isExhibition, isHackathon, isWeek, isMonth, isNewest, isOldest);
        });
    }

    private void applyFilters(boolean isRegistered, boolean isJoin, boolean isFull,
                              boolean isWorkshop, boolean isSeminar, boolean isExhibition,
                              boolean isHackathon, boolean isWeek, boolean isMonth,
                              boolean isNewest, boolean isOldest) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isRegistered", isRegistered);
        resultIntent.putExtra("isJoin", isJoin);
        resultIntent.putExtra("isFull", isFull);
        resultIntent.putExtra("isWorkshop", isWorkshop);
        resultIntent.putExtra("isSeminar", isSeminar);
        resultIntent.putExtra("isExhibition", isExhibition);
        resultIntent.putExtra("isHackathon", isHackathon);
        resultIntent.putExtra("isWeek", isWeek);
        resultIntent.putExtra("isMonth", isMonth);
        resultIntent.putExtra("isNewest", isNewest);
        resultIntent.putExtra("isOldest", isOldest);
        
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Filters Applied", Toast.LENGTH_SHORT).show();
        finish();
    }
}
