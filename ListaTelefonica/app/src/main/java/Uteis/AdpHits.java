package Uteis;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schmidt.gustavo.listatelefonica.ActDetalhes;
import com.schmidt.gustavo.listatelefonica.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

import Objetos.Book.Fields;
import Objetos.Book.Hit;

/**
 * Created by Gustavo on 8/5/2014.
 */
public class AdpHits extends BaseAdapter {

    private List<Hit> hitList;
    private Context context;

    private LruCache<String, Bitmap> mMemoryCache;
    private Bitmap mPlaceHolderBitmap;
    public ImageButton m_imgbActions;

    public AdpHits(List<Hit> hitList, Context context) {
        this.hitList = hitList;
        this.context = context;

        mPlaceHolderBitmap = ImageCommons.decodeSampledBitmapFromResource(context.getResources(), R.drawable.ic_launcher, 70, 70);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount() / 1024;
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
            }
        };
    }

    public List<Hit> getHitList(){
        return hitList;
    }

    @Override
    public int getCount() {
        return hitList.size();
    }

    @Override
    public Object getItem(int position) {
        return hitList.get(position);
    }

    @Override
    public long getItemId(int position) {
        Hit hit = (Hit) hitList.get(position);
        return Long.parseLong(hit.id);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listViewItem = convertView;

        if(listViewItem == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listViewItem = inflater.inflate(R.layout.listview_hits, null);
        }

        Hit hit = hitList.get(position);

        ((TextView)listViewItem.findViewById(R.id.tvTitulo)).setText(hit.fields.cli_nome);

        final TextView tvCategoria = (TextView)listViewItem.findViewById(R.id.tvCategoria);
        TextView tvLabelCategoria = (TextView)listViewItem.findViewById(R.id.tvLabelCategoria);
        if (hit.fields.faceta_descricao == null || hit.fields.faceta_descricao.isEmpty()) {
            tvCategoria.setVisibility(View.GONE);
            tvLabelCategoria.setVisibility(View.GONE);
        }
        else {
            tvCategoria.setVisibility(View.VISIBLE);
            tvLabelCategoria.setVisibility(View.VISIBLE);
            tvCategoria.setText(hit.fields.faceta_descricao);
            tvCategoria.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Sistema.consultaBook.Consultar(tvCategoria.getText().toString());
                }
            });
        }

        ((TextView)listViewItem.findViewById(R.id.tvEndereco)).setText(hit.fields.end_completo + ", " + hit.fields.end_numero);

        ((TextView)listViewItem.findViewById(R.id.tvBairroCep)).setText(hit.fields.end_bairro + " - CEP: " + hit.fields.end_cep);

        ((TextView)listViewItem.findViewById(R.id.tvCidadeEstado)).setText(hit.fields.cid_descricao + " - " + hit.fields.cid_uf);

        setTelefone((TextView)listViewItem.findViewById(R.id.tvTelefone1), hit.fields.cli_telefone);

        if (hit.fields.cli_telefone_1 != null && hit.fields.cli_telefone != hit.fields.cli_telefone_1)
            setTelefone((TextView)listViewItem.findViewById(R.id.tvTelefone2), hit.fields.cli_telefone_1);
        else
            ((TextView)listViewItem.findViewById(R.id.tvTelefone2)).setVisibility(View.GONE);

        if (hit.fields.cli_telefone_2 != null)
            setTelefone((TextView)listViewItem.findViewById(R.id.tvTelefone3), hit.fields.cli_telefone_2);
        else
            ((TextView)listViewItem.findViewById(R.id.tvTelefone3)).setVisibility(View.GONE);

        ImageView logo = (ImageView) listViewItem.findViewById(R.id.imgViewLogo);

        if (hit.fields.ianuncio.equals("1") && hit.fields.anu_logo != null && !hit.fields.anu_logo.isEmpty()) {
            loadBitmap(Sistema.UrlLogo + hit.fields.anu_logo, logo);
            logo.setVisibility(View.VISIBLE);
            logo.setTag(position);

            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ImageView logo = (ImageView) v.findViewById(R.id.imgViewLogo);
                    /*logo.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    logo.playSoundEffect(SoundEffectConstants.CLICK);*/

                    int position = Integer.parseInt(logo.getTag().toString());

                    Intent i = new Intent(Sistema.activityActMain, ActDetalhes.class);

                    Bundle b = new Bundle();
                    b.putInt("position", position);
                    i.putExtras(b);

                    Sistema.activityActMain.startActivity(i);
                }
            });
        }
        else
            logo.setVisibility(View.GONE);


        final ImageButton imgbActions = (ImageButton) listViewItem.findViewById(R.id.imgbActions);
        ImageButton imgNavegar = (ImageButton) listViewItem.findViewById(R.id.imgNavegar);
        ImageButton imgMapa = (ImageButton) listViewItem.findViewById(R.id.imgMapa);
        ImageButton imgFacebook = (ImageButton) listViewItem.findViewById(R.id.imgFacebook);
        ImageButton imgEmail = (ImageButton) listViewItem.findViewById(R.id.imgEmail);

        imgbActions.setVisibility(View.VISIBLE);
        imgbActions.setTag(position);
        imgNavegar.setTag(position);
        imgMapa.setTag(position);
        imgFacebook.setTag(position);
        imgEmail.setTag(position);

        imgbActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = Integer.parseInt(v.getTag().toString());
                Fields fields = ((Hit) Sistema.consultaBook.adpHits.getItem(pos)).fields;

                String telefone = fields.cli_nome
                        + ": " + fields.cli_telefone;

                Sistema.Compartilhar(telefone, Sistema.activityActMain);
            }
        });

        imgNavegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double latitude = -22.121364;
                Double longitude = -51.385896;
                int pos = Integer.parseInt(v.getTag().toString());

                Fields fields = ((Hit) Sistema.consultaBook.adpHits.getItem(pos)).fields;

                if (fields.getLatitude() == 0 && fields.getLongitude() == 0) {
                    //Sistema.ExibeMensagem("Não há localização.");
                    String uri = "geo:0,0?q=" + fields.faceta_endereco + ", " + fields.end_numero + ", " + fields.cid_descricao;
                    Sistema.AbrirRotaGPS(Sistema.activityActMain, uri);
                } else {
                    latitude = fields.getLatitude();
                    longitude = fields.getLongitude();
                    Sistema.AbrirRotaGPS(latitude, longitude, Sistema.activityActMain);
                }
            }
        });

        imgMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Double latitude = -22.121364;
                    Double longitude = -51.385896;
                    int pos = Integer.parseInt(v.getTag().toString());
                    Fields fields = ((Hit) Sistema.consultaBook.adpHits.getItem(pos)).fields;

                    if (fields.getLatitude() == 0 && fields.getLongitude() == 0) {
                        //Sistema.ExibeMensagem("Não há localização.");
                        String uri = "geo:0,0?q=" + fields.faceta_endereco + ", " + fields.end_numero + ", " + fields.cid_descricao;
                        Sistema.AbrirMapaLocal(Sistema.activityActMain, uri);
                    } else {
                        latitude = fields.getLatitude();
                        longitude = fields.getLongitude();
                        Sistema.AbrirMapaLocal(latitude, longitude, Sistema.activityActMain);
                    }
                }
                catch(NullPointerException e){
                    e.printStackTrace();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        LinearLayout llItemListView = (LinearLayout) listViewItem.findViewById(R.id.llItemListView);

        if (!hit.fields.ianuncio.equals("1")) {
            llItemListView.setBackgroundResource(R.drawable.custom_border_simples);
            imgFacebook.setVisibility(View.GONE);
            imgEmail.setVisibility(View.GONE);
        }
        else {
            LinearLayout llButonsActionItemList = (LinearLayout) listViewItem.findViewById(R.id.llButonsActionItemList);
            llItemListView.setBackgroundResource(R.drawable.custom_border_anunciante);

            if (hit.fields.anu_url_facebook == null || hit.fields.anu_url_facebook.isEmpty())
                imgFacebook.setVisibility(View.GONE);
            else
            {
                imgFacebook.setVisibility(View.VISIBLE);
                imgFacebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            int pos = Integer.parseInt(v.getTag().toString());
                            Fields fields = ((Hit) Sistema.consultaBook.adpHits.getItem(pos)).fields;

                            if (fields.anu_url_facebook != null && !fields.anu_url_facebook.isEmpty()) {
                                Sistema.AbrirFacebook(fields.anu_url_facebook, Sistema.activityActMain);
                            } else {
                                Sistema.ExibeMensagem("Não há facebook.");
                            }
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }

            if (hit.fields.anu_email == null || hit.fields.anu_email.isEmpty())
                imgEmail.setVisibility(View.GONE);
            else
            {
                imgEmail.setVisibility(View.VISIBLE);
                imgEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                       try {
                           int pos = Integer.parseInt(v.getTag().toString());
                           Fields fields = ((Hit) Sistema.consultaBook.adpHits.getItem(pos)).fields;

                           if (fields.anu_email != null && !fields.anu_email.isEmpty()) {
                               Sistema.EnviarEmail(fields.anu_email, Sistema.activityActMain);
                           } else {
                               Sistema.ExibeMensagem("Não há email.");
                           }
                       }
                       catch(Exception e){
                           e.printStackTrace();
                       }
                    }
                });
            }
        }

        return listViewItem;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }

    private void setTelefone(TextView tvTelefone, String numero){
        tvTelefone.setVisibility(View.VISIBLE);
        tvTelefone.setText(numero);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(String imageUrl, ImageView imageView) {
        final Bitmap bitmap = getBitmapFromMemCache(imageUrl);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (cancelPotentialWork(imageUrl, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(imageUrl);
            }
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        //        private int resId = 0;
        private String imageUrl;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];
            ImageView thumbnail = imageViewReference.get();
            try {
                InputStream is = (InputStream) new URL(imageUrl).getContent();
                Bitmap bitmap = ImageCommons.decodeSampledBitmapFromInputStream(is, thumbnail.getWidth(), thumbnail.getHeight());
                addBitmapToMemoryCache(imageUrl, bitmap);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }

            Log.d("myLog", getClass().getSimpleName() + " Erro ao fazer download de imagem");
            return ImageCommons.decodeSampledBitmapFromResource(context.getResources(), R.drawable.ic_launcher, 70, 70);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String imageUrl, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null && bitmapWorkerTask.imageUrl != null) {
            final String bitmapWorkerTaskImageUrl = bitmapWorkerTask.imageUrl;
            if (!bitmapWorkerTaskImageUrl.equals(imageUrl)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public void setButtonActionItemList(ImageButton img){
        if (Sistema.consultaBook.adpHits.m_imgbActions != null) {
            LinearLayout llActionItemList = (LinearLayout) Sistema.consultaBook.adpHits.m_imgbActions.getParent();
            LinearLayout llButonsActionItemList = (LinearLayout) llActionItemList.findViewById(R.id.llButonsActionItemList);
            llButonsActionItemList.setVisibility(View.GONE);
            m_imgbActions.setVisibility(View.VISIBLE);
        }
        if (img != null)
            img.setVisibility(View.GONE);

        Sistema.consultaBook.adpHits.m_imgbActions = img;
    }
}
