package com.e.aucrypto;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Provider;
import java.security.Security;
import java.util.concurrent.ExecutionException;

public class WallectAccountActivity extends AppCompatActivity {

   //pass required for transaction only

    String address; String fullpath; String name; TextView t1,t2;
    private Web3j web3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallect_account);
        Intent intent = getIntent();
        if (null != intent) { //Null Checking
            address=intent.getStringExtra("address");
            name=intent.getStringExtra("name");
            fullpath=intent.getStringExtra("fullpath");

        }

        t1=findViewById(R.id.accname);
        t2=findViewById(R.id.accaddress);
        t1.setText(name);
        t2.setText(address);
        setupBouncyCastle();
        connectToEthNetwork();

    }

    public void ccopy(View view){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("public address", address);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"address copied",Toast.LENGTH_SHORT).show();
    }

    public void del(View view){
        SQLiteDatabase database;
        database=this.openOrCreateDatabase("Accounts",MODE_PRIVATE,null);
        database.delete("accounts", "accaddress = '"+address+"'",null);
        Toast.makeText(this,"Deleted",Toast.LENGTH_SHORT).show();
    }

    public void createAndShowDialog() {

        final EditText etext;
        String topass;
        Button go;
        final AlertDialog.Builder alert= new AlertDialog.Builder(WallectAccountActivity.this);
        View mview= getLayoutInflater().inflate(R.layout.walletloginallert,null);
        alert.setView(mview);
        final AlertDialog dialog= alert.create();
        alert.setCancelable(true);
        go= (Button) mview.findViewById(R.id.go);
        etext= (EditText) mview.findViewById(R.id.passwordeditText);
        final EditText rec=(EditText) mview.findViewById(R.id.sendaddress);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String topass=etext.getText().toString();
                    Credentials credentials = WalletUtils.loadCredentials(topass, fullpath);
                    TransactionReceipt receipt = Transfer.sendFunds(web3,credentials,rec.getText().toString(),new BigDecimal(0.5), Convert.Unit.ETHER).sendAsync().get();
                    toastAsync("Transaction complete: " +receipt.getTransactionHash());
                }
                catch (Exception e){
                    toastAsync(e.toString());

                }

            }
        });

        dialog.show();


    }

    public void sendTransaction(View v){
        createAndShowDialog();
    }

    public  void  balance(View v){

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
        toastAsync("Your balance is " +  wei.toString());

    }



    public void connectToEthNetwork() {
        //toastAsync("Connecting to Ethereum network...");
        // FIXME: Add your own API key here
        web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/8760bea229ba4c0fb1b51baa321f9bcd"));
        try {
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if(!clientVersion.hasError()){
                //toastAsync("Connected!");
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
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
}
