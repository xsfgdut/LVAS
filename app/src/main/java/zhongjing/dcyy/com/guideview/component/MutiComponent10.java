package zhongjing.dcyy.com.guideview.component;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import zhongjing.dcyy.com.R;
import zhongjing.dcyy.com.guideview.Component;

/**
 * Created by binIoter on 16/6/17.
 */
public class MutiComponent10 implements Component {

    OnGuideNextListener listener;

    public void setOnGuideNextListener(OnGuideNextListener listener) {
        this.listener = listener;
    }

    public interface OnGuideNextListener {
        void next();
    }

    @Override
    public View getView(LayoutInflater inflater) {
        LinearLayout ll = new LinearLayout(inflater.getContext());
        LinearLayout.LayoutParams param =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(param);

        ImageView imageView = new ImageView(inflater.getContext());
        imageView.setImageResource(R.mipmap.next);
        ll.removeAllViews();

        ll.addView(imageView);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.next();
            }
        });
        return ll;
    }

    @Override
    public int getAnchor() {
        return Component.ANCHOR_TOP;
    }

    @Override
    public int getFitPosition() {
        return Component.FIT_CENTER;
    }

    @Override
    public int getXOffset() {
        return 0;
    }

    @Override
    public int getYOffset() {
        return 20;
    }
}
