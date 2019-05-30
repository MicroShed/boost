package boost.runtimes.boosters;

import boost.runtimes.LibertyServerConfigGenerator;
import io.openliberty.boost.common.BoostException;

public interface LibertyBoosterI {

    public String getFeature();
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) throws BoostException;
}