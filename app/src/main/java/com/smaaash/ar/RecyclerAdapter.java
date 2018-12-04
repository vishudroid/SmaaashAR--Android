package com.smaaash.ar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.smaaash.ar.videocamera.VideoCameraActivity;

import java.util.List;



public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ReviewViewHolder> {

    private static final String TAG = "RecyclerAdapter";
    private List<Integer> urlsList;
    private List<String> titlesList;
    private Context context;

    public RecyclerAdapter(List<Integer> urlsList, List<String> titlesList, Activity context) {
        this.urlsList = urlsList;
        this.titlesList = titlesList;
        this.context = context;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card_item, parent, false);
        ReviewViewHolder pvh = new ReviewViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        int url = urlsList.get(position);
        holder.iv_image.setBackgroundResource(url);

    }

    @Override
    public int getItemCount() {
        if (urlsList != null)
            return urlsList.size();
        else
            return 0;
    }

    public  class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_image;
        TextView tv_text;
        LinearLayout ll_card;


        public ReviewViewHolder(View itemView) {
            super(itemView);

            iv_image = (ImageView) itemView.findViewById(R.id.iv_image);
            tv_text = (TextView) itemView.findViewById(R.id.tv_text);
            ll_card = (LinearLayout) itemView.findViewById(R.id.ll_card);


            ll_card.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    String  whichContent = titlesList.get(getAdapterPosition());
                  //  Toast.makeText(context,whichContent,Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, VideoCameraActivity.class);
                    intent.putExtra("whichContent", whichContent);
                    context.startActivity(intent);


                }
            });


        }
    }
}
