package cniao5.com.cniao5shop.adapter.decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class CardViewtemDecortion extends RecyclerView.ItemDecoration {

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int layoutOrientation = getOrientation(parent);
        if (layoutOrientation == LinearLayoutManager.VERTICAL) {
            outRect.top = 10;
            outRect.left=5;
            outRect.right=5;
        } else if(layoutOrientation == LinearLayoutManager.HORIZONTAL) {
            outRect.left = 5;
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            return layoutManager.getOrientation();
        } else throw new IllegalStateException("DividerItemDecoration can only be used with a LinearLayoutManager.");
    }
}
