package zk_ui.config;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;

public class ConfigurationManager {
    private static final String CONFIG_XML_FILE_NAME = "config.xml";
    private Config config;
    private String configPath;
    private JAXBContext jaxbCtx;

    public ConfigurationManager() throws ConfigurationException {
        try {
            jaxbCtx = JAXBContext.newInstance("zk_ui.config");
            config = load();

            if (config == null) {
                config = new Config();
                config.setZkServerList(new ZkServerList());
                save(config, getClasspathRoot() + CONFIG_XML_FILE_NAME);
            }
        } catch (Exception e) {
            throw new ConfigurationException("Could not initialize configuration", e);
        }
    }

    public void addZkServer(String name, String host, String port) throws ConfigurationException {
        ZkServer zkServer = new ZkServer();
        zkServer.setName(name);
        zkServer.setHostName(host);
        zkServer.setPort(port);

        config.getZkServerList().getZkServer().add(zkServer);

        try {
            save();
        } catch (Exception e) {
            throw new ConfigurationException("Could not add ZkServer", e);
        }
    }

    public List<ZkServer> getZkServers() {
        return config.getZkServerList().getZkServer();
    }

    private void save() throws JAXBException, IOException {
        save(config, configPath);
    }

    private void save(Config config, String path) throws JAXBException, IOException {
        Marshaller marshaller = jaxbCtx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter writer = new StringWriter();
        marshaller.marshal(config, writer);
        String configString = writer.toString();
        System.out.println(configString);

        File tmpFile = new File(FileUtils.getTempDirectoryPath() + File.separator + CONFIG_XML_FILE_NAME  + "." +
                System.currentTimeMillis());
        FileUtils.write(tmpFile, configString);
        FileUtils.forceDelete(new File(path));
        FileUtils.moveFile(tmpFile, new File(path));
    }

    private Config load() throws JAXBException, IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(CONFIG_XML_FILE_NAME);
        if (url != null) {
            configPath = url.getFile();
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            return (Config) unmarshaller.unmarshal(new File(url.getFile()));
        }
        return null;
    }

    private String getClasspathRoot() throws IOException {
        Enumeration<URL> roots = Thread.currentThread().getContextClassLoader().getResources("");
        while (roots.hasMoreElements()) {
            String path = roots.nextElement().getFile();
            //should work for unit tests (/target/classes) and war (WEB-INF/classes)
            if (path.contains("classes")) {
                return path;
            }
        }
        throw new IOException("could not find classpath root");
    }

    public class ConfigurationException extends Exception {
        ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
