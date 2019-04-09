package io.openliberty.boost.common.runtimes;

import io.openliberty.boost.common.BoostException;

public abstract interface RuntimeI {
    
    public void doPackage() throws BoostException;
    
    public void doDebug(boolean clean) throws BoostException;
    
    public void doRun(boolean clean) throws BoostException;
    
    public void doStart(boolean clean, int verifyTimeout, int serverStartTimeout) throws BoostException;
    
    public void doStop() throws BoostException;

}
