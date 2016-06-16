package centrale;

/**
 * @author Edwin
 *         Created on 6/16/2016
 */
class Constants {
//    Config
    static final String PROPERTIES_FILENAME = "centrale.props";
    static final int PORT = 1234;
    static final String IP = "127.0.0.1";
    static final String RMI_BINDNAME = IP + ":" + Constants.PORT + Constants.KEY_RMI_BINDNAME;

//    Keys
    static final String KEY_RMI_BINDNAME = "main/centrale";
    static final String KEY_IP = "ip";
    static final String KEY_PORT = "port";
}
