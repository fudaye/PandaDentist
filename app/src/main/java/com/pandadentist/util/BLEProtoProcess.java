package com.pandadentist.util;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by maya on 17/6/21.
 */
public class BLEProtoProcess {

    private static final String TAG = BLEProtoProcess.class.getSimpleName();

    private Map<Integer, byte[]> buffer = new ConcurrentHashMap<Integer, byte[]>();
    private Vector<Integer> missIndex = null;
    private int software;
    private int hardware;
    private int factory;
    private int model;

    private int totalnum;
    private int power;
    private int time;

    public static void main(String[] args) throws Exception {
        BLEProtoProcess bleProtoProcess = new BLEProtoProcess();

        List<byte[]> mock = new ArrayList<byte[]>();

        //数据起始帧
        mock.add(new byte[]{0X00, 0X00, 0x00, 0x00, 0x01, 0x00, 0x02, 0x00, 0x03, 0x00, 0x04, 0x00, 0x02, 0x00, 0x00, 0x00, (byte) 0Xe7, (byte) 0Xef, 0X5b, 0X68});

        //数据帧
        mock.add(new byte[]{0x01, 0x00, 0x01, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f});
        mock.add(new byte[]{0x01, 0x00, 0x02, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f});

        //结束帧
        mock.add(new byte[]{0x02, 0x00, 0x03, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f});

        //发情数据请求帧
        byte[] request = bleProtoProcess.getRequests();
        System.out.println(Bytes.bytes2hexString(request)); //debug

        /**
         * 从蓝牙接受数据后调用interp完成数据解析，每收到一次调用一次
         * 这里模拟接受到三帧，包含起始帧、两个数据帧、一个结束帧
         * 注视掉1或者2可以模拟丢帧
         * interp每次会返回TRUE或FLASE用以代表设备是否发送结束
         */
        bleProtoProcess.interp(mock.get(0));
        bleProtoProcess.interp(mock.get(1));
        bleProtoProcess.interp(mock.get(2));
        bleProtoProcess.interp(mock.get(3));

        /**
         * 检测是否丢帧，如果丢帧则调用getMissedRequests获取丢失帧重传请求发送给设备即可
         * 如果检测没有丢帧，则调用getCompleted得到接收完成帧发送给设备即可
         * 最后调用getBuffer获取到要发送给后台的BASE64编码后的数据通过HTTP POST即可
         */
        if(bleProtoProcess.checkMissed()) {
            List<byte[]> missedRequests = bleProtoProcess.getMissedRequests();
            for (byte[] missedRequest : missedRequests) {
                System.out.println(Bytes.bytes2hexString(missedRequest)); //debug
            }
        } else {
            byte[] completed = bleProtoProcess.getCompleted();
            System.out.println(Bytes.bytes2hexString(completed)); //debug
            String base64 = bleProtoProcess.getBuffer();
            System.out.println(base64);
        }
    }

    //数据请求帧
    public byte[] getRequests() {
        //byte[] data = new byte[20];
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);

        Date date = new Date();

        //data[0] = 0x0; // //1bytes 类型，固定值 0- 请求数据帧 1-请求结果(预留) 2-请求配置信息
        //data[1] = 0x0; //1bytes 保留
        data.put((byte) 0x0);
        data.put((byte) 0x0);
        data.putShort((short) 0); //2bytes 保留 2

        /**
         * 当前服务器时间用于校准牙刷时间
         */
        data.put((byte) date.getSeconds());
        data.put((byte) date.getMinutes());
        data.put((byte) date.getHours());
        data.put((byte) 0x0);
        data.put((byte) date.getDay());
        data.put((byte) date.getMonth());
        data.put((byte) date.getYear());

