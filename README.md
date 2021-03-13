# HandlerIPC
通过 Handler  、Messenger、Message 实现跨进程通信（IPC）


### Messenger的优势：
- 实际传递的是Message，可以复用信息池
- 支持信息回调
- 不需要编写aidl

## Messenger IPC 架构
![](https://github.com/EmotionalRonanyg/HandlerIPC/blob/main/MessengerIPC.png)



### 多应用程序 多进程 之间进行 双向通信

1. **Server 端**   MyService 实现 代码：

   ```java
   public class MyService extends Service {
   
       private static final int RECEIVE_MESSAGE_CODE = 0x0001;
       private static final int SEND_MESSAGE_CODE = 0x0002;
   
       //保存当前连接的所有 客户端.
       ArrayList<Messenger> mClients = new ArrayList<>();
   
       static final int MSG_REGISTER_CLIENT = 1;
       static final int MSG_UNREGISTER_CLIENT = 2;
       static final int MSG_SET_VALUE = 3;
   
       private Messenger clientMessenger = null;
       private Messenger serviceMessenger = new Messenger(new ServiceHandler());
   
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
                       int a = msg.arg1;
                       int b = msg.arg2;
                       int c = a+b;
                       for (int i = mClients.size() - 1; i >= 0; i--) {
                           try {
                               Thread.sleep(5000);//模拟耗时
                               // 给 客户端发消息
                               mClients.get(i).send(Message.obtain(null, MSG_SET_VALUE, c, 0));
                           } catch (RemoteException | InterruptedException e) {
                               mClients.remove(i);// 客户端连接异常， 删除客户端
                           }
                       }
                       break;
                   default:
                       super.handleMessage(msg);
               }
           }
       }// end ServiceHandler
     
       @Override
       public IBinder onBind(Intent intent) {
           Log.d("MyService", "onBind:");
           return serviceMessenger.getBinder();
       }
   
       @Override
       public void onDestroy() {
           Log.d("MyService", "onDestroy:");
           super.onDestroy();
           clientMessenger = null;
       }
   }
   
   ```

   

2.  **Client 端**  MainActivity 实现 代码：

```java
public class MainActivity extends AppCompatActivity {

    private Button mBtBind;
    private Button mBtUnBind;
    private TextView mTvMsg;

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_VALUE = 3;

    private boolean isBound = false;
    private String SERVICE_ACTION = "tk.iovr.messagehandlerservice.MyService";

    private Messenger serviceMessenger = null;
    private Messenger clientMessenger = new Messenger(new ClientHandler());

    private class ClientHandler extends Handler  {
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
                Message message = Message.obtain(null,MSG_REGISTER_CLIENT);
                message.replyTo = clientMessenger;
                serviceMessenger.send(message);

                int a = new Random().nextInt(10);
                int b = new Random().nextInt(10);
                Log.d("MainActivity","send to remote a:"+a+", b:"+b);
              
                // 调用 Server 端代码 
                message = Message.obtain(null,MSG_SET_VALUE, a,b);
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

// 						发送 参数变量
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

        //Android 11 查询其他应用 信息需要获取权限 详见 AndroidManifest.xml
        Intent intent = new Intent(SERVICE_ACTION);
        intent.setComponent(new ComponentName("tk.iovr.messagehandlerservice",
                                              "tk.iovr.messagehandlerservice.MyService"));
        mTvMsg.setText("Binding.");
        bindService(intent,connection,BIND_AUTO_CREATE);//绑定服务
        isBound = true;
    }

    private void doUnBindService() {
        Log.d("mainActivity","doUnBindService");
        if (isBound){
            if (serviceMessenger !=null){
                try {
         						//发送解绑消息
                    Message message = Message.obtain(null,MSG_UNREGISTER_CLIENT);
                    message.replyTo = clientMessenger;
                    serviceMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            unbindService(connection);  //解绑服务
            isBound = false;
            mTvMsg.setText("Unbinding");
        }
    }
  
}
```

客户端 在 setComponent 来 BindService 时，Android 11 上面 查询其他 应用信息 需要进行权限申请：

- 第一种方式：只指定需要查询 的应用包名

  ```xml
      <queries>
          <package android:name="tk.iovr.messagehandlerservice"/>
      </queries>
  ```

  第二种方式：声明 查询所有应用信息的权限

- ```xml
  <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
  								 tools:ignore="QueryAllPackagesPermission" />
  ```

  

### 总结：

1. messenger 适合一个个消息传递，不适合高并发