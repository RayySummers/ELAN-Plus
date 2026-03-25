package mpi.eudico.client.annotator.prefs.gui;

import mpi.eudico.client.annotator.ELAN;
import mpi.eudico.client.annotator.Preferences;
import org.junit.jupiter.api.*;

class MediaNavPanelTest {

    @BeforeEach
    protected void setUp() {
        ELAN.detectUILabelFont();
        Preferences.set("Media.VideoAlwaysOnTop", null, null, false);
    }

    @AfterEach
    protected void tearDown() {
        Preferences.set("Media.VideoAlwaysOnTop", null, null, false);
    }

    @Test
    @DisplayName("Test instantiation when video frame 'AlwaysOnTop' preference is NOT present.")
    void testInstantiationWhenVideoFrameAlwaysOnTopPreferenceIsNotPresent() {
        MediaNavPanel mediaNavPanel = new MediaNavPanel();
        Assertions.assertTrue(mediaNavPanel.getVideoFrameAlwaysOnTopCB().isSelected(), "The checkbox should be selected/check-marked");
    }

    @Test
    @DisplayName("Test instantiation when video frame 'AlwaysOnTop' preference is 'True'.")
    void testInstantiationWhenVideoFrameAlwaysOnTopPreferenceIsTrue() {
        Preferences.set("Media.VideoAlwaysOnTop", true, null, false);
        MediaNavPanel mediaNavPanel = new MediaNavPanel();
        Assertions.assertTrue(mediaNavPanel.getVideoFrameAlwaysOnTopCB().isSelected(), "The checkbox should be selected/check-marked");
    }

    @Test
    @DisplayName("Test instantiation when video frame 'AlwaysOnTop' preference is 'False'.")
    void testInstantiationWhenVideoFrameAlwaysOnTopPreferenceIsFalse() {
        Preferences.set("Media.VideoAlwaysOnTop", false, null, false);
        MediaNavPanel mediaNavPanel = new MediaNavPanel();
        Assertions.assertFalse(mediaNavPanel.getVideoFrameAlwaysOnTopCB().isSelected(), "The checkbox should NOT be selected/check-marked");
    }

    @Test
    @DisplayName("Test getChangedPreferences when there is no change made.")
    void testGetChangedPreferencesWhenNothingChanged() {
        MediaNavPanel mediaNavPanel = new MediaNavPanel();
        Assertions.assertNull(mediaNavPanel.getChangedPreferences(), "No change should be made");
    }

    @Test
    @DisplayName("Test getChangedPreferences when preference was changed.")
    void testGetChangedPreferencesWhenAlwaysOnTopPrefChanged() {
        Preferences.set("Media.VideoAlwaysOnTop", false, null, false);
        MediaNavPanel mediaNavPanel = new MediaNavPanel();
        mediaNavPanel.getVideoFrameAlwaysOnTopCB().setSelected(true);
        Assertions.assertTrue((Boolean) mediaNavPanel.getChangedPreferences().get("Media.VideoAlwaysOnTop"), "Resulting map should have updated value.");
    }

    @Test
    @DisplayName("Test isChanged when no change was made.")
    void testIsChangedWhenNoChange() {
        Preferences.set("Media.VideoAlwaysOnTop", false, null, false);
        MediaNavPanel mediaNavPanel = new MediaNavPanel();
        Assertions.assertFalse(mediaNavPanel.isChanged(), "isChange should return false when the checkbox value has NOT been changed");
    }

    @Test
    @DisplayName("Test isChanged when preference was changed.")
    void testIsChangedWhenSomeChangeWasMade() {
        Preferences.set("Media.VideoAlwaysOnTop", false, null, false);
        MediaNavPanel mediaNavPanel = new MediaNavPanel();
        mediaNavPanel.getVideoFrameAlwaysOnTopCB().setSelected(true);
        Assertions.assertTrue(mediaNavPanel.isChanged(), "isChange should return true when the checkbox value has been changed");
    }

}