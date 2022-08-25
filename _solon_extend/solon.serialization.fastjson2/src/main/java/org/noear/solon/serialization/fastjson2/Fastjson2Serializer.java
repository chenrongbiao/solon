package org.noear.solon.serialization.fastjson2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.noear.solon.serialization.StringSerializer;

import java.io.IOException;

/**
 * @author noear
 * @since 1.9
 */
public class Fastjson2Serializer implements StringSerializer {
    ObjectWriterProvider config;
    JSONWriter.Feature[] features;

    protected Fastjson2Serializer(ObjectWriterProvider config, JSONWriter.Feature... features) {
        this.config = config;
        this.features = features;
    }

    @Override
    public String serialize(Object obj) throws IOException {
        if (config == null) {
            return JSON.toJSONString(obj, features);
        } else {
            return toString(obj);
        }
    }

    private String toString(Object object) {
        JSONWriter.Context context = new JSONWriter.Context(config, features);
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        } catch (NullPointerException | NumberFormatException e) {
            throw new JSONException("JSON#toJSONString cannot serialize '" + object + "'", e);
        }
    }
}
