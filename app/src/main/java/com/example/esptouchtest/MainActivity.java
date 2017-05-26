package com.example.esptouchtest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.esptouchtest.esptouch.EsptouchTask;
import com.example.esptouchtest.esptouch.IEsptouchResult;
import com.example.esptouchtest.esptouch.IEsptouchTask;
import com.example.esptouchtest.esptouch.task.__IEsptouchTask;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView tvWifiName;
    private EditText etPassword;
    private CheckBox ckIsShow;
    private Button btnESPTouch;
    private ListView lvWifi;
    private ArrayAdapter<String> adapter;
    private WifiHelper wifiHelper;
    private ArrayList<String> wifiList;
    private String isHiddenStr="YES";//记得初始化这个值，如果用户没有去按checkbox的话，这个值会是null；
    /**
     * 启动ESPTouch
     */
    private View.OnClickListener espTouchListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String ssid=tvWifiName.getText().toString();
            int wifiIndex=0;
            //由scanResultList得到一个wifi对应的scanResult，再由这个scanResult得到bssid
            ArrayList<ScanResult> resultList=(ArrayList)wifiHelper.getScanResult();
            for(int i=0;i<resultList.size();i++){
                if(resultList.get(i).SSID.equals(ssid)){
                    wifiIndex=i;
                }
            }
            String bssid=resultList.get(wifiIndex).BSSID;
            String password=etPassword.getText().toString();
//            LogUtil.v("test","ssid="+ssid);
//            LogUtil.v("test","bssid="+bssid);
//            LogUtil.v("test","password="+password);
            new EsptouchAsyncTask2().execute(ssid,bssid,password,isHiddenStr);
        }
    };
    /**
     * 输入密码的时候enable  按键
     */
    private TextWatcher passwordWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(TextUtils.isEmpty(s)){
                btnESPTouch.setEnabled(false);
            }else{
                btnESPTouch.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {        }
    };

    /**
     * 隐藏或显示密码
     * @param savedInstanceState
     */
    private CompoundButton.OnCheckedChangeListener isShowListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                isHiddenStr="NO";
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                etPassword.setSelection(etPassword.getText().length());
            }else{
                isHiddenStr="YES";
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                etPassword.setSelection(etPassword.getText().length());
            }
        }
    };

    /**
     * listView 点击监听
     * @param savedInstanceState
     */
    private AdapterView.OnItemClickListener listViewListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String newWifiName=wifiList.get(position);
            tvWifiName.setText(newWifiName);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance();
        lvWifi.setAdapter(adapter);
        btnESPTouch.setOnClickListener(espTouchListener);
        etPassword.addTextChangedListener(passwordWatcher);
        ckIsShow.setOnCheckedChangeListener(isShowListener);
        lvWifi.setOnItemClickListener(listViewListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String wifiName=wifiHelper.getWifiInfo().getSSID();
        wifiName=wifiName.substring(1,wifiName.length()-1);//去掉SSID前后的引号
        if(wifiName!=null){
            tvWifiName.setText(wifiName);
        }else{
            tvWifiName.setText("");
        }
        btnESPTouch.setEnabled(TextUtils.isEmpty(wifiName));
    }

    private void instance(){
        tvWifiName=(TextView)findViewById(R.id.tv_ssid);
        etPassword=(EditText)findViewById(R.id.et_password);
        ckIsShow=(CheckBox)findViewById(R.id.ck_show_password);
        btnESPTouch=(Button)findViewById(R.id.btn_espTouch);
        lvWifi=(ListView)findViewById(R.id.lv_wifi);
        wifiHelper=new WifiHelper(this);
        wifiList=wifiHelper.getWifiSSIDList();
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,wifiList);
    }
    /**
     * 开始ESPTouch功能的异步消息处理
     */
    private class EsptouchAsyncTask2 extends AsyncTask<String, Void, IEsptouchResult> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog
                    .setMessage("Esptouch is configuring, please wait for a moment...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (__IEsptouchTask.DEBUG) {

                    }
                    if (mEsptouchTask != null) {
                        mEsptouchTask.interrupt();
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(false);
        }

        @Override
        protected IEsptouchResult doInBackground(String... params) {
            String apSsid = params[0];
            String apBssid = params[1];
            String apPassword = params[2];
            String isSsidHiddenStr = params[3];
            boolean isSsidHidden = false;
            if (isSsidHiddenStr.equals("YES")) {
                isSsidHidden = true;
            }
            mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, MainActivity.this);
            IEsptouchResult result = mEsptouchTask.executeForResult();
            return result;

        }

        @Override
        protected void onPostExecute(IEsptouchResult result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    "Confirm");
            // it is unnecessary at the moment, add here just to show how to use isCancelled()
            if (!result.isCancelled()) {
                if (result.isSuc()) {
                    mProgressDialog.setMessage("Esptouch success, bssid = "
                            + result.getBssid() + ",InetAddress = "
                            + result.getInetAddress().getHostAddress());
                } else {
                    mProgressDialog.setMessage("Esptouch fail");
                }
            }
        }
    }
}