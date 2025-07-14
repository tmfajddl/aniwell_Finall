package com.example.RSW.arduino;

import com.fazecast.jSerialComm.SerialPort;

public class SerialReader {
    private static String latestData = "0";  // 최신 데이터 저장

    public static String getLatestData() {
        return latestData;
    }

    public static void start() {
        SerialPort comPort = SerialPort.getCommPorts()[0]; // 첫 번째 포트 선택
        comPort.setBaudRate(9600);
        comPort.openPort();

        new Thread(() -> {
            while (true) {
                if (comPort.bytesAvailable() > 0) {
                    byte[] buffer = new byte[comPort.bytesAvailable()];
                    comPort.readBytes(buffer, buffer.length);
                    latestData = new String(buffer).trim();
                    System.out.println("📡 아두이노 데이터 수신: " + latestData);
                }
                try {
                    Thread.sleep(500);  // 너무 빠르지 않게
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
