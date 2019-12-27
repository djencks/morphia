package dev.morphia.aggregation.experimental.codecs.stages;

import dev.morphia.aggregation.experimental.stages.Projection;
import dev.morphia.aggregation.experimental.stages.Projection.ProjectionField;
import dev.morphia.mapping.Mapper;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class ProjectionCodec extends StageCodec<Projection> {

    public ProjectionCodec(final Mapper mapper) {
        super(mapper);
    }

    @Override
    protected void encodeStage(final BsonWriter writer, final Projection value, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        for (final ProjectionField field : value.getFields()) {
            write(writer, field, encoderContext);
        }
        writer.writeEndDocument();
    }

    private void write(final BsonWriter writer, final ProjectionField field, final EncoderContext encoderContext) {
        writer.writeName(field.getName());
        Class aClass = field.getValue().getClass();
        Codec codec = getCodecRegistry().get(aClass);
        encoderContext.encodeWithChildContext(codec, writer, field.getValue());
    }

    @Override
    public Class<Projection> getEncoderClass() {
        return Projection.class;
    }
}
