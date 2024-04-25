package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private volatile static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();


    public static void main(String[] args) throws IOException {
        int port = ConsoleHelper.readInt();
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("Сервер запущен");
            while (true) {

                new Handler(socket.accept()).start();

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> ob : connectionMap.entrySet()) {
            try {
                ob.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Не смогли отправить сообщение");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> ob : connectionMap.entrySet()) {
                String name = ob.getKey();
                if (name.equals(userName)) {
                } else
                    connection.send(new Message(MessageType.USER_ADDED, name));
            }
        }

        public void run() {
            ConsoleHelper.writeMessage("Установленно новое соединение с " + socket.getRemoteSocketAddress());
            String name = null;
            try (Connection connection = new Connection(socket)) {

                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("произошла ошибка при обмене данными с удаленным адресом");
            }
            if (name != null) {
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
            }
            ConsoleHelper.writeMessage("Cоединение с удаленным адресом закрыто");

        }


        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(userName + ": " + message.getData());
                    sendBroadcastMessage(new Message(MessageType.TEXT, stringBuilder.toString()));
                } else {
                    ConsoleHelper.writeMessage("Не смогли отправить сообщение \" + connection.getRemoteSocketAddress()");
                }
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if (message.getType() != MessageType.USER_NAME) {
                    ConsoleHelper.writeMessage("Получено сообщение от "
                            + socket.getRemoteSocketAddress() +
                            ". Тип сообщения не соответствует протоколу.");
                    continue;
                }

                String name = message.getData();
                if (name.isEmpty()) {
                    ConsoleHelper.writeMessage("Попытка подключения к серверу " +
                            "с пустым именем от " + socket.getRemoteSocketAddress());
                    continue;
                }
                if (connectionMap.containsKey(name)) {
                    ConsoleHelper.writeMessage("Попытка подключения к серверу с уже " +
                            "используемым именем от " + socket.getRemoteSocketAddress());
                    continue;
                }
                connectionMap.put(name, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return name;

            }
        }

    }
}

