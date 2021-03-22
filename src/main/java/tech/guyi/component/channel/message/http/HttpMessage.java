package tech.guyi.component.channel.message.http;

import lombok.Data;
import lombok.SneakyThrows;
import tech.guyi.component.channel.message.Message;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author e-Peng.Zhang2
 * @version 2021/3/22
 */
@Data
public class HttpMessage implements Serializable {

    private String uri;
    private String method;
    private Map<String,String> headers = new HashMap<>();
    private byte[] body;

    private Message message;

    @SneakyThrows
    public byte[] toBytes(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(out);
        outputStream.writeObject(this);
        outputStream.flush();
        byte[] bytes = out.toByteArray();
        outputStream.close();
        return bytes;
    }

    @SneakyThrows
    public static HttpMessage from(Message message){
        ByteArrayInputStream in = new ByteArrayInputStream(message.getContent());
        ObjectInputStream inputStream = new ObjectInputStream(in);
        HttpMessage httpMessage = (HttpMessage) inputStream.readObject();
        httpMessage.message = message;
        inputStream.close();
        return httpMessage;
    }

    public void reply(HttpMessageResponse response){
        this.message.reply(response.toBytes());
    }

}
