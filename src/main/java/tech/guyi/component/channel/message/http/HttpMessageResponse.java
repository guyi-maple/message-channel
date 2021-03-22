package tech.guyi.component.channel.message.http;

import lombok.Data;
import lombok.SneakyThrows;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author e-Peng.Zhang2
 * @version 2021/3/22
 */
@Data
public class HttpMessageResponse implements Serializable {

    private byte[] body;
    private Map<String,String> headers;

    public HttpMessageResponse(byte[] body, Map<String, String> headers) {
        this.body = body;
        this.headers = Optional.ofNullable(headers)
                .orElseGet(HashMap::new);
    }

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
    public static HttpMessageResponse from(byte[] bytes){
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream inputStream = new ObjectInputStream(in);
        HttpMessageResponse message = (HttpMessageResponse) inputStream.readObject();
        inputStream.close();
        return message;
    }


}
