package apps.itaas.com.weatherapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private static ArrayAdapter<String> mForecastAdapter;
    private static String location;
    private static TextView textBox;
    private static FetchWeatherTask fetchWeatherTask;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location = this.getArguments().getString("location");
        setHasOptionsMenu(true);// Added this line in order for this fragment to handle menu events.
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            fetchWeatherTask = new FetchWeatherTask();
            fetchWeatherTask.execute(location);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - Dummy data. Press refresh to update - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        //creating a ListView adapter
        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        textBox = (TextView) rootView.findViewById(R.id.json_text);
        return rootView;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private String forecastJsonStr;// Will contain the raw JSON response as a string.
        //method to get max temperature using JSONparsing
        private double getMaxTemperatureForDay(int dayIndex) throws JSONException{
            JSONObject rootWeatherObject = new JSONObject(forecastJsonStr);
            JSONArray days = rootWeatherObject.getJSONArray("list");
            JSONObject dayInfo = days.getJSONObject(dayIndex);
            JSONObject tempInfo = dayInfo.getJSONObject("temp");
            return tempInfo.getDouble("max");
        }
        //method to get min temperature using JSONparsing
        private double getMinTemperatureForDay(int dayIndex) throws JSONException{
            JSONObject rootWeatherObject = new JSONObject(forecastJsonStr);
            JSONArray days = rootWeatherObject.getJSONArray("list");
            JSONObject dayInfo = days.getJSONObject(dayIndex);
            JSONObject tempInfo = dayInfo.getJSONObject("temp");
            return tempInfo.getDouble("min");
        }

        //method to get weather state using JSONparsing
        private String getMainData(int dayIndex) throws JSONException{
            JSONObject rootWeatherObject = new JSONObject(forecastJsonStr);
            JSONArray days = rootWeatherObject.getJSONArray("list");
            JSONObject dayInfo = days.getJSONObject(dayIndex);
            JSONArray weatherStateInfo = dayInfo.getJSONArray("weather");
            return weatherStateInfo.getJSONObject(0).getString("main");
        }

        //Appends a week's weather data into string array using the above methods
        private String[] appendDaysWeatherData(){
            String[] dataStr = new String[7];
            for(int i=0;i<7;i++){
                String main;
                double min,max;
                try{
                    main = getMainData(i);
                    min = getMinTemperatureForDay(i);
                    max = getMaxTemperatureForDay(i);
                    dataStr[i]= "Today "+"+ "+i+" "+"--Weather State: "+main+" --max/min: "+max+"/"+min;
                    //data.add(i,"Today "+i+" "+"Weather Like: "+main+" max/min: "+max+"/"+min);
                    //Log.v(LOG_TAG,"Today "+i+" "+"Weather Like: "+main+" max/min: "+max+"/"+min);
                }
                catch (JSONException e){
                    Log.e(LOG_TAG,e.getMessage());
                }
            }
            return dataStr;
        }

        //Generic method to perform background operations like network calls.
        @Override
        protected String[] doInBackground(String... params) {
            String[] weatherForecastData= null;
            //If location name is not given then there's nothing to look up.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            String format = "json";
            String units = "metric";
            int numOfDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numOfDays))
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI: " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                //Log.v(LOG_TAG, "JSON weather data: " + forecastJsonStr);

                weatherForecastData = appendDaysWeatherData();//returns the string array of new weather data.

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: " + e.getMessage());
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return weatherForecastData;
        }

        //After executing the background method, the
        //return value is "grabbed" using this method's argument.
        @Override
        protected void onPostExecute(String[] result){
            if(result !=null){
                mForecastAdapter.clear();//clearing the previous values
                for(String day:result){
                    mForecastAdapter.add(day);//Adding each new value to overwrite previous values
                }
                mForecastAdapter.notifyDataSetChanged();//Notifying the adapter to refresh

            }
        }



    }

}
