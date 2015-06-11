package Uteis;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.schmidt.gustavo.listatelefonica.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Objetos.ConsultaBook;

/**
 * Created by Gustavo on 8/3/2014.
 */
public class Sistema {

    static public Context contextActMain;
    static public Activity activityActMain;
    static public ConsultaBook consultaBook;
    static public boolean isConnectedInternet = false;

    final static public List<AsyncTask> listAsyncTask = new ArrayList<AsyncTask>();
    final static public int lastQttSearch = 4;//ao scroll da lista de consulta restar essa quantide de intens para o final da lista uma consulta é realizada e preenche os próximos itens da lista
    final static public int qttResultsAmazon = 10; //quantidade de hits de cada consulta

    final static public String UrlConsultaBook = "";

    final static public String UrlConsultaBook_Location = "";

    final static public String UrlLogo = "";

    static private int progressBar = 0;

    static public Location location;

    static public void ExibeMensagem(String msg){
        if (!msg.equals(""))
        {
            Toast to = Toast.makeText(contextActMain, msg, Toast.LENGTH_SHORT);
            to.setGravity(Gravity.CENTER_VERTICAL , 0, 0);
            to.show();
        }
    }

    static public void CheckNetworkConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) activityActMain.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            isConnectedInternet = true;
        } else {
            ExibeMensagem("Sem conexão com a internet!");
            isConnectedInternet = false;
        }
    }

    static public void Call(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        Sistema.activityActMain.startActivity(callIntent);
    }

    static public void ShowProgressBar(){
        if (++progressBar > 0)
            Sistema.activityActMain.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    static public void HideProgressBar(AsyncTask asyncTask){
        if (--progressBar <= 0)
            Sistema.activityActMain.findViewById(R.id.progressBar).setVisibility(View.GONE);

        listAsyncTask.remove(asyncTask);
    }

    static public void CancelAllAsyncTask(){
        for(AsyncTask asyncTask: listAsyncTask){
            if(asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
                asyncTask.cancel(true);
        }
        listAsyncTask.removeAll(listAsyncTask);
        Sistema.activityActMain.findViewById(R.id.progressBar).setVisibility(View.GONE);
        progressBar = 0;
        Sistema.consultaBook.consultandoMais = false;
    }

    static public void AbrirRotaGPS(Double latitude, Double longitude, Activity activity){

        String uriString = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);

//            String label = "ABC Label";
//            String uriBegin = "geo:" + latitude + "," + longitude;
//            String query = latitude + "," + longitude + "(" + label + ")";
//            String encodedQuery = Uri.encode(query);
//            uriString = uriBegin + "?q=" + encodedQuery + "&z=16";

        //Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));

        AbrirRotaGPS(activity, uriString);
    }

    static public void AbrirRotaGPS(Activity activity, String uriString){

        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            activity.startActivity(intent);
        }
        catch(ActivityNotFoundException innerEx)
        {
            ExibeMensagem("Por favor instale um aplicativo de mapa.");
        }
    }

    static public void AbrirMapaLocal(Double latitude, Double longitude, Activity activity){
        String uri;

//        uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?&daddr=%f,%f (%s)", 12f, 2f, "Where the party is at");
//        uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", latitude, longitude, "Este é o lugar");
//        uri = "geo:0,0?q=" + latitude + "," + longitude;
//        uri = "google.maps:q=" + latitude + "," + longitude;

        //uri = "https://maps.google.com/?q=-22.121364,-51.385896";
        uri = "https://maps.google.com/?q="+ latitude +","+ longitude +"";
        AbrirMapaLocal(activity, uri);
    }

    static public void AbrirMapaLocal(Activity activity, String uriString){
        try
        {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            activity.startActivity(intent);
        }
        catch(ActivityNotFoundException ex)
        {
            ExibeMensagem("Por favor instale um aplicativo de mapa.");
        }
    }

    static public void AbrirFacebook(String url, Activity activity){

        String id = url.replace("https://www.facebook.com/", "")
               .replace("https://pt-br.facebook.com/", "")
               .replace("pages/", "");

        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

//        try {
//            activity.getPackageManager().getPackageInfo("com.facebook.katana", 0);
//            activity.startActivity(new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("fb://profile/" + id)));
//        } catch (Exception e) {
//            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//        }
    }

    static public void EnviarEmail(String email, Activity activity){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, email);
//        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
//        i.putExtra(Intent.EXTRA_TEXT   , "body of email");
        try {
            activity.startActivity(Intent.createChooser(i, "Enviar email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ExibeMensagem("Não foi encontrado aplicativo de email");
        }
    }

    static public void Compartilhar(String texto, Activity activity){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, texto);
        sendIntent.setType("text/plain");
        //Sistema.activityActMain.startActivity(sendIntent);
        activity.startActivity(Intent.createChooser(sendIntent, "Compartilhar Telefone..."));
    }
}
