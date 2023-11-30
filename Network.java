//Assignment 2 – Synchronization
//Operating System

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Network {


    public static void main(String[] args)  {
        int  numberOfConnections, numberOfDevices;
        ArrayList<Device> devices = new ArrayList<>();

        Scanner input = new Scanner(System.in);

        System.out.println("What is number of WI-FI Connections?");
        numberOfConnections = input.nextInt();


        System.out.println("What is number of devices Clients want to connect?");
        numberOfDevices = input.nextInt();
        Router router = new Router(numberOfConnections);


        for (int i = 0; i < numberOfDevices; i++) {
            Device newDevice = new Device(input.next(),input.next(), router);
            devices.add(newDevice);
        }

        for (Device device : devices) {
            device.start();
            //    sleep(100);
        }
    }

    public static void print(String s) {
        try {
            System.out.println(s);
            FileOutputStream fos = new FileOutputStream("output.txt", true);
            fos.write((s+ "\n").getBytes() );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

class semaphore {
    public static   int value  ;


    public semaphore(int value) {
        semaphore.value = value;
    }
    public synchronized void P(Device device) {
        if (value > 0 ){
          //  System.out.println(device.getN() + " arrived");
            Network.print("(" + device.getN() + ") (" + device.getType() + ") arrived");
        }
        else {
           // System.out.println("(" + device.getN() + ") (" + device.getType() + ") arrived");
            Network.print("(" + device.getN() + ") (" + device.getType() + ") arrived and waiting");

        }

        value-- ;
        if (value < 0)
            try { wait() ;
            } catch( InterruptedException e ) {
                throw new RuntimeException(e);
            }
    }


    public synchronized void V(Device device) {
        value++;
        if (value <= 0) {
            notify();
        }
    }



}

class Router {
    private final semaphore semaphore;
    private final List<Device> connectedDevices;
    private int connectionCount = 0;

    private final int maxCount;

    public Router(int maxNumberOfConnection) {
        semaphore = new semaphore(maxNumberOfConnection);
        connectedDevices = new LinkedList<>();
        maxCount = maxNumberOfConnection;
    }

    public void occupyConnection(Device device) throws InterruptedException {
        semaphore.P(device);

        synchronized (this) {
            while (connectionCount >= maxCount) {
                // If all connection links are occupied, wait for a release
                wait();
            }

            connectionCount++;
            device.setConnectionID(connectionCount);

           // System.out.println("Connection " + connectionCount + ": " + device.getN() + " Occupied");
            Network.print("Connection " + connectionCount + ": " + device.getN() + " Occupied");

        }
    }

    public synchronized void releaseConnection(Device device) {
        if (connectionCount > 0) {
            connectedDevices.remove(device);
            semaphore.V(device);
          //  System.out.println("Connection " + connectionCount + ": " + device.getN() + " Logged out");
            Network.print("Connection " + connectionCount + ": " + device.getN() + " Logged out");
            device.setConnectionID(connectionCount);
            connectionCount--;

            notifyAll();
        }
    }


}

class Device extends Thread {
    private final String name;
    private final String type;
    private final Router router;
    private int connectionID;

    public Device(String name, String type, Router router) {
        this.name = name;
        this.type = type;
        this.router = router;
    }

    public String getN() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }

    @Override
    public void run() {
        try {
            router.occupyConnection(this);

           // System.out.println("Connection " + getConnectionID() + ": " + name + " login");
            Network.print("Connection " + getConnectionID() + ": " + name + " login");

         //   System.out.println("Connection " + getConnectionID() + ": 2" + name + " performs online activity");
            Network.print("Connection " + getConnectionID() + ": " + name + " performs online activity");

            Thread.sleep(1000);

            router.releaseConnection(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
