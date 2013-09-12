package org.dna.mqtt.moquette.parser.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import static org.dna.mqtt.moquette.parser.netty.TestUtils.mockChannelHandler;
import org.dna.mqtt.moquette.proto.messages.AbstractMessage;
import org.dna.mqtt.moquette.proto.messages.UnsubscribeMessage;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.dna.mqtt.moquette.parser.netty.TestUtils.*;

/**
 *
 * @author andrea
 */
public class UnsubscribeEncoderTest {
    UnsubscribeEncoder m_encoder = new UnsubscribeEncoder();
    ChannelHandlerContext m_mockedContext;
    ByteBuf m_out;
    UnsubscribeMessage m_msg;
         
    @Before
    public void setUp() {
        //mock the ChannelHandlerContext to return an UnpooledAllocator
        m_mockedContext = mockChannelHandler();
        m_out = Unpooled.buffer();
        m_msg = new UnsubscribeMessage();
        m_msg.setMessageID(0xAABB);
    }
    
    @Test
    public void testEncodeWithSingleTopic() throws Exception {
        m_msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
        
        //variable part
        String topic1 = "/topic";
        m_msg.addTopic(topic1);

        //Exercise
        m_encoder.encode(m_mockedContext, m_msg, m_out);
        
        //Verify
        assertEquals((byte)0xA2, (byte)m_out.readByte()); //1 byte
        //2 messageID + 2 length + 6 chars = 10
        assertEquals(10, m_out.readByte()); //remaining length
        
        //verify M1ssageID
        assertEquals((byte)0xAA, m_out.readByte());
        assertEquals((byte)0xBB, m_out.readByte());
        
        //Variable part
        verifyString(topic1, m_out);
    }
    

    @Test
    public void testEncodeWithMultiTopic() throws Exception {
        m_msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
        
        //variable part
        String topic1 = "a/b";
        String topic2 = "a/b/c";
        m_msg.addTopic(topic1);
        m_msg.addTopic(topic2);

        //Exercise
        m_encoder.encode(m_mockedContext, m_msg, m_out);

        //Verify
        assertEquals((byte)0xA2, (byte)m_out.readByte()); //1 byte
        assertEquals(14, m_out.readByte()); //remaining length
        
        //verify M1ssageID
        assertEquals((byte)0xAA, m_out.readByte());
        assertEquals((byte)0xBB, m_out.readByte());
        
        //Variable part
        verifyString(topic1, m_out);
        verifyString(topic2, m_out);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEncode_empty_topics() throws Exception {
        m_msg.setQos(AbstractMessage.QOSType.LEAST_ONE);

        //Exercise
        m_encoder.encode(m_mockedContext, m_msg, m_out);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEncode_badQos() throws Exception {
        m_msg.setQos(AbstractMessage.QOSType.EXACTLY_ONCE);

        //Exercise
        m_encoder.encode(m_mockedContext, m_msg, m_out);
    }
}
