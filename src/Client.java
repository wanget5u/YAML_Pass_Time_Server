import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client
{
    private final String host;
    private final int port;
    private final String id;

    private SocketChannel socketChannel;

    public Client(String host, int port, String id)
    {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void connect()
    {
        try
        {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);
        }
        catch (IOException exception)
        {System.err.println("connect exception: " + exception.getMessage());}
    }

    public String send(String request)
    {
        if (socketChannel == null || !socketChannel.isConnected())
        {throw new IllegalStateException("send exception: Client is not connected to the server");}

        try
        {
            ByteBuffer buffer = ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8));

            while (buffer.hasRemaining())
            {socketChannel.write(buffer);}

            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            StringBuilder response = new StringBuilder();

            long lastDataTime = System.currentTimeMillis();
            final long timeout = 100;

            while (true)
            {
                int bytesRead = socketChannel.read(readBuffer);
                if (bytesRead > 0)
                {
                    lastDataTime = System.currentTimeMillis();
                    readBuffer.flip();
                    response.append(StandardCharsets.UTF_8.decode(readBuffer).toString().trim());
                    readBuffer.clear();
                }
                else if (bytesRead == 0)
                {
                    if (System.currentTimeMillis() - lastDataTime > timeout)
                    {break;}
                }
                else if (bytesRead == -1)
                {break;}
            }
            response.append("\n");

            return response.toString();
        }
        catch (IOException exception)
        {System.err.println("client send exception: " + exception.getMessage()); return "";}
    }

    public void close()
    {
        try
        {
            if (socketChannel != null && socketChannel.isOpen())
            {socketChannel.close();}
        }
        catch (IOException exception)
        {System.err.println("client close exception: " + exception.getMessage());}
    }

    public String getId()
    {return id;}
}
