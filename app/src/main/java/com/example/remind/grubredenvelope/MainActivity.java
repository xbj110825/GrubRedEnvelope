package com.example.remind.grubredenvelope;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_start_service = (Button)findViewById(R.id.button_start_service);
        button_start_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_accessibility_settings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent_accessibility_settings);
                Toast.makeText(MainActivity.this, "请开启【自动抢红包】服务", Toast.LENGTH_LONG).show();
            }
        });


        Button button_apply = (Button)findViewById(R.id.button_apply);
        button_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edittext_sleep_time = (EditText)findViewById(R.id.edittext_sleep_time);
                EditText edittext_begin_hour = (EditText)findViewById(R.id.edittext_begin_hour);
                EditText edittext_end_hour   = (EditText)findViewById(R.id.edittext_end_hour);
                TextView textview_current_settings = (TextView)findViewById(R.id.textview_current_settings);
                int begin_hour;
                int end_hour;

                if (!edittext_sleep_time.getText().toString().equals(""))
                    GrubRedEnvelopeService.sleep_time = (int)(Double.valueOf(edittext_sleep_time.getText().toString()) * 1000);

                if (!edittext_begin_hour.getText().toString().equals(""))
                    begin_hour = Integer.valueOf(edittext_begin_hour.getText().toString());
                else
                    begin_hour = 0;

                if (!edittext_end_hour.getText().toString().equals(""))
                    end_hour = Integer.valueOf(edittext_end_hour.getText().toString());
                else
                    end_hour = 24;

                if ((end_hour >= 0 && end_hour <= 24) && (begin_hour >= 0 && begin_hour <= 24) && (begin_hour <= end_hour)) {
                    GrubRedEnvelopeService.begin_hour = begin_hour;
                    GrubRedEnvelopeService.end_hour = end_hour;
                }

                textview_current_settings.setText("拆红包前将延迟" +
                        String.valueOf(GrubRedEnvelopeService.sleep_time / 1000.0) +
                        "秒\n程序将从" + String.valueOf(GrubRedEnvelopeService.begin_hour) +
                        "点工作到" + String.valueOf(GrubRedEnvelopeService.end_hour) + "点");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView textview_grub_num = (TextView)findViewById(R.id.textview_grub_num);
        textview_grub_num.setText("已抢到" + String.valueOf(GrubRedEnvelopeService.grub_num) + "个红包");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
