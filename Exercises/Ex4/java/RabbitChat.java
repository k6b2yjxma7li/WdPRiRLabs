import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DeliverCallback;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class RabbitChat {
    public static class RabbitChit extends Thread {
        public static String chatName;
        public static String peerName;
        private static Connection connection;
        private static Channel channel;
        private static DeliverCallback printMessage;
        
        public RabbitChit(ConnectionFactory connectionFactory, String myChatName, String peerChatName) throws IOException, TimeoutException {
            chatName = myChatName;
            peerName = peerChatName;
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(
                chatName, 
                true, 
                false, 
                false, 
                null
            );
            printMessage = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(peerChatName+": " + message + "");
            };
        }

        @Override
        public void run() {
            System.out.println("Chat with "+peerName+". Say hi! Ctrl+C to exit.");
            try {
                channel.basicConsume(chatName, true, printMessage, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String configFilePath = "rabbitchat.config";
    public static Properties propertiesRabbitMQHost = new Properties();
    
    public static void loadConfig() throws IOException {
        FileInputStream configFileInputStream = new FileInputStream(configFilePath);
        propertiesRabbitMQHost.load(configFileInputStream);
        configFileInputStream.close();
    }


    public static void main(String[] argv) throws IOException, TimeoutException {
        loadConfig();
        Console console = System.console();
        String username = console.readLine("Your name: ");
        String peername = console.readLine("Chat with: ");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(propertiesRabbitMQHost.getProperty("RABBITMQ_SERVER_ADDRESS"));
        try (
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()
        ) {
            RabbitChit chat = new RabbitChit(factory, "Alice", "Bob");
            chat.start();
            channel.queueDeclare(
                peername, 
                true, 
                false, 
                false, 
                null
            );
            // Notify users that one of them is cooler than others
            channel.basicPublish(
                "", 
                peername, 
                null, 
                ("*is using RabbitChat Java Edition*").getBytes(StandardCharsets.UTF_8)
            );
            while(true){
                String message = console.readLine(username+": ");
                channel.basicPublish(
                    "", 
                    peername, 
                    null, 
                    message.getBytes(StandardCharsets.UTF_8)
                );
            }
        }
    }
}