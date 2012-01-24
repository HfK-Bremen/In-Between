

package de.hfkbremen.echo.sketches;


import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import processing.core.PApplet;


public class SketchHelloRXTX
        extends PApplet {

    public void setup() {
        listPorts();
        openPort("/dev/tty.SLAB_USBtoUART");
    }

    static Enumeration portList;

    static CommPortIdentifier portId;

    static String messageString = "Hello, world!";

    static SerialPort serialPort;

    static OutputStream outputStream;

    static boolean outputBufferEmptyFlag = false;

    public void openPort(String defaultPort) {
        boolean portFound = false;
        final int mComPortIdentifier = CommPortIdentifier.PORT_SERIAL;

        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier)portList.nextElement();
            System.out.println("Found port id: " + portId);

            if (portId.getPortType() == mComPortIdentifier) {
                System.out.println("Found CommPortIdentifier.");

                if (portId.getName().equals(defaultPort)) {
                    System.out.println("Found port " + defaultPort);

                    portFound = true;

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
                        serialPort.setSerialPortParams(115200,
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


                    System.out.println("Writing \"" + messageString + "\" to " + serialPort.getName());

                    try {
                        outputStream.write(messageString.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(10000);  // Be sure data is xferred before closing
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    serialPort.close(); // this seems to bail on my machine (d3)
                    System.exit(1);
                }
            }
        }

        if (!portFound) {
            System.out.println("port " + defaultPort + " not found.");
        }
    }

    private void listPorts() {
        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName() + " - " + getPortTypeName(portIdentifier.getPortType()));
        }
    }

    private String getPortTypeName(int portType) {
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

    public static void main(String[] args) {
        PApplet.main(new String[] {SketchHelloRXTX.class.getName()});
    }
}
