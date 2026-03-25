package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.gui.AboutPanel;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.swing.*;

class DetachedFrameTest {

    @Mock
    ElanLayoutManager elanLayoutManager;

    @Mock
    JFrame elanFrame;

    @Mock
    JRootPane jRootPane;

    @Mock
    InputMap inputMap;

    @Mock
    KeyStroke keyStroke;

    @BeforeEach
    protected void setUp() {
        Preferences.set("Media.VideoAlwaysOnTop", null, null, false);
        MockitoAnnotations.openMocks(this);
        Mockito.when(inputMap.allKeys()).thenReturn(new KeyStroke[]{keyStroke});
        Mockito.when(inputMap.get(ArgumentMatchers.any(KeyStroke.class))).thenReturn(new Object());
        Mockito.when(jRootPane.getInputMap(ArgumentMatchers.anyInt())).thenReturn(inputMap);
        Mockito.when(elanFrame.getRootPane()).thenReturn(jRootPane);
        Mockito.when(elanLayoutManager.getElanFrame()).thenReturn(elanFrame);
    }

    @AfterEach
    protected void tearDown() {
        Preferences.set("Media.VideoAlwaysOnTop", null, null, false);
    }

    @Test
    @DisplayName("Test instantiation when video frame 'AlwaysOnTop' preference is NOT present.")
    void testInstantiationWhenVideoFrameAlwaysOnTopPreferenceIsNotPresent() {
        DetachedFrame detachedFrame = new DetachedFrame(elanLayoutManager, new AboutPanel(), "");
        Assertions.assertTrue(detachedFrame.isAlwaysOnTop(), "Frame should be always on top when there was no preference present");
    }

    @Test
    @DisplayName("Test instantiation when video frame 'AlwaysOnTop' preference is 'True'.")
    void testInstantiationWhenVideoFrameAlwaysOnTopPreferenceIsTrue() {
        Preferences.set("Media.VideoAlwaysOnTop", true, null, false);
        DetachedFrame detachedFrame = new DetachedFrame(elanLayoutManager, new AboutPanel(), "");
        Assertions.assertTrue(detachedFrame.isAlwaysOnTop(), "Frame should be always-on-top when the preference is set to 'True'");
    }

    @Test
    @DisplayName("Test instantiation when video frame 'AlwaysOnTop' preference is 'False'.")
    void testInstantiationWhenVideoFrameAlwaysOnTopPreferenceIsFalse() {
        Preferences.set("Media.VideoAlwaysOnTop", false, null, false);
        DetachedFrame detachedFrame = new DetachedFrame(elanLayoutManager, new AboutPanel(), "");
        Assertions.assertFalse(detachedFrame.isAlwaysOnTop(), "Frame should NOT be always-on-top when the preference is set to 'False'");
    }

}
