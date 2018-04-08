package zhongjing.dcyy.com.guideview.component;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import zhongjing.dcyy.com.R;
import zhongjing.dcyy.com.guideview.Component;

/**
 * Created by binIoter on 16/6/17.
 */
public class MutiComponent8 implements Component {

  @Override public View getView(LayoutInflater inflater) {
    LinearLayout ll = new LinearLayout(inflater.getContext());
    LinearLayout.LayoutParams param =
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setLayoutParams(param);

    ImageView imageView = new ImageView(inflater.getContext());
    imageView.setImageResource(R.mipmap.volume_down);
    ll.removeAllViews();

    ll.addView(imageView);
    ll.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        Toast.makeText(view.getContext(), "引导层被点击了", Toast.LENGTH_SHORT).show();
      }
    });
    return ll;
  }

  @Override public int getAnchor() {
    return Component.ANCHOR_TOP;
  }

  @Override public int getFitPosition() {
    return Component.FIT_CENTER;
  }

  @Override public int getXOffset() {
    return -3;
  }

  @Override public int getYOffset() {
    return 55;
  }
}
