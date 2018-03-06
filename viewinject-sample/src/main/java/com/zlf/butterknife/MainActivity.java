package com.zlf.butterknife;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zlf.ioc.Bind;
import com.zlf.ioc.ViewInjector;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.btn_click)
    Button btnClick;
    @Bind(R.id.tv_content)
    TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ViewInjector.injectView(this);

        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvContent.setText("当前时间:"+new Date());
            }
        });
    }
}
