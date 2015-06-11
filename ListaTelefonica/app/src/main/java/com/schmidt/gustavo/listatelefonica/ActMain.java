package com.schmidt.gustavo.listatelefonica;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;

import com.schmidt.gustavo.listatelefonica.R;

import Objetos.ConsultaBook;
import Uteis.Sistema;


public class ActMain extends Activity implements ViewTreeObserver.OnScrollChangedListener{

    private ActionBar mActionBar;
    static private int oldVisibleItemListView = 0;
    static private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_main);

        mActionBar = getActionBar();
        Sistema.contextActMain = this;
        Sistema.activityActMain = this;
        ListView lv = (ListView) Sistema.activityActMain.findViewById(R.id.listView_Book);

        if (Sistema.consultaBook == null) {
            Sistema.consultaBook = new ConsultaBook();

            // Acquire a reference to the system Location Manager
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    Sistema.location = location;
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            if(locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)) {
                // Register the listener with the Location Manager to receive location updates
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
            //Sistema.consultaBook.Consultar("donna oliva");
        }
        else
        {
            Sistema.consultaBook.SetListView();
            lv.setSelection(oldVisibleItemListView);
            Sistema.CancelAllAsyncTask();
        }

        IniciaListView(lv);
    }

    private void IniciaListView(ListView lv){

        lv.setDivider(null);
        ViewGroup footer = (ViewGroup) Sistema.activityActMain.getLayoutInflater().inflate(R.layout.footer_list_view_loading, lv, false);
        lv.addFooterView(footer);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Sistema.consultaBook.adpHits.setButtonActionItemList(null);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

//                if (firstVisibleItem > oldVisibleItemListView)
//                {
//                    if(mActionBar.isShowing())
//                        mActionBar.hide();
//                }
//                else
//                if (firstVisibleItem < oldVisibleItemListView && !mActionBar.isShowing())
//                    mActionBar.show();
                oldVisibleItemListView = firstVisibleItem;

                if (!Sistema.consultaBook.consultandoMais && (firstVisibleItem + visibleItemCount) > totalItemCount - Sistema.lastQttSearch) {
                    Sistema.consultaBook.ConsultaMais();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act_main, menu);

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();

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
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if (!Sistema.consultaBook.ConsultarBack()) {
            super.onBackPressed();
        }
        else{
            String query = Sistema.consultaBook.palavrasConsultada.get(Sistema.consultaBook.palavrasConsultada.size() - 1);
            searchView.setQuery(query, false);
        }
    }

    @Override
    public void onScrollChanged() {

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        String query = Sistema.consultaBook.palavrasConsultada.get(Sistema.consultaBook.palavrasConsultada.size() - 1);
        searchView.setQuery(query, false);
    }
}
