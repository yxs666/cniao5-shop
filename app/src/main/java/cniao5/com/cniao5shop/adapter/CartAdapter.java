package cniao5.com.cniao5shop.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Iterator;
import java.util.List;

import cniao5.com.cniao5shop.R;
import cniao5.com.cniao5shop.bean.ShoppingCart;
import cniao5.com.cniao5shop.utils.CartProvider;
import cniao5.com.cniao5shop.widget.NumberAddSubView;


/**
 * Created by <a href="http://www.cniao5.com">菜鸟窝</a>
 * 一个专业的Android开发在线教育平台
 */
public class CartAdapter extends SimpleAdapter<ShoppingCart> implements BaseAdapter.OnItemClickListener {


    public static final String TAG="CartAdapter";





    private CheckBox checkBox;
    private TextView textView;

    private CartProvider cartProvider;


    public CartAdapter(Context context, List<ShoppingCart> datas, final CheckBox checkBox,TextView tv) {
        super(context, R.layout.template_cart, datas);

        setCheckBox(checkBox);
        setTextView(tv);

        cartProvider = new CartProvider(context);

        setOnItemClickListener(this);

        showTotalPrice();


    }


    @Override
    protected void convert(BaseViewHolder viewHoder, final ShoppingCart item) {

        viewHoder.getTextView(R.id.text_title).setText(item.getName());
        viewHoder.getTextView(R.id.text_price).setText("￥"+item.getPrice());
        SimpleDraweeView draweeView = (SimpleDraweeView) viewHoder.getView(R.id.drawee_view);
        draweeView.setImageURI(Uri.parse(item.getImgUrl()));

        CheckBox checkBox = (CheckBox) viewHoder.getView(R.id.checkbox);
        checkBox.setChecked(item.isChecked());


        NumberAddSubView numberAddSubView = (NumberAddSubView) viewHoder.getView(R.id.num_control);

        numberAddSubView.setValue(item.getCount());

        numberAddSubView.setOnButtonClickListener(new NumberAddSubView.OnButtonClickListener() {
            @Override
            public void onButtonAddClick(View view, int value) {

                item.setCount(value);
                cartProvider.update(item);
                showTotalPrice();


            }

            @Override
            public void onButtonSubClick(View view, int value) {

                item.setCount(value);
                cartProvider.update(item);
                showTotalPrice();
            }
        });


    }


    private  float getTotalPrice(){

        float sum=0;
        if(!isNull())
            return sum;

        for (ShoppingCart cart:
                datas) {
            if(cart.isChecked())
                sum += cart.getCount()*cart.getPrice();
        }

        return sum;
    }



    public void showTotalPrice(){

        float total = getTotalPrice();

        textView.setText(Html.fromHtml("合计 ￥<span style='color:#eb4f38'>" + total + "</span>"), TextView.BufferType.SPANNABLE);
    }



    private boolean isNull(){

        return (datas !=null && datas.size()>0);
    }


    @Override
    public void onItemClick(View view, int position) {

       ShoppingCart cart =  getItem(position);
        cart.setIsChecked(!cart.isChecked());
        notifyItemChanged(position);

        checkListen();
        showTotalPrice();

    }


    private void checkListen() {


        int count = 0;
        int checkNum = 0;
        if (datas != null) {
            count = datas.size();

            for (ShoppingCart cart : datas) {
                if (!cart.isChecked()) {
                    checkBox.setChecked(false);
                    break;
                } else {
                    checkNum = checkNum + 1;
                }
            }

            if (count == checkNum) {
                checkBox.setChecked(true);
            }

        }
    }


    public void checkAll_None(boolean isChecked){


        if(!isNull())
            return ;


        int i=0;
        for (ShoppingCart cart :datas){
            cart.setIsChecked(isChecked);
            notifyItemChanged(i);
            i++;
        }


    }



    public void delCart(){


        if(!isNull())
            return ;

//        for (ShoppingCart cart : datas){
//
//            if(cart.isChecked()){
//                int position = datas.indexOf(cart);
//                cartProvider.delete(cart);
//                datas.remove(cart);
//                notifyItemRemoved(position);
//            }
//        }


        for(Iterator iterator = datas.iterator();iterator.hasNext();){

            ShoppingCart cart = (ShoppingCart) iterator.next();
            if(cart.isChecked()){
                int position = datas.indexOf(cart);
                cartProvider.delete(cart);
                iterator.remove();
                notifyItemRemoved(position);
            }

        }




    }





    public void setTextView(TextView textview){
        this.textView = textview;
    }

    public void setCheckBox(CheckBox ck){
        this.checkBox = ck;

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkAll_None(checkBox.isChecked());
                showTotalPrice();

            }
        });
    }




}
