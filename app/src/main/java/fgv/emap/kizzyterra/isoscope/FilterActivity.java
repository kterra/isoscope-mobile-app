package fgv.emap.kizzyterra.isoscope;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "Filter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Button filterButton = (Button) findViewById(R.id.filter_button);

        filterButton.setOnClickListener(this);
    }

    public void onClick(View v) {

        EditText editTextHour  = (EditText)findViewById(R.id.editText2);
        EditText editTextMinute  = (EditText)findViewById(R.id.editText3);
        EditText editTextSecond  = (EditText)findViewById(R.id.editText4);

        Integer hours;
        Integer minutes;
        Integer seconds;
        try{
            hours = Integer.valueOf(editTextHour.getText().toString());
        }catch (NumberFormatException ne){
            hours = 0;
        }

        try{
          minutes = Integer.valueOf(editTextMinute.getText().toString());
        }catch (NumberFormatException ne){
            minutes = 0;
        }
        try{
            seconds = Integer.valueOf(editTextSecond.getText().toString());
        }
        catch (NumberFormatException ne){
            seconds = 0;
        }


        Double isochroneDuration = hours*60.0 + minutes + seconds/60.0;

        Log.d(TAG, String.valueOf(isochroneDuration) + '=' + String.valueOf(hours) + " " +  String.valueOf(minutes)+ " " + String.valueOf(seconds));

        Intent intent = new Intent();
        intent.putExtra("duration", isochroneDuration);
        setResult(RESULT_OK,intent);

        finish();



     }
}
