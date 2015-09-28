package apps.itaas.com.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public final static String LOCATION_NAME = "apps.itaas.com.weatherapp.LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Called when the user clicks the Set button */
    public void setLocation(View view) {
        Intent intent = new Intent(this, ForecastActivity.class);
        EditText editText = (EditText) findViewById(R.id.set_location);
        String location = editText.getText().toString();
        //Passing the location to ForecastActivity which holds ForecastFragment
        intent.putExtra(LOCATION_NAME,location);
        startActivity(intent);
    }
}
