package io.openliberty.boost;

import org.w3c.dom.Document;
import io.openliberty.boost.BoosterPackConfigurator;

public class JAXRSBoosterPackConfigurator implements BoosterPackConfigurator {

	//default to the EE8 feature
	String featureGAV = "jaxrs-2.1";
	
    @Override
    public String getFeatureString() {
        return featureGAV;
    }

    @Override
    public void writeConfigToServerXML(Document doc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFeatureString(String featureStr){
    	// if it is the 1.0 version = EE7 feature level
    	if (featureStr.equals(BoosterPacksParent.JAXRS_BOOSTER_PACK_STRING_10 ))
    		featureGAV = "jaxrs-2.0";
    }
}
