package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.server.corpora.clom.Transcription;

import javax.swing.*;

/**
 * A command to set the milliseconds per frame/sample value of a media player to a custom value, specified by the user.
 */
public class SetCustomMsPerFrameCommand implements Command {
    private final String name;

    /**
     * Constructor.
     *
     * @param name name of the command
     */
    public SetCustomMsPerFrameCommand(String name) {
        this.name = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the ElanMediaPlayer
     * @param arguments the arguments:  <ul><li>arg[0] = the transcription loaded in the window containing the media
     *     player (Transcription)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof ElanMediaPlayer player) {
            Transcription transcription = (Transcription) arguments[0];
            double curMsPerSample = player.getMilliSecondsPerSample();

            String userMsPerSample = (String) JOptionPane.showInputDialog(ELANCommandFactory.getRootFrame(transcription),
                                                                          ElanLocale.getString(
                                                                              "Player.SetFrameDurationMessage"),
                                                                          "",
                                                                          JOptionPane.PLAIN_MESSAGE,
                                                                          null,
                                                                          null,
                                                                          Double.valueOf(curMsPerSample));

            if (userMsPerSample != null) {
                double userMsPS = 0d;
                try {
                    userMsPS = Double.parseDouble(userMsPerSample);
                } catch (NumberFormatException nfe) {
                    int divIndex = userMsPerSample.indexOf('/');
                    if (divIndex < 0) {
                        divIndex = userMsPerSample.indexOf(':');
                    }
                    if (divIndex > 0) {
                        try {
                            double numerator = Double.parseDouble(userMsPerSample.substring(0, divIndex));
                            double denominator = Double.parseDouble(userMsPerSample.substring(divIndex + 1));
                            if (denominator > 0) {
                                userMsPS = numerator / denominator;
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException nie) {
                            // return, do nothing
                        }
                    }
                }

                if (userMsPS != 0d) {
                    player.setMilliSecondsPerSample(userMsPS);
                }
            }
        }

    }

    @Override
    public String getName() {
        return name;
    }

}
