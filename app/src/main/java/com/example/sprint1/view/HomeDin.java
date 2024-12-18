package com.example.sprint1.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sprint1.R;
import com.example.sprint1.viewmodel.MainViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class HomeDin extends AppCompatActivity {

    private final MainViewModel mainViewModel = new MainViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_din);

        this.listDining();
        this.buttonAddDining();
        this.buttonNavigationBar();
    }

    private void listDining() {
        // Find the dining container
        LinearLayout diningContainer = findViewById(R.id.dining_records_container);

        // Clear previous records
        diningContainer.removeAllViews();

        // Fetch dining records from ViewModel
        mainViewModel.getDining(diningData -> {
            if (diningData == null || diningData.isEmpty()) {
                return;
            }

            // Prepare a list of dining data with time parsing
            List<Map.Entry<String, HashMap<String, String>>> sortedDiningList;
            sortedDiningList = new ArrayList<>(diningData.entrySet());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

            // Sort dining records by time
            sortedDiningList.sort((entry1, entry2) -> {
                String time1 = entry1.getValue() != null ? entry1.getValue().get("time") : null;
                String time2 = entry2.getValue() != null ? entry2.getValue().get("time") : null;

                if ((time1 == null || time1.isEmpty()) && (time2 == null || time2.isEmpty())) {
                    return 0;
                }
                if (time1 == null || time1.isEmpty()) {
                    return 1; // Treat null or empty as greater to sort it last
                }
                if (time2 == null || time2.isEmpty()) {
                    return -1; // Treat null or empty as greater to sort it last
                }

                try {
                    Date date1 = dateFormat.parse(time1);
                    Date date2 = dateFormat.parse(time2);
                    if (date1 == null || date2 == null) {
                        return 0;
                    }
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    return 0;
                }
            });

            // Get current date
            Date currentDate = new Date();

            // Create cards for each dining record
            for (Map.Entry<String, HashMap<String, String>> entry : sortedDiningList) {
                HashMap<String, String> diningInfo = entry.getValue();

                // Parse the time for comparison
                Date reservationDate = null;
                String timeString = diningInfo.get("time");

                if (timeString != null && !timeString.isEmpty()) {
                    try {
                        reservationDate = dateFormat.parse(timeString);
                    } catch (ParseException ignored) { }
                }

                // Inflate the dining card layout
                View diningCard = getLayoutInflater()
                        .inflate(R.layout.home_din_card, diningContainer, false);

                // Set dining details
                TextView locationText = diningCard.findViewById(R.id.Home_Din_ListDining_Location);
                TextView websiteText = diningCard.findViewById(R.id.Home_Din_ListDining_Website);
                TextView timeText = diningCard.findViewById(R.id.Home_Din_ListDining_Time);

                locationText.setText(String.format(
                        "%s %s", getString(R.string.Home_Din_ListDining_Location),
                        diningInfo.getOrDefault("location", "")
                ));
                websiteText.setText(String.format(
                        "%s %s", getString(R.string.Home_Din_ListDining_Website),
                        diningInfo.getOrDefault("website", "")
                ));
                timeText.setText(String.format(
                        "%s %s", getString(R.string.Home_Din_ListDining_Time),
                        diningInfo.getOrDefault("time", "")
                ));

                // Change the color of the time text based on whether the reservation is
                // in the past or future
                if (reservationDate != null) {
                    if (reservationDate.before(currentDate)) {
                        // Past reservation: red text
                        timeText.setTextColor(
                                ContextCompat.getColor(this, android.R.color.holo_red_dark)
                        );
                    } else {
                        // Upcoming reservation: green text
                        timeText.setTextColor(
                                ContextCompat.getColor(this, android.R.color.holo_green_dark)
                        );
                    }
                }

                // Add the card to the container
                diningContainer.addView(diningCard);
            }
        });
    }

    private void buttonAddDining() {
        Button addDiningButton = findViewById(R.id.Home_Din_AddDining);
        LinearLayout addDiningForm = findViewById(R.id.Home_Din_AddDining_Form);

        EditText inputLocation = findViewById(R.id.Home_Din_AddDining_Location);
        EditText inputWebsite = findViewById(R.id.Home_Din_AddDining_Website);
        EditText inputTime = findViewById(R.id.Home_Din_AddDining_Time);

        addDiningButton.setOnClickListener(v -> {
            if (addDiningForm.getVisibility() == View.GONE) {
                addDiningForm.setVisibility(View.VISIBLE);
            } else {
                addDiningForm.setVisibility(View.GONE);
                inputLocation.setText("");
                inputWebsite.setText("");
                inputTime.setText("");
            }
        });

        findViewById(R.id.Home_Din_AddDining_Cancel).setOnClickListener(v -> {
            addDiningForm.setVisibility(View.GONE);
            inputLocation.setText("");
            inputWebsite.setText("");
            inputTime.setText("");
        });

        findViewById(R.id.Home_Din_AddDining_Submit).setOnClickListener(v -> {
            String location = inputLocation.getText().toString().trim();
            String website = inputWebsite.getText().toString().trim();
            String time = inputTime.getText().toString().trim();
            mainViewModel.addDining(location, website, time, success -> {
                if (success) {
                    this.listDining();
                    addDiningForm.setVisibility(View.GONE);
                    inputLocation.setText("");
                    inputWebsite.setText("");
                    inputTime.setText("");
                }
            });
        });
    }

    private void buttonNavigationBar() {
        findViewById(R.id.view_din_button_log).setOnClickListener(
                v -> startActivity(new Intent(HomeDin.this, HomeLog.class))
        );
        findViewById(R.id.view_din_button_des).setOnClickListener(
                v -> startActivity(new Intent(HomeDin.this, HomeDes.class))
        );
        findViewById(R.id.view_din_button_acc).setOnClickListener(
                v -> startActivity(new Intent(HomeDin.this, HomeAcc.class))
        );
        findViewById(R.id.view_din_button_tra).setOnClickListener(
                v -> startActivity(new Intent(HomeDin.this, HomeTra.class))
        );
    }
}