package boost.runtimes.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.JSONP_11;
import java.util.Map;
import io.openliberty.boost.common.boosters.JSONPBoosterConfig;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import boost.runtimes.LibertyServerConfigGenerator;
import boost.runtimes.boosters.LibertyBoosterI;

public class LibertyJSONPBoosterConfig extends JSONPBoosterConfig implements LibertyBoosterI {

    public LibertyJSONPBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    @Override
	public String getFeature() {
        if (getVersion().equals(MP_20_VERSION)) {
            return JSONP_11;
        }
        return null;
    }

    @Override
	public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {
        
    }
}