package com.e.aucrypto;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity
{
    private Web3j web3;
    //FIXME: Add your own password here
    private String password ;
    private String walletPath,fileName;
    private File walletDir;
    String wal,ad,namee;
    SQLiteDatabase database;
    TextView btc,usd;

    public class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String rates="";
            URL url;
            HttpsURLConnection con= null;
            try{
                url=new URL(urls[0]);
                con=(HttpsURLConnection)url.openConnection();
                InputStream in=con.getInputStream();
                InputStreamReader re=new InputStreamReader(in);
                int data= re.read();
                while(data!=-1){
                    char current=(char)data;
                    rates+=current;
                    data=re.read();

                }

                return rates;

            }
            catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                return null;

            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                JSONObject js=new JSONObject(s);
                String eth=js.getString("ETH"); Log.i("dfafa", eth);
                JSONObject obj= new JSONObject(eth);

                    String us=obj.getString("USD");
                    String bt=obj.getString("BTC");
                    btc.setText(bt+" BTC"); usd.setText(us+" USD");
                Toast.makeText(getApplicationContext(),"Rates Updated!",Toast.LENGTH_SHORT).show();

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.imageView3);
        walletPath = getFilesDir().getAbsolutePath();
        database=this.openOrCreateDatabase("Accounts",MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS accounts(accname VARCHAR, accaddress VARCHAR, accpath VARCHAR)");
        btc=(TextView)findViewById(R.id.BTC);
        usd=(TextView)findViewById(R.id.USD);
        btc.setText(""); usd.setText("");
        DownloadTask task=new DownloadTask();

        task.execute("https://min-api.cryptocompare.com/data/pricemulti?fsyms=ETH,DASH&tsyms=BTC,USD,EUR&api_key=12c64697d7a2317582691e3b7bf502c217ea0d90a5e4979771992e589929878d");

        Glide.with(this)
                .load("https://media.giphy.com/media/L59aKIC2MFyfUfrz3n/giphy.gif")
                .into(imageView);
        setupBouncyCastle();

    }

    public void  refreshrates(View v){
        DownloadTask task=new DownloadTask();
        task.execute("https://min-api.cryptocompare.com/data/pricemulti?fsyms=ETH,DASH&tsyms=BTC,USD,EUR&api_key=12c64697d7a2317582691e3b7bf502c217ea0d90a5e4979771992e589929878d");

    }

    public void getAddress(){
        try {

            Credentials credentials = WalletUtils.loadCredentials(password, wal);
            ad=credentials.getAddress();        //public address is needed to check balance so store this also
        }
        catch (Exception e){
            toastAsync(e.getMessage());
        }
    }

    public void putindatabse(){
        try {
            database.execSQL("INSERT INTO accounts (accname, accaddress, accpath) VALUES('"+namee+"', '"+ad+"', '"+wal+"')");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Sorry couldnt add",Toast.LENGTH_SHORT).show();

        }
    }
    public void createAndShowDialog() {

        final EditText etext;
        Button create;
        final AlertDialog.Builder alert= new AlertDialog.Builder(MainActivity.this);
        View mview= getLayoutInflater().inflate(R.layout.createwallet,null);
        alert.setView(mview);
        final AlertDialog dialog= alert.create();
        alert.setCancelable(true);
        create= (Button) mview.findViewById(R.id.create);
        etext=mview.findViewById(R.id.createpass);
        final EditText ename=mview.findViewById(R.id.createname);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etext.getText().toString().length()==0||ename.getText().toString().length()==0)Toast.makeText(MainActivity.this,"Write something",Toast.LENGTH_SHORT ).show();
                else{
                    try{
                        connectToEthNetwork();
                        password=etext.getText().toString(); namee=ename.getText().toString();
                        walletDir = new File(walletPath);// the etherium wallet location
                        //create the directory if it does not exist
                        if (!walletDir.mkdirs() ) {
                            walletDir.mkdirs();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Directory already created",
                                    Toast.LENGTH_LONG).show();

                        }
                        fileName =  WalletUtils.generateLightNewWalletFile(password,walletDir);
                        // walletDir = new File(walletPath + "/" + fileName); //no need now as we have saved source in wal string
                        wal=walletPath + "/" + fileName;
                        Log.i("FFFFF", password);
                        toastAsync("Wallet generated");
                        getAddress();
                        putindatabse();

                    }
                    catch (Exception e){
                        toastAsync(e.getMessage());
                    }

                }

            }
        });

        dialog.show();


    }



    public void mywallet(View v){
       CardView cd=(CardView) findViewById(R.id.cardView2);
        cd.animate().scaleY(0.8f).scaleX(0.8f).setDuration(50);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                CardView cd=(CardView) findViewById(R.id.cardView2);
                cd.animate().scaleY(1f).scaleX(1f).setDuration(50);

            }
        }, 50);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent mainIntent = new Intent(MainActivity.this, accountlist.class);
                MainActivity.this.startActivity(mainIntent);

            }
        }, 100);

    }

    public void connectToEthNetwork() {
        //toastAsync("Connecting to Ethereum network...");
        // FIXME: Add your own API key here
        web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/8760bea229ba4c0fb1b51baa321f9bcd"));
        try {
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if(!clientVersion.hasError()){
               // toastAsync("Connected!");
            }
            else {
                toastAsync(clientVersion.getError().getMessage());
            }
        } catch (Exception e) {
            toastAsync(e.getMessage());
        }
    }

    public void createWallet(View v){
        createAndShowDialog();

    }

    public void toastAsync(String message) {

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Workaround for bug with ECDA signatures.
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


}