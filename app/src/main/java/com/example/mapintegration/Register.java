package com.example.mapintegration;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Register extends AppCompatActivity {

    private EditText editTextName, editTextPhone;
    private Button buttonAdd, buttonDelete, buttonView;
    private ListView listViewContacts;
    private com.example.mapintegration.DatabaseHandler myDB;
    private ArrayList<String> contactList;
    private ArrayAdapter<String> adapter;
    private SQLiteDatabase sqLiteDatabase;

    //@SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonView = findViewById(R.id.buttonView);
        listViewContacts = findViewById(R.id.listViewContacts);

        myDB = new com.example.mapintegration.DatabaseHandler(this);
        contactList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        listViewContacts.setAdapter(adapter);

        buttonAdd.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            if (!name.isEmpty() && !phone.isEmpty()) {
                String contact = name + ": " + phone;
                addData(contact);
                editTextName.setText("");
                editTextPhone.setText("");
            } else {
                Toast.makeText(this, "Please enter both name and phone number.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDelete.setOnClickListener(v -> {
            String phone = editTextPhone.getText().toString().trim();
            if (!phone.isEmpty()) {
                deleteData(phone);
            } else {
                Toast.makeText(this, "Please enter a phone number to delete.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonView.setOnClickListener(v -> loadData());
    }

    private void loadData() {
        contactList.clear();
        Cursor data = myDB.getListContents();
        if (data.getCount() == 0) {
            Toast.makeText(this, "No contacts found.", Toast.LENGTH_SHORT).show();
        } else {
            while (data.moveToNext()) {
                contactList.add(data.getString(1)); // Assuming column 1 is contact details
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void addData(String newEntry) {
        boolean insertData = myDB.addData(newEntry);
        if (insertData) {
            Toast.makeText(this, "Contact Added.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error adding contact.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteData(String phone) {
        sqLiteDatabase = myDB.getWritableDatabase();
        boolean result = sqLiteDatabase.delete(com.example.mapintegration.DatabaseHandler.TABLE_NAME, com.example.mapintegration.DatabaseHandler.COL2 + "=?", new String[]{phone}) > 0;
        if (result) {
            Toast.makeText(this, "Contact Deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error deleting contact.", Toast.LENGTH_SHORT).show();
        }
    }
}
