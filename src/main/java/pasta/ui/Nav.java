package pasta.ui;

public interface Nav {

    String WELCOME = "welcome";
    String LOGIN = "login";
    String SIGNUP = "signup";
    String DASHBOARD = "dashboard";
    String ORDER = "order";
    String SALES = "sales";

    void go(String screen);
}
