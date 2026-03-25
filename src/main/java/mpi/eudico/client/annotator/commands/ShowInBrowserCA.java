package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.UrlOpener;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

import java.awt.event.ActionEvent;

/**
 * A command action to show a URL in the default browser if the active annotation is linked to a CVEntry which contains a
 * hyperlink.
 */
@SuppressWarnings("serial")
public class ShowInBrowserCA extends CommandAction implements ActiveAnnotationListener {

    /**
     * Creates a new ShowInBrowserCA instance
     *
     * @param viewerManager the ViewerManager
     */
    public ShowInBrowserCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHOW_IN_BROWSER);
        viewerManager.connectListener(this);
    }

    /**
     * Constructor to be called by subclasses, otherwise the wrong "commandId" will be set.
     *
     * @param viewerManager the viewer manager
     * @param name the name of the command
     */
    ShowInBrowserCA(ViewerManager2 viewerManager, String name) {
        super(viewerManager, name);
    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SHOW_IN_BROWSER);
    }

    /**
     * The active annotation.
     *
     * @return an Object array size = 1, containing the active annotation
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getActiveAnnotation().getAnnotation()};
    }

    /**
     * Don't create a command when there is no active annotation or if there is no clipboard access.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        AbstractAnnotation annotation = (AbstractAnnotation) vm.getActiveAnnotation().getAnnotation();
        if (annotation == null) {
            return;
        }

        if (!UrlOpener.hasAnyExternalResourceRef(annotation)) {
            return;
        }
        super.actionPerformed(event);
    }

    /**
     * @see mpi.eudico.client.annotator.ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
    public void updateActiveAnnotation() {
        AbstractAnnotation annotation = (AbstractAnnotation) vm.getActiveAnnotation().getAnnotation();
        if (annotation != null) {
            // adapt to more generalized open external resource action
            setEnabled(UrlOpener.hasAnyExternalResourceRef(annotation));
        } else {
            setEnabled(false);
        }
    }
}
