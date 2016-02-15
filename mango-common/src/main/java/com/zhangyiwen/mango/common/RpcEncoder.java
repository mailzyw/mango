package com.zhangyiwen.mango.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC编码器
 * 将genericClass类型的Object,编码为byte[]流
 * byte[]流的前4个Byte对应一个Int，记录对象编码后的byte[]流长度
 * byte[]流的剩余内容对应对象编码后的内容
 * Created by zhangyiwen on 15/12/24.
 */
public class RpcEncoder extends MessageToByteEncoder{

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object in, ByteBuf out) throws Exception {
        if(genericClass.isInstance(in)){
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
