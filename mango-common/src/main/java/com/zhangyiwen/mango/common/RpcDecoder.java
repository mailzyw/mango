package com.zhangyiwen.mango.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC解码器
 * 将序列化的byte流，解码成genericClass对应的对象
 * byte流中的前四个Byte对应一个Int,标示对象实际信息byte[] data的长度
 * byte流的剩余字节为对象实际信息byte[] data
 * Created by zhangyiwen on 15/12/24.
 */
public class RpcDecoder extends ByteToMessageDecoder{

    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes()<4){
            return;
        }
        in.markReaderIndex();   //设置读标记
        int dataLength = in.readInt();
        if(dataLength<0){
            channelHandlerContext.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();  //恢复至之前设置的读标记
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = SerializationUtil.deserialize(data,genericClass);
        out.add(obj);
    }
}
