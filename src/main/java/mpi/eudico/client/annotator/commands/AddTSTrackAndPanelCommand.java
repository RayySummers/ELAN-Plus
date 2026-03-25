package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.timeseries.TSTrackManager;
import mpi.eudico.client.annotator.timeseries.TimeSeriesTrack;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;

import java.util.List;

/**
 * A command that adds timeseries tracks to the timeseries viewer. If the viewer does not exist yet, it is created, together
 * with a {@code TSTrackManager}, and added to the layout.
 */
public class AddTSTrackAndPanelCommand implements Command {
    private final String name;
    private List<Object> tracks;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AddTSTrackAndPanelCommand(String name) {
        this.name = name;
    }

    /**
     * @param receiver the viewer manager
     * @param arguments args[0] is a List of Track objects
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) receiver;
        tracks = (List<Object>) arguments[0];

        if (tracks != null && tracks.size() > 0) {
            TSTrackManager trackManager = ELANCommandFactory.getTrackManager(vm.getTranscription());

            if (trackManager == null) {
                trackManager = new TSTrackManager(vm.getTranscription());
                ELANCommandFactory.addTrackManager(vm.getTranscription(), trackManager);

                // get viewer manager, create viewer
                TimeSeriesViewer tsViewer =
                    ELANCommandFactory.getViewerManager(vm.getTranscription()).createTimeSeriesViewer();
                tsViewer.setTrackManager(trackManager);
                // get layout manager, add viewer
                ELANCommandFactory.getLayoutManager(vm.getTranscription()).add(tsViewer);
            }

            TimeSeriesTrack track;
            for (int i = 0; i < tracks.size(); i++) {
                track = (TimeSeriesTrack) tracks.get(i);
                trackManager.addTrack(track);
            }
        }

    }

    @Override
    public String getName() {
        return name;
    }

}
