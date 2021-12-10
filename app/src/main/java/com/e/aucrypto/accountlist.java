package com.e.aucrypto;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class accountlist extends AppCompatActivity  {

    private PieChart pieChart;
    private RecyclerView recyclerView;
    private ArrayList<EtherAccount> accountArrayList;
    private AccountAdapter.RecyclerViewOnclickListner listner;
    private Web3j web3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountlist);
        setupBouncyCastle();
        SQLiteDatabase database=this.openOrCreateDatabase("Accounts",MODE_PRIVATE,null);

        database.execSQL("CREATE TABLE IF NOT EXISTS accounts(accname VARCHAR, accaddress VARCHAR, accpath VARCHAR)");

        recyclerView=findViewById(R.id.recycleraccount);
        accountArrayList=new ArrayList<>();
        try {
            Cursor cur = database.rawQuery("SELECT * FROM accounts", null);
            int nameindex = cur.getColumnIndex("accname");
            int addrindex = cur.getColumnIndex("accaddress");
            int pathindex = cur.getColumnIndex("accpath");
            if(cur.moveToFirst());
            while (cur!=null) {

                accountArrayList.add(new EtherAccount(cur.getString(nameindex), cur.getString(addrindex), cur.getString(pathindex)));
                 cur.moveToNext();
            }
        }
        catch (Exception e){
            e.printStackTrace();

        }

        setOnClickListner();
        AccountAdapter accountAdapter=new AccountAdapter(this, accountArrayList, listner);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(accountAdapter);


        pieChart = findViewById(R.id.pieChart);
        pieChart.setNoDataText("OOPS!! NO ACCOUNTS ");
        pieChart.setNoDataTextColor(Color.WHITE);
        setupPieChart();
        loadPieChartData();


    }

    public void setOnClickListner(){
        listner= new AccountAdapter.RecyclerViewOnclickListner() {
            @Override
            public void onClick(View v, int position) {
                // Toast toast= Toast.makeText(accountlist.this, "Opening Wallet!",
                //        Toast.LENGTH_SHORT);
                // toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
                // toast.show();

                Intent mainIntent = new Intent(accountlist.this, WallectAccountActivity.class);
                mainIntent.putExtra("name",accountArrayList.get(position).get_name().toString());
                mainIntent.putExtra("fullpath",accountArrayList.get(position).get_fullpath().toString());
                mainIntent.putExtra("address",accountArrayList.get(position).get_address().toString());
                accountlist.this.startActivity(mainIntent);
                //accountlist.this.finish();
                //Log.i("FFF", accountArrayList.get(position).get_name().toString());

            }
        };
    }



    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Balance Division");
        pieChart.setCenterTextSize(12);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.BLACK);



    }

    private void loadPieChartData() {
        connectToEthNetwork();
        ArrayList<PieEntry> entries = new ArrayList<>();
        for(EtherAccount x: accountArrayList){
            entries.add( new PieEntry(balance(x.get_address()), x.get_name()));
        }
        //entries.add(new PieEntry(6f, "7"));

        Legend l=pieChart.getLegend();
        l.setEnabled(false);

        ArrayList<Integer> colors = new ArrayList<>();

        for (Integer color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }

        for (Integer color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }


    public void connectToEthNetwork() {
        toastAsync("Connecting to Ethereum network...");
        // FIXME: Add your own API key here
        web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/8760bea229ba4c0fb1b51baa321f9bcd"));
        try {
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if(!clientVersion.hasError()){
                toastAsync("Connected!");
            }
            else {
                toastAsync(clientVersion.getError().getMessage());
            }
        } catch (Exception e) {
            toastAsync(e.getMessage());
        }
    }
    public void toastAsync(String message) {

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }

        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public int  balance(String address){

        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = web3
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        BigInteger wei = ethGetBalance.getBalance();
        BigInteger div=new BigInteger("100000000");
        wei= wei.divide(div);
        int w=wei.intValue();
        return w;

    }



}



