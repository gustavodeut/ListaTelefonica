package Uteis;

/**
 * Created by gustavo on 8/24/2014.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.schmidt.gustavo.listatelefonica.R;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    List<Bitmap> listImage;
    Context context;
    LayoutInflater inflater;

    public ViewPagerAdapter(Context context, List<Bitmap> listImage) {
        this.context = context;
        this.listImage = listImage;
    }

    @Override
    public int getCount() {
        return listImage.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        ImageView imgAnuncio;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpage_image, container, false);

        imgAnuncio = (ImageView) itemView.findViewById(R.id.imageViewAnuncio);
        imgAnuncio.setImageBitmap(listImage.get(position));

        imgAnuncio.setMinimumHeight(800);



        //imgAnuncio.setScaleType(ImageView.ScaleType.FIT_XY);

        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}