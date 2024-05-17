public enum ResponseCodes {
    OK,
    RESPONSE_CODE_NOT_FOUND,
    FILE_NOT_FOUND,
    USER_NOT_FOUND,
    FILE_WRITING_ERROR,
    CREATE_USER,
    CHECK_USER,
    USER_TAKEN,
    FALSE_PASSWORD,
    SEND_ECHO,
    GET_FILE,
    GET_MESSAGE_BACKLOG,
    SEND_MESSAGE_TO_BACKLOG,
    SEND_FILE_TO_SERVER;

    public static ResponseCodes getCode(int n) {
        return switch (n) {
            case 0 -> OK;
            case 1 -> SEND_ECHO;
            case 2 -> GET_FILE;
            case 3 -> GET_MESSAGE_BACKLOG;
            case 4 -> SEND_MESSAGE_TO_BACKLOG;
            case 5 -> SEND_FILE_TO_SERVER;//klient saadab faili serverile
            case 6 -> CREATE_USER;
            case 7 -> CHECK_USER;
            case -1 -> RESPONSE_CODE_NOT_FOUND;
            case -2 -> FILE_NOT_FOUND;
            case -3 -> USER_NOT_FOUND;
            case -4 -> USER_TAKEN;
            case -5 -> FILE_WRITING_ERROR;
            case -6 -> FALSE_PASSWORD;
            default -> throw new IllegalStateException("Tundmatu kood: " + n);
        };
    }

    public static int getValue(ResponseCodes rc) {
        return switch (rc) {
            case OK -> 0;
            case SEND_ECHO -> 1;
            case GET_FILE -> 2;
            case GET_MESSAGE_BACKLOG -> 3;
            case SEND_MESSAGE_TO_BACKLOG -> 4;
            case SEND_FILE_TO_SERVER -> 5;
            case CREATE_USER -> 6;
            case CHECK_USER -> 7;
            case RESPONSE_CODE_NOT_FOUND -> -1;
            case FILE_NOT_FOUND -> -2;
            case USER_NOT_FOUND -> -3;
            case USER_TAKEN -> -4;
            case FILE_WRITING_ERROR -> -5;
            case FALSE_PASSWORD -> -6;
        };
    }

    public static ResponseCodes stringToCode(String s) {
        return switch (s) {
            case "echo" -> SEND_ECHO;
            case "file" -> GET_FILE;
            case "sendfile" -> SEND_FILE_TO_SERVER;
            case "getsonum" -> GET_MESSAGE_BACKLOG;
            case "writesonum" -> SEND_MESSAGE_TO_BACKLOG;
            case "createuser" -> CREATE_USER;
            case "checkuser" -> CHECK_USER;
            default -> throw new IllegalStateException("Tundmatu kood: " + s);
        };
    }
    public static void koodid(){
        String[] koodid={
                "echo $sõnum",
                "file $path",
                "sendfile $path",
                "getsonum $kasutaja",
                "writesonum $saaja $sõnum",
                "createuser $kasutaja $parool",
                "checkuser $kasutaja $parool"
        };
        for(String kood:koodid) System.out.println(kood);

        }
    }
