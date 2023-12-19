package choral.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import choral.runtime.Serializers.ChoralSerializer;

public class JavaSerializer implements ChoralSerializer< Serializable, ByteBuffer > {
    
    @Override
	public < M extends Serializable > ByteBuffer fromObject( M obj ) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);

			// Serialize the object
			objectOutputStream.writeObject(obj);
			objectOutputStream.flush();
			ByteBuffer buffer = ByteBuffer.wrap(byteStream.toByteArray());

			// Close the streams
			objectOutputStream.close();
			byteStream.close();

			return buffer;
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
    }

    @Override
	public < M extends Serializable > M toObject( ByteBuffer buffer ) {
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer.array());
			ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);

			// Use the ObjectInputStream to deserialize the object
			Object deserializedObject = objectInputStream.readObject();

			// Close the streams
			objectInputStream.close();
			byteStream.close();

			return deserializedObject;
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
    }

}
