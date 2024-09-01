compile server program

```
javac -cp snakeyaml.jar FileServer.java FileServerImpl.java Server.java Util.java ComputeServer.java ComputeServerImpl.java
```

Run server program

```
java -cp .:snakeyaml.jar Server
```


compile client program

```
javac -cp snakeyaml.jar DirectoryWatcher.java Client.java Util.java FileServer.java ComputeServer.java
```

Run client program

```
java -cp .:snakeyaml.jar Client
```