package jakarta.servlet.http;

public @interface CookieValue {
    String value();

    boolean required();
}
