package communication;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

//todo: Message nur noch per referenz aufrufen
public class Message implements Serializable {
    private static final long serialVersionUID = 7829136421241571165L;
    @Setter
    @Getter
    private String TYPE;  //for client: REGISTER, MESSAGE, CLOSE_CONNECTION // for server: ASK_PUBLIC_KEY, OK, ERROR
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
    @Getter
    @Setter
    private int message_ID;

    @Override
    public String toString(){
         return "Message: "+TYPE;
    }

}
