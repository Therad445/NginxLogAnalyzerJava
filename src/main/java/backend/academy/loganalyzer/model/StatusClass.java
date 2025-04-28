package backend.academy.loganalyzer.model;

public enum StatusClass {
    INFORMATIONAL(1),
    SUCCESS(2),
    REDIRECTION(3),
    CLIENT_ERROR(4),
    SERVER_ERROR(5),
    UNKNOWN(0);

    private final int codePrefix;

    StatusClass(int codePrefix) {
        this.codePrefix = codePrefix;
    }

    public static StatusClass fromStatusCode(int statusCode) {
        int firstDigit = statusCode / 100;
        return switch (firstDigit) {
            case 1 -> INFORMATIONAL;
            case 2 -> SUCCESS;
            case 3 -> REDIRECTION;
            case 4 -> CLIENT_ERROR;
            case 5 -> SERVER_ERROR;
            default -> UNKNOWN;
        };
    }
}
