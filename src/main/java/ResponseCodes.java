public enum ResponseCodes {
    OK,
    RESPONSE_CODE_NOT_FOUND,
    FILE_NOT_FOUND,
    USER_NOT_FOUND,
    SEND_ECHO,
    GET_FILE,
    GET_MESSAGE_BACKLOG,
    SEND_MESSAGE_TO_BACKLOG;

    public static ResponseCodes getCode(int n) {
        return switch (n) {
            case 0 -> OK;
            case 1 -> SEND_ECHO;
            case 2 -> GET_FILE;
            case 3 -> GET_MESSAGE_BACKLOG;
            case 4 -> SEND_MESSAGE_TO_BACKLOG;
            case -1 -> RESPONSE_CODE_NOT_FOUND;
            case -2 -> FILE_NOT_FOUND;
            case -3 -> USER_NOT_FOUND;
            default -> throw new IllegalStateException("Tundmatu kood: " + n);
        };
    }

    public static ResponseCodes stringToCode(String s) {
        return switch (s) {
            case "echo" -> SEND_ECHO;
            case "file" -> GET_FILE;
            case "getsonum" -> GET_MESSAGE_BACKLOG;
            case "writesonum" -> SEND_MESSAGE_TO_BACKLOG;
            default -> throw new IllegalStateException("Tundmatu kood: " + s);
        };
    }
}
