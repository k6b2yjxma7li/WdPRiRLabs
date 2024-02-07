import pika
import threading
import configparser

class RabbitChat:
    RABBITMQ_HOST = '127.0.0.1'
    RABBITMQ_PORT = 5672
    RABBITMQ_USERNAME = 'guest'
    RABBITMQ_PASSWORD = 'guest'
    CONFIG_FILE="rabbitchat.ini"

    def __init__(self, username, peername):
        self.username = username
        self.peername = peername
        self.__credentials__ = None

        self.__connection__ = None
        self.connection_init()

        self.__peer_connection__ = None
        self.__peer_channel__ = None
        self.receiver_thread = threading.Thread(
            target=self.setup_receiver
        )
        self.receiver_thread.start()

    def connection_init(self):
        self.config_load()
        self.__credentials__ = pika.PlainCredentials(
            RabbitChat.RABBITMQ_USERNAME,
            RabbitChat.RABBITMQ_PASSWORD
        )
        self.__connection__ = pika.BlockingConnection(
            pika.ConnectionParameters(
                host=RabbitChat.RABBITMQ_HOST,
                port=RabbitChat.RABBITMQ_PORT,
                credentials=self.__credentials__
            )
        )
        self.__channel__ = self.__connection__.channel()
        self.__channel__.queue_declare(queue=self.peername, durable=True)


    def config_load(self):
        config = configparser.ConfigParser()
        try:
            config.read(RabbitChat.CONFIG_FILE)
            config['RABBITMQ_SERVER']
        except KeyError:
            print("Setup chat server connection: ")
            RabbitChat.RABBITMQ_HOST = input("Server address: ")
            RabbitChat.RABBITMQ_USERNAME = input("Organization name: ")
            RabbitChat.RABBITMQ_PASSWORD = input("Organization password: ")
            with open(RabbitChat.CONFIG_FILE, "w") as inifile:
                print("[RABBITMQ_SERVER]", file=inifile)
                print(f"address = {RabbitChat.RABBITMQ_HOST}", file=inifile)
                print(
                    f"username = {RabbitChat.RABBITMQ_USERNAME}",
                    file=inifile
                )
                print(
                    f"password = {RabbitChat.RABBITMQ_PASSWORD}",
                    file=inifile
                )
            print("Success!")
        finally:
            config.read(RabbitChat.CONFIG_FILE)
            config['RABBITMQ_SERVER']
        RabbitChat.RABBITMQ_HOST = config["RABBITMQ_SERVER"]['address']
        RabbitChat.RABBITMQ_USERNAME = config["RABBITMQ_SERVER"]['username']
        RabbitChat.RABBITMQ_PASSWORD = config["RABBITMQ_SERVER"]['password']

    def setup_receiver(self):
        def get_message(channel, method, properties, body):
            print(f"{self.peername}: {body.decode()}")
            channel.basic_ack(delivery_tag=method.delivery_tag)

        peer_connection = pika.BlockingConnection(
            pika.ConnectionParameters(
                host=RabbitChat.RABBITMQ_HOST,
                port=RabbitChat.RABBITMQ_PORT,
                credentials=self.__credentials__
            )
        )
        self.__peer_channel__ = peer_connection.channel()
        self.__peer_channel__.queue_declare(queue=self.username, durable=True)
        self.__peer_channel__.basic_consume(
            queue=self.username,
            on_message_callback=get_message
        )
        print(f"Chat with {self.peername}. Say hi! Ctrl+C to exit.")
        try:
            self.__peer_channel__.start_consuming()
        except KeyboardInterrupt:
            print("Bye, bye!")

    def send(self, message):
        self.__channel__.basic_publish(
            exchange='',
            routing_key=self.peername,
            body=message
        )

    def close(self):
        self.receiver_thread.join()
    
    def is_open(self):
        return self.receiver_thread.is_alive()

    
if __name__ == '__main__':
    chat = None
    try:
        username = input("Your name: ")
        peername = input("Chat with: ")
        chat = RabbitChat(username, peername)
        # notify users that one of them is cooler than others
        while True:
            try:
                if chat.receiver_thread.is_alive():
                    chat.send(f"*using RabbitChat Python Edition*")
                    break
            except RuntimeError:
                break
        while True:
            try:
                if chat.receiver_thread.is_alive():
                    chat.send(input(f"{username}: "))
            except RuntimeError:
                break
    except KeyboardInterrupt:
        if chat is not None:
            chat.send("*Offline*")
        print("Exiting...")
