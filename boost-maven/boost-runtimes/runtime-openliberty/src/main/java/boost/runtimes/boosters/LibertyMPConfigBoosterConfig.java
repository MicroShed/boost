package boost.runtimes.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.MPCONFIG_13;
import java.util.Map;
import io.openliberty.boost.common.boosters.MPConfigBoosterConfig;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import boost.runtimes.LibertyServerConfigGenerator;
import boost.runtimes.boosters.LibertyBoosterI;

public class LibertyMPConfigBoosterConfig extends MPConfigBoosterConfig implements LibertyBoosterI {

    public LibertyMPConfigBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    @Override
	public String getFeature() {
        if (getVersion().equals(MP_20_VERSION)) {
            return MPCONFIG_13;
        }
        return null;
    }

    @Override
	public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {
        
    }
}