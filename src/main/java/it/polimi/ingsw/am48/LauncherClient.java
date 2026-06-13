package it.polimi.ingsw.am48;

/**
 * Thin launcher wrapper around {@link ClientMain}, used as the JAR's main class.
 *
 * <p>This indirection exists because of how the shaded JAR is built: when a class that
 * directly references JavaFX types (such as {@link ClientMain}, which imports
 * {@code javafx.application.Application}) is used as the {@code Main-Class} of an
 * executable JAR, the JVM's module/launcher machinery performs an early JavaFX runtime
 * presence check on that class — before {@code main()} even runs — and fails if the JavaFX
 * modules are not on the module path. This breaks on platforms/configurations where JavaFX
 * is provided differently than expected.
 *
 * <p>By making {@code LauncherClient} — a class with <b>no</b> JavaFX imports — the actual
 * {@code Main-Class}, that early check is avoided. {@link ClientMain} (and its JavaFX
 * dependency) is only loaded once {@link #main} explicitly delegates to it, by which point
 * the classpath set up by {@code maven-shade-plugin} is fully available.
 *
 * @see ClientMain
 */
public class LauncherClient {

    /**
     * Delegates directly to {@link ClientMain#main(String[])}.
     *
     * @param args command-line arguments, forwarded unchanged
     * @throws Exception propagated from {@link ClientMain#main(String[])}
     */
    public static void main(String[] args) throws Exception{
        ClientMain.main(args);
    }
}
