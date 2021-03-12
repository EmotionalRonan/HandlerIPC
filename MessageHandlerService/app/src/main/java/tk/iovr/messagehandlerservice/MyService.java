package tk.iovr.messagehandlerservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.util.ArrayList;

public class MyService extends Service {

    private static final int RECEIVE_MESSAGE_CODE = 0x0001;
    private static final int SEND_MESSAGE_CODE = 0x0002;

    /**
     * Keeps track of all current registered clients.
     */
    ArrayList<Messenger> mClients = new ArrayList<>();
    /**
     * Holds last value set by a client.
     */
    int mValue = 0;

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_VALUE = 3;


    private Messenger clientMessenger = null;
    private Messenger serviceMessenger = new Messenger(new ServiceHandler());

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d("MyService", "bind");

        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clientMessenger = null;
    }

    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d("MyService", "handleMessage:" + msg.what);

            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.d("MyService", "client added :" + msg.replyTo.toString());
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;

                    int a = msg.arg1;
                    int b = msg.arg2;
                    int c = a+b;

                    for (int i = mClients.size() - 1; i >= 0; i--) {
                        try {
                            //模拟耗时
                            Thread.sleep(5000);
                            // 给 客户端发消息
                            mClients.get(i).send(Message.obtain(null, MSG_SET_VALUE, c, 0));
                        } catch (RemoteException | InterruptedException e) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            mClients.remove(i);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);

            }

//            if (msg.what == RECEIVE_MESSAGE_CODE){
//                Bundle data = msg.getData();
//                if (data !=null){
//                    String str = data.getString("msg");
//                    Toast.makeText(getApplicationContext(), "Service:I received the message:"+str, Toast.LENGTH_SHORT).show();
//                    Log.d("MyService","Service:I received the message:"+str);
//                }
//                clientMessenger = msg.replyTo; // 在客户端中创建
//                if (clientMessenger!=null){
//                    Message messageToClient = Message.obtain();
//                    messageToClient.what = SEND_MESSAGE_CODE;
//                    Bundle bundle  = new Bundle();
//                    bundle.putString("msg","客户端，我接收到你的消息了，这是我回应给你的，看到了吗？");
//                    messageToClient.setData(bundle);
//                    try {
//                        clientMessenger.send(messageToClient);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }

        }
    }// end ServiceHandler

}
