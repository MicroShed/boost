package boost.project;

import org.w3c.dom.Document;

public class JAXRSBoosterPackConfigurator implements BoosterPackConfigurator {

	@Override
	public String getFeatureString() {
		return "jaxrs-2.0";
	}

	@Override
	public void writeConfigToServerXML(Document doc) {
		// TODO Auto-generated method stub
		
	}

}
