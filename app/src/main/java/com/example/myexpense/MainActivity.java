package com.example.myexpense;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.text.InputType;
import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashSet;
import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    static SQLiteDatabase myDataBase;
    public static int width, height, myBalance = 0, calenderViewState;
    SharedPreferences sharedPreferences;
    LinearLayout linearViewTable;
    static int totalExpense=0;
    int navigationId =  R.id.expenses;

    public void setClick(View view) {
        linearViewTable.removeAllViews();
        ImageButton tempImageView =((ImageButton)((GridLayout)view.getParent()).getChildAt(calenderViewState));
        TextView tempTextView =((TextView)((GridLayout)view.getParent()).getChildAt(calenderViewState+4));
        tempTextView.setTextColor(getColor(R.color.black));
        switch(calenderViewState){
            case 0:
                tempImageView.setImageDrawable(getDrawable(R.drawable.day_view_0));
                break;
            case 1:
                tempImageView.setImageDrawable(getDrawable(R.drawable.month_view_0_30));
                break;
            case 2:
                tempImageView.setImageDrawable(getDrawable(R.drawable.year_view_0_30));
                break;
            case 3:
                tempImageView.setImageDrawable(getDrawable(R.drawable.alltime_view_0_30));
                break;
        }

        tempImageView = (ImageButton) view;
        switch(view.getTag().toString()) {
            case "daily0":
                ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT DISTINCT Date FROM ExpenseTable;", null));
                tempImageView.setImageDrawable(getDrawable(R.drawable.day_view_1));
                calenderViewState = 0;
                break;
            case "monthly0":
                ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT DISTINCT SUBSTR(Date,4) FROM ExpenseTable;", null));
                tempImageView.setImageDrawable(getDrawable(R.drawable.month_view_1_30));
                calenderViewState = 1;
                break;
            case "yearly0":
                ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT DISTINCT SUBSTR(Date,7) FROM ExpenseTable;", null));
                tempImageView.setImageDrawable(getDrawable(R.drawable.year_view_1_30));
                calenderViewState = 2;
                break;
            case "alltime0":
                ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT  Date FROM ExpenseTable;", null));
                tempImageView.setImageDrawable(getDrawable(R.drawable.alltime_view_1_30));
                calenderViewState = 3;
                break;
        }
        tempTextView =((TextView)((GridLayout)view.getParent()).getChildAt(calenderViewState+4));
        tempTextView.setTextColor(getColor(R.color.colorPrimary));

    }

    public void addBalance(View view) {
        AppBarLayout appBarLayout = findViewById(R.id.appBar);
        if (appBarLayout.findViewById(R.id.myBalanceEdit) == null) {
            EditText editText = new EditText(this);
            editText.setId(R.id.myBalanceEdit);
            editText.setBackground(getDrawable(R.drawable.rounded_corner_text));
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(width / 40, width / 40, width / 40, width / 40);
            appBarLayout.addView(editText, layoutParams);
        } else {
            EditText editText = findViewById(R.id.myBalanceEdit);
            Toast.makeText(this, editText.getText().length() + "", Toast.LENGTH_SHORT).show();
            if (editText.getText().length() != 0)
                try {
                    myBalance = Integer.parseInt((editText).getText().toString());
                } catch (Exception e) {
                    Toast.makeText(this, "That is too much to handle!", Toast.LENGTH_SHORT).show();
                }
            appBarLayout.removeView(editText);
        }
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt("myBalance", myBalance);
        edit.apply();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(myBalance>=0)
            toolbar.setSubtitle("₹" + myBalance + " ");
        else
            toolbar.setSubtitle("-₹" +  abs(myBalance) + " ");
    }

    public void startExpenseIntent(View view) {
        Intent i = new Intent(this, ExpenseActivity.class);
        startActivity(i);
    }

    public void refresh(){
        switch(MainActivity.calenderViewState){
            case 0:
                ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT DISTINCT Date FROM ExpenseTable;", null));
                break;
            case 1:
                ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT DISTINCT SUBSTR(Date,4) FROM ExpenseTable;", null));
                break;
            case 2:
                ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT DISTINCT SUBSTR(Date,7) FROM ExpenseTable;", null));
                break;
            case 3:
                ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT  Date FROM ExpenseTable;", null));
                break;
        }
    }

    public void searchView(View view) {
        EditText editText = findViewById(R.id.seachTextView);
        if(editText.getAlpha()==0){
            editText.animate().alpha(1).setDuration(3000);
        }
        else{
            editText.animate().alpha(0).setDuration(3000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        sharedPreferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        myBalance = sharedPreferences.getInt("myBalance", 0);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawable addMoney = getDrawable(R.drawable.rupees);
        Bitmap bitmap = ((BitmapDrawable) addMoney).getBitmap();
        addMoney = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 60, 60, true));
        ((ImageView)findViewById(R.id.imageView42)).setImageDrawable(addMoney);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bitmap =Bitmap.createBitmap(32,32,conf);
        addMoney = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 70, 70, true));
        ((ImageView)findViewById(R.id.imageView2)).setImageDrawable(addMoney);
        toolbar.setTitle("My Balance");
        if(myBalance>=0)
            toolbar.setSubtitle("₹" + myBalance + " ");
        else
            toolbar.setSubtitle("-₹" +  abs(myBalance) + " ");
        toolbar.setTitleTextAppearance(this, R.style.title);

        toolbar.setSubtitleTextAppearance(this, R.style.subtitle);
        toolbar.setMinimumHeight((int) (height * 0.15));
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        myDataBase = openOrCreateDatabase("ExpenseData", MODE_PRIVATE, null);
        myDataBase.execSQL("CREATE TABLE IF NOT EXISTS ExpenseTable(SiNo int(3),Expense VARCHAR, Cost INT(5),Date VARCHAR,Type INT(1),ReminderDate VARCHAR);");
        linearViewTable = findViewById(R.id.linearViewTable);


        linearViewTable.setPadding(width / 40, width / 40, width / 40, width / 40);
        ConvertDbToTableInitiator(myDataBase.rawQuery("SELECT DISTINCT Date FROM ExpenseTable;", null));
        calenderViewState=0;
        CardView calenderWiseCardView= findViewById(R.id.calenderWiseCardView);
        calenderWiseCardView.setBackground(getDrawable(R.drawable.rounded_corner_1));
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setCustomSize(calenderWiseCardView.getHeight());

