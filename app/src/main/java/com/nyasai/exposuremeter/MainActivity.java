package com.nyasai.exposuremeter;
/*
EV,TV,AVのどれを計算するべきなのかを判別するフラグを用意
(各計算でどれがいるかによって，フラグをたてる)


 */
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;



public class MainActivity extends Activity implements SensorEventListener,View.OnClickListener{
    private SensorManager sensorManager;
    private TextView textView,text_tv,text_av,text_iso,text_ev;
    private Button button_tv,button_av,button_iso,button_start,button_set,button_reset;
    AlertDialog.Builder alertDialog;

    int[] onFlag={0,0,0,0};
    int[] setFlag={0,0,0,0};
    int tempFlagTv=0;
    int startFlag=0;
    int stopFlag=0;
    int resetFlag=0;
    double tv=0,av=0,ev=0;
    String tempStrTv,tempStrAv,tempStrIso,tempStr;
    String F="0",T="0",ISO="0";


    String[] str_tv={"1/4000","1/2000","1/1000","1/500","1/250",
                    "1/125","1/60","1/30","1/15","1/8","1/4","1/2"};
    String[] temp_str_tv={"0.00025","0.0005","0.001","0.002","0.004",
                          "0.008","0.016667","0.033334","0.066667","0.125","0.25","0.5"};

    String[] str_av={"2","2.8","4","5.6","8","11","16","22"};

    String[] str_iso={"6","12","25","50","100","200","400",
                      "800","1600","3200","6400"};

    //センサーの指定
    int SENSOR_NAME = Sensor.TYPE_LIGHT;
    //センサーの値を取得するタイミングの指定
    int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.myGetInstance();
        this.mySetLisner();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //センサーを指定してSensorインスタンスを取得
        Sensor sensor = sensorManager.getDefaultSensor(SENSOR_NAME);
        //センサーリスナーに登録
        sensorManager.registerListener(this,sensor,SENSOR_DELAY);
    }

    @Override
    protected void onPause(){
        super.onPause();
        //センサーリスナーを解除
        sensorManager.unregisterListener(this);
    }


    //精度変更時に呼び出される
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //センサータイプが取得したいものか否かを確認
        if(event.sensor.getType()==SENSOR_NAME){
            textView.setText("IlluminanceValue : " + String.valueOf(Math.round(event.values[0])));
            Log.d(
                    "IlluminanceValue",
                    String.valueOf(event.values[0])
            );
        }
    }

    //ボタン
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_TV :
                onFlag[0]=1;
                setFlag[0]=1;
                setDialog("シャッター速度",str_tv);
                break;

            case R.id.button_AV :
                onFlag[1]=1;
                setFlag[1]=1;
                setDialog("絞り",str_av);
                break;

            case  R.id.button_ISO :
                onFlag[2]=1;
                setFlag[2]=1;
                setDialog("ISO感度",str_iso);
                break;

            case  R.id.START :
                EVCalculation();
                onFlag[3]=1;
                setFlag[3]=1;
                break;

            case  R.id.RESET :
                onFlag[0]=0;onFlag[1]=0;onFlag[2]=0;onFlag[3]=0;
                setFlag[0]=1;setFlag[1]=1;setFlag[2]=1;setFlag[3]=1;
                text_tv.setText("0");text_av.setText("0");text_iso.setText("0");text_ev.setText("0");
                startFlag=0;tempFlagTv=0;
                break;

            case R.id.SET :
                mySetText();
                break;

        }
    }

    //インスタンスの生成
    public void myGetInstance(){
        textView = (TextView) this.findViewById(R.id.IlluminanceValue);
        button_tv = (Button) this.findViewById(R.id.button_TV);
        button_av = (Button) this.findViewById(R.id.button_AV);
        button_iso = (Button) this.findViewById(R.id.button_ISO);
        button_start = (Button) this.findViewById(R.id.START);
        button_set = (Button) this.findViewById(R.id.SET);
        button_reset = (Button) this.findViewById(R.id.RESET);
        text_tv=(TextView) this.findViewById(R.id.TV_Text);
        text_av=(TextView) this.findViewById(R.id.AV_Text);
        text_iso=(TextView) this.findViewById(R.id.ISO_Text);
        text_ev=(TextView) this.findViewById(R.id.EV_Text);
        alertDialog=new AlertDialog.Builder(this);

        //SensorManagerインスタンスの取得
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    //リスナーの設定
    public void mySetLisner(){
        button_tv.setOnClickListener(this);
        button_av.setOnClickListener(this);
        button_iso.setOnClickListener(this);
        button_start.setOnClickListener(this);
        button_set.setOnClickListener(this);
        button_reset.setOnClickListener(this);
    }

    public void setDialog(String title, final String[] setStr){
        alertDialog.setTitle(title);
        alertDialog.setItems(setStr,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(setFlag[0]==1){
                    //選択された項目をセットする
                    tempStr = setStr[which];
                    tempStrTv =temp_str_tv[which];
                    setFlag[0]=0;
                }
                else if(setFlag[1]==1){
                    tempStrAv =setStr[which];
                    setFlag[1]=0;
                }
                else if(setFlag[2]==1){
                    tempStrIso=setStr[which];
                    setFlag[2]=0;
                }
            }
        }).show();

        //return tempStr;
    }


    public void mySetText(){
        if(onFlag[0]==1){
            text_tv.setText(tempStr);
            T=tempStrTv;
            onFlag[0]=0;
        }
        if(onFlag[1]==1){
            text_av.setText(tempStrAv);
            F=tempStrAv;
            onFlag[1]=0;
        }
        if(onFlag[2]==1){
            text_iso.setText(tempStrIso);
            ISO=tempStrIso;
            onFlag[2]=0;
        }
        if(onFlag[3]==1){
            text_ev.setText(String.valueOf(Math.round(ev)));
            onFlag[3]=0;
        }
    }

    public void EVCalculation(){
        tv=-(Math.log10(Double.parseDouble(T))/Math.log10(2.0f));
        av=2*(Math.log10(Double.parseDouble(F))/Math.log10(2.0f));
        ev=(av+tv)-((Math.log10(Double.parseDouble(ISO))/Math.log10(2))-(Math.log10(100)/Math.log10(2)));
    }







/*
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
    }*/
}
