import socket

#define port number
port = 5000

#create and bind socket
my_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
my_socket.bind(('localhost', port))

#start listen
my_socket.listen()

try:

    client_socket, address = my_socket.accept()
    print(f"Connection from: {address}")

    data = client_socket.recv(1024)
    print(f"Received: {data.decode()}")

    client_socket.sendall(b'Hello, Client!')

    client_socket.close()

finally:
    my_socket.close()