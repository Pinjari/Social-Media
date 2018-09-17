package com.welserv.samplesocialmediaapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.welserv.samplesocialmediaapp.R;
import com.welserv.samplesocialmediaapp.entity.SharedPostUserDetails;
import com.welserv.samplesocialmediaapp.utils.CommonFun;
import com.welserv.samplesocialmediaapp.view.UserVisitInfoActivity;

import java.util.ArrayList;

public class SharedByUsersAdapter extends RecyclerView.Adapter<SharedByUsersAdapter.ViewHolder> {
    private final ArrayList<SharedPostUserDetails> sharedByList;
    private final Context context;

    public SharedByUsersAdapter(ArrayList<SharedPostUserDetails> sharedBy, Context mContext) {
        sharedByList = sharedBy;
        context = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.shared_by_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.textViewsharedTime.setText("" + CommonFun.getDate(Long.parseLong(sharedByList.get(holder.getAdapterPosition()).getSharedTime()), "hh:mm aa  dd/MMM/yyyy"));
        holder.textViewUserName.setText("" + sharedByList.get(holder.getAdapterPosition()).getUserName());
        holder.textViewUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, UserVisitInfoActivity.class)
                        .putExtra("post_user_id", sharedByList.get(holder.getAdapterPosition()).getUserID())
                        .putExtra("post_user_name", sharedByList.get(holder.getAdapterPosition()).getUserName().toString()));

            }
        });


    }

    @Override
    public int getItemCount() {
        return sharedByList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName, textViewsharedTime;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.shared_user_name);
            textViewsharedTime = itemView.findViewById(R.id.shared_time);

        }
    }
}
