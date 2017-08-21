package com.pandadentist.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by maya on 17/6/21.
 */
public class BLEProtoProcess {

    public static final int BLE_DATA_START = 0;
    public static final int BLE_DATA_RECEIVER = 1;
    public static final int BLE_DATA_END = 2;
    public static final int BLE_MISSED_RECEIVER = 3;
    public static final int BLE_MISSED_END = 4;
    public static final int BLE_NO_SYNC = 0xff;

    private Map<Integer, byte[]> buffer = Collections.synchronizedMap(new LinkedHashMap<Integer, byte[]>());
    private Vector<Integer> missIndex = null;
    private int missedCheckLimit = 0;

    private int software = -1;
    private int hardware = -1;
    private int factory = -1;
    private int model = -1;

    private int totalnum = 0;
    private int power = 0;
    private int time = 0;

    private StringBuffer mLog = new StringBuffer();

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
        if (bleProtoProcess.checkMissed()) {
            byte[] missedRequests = bleProtoProcess.getMissedRequests();
            System.out.println(Bytes.bytes2hexString(missedRequests)); //debug
        } else {
            byte[] completed = bleProtoProcess.getCompleted();
            System.out.println(Bytes.bytes2hexString(completed)); //debug
            String base64 = bleProtoProcess.getBuffer();
            System.out.println(base64);
        }
    }
/*
    //数据大小端转换
    public short _chg_(short  value)    {    return (value<<8)|(value>>8) ; }
    public int   _chg_(int    value)    {    return _chg_((short) (value<<16))  | _chg_((short)(value>>16)) ;   }
    public long  _chg_(long   value)    {    return _chg_((int) (value<<32))    | _chg_((int)(value>>32)) ;     }
*/

    //数据请求帧
    public byte[] getRequests() {
        //byte[] data = new byte[20];
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);

        Date date = new Date(System.currentTimeMillis());

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
        data.put((byte) date.getDate());
        data.put((byte) (date.getMonth()+1));                 //0 开始
        data.putShort((short)(date.getYear()-100 + 2000));  //1900开始

        //时间换算为从 1970-1-1 00:00:00 到现在的秒数
        data.putInt((int) (date.getTime()/1000 + 8*60*60));   //+时区 cn=8:00:00
        return data.array();
    }

    /**
     * 丢失帧重传请求帧
     *
     * @return 要发送的丢失请求帧数组，每次发送8个重传帧
     */
    public byte[] getMissedRequests() {
        mLog.append("---------------丢失帧请求开始-------------");
        int type = 0x10;

        int missedPackageCount = missIndex.size() > 8 ? 8 : missIndex.size();


        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        //ByteBuffer data = ByteBuffer.allocate(20);
        data.put((byte) type++);
        data.put((byte) missedPackageCount);
        data.putShort((short) missIndex.size());

        for (int index = 0; index < missedPackageCount; index++) {
            data.putShort((short) (int) missIndex.get(index));
            mLog.append((short) (int) missIndex.get(index));
            mLog.append("-");
        }

        return data.array();
    }

    /**
     * 接收完成帧
     */
    public byte[] getCompleted() {
        byte[] data = new byte[20];
        data[0] = (byte) 0xf0;
        this.missIndex = null;
        this.missedCheckLimit = 0;
        return data;
    }

    public String getBuffer() {
        System.out.println("total=" + totalnum * 16);
        ByteBuffer data = ByteBuffer.allocate(totalnum * 16).order(ByteOrder.LITTLE_ENDIAN);
        //byte[] data = new byte[totalnum * 16];
        Iterator it = this.buffer.entrySet().iterator();
        System.out.println("buffer size=" + this.buffer.size());
        System.out.println("buffer length=" + this.buffer.size() * 16);
        while(it.hasNext()) {
            Map.Entry<Integer, byte[]> entry = (Map.Entry<Integer, byte[]>) it.next();
            //System.arraycopy(entry.getValue(), 0, data, (entry.getKey() - 1) * 16, 16);
            data.put(entry.getValue());
        }
        return Bytes.bytes2base64(data.array());
    }

    public int interp(byte[] response) {
        ByteBuffer data = ByteBuffer.wrap(response).order(ByteOrder.LITTLE_ENDIAN);
        System.out.println(Bytes.bytes2hexString(data.array()));
        int type = data.get();//response[0];
        int pagenum = data.get();//response[1];
        int index = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 2, 2, ByteOrder.LITTLE_ENDIAN));

        StringBuilder logBuilder = new StringBuilder();

        switch (type) {
            case BLE_DATA_START:    //0
                software = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 4, 2, ByteOrder.LITTLE_ENDIAN));
                hardware = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 6, 2, ByteOrder.LITTLE_ENDIAN));
                factory = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 8, 2, ByteOrder.LITTLE_ENDIAN));
                model = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 10, 2, ByteOrder.LITTLE_ENDIAN));
                totalnum = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 12, 2, ByteOrder.LITTLE_ENDIAN));
                power = data.getShort();//Bytes.bytes2short(Bytes.copyOf(response, 14, 2, ByteOrder.LITTLE_ENDIAN));
                time = data.getInt();//Bytes.bytes2int(Bytes.copyOf(response, 16, 4, ByteOrder.LITTLE_ENDIAN));

                //总帧数的两倍作为检查的截断限制条件，如果超过则不再检查丢帧判定本次同步失败
                missedCheckLimit = totalnum * 2;

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

                this.buffer.clear();
                break;
            case BLE_DATA_RECEIVER:     //1
                logBuilder.append("data(");
                logBuilder.append(index);
                logBuilder.append(") :");
//                logBuilder.append(Bytes.bytes2hexString(Bytes.copyOf(response, 4, 16)));
                this.buffer.put(index, Bytes.copyOf(response, 4, 16));

                /**
                 * 检查是否处于丢帧重传中
                 * 如果处于丢帧重传中，每次获取到之后从丢帧列表中清除已经收到的帧
                 * 当丢帧列表为0时代表本次丢帧重传结束，返回BLE_MISSED_END
                 * 后续需要继续调用checkMissed检查是否存在其他丢帧
                 */
                if (missIndex != null) {
                    missIndex.remove(Integer.valueOf(index));

                    if (missIndex.size()<=1) {      //==0,最后一帧往往接不到
                        type = BLE_MISSED_END;
                    } else {
                        type = BLE_MISSED_RECEIVER;
                    }
                }
                break;
            case BLE_DATA_END:      //2 数据结束帧
                logBuilder.append("end");
                break;
            case BLE_NO_SYNC:       //0xff 没有数据
                logBuilder.append("no sync data");
                break;
        }
        mLog.append("log-->" + logBuilder.toString());
        System.out.println(logBuilder.toString());
        return type;
    }

    public boolean checkMissed() throws IllegalAccessException {
        /**
         * 检查丢帧次数限制不能超过总帧数的两倍
         * 如果超过则抛出IllegalAccessException异常，判定本次同步失败
         */
        if ((--missedCheckLimit) < 0) {
            throw new IllegalAccessException("out of check limit");
        }

        missIndex = new Vector<Integer>();

        for (int frame = 0; frame < totalnum; frame++) {
            if (!buffer.containsKey(frame)) {
                missIndex.addElement(frame);
                /**
                 * 丢帧重传请求每次最多只能请求8帧
                 * 这里不再全部遍历，当检测到需要重传8帧时中断
                 */
                if (missIndex.size() >= 8) {
                    break;
                }
            }
        }

        return missIndex.size() > 0 ? true : false;
    }

    public String getLog() {
        return mLog.toString();
    }

    public void clearLog() {
        mLog = new StringBuffer();
    }
}
