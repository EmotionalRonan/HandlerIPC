package tk.iovr.messagehandlerdemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MyService extends Service {


    static final int MSG_SAY_HELLO = 1;

    //实现一个能够处理接收信息的Handler
    class InComingHandler extends Handler{
        private Context applicationContext;

        InComingHandler(Context context){
            applicationContext = context.getApplicationContext();
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            Log.d("MainActivity-MyService","recivied :"+msg.what);

            switch (msg.what){
                case MSG_SAY_HELLO:
//                    Toast.makeText(getApplicationContext(), "Hello!", Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity-MyService","Hello,Hello,Hello ");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    //被客户端接收的Messenger对象
     Messenger messenger ;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        Toast.makeText(getApplicationContext(), "onBind", Toast.LENGTH_SHORT).show();
        Log.d("MainActivity-MyService","onBind");
        messenger  = new Messenger(new InComingHandler(this));

        return messenger.getBinder();
    }

}