import org.bytedeco.javacv.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class RtmpViewer {

    private static final String STREAM_URL = "rtmp://192.168.0.105/live/stream";
    private static final String RPI_IP = "192.168.0.105";  // <-- Raspberry Pi IP
    private static final int UDP_PORT = 5005;

    public static void main(String[] args) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(STREAM_URL)) {
            grabber.start();

            CanvasFrame canvas = new CanvasFrame("RTMP Stream", CanvasFrame.getDefaultGamma() / grabber.getGamma());
            canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Register key listener on AWT canvas
            java.awt.Canvas awtCanvas = canvas.getCanvas();
            awtCanvas.setFocusable(true);
            awtCanvas.requestFocus();

            awtCanvas.addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> sendUdpMessage("UP");
            case KeyEvent.VK_DOWN -> sendUdpMessage("DOWN");
            case KeyEvent.VK_LEFT -> sendUdpMessage("LEFT");
            case KeyEvent.VK_RIGHT -> sendUdpMessage("RIGHT");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Stop only if movement keys were released
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> sendUdpMessage("STOP");
        }
    }
});


            org.bytedeco.javacv.Frame frame;
            while ((frame = grabber.grab()) != null && canvas.isVisible()) {
                canvas.showImage(frame);
            }

            grabber.stop();
            canvas.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendUdpMessage(String message) {
        try {
            System.out.println("Sent: " + message);
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(RPI_IP);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, UDP_PORT);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

