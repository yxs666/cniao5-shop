package cniao5.com.cniao5shop.adapter;

import android.content.Context;

import java.util.List;

import cniao5.com.cniao5shop.R;
import cniao5.com.cniao5shop.bean.Category;

/**
 * Created by <a href="http://www.cniao5.com">菜鸟窝</a>
 * 一个专业的Android开发在线教育平台
 */
public class CategoryAdapter extends SimpleAdapter<Category> {


    public CategoryAdapter(Context context, List<Category> datas) {
        super(context, R.layout.template_single_text, datas);
    }

    @Override
    protected void convert(BaseViewHolder viewHoder, Category item) {


        viewHoder.getTextView(R.id.textView).setText(item.getName());

    }
}
