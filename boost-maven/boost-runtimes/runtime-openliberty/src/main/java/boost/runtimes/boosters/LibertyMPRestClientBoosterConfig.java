package boost.runtimes.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.MPRESTCLIENT_11;
import java.util.Map;
import io.openliberty.boost.common.boosters.MPRestClientBoosterConfig;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import boost.runtimes.LibertyServerConfigGenerator;
import boost.runtimes.boosters.LibertyBoosterI;

public class LibertyMPRestClientBoosterConfig extends MPRestClientBoosterConfig implements LibertyBoosterI {

    public LibertyMPRestClientBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    @Override
	public String getFeature() {
        if (getVersion().equals(MP_20_VERSION)) {
            return MPRESTCLIENT_11;
        }
        return null;
    }

    @Override
	public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {
        
    }
}