package boost.runtimes.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.CDI_20;
import java.util.Map;
import io.openliberty.boost.common.boosters.CDIBoosterConfig;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import boost.runtimes.LibertyServerConfigGenerator;
import boost.runtimes.boosters.LibertyBoosterI;

public class LibertyCDIBoosterConfig extends CDIBoosterConfig implements LibertyBoosterI {

    public LibertyCDIBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    public String getFeature() {
        if (getVersion().equals(MP_20_VERSION)) {
            return CDI_20;
        }
        return null;
    }

    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {
        
    }
}