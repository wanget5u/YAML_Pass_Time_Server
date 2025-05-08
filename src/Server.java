import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable
{
    private final String host;
    private final int port;
    private boolean isRunning = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Map<String, List<String>> clientLogs = new ConcurrentHashMap<>();
    private final Map<SocketChannel, String> activeClients = new ConcurrentHashMap<>();
    private final StringBuilder serverLog = new StringBuilder();

    public Server(String host, int port)
    {
        this.host = host;
        this.port = port;
    }
    public void startServer()
    {
        if (isRunning) return;
        isRunning = true;

        executor.submit(this);
    }
    public void stopServer()
    {
        isRunning = false;
        executor.shutdown();
    }
    public String getServerLog()
    {return serverLog.toString();}

    @Override
    public void run()
    {
        try
        (
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            Selector selector = Selector.open()
        )
        {
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (isRunning)
            {
                selector.select(200);

                for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext();)
                {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    if (selectionKey.isAcceptable())
                    {
                        SocketChannel clientSocketChannel = serverSocketChannel.accept();
                        clientSocketChannel.configureBlocking(false);
                        clientSocketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    if (selectionKey.isReadable())
                    {
                        SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int bytesRead = clientChannel.read(buffer);

                        if (bytesRead == -1)
                        {
                            String clientName = activeClients.remove(clientChannel);
                            if (clientName != null)
                            {clientLogs.remove(clientName);}

                            selectionKey.cancel();
                            clientChannel.shutdownOutput();
                            clientChannel.close();
                            continue;
                        }

                        buffer.flip();
                        String clientRequest = StandardCharsets.UTF_8.decode(buffer).toString();
                        buffer.clear();

                        handleClientRequest(clientChannel, clientRequest);
                    }
                }
            }
        }
        catch (IOException exception)
        {System.err.println("run exception: " + exception.getMessage());}
    }

    private void handleClientRequest(SocketChannel clientChannel, String request)
    {
        request = request.trim();
        LocalTime localTime = LocalTime.now();

        try
        {
            String response = "";
            if (request.startsWith("login"))
            {
                String clientName = request.substring(6).trim();

                activeClients.putIfAbsent(clientChannel, clientName);
                clientLogs.putIfAbsent(clientName, new ArrayList<>());

                clientLogs.get(clientName).add("logged in");

                serverLog.append(clientName).append(" logged in at ").append(localTime).append("\n");
            }
            else if (request.equals("bye"))
            {
                String clientName = activeClients.get(clientChannel);

                if (clientName != null)
                {
                    clientLogs.remove(clientName);
                    activeClients.remove(clientChannel);

                    serverLog.append(clientName).append(" logged out at ").append(localTime).append("\n");
                }

                clientChannel.shutdownOutput();
                clientChannel.close();
                return;
            }
            else if (request.equals("bye and log transfer"))
            {
                String clientName = activeClients.get(clientChannel);

                if (clientName == null)
                {
                    clientChannel.shutdownOutput();
                    clientChannel.close();
                    return;
                }

                clientLogs.get(clientName).add("logged out");

                List<String> clientLogList = clientLogs.getOrDefault(clientName, Collections.emptyList());

                StringBuilder logBuilder = new StringBuilder();

                logBuilder.append("\n=== ").append(clientName).append(" log start ===\n");
                for (String logEntry : clientLogList)
                {logBuilder.append(logEntry).append("\n");}
                logBuilder.append("=== ").append(clientName).append(" log end ===\n");

                activeClients.remove(clientChannel);

                serverLog.append(clientName).append(" logged out at ").append(localTime).append("\n");
                ByteBuffer responseBuffer = ByteBuffer.wrap(logBuilder.toString().getBytes(StandardCharsets.UTF_8));

                while (responseBuffer.hasRemaining())
                {clientChannel.write(responseBuffer);}

                clientChannel.shutdownOutput();
                clientChannel.close();
                return;
            }
            else
            {
                String[] dates = request.split("\\s+");

                if (dates.length == 2)
                {
                    String clientName = activeClients.get(clientChannel);

                    if (clientName == null)
                    {
                        clientChannel.shutdownOutput();
                        clientChannel.close();
                        return;
                    }

                    String result = Time.passed(dates[0], dates[1]);

                    clientLogs.get(clientName).add("Request: " + request + "\nResult: \n" + result);
                    serverLog.append(clientName).append(" request at ").append(localTime).append(": ").append("\"").append(request).append("\"").append("\n");

                    response = result;
                }
            }

            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
            clientChannel.write(responseBuffer);
        }
        catch (IOException exception)
        {System.err.println("handleClientRequest exception: " + exception.getMessage());}
    }
}
