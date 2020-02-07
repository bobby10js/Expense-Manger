package com.example.myexpense;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static com.example.myexpense.MainActivity.myDataBase;

public class ExpenseActivity extends AppCompatActivity {
    private int year, month, day;
    float tempCost;
    EditText costView;
    AutoCompleteTextView expenseNameView;
    private TextView dateView,tempDateView,reminderDateView;
    Set<String> expenses;
    SharedPreferences sharedPreferences;
    RadioGroup radioGroup;
    int type=0,initCost=0,SiNo=0;


    private void showDate(int year, int month, int day) {
        tempDateView.setText(String.format("%02d/%02d/%04d", day,month,year));
    }


    public void costChange(View view) {
        costView= findViewById(R.id.costView);
        if(costView.length()==0)
            return;
        tempCost=Float.parseFloat(costView.getText().toString());
        switch(view.getId()){
            case R.id.costDown:
                tempCost--;
                break;
            case R.id.costUp:
                tempCost++;
        }
        if(tempCost<0)
            tempCost=0;
        costView.setText(String.valueOf(tempCost));
    }

    public void dateReload(View view) {
        RelativeLayout v = (RelativeLayout) view.getParent();
        ((TextView) v.getChildAt(0)).setText(String.format("%02d/%02d/%04d", day,month+1,year));
    }

    public void saveExpense(View view) {
        Switch sw = findViewById(R.id.switch1);
        int cost;
        String definition,date,reminderDate;
        ContentValues contentValues = new ContentValues();
        try {
            definition =  expenseNameView.getText().toString();
            cost = Integer.parseInt(costView.getText().toString());
            date = dateView.getText().toString();
            reminderDate=reminderDateView.getText().toString();

            if(expenseNameView.length()==0){ //customizable
                Toast.makeText(this, "Write something to remember the expense", Toast.LENGTH_LONG).show();
                return;
            }
            for (int i=0;i<3;i++)
                if(((RadioButton)radioGroup.getChildAt(i)).isChecked())
                    type = i-1;
            if(type==-1)
                cost=cost*-1;
            Cursor resultSet = myDataBase.rawQuery("Select * from ExpenseTable ",null);
            contentValues.put("Cost", cost);
            contentValues.put("Date", date);
            contentValues.put("Expense", definition);
            contentValues.put("Type",type);
            if(reminderDateView.length()==0 || !sw.isChecked()){
                contentValues.put("ReminderDate", "");
            }else {
                contentValues.put("ReminderDate", reminderDate);
            }
            contentValues.put("SiNo", SiNo);
            if(getIntent().getExtras() ==null) {
                myDataBase.insert("ExpenseTable", null, contentValues);
            }
            else {
                String whereClause = "SiNo="+SiNo;
                myDataBase.update("ExpenseTable", contentValues, whereClause, null);
            }
            resultSet.close();
        }
        catch(NumberFormatException e){
                LinearLayout linearlayout = findViewById(R.id.constraintLayout);
                Snackbar snackbar = Snackbar.make(linearlayout, "Empty fields", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
        }
        catch (Exception e){
                finish();
                return;
        }
        sharedPreferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        expenses.add(definition.toLowerCase());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        MainActivity.myBalance-=(cost-initCost);
        edit.putInt("myBalance",MainActivity.myBalance);
        edit.putStringSet("autoCompleteSuggestion",expenses);
        edit.apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Drawable navIcon = getDrawable(R.drawable.back);
        Bitmap bitmap = ((BitmapDrawable) navIcon).getBitmap();
        navIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 70, 70, true));
        myToolbar.setNavigationIcon(navIcon);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        expenseNameView = findViewById(R.id.expenseNameView);
        radioGroup = findViewById(R.id.radioGroup);
        costView = findViewById(R.id.costView);
        dateView = findViewById(R.id.dateText);
        reminderDateView = findViewById(R.id.dateReminderText);
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempDateView=(TextView) view;
                showDialog(999);
            }
        });

        reminderDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempDateView=(TextView) view;
                showDialog(999);
            }
        });
        Cursor resultSet = myDataBase.rawQuery("Select * from ExpenseTable ",null);
        SiNo = getIntent().getIntExtra("dataArray",resultSet.getCount()+1);
        if(getIntent().getExtras() !=null) {
                Cursor rs1 = myDataBase.rawQuery("Select * from ExpenseTable where SiNo="+SiNo+";", null);
                if(rs1.moveToFirst()){
                    expenseNameView.setText(rs1.getString(1));
                    costView.setText(rs1.getString(2));
                    initCost=rs1.getInt(2);
                    dateView.setText(rs1.getString(3));
                    type=rs1.getInt(4);
                    radioGroup.clearCheck();
                    ((RadioButton)(radioGroup.getChildAt(type+1))).setChecked(true);
                    Toast.makeText(this, ""+rs1.getString(5), Toast.LENGTH_SHORT).show();
                    if(rs1.getString(5)!=null){
                        reminderDateView.setText(rs1.getString(5));
                        Reminder(null);
                    }
                }
        }
        else{
            dateView.setText(String.format("%02d/%02d/%04d", day,month+1,year));
        }
        sharedPreferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        expenses=sharedPreferences.getStringSet("autoCompleteSuggestion", new HashSet<String>());
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,expenses.toArray());
        expenseNameView.setAdapter(adapter);
        expenseNameView.setThreshold(1);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
                finish();
                return true;
        }

    public void Reminder(View view) {
        Switch sw;
        if(view!=null) {
             sw = (android.widget.Switch) view;
        }
        else{
             sw = findViewById(R.id.switch1);
             sw.setChecked(true);
        }
        RelativeLayout relativeLayout = findViewById(R.id.dateReminderLayout);
        if(sw.isChecked()){
            relativeLayout.setAlpha(1);
            relativeLayout.setClickable(true);
        }
        else{
            relativeLayout.setAlpha(0);
            relativeLayout.setClickable(false);
        }
    }
}




