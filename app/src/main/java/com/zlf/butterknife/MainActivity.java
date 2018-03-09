package com.zlf.butterknife;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zlf.ioc.Bind;
import com.zlf.ioc.ViewInjectImpl;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.tv_content)
    TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ViewInjector.injectView(this);
        ViewInjectImpl.bind(this);
        tvContent.setText("替换了");
    }
}
