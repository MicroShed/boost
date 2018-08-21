package boost.project;

public class BoostException extends Exception {

    private static final long serialVersionUID = 1L;

    public BoostException(String message) {
        super(message);
    }
    
    public BoostException(String message, Throwable e) {
        super(message, e);
    }
    
    public BoostException(Throwable e) {
        super(e);
}
    
}
