package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.UrlOpener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A command to open a URL in the default browser or in the default application.
 *
 * @version Apr 2022: this command is now also used for opening local resources that are referenced from an annotation.
 *     Either via its {@code external reference} field, or via {@code CVEntry}'s external reference or via the annotation
 *     content.
 *     <p>Note: the current implementation still includes the very project specific
 *     assumptions concerning external CV's and the format of an entry's value and id.
 * @see mpi.eudico.client.annotator.util.UrlOpener
 */
public class ShowInBrowserCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ShowInBrowserCommand instance
     *
     * <p>The idea is that each ECV entry has a link to an external reference which is a base URL.
     *
     * <p>By concatenating the entry ID to the end of the base URL, one becomes the direct URL to an entry.
     *
     * @param name the name of the command
     */
    public ShowInBrowserCommand(String name) {
        commandName = name;
    }

    /**
     * Opens a referenced remote resource in the default browser or, if it is a video or audio file, in the default media
     * player. Opens any other local resource in the default application for the type of resource.
     *
     * @param receiver ignored
     * @param arguments: <ul> <li>arguments[0] = the annotation (possibly) containing a link to an external resource
     *     (AbstractAnnotation)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof Annotation) {
            AbstractAnnotation annotation = (AbstractAnnotation) arguments[0];

            if (UrlOpener.hasBrowserLink(annotation)) {
                try {
                    //                    URI mediaURI = UrlOpener.getBrowserURIFrom(annotation);
                    //                    if (mediaURI != null && UrlOpener.isRemoteAVMediaLink(mediaURI.toString())) {
                    // Desktop.open(f), the basis of openResourceInDefaultApplication,
                    // only works for file: URL files
                    //                        UrlOpener.openResourceInDefaultApplication(mediaURI, true);
                    //                    } else {
                    UrlOpener.openUrl(UrlOpener.getBrowserURIFrom(annotation), true);
                    //                    }
                } catch (Exception ex) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("The url could not be opened (" + ex.getMessage() + ")");
                    }
                }
            } else if (UrlOpener.hasLocalExternalResourceRef(annotation)) {
                try {
                    UrlOpener.openResourceInDefaultApplication(UrlOpener.getLocalURIFrom(annotation), true);
                } catch (Exception ex) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("The file url could not be opened (" + ex.getMessage() + ")");
                    }
                }
            }
        }

    }

    @Override
    public String getName() {
        return commandName;
    }

}
