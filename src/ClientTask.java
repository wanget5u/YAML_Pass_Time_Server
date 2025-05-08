import java.util.List;
import java.util.concurrent.FutureTask;

public class ClientTask extends FutureTask<String>
{
    public ClientTask(Client client, List<String> requests, boolean showSendRes)
    {
        super(() ->
        {
            StringBuilder clientLog = new StringBuilder();

            try
            {
                client.connect();
                client.send("login " + client.getId());

                clientLog.append("=== ").append(client.getId()).append(" log start ===").append("\n").append("logged in").append("\n");

                for (String request : requests)
                {
                    String response = client.send(request);

                    clientLog.append("Request: ").append(request).append("\n").append("Result:").append("\n").append(response);

                    if (showSendRes)
                    {System.out.println("[" + client.getId() + "] sent request for \"" + request + "\"\n" + response);}
                }
                client.send("bye and log transfer");

                clientLog.append("logged out").append("\n").append("=== ").append(client.getId()).append(" log end ===").append("\n");
            }
            catch (Exception exception)
            {System.err.println("ClientTask for " + client.getId() + " failed: " + exception.getMessage());}
            finally
            {client.close();}

            return clientLog.toString();
        });
    }
    public static ClientTask create(Client client, List<String> requests, boolean showSendRes)
    {return new ClientTask(client, requests, showSendRes);}
}