import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class Tools
{
    public static Options createOptionsFromYaml(String fileName) throws IOException
    {
        InputStream inputStream = Files.newInputStream(new File(fileName).toPath());
        Yaml yaml = new Yaml();
        Map<String, Object> optionsMap = yaml.load(inputStream);

        return new Options(
                (String) optionsMap.get("host"),
                (int) optionsMap.get("port"),
                (boolean) optionsMap.get("concurMode"),
                (boolean) optionsMap.get("showSendRes"),
                (Map<String, List<String>>) optionsMap.get("clientsMap"));
    }
}
