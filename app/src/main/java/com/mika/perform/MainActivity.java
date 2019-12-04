package com.mika.perform;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mika.pm.android.core.PerformMonitor;
import com.mika.pm.android.core.util.DeviceUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnJcmd;
    private Button btnDump;
    private Button btn_jump;

    private List<LargeModel> models = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnJcmd = findViewById(R.id.btn_jcmd);
        btnJcmd.setOnClickListener(this);

        btnDump = findViewById(R.id.btn_dump);
        btnDump.setOnClickListener(this);
        btn_jump = findViewById(R.id.btn_jump);
        btn_jump.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_jcmd:
                testJcmd();
                break;
            case R.id.btn_dump:
                testDump();
                break;
            case R.id.btn_jump:
                startActivity(new Intent(this, LeakActivity.class));
                break;
        }
    }


    private void testJcmd() {
//        String s = ShellUtil.execCmd("jcmd -heap " + DeviceUtil.getPId());
//        Log.e("sxj", s);

        long[] vmLimit = DeviceUtil.getVmLimit();
        Log.e("sxj", vmLimit[0] + ", " + vmLimit[1]);
    }

    private void testDump() {
//        for(int i =0; i< 1000; i++){
//            models.add(new LargeModel());
//        }
        PerformMonitor.getInstance().startAllPlugins();
    }
}
