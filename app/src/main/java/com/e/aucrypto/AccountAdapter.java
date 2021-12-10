package com.e.aucrypto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.Viewholder> {

    private Context context;
    private ArrayList<EtherAccount> accountArrayList;
    private RecyclerViewOnclickListner listner;
    // Constructor
    public AccountAdapter(Context context, ArrayList<EtherAccount> acArrayList, RecyclerViewOnclickListner listner) {
        this.context = context;
        this.accountArrayList = acArrayList;
        this.listner=listner;
    }

    @NonNull
    @Override
    public AccountAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardaccount, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountAdapter.Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        EtherAccount model = accountArrayList.get(position);
        holder.accountname.setText(model.get_name());
        holder.accountimage.setImageResource(R.drawable.logo1);

    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return accountArrayList.size();
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView accountimage;
        private TextView accountname;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            accountname = itemView.findViewById(R.id.accountName);
            accountimage = itemView.findViewById(R.id.accountImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listner.onClick(itemView, getAdapterPosition());
        }
    }

    public interface RecyclerViewOnclickListner{
        void onClick(View v, int position);
    }

}
