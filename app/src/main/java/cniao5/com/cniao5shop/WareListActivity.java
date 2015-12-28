package cniao5.com.cniao5shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cjj.MaterialRefreshLayout;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.List;

import cniao5.com.cniao5shop.adapter.BaseAdapter;
import cniao5.com.cniao5shop.adapter.HWAdatper;
import cniao5.com.cniao5shop.adapter.decoration.DividerItemDecoration;
import cniao5.com.cniao5shop.bean.Page;
import cniao5.com.cniao5shop.bean.Wares;
import cniao5.com.cniao5shop.utils.Pager;
import cniao5.com.cniao5shop.widget.CNiaoToolBar;


/**
 * Created by <a href="http://www.cniao5.com">菜鸟窝</a>
 * 一个专业的Android开发在线教育平台
 */
public class WareListActivity extends AppCompatActivity  implements Pager.OnPageListener<Wares>,TabLayout.OnTabSelectedListener,View.OnClickListener {

    private static final String TAG = "WareListActivity";

    public static final int TAG_DEFAULT=0;
    public static final int TAG_SALE=1;
    public static final int TAG_PRICE=2;

    public static final int ACTION_LIST=1;
    public static final int ACTION_GIRD=2;




    @ViewInject(R.id.tab_layout)
    private TabLayout mTablayout;

    @ViewInject(R.id.txt_summary)
    private TextView mTxtSummary;


    @ViewInject(R.id.recycler_view)
    private RecyclerView mRecyclerview_wares;

    @ViewInject(R.id.refresh_layout)
    private MaterialRefreshLayout mRefreshLayout;

    @ViewInject(R.id.toolbar)
    private CNiaoToolBar mToolbar;


    private int orderBy = 0;
    private long campaignId = 0;


    private HWAdatper mWaresAdapter;


    private   Pager pager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_warelist);
        ViewUtils.inject(this);

        initToolBar();

        campaignId=getIntent().getLongExtra(Contants.COMPAINGAIN_ID,0);

        initTab();

        getData();


    }


    private void initToolBar(){

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WareListActivity.this.finish();
            }
        });


        mToolbar.setRightButtonIcon(R.drawable.icon_grid_32);
        mToolbar.getRightButton().setTag(ACTION_LIST);


        mToolbar.setRightButtonOnClickListener(this);


    }


    private void getData(){


       pager= Pager.newBuilder().setUrl(Contants.API.WARES_CAMPAIN_LIST)
                .putParam("campaignId",campaignId)
                .putParam("orderBy",orderBy)
                .setRefreshLayout(mRefreshLayout)
                .setLoadMore(true)
                .setOnPageListener(this)
                .build(this,new TypeToken<Page<Wares>>(){}.getType());

        pager.request();

    }



    private void initTab(){


       TabLayout.Tab tab= mTablayout.newTab();
        tab.setText("默认");
        tab.setTag(TAG_DEFAULT);

        mTablayout.addTab(tab);



        tab= mTablayout.newTab();
        tab.setText("价格");
        tab.setTag(TAG_PRICE);

        mTablayout.addTab(tab);

        tab= mTablayout.newTab();
        tab.setText("销量");
        tab.setTag(TAG_SALE);

        mTablayout.addTab(tab);


        mTablayout.setOnTabSelectedListener(this);


    }


    @Override
    public void load(List<Wares> datas, int totalPage, int totalCount) {

        mTxtSummary.setText("共有"+totalCount+"件商品");

        if (mWaresAdapter == null) {
            mWaresAdapter = new HWAdatper(this, datas);
            mWaresAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Wares wares = mWaresAdapter.getItem(position);

                    Intent intent = new Intent(WareListActivity.this, WareDetailActivity.class);

                    intent.putExtra(Contants.WARE,wares);
                    startActivity(intent);
                }
            });
            mRecyclerview_wares.setAdapter(mWaresAdapter);
            mRecyclerview_wares.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerview_wares.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));
            mRecyclerview_wares.setItemAnimator(new DefaultItemAnimator());
        } else {
            mWaresAdapter.refreshData(datas);
        }

    }

    @Override
    public void refresh(List<Wares> datas, int totalPage, int totalCount) {

        mWaresAdapter.refreshData(datas);
        mRecyclerview_wares.scrollToPosition(0);
    }

    @Override
    public void loadMore(List<Wares> datas, int totalPage, int totalCount) {
        mWaresAdapter.loadMoreData(datas);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        orderBy = (int) tab.getTag();
        pager.putParam("orderBy",orderBy);
        pager.request();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View v) {

        int action = (int) v.getTag();

        if(ACTION_LIST == action){

            mToolbar.setRightButtonIcon(R.drawable.icon_list_32);
            mToolbar.getRightButton().setTag(ACTION_GIRD);

            mWaresAdapter.resetLayout(R.layout.template_grid_wares);


           mRecyclerview_wares.setLayoutManager(new GridLayoutManager(this,2));

        }
        else if(ACTION_GIRD == action){


            mToolbar.setRightButtonIcon(R.drawable.icon_grid_32);
            mToolbar.getRightButton().setTag(ACTION_LIST);

            mWaresAdapter.resetLayout(R.layout.template_hot_wares);

            mRecyclerview_wares.setLayoutManager(new LinearLayoutManager(this));
        }

    }
}
