package org.noear.solon.serialization.jackson.xml.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import org.noear.solon.serialization.prop.JsonProps;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

/**
 * @author painter
 * @since 1.12
 * @since 2.8
 */
public class NullValueSerializerImpl extends JsonSerializer<Object> {
    private JsonProps jsonProps;
    public Class<?> type0;

    public NullValueSerializerImpl(JsonProps jsonProps) {
        this.jsonProps = jsonProps;
    }

    public NullValueSerializerImpl(JsonProps jsonProps, final JavaType type) {
        this.jsonProps = jsonProps;
        this.type0 = type == null ? Object.class : type.getRawClass();
    }

    @Override
    public void serialize(Object o, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        Class<?> type = type0;

        if (type == null) {
            try {
                if (gen.getCurrentValue() != null) {
                    String fieldName = gen.getOutputContext().getCurrentName();
                    Field field = gen.getCurrentValue().getClass().getDeclaredField(fieldName);
                    type = field.getType();
                }
            } catch (NoSuchFieldException e) {
            }
        }

        if (type != null) {
            if (jsonProps.nullStringAsEmpty && type == String.class) {
                gen.writeString("");
                return;
            }

            if (jsonProps.nullBoolAsFalse && type == Boolean.class) {
                if (jsonProps.boolAsInt) {
                    gen.writeNumber(0);
                } else {
                    gen.writeBoolean(false);
                }
                return;
            }

            if (jsonProps.nullNumberAsZero && Number.class.isAssignableFrom(type)) {
                if (jsonProps.longAsString && type == Long.class) {
                    gen.writeString("0");
                } else {
                    if (type == Long.class) {
                        gen.writeNumber(0L);
                    } else if (type == Double.class) {
                        gen.writeNumber(0D);
                    } else if (type == Float.class) {
                        gen.writeNumber(0F);
                    } else {
                        gen.writeNumber(0);
                    }
                }

                return;
            }

            if (jsonProps.nullArrayAsEmpty) {
                if (Collection.class.isAssignableFrom(type) || type.isArray()) {
                    gen.writeObject(Collections.emptyList()); //xml 空数组指定,必须这样写入
//                    gen.writeStartArray(); //xml不兼容
//                    gen.writeEndArray();  //xml不兼容
                    return;
                }
            }
        }

        gen.writeNull();
    }
}
