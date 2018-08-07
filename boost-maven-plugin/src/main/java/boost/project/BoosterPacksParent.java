package boost.project;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;

public class BoosterPacksParent {

	/**
	 * Creates a list of config writer objects for all boost dependencies found
	 */
	
	List<String> featureList;
	
	JDBCBoosterPackConfigurator jdbcConfig = null;
	private String JDBC_BOOSTER_PACK_STRING = "liberty-booster-data-jdbc";
	private String JAXRS_BOOSTER_PACK_STRING = "liberty-booster-jaxrs";
	private List<BoosterPackConfigurator> boosterPackConfigList = new ArrayList<BoosterPackConfigurator>();

	/**
	 * take a list of pom boost dependency strings and map to liberty features for config. 
	 * return a list of feature configuration objects for each found dependency.
	 * 
	 * @param dependencies
	 * @return
	 */
	public List<BoosterPackConfigurator> mapDependenciesToFeatureList(List<String> dependencies) {
					
		featureList = new ArrayList<String>();
		for(String dep: dependencies) {
			if (dep.equals(JDBC_BOOSTER_PACK_STRING)){			
				boosterPackConfigList.add(new JDBCBoosterPackConfigurator());
			} else if (dep.equals(JAXRS_BOOSTER_PACK_STRING)){			
				boosterPackConfigList.add(new JAXRSBoosterPackConfigurator());
			}
			
		}
		
		return boosterPackConfigList;
	}

	public void writeConfigForFeature(String feature) {
		
	}
}
