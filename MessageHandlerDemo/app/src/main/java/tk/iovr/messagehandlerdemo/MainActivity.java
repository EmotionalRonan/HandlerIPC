package tk.iovr.messagehandlerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button start;
    private Messenger messenger = null;

    private boolean bound;
    private TextView tvMsg;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("MainActivity", "onServiceConnected: ");
            messenger = new Messenger(service);
            bound = true;

//            sayHello();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MainActivity", "onServiceDisconnected: ");

            messenger = null;
            bound = false;
        }
    };

    public void  sayHello(View view){

        Log.d("MainActivity", "sayHello: bound:"+bound);

        if (!bound){
            Log.d("MainActivity", "not bound  :");
            return;
        }else {
            Message message = Message.obtain(null,MyService.MSG_SAY_HELLO,0,0);
            try {
                Log.d("MainActivity", "send: ");

                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.button);
        tvMsg = findViewById(R.id.textshow);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sayHello(v);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity.this,MyService.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound){
            unbindService(serviceConnection);
            bound = false;
        }
    }
}