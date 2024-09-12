```
System Config

javac: 17.0.8
java: 17.0.8 LTS
```


# How to compile and run server/client programs

## Compile server program

### Linux/Mac/Windows
```
javac -cp snakeyaml.jar FileServer.java FileServerImpl.java Server.java Util.java ComputeServer.java ComputeServerImpl.java
```


## Run server program

### Linux/Mac
```
java -cp .:snakeyaml.jar Server
```

### Windows

```
java -cp ".;snakeyaml.jar" Server
```

## Compile client program

### Linux/Mac/Windows
```
javac -cp snakeyaml.jar DirectoryWatcher.java Client.java Util.java FileServer.java ComputeServer.java
```

## Run client program

### Linux/mac

#### Connect Client to File-Server Mode
```
java -cp .:snakeyaml.jar Client
```

#### Connect Client to Compute-Server Mode
```
java -cp .:snakeyaml.jar Client computeServer
```

### Windows

#### Connect Client to File-Server Mode
```
java -cp ".;snakeyaml.jar" Client
```

#### Connect Client to Compute-Server Mode
```
java -cp ".;snakeyaml.jar" Client computeServer
```

# How to test the program


```Client.java``` watches the ```client/``` folder for changes. It triggers when we create a file, modify a file or delete a file and send a rmi request to server to take necessary actions.

```Server.java``` watches the ```serv/``` folder and creates/modifies files when it gets a request from client.


<p>Client and Server config is managed by config.yaml</p>

```
Frequently Caused Problems

We observed that in windows operating system even when the Server and Client is terminated, the port may not be released. This can cause the Server/Client program to fail to start. In this case, please change the server port and client in config.yaml
```