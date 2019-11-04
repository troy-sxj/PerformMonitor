package com.mika.perform;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mika.pm.android.core.util.DeviceUtil;
import com.mika.pm.android.core.util.ShellUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnJcmd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnJcmd = findViewById(R.id.btn_jcmd);
        btnJcmd.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_jcmd:
                testJcmd();
                break;
        }
    }


    private void testJcmd(){
        String s = ShellUtil.execCmd("jcmd -heap " + DeviceUtil.getPId());
        Log.e("sxj", s);
    }
}
