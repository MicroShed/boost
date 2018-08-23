package io.openliberty.boost;

import org.w3c.dom.Document;
import io.openliberty.boost.BoosterPackConfigurator;

import io.openliberty.boost.BoosterPackConfigurator;

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
