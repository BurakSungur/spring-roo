package org.springframework.roo.addon.web.mvc.jsp;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Commands for Web-related add-on to be used by the Roo shell.
 * 
 * @author Stefan Schmidt
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class JspCommands implements CommandMarker {

  // ------------ OSGi component attributes ----------------
  private BundleContext context;

  private static Logger LOGGER = HandlerUtils.getLogger(JspCommands.class);

  private JspOperations jspOperations;
  private PathResolver pathResolver;

  protected void activate(final ComponentContext cContext) {
    context = cContext.getBundleContext();
  }

  @CliAvailabilityIndicator({"web mvc controller", "controller class", "web mvc install view",
      "web mvc view", "web mvc update tags"})
  public boolean isControllerClassAvailable() {
    return getJspOperations().isControllerAvailable();
  }

  @CliAvailabilityIndicator({"web mvc install language", "web mvc language"})
  public boolean isInstallLanguageAvailable() {
    return getJspOperations().isInstallLanguageCommandAvailable();
  }

  @CliCommand(value = "web mvc language",
      help = "Install new internationalization bundle for MVC scaffolded UI.")
  public void language(@CliOption(key = {"", "code"}, mandatory = true,
      help = "The language code for the desired bundle") final I18n i18n) {

    if (i18n == null) {
      LOGGER.warning("Could not parse language code");
      return;
    }
    getJspOperations().installI18n(i18n, getPathResolver().getFocusedPath(Path.SRC_MAIN_WEBAPP));
  }

  @CliCommand(value = "web mvc controller",
      help = "Create a new manual Controller (ie where you write the methods)")
  public void newMvcArtifact(
      @CliOption(key = {"class", ""}, mandatory = true,
          help = "The path and name of the controller object to be created") final JavaType controller,
      @CliOption(key = "preferredMapping", mandatory = false,
          help = "Indicates a specific request mapping path for this controller (eg /foo/)") final String preferredMapping) {

    getJspOperations().createManualController(controller, preferredMapping,
        getPathResolver().getFocusedPath(Path.SRC_MAIN_WEBAPP));
  }

  @CliCommand(
      value = "web mvc update tags",
      help = "Replace an existing application tagx library with the latest version (use --backup option to backup your application first)")
  public void update(
      @CliOption(key = "backup", mandatory = false, specifiedDefaultValue = "true",
          unspecifiedDefaultValue = "false",
          help = "Backup your application before replacing your existing tag library") final boolean backup) {

    getJspOperations().updateTags(backup, getPathResolver().getFocusedPath(Path.SRC_MAIN_WEBAPP));
  }

  @CliCommand(value = "web mvc view", help = "Create a new static view.")
  public void view(
      @CliOption(key = "path", mandatory = true,
          help = "The path the static view to create in (required, ie '/foo/blah')") final String path,
      @CliOption(key = "viewName", mandatory = true,
          help = "The view name the mapping this view should adopt (required, ie 'index')") final String viewName,
      @CliOption(key = "title", mandatory = true, help = "The title of the view") final String title) {

    getJspOperations().installView(path, viewName, title, "View",
        getPathResolver().getFocusedPath(Path.SRC_MAIN_WEBAPP));
  }

  public JspOperations getJspOperations() {
    if (jspOperations == null) {
      // Get all Services implement JspOperations interface
      try {
        ServiceReference<?>[] references =
            this.context.getAllServiceReferences(JspOperations.class.getName(), null);

        for (ServiceReference<?> ref : references) {
          return (JspOperations) this.context.getService(ref);
        }

        return null;

      } catch (InvalidSyntaxException e) {
        LOGGER.warning("Cannot load JspOperations on JspCommands.");
        return null;
      }
    } else {
      return jspOperations;
    }

  }

  public PathResolver getPathResolver() {
    if (pathResolver == null) {
      // Get all Services implement PathResolver interface
      try {
        ServiceReference<?>[] references =
            this.context.getAllServiceReferences(PathResolver.class.getName(), null);

        for (ServiceReference<?> ref : references) {
          return (PathResolver) this.context.getService(ref);
        }

        return null;

      } catch (InvalidSyntaxException e) {
        LOGGER.warning("Cannot load PathResolver on JspCommands.");
        return null;
      }
    } else {
      return pathResolver;
    }

  }
}
