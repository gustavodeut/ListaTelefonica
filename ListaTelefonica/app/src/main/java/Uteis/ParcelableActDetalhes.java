package Uteis;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

/**
 * Created by gustavo on 9/7/2014.
 */
public class ParcelableActDetalhes implements Parcelable {

    public ViewPagerAdapter adpViewPager;
    public ImageView imgTopo;

    public ParcelableActDetalhes(ViewPagerAdapter adpViewPager, ImageView imgTopo){
        this.adpViewPager = adpViewPager;
        this.imgTopo = imgTopo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
