import java.util.*;

public class Options {

  private final String host;
  private final int port;
  private final boolean concurMode;
  private final boolean showSendRes;
  private final Map<String, List<String>> clientsMap;

  public Options(String host, int port, boolean concurMode, boolean showSendRes, 
                 Map<String, List<String>> clientsMap) {
    this.host = host;
    this.port = port;
    this.concurMode = concurMode;
    this.showSendRes = showSendRes;
    this.clientsMap = clientsMap;
  }

  public String getHost()
  {return host;}

  public int getPort()
  {return port;}

  public boolean isConcurMode()
  {return concurMode;}

  public boolean isShowSendRes()
  {return showSendRes;}

  public Map<String, List<String>> getClientsMap()
  {return clientsMap;}

  public String toString()
  {
    StringBuilder out = new StringBuilder();
    out.append(host).append(" ").append(port).append(" ").append(concurMode).append(" ").append(showSendRes).append("\n");

    for (Map.Entry<String, List<String>> e : clientsMap.entrySet())
    {out.append(e.getKey()).append(": ").append(e.getValue()).append("\n");}

    return out.toString();
  }
}
