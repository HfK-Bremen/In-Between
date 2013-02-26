

package de.hfkbremen.echo.sketches;


import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;


public class Serial {

    public final SerialPort serialPort;

    public final OutputStream out;

    public InputStream in; // should be done like with out ?

    public Serial(SerialPort pSerialPort, OutputStream pOutputStream) {
        serialPort = pSerialPort;
        out = pOutputStream;

//           serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);


        in = null;
        try {
            in = serialPort.getInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
//        try {
//            OutputStream out = serialPort.getOutputStream();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

        (new Thread(new SerialReader(in))).start();
//        (new Thread(new SerialWriter(out))).start();
    }

    public void close() {
        serialPort.close(); // this seems to bail on my machine (d3)
    }

    public void write(final String pMessageString) {
        System.out.println("Writing \"" + pMessageString + "\" to " + serialPort.getName());

        try {
            out.write(pMessageString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Serial open(String defaultPort) {
        boolean portFound = false;
        final int mComPortIdentifier = CommPortIdentifier.PORT_SERIAL;
        final Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        final int BAUD = 115200;

        while (portList.hasMoreElements()) {
            final CommPortIdentifier portId = (CommPortIdentifier)portList.nextElement();
            System.out.println("Found port id: " + portId);

            if (portId.getPortType() == mComPortIdentifier) {
                System.out.println("Found CommPortIdentifier.");

                if (portId.getName().equals(defaultPort)) {
                    System.out.println("Found port " + defaultPort);

                    SerialPort serialPort;
                    OutputStream outputStream = null;

                    try {
                        serialPort = (SerialPort)portId.open("SimpleWrite", 2000);
                    } catch (PortInUseException e) {
                        System.out.println("Port in use.");
                        continue;
                    }

                    try {
                        outputStream = serialPort.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        serialPort.setSerialPortParams(BAUD,
                                                       SerialPort.DATABITS_8,
                                                       SerialPort.STOPBITS_1,
                                                       SerialPort.PARITY_NONE);
                    } catch (UnsupportedCommOperationException e) {
                        e.printStackTrace();
                    }


                    try {
                        serialPort.notifyOnOutputEmpty(true);
                    } catch (Exception e) {
                        System.out.println("Error setting event notification");
                        System.out.println(e.toString());
                        System.exit(-1);
                    }

                    return new Serial(serialPort, outputStream);
                }
            }
        }

        if (!portFound) {
            System.out.println("port " + defaultPort + " not found.");
        }
        return null;
    }

    private static String getPortTypeName(int portType) {
        switch (portType) {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    public static void listPorts() {
        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName() + " - " + getPortTypeName(portIdentifier.getPortType()));
        }
    }

    /**
     *
     */
    public static class SerialReader
            implements Runnable {

        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = this.in.read(buffer)) > -1) {
                    System.out.print("." + new String(buffer, 0, len));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SerialWriter
            implements Runnable {

        OutputStream out;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void run() {
            try {
                int c = 0;
                while ((c = System.in.read()) > -1) {
                    this.out.write(c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                SerialPort mSerialPort = (SerialPort)commPort;
                mSerialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                InputStream mIn = mSerialPort.getInputStream();
                OutputStream mOut = mSerialPort.getOutputStream();

                (new Thread(new SerialReader(mIn))).start();
                (new Thread(new SerialWriter(mOut))).start();

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }
}
