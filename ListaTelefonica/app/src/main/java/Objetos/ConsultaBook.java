package Objetos;

import android.app.ActionBar;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.schmidt.gustavo.listatelefonica.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import Objetos.Book.Book;
import Uteis.AbsTransferencia;
import Uteis.AdpHits;
import Uteis.Sistema;

/**
 * Created by Gustavo on 8/3/2014.
 */
public class ConsultaBook {

    private boolean consultando;
    public boolean consultandoMais;

    public Book book;
    public AdpHits adpHits;
    public InfTransferencia infTransferencia;
    final public List<String> palavrasConsultada = new ArrayList<String>();

    public ConsultaBook(){
        infTransferencia = new InfTransferencia();
    }

//    private ConsultaBook getThis(){
//        return this;
//    }
//
//    private void Clear(){
//        if (adpHits != null) {
//            adpHits.getHitList().clear();
//            adpHits.notifyDataSetChanged();
//        }
//    }

    public boolean ConsultarBack(){
        if (palavrasConsultada.size() <= 1)
            return false;

        palavrasConsultada.remove(palavrasConsultada.size() - 1);
        Consulta();

        return true;
    }

    public void Consultar(String query){
        palavrasConsultada.add(query);
        Consulta();
    }

    private void Consulta(){

        if (!Sistema.isConnectedInternet) {
            consultando = false;
            Sistema.CheckNetworkConnection();
            if (!Sistema.isConnectedInternet)
                return;
        }

        if (consultando) {
            Sistema.CancelAllAsyncTask();
        }

        consultando = true;
        consultandoMais = false;
        adpHits = null;
        ConsultaMais();
    }

    public void ConsultaMais(){

        if (consultandoMais || palavrasConsultada.size() == 0)
            return;

        consultandoMais = true;

        infTransferencia.query = palavrasConsultada.get(palavrasConsultada.size() - 1);

        if (book != null && adpHits != null)
            if(adpHits.getHitList().size() < book.hits.found)
                infTransferencia.start = adpHits.getHitList().size();
            else {
                if (adpHits.getHitList().size() > Sistema.qttResultsAmazon) {
                    Sistema.ExibeMensagem("Todos os " + book.hits.found + " foram exibidos!");
                }
                return;
            }

        new AsyncConsulta(infTransferencia, Sistema.contextActMain,
            new AbsTransferencia() {

                @Override
                public void Sucess(String resposta) {

                    consultando = consultandoMais = false;

                    try {
                        //resposta = resposta.replace("time-ms", "time_ms").replace("&quot;", "\"");

                        //String teste = resposta.substring(6807);

                        book = (Book) new Gson().fromJson(resposta, Book.class);

                        if (book.hits.hit.size() == 0) {
                            Sistema.ExibeMensagem("Nenhum resultado encontrado.");
                        } else {
                            if (adpHits != null) {
                                adpHits.getHitList().addAll(book.hits.hit);
                                adpHits.notifyDataSetChanged();
                            } else {
                                adpHits = new AdpHits(book.hits.hit, Sistema.contextActMain);
                                SetListView();
                            }
                        }
                    }
                    catch(JsonSyntaxException ex){
                        Sistema.ExibeMensagem("ERRO: " + ex.getMessage());
                    }
                    catch(Exception ex){
                        Sistema.ExibeMensagem("ERRO: " + ex.getMessage());
                    }
                }

                @Override
                public void Error(String resposta) {
                    consultando = consultandoMais = false;
                    Sistema.ExibeMensagem("ERRO: " + resposta);
                }
            });
    }

    public void SetListView(){

        ListView lv = (ListView) Sistema.activityActMain.findViewById(R.id.listView_Book);

        lv.setAdapter(adpHits);
    }
}

class InfTransferencia {

    String query;
    int start;

    InfTransferencia() {
        super();
        this.Clear();
    }

    void Clear(){
        this.query = "";
        this.start = 0;
    }

    String getUrl() {

        String url = "";

        try
        {
//            Antiga URL sem localização
//            if (Sistema.location == null) {
//                url = Sistema.UrlConsultaBook;
//                url += "q=" + URLEncoder.encode(query.trim(), "UTF-8");
//                url += "&start=" + start;
//            }
//            else
            {
                url = Sistema.UrlConsultaBook_Location;
                query = query.trim();
                //url += "q=" + URLEncoder.encode(query, "UTF-8");
                url += "q=" + query.replace(" ", "%20");
                url += "-" + start;
                String latitude = "0";
                String longitude = "0";

                if (Sistema.location != null) {

                    if (Sistema.location.getLatitude() > 0)
                        latitude = "" + Sistema.location.getLatitude();
                    else
                        latitude = "t" + (Sistema.location.getLatitude() * -1);

                    if (Sistema.location.getLongitude() > 0)
                        longitude = "" + Sistema.location.getLongitude();
                    else
                        longitude = "t" + (Sistema.location.getLongitude() * -1);

                    latitude = latitude.replace('.', 'p');
                    longitude = longitude.replace('.', 'p');
                }
                url += "-" + latitude + "," + longitude;
            }
        }
//        catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Clear();

        Log.d("myLog", "URL: " + url);

        return url;
    }

}

class AsyncConsulta extends AsyncTask<Void, Integer, Boolean> {

    private InfTransferencia infTransferencia;
    private AbsTransferencia absResposta;
    private String strResposta;
    private Context context;

    public AsyncConsulta(InfTransferencia infTra, Context context, AbsTransferencia absResposta)
    {
        super();
        this.infTransferencia = infTra;
        this.absResposta = absResposta;
        this.context = context;

        Sistema.listAsyncTask.add(this);

        super.execute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try
        {
            StringBuilder sbResposta = new StringBuilder();
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(infTransferencia.getUrl());

            HttpResponse resposta = httpClient.execute(httpGet);

            int status = resposta.getStatusLine().getStatusCode();
            if(status == HttpStatus.SC_OK) {

                HttpEntity entidade = resposta.getEntity();
                InputStream conteudo = entidade.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conteudo));

                String linha;
                while((linha = reader.readLine()) != null) {
                    sbResposta.append(linha);
                }
            }
            else{
                Log.d("myLog", getClass().getSimpleName() + "Erro " + status + " ao pesqusiar URL principal");
            }
            strResposta = sbResposta.toString();

            // retornar verdadeiro para indicar que a execu��o foi conclu�da com sucesso!
            return Boolean.valueOf(true);

        } catch (UnknownHostException e){
            Sistema.isConnectedInternet = false;
            e.printStackTrace();
            this.strResposta = "ERRO: " + e.toString();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            this.strResposta = "ERRO: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            this.strResposta = "ERRO: " + e.toString();
        }catch(Exception e){
            e.printStackTrace();
            this.strResposta = "ERRO: " + e.toString();
        }
        finally{
        }

        // retornar falso para indicar que houve algum problema na execu��o
        return Boolean.valueOf(false);
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();

        Sistema.ShowProgressBar();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);

        if(result)
            this.absResposta.Sucess(strResposta);
        else
            this.absResposta.Error(strResposta);

        Sistema.HideProgressBar(this);
    }
}