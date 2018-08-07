package boost.project;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JDBCBoosterPackConfigurator implements BoosterPackConfigurator {

	private String jdbcDefault = "jdbc-4.1";

	/**
	 * retrieves the default boost feature string for the jdbc dependency
	 */
	public String getFeatureString() {
		System.out.println("AJM: getting default string for jdbc booster");
		return jdbcDefault;
	}

	/**
	 * writes out jdbc default config data when selected by the presence of a 
	 * jdbc boost dependency 
	 */
	public void writeConfigToServerXML(Document doc) {
		
		System.out.println("AJM: creating config for jdbc ");
		
		Element serverRoot;
		// find the root server element
	    NodeList list = doc.getChildNodes();
	    for (int i = 0; i < list.getLength(); i++) {
	      if (list.item(i) instanceof Element) {
	        serverRoot = (Element) list.item(i);
	        break;
	      }
	    }
	    serverRoot = doc.getDocumentElement();
		
		Element jdbcDriver = doc.createElement("jdbcDriver");
		jdbcDriver.setAttribute("id", "DerbyEmbedded");
		jdbcDriver.setAttribute("libraryRef", "DerbyLib");
		serverRoot.appendChild(jdbcDriver);
		
		Element dataSource = doc.createElement("datasource");
		dataSource.setAttribute("databaseName", "${server.output.dir}/DefaultDB");
		dataSource.setAttribute("id", "default_ds_id");
		dataSource.setAttribute("jndiName", "jndi/DefaultDB");
		dataSource.setAttribute("jdbcDriverRef", "DerbyEmbedded");
		serverRoot.appendChild(dataSource);
		
		
		Element lib = doc.createElement("library");
		Element fileLoc = doc.createElement("fileset");
		fileLoc.setAttribute("dir", "${shared.resource.dir}");
		fileLoc.setAttribute("includes", "derby-10.13.1.1.jar");
		lib.appendChild(fileLoc);
		serverRoot.appendChild(lib);		
	}
}