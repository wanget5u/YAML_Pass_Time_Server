import java.util.*;
import java.util.concurrent.*;

public class Main {

  public static void main(String[] args) throws Exception
  {
    String fileName = "src/PassTimeServerOptions.yaml";
    Options options = Tools.createOptionsFromYaml(fileName);
    String host = options.getHost();
    int port = options.getPort();
    boolean concurrent =  options.isConcurMode();
    boolean showResult = options.isShowSendRes();
    Map<String, List<String>> clientRequests = options.getClientsMap();
    ExecutorService executorService = Executors.newCachedThreadPool();
    List<ClientTask> clientTasks = new ArrayList<>();
    List<String> clientLogs = new ArrayList<>();

    Server server = new Server(host, port);
    server.startServer();

    clientRequests.forEach((id, requestsList) ->
    {
      Client client = new Client(host, port, id);
      if (concurrent)
      {
        ClientTask ctask = ClientTask.create(client, requestsList, showResult);
        clientTasks.add(ctask);
        executorService.execute(ctask);
      }
      else
      {
        client.connect();
        client.send("login " + id);

        for (String req : requestsList)
        {
          String res = client.send(req);

          if (showResult)
          {System.out.println(res);}
        }
        String clog = client.send("bye and log transfer");
        System.out.println(clog);
      }
    });

    if (concurrent)
    {
      clientTasks.forEach(task ->
      {
        try
        {
          String log = task.get();
          clientLogs.add(log);
        }
        catch (InterruptedException | ExecutionException exception)
        {System.out.println(exception);}
      });
      clientLogs.forEach( System.out::println);
      executorService.shutdown();
    }
    server.stopServer();
    System.out.println("\n=== Server log ===");
    System.out.println(server.getServerLog());
  }
}