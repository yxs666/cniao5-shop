package cniao5.com.cniao5shop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.gson.Gson;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.List;

import cniao5.com.cniao5shop.Contants;
import cniao5.com.cniao5shop.R;
import cniao5.com.cniao5shop.WareListActivity;
import cniao5.com.cniao5shop.adapter.HomeCatgoryAdapter;
import cniao5.com.cniao5shop.adapter.decoration.CardViewtemDecortion;
import cniao5.com.cniao5shop.bean.Banner;
import cniao5.com.cniao5shop.bean.Campaign;
import cniao5.com.cniao5shop.bean.HomeCampaign;
import cniao5.com.cniao5shop.http.BaseCallback;
import cniao5.com.cniao5shop.http.OkHttpHelper;
import cniao5.com.cniao5shop.http.SpotsCallBack;


public class HomeFragment extends BaseFragment {


    @ViewInject(R.id.slider)
    private SliderLayout mSliderLayout;


    @ViewInject(R.id.recyclerview)
    private RecyclerView mRecyclerView;

    private HomeCatgoryAdapter mAdatper;


    private static  final  String TAG="HomeFragment";


    private Gson mGson = new Gson();

    private List<Banner> mBanner;



    private OkHttpHelper httpHelper = OkHttpHelper.getInstance();



    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return    inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void init() {

        requestImages();

        initRecyclerView();
    }


    private  void requestImages(){

        String url ="http://112.124.22.238:8081/course_api/banner/query?type=1";



        httpHelper.get(url, new SpotsCallBack<List<Banner>>(getContext()){


            @Override
            public void onSuccess(Response response, List<Banner> banners) {

                mBanner = banners;
                initSlider();
            }

            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });



    }


    private void initRecyclerView() {


        httpHelper.get(Contants.API.CAMPAIGN_HOME, new BaseCallback<List<HomeCampaign>>() {
            @Override
            public void onBeforeRequest(Request request) {

            }

            @Override
            public void onFailure(Request request, Exception e) {

            }

            @Override
            public void onResponse(Response response) {

            }

            @Override
            public void onSuccess(Response response, List<HomeCampaign> homeCampaigns) {

                initData(homeCampaigns);
            }


            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });

    }


    private  void initData(List<HomeCampaign> homeCampaigns){


        mAdatper = new HomeCatgoryAdapter(homeCampaigns,getActivity());

        mAdatper.setOnCampaignClickListener(new HomeCatgoryAdapter.OnCampaignClickListener() {
            @Override
            public void onClick(View view, Campaign campaign) {


                Intent intent = new Intent(getActivity(), WareListActivity.class);
                intent.putExtra(Contants.COMPAINGAIN_ID,campaign.getId());

                startActivity(intent);


            }
        });

        mRecyclerView.setAdapter(mAdatper);

        mRecyclerView.addItemDecoration(new CardViewtemDecortion());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
    }




    private void initSlider(){




        if(mBanner !=null){

            for (Banner banner : mBanner){


                TextSliderView textSliderView = new TextSliderView(this.getActivity());
                textSliderView.image(banner.getImgUrl());
                textSliderView.description(banner.getName());
                textSliderView.setScaleType(BaseSliderView.ScaleType.Fit);
                mSliderLayout.addSlider(textSliderView);

            }
        }



        mSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);

        mSliderLayout.setCustomAnimation(new DescriptionAnimation());
        mSliderLayout.setPresetTransformer(SliderLayout.Transformer.RotateUp);
        mSliderLayout.setDuration(3000);




    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSliderLayout.stopAutoCycle();
    }
}
