package com.example.admin.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class StartPage extends Activity implements OnClickListener {
    private Button connOnServ;
    private Button startChatting;
    private TextView text;
    private EditText editText;
    private Button startServ;
    private Button stop;
    private NetworkAdapter networkAdapter;
    private TextView id;
    private String [] online =null;
    private Connection connection = Connection.getInstance();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);


        connOnServ = (Button) findViewById(R.id.connect);
        startChatting = (Button) findViewById(R.id.startChat);
        text = (TextView) findViewById(R.id.textView);
        startServ = (Button) findViewById(R.id.startServ);
        stop = (Button) findViewById(R.id.stop);
        editText = (EditText) findViewById(R.id.editText);
        id = (TextView) findViewById(R.id.ID);


        connOnServ.setOnClickListener(this);
        startChatting.setOnClickListener(this);
        startServ.setOnClickListener(this);
        stop.setOnClickListener(this);

        text.setText("State: connect or start server");


        if(connection.getSocket() != null){
            networkAdapter = new NetworkAdapter();
            networkAdapter.execute(connection.getSocket());
            startServ.setEnabled(false);
            connOnServ.setEnabled(false);
            id.setText("ID: " + connection.getID());
            editText.setText(connection.getSocket().getInetAddress().getHostAddress().toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_page, menu);
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
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect) {
            startServ.setEnabled(false);
            connOnServ.setEnabled(false);
            String IP = editText.getText() + "";
            IP = IP.trim();
            text.setText("Connect on: " + IP);
            createNetworkConnection(IP);
        }
        if (v.getId() == R.id.startServ) {
            text.setText("start server ");
            startServ.setEnabled(false);
            connOnServ.setEnabled(false);
        }
        if (v.getId() == R.id.startChat) {
            onCreateDialog(Connection.getInstance().getOnline()).show();

        }
        if (v.getId() == R.id.stop) {
            connOnServ.setEnabled(true);
            startServ.setEnabled(true);
            text.setText("stop");
            id.setText("ID:");
            if(networkAdapter != null){
                networkAdapter.closing();
                networkAdapter.close();
            }else text.setText("not connection...");
        }
    }

    private NetworkAdapter createNetworkConnection(String Ip) {
        networkAdapter = new NetworkAdapter(Ip);
        networkAdapter.execute(null,null);
        return networkAdapter;
    }


    protected Dialog onCreateDialog(String mass[]) {
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setSingleChoiceItems(mass, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("прикол");
            }
        });
        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // формируем список пользователей сервера
                ListView lv = ((AlertDialog) dialog).getListView();
                // если он не равен нулю, то
                if(lv != null){
                    // получаем позицию нужного елемента
                    int pos = lv.getCheckedItemPosition();
                    // если плдьзователь выбрал из списка, то
                    if(pos != -1){
                        // посылаем на сервер строку "%connect="+ID
                        networkAdapter.connectWith(Connection.getInstance().getOnline()[pos]);
                        // завершаем вспомогательный поток
                        networkAdapter.interupted();
                        // запускаем активити
                        startActivity();
                    }
                }
            }
        });
        return adb.create();
    }

    private void startActivity() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    private void startActivity(String massage) {
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra("massage", massage);
        startActivity(intent);
    }

    class NetworkAdapter extends AsyncTask<Socket, String, Void> {
        private PrintWriter out;
        private Scanner in;
        private String host = "192.168.1.3";
        private int PORT = 8081;
        private boolean interupted = false;

        public NetworkAdapter() {

        }

        String MyID = null;

        public NetworkAdapter(String Ip) {
            this.host = Ip;
        }

        public void interupted() {
            this.interupted = !interupted;
        }

        public boolean isInterrupted() {
            return this.interupted;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            text.setText("Connect...");
        }

        @Override
        protected Void doInBackground(Socket... sockets) {
            if(sockets[0] == null){
                openConnection();
                publishProgress("Connection Accept!");
            } else{
                    try {
                        out = new PrintWriter(sockets[0].getOutputStream());
                        in = new Scanner(new InputStreamReader(sockets[0].getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            if(out != null && in != null){
                publishProgress("Connection and in, out is create...");
                while (!isInterrupted()) {
                    if (in.hasNext()) {
                        String massage = in.nextLine();
                        publishProgress(massage);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String massage = values[0];
            if (massage.toCharArray()[0] == '%') {
                systemMasage(massage);
            }else text.setText(massage);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            text.setText("Disconnect...");
        }

        public void close() {
            if(!interupted){
                // проверяем есть ли подключение
                if(out != null){
                    closeConnection();
                }
                interupted();
            }
            Connection.getInstance().close();
        }


        private void systemMasage(String massage) {
            if (massage.split("=")[0].equals("%reciveFile")) {

            }
            if (massage.split("=")[0].equals("%online")) {
                online = massage.split("=")[1].split(":");
                online = getOnlineMas();
                Connection.getInstance().setOnline(online);
            }
            if (massage.split("=")[0].equals("%id")) {
                MyID = massage.split("=")[1];
                id.setText("ID: " + MyID);
                Connection.getInstance().setID(MyID);
            }
            if (massage.split("=")[0].equals("%Connection Accept!")) {
                interupted();
                startActivity("ID = "+massage.split("=")[1] + " Connection Accept!");
            }
            if (massage.split("=")[0].equals("%disconnect!")) {
                interupted();
            }
            if (massage.split("=")[0].equals("%serverClose")) {
                interupted();
                id.setText("ID:");
                online = null;
                Connection.getInstance().close();
            }
        }

        public void connectWith(String ID){
            String massage = "%connect="+ID+"\n\r";
            sendMassage(massage);
        }

        public void sendMassage(String massage) {
            if(out != null){
                out.write(massage + "\n\r");
                out.flush();
            }
        }

        public void disconnect() {
            sendMassage("%disconnect=" + connection.getID() + "\n\r");
        }

        public void closeConnection() {
            sendMassage("%close=" +connection.getID()+"\n\r");
        }

        public void closing() {
            sendMassage("%close=" + connection.getID() + "\n\r");
        }


        public Scanner getIn() {
            return in;
        }

        public PrintWriter getOut() {
            return out;
        }



        public String [] getOnlineMas(){
                String [] tmp = new String[online.length-1];
                for(int i=0;i<tmp.length;i++){
                    tmp[i] = online[i];
                }
                return tmp;
        }

        private void openConnection(){
            Socket socket = null;
            try {
                socket = new Socket(host, PORT);
            } catch (IOException e) {
                e.printStackTrace();

            }
            Connection.getInstance().setSocket(socket);
            if (socket != null) {
                try {
                    out = new PrintWriter(socket.getOutputStream());
                    in = new Scanner(new InputStreamReader(socket.getInputStream()));
                    // ложим потоки ввода и вывода в контейнер для возможной передачи другому активити
                    Connection.getInstance().setOutStream(out);
                    Connection.getInstance().setInStream(in);
                    Connection.getInstance().setSocket(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress("IP not correct...");
                }
            }
        }
    }





    public void setConnOnServ(Button connOnServ) {
        this.connOnServ = connOnServ;
    }

    public void setStartChatting(Button startChatting) {
        this.startChatting = startChatting;
    }

    public void setText(TextView text) {
        this.text = text;
    }

    public Button getConnOnServ() {

        return connOnServ;
    }

    public Button getStartChatting() {
        return startChatting;
    }

    public TextView getText() {
        return text;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(networkAdapter != null){
            networkAdapter.closing();
            if (!networkAdapter.isInterrupted()) {
                networkAdapter.interupted();
            }
        }
        if(connection != null){
            connection.close();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
/*        if(networkAdapter != null){
            networkAdapter.close();
        }*/
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartPage.this);
        builder.setTitle("Exit with program?");
        // builder.setMessage("Покормите кота!");
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setCancelable(false);
        // системно выходим сприложения
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                onDestroy();
                startActivity(intent);


            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel(); // отмена возвращаемся к MainActivity
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}