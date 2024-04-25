package chat;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public Connection(Socket socket) {
       this.socket=socket;
        try {
            out=new ObjectOutputStream(socket.getOutputStream());
            in=new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void send(Message message) throws IOException{
        synchronized (out){
        out.writeObject(message);}
    }
    public Message receive() throws IOException, ClassNotFoundException{
        synchronized (in){
        return (Message) in.readObject();}
    }
    public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
    }
    public void close()  throws IOException{
        socket.close();
        out.close();
        in.close();
    }

}
