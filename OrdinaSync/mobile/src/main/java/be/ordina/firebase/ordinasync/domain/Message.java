package be.ordina.firebase.ordinasync.domain;

/**
 * Created by fbousson on 16/11/14.
 */
public class Message {

    private final String key, text;

    public Message(String key, String text) {
        this.key = key;
        this.text = text;
    }

    public String getKey() {
        return key;
    }

    public String getText() {
        return text;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (key != null ? !key.equals(message.key) : message.key != null) return false;
        if (text != null ? !text.equals(message.text) : message.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "key='" + key + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
