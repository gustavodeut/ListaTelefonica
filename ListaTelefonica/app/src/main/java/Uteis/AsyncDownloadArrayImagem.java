package Uteis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.schmidt.gustavo.listatelefonica.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.util.List;

/**
 * Created by gustavo on 8/20/2014.
 */
public class AsyncDownloadArrayImagem extends AsyncTask<Void,Void,Void> {

    private Bitmap bitmap;
    private String url;
    private List<Bitmap> listBitmap;
    private AbsUtil absUtil;
    private int quantidadeImgens;

    public AsyncDownloadArrayImagem(String url, List<Bitmap> listBitmap, AbsUtil absUtil, int qtdeImagens)
    {
        super();

        this.url = url;
        this.listBitmap = listBitmap;
        this.absUtil = absUtil;
        this.quantidadeImgens = qtdeImagens;

        Sistema.listAsyncTask.add(this);

        super.execute();
    }

    private Bitmap downloadBitmap(String url) {
        // initilize the default HTTP client object
        final DefaultHttpClient client = new DefaultHttpClient();

        //forming a HttoGet request
        final HttpGet getRequest = new HttpGet(url);
        try {

            HttpResponse response = client.execute(getRequest);

            //check 200 OK for success
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w("myLog ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return ImageCommons.decodeSampledBitmapFromResource(Sistema.contextActMain.getResources(), R.drawable.ic_launcher, 70, 70);
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    // getting contents from the stream
                    inputStream = entity.getContent();

                    // decoding stream data back into image Bitmap that android understands
                    bitmap = BitmapFactory.decodeStream(inputStream);

                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // You Could provide a more explicit error message for IOException
            getRequest.abort();
            Log.e("ImageDownloader", "Something went wrong while" +
                    " retrieving bitmap from " + url + e.toString());
        }

        return bitmap;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        Sistema.ShowProgressBar();
    }

    @Override
    protected Void doInBackground(Void... params) {
        // TODO Auto-generated method stub
        try
        {
            bitmap = downloadBitmap(url);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);

        Sistema.HideProgressBar(this);

        if(bitmap != null)
        {
            listBitmap.add(bitmap);
            if (quantidadeImgens == listBitmap.size())
                absUtil.Executa(listBitmap);
        }
    }
}