        //时间换算为从 1970-1-1 00:00:00 到现在的秒数
        data.put(Bytes.int2bytes((int) date.getTime()));
        return data.array();
    }

    /**
     * 丢失帧重传请求帧
     * @return 要发送的丢失请求帧数组，每次发送8个重传帧
     */
    public List<byte[]> getMissedRequests() {
        List<byte[]> missedList = new ArrayList<byte[]>();

        int type = 0x10;
        for(int i = 0; i < missIndex.size();) {
            //byte[] data = new byte[20];
            ByteBuffer data = ByteBuffer.allocate(20);
            int count = missIndex.size() - i > 8 ? 8 : missIndex.size() - i;
            data.put((byte) type++);
            data.put((byte) count);
            data.put((byte) missIndex.size());

            int offset = 3;
            for(;count > 0; count--) {
                //Bytes.short2bytes((short) (int) missIndex.get(i++), data, offset);
                data.putShort((short) (int) missIndex.get(i++));
                //offset += 2;
            }

            missedList.add(data.array());
        }

        return missedList;
    }

    /**
     * 接收完成帧
     */
    public byte[] getCompleted() {
        byte[] data = new byte[20];
        data[0] = (byte) 0xf0;
        return data;
    }

    public String getBuffer() {
        byte[] data = new byte[totalnum * 16];
        Iterator it = this.buffer.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Integer, byte[]> entry = (Map.Entry<Integer, byte[]>) it.next();
            System.arraycopy(entry.getValue(), 0, data, entry.getKey() * 16, 16);
        }
        return Bytes.bytes2base64(data);
    }

    public boolean interp(byte[] response) {
        ByteBuffer data = ByteBuffer.wrap(response).order(ByteOrder.LITTLE_ENDIAN);
        System.out.println(Bytes.bytes2hexString(data.array()));
        int type = data.get();//response[0];
        int pagenum = data.get();//response[1];
        int index = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 2, 2, ByteOrder.LITTLE_ENDIAN));

        StringBuilder logBuilder = new StringBuilder();

        switch (type) {
            case 0:
                software = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 4, 2, ByteOrder.LITTLE_ENDIAN));
                hardware= data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 6, 2, ByteOrder.LITTLE_ENDIAN));
                factory = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 8, 2, ByteOrder.LITTLE_ENDIAN));
                model = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 10, 2, ByteOrder.LITTLE_ENDIAN));
                totalnum = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 12, 2, ByteOrder.LITTLE_ENDIAN));
                power = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 14, 2, ByteOrder.LITTLE_ENDIAN));
                time = data.getInt();//Bytes.bytes2int(Bytes.copyOf(response, 16, 4, ByteOrder.LITTLE_ENDIAN));

                logBuilder.append("first index=");
                logBuilder.append(index);
                logBuilder.append(" pagenum=");
                logBuilder.append(pagenum);
                logBuilder.append(" type=");
                logBuilder.append(type);
                logBuilder.append("\n\tsoftware:");
                logBuilder.append(software);
                logBuilder.append("\n\thardware:");
                logBuilder.append(hardware);
                logBuilder.append("\n\tfactory:");
                logBuilder.append(factory);
                logBuilder.append("\n\tmodel:");
                logBuilder.append(model);
                logBuilder.append("\n\ttotalnum:");
                logBuilder.append(totalnum);
                logBuilder.append("\n\tpower:");
                logBuilder.append(power);
                logBuilder.append("\n\ttime:");
                logBuilder.append(time);
                Log.d(TAG,"logBuilder-->"+logBuilder.toString());
                this.buffer.clear();
                break;
            case 1:
                logBuilder.append("data(");
                logBuilder.append(index);
                logBuilder.append(") :");
                logBuilder.append(Bytes.bytes2hexString(Bytes.copyOf(response, 4, 16)));
                this.buffer.put(index, Bytes.copyOf(response, 4, 16));
                break;
            case 2:
                logBuilder.append("end");
                return true;
        }

        System.out.println(logBuilder.toString());
        return false;
    }

    public boolean checkMissed() {
        missIndex = new Vector<Integer>();

        for(int frame = 0; frame < totalnum; frame++) {
            if(!buffer.containsKey(frame)) {
                missIndex.addElement(frame);
            }
        }

        return missIndex.size() > 0 ? true : false;
    }
}
