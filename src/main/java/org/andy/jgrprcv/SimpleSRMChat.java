package org.andy.jgrprcv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class SimpleSRMChat extends ReceiverAdapter {
	
    JChannel channel;
    String user_name=System.getProperty("user.name", "n/a");
    Address myAddress;
    private final String appName;
    static FileWriter simpleLogOS;
    ArrayBlockingQueue<String> blockQ = new ArrayBlockingQueue<>(1);
    
    public SimpleSRMChat() {
        this.appName = "SRM";
    }

    private void start() throws Exception {
        channel=new JChannel(); // use the default config, udp.xml
        channel.setReceiver(this);
        channel.connect("ChatCluster");
        myAddress = channel.getAddress();
        eventLoop();
        channel.close();
    }

    public static void main(String[] args) throws Exception {
        
        simpleLogOS = new FileWriter(new File("//home//andy//logCollect//srm//SRMLogs.txt"));
        
        simpleLogOS.write("I AM SRM");
        
        new SimpleSRMChat().start();
        
    }
    
    private void eventLoop() {
        
        while(true) {
            try {
                System.out.print("> "); 
                System.out.flush();
                String msgFromBM = blockQ.take();
                Thread.sleep(1000L);
                String line = "SRM responding to msg->" + msgFromBM;
                System.out.println("App " + appName + " Sending line " + line);
                simpleLogOS.write("App " + appName + " Sending line " + line + "\n");
                simpleLogOS.flush();
                line="[" + user_name + "] " + line;
                Message msg=new Message(null, line);
                channel.send(msg);
                
            }
            catch(Exception e) {
            }
        }
    }
    
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
        
        try {
            if(simpleLogOS != null) {
                simpleLogOS.write("** view: " + new_view + "\n");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void receive(Message msg) {
        if(!msg.getSrc().equals(myAddress)) {
            System.out.println("App->" + appName + " received message: " + msg.getSrc() + ": " + msg.getObject());
            try {
                if(simpleLogOS != null) {
                    simpleLogOS.write("App->" + appName + " received message: " + msg.getSrc() + ": " + msg.getObject() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            blockQ.add(msg.getSrc() + ": " + msg.getObject());
        }
    }
}