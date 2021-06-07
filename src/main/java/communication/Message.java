package communication;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

//todo: Message nur noch per referenz aufrufen
public class Message implements Serializable {
    private static final long serialVersionUID = 7829136421241571165L;
    @Setter
    @Getter
    private String TYPE;  //for client: REGISTER, MESSAGE // for server: ASK_PUBLIC_KEY, OK, ERROR
    @Setter
    @Getter
    private String id;
    @Setter
    @Getter
    private String firstName;
    @Setter
    @Getter
    private String lastName;
    @Setter
    @Getter
    private String publicKey;
    @Setter
    @Getter
    private String messageText;

     void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        TYPE = aInputStream.readUTF();
        id = aInputStream.readUTF();
        firstName = aInputStream.readUTF();
        lastName = aInputStream.readUTF();
        publicKey = aInputStream.readUTF();
        messageText= aInputStream.readUTF();
    }

     void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.writeUTF(TYPE);
        aOutputStream.writeUTF(id);
        aOutputStream.writeUTF(firstName);
        aOutputStream.writeUTF(lastName);
        aOutputStream.writeUTF(publicKey);
        aOutputStream.writeUTF(messageText);
    }

}
