package tk.iovr.messagehandlerclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button mBtBind;
    private Button mBtUnBind;
    private TextView mTvMsg;

    private static final int SEND_MESSAGE_CODE = 0x0001;
    private static final int RECEIVE_MESSAGE_CODE = 0x0002;


    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_VALUE = 3;

    private boolean isBound = false;

    private String SERVICE_ACTION = "tk.iovr.messagehandlerservice.MyService";


    private Messenger serviceMessenger = null;
    private Messenger clientMessenger = new Messenger(new ClientHandler());


    private class ClientHandler extends Handler  {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_SET_VALUE:
                    Log.d("MainActivity","Received value from service: " + msg.arg1);
                    mTvMsg.setText("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }

            //old
            if (msg.what == RECEIVE_MESSAGE_CODE){
                Bundle data  = msg.getData();
                if(data !=null){
                    String str = data.getString("msg");
                    mTvMsg.setText(str);
                }
            }
        }
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d("MainActivity","onServiceConnected:");

            serviceMessenger = new Messenger(service);
            isBound = true;
            mTvMsg.setText("Attached.");


            try {

                Message message =
                        Message.obtain(null,MSG_REGISTER_CLIENT);
                message.replyTo = clientMessenger;
                serviceMessenger.send(message);

                int a =   new Random().nextInt(10);
                int b = new Random().nextInt(10);

                Log.d("MainActivity","send to remote a:"+a+", b:"+b);

                // Give it some value as an example.
                message = Message.obtain(null,MSG_SET_VALUE, a,b);
                serviceMessenger.send(message);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            Log.d("MainActivity","remote service connected:");


//            Message msg = Message.obtain();
//            msg.what =SEND_MESSAGE_CODE;
//
//            Bundle data = new Bundle();
//            data.putString("msg","你好，MyService，我是客户端");
//            msg.setData(data);
//
//            msg.replyTo = clientMessenger;
//            try {
//                serviceMessenger.send(msg);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
            isBound = false;
            mTvMsg.setText("Disconnected.");
            Log.d("MainActivity","remote service disconnected:");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mBtBind = findViewById(R.id.bound);
        mBtUnBind = findViewById(R.id.unbound);
        mTvMsg = findViewById(R.id.msgshow);

        mTvMsg.setText("Not attached.");

        mBtBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBound){

                     doBindService();
                 }
            }
        });

        mBtUnBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound){
                    doUnBindService();


                }
            }
        });


    }


    private void doBindService()  {

        Log.d("mainActivity","doBindService");

        //Android  11 查询其他应用 信息需要获取权限 详见 AndroidManifest.xml
        Intent intent = new Intent(SERVICE_ACTION);
        intent.setComponent(new ComponentName("tk.iovr.messagehandlerservice","tk.iovr.messagehandlerservice.MyService"));
        mTvMsg.setText("Binding.");
        //绑定服务
        bindService(intent,connection,BIND_AUTO_CREATE);
        isBound = true;


    }

    private void doUnBindService() {
        Log.d("mainActivity","doUnBindService");
        if (isBound){

            if (serviceMessenger !=null){
                try {
                    Message message = Message.obtain(null,MSG_UNREGISTER_CLIENT);
                    message.replyTo = clientMessenger;
                    serviceMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            //解绑服务
            unbindService(connection);
            isBound = false;
            mTvMsg.setText("Unbinding");
        }

    }


}