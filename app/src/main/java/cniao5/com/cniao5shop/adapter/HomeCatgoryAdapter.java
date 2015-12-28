package cniao5.com.cniao5shop.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import cniao5.com.cniao5shop.R;
import cniao5.com.cniao5shop.bean.Campaign;
import cniao5.com.cniao5shop.bean.HomeCampaign;

/**
 * Created by Ivan on 15/9/30.
 */
public class HomeCatgoryAdapter extends RecyclerView.Adapter<HomeCatgoryAdapter.ViewHolder> {



    private  static int VIEW_TYPE_L=0;
    private  static int VIEW_TYPE_R=1;

    private LayoutInflater mInflater;



    private List<HomeCampaign> mDatas;

    private  Context mContext;


    private  OnCampaignClickListener mListener;


    public HomeCatgoryAdapter(List<HomeCampaign> datas,Context context){
        mDatas = datas;
        this.mContext = context;
    }



    public void setOnCampaignClickListener(OnCampaignClickListener listener){

        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {


        mInflater = LayoutInflater.from(viewGroup.getContext());
        if(type == VIEW_TYPE_R){

            return  new ViewHolder(mInflater.inflate(R.layout.template_home_cardview2,viewGroup,false));
        }

        return  new ViewHolder(mInflater.inflate(R.layout.template_home_cardview,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {


        HomeCampaign homeCampaign = mDatas.get(i);
        viewHolder.textTitle.setText(homeCampaign.getTitle());

        Picasso.with(mContext).load(homeCampaign.getCpOne().getImgUrl()).into(viewHolder.imageViewBig);
        Picasso.with(mContext).load(homeCampaign.getCpTwo().getImgUrl()).into(viewHolder.imageViewSmallTop);
        Picasso.with(mContext).load(homeCampaign.getCpThree().getImgUrl()).into(viewHolder.imageViewSmallBottom);

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }


    @Override
    public int getItemViewType(int position) {

        if(position % 2==0){
            return  VIEW_TYPE_R;
        }
        else return VIEW_TYPE_L;


    }

      class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        TextView textTitle;
        ImageView imageViewBig;
        ImageView imageViewSmallTop;
        ImageView imageViewSmallBottom;

        public ViewHolder(View itemView) {
            super(itemView);


            textTitle = (TextView) itemView.findViewById(R.id.text_title);
            imageViewBig = (ImageView) itemView.findViewById(R.id.imgview_big);
            imageViewSmallTop = (ImageView) itemView.findViewById(R.id.imgview_small_top);
            imageViewSmallBottom = (ImageView) itemView.findViewById(R.id.imgview_small_bottom);


            imageViewBig.setOnClickListener(this);
            imageViewSmallTop.setOnClickListener(this);
            imageViewSmallBottom.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {


            if(mListener !=null){

                anim(v);

            }


        }

          private  void anim(final View v){

              ObjectAnimator animator =  ObjectAnimator.ofFloat(v, "rotationX", 0.0F, 360.0F)
                      .setDuration(200);
              animator.addListener(new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationEnd(Animator animation) {

                      HomeCampaign campaign = mDatas.get(getLayoutPosition());

                      switch (v.getId()){

                          case  R.id.imgview_big:
                              mListener.onClick(v, campaign.getCpOne());
                              break;

                          case  R.id.imgview_small_top:
                              mListener.onClick(v, campaign.getCpTwo());
                              break;

                          case R.id.imgview_small_bottom:
                              mListener.onClick(v,campaign.getCpThree());
                              break;

                      }

                  }
              });
              animator.start();
          }
    }


    public  interface OnCampaignClickListener{


        void onClick(View view,Campaign campaign);

    }

}
