package com.example.agriautomationhub;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SellingPriceCalculatorActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText editTextExpenseName, editTextPrice, editTextYield;
    private Button buttonSaveExpense, buttonAddNewExpense;
    private LinearLayout layoutNewExpenseForm;
    private TextView textViewLowestSellingPrice;
    private RecyclerView recyclerViewExpenses;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private double totalExpenses = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selling_price_calculator);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        editTextExpenseName = findViewById(R.id.editTextExpenseName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextYield = findViewById(R.id.editTextYield);
        buttonSaveExpense = findViewById(R.id.buttonSaveExpense);
        buttonAddNewExpense = findViewById(R.id.buttonAddNewExpense);
        layoutNewExpenseForm = findViewById(R.id.layoutNewExpenseForm);
        textViewLowestSellingPrice = findViewById(R.id.textViewLowestSellingPrice);
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);

        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(expenseList);

        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExpenses.setAdapter(expenseAdapter);

        // Populate spinnerCategory with predefined categories
        String[] categories = getResources().getStringArray(R.array.expense_categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Set up text watchers for enabling/disabling buttons
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues();
                updateLowestSellingPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextPrice.addTextChangedListener(textWatcher);
        editTextYield.addTextChangedListener(textWatcher);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkFieldsForEmptyValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        buttonSaveExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExpense();
            }
        });

        buttonAddNewExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the form for entering a new expense
                layoutNewExpenseForm.setVisibility(View.VISIBLE);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_selling_price);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return true;
            }else if (id == R.id.navigation_marketView) {
                // Handle News navigation
                startActivity(new Intent(getApplicationContext(), MarketViewActivity.class));
                return true;
            }else if (id == R.id.navigation_news) {
                // Handle News navigation
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(getApplicationContext(), MandiActivity.class));
                return true;
            }
            return false;
        });
    }

    private void checkFieldsForEmptyValues() {
        String price = editTextPrice.getText().toString();
        String yield = editTextYield.getText().toString();
        boolean isCategorySelected = spinnerCategory.getSelectedItemPosition() != 0;

        // Enable the save button only if price, category, and yield are filled
        buttonSaveExpense.setEnabled(!price.isEmpty() && isCategorySelected && !yield.isEmpty());
    }

    private void saveExpense() {
        String category = spinnerCategory.getSelectedItem().toString();
        String expenseName = editTextExpenseName.getText().toString();
        String priceText = editTextPrice.getText().toString();

        if (priceText.isEmpty()) {
            Toast.makeText(this, "Please enter the price", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceText);
        Expense expense = new Expense(category, expenseName, price);
        expenseList.add(expense);
        totalExpenses += price; // Update total expenses
        expenseAdapter.notifyDataSetChanged();
        updateLowestSellingPrice(); // Update the lowest selling price after saving

        // Keep the yield field filled, so user can continue using it
        // Clear only the category and expense name fields
        clearExpenseNameAndCategory();
        layoutNewExpenseForm.setVisibility(View.GONE);
    }
    private void clearExpenseNameAndCategory() {
        spinnerCategory.setSelection(0);
        editTextExpenseName.setText("");
        editTextPrice.setText("");
        // Do not clear editTextYield to keep the yield value
    }

    private void updateLowestSellingPrice() {
        String yieldText = editTextYield.getText().toString();
        if (!yieldText.isEmpty() && !expenseList.isEmpty()) {
            double yield = Double.parseDouble(yieldText); // Convert kg to quintals
            double lowestPrice = (totalExpenses / yield);
            double updatedlowestPrice = lowestPrice + (0.20 * lowestPrice);
            textViewLowestSellingPrice.setText("Lowest Selling Price: ₹" + String.format("%.2f", updatedlowestPrice));
        } else {
            textViewLowestSellingPrice.setText("Lowest Selling Price: ₹0.00");
        }
    }

    // Private Expense class
    private static class Expense {
        String category;
        String expenseName;
        double price;

        Expense(String category, String expenseName, double price) {
            this.category = category;
            this.expenseName = expenseName;
            this.price = price;
        }

        public String getCategory() {
            return category;
        }

        public String getExpenseName() {
            return expenseName;
        }

        public double getPrice() {
            return price;
        }
    }

    // Inner class for the adapter
    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
        private final List<Expense> expenseList;

        public ExpenseAdapter(List<Expense> expenseList) {
            this.expenseList = expenseList;
        }

        @NonNull
        @Override
        public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
            Expense expense = expenseList.get(holder.getAdapterPosition());
            holder.textViewCategory.setText(expense.getCategory());
            holder.textViewExpenseName.setText(expense.getExpenseName().isEmpty() ? "No Name" : expense.getExpenseName());
            holder.textViewPrice.setText("₹" + expense.getPrice());
            holder.imageButtonOptions.setBackgroundColor(0);

            holder.imageButtonOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(SellingPriceCalculatorActivity.this, holder.imageButtonOptions);
                    popupMenu.inflate(R.menu.menu_expense_options);
                    popupMenu.setOnMenuItemClickListener(item -> {
                        int currentPosition = holder.getAdapterPosition();
                        if (item.getItemId() == R.id.action_edit) {
                            editExpense(currentPosition);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete) {
                            deleteExpense(currentPosition);
                            return true;
                        }
                        return false;
                    });
                    popupMenu.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return expenseList.size();
        }

        class ExpenseViewHolder extends RecyclerView.ViewHolder {
            TextView textViewCategory, textViewExpenseName, textViewPrice;
            ImageButton imageButtonOptions;

            public ExpenseViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewCategory = itemView.findViewById(R.id.textViewCategory);
                textViewExpenseName = itemView.findViewById(R.id.textViewExpenseName);
                textViewPrice = itemView.findViewById(R.id.textViewPrice);
                imageButtonOptions = itemView.findViewById(R.id.imageButtonOptions);
            }
        }
    }

    private void editExpense(int position) {
        Expense expense = expenseList.get(position);
        spinnerCategory.setSelection(getCategoryPosition(expense.getCategory()));
        editTextExpenseName.setText(expense.getExpenseName());
        editTextPrice.setText(String.valueOf(expense.getPrice()));
        expenseList.remove(position);
        expenseAdapter.notifyItemRemoved(position);
        layoutNewExpenseForm.setVisibility(View.VISIBLE);
    }

    private void deleteExpense(int position) {
        Expense expense = expenseList.get(position);
        totalExpenses -= expense.getPrice(); // Update total expenses
        expenseList.remove(position);
        expenseAdapter.notifyItemRemoved(position);
        updateLowestSellingPrice(); // Update the lowest selling price after deletion
    }

    private int getCategoryPosition(String category) {
        String[] categories = getResources().getStringArray(R.array.expense_categories);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            return logoutUser();
        }
        if (id == R.id.action_settings) {
            return settings();
        }
        if (id == R.id.action_help) {
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean logoutUser() {
        FirebaseAuth.getInstance().signOut();
        // Redirect to login screen or any other desired activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    private boolean settings() {
        Intent intent = new Intent(getApplicationContext(), SettingsPage.class);
        startActivity(intent);
        return true;
    }
}
