package com.welserv.samplesocialmediaapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.welserv.samplesocialmediaapp.R;
import com.welserv.samplesocialmediaapp.entity.PostComments;
import com.welserv.samplesocialmediaapp.utils.CommonFun;

import java.util.ArrayList;

public class PostCommentsAdapter extends RecyclerView.Adapter<PostCommentsAdapter.ViewHolder> {


    private final ArrayList<PostComments> commenstList;
    private final Context context;

    public PostCommentsAdapter(Context mContext, ArrayList<PostComments> postComments) {
        context = mContext;
        commenstList = postComments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewUserName.setText("" + commenstList.get(holder.getAdapterPosition()).getUserName().toString());
        holder.textViewUserComment.setText("" + commenstList.get(holder.getAdapterPosition()).getUserComments().toString());
        holder.textViewCommentTime.setText("" + CommonFun.getDate(Long.parseLong(commenstList.get(holder.getAdapterPosition()).getCommentTime()), "hh:mm aa  dd/MMM/yyyy"));
    }

    @Override
    public int getItemCount() {
        return commenstList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName, textViewUserComment, textViewCommentTime;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewUserComment = itemView.findViewById(R.id.user_comment_comments_row);
            textViewUserName = itemView.findViewById(R.id.user_name_comments_row);
            textViewCommentTime = itemView.findViewById(R.id.comment_time_comments_row);
        }
    }
}
