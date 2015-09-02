package com.example.admin.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


public class SecondActivity extends Activity implements View.OnClickListener {

    private ScrollView scrollView;
    private Button send;
    private PrintWriter out = null;
    private Scanner in = null;
    private EditText editText;
    private TextView title;
    private static LinearLayout textLayout;
    private SimpleDateFormat format = new SimpleDateFormat("[hh:mm]");
    private PrintWriterMassage printWriterMassage = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second);


        scrollView = (ScrollView) findViewById(R.id.scrollView);
        send = (Button) findViewById(R.id.send);
        textLayout = (LinearLayout) findViewById(R.id.textLayout);
        send.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.editText2);
        title = (TextView) findViewById(R.id.state2);

        out = Connection.getInstance().getOutStream();
        in = Connection.getInstance().getInStream();



        String massage = getIntent().getStringExtra("massage");
        if(massage != null){
           String massiv [] = massage.split("=")[1].trim().split(" ");
            title.setText("Chating with: " + massiv[0]);
            createMassage(massage,true);
        }
        if(in != null && out !=null ){
            printWriterMassage = new PrintWriterMassage();
            printWriterMassage.execute();

        }else{
            title.setText("Stream not created!");
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_second, menu);
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
    public void onClick(View v) {;
        String massage = editText.getText() +"";
        sendMassage(massage);
        createMassage(massage,false);
        editText.setText("");
    }

    public  void sendMassage(String massage){
        if(out != null){
            out.write(massage+"\n\r");
            out.flush();
        }
    }

    private TextView createMassage(String mass,boolean who){
        TextView textView = new TextView(this);
        textView.setTextSize(15);
        textView.setTextColor(Color.BLACK);
        textView.setText(format.format(new Date())+ "  " + mass);
        textView.setGravity(View.FOCUS_RIGHT);
        if(who == true){
            textView.setBackgroundColor(Color.YELLOW);
        }else{
            textView.setBackgroundColor(Color.LTGRAY);
        }
        textView.setShadowLayer(
                5f,   //float radius
                10f,  //float dx
                10f,  //float dy
                0xFFFFFF //int color
        );
        textLayout.addView(textView);
        return textView;
    }




    class PrintWriterMassage extends AsyncTask<Void, String, Void> {
        private boolean interupted = false;
        public void interupted() {
            this.interupted = !interupted;
        }
        public boolean isInterrupted() {
            return this.interupted;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
 /*           title.setText("doInBackground");*/
            while (!isInterrupted()) {
                if (in.hasNext()) {
                    String massage = in.nextLine();
                    publishProgress(massage);
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
            } else {
                createMassage(massage,true);
            }
            System.out.println(massage);
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
/*            disconnect();
            onDestroy();
            startActivity();*/
        }


        private void systemMasage(String massage) {
            System.out.println(massage);
            if (massage.split("=")[0].equals("%reciveFile")) {

            }

            /*  online = massage.split("=")[1].split(":");
                online = getOnlineMas();
                Connection.getInstance().setOnline(online);*/

            if (massage.split("=")[0].equals("%online")) {
                String online[] = massage.split("=")[1].split(":");
                Connection.getInstance().setOnline(online);
                online = getOnlineMas();
                Connection.getInstance().setOnline(online);
            }
            if (massage.split("=")[0].equals("%id")) {

            }
            if (massage.split("=")[0].equals("%disconnect!")) {
                interupted();
            }
            if (massage.split("=")[0].equals("%serverClose")) {
                back();
            }
        }

        public String [] getOnlineMas(){
            String [] tmp = new String[Connection.getInstance().getOnline().length-1];
            List<String> list = new ArrayList<String>();
            for(int i=0;i<Connection.getInstance().getOnline().length;i++){
                if(!Connection.getInstance().getOnline()[i].equals(Connection.getInstance().getID())){
                    list.add(Connection.getInstance().getOnline()[i]);
                }
            }
            for(int i=0;i<tmp.length;i++){
                tmp[i] = list.get(i);
            }
            return tmp;
        }

        public void disconnect() {
            sendMassage("%disconnect=" +Connection.getInstance().getID()+"\n\r");
        }
    }




    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void setScrollView(ScrollView scrollView) {
        this.scrollView = scrollView;
    }

    public void setSend(Button send) {
        this.send = send;
    }

    public void setTextLayout(LinearLayout textLayout) {
        this.textLayout = textLayout;
    }

    public static LinearLayout getTextLayout() {
        return textLayout;
    }

    public Button getSend() {
        return send;
    }

    @Override
    public void onBackPressed() {
        back();
    }



    private void back(){
        if(printWriterMassage != null){
            printWriterMassage.disconnect();
            printWriterMassage.interupted();
        }
        startActivity();
    }
    private void startActivity() {
        Intent intent = new Intent(this, StartPage.class);
        startActivity(intent);
    }
}
