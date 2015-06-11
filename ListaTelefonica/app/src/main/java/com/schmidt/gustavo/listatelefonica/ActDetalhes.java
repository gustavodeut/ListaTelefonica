package com.schmidt.gustavo.listatelefonica;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.schmidt.gustavo.listatelefonica.R;

import java.util.ArrayList;
import java.util.List;

import Objetos.Book.Fields;
import Objetos.Book.Hit;
import Uteis.AbsUtil;
import Uteis.AsyncDownloadArrayImagem;
import Uteis.AsyncDownloadImagem;
import Uteis.ParcelableActDetalhes;
import Uteis.Sistema;
import Uteis.ViewPagerAdapter;

public class ActDetalhes extends Activity implements ViewTreeObserver.OnScrollChangedListener {

    private ViewPager viewPager;
    private ViewPagerAdapter adpViewPager;

    private ActionBar mActionBar;
    private float oldScrollY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_act_detalhes);

        mActionBar = getActionBar();

        ((ScrollView)findViewById(R.id.scrollView)).getViewTreeObserver().addOnScrollChangedListener(this);

        setTitle(Sistema.consultaBook.palavrasConsultada.get(Sistema.consultaBook.palavrasConsultada.size() - 1));

        CarregarDetalhes(getIntent().getExtras().getInt("position"), savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act_detalhes, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.search_detalhes).getActionView();

        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Pesquisar");
        searchView.setIconifiedByDefault(false);
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                return true;
            }

            public boolean onQueryTextSubmit(String query) {

                Sistema.consultaBook.Consultar(query);

                getThis().finish();

                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_rota:{
                Double latitude = -22.121364;
                Double longitude = -51.385896;
                int pos = getIntent().getExtras().getInt("position");
                Fields fields =((Hit) Sistema.consultaBook.adpHits.getItem(pos)).fields;

                if(fields.getLatitude() == 0 && fields.getLongitude() == 0) {
                    Sistema.ExibeMensagem("Não há localização.");
                }
                else {
                    latitude = fields.getLatitude();
                    longitude = fields.getLongitude();
                    Sistema.AbrirRotaGPS(latitude, longitude, this);
                }
                break;
            }
            case R.id.action_mapa: {
                Double latitude = -22.121364;
                Double longitude = -51.385896;
                int pos = getIntent().getExtras().getInt("position");
                Fields fields =((Hit) Sistema.consultaBook.adpHits.getItem(pos)).fields;

                if (fields.getLatitude() == 0 && fields.getLongitude() == 0) {
                    Sistema.ExibeMensagem("Não há localização.");
                } else {
                    latitude = fields.getLatitude();
                    longitude = fields.getLongitude();
                    Sistema.AbrirMapaLocal(latitude, longitude, this);
                }
                break;
            }
            case R.id.action_facebook:{
                int pos = getIntent().getExtras().getInt("position");
                Fields fields =((Hit) Sistema.consultaBook.adpHits.getItem(pos)).fields;

                if (fields.anu_url_facebook != null && !fields.anu_url_facebook.isEmpty() ) {
                    Sistema.AbrirFacebook(fields.anu_url_facebook, this);
                }
                else{
                    Sistema.ExibeMensagem("Não há facebook.");
                }
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        ImageView imgTopo = (ImageView) findViewById(R.id.imgTopo);

        ParcelableActDetalhes parcelableActDetalhes = new ParcelableActDetalhes(adpViewPager, imgTopo);

        outState.putParcelable("parcelableActDetalhes", parcelableActDetalhes);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onScrollChanged() {
        //http://www.techrepublic.com/article/pro-tip-maximize-android-screen-real-estate-by-showing-and-hiding-the-action-bar/

        ScrollView scrollView = ((ScrollView)findViewById(R.id.scrollView));
        float y = scrollView.getScrollY();

        if (y <= 0) {
            if (!mActionBar.isShowing())
                mActionBar.show();
        }
        else if (y >= scrollView.getHeight()) {
            if (mActionBar.isShowing())
                mActionBar.hide();
        }
        else if (y > oldScrollY)
        {
            if(mActionBar.isShowing())
                mActionBar.hide();
        }
        else
        if (y < oldScrollY && !mActionBar.isShowing())
            mActionBar.show();
        oldScrollY = y;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void CarregarDetalhes(int postion, Bundle savedInstanceState){

        try {

            final Fields fields = Sistema.consultaBook.adpHits.getHitList().get(postion).fields;

            ImageView imgTopo = (ImageView) findViewById(R.id.imgTopo);
            final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                    new int[]{android.R.attr.actionBarSize});

            int mActionBarHeight = (int) styledAttributes.getDimension(0, 0);
            imgTopo.setPadding(0, mActionBarHeight, 0, 0);
            imgTopo.setScaleType(ImageView.ScaleType.FIT_END);

            if (savedInstanceState != null){

                ParcelableActDetalhes parcelableActDetalhes = savedInstanceState.getParcelable("parcelableActDetalhes");
                adpViewPager = parcelableActDetalhes.adpViewPager;
                viewPager = (ViewPager) findViewById(R.id.pager);
                viewPager.setAdapter(adpViewPager);

                Bitmap bitmap = ((BitmapDrawable)parcelableActDetalhes.imgTopo.getDrawable()).getBitmap();
                imgTopo.setImageBitmap(bitmap);
                imgTopo.setVisibility(View.VISIBLE);
            }
            else {

                if (fields.strtopo != null && fields.strtopo != "") {
                    new AsyncDownloadImagem(fields.strtopo.toString(), imgTopo);
                }

                if (fields.anu_imagens != null && fields.anu_imagens.size() > 0) {
                    ViewPager vp = (ViewPager) findViewById(R.id.pager);

                    final List<Bitmap> listBitmap = new ArrayList<Bitmap>();

                    AbsUtil absUtil = new AbsUtil() {
                        @TargetApi(Build.VERSION_CODES.KITKAT)
                        @Override
                        public void Executa(Object obj) {
                            viewPager = (ViewPager) findViewById(R.id.pager);
                            adpViewPager = new ViewPagerAdapter(getThis(), listBitmap);
                            viewPager.setAdapter(adpViewPager);
                        }
                    };

                    for (Object urlImagem : fields.anu_imagens) {
                        new AsyncDownloadArrayImagem(urlImagem.toString(), listBitmap, absUtil, fields.anu_imagens.size());
                    }
                } else
                    ((FrameLayout) findViewById(R.id.frameLayoutImagensAnuncio)).setVisibility(View.GONE);
            }
            WebView webView = (WebView) findViewById(R.id.webView);

            if (fields.anu_conteudo != null && !fields.anu_conteudo.isEmpty()) {

                webView.loadData(fields.anu_conteudo.toString(), "text/html", "UTF-8");
            }
            else
                if (fields.anu_site != null && !fields.anu_site.isEmpty())
                    webView.loadUrl(fields.anu_site);
                else
                    webView.setVisibility(View.GONE);

            //Detalhes-------------------------------------------------------------

            ((TextView) findViewById(R.id.tvTitulo)).setText(fields.cli_nome);

            final TextView tvCategoria = (TextView) findViewById(R.id.tvCategoria);
            TextView tvLabelCategoria = (TextView) findViewById(R.id.tvLabelCategoria);
            if (fields.faceta_descricao == null || fields.faceta_descricao.isEmpty()) {
                tvCategoria.setVisibility(View.GONE);
                tvLabelCategoria.setVisibility(View.GONE);
            }
            else {
                tvCategoria.setVisibility(View.VISIBLE);
                tvLabelCategoria.setVisibility(View.VISIBLE);
                tvCategoria.setText(fields.faceta_descricao);
                tvCategoria.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                        if (!Sistema.consultaBook.palavrasConsultada.equals(tvCategoria.getText().toString()))
                            Sistema.consultaBook.Consultar(tvCategoria.getText().toString());
                        onBackPressed();
                    }
                });
            }

            ((TextView) findViewById(R.id.tvEndereco)).setText( fields.end_completo + ", " + fields.end_numero);

            ((TextView) findViewById(R.id.tvBairroCep)).setText(fields.end_bairro + " - CEP: " + fields.end_cep);

            ((TextView) findViewById(R.id.tvCidadeEstado)).setText(fields.cid_descricao + " - " + fields.cid_uf);

            if (fields.anu_site != null && fields.anu_site != "")
                ((TextView) findViewById(R.id.tvSite)).setText(fields.anu_site);
            else
                ((TextView) findViewById(R.id.tvSite)).setVisibility(View.GONE);

            if (fields.anu_email != null && fields.anu_email != "")
                ((TextView) findViewById(R.id.tvEmail)).setText(fields.anu_email);
            else
                ((TextView) findViewById(R.id.tvEmail)).setVisibility(View.GONE);

            if (fields.anu_url_facebook != null && fields.anu_url_facebook!= "") {
                ((TextView) findViewById(R.id.tvFacebook)).setText(fields.anu_url_facebook);
                ((TextView) findViewById(R.id.tvFacebook)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Sistema.AbrirFacebook(fields.anu_url_facebook, getThis());
                    }
                });
            }
            else
                ((TextView) findViewById(R.id.tvFacebook)).setVisibility(View.GONE);

            setTelefone((TextView) findViewById(R.id.tvTelefone1), fields.cli_telefone);

            if (fields.cli_telefone_1 != null && fields.cli_telefone != fields.cli_telefone_1)
                setTelefone((TextView) findViewById(R.id.tvTelefone2), fields.cli_telefone_1);
            else
                ((TextView)findViewById(R.id.tvTelefone2)).setVisibility(View.GONE);

            if (fields.cli_telefone_2 != null)
                setTelefone((TextView) findViewById(R.id.tvTelefone3), fields.cli_telefone_2);
            else
                ((TextView) findViewById(R.id.tvTelefone3)).setVisibility(View.GONE);

            TextView tvDetalhes = (TextView) findViewById(R.id.tvDetalhes);
            if (fields.anu_detalhe_1 != null) {
                tvDetalhes.setText(fields.anu_detalhe_1);
                tvDetalhes.setVisibility(View.VISIBLE);
            }
            else
                tvDetalhes.setVisibility(View.GONE);
        }
        catch(Exception e){
            Sistema.ExibeMensagem("ERRO:" + e.getMessage());
        }
    }

    private void setTelefone(TextView tvTelefone, String numero) {
        tvTelefone.setVisibility(View.VISIBLE);
        tvTelefone.setText(numero);
    }

    private ActDetalhes getThis(){
        return this;
    }
}
