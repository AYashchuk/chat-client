package com.example.admin.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by admin on 01.03.2015.
 *
 *  поскольку передавать обекты между активностями процес затруднительный
 *  то создаем клас контейнер который по шаблону одиночка, хранит потоки, для
 *  возможной передачи другой активности
 */
public class Connection {
    private String [] online =null;
    private Socket socket = null;
    private String ID = null;
    private static Connection instance = new Connection();
    private PrintWriter outStream = null;
    private Scanner inStream = null;


    public String[] getOnline() {
        return online;
    }

    public void setOnline(String[] online) {
        this.online = online;
    }

    public void setOutStream(PrintWriter outStream) {
        this.outStream = outStream;
    }

    public void setInStream(Scanner inStream) {
        this.inStream = inStream;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public static void setInstance(Connection instance) {
        Connection.instance = instance;
    }

    public Socket getSocket() {

        return socket;
    }

    public String getID() {
        return ID;
    }

    public PrintWriter getOutStream() {

        return outStream;

    }

    public Scanner getInStream() {
        return inStream;
    }

    public static Connection getInstance() {
        return instance;
    }

    private Connection(){

    }
    public void clearStream(){
        this.inStream = null;
        this.outStream = null;
        this.socket = null;
        this.ID = null;
        this.online = null;
    }
    public void close(){
        if(socket != null){
            try {
                inStream.reset();
                inStream.close();
                outStream.flush();
                outStream.close();
                socket.close();
                clearStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