//        final HorizontalScrollView sv = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
//        Drawable image=(Drawable)getResources().getDrawable(R.drawable.main_icon);
//        Matrix matrix = new Matrix();
//        matrix.postRotate(45);
//        Bitmap source = getDrawable(getResources(R.drawable.main_icon));
//        Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
//                matrix, true);
//        sv.setBackground(image);

//        final HorizontalScrollView sv = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
//        sv.smoothScrollTo((int)(sv.getX())+50,0);
////        sv.setScrollX(50);
////        sv.scrollBy(1,0);
//        sv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                int scrollX = sv.getScrollX(); // For HorizontalScrollView
////                Toast.makeText(MainActivity.this, ""+scrollX, Toast.LENGTH_SHORT).show();
////                    for (float x : arrayList){
////                        if (scrollX>x)
////                            sv.scrollTo((int)x,0);
////                    }
//                int n =Integer.parseInt((arrayList.get(1)).toString());
//                sv.scrollTo(n,0);
//                // DO SOMETHING WITH THE SCROLL COORDINATES
//            }
//        });
    }

    public void ConvertDbToTableInitiator(Cursor resultSet) {
        TextView textView;
        if (resultSet.moveToFirst()) {
            do {
                ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(width / 60, width / 60, width / 60, width / 60);
                CardView cardView = new CardView(this);//cardView->linearLayout->(textView,scrollView->tableLayout)
                LinearLayout linearLayout = new LinearLayout(this);
                ScrollView scrollView = new ScrollView(this);
                textView = new TextView(this);
                linearViewTable.addView(cardView, layoutParams);
                textView.setText(resultSet.getString(0));
                textView.setTextSize(((int) (height * 0.02)));
                scrollView.addView(ConvertDbToTable(myDataBase.rawQuery("Select SiNo,Expense,Cost,Date from ExpenseTable where Date like '%"+resultSet.getString(0)+"';", null), textView.length()==10));
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setGravity(LinearLayout.TEXT_ALIGNMENT_CENTER);
                linearLayout.addView(textView);
                linearLayout.addView(scrollView);
                cardView.addView(linearLayout,layoutParams);
                cardView.setBackground(getDrawable(R.drawable.rounded_corner));
                TextView totalExpense = new TextView(this);
                Cursor rs1 = myDataBase.rawQuery("Select SUM(Cost) from ExpenseTable where Date like '%"+resultSet.getString(0)+"';", null);
                rs1.moveToFirst();
                totalExpense.setText("Total Expense is ₹"+rs1.getString(0));
                totalExpense.setTextSize(((int) (height * 0.02)));
                linearLayout.addView(totalExpense);
            } while (resultSet.moveToNext());


        } else {
            textView = new TextView(this);
            textView.setText("Running on \nMoney Saver \nMode?");
            textView.setTextSize((int) (height * 0.04));
            textView.setTextColor(getResources().getColor(R.color.colorAccent));
            textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            linearViewTable.addView(textView);
        }
        resultSet.close();
    }

    public TableLayout ConvertDbToTable(Cursor resultSet, boolean flag) {
        int usedWidth = 0;
        totalExpense=0;
        TableLayout tableLayout;
        tableLayout = new TableLayout(this);
        TableRow tr_head = new TableRow(this);
        tr_head.setBackgroundColor(Color.GRAY);
        for (String columnName : resultSet.getColumnNames()) {
            if (columnName.equals("Date") && flag)
                continue;
            TextView label = new TextView(this);
            label.setText(columnName);
            label.setTextColor(Color.WHITE);
            switch (columnName) {
                case "Expense":
                    label.setWidth(width / 4);
                    usedWidth+=width/4;
                    break;
                case "SiNo":
                    label.setWidth(width / 11);
                    usedWidth+=width/10;
                    break;
                case "Cost":
                    label.setWidth(width / 6);
                    usedWidth+=width/6;
                    break;
                default:
                    label.setWidth((width- usedWidth) / 2);
            }
            tr_head.addView(label);
        }
        tableLayout.addView(tr_head, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        try {
            if (resultSet.moveToFirst()) {
                do {
                    tr_head = new TableRow(this);
                    tr_head.setBackgroundColor(Color.WHITE);
                    tr_head.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.FILL_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                    int i = 0;
                    while (i < resultSet.getColumnCount()) {
                        if (!(flag && i == 3)){
                            TextView label = new TextView(this);
                            label.setMaxWidth(width/3);
                            label.setText(resultSet.getString(i));
                            label.setPadding(5, 5, 5, 5);
                            label.setTextColor(Color.GRAY);
                            tr_head.addView(label);
                        }
                        i++;
                    }
                    final int SiNo = resultSet.getInt(0);
                    tableLayout.addView(tr_head, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    final ConstraintLayout mainLayout = findViewById(R.id.constraintLayout);
                    tr_head.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(v.getContext(), SiNo+"", Toast.LENGTH_SHORT).show();
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, "EDIT?", Snackbar.LENGTH_LONG)
                                    .setAction("YESSSSSSSSSSSSSS", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(view.getContext(), ExpenseActivity.class);
                                            intent.putExtra("dataArray", SiNo);
                                            startActivity(intent);
                                        }
                                    });
                            snackbar.show();
                        }
                    });
                } while (resultSet.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultSet.close();
        tableLayout.setPadding(width/100,width/100,width/100,width/100);
        return tableLayout;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();
        if(navigationId==id){
            //do nothing
        }
        else if (id == R.id.expenses) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.statistics) {

        } else if (id == R.id.reminders) {
//            Intent intent = new Intent(this, ReminderActivity.class);
//            startActivity(intent);
            linearViewTable.removeAllViews();
            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(width / 60, width / 60, width / 60, width / 60);
            CardView cardView =new CardView(this);
            linearViewTable.addView(cardView,layoutParams);
            cardView.setBackground(getDrawable(R.drawable.rounded_corner));
            cardView.addView((TableLayout)ConvertDbToTable(myDataBase.rawQuery("Select SiNo,Expense,Cost,Date,ReminderDate from ExpenseTable where ReminderDate!='';", null),false),layoutParams);
        } else if (id == R.id.export) {
            myDataBase.execSQL("DROP TABLE ExpenseTable");
        } else if (id == R.id.deleteDB) {
            String[] strings = {"Delete expense suggestion data?"};
            final boolean[] checkedItems = {false};
            new AlertDialog.Builder(this)
                    .setTitle("Delete database?")
                    .setMultiChoiceItems(strings, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            checkedItems[0] = true;
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myDataBase.execSQL("DELETE FROM ExpenseTable");
                            linearViewTable.removeAllViews();
                            if (checkedItems[0]) {
                                SharedPreferences.Editor edit = sharedPreferences.edit();
                                edit.putStringSet("autoCompleteSuggestion", new HashSet<String>());
                                edit.apply();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else if (id == R.id.about) {
        }
        navigationId=id;
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}