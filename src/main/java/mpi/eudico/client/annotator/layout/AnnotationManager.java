package mpi.eudico.client.annotator.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.Zoomable;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.grid.GridViewer;
import mpi.eudico.client.annotator.gui.ResizeComponent;
import mpi.eudico.client.annotator.recognizer.gui.AbstractRecognizerPanel;
import mpi.eudico.client.annotator.recognizer.gui.RecognizerPanel;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.CommentViewer;
import mpi.eudico.client.annotator.viewer.InterlinearViewer;
import mpi.eudico.client.annotator.viewer.LexiconEntryViewer;
import mpi.eudico.client.annotator.viewer.MetadataViewer;
import mpi.eudico.client.annotator.viewer.MultiTierControlPanel;
import mpi.eudico.client.annotator.viewer.MultiTierViewer;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.annotator.viewer.SignalViewerControlPanel;
import mpi.eudico.client.annotator.viewer.SingleTierViewer;
import mpi.eudico.client.annotator.viewer.SingleTierViewerPanel;
import mpi.eudico.client.annotator.viewer.SpectrogramViewer;
import mpi.eudico.client.annotator.viewer.SubtitleViewer;
import mpi.eudico.client.annotator.viewer.TextViewer;
import mpi.eudico.client.annotator.viewer.TimeLineViewer;
import mpi.eudico.client.annotator.viewer.TimeScaleBasedViewer;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;


/**
 * Creates the Annotation Mode layout, the initial and default working mode. 
 *
 * @author Aarthy Somasundaram
 */
public class AnnotationManager implements ModeLayoutManager {	
	
    private ElanLayoutManager layoutManager;
    private Container container;
    private ViewerManager2 viewerManager;  

    private boolean showTimeLineViewer;
    private boolean showInterlinearViewer;  
    
    // viewerlist that only contains detachable viewers
    private List<ViewerLayoutModel> viewerList;
    private ElanMediaPlayerController mediaPlayerController;
   
    private SignalViewer signalViewer;
    private JComponent signalComponent;
    private SignalViewerControlPanel signalControlPanel;
    private TimeLineViewer timeLineViewer;
    private JScrollPane timeLineScrollPane;
    private InterlinearViewer interlinearViewer;
    private TimeSeriesViewer timeseriesViewer;
    private JComponent timeseriesComponent;
    private JSplitPane wav_tsSplitPane;
    private JPanel timeLineComponent;
    private MultiTierControlPanel multiTierControlPanel;
    private JSplitPane timeLineSplitPane;
	private SpectrogramViewer spectrogramViewer;
	private JComponent spectrogramControlPanel;
	private JComponent spectrogramHostComponent;
	private JSplitPane wavSpectSplitPane;
	private ComponentListener splitComponentListener;
    private ResizeComponent vertMediaResizer;
    private JComponent audioHostComponent;// try as alternative to wavSpectSplitPane
    private int dividerHeight = 6;

    private JTabbedPane tabPane;
    private JTabbedPane leftTabPane;
    private SingleTierViewerPanel gridPanel;
    private SingleTierViewerPanel textPanel;
    private JPanel subtitlePanel;
    private JComponent lexiconPanel;
	private CommentViewer commentPanel;
    private JComponent recognizerPanel;
    private JPanel controlPanel;
	private JScrollPane controlPanelScrollPane;
    private JComponent metadataPanel;    
    
    private boolean mediaInCentre = false;   
    private boolean gridViewerLeft;
    private boolean textViewerLeft;
    private boolean subtitleViewerLeft;
    private boolean lexiconViewerLeft;
    private boolean commentViewerLeft;
    private boolean recognizerLeft;    
    private boolean metaDataLeft; 
    
    private  List<String> viewerSortOrder;
    
    private boolean oneRowForVisuals = false;
    private boolean preferenceChanged = false;
    private int minTabWidth = 150;       
    private int numOfPlayers;
	private List<KeyStroke> ksNotToBeConsumed;


	/**
	 * Constructor.
	 * 
	 * @param viewerManager the viewer manager for creation and connecting of viewers
	 * @param elanLayoutManager the main layout manager, managing switching 
	 * between modes 
	 */
	public AnnotationManager(ViewerManager2 viewerManager,
			ElanLayoutManager elanLayoutManager) {
        this.viewerManager = viewerManager;
        this.layoutManager = elanLayoutManager;        
        container = layoutManager.getContainer();       
        viewerList = new ArrayList<ViewerLayoutModel>(4);
		showTimeLineViewer = true;
		viewerSortOrder = new ArrayList<String>();
		splitComponentListener = this.layoutManager.new SignalSplitPaneListener();
	}

	@Override
	public void add(Object object) {
		if(object == null){
			return;
		}
		
       if (object instanceof ElanMediaPlayerController) {
           setMediaPlayerController((ElanMediaPlayerController) object);
       } else if (object instanceof SignalViewer) {
           setSignalViewer((SignalViewer) object);
       } else if (object instanceof TimeLineViewer) {
           setTimeLineViewer((TimeLineViewer) object);
       } else if (object instanceof InterlinearViewer ) {
           setInterlinearViewer((InterlinearViewer) object);
       } else if (object instanceof GridViewer ) {
           addSingleTierViewer((SingleTierViewer) object);
       } else if (object instanceof TextViewer ) {
           addSingleTierViewer((SingleTierViewer) object);
       } else if (object instanceof SubtitleViewer) {
           addSingleTierViewer((SingleTierViewer) object);
       } else if (object instanceof TimeSeriesViewer) {
       		setTimeSeriesViewer((TimeSeriesViewer) object);
       } else if (object instanceof RecognizerPanel) {
       		recognizerPanel = (JComponent) object;
			addToTabPane(ElanLocale.getString("Tab.Recognizer"), recognizerPanel);
       } else if (object instanceof MetadataViewer) {
    	   metadataPanel = (JComponent) object;
    	   addToTabPane(ElanLocale.getString("Tab.Metadata"), (MetadataViewer) object);
       } else if (object instanceof LexiconEntryViewer) {
       		lexiconPanel = (JComponent) object;
       		addToTabPane(ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER), lexiconPanel);
       } else if (object instanceof CommentViewer) {
      		commentPanel = (CommentViewer) object;
      		addToTabPane(ElanLocale.getString(ELANCommandFactory.COMMENT_VIEWER), commentPanel);
       } else if (object instanceof SpectrogramViewer) {
    	   setSpectrogramViewer((SpectrogramViewer) object);
       }
	}
	
	/**
     * Sets the player controller.
     *
     * @param mediaPlayerController the player controller
     */
    private void setMediaPlayerController(ElanMediaPlayerController mediaPlayerController) {    	
   
        this.mediaPlayerController = mediaPlayerController;
        
        mediaPlayerController.getSliderPanel().addMouseListener(
        		mediaPlayerController.getAnnotationDensityViewer());
        
        // add the control components to the container
        container.add(mediaPlayerController.getPlayButtonsPanel());
        container.add(mediaPlayerController.getTimePanel());
        //container.add(mediaPlayerController.getDurationPanel());
        container.add(mediaPlayerController.getModePanel());
        container.add(mediaPlayerController.getSelectionPanel());
        container.add(mediaPlayerController.getSelectionButtonsPanel());
        container.add(mediaPlayerController.getAnnotationNavigationPanel());
        container.add(mediaPlayerController.getSliderPanel());
        container.add(mediaPlayerController.getAnnotationDensityViewer());  
        container.add(mediaPlayerController.getVolumeIconPanel());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(mediaPlayerController.getVolumePanel(), gbc);
        controlPanel.add(mediaPlayerController.getPlayersVolumePanel(), gbc);
        JPanel filler = new JPanel();
        gbc.weighty = 3;
        controlPanel.add(filler, gbc);      
        gbc.weighty = 1;
        controlPanel.add(mediaPlayerController.getRatePanel(), gbc);
        controlPanelScrollPane = new JScrollPane(controlPanel);

        addToTabPane(ElanLocale.getString("Tab.Controls"), controlPanelScrollPane);
    }
    
    private void removeMediaPlayerController(){
      // HS remove the density viewer as mouse listener of the slider panel
         mediaPlayerController.getSliderPanel().removeMouseListener(
         		mediaPlayerController.getAnnotationDensityViewer()); 
         
         container.remove(mediaPlayerController.getPlayButtonsPanel());
         container.remove(mediaPlayerController.getTimePanel());
         viewerManager.destroyTimePanel();
         //container.add(mediaPlayerController.getDurationPanel());
         container.remove(mediaPlayerController.getModePanel());
         container.remove(mediaPlayerController.getSelectionPanel());
         container.remove(mediaPlayerController.getSelectionButtonsPanel());
         container.remove(mediaPlayerController.getAnnotationNavigationPanel());
         container.remove(mediaPlayerController.getSliderPanel());
         viewerManager.destroyMediaPlayerControlSlider();
         container.remove(mediaPlayerController.getAnnotationDensityViewer());
         viewerManager.destroyViewerByName(ELANCommandFactory.ANNOTATION_DENSITY_VIEWER);
         container.remove(mediaPlayerController.getVolumeIconPanel());
         
         controlPanel.removeAll();
         
         viewerManager.destroyElanMediaPlayerController();
 		 mediaPlayerController = null;
    }
    
    /**
     * Sets the time line viewer.
     *
     * @param timeLineViewer the time line viewer
     */
    private void setTimeLineViewer(TimeLineViewer timeLineViewer) {
        this.timeLineViewer = timeLineViewer;

        if (timeLineComponent == null) {
            timeLineComponent = new JPanel();
            timeLineComponent.setLayout(null);
        }

        if (multiTierControlPanel == null) {
            multiTierControlPanel = viewerManager.getMultiTierControlPanel();
            multiTierControlPanel.setSize(ElanLayoutManager.CONTROL_PANEL_WIDTH, ElanLayoutManager.CONTROL_PANEL_WIDTH);
            ResizeComponent mcpResize = new ResizeComponent(layoutManager, SwingConstants.HORIZONTAL, ResizeComponent.CONTROL_PANEL);
            mcpResize.setSize(8, 16);
            multiTierControlPanel.setResizeComponent(mcpResize);
            timeLineComponent.add(multiTierControlPanel);
        }

        // disable the interlinear viewer if it exists
        if (interlinearViewer != null) {
            viewerManager.disableViewer(interlinearViewer);
        }

        // place the component in the split pane
        timeLineScrollPane = new JScrollPane(timeLineViewer, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        timeLineScrollPane.setBorder(null);
        timeLineComponent.add(timeLineScrollPane);
        //timeLineComponent.add(timeLineViewer);
        getTimeLineSplitPane().setBottomComponent(timeLineComponent);

        if (getTimeLineSplitPane().getTopComponent() != null) {
			Integer sigHeight = Preferences.getInt("LayoutManager.SplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
				getTimeLineSplitPane().setDividerLocation(sigHeight.intValue());
			} else {
				getTimeLineSplitPane().setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT);
			}
    		//getTimeLineSplitPane().setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT);
        }
        
        //doLayout();// necessary?
    }
    
    /**
     * Sets the interlinear viewer.
     *
     * @param interlinearViewer the interlinear viewer
     */
    private void setInterlinearViewer(InterlinearViewer interlinearViewer) {
        this.interlinearViewer = interlinearViewer;

        if (timeLineComponent == null) {
            timeLineComponent = new JPanel();
            timeLineComponent.setLayout(null);
        }

        if (multiTierControlPanel == null) {
            multiTierControlPanel = viewerManager.getMultiTierControlPanel();
            multiTierControlPanel.setSize(ElanLayoutManager.CONTROL_PANEL_WIDTH, ElanLayoutManager.CONTROL_PANEL_WIDTH);
            ResizeComponent mcpResize = new ResizeComponent(layoutManager, SwingConstants.HORIZONTAL, ResizeComponent.CONTROL_PANEL);
            mcpResize.setSize(8, 16);
            multiTierControlPanel.setResizeComponent(mcpResize);
            timeLineComponent.add(multiTierControlPanel);
        }

        // disable the timeLine viewer if it exists
        if (timeLineViewer != null) {
            viewerManager.disableViewer(timeLineViewer);
        }

        // place the component in the split pane
        timeLineComponent.add(interlinearViewer);
        getTimeLineSplitPane().setBottomComponent(timeLineComponent);

        //doLayout();// necessary?
    }
    
    /**
     * Sets the waveform viewer.
     *
     * @param signalViewer the signal viewer
     */
    private void setSignalViewer(SignalViewer signalViewer) {
    	if (this.signalViewer != null) {
    		return;// remove the existing viewer first
    	}
        this.signalViewer = signalViewer;
              
        if(signalControlPanel == null){
        	signalControlPanel = viewerManager.getSignalViewerControlPanel();
        	signalControlPanel.setSize(ElanLayoutManager.CONTROL_PANEL_WIDTH, ElanLayoutManager.CONTROL_PANEL_WIDTH);
        }
        if (signalComponent == null) {
            signalComponent = new JPanel();
            signalComponent.setLayout(null);
			signalComponent.add(signalControlPanel);
			signalComponent.add(this.signalViewer);
        }
        
        addAudioViewer(this.signalViewer);
    }
    
    /**
	 * Sets the one timeseries viewer. This has to be changed if there is a need
	 * for more than one timeseries viewer.
	 * Reused for attaching an existing, previously detached, timeseries viewer.
	 *  
	 * @param viewer the timeseries viewer
	 */
	private void setTimeSeriesViewer(TimeSeriesViewer timeseriesViewer) {
		if (this.timeseriesViewer == null) {
			this.timeseriesViewer = timeseriesViewer;

			ViewerLayoutModel vlm = new ViewerLayoutModel(timeseriesViewer, layoutManager);
			viewerList.add(vlm);
			
			boolean detached = false;
			Boolean detObj = Preferences.getBool("TimeSeriesViewer.Detached", 
					viewerManager.getTranscription());
			if(detObj != null) {
				detached = detObj.booleanValue();
			}
			
			if (detached) {
				vlm.detach();
				timeseriesViewer.setAttached(false);
				return;
			}
		}	
		
		if (wavSpectSplitPane == null) {
			timeseriesComponent = new JPanel();
			timeseriesComponent.setLayout(null);
			timeseriesComponent.addComponentListener(splitComponentListener);
		
	        // place the component in the outer split pane
			timeseriesComponent.add(timeseriesViewer);
			getTimeLineSplitPane().setTopComponent(timeseriesComponent);
			int divLoc = timeseriesComponent.getHeight() < ElanLayoutManager.DEF_SIGNAL_HEIGHT ? 
					ElanLayoutManager.DEF_SIGNAL_HEIGHT : timeseriesComponent.getHeight();
			
			Integer sigHeight = Preferences.getInt("LayoutManager.SplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
				divLoc = sigHeight.intValue();
			}
	        timeLineSplitPane.setDividerLocation(divLoc);
	        timeseriesViewer.setAttached(true);
			if (multiTierControlPanel != null) {
				timeseriesViewer.setVerticalRulerWidth(multiTierControlPanel.getWidth());
			}
		} else {
			if (signalComponent != null ) {
				//signalComponent.removeComponentListener(splitComponentListener);
			}
			
			int curHeight = getTimeLineSplitPane().getDividerLocation();
			timeseriesComponent = new JPanel();
			timeseriesComponent.setLayout(null);
			timeseriesComponent.add(timeseriesViewer);
			getTimeLineSplitPane().setTopComponent(null);
			
			wav_tsSplitPane = getWav_TSSplitPane();
			wav_tsSplitPane.setTopComponent(timeseriesComponent);
			wav_tsSplitPane.setBottomComponent(wavSpectSplitPane);
			wav_tsSplitPane.setDividerLocation(curHeight / 2);
			
			getTimeLineSplitPane().setTopComponent(wav_tsSplitPane);
			getTimeLineSplitPane().setDividerLocation(curHeight);
			timeseriesComponent.addComponentListener(splitComponentListener);
			timeLineComponent.addComponentListener(splitComponentListener);
			timeseriesViewer.setAttached(true);
			if (multiTierControlPanel != null) {
				timeseriesViewer.setVerticalRulerWidth(multiTierControlPanel.getWidth());
				timeseriesViewer.setBounds(timeseriesComponent.getBounds());
			}
			
			Integer divLoc = Preferences.getInt("LayoutManager.TSWavSplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (divLoc != null && wav_tsSplitPane != null) {
				if (divLoc.intValue() < curHeight - 20) {
					wav_tsSplitPane.setDividerLocation(divLoc.intValue());
				}
			}
		}
		
        doLayout();// necessary?
	}
	
	//
	private void setSpectrogramViewer(SpectrogramViewer viewer) {
		if (spectrogramViewer == null) {
			spectrogramViewer = viewer;
			spectrogramControlPanel = new JPanel();
			spectrogramHostComponent = new JPanel();
			spectrogramHostComponent.setLayout(null);
			spectrogramHostComponent.add(spectrogramControlPanel);
			spectrogramHostComponent.add(spectrogramViewer);
			
			addAudioViewer(spectrogramViewer);
		} // else remove the existing one first
	}
	
	private void setAudioSplitPane() {
		if (wavSpectSplitPane != null) {
	        if (timeseriesViewer == null) {
				//signalComponent.addComponentListener(splitComponentListener);
				getTimeLineSplitPane().setTopComponent(wavSpectSplitPane);
//				int divLoc = signalComponent.getHeight() < ElanLayoutManager.DEF_SIGNAL_HEIGHT ? 
//						ElanLayoutManager.DEF_SIGNAL_HEIGHT : signalComponent.getHeight();
				Integer sigHeight = Preferences.getInt("LayoutManager.SplitPaneDividerLocation", 
						viewerManager.getTranscription());
				if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
					getTimeLineSplitPane().setDividerLocation(sigHeight.intValue());
				} else {
					getTimeLineSplitPane().setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT);
				}
//		        timeLineSplitPane.setDividerLocation(divLoc);
	        } else {
	        	// check the attached/detached state of timeseries viewer
	        	/*
				timeseriesComponent.removeComponentListener(splitComponentListener);	
				*/
				int curHeight = timeseriesComponent.getHeight();
				getTimeLineSplitPane().setTopComponent(null);
	            
				wav_tsSplitPane = getWav_TSSplitPane();
				wav_tsSplitPane.setTopComponent(timeseriesComponent);
				wav_tsSplitPane.setBottomComponent(wavSpectSplitPane);
				wav_tsSplitPane.setDividerLocation(curHeight / 2);
				getTimeLineSplitPane().setTopComponent(wav_tsSplitPane);
				//timeseriesComponent.addComponentListener(splitComponentListener);// has already one
				timeLineComponent.addComponentListener(splitComponentListener);
				getTimeLineSplitPane().setDividerLocation(curHeight);
				Integer divLoc = Preferences.getInt("LayoutManager.TSWavSplitPaneDividerLocation", 
						viewerManager.getTranscription());
				Integer spliDivLoc = Preferences.getInt("LayoutManager.SplitPaneDividerLocation", 
						viewerManager.getTranscription());
				if (divLoc != null && spliDivLoc != null ) {
					if(wav_tsSplitPane != null){
						wav_tsSplitPane.setDividerLocation(divLoc.intValue());
					}				
					getTimeLineSplitPane().setDividerLocation(spliDivLoc.intValue());
				}
	        }
		}
	}
	
	private void removeAudioSplitPane() {
		if (wavSpectSplitPane != null) {
			// check if storing a preference is useful here
			if(timeLineSplitPane!= null){
				layoutManager.setPreference("LayoutManager.SplitPaneDividerLocation", 
						timeLineSplitPane.getDividerLocation() , viewerManager.getTranscription());
			}
			
			if (wav_tsSplitPane != null && wav_tsSplitPane.getBottomComponent() == wavSpectSplitPane) {
				// store divider location?
				Component topComp = wav_tsSplitPane.getTopComponent();
				wav_tsSplitPane.setTopComponent(null);
				wav_tsSplitPane.setBottomComponent(null);
				
				getTimeLineSplitPane().setTopComponent(topComp);
			} else {
				getTimeLineSplitPane().setTopComponent(null);
				//timeLineSplitPane.setDividerLocation(0);						
			}
			wavSpectSplitPane.removeAll();
			wavSpectSplitPane = null;
		}
	}
	
	/**
	 * Always add the SignalViewer and the SpectrogramViewer to a splitpane,
	 * even if only one of the viewers is used/visible. Otherwise adding and
	 * removing one or more of the 3 possible additional timescale based 
	 * viewers would become even more complex than it already is.
	 * 
	 * @param viewer the SignalViewer or the SpectrogramViewer
	 */
	private void addAudioViewer(TimeScaleBasedViewer viewer) {
		Integer divLoc = Preferences.getInt("LayoutManager.AudioSplitPaneDividerLocation", 
				viewerManager.getTranscription());

		boolean splitPaneExisting = (wavSpectSplitPane != null);
		JSplitPane splitPane = getWavSpectSplitPane();
		if (viewer == spectrogramViewer) {
			splitPane.setTopComponent(spectrogramHostComponent);
			spectrogramHostComponent.addComponentListener(splitComponentListener);
		} else if (viewer == signalViewer) {
			splitPane.setBottomComponent(signalComponent);
			signalComponent.addComponentListener(splitComponentListener);
		}
		
		if (spectrogramViewer != null && signalViewer != null) {
			splitPane.setDividerSize(dividerHeight);
			if (divLoc != null) {
				splitPane.setDividerLocation(divLoc.intValue());
			} else {
				splitPane.setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT / 3);
			}
			splitPane.setResizeWeight(0.5d);
		} else {
			splitPane.setDividerSize(1);// setting to 0 leads to unpredictable sizing
			splitPane.setDividerLocation(0);
			splitPane.setLastDividerLocation(0);
			if (signalComponent != null) {				
				splitPane.setResizeWeight(0d);// reset to default
			} else {
				// if there is one component the location always seems to be 0
				//splitPane.setDividerLocation(splitPane.getHeight());
				splitPane.setResizeWeight(1d);
			}
		}
		if (!splitPaneExisting) {
			splitPane.addComponentListener(splitComponentListener);// check this listener
			setAudioSplitPane();
		}
		
		doLayout();
	}
	
	private void removeAudioViewer(TimeScaleBasedViewer viewer) {
        if (wavSpectSplitPane != null && wavSpectSplitPane.getTopComponent() != null && 
        		wavSpectSplitPane.getBottomComponent() != null) {
			layoutManager.setPreference("LayoutManager.AudioSplitPaneDividerLocation", 
					wavSpectSplitPane.getDividerLocation() , viewerManager.getTranscription());
        }
		JSplitPane splitPane = getWavSpectSplitPane();
		if (viewer == spectrogramViewer) {
			//splitPane.remove(1);// top component
			spectrogramHostComponent.removeComponentListener(splitComponentListener);
			splitPane.remove(spectrogramHostComponent);
		} else if (viewer == signalViewer) {
			//splitPane.remove(2);// bottom component
			signalComponent.removeComponentListener(splitComponentListener);
			splitPane.remove(signalComponent);
		}
		
		if (splitPane.getTopComponent() != null || splitPane.getBottomComponent() != null) {
			splitPane.setDividerSize(1);
			splitPane.setDividerLocation(0);
			splitPane.setResizeWeight(0d);// reset to default
		} else {
			// both components are null, splitpane can be removed
			removeAudioSplitPane();
		}
		
	}
    
    private void addToTabPane(String tabName, Component component) {  
    	getTabPane().insertTab(tabName, null, component, tabName, getIndexOfComponent(getTabPane(), component));   	    	
    	preferenceChanged = true;
    	doLayout();
    }
    
    private void addSingleTierViewer(SingleTierViewer viewer) {
        SingleTierViewerPanel panel = viewerManager.createSingleTierViewerPanel();
        panel.setViewer(viewer);

        if (viewer instanceof GridViewer) {
            gridPanel = panel;
            addToTabPane(ElanLocale.getString("Tab.Grid"), panel);
        } else if (viewer instanceof TextViewer) {
            textPanel = panel;
            addToTabPane(ElanLocale.getString("Tab.Text"), panel);
        } else if (viewer instanceof SubtitleViewer) {        	
            getSubtitlePanel().add(panel);
        }

        doLayout();
    }
    
    /**
	 * Remove an object from the layout.
	 *
	 * @param object the object, most likely a viewer, to remove
	 */
    @Override
	public void remove(Object object) {
		if(object == null){
			return;
		}
		if (object instanceof SignalViewer) {
			removeSignalViewer();
		} else if (object instanceof TimeSeriesViewer) {
			removeTimeSeriesViewer();
		} else if (object instanceof RecognizerPanel) {
			removeFromTabPane((Component) object);
		} else if (object instanceof MetadataViewer) {
			removeFromTabPane((Component) object);
		} else if (object instanceof GridViewer) {
			removeFromTabPane(gridPanel);
		} else if (object instanceof TextViewer) {
			removeFromTabPane(textPanel);
		} else if (object instanceof SubtitleViewer) {
			removeFromTabPane(subtitlePanel);
			//subtitlePanel = null;
		} 
        else if (object instanceof LexiconEntryViewer) {
        	removeFromTabPane((Component) object);
        }
        else if (object instanceof CommentViewer) {
        	removeFromTabPane((Component) object);
        }
        else if (object instanceof SpectrogramViewer) {
        	removeSpectrogramViewer();
        }
	}
    
    private void removeFromTabPane(Component component) {
    	if(component == null){
    		return;
    	}
		if(getTabPane().indexOfComponent(component) >= 0){
			getTabPane().remove(component);
		} else if(getLeftTabPane().indexOfComponent(component) >= 0){
			getLeftTabPane().remove(component);
		}
	}
    
    /**
	 * Removes the SignalViewer from the layout.
	 */
	private void removeSignalViewer() {		
		if (signalViewer != null) {
			if (wavSpectSplitPane != null && wavSpectSplitPane.getTopComponent() != null) {
				// or proportional?
//				int h = wavSpectSplitPane.getHeight(); // or wavSpectSplitPane.getMaximumDividerLocation()?
//				int dl = wavSpectSplitPane.getDividerLocation();
//				double pp = 0.5d;
//				if (h > 0) {
//					pp = dl / (double) h;
//				}
				layoutManager.setPreference("LayoutManager.AudioSplitPaneDividerLocation", 
						wavSpectSplitPane.getDividerLocation() , viewerManager.getTranscription());//getLastDividerLocation() ?				
			}
			if (wav_tsSplitPane != null && wav_tsSplitPane.getTopComponent() != null) {
				layoutManager.setPreference("LayoutManager.LayoutManager.TSWavSplitPaneDividerLocation", 
						wavSpectSplitPane.getDividerLocation() , viewerManager.getTranscription());//getLastDividerLocation() ?
			}

			removeAudioViewer(signalViewer);
			
			signalComponent.remove(signalViewer);
			signalComponent.remove(signalControlPanel);
			signalViewer = null;
			signalComponent = null;					
			signalControlPanel = null;
			
			doLayout();
		}
	}
	
	/**
	 * Removes the timeseries viewer from the viewer list and from the layout.
	 */
	private void removeTimeSeriesViewer() {
		if (timeseriesViewer != null) {
			 if(timeLineSplitPane!= null){
					layoutManager.setPreference("LayoutManager.SplitPaneDividerLocation", 
							timeLineSplitPane.getDividerLocation() , viewerManager.getTranscription());
				}
			ViewerLayoutModel vlm = null;
			for (int i = 0; i < viewerList.size(); i++) {
				vlm = viewerList.get(i);
				if (vlm.viewer == timeseriesViewer) {
					break;
				} else {
					vlm = null;
				}
			}
			
			if (vlm == null) {
				return;
			}
			
			if (vlm.isAttached()) {
				if (wav_tsSplitPane != null) {
					// set Preferences  "LayoutManager.TSWavSplitPaneDividerLocation"? 
					layoutManager.setPreference("LayoutManager.TSWavSplitPaneDividerLocation", 
							wav_tsSplitPane.getDividerLocation(), viewerManager.getTranscription());
					
    				int curHeight = getTimeLineSplitPane().getDividerLocation();
    				wav_tsSplitPane.setTopComponent(null);
    				wav_tsSplitPane.setBottomComponent(null);
    				timeseriesComponent.remove(timeseriesViewer);
    				timeseriesComponent = null;
    				wav_tsSplitPane = null;
    				
    				if (wavSpectSplitPane != null) {
    					getTimeLineSplitPane().setTopComponent(wavSpectSplitPane);
    					getTimeLineSplitPane().setDividerLocation(curHeight);
    					wavSpectSplitPane.setSize(wavSpectSplitPane.getWidth(), curHeight);
    				} else {
    					getTimeLineSplitPane().setTopComponent(null);
    				}   				
    			} else {
    				getTimeLineSplitPane().setTopComponent(null);
    				if (timeseriesComponent != null) {
	    				timeseriesComponent.remove(timeseriesViewer);
	    				timeseriesComponent = null;
    				}
    			}
				timeseriesViewer = null;
				doLayout();
			} else {
				vlm.attach();//destroys frame
				viewerList.remove(vlm);
				if(timeseriesComponent != null) {
					timeseriesComponent.remove(vlm.viewer);
					timeseriesComponent = null;
				}
				timeseriesViewer = null;
			}			
		}
		
	}
	
	private void removeSpectrogramViewer() {
		if (spectrogramViewer != null) {
			if (wavSpectSplitPane != null && wavSpectSplitPane.getBottomComponent() != null) {
				layoutManager.setPreference("LayoutManager.AudioSplitPaneDividerLocation", 
						wavSpectSplitPane.getDividerLocation() , viewerManager.getTranscription());//getLastDividerLocation() ?
			}
			removeAudioViewer(spectrogramViewer);
			
			spectrogramHostComponent.remove(spectrogramViewer);
			spectrogramHostComponent.remove(spectrogramControlPanel);
			spectrogramHostComponent = null; // listener has been removed
			spectrogramControlPanel = null;
			spectrogramViewer = null;
			
			doLayout();
		}
	}
    
    private JTabbedPane getTabPane() {
        if (tabPane == null) {
            tabPane = new JTabbedPane();            
            container.add(tabPane);
        } 
        return tabPane;
    }
    
    private JTabbedPane getLeftTabPane() {
        if (leftTabPane == null) {
        	leftTabPane = new JTabbedPane();           	
            container.add(leftTabPane);
        }

        return leftTabPane;
    }
    
    /**
     * destroys the left tabpane
     */
    private void destroyLeftPane(){
    	if(leftTabPane != null){
    		container.remove(leftTabPane);
    		leftTabPane = null;
    	}    	
    }
    
	/**
	 * Destroy an object from the layout.
	 *
	 * @param viewerName the object (viewer) to remove, disconnect and destroy
	 */
    @Override
	public boolean destroyAndRemoveViewer(String viewerName) {	
		boolean doLayout  = false;
		
		if(viewerName == null){
			return doLayout;
		}
		
		if (viewerName.equals(ELANCommandFactory.RECOGNIZER)) {
			if(recognizerPanel != null){
				removeFromTabPane(recognizerPanel);				
				recognizerPanel = null;
				
				doLayout = true;
			}
			viewerManager.destroyPanel(ELANCommandFactory.RECOGNIZER);		
		} 
		else if (viewerName.equals(ELANCommandFactory.METADATA_VIEWER)) {
			if(metadataPanel != null){
				removeFromTabPane(metadataPanel);
				metadataPanel = null;				
				doLayout = true;
			}					
			viewerManager.destroyViewerByName(ELANCommandFactory.METADATA_VIEWER);
		} 
		else if (viewerName.equals(ELANCommandFactory.GRID_VIEWER)) {
			if(gridPanel != null){
				removeFromTabPane(gridPanel);
				((GridViewer)gridPanel.getViewer()).isClosing();
				viewerManager.destroySingleTierViewerPanel(gridPanel);				
				gridPanel = null;
				doLayout = true;
			}
			viewerManager.destroyViewerByName(ELANCommandFactory.GRID_VIEWER); 
		} 
		else if (viewerName.equals(ELANCommandFactory.TEXT_VIEWER)) {
			if(textPanel != null){
				removeFromTabPane(textPanel);
				viewerManager.destroySingleTierViewerPanel(textPanel);				
				textPanel = null;
				doLayout = true;
			}
			viewerManager.destroyViewerByName(ELANCommandFactory.TEXT_VIEWER); 
		} 
		else if (viewerName.equals(ELANCommandFactory.SUBTITLE_VIEWER)) {
			if(subtitlePanel != null){
				removeFromTabPane(subtitlePanel);
				Component panel[] = subtitlePanel.getComponents();
				for (Component element : panel) {
					if(element instanceof SingleTierViewerPanel){
						((SubtitleViewer)((SingleTierViewerPanel)element).getViewer()).isClosing();
						viewerManager.destroySingleTierViewerPanel((SingleTierViewerPanel) element);
					}
				}				
				subtitlePanel = null;
				doLayout = true;
			}			
			viewerManager.destroyViewerByName(ELANCommandFactory.SUBTITLE_VIEWER);  
		}
		else if (viewerName.equals(ELANCommandFactory.INTERLINEAR_VIEWER)) {
			if(interlinearViewer != null){
				remove(interlinearViewer);
				interlinearViewer.isClosing();
				if(layoutManager.getMode() == ElanLayoutManager.NORMAL_MODE){
					showTimeLineViewer();
				}
				interlinearViewer = null;	
				doLayout = true;
			}	
			viewerManager.destroyViewerByName(ELANCommandFactory.INTERLINEAR_VIEWER);
			multiTierControlPanel.disableShowInterlinearViewer(true);			
		} 
		else if (viewerName.equals(ELANCommandFactory.LEXICON_VIEWER)) {
			if(lexiconPanel != null){
				removeFromTabPane(lexiconPanel);
				lexiconPanel = null;	
				doLayout = true;
			}	
					
			viewerManager.destroyViewerByName(ELANCommandFactory.LEXICON_VIEWER);
		}
		else if (viewerName.equals(ELANCommandFactory.COMMENT_VIEWER)) {
			if (commentPanel != null) {
				removeFromTabPane(commentPanel);
				commentPanel = null;
				doLayout = true;
			}	
					
			viewerManager.destroyViewerByName(ELANCommandFactory.COMMENT_VIEWER);
		}
		else if (viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)) {
			if(signalViewer != null){
				removeSignalViewer();
				doLayout = true;
			}
			viewerManager.destroyViewerByName(ELANCommandFactory.SIGNAL_VIEWER);			
		}
		else if (viewerName.equals(ELANCommandFactory.SPECTROGRAM_VIEWER)) {
			if(spectrogramViewer != null){
				removeSpectrogramViewer();
				doLayout = true;
			}
			viewerManager.destroyViewerByName(ELANCommandFactory.SPECTROGRAM_VIEWER);			
		}
		else if (viewerName.equals(ELANCommandFactory.TIMESERIES_VIEWER)) {
			if(timeseriesViewer != null){
				layoutManager.remove(layoutManager.getTimeSeriesViewer());
				doLayout = true;
				viewerManager.connectViewer(timeseriesViewer, false);
			}
		}		
		return doLayout;
	}
	
	@Override
	public void createAndAddViewer(String viewerName) {	
		if(viewerName == null){
			return;
		}
		
		if (viewerName.equals(ELANCommandFactory.RECOGNIZER)) {
				add(viewerManager.createRecognizerPanel());
		} 
		else if (viewerName.equals(ELANCommandFactory.METADATA_VIEWER)) {
			add(viewerManager.createMetadataViewer());			
		} 
		else if (viewerName.equals(ELANCommandFactory.GRID_VIEWER)) {
			add(viewerManager.createGridViewer());	
			if(viewerManager.getGridViewer() != null){
				viewerManager.getGridViewer().setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
			}
		} 
		else if (viewerName.equals(ELANCommandFactory.TEXT_VIEWER)) {
			add(viewerManager.createTextViewer());
		} 
		else if (viewerName.equals(ELANCommandFactory.SUBTITLE_VIEWER)) {
			createSubtitleViewer();
		} 		
		else if (viewerName.equals(ELANCommandFactory.INTERLINEAR_VIEWER)) {
			 add(viewerManager.createInterlinearViewer()); 
			 if(interlinearViewer != null){
				 interlinearViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
				 multiTierControlPanel.disableShowInterlinearViewer(false);
				 showTimeLineViewer();
			 }
		} 
		else if (viewerName.equals(ELANCommandFactory.LEXICON_VIEWER)) {
			add(viewerManager.createLexiconEntryViewer());
		} 
		else if (viewerName.equals(ELANCommandFactory.COMMENT_VIEWER)) {
			add(viewerManager.createCommentViewer(viewerManager.getTranscription()));
		} 
		else if (viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)) {
			layoutManager.add(viewerManager.createSignalViewer());
		}
		else if (viewerName.equals(ELANCommandFactory.SPECTROGRAM_VIEWER)) {
			add(viewerManager.createSpectrogramViewer());
		}
		else if (viewerName.equals(ELANCommandFactory.TIMESERIES_VIEWER)) {
			Boolean val = Preferences.getBool(ELANCommandFactory.TIMESERIES_VIEWER, null);
			if(val == null || val){
				add(layoutManager.getTimeSeriesViewer());
				if(timeseriesViewer !=null){
					viewerManager.connectViewer(timeseriesViewer, true);
				}
			}
		}
		
	}
    
    private JPanel getSubtitlePanel() {
        if (subtitlePanel == null) {
            subtitlePanel = new JPanel(new GridLayout(0, 1));           
            addToTabPane(ElanLocale.getString("Tab.Subtitles"), subtitlePanel);
        }

        return subtitlePanel;
    }
    
    private JSplitPane getTimeLineSplitPane() {
        if (timeLineSplitPane == null) {
            timeLineSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            timeLineSplitPane.setOneTouchExpandable(true);

            // HS 24 nov set the divider location when a top component is added
            timeLineSplitPane.setDividerLocation(0);
            timeLineSplitPane.setContinuousLayout(true);
            container.add(timeLineSplitPane);

    		dividerHeight = timeLineSplitPane.getDividerSize();
        }

        return timeLineSplitPane;
    }
    
    private JSplitPane getWav_TSSplitPane() {
        if (wav_tsSplitPane == null) {
        	wav_tsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        	wav_tsSplitPane.setOneTouchExpandable(true);
        	wav_tsSplitPane.setBorder(null);

            //set the divider location when a top component is added
        	wav_tsSplitPane.setDividerLocation(0);
        	wav_tsSplitPane.setDividerSize(dividerHeight);
        	wav_tsSplitPane.setContinuousLayout(true);
        }

        return wav_tsSplitPane;
    }
    
    private JSplitPane getWavSpectSplitPane() {
    	if (wavSpectSplitPane == null) {
    		wavSpectSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    		wavSpectSplitPane.setOneTouchExpandable(true);
    		wavSpectSplitPane.setBorder(null);
    		wavSpectSplitPane.setDividerLocation(0);
    		wavSpectSplitPane.setDividerSize(0);
    		wavSpectSplitPane.setContinuousLayout(true);
    	}
    	
    	return wavSpectSplitPane;
    }
    
    /**
	 * Returns the TimeLineViewer.
	 * 
	 * @return the TimeLineViewer, can be null
	 */
	public TimeLineViewer getTimeLineViewer() {
		return timeLineViewer;
	}
	
	/**
	 * Returns the InterlinearViewer.
	 * 
	 * @return the InterlinearViewer, can be null
	 */
	public InterlinearViewer getInterlinearViewer() {
		return interlinearViewer;
	}
	
	/**
	 * Returns the TimeSeries Viewer or null.
	 * 
	 * @return the TimeSeries Viewer
	 */
	public TimeSeriesViewer getTimeSeriesViewer() {
		return timeseriesViewer;
	}
	
    /**
     * Returns the panel showing and controlling tier names as a kind of row
     * headers for an attached viewer.
     *
     * @return the multi tier control panel 
     */
    public MultiTierControlPanel getMultiTierControlPanel() {
        return multiTierControlPanel;
    }
    
    /**
     * Returns the current visible MultiTierViewer.
     * 
     * @return the Timeline viewer or Interlinear viewer
     */
    public MultiTierViewer getVisibleMultiTierViewer() {
    	if (showTimeLineViewer) {
    		return timeLineViewer;
    	} else {
    		return interlinearViewer;
    	}
    }
    
    /**
     * Returns the index of the given component in the tabpane.
     * 
     * @param pane 	     the tab pane containing the specified component
     * @param component  the component or viewer for which the index is needed
     * 
     * @return the index of the component 
     */
    private int getIndexOfComponent(JTabbedPane pane, Component component){   
    	int index =0;

    	if (component == controlPanel) {
    		return pane.getTabCount();
    	}

    	if(viewerSortOrder != null && viewerSortOrder.size() > 0){    		
    		// Generalised version of the else part.
    		for (int i=0; i< viewerSortOrder.size(); i++){
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.GRID_VIEWER))){
        			if(component == gridPanel){
            			break;
            		}else if(pane.indexOfComponent(gridPanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.TEXT_VIEWER))){
        			if(component == textPanel){
            			break;
            		}else if(pane.indexOfComponent(textPanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.SUBTITLE_VIEWER))){
        			if(component == subtitlePanel){
            			break;
            		}else if(pane.indexOfComponent(subtitlePanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER))){
        			if(component == lexiconPanel){
            			break;
            		}else if(pane.indexOfComponent(lexiconPanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.COMMENT_VIEWER))){
        			if(component == commentPanel){
            			break;
            		}else if(pane.indexOfComponent(commentPanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.RECOGNIZER))){
        			if(component == recognizerPanel){
            			break;
            		}else if(pane.indexOfComponent(recognizerPanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.METADATA_VIEWER))){
            		if(component == metadataPanel){
            			break;
            		}else if(pane.indexOfComponent(metadataPanel) >= 0){
        				index++;            			
            		}
        		}        		
        	}
    		
    		return index;
        } else {
    		if(component == gridPanel){
    			return index;
    		}else if(pane.indexOfComponent(gridPanel) >= 0){
    			index++;
    		}

    		if(component == textPanel){
    			return index;
    		}else if(pane.indexOfComponent(textPanel) >= 0){
    			index++;
    		}

    		if(component == subtitlePanel){
    			return index;
    		}else if(pane.indexOfComponent(subtitlePanel) >= 0){
    			index++;
    		}

    		if(component == lexiconPanel){
    			return index;
    		}else if(pane.indexOfComponent(lexiconPanel) >= 0){
    			index++;
    		}

    		if(component == commentPanel){
    			return index;
    		}else if(pane.indexOfComponent(commentPanel) >= 0){
    			index++;
    		}

    		if(component == recognizerPanel){
    			return index;
    		}else if(pane.indexOfComponent(recognizerPanel) >= 0){
    			index++;
    		}

    		if(pane.indexOfComponent(metadataPanel) >= 0){
    			index++;
    		}else if(component == metadataPanel){
    			return index;
    		}
    		
    		return index > pane.getTabCount() ? pane.getTabCount() : index;
        }
    }
   
    /**
     * Rearranges the viewers in the left and right tabpanes according to the
     * changed preference settings.
     */
    private void reArrangeViewers() {  
    	if(mediaInCentre ){//&& layoutManager.getAttachedVisualPlayers().length > 0){ 
    		int selectedIndexLeftPane = 0;
    		int selectedIndexRightPane= 0; 
    		Component selectedLeftComponent = null;
    		Component selectedRightComponent = null;
    		
    		if(getLeftTabPane().getTabCount() > 0){
    			selectedIndexLeftPane = getLeftTabPane().getSelectedIndex();
    			selectedLeftComponent = getLeftTabPane().getSelectedComponent();
    		}
    		if(getTabPane().getTabCount() > 0){
    			selectedIndexRightPane = getTabPane().getSelectedIndex();
    			selectedRightComponent = getTabPane().getSelectedComponent();
    		}    		
    		
    		if( gridPanel != null){
    			String tabName = ElanLocale.getString("Tab.Grid");
    			getTabPane().remove(gridPanel);    						
    			getLeftTabPane().remove(gridPanel);
    			if(gridViewerLeft ){
    				getLeftTabPane().insertTab(tabName, null, gridPanel, tabName, getIndexOfComponent(getLeftTabPane(), gridPanel));    
    			} else{
    				getTabPane().insertTab(tabName, null, gridPanel, tabName, getIndexOfComponent(getTabPane(), gridPanel));  
    			} 
    		}
    		if(textPanel != null){
    			String tabName = ElanLocale.getString("Tab.Text");
				getTabPane().remove(textPanel);
				getLeftTabPane().remove(textPanel);
    			if(textViewerLeft){
    				getLeftTabPane().insertTab(tabName, null, textPanel, tabName, getIndexOfComponent(getLeftTabPane(), textPanel));    
    			} else{
    				getTabPane().insertTab(tabName, null, textPanel, tabName, getIndexOfComponent(getTabPane(), textPanel)); 
    			}    				    			
    		}
    			
    		if(subtitlePanel != null){
    			String tabName = ElanLocale.getString("Tab.Subtitles");
    			getTabPane().remove(subtitlePanel);
				getLeftTabPane().remove(subtitlePanel);
    			if(subtitleViewerLeft){    					
    				getLeftTabPane().insertTab(tabName, null, subtitlePanel, tabName, getIndexOfComponent(getLeftTabPane(), subtitlePanel));    
    			} else {
    				getTabPane().insertTab(tabName, null, subtitlePanel, tabName, getIndexOfComponent(getTabPane(), subtitlePanel)); 
        		}     			
    		} 
    		
    		if(lexiconPanel != null){
    			String tabName = ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER);
    			getTabPane().remove(lexiconPanel);
				getLeftTabPane().remove(lexiconPanel);
    			if(lexiconViewerLeft){    					
    				getLeftTabPane().insertTab(tabName, null, lexiconPanel, tabName, getIndexOfComponent(getLeftTabPane(), lexiconPanel));    
    			} else {
    				getTabPane().insertTab(tabName, null, lexiconPanel, tabName, getIndexOfComponent(getTabPane(), lexiconPanel)); 
        		}     			
    		} 
    			
    		if(commentPanel != null){
    			String tabName = ElanLocale.getString(ELANCommandFactory.COMMENT_VIEWER);
    			getTabPane().remove(commentPanel);
				getLeftTabPane().remove(commentPanel);
    			if(commentViewerLeft){    					
    				getLeftTabPane().insertTab(tabName, null, commentPanel, tabName, getIndexOfComponent(getLeftTabPane(), commentPanel));    
    			} else {
    				getTabPane().insertTab(tabName, null, commentPanel, tabName, getIndexOfComponent(getTabPane(), commentPanel)); 
        		}     			
    		} 
    			
    		if(recognizerPanel != null){
    			String tabName = ElanLocale.getString("Tab.Recognizer");
    			getTabPane().remove(recognizerPanel);
    			getLeftTabPane().remove(recognizerPanel);
    			if(recognizerLeft){    					
    				getLeftTabPane().insertTab(tabName, null, recognizerPanel, tabName, getIndexOfComponent(getLeftTabPane(), recognizerPanel));    
        		} else {
        			getTabPane().insertTab(tabName, null, recognizerPanel, tabName, getIndexOfComponent(getTabPane(), recognizerPanel));   
        		}     			
    		} 
    			
    		if(metadataPanel != null){
    			String tabName = ElanLocale.getString("Tab.Metadata");
    			getTabPane().remove(metadataPanel);
				getLeftTabPane().remove(metadataPanel);
    			if(metaDataLeft){    					
    				getLeftTabPane().insertTab(tabName, null, metadataPanel, tabName, getIndexOfComponent(getLeftTabPane(), metadataPanel));
        		} else {
        			getTabPane().insertTab(tabName, null, metadataPanel, tabName, getIndexOfComponent(getTabPane(), metadataPanel));    
        		}     			
    		} 
    		
    		if(getLeftTabPane().indexOfComponent(selectedLeftComponent) > 0){
    			getLeftTabPane().setSelectedComponent(selectedLeftComponent);
    		}else if(getLeftTabPane().getTabCount() > selectedIndexLeftPane){
    			getLeftTabPane().setSelectedIndex(selectedIndexLeftPane);
    		}else {
    			getLeftTabPane().setSelectedIndex(getLeftTabPane().getTabCount()-1);
    		}
    		
    		if(getTabPane().indexOfComponent(selectedRightComponent) > 0){
    			getTabPane().setSelectedComponent(selectedRightComponent);
    		}else if(getTabPane().getTabCount() > selectedIndexRightPane){
    			getTabPane().setSelectedIndex(selectedIndexRightPane);
    		}else {
    			getTabPane().setSelectedIndex(getTabPane().getTabCount()-1);
    		}
    	} else {
    		int selectedIndex= 0; 
    		Component selectedComponent = null;
    		if(getTabPane().getTabCount() > 0){
    			selectedIndex = getTabPane().getSelectedIndex();
    			selectedComponent = getTabPane().getSelectedComponent();
    		}   
    		if(gridPanel != null){
    			String tabName = ElanLocale.getString("Tab.Grid");  
    			getTabPane().remove(gridPanel);
    			getTabPane().insertTab(tabName, null, gridPanel, tabName, getIndexOfComponent(getTabPane(), gridPanel));
    		}
    		if(textPanel != null){
    			String tabName = ElanLocale.getString("Tab.Text");
    			getTabPane().remove(textPanel);
    			getTabPane().insertTab(tabName, null, textPanel, tabName, getIndexOfComponent(getTabPane(), textPanel));
    		}
    		if(subtitlePanel != null){
    			String tabName = ElanLocale.getString("Tab.Subtitles");			
    			getTabPane().remove(subtitlePanel);
    			getTabPane().insertTab(tabName, null, subtitlePanel, tabName, getIndexOfComponent(getTabPane(), subtitlePanel));
    		}
    		if(lexiconPanel != null){
    			String tabName = ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER);			
    			getTabPane().remove(lexiconPanel);
    			getTabPane().insertTab(tabName, null, lexiconPanel, tabName, getIndexOfComponent(getTabPane(), lexiconPanel));
    		}
    		if(commentPanel != null){
    			String tabName = ElanLocale.getString(ELANCommandFactory.COMMENT_VIEWER);			
    			getTabPane().remove(commentPanel);
    			getTabPane().insertTab(tabName, null, commentPanel, tabName, getIndexOfComponent(getTabPane(), commentPanel));
    		}
    		if(recognizerPanel != null){
    			String tabName = ElanLocale.getString("Tab.Recognizer");			
    			getTabPane().remove(recognizerPanel);
    			getTabPane().insertTab(tabName, null, recognizerPanel, tabName, getIndexOfComponent(getTabPane(), recognizerPanel));
    		}			
    		if(metadataPanel != null){
    			String tabName = ElanLocale.getString("Tab.Metadata");
    			getTabPane().remove(metadataPanel);
    			getTabPane().insertTab(tabName, null, metadataPanel, tabName, getIndexOfComponent(getTabPane(), metadataPanel));
    		}
			
			if(getTabPane().indexOfComponent(selectedComponent) > 0){
    			getTabPane().setSelectedComponent(selectedComponent);
    		}else if(getTabPane().getTabCount() > selectedIndex){
    			getTabPane().setSelectedIndex(selectedIndex);
    		}else {
    			getTabPane().setSelectedIndex(getTabPane().getTabCount()-1);
    		}
    	}
    } 
    
    /**
     * Makes the TimelineViewer the active viewer, connected to the multitier
     * control panel.
     */
    public void showTimeLineViewer() {
        showTimeLineViewer = true;
        showInterlinearViewer = false;

        enableDisableLogic();

        doLayout();
    }

    /**
     * Makes the InterlinearViewer the active viewer, connected to the multitier
     * control panel.
     */
    public void showInterlinearViewer() {
        showTimeLineViewer = false;
        showInterlinearViewer = true;

        enableDisableLogic();

        doLayout();
    }
    
    private void enableDisableLogic() {
        if (showTimeLineViewer) {
            if (timeLineViewer != null) {
                viewerManager.enableViewer(timeLineViewer);
            }

            if (interlinearViewer != null) {
            	interlinearViewer.isClosing();
                viewerManager.disableViewer(interlinearViewer);
            }
            layoutManager.setPreference("LayoutManager.VisibleMultiTierViewer", 
            		TimeLineViewer.class.getName(), viewerManager.getTranscription());
            if (timeLineViewer != null) {
            	timeLineViewer.preferencesChanged();
            }
        } else if (showInterlinearViewer) {
            if (timeLineViewer != null) {
            	timeLineViewer.isClosing();
                viewerManager.disableViewer(timeLineViewer);
            }

            if (interlinearViewer != null) {
                viewerManager.enableViewer(interlinearViewer);
            }
            layoutManager.setPreference("LayoutManager.VisibleMultiTierViewer", 
            		InterlinearViewer.class.getName(), viewerManager.getTranscription());
            if (interlinearViewer != null) {
            	interlinearViewer.preferencesChanged();
            }
        }
    }
    
    /**
     * Detaches the specified viewer or player.
     * 
     * @param object the viewer or player to remove from the main application frame
     */
    @Override
    public void detach(Object object) {
    	if (object instanceof AbstractViewer) {
    		container.remove((Component)object);

    		if (object instanceof TimeSeriesViewer) {
    			ViewerLayoutModel vlm = null;
    			for (int i = 0; i < viewerList.size(); i++) {
    				vlm = viewerList.get(i);
    				if (vlm.viewer == object) {
    					break;
    				} else {
    					vlm = null;
    				}
    			}
    			if (vlm == null) {
    				return;
    			}
    			
    			if (wav_tsSplitPane != null) {
    				int curHeight = getTimeLineSplitPane().getDividerLocation();
    				wav_tsSplitPane.setTopComponent(null);
    				wav_tsSplitPane.setBottomComponent(null);
    				timeseriesComponent.remove(timeseriesViewer);
    				timeseriesComponent = null;
    				wav_tsSplitPane = null;
    				
    				if (wavSpectSplitPane != null) {
    					getTimeLineSplitPane().setTopComponent(wavSpectSplitPane);
    					getTimeLineSplitPane().setDividerLocation(curHeight);
    					wavSpectSplitPane.setSize(wavSpectSplitPane.getWidth(), curHeight);
    				} else
    				if (signalComponent != null) {
    					getTimeLineSplitPane().setTopComponent(signalComponent);
    					getTimeLineSplitPane().setDividerLocation(curHeight);
    					signalComponent.setSize(signalComponent.getWidth(), curHeight);
    				} else
    				if (spectrogramHostComponent != null) {
    					getTimeLineSplitPane().setTopComponent(spectrogramHostComponent);
    					getTimeLineSplitPane().setDividerLocation(curHeight);
    					spectrogramHostComponent.setSize(spectrogramHostComponent.getWidth(), curHeight);
    				} else {
    					getTimeLineSplitPane().setTopComponent(null);
    				}   				
    			} else {
    				getTimeLineSplitPane().setTopComponent(null);
    				if (timeseriesComponent != null) {
	    				timeseriesComponent.remove(timeseriesViewer);
	    				timeseriesComponent = null;
    				}
    			}
    			doLayout();
    			vlm.detach();
    		}
    	}        
    }
    
    /**
     * Attaches the specified viewer or player. 
     *
     * @param object the viewer or player to attach
     */
    @Override
    public void attach(Object object) {
    	
		if (object instanceof AbstractViewer) {
			// detach from frame/dialog, destroy dialog and add to container
			// use a ViewerLayoutModel
			ViewerLayoutModel vlm = null;
			for (int i = 0; i < viewerList.size(); i++) {
				vlm = viewerList.get(i);
				if (vlm.viewer == object) {
					break;
				} else {
					vlm = null;
				}
			}
			if (vlm == null) {
				return;
			}
			// make sure that the dialog/frame has been destroyed
			vlm.attach();
			
			if (vlm.viewer instanceof TimeSeriesViewer) {
				//setPreference("TimeSeriesViewer.Detached", Boolean.FALSE, viewerManager.getTranscription()); 
				setTimeSeriesViewer((TimeSeriesViewer) vlm.viewer);
			}
			//container.add((Component)object);
		} 
    }
    
    /**
     * The actual update of the layout depending on attached viewers, current
     * dimensions and preference settings.
     */
	@Override
	public void doLayout() {
		
		if(!layoutManager.isIntialized()){
			return;
		}
		
        // get the width and height of the usable area
        int containerWidth = container.getWidth();
        int containerHeight = container.getHeight();
        int containerMargin = 3;
        int componentMargin = 5;

        PlayerLayoutModel[] visualPlayers = layoutManager.getAttachedVisualPlayers();
		int numVisualPlayers = visualPlayers.length;	
		
		// first layout the player components, next the tabpane
		int mediaAreaHeight = layoutManager.getMediaAreaHeight();
		int visibleMediaX = containerMargin;
		int visibleMediaY = containerMargin;
		int visibleMediaWidth = 0;
		int visibleMediaHeight = mediaAreaHeight;

		int firstMediaWidth = visibleMediaWidth;
		int firstMediaHeight = mediaAreaHeight;
					
		if (oneRowForVisuals) {
			if (numVisualPlayers >= 1) {
				int maxPerMedia = (containerWidth - minTabWidth) / numVisualPlayers;
				int maxUsedHeight = 0;
				float aspectRatio;
				Component visComp;
				for (int i = 0; i < numVisualPlayers && i < 4; i++) {
					visComp = visualPlayers[i].visualComponent;
					aspectRatio = visualPlayers[i].player.getAspectRatio();
					if (aspectRatio == 0) {
						aspectRatio = 1.0f;
					}
					int curWidth = 0, curHeight = 0;
					if (mediaAreaHeight * aspectRatio > maxPerMedia) {
						curWidth = maxPerMedia;
						curHeight = (int) (maxPerMedia / aspectRatio);
						maxUsedHeight = curHeight > maxUsedHeight ? curHeight : maxUsedHeight;
					} else {
						curWidth = (int) (mediaAreaHeight * aspectRatio);
						curHeight = mediaAreaHeight;
					}					
					if (i == 0) {		
						visibleMediaWidth = visibleMediaX + curWidth + componentMargin;
						visComp.setBounds(visibleMediaX, visibleMediaY, curWidth, curHeight);
						firstMediaWidth = curWidth;// used by the time panel
					} else {
						visComp.setBounds(visibleMediaX + visibleMediaWidth, visibleMediaY, curWidth, curHeight);
						visibleMediaWidth = visibleMediaWidth + curWidth + componentMargin;
					}
				}
				// recalculate X coordinates now that the total width of the videos is known
				if (mediaInCentre) {
					visibleMediaX = (containerWidth - visibleMediaWidth) / 2;
					int shiftX = 0;
					for (int i = 0; i < numVisualPlayers && i < 4; i++) {
						if (i == 0) {
							shiftX = visibleMediaX - visualPlayers[i].visualComponent.getX();
						}
						Point p = visualPlayers[i].visualComponent.getLocation();
						visualPlayers[i].visualComponent.setLocation(p.x + shiftX, p.y);
					}
				}
			}
		} else {
			//if (numVisualPlayers == 0) {
			//	visibleMediaHeight = mediaAreaHeight;
			//}
			int maxWidthForMedia = containerWidth - minTabWidth;
			if (numVisualPlayers >= 1) {
				// layout the first video
				Component firstVisualComp = visualPlayers[0].visualComponent;
				float aspectRatio = visualPlayers[0].player.getAspectRatio();
				if (aspectRatio == 0) {
					aspectRatio = 1.0f;
				}
				firstMediaWidth = ElanLayoutManager.MASTER_MEDIA_WIDTH;
				// jan 2007 if the source- or encoded-width of the video is more than twice the MASTER_
				// MEDIA_WIDTH constant, then divide the real source width by 2 for optimal rendering
				if (visualPlayers[0].player.getSourceWidth() > 2 * ElanLayoutManager.MASTER_MEDIA_WIDTH && 
				        mediaAreaHeight == ElanLayoutManager.MASTER_MEDIA_HEIGHT) {
				    firstMediaWidth = visualPlayers[0].player.getSourceWidth() / 2;
				    //System.out.println("adj. width: " + firstMediaWidth);
				} else {
				    firstMediaWidth = (int) (firstMediaHeight * aspectRatio);
				}
				// force inside media area
				firstMediaWidth = firstMediaWidth > maxWidthForMedia ? maxWidthForMedia : firstMediaWidth;
			    firstMediaHeight = (int) (firstMediaWidth / aspectRatio);
			    // revert if the height > media area height
			    if (firstMediaHeight > mediaAreaHeight) {
			    	firstMediaHeight = mediaAreaHeight;
			    	firstMediaWidth = (int) (firstMediaHeight * aspectRatio);
			    }
				
				visibleMediaWidth = firstMediaWidth + componentMargin;	
				visibleMediaHeight = firstMediaHeight;
				if(numVisualPlayers == 1){
					if(mediaInCentre){	
						visibleMediaX = (containerWidth - visibleMediaWidth)/2;
					}
					firstVisualComp.setBounds(containerMargin+visibleMediaX, visibleMediaY, firstMediaWidth,
							firstMediaHeight);				
				}
				//System.out.println("width: " + firstMediaWidth + " height: " + firstMediaHeight);
			}
			if (numVisualPlayers == 2) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float secondAR = visualPlayers[1].player.getAspectRatio();
							
				int secondMediaWidth = (int) (visibleMediaHeight * secondAR);
				int secondMediaHeight = visibleMediaHeight; 
				// try to use exactly half of the width in some cases
				if (visualPlayers[1].player.getSourceWidth() > 2 * ElanLayoutManager.MASTER_MEDIA_WIDTH && 
						visualPlayers[1].player.getSourceWidth() > visualPlayers[0].player.getSourceWidth()) {
					secondMediaWidth = visualPlayers[1].player.getSourceWidth() / 2;
					secondMediaHeight = (int) (secondMediaWidth / secondAR);
					// revert if the height > mediaAreaHeight
					if (secondMediaHeight > mediaAreaHeight) {
						secondMediaHeight = mediaAreaHeight;
						secondMediaWidth = (int) (secondMediaHeight * secondAR);
					}
				}
				// force the two video's inside the available area, try to maintain same height, possibly different widths
				if (firstMediaWidth + secondMediaWidth + componentMargin > maxWidthForMedia) {
					// opt. 1: calculate the ratio to fit the two video's in the available width
					float sizeRatio = (maxWidthForMedia - componentMargin) / (float)(firstMediaWidth + secondMediaWidth);
					firstMediaWidth = (int) (sizeRatio * firstMediaWidth);
					firstMediaHeight = (int) (firstMediaWidth / visualPlayers[0].player.getAspectRatio());
					
					visibleMediaWidth = firstMediaWidth + componentMargin;					
					secondMediaWidth = (int) (sizeRatio * secondMediaWidth);
					secondMediaHeight = (int) (secondMediaWidth / secondAR);
					// due to rounding effects the two heights might not be equal
					secondMediaHeight = firstMediaHeight;					
					// opt. 2: equal width for both videos, possibly different heights, doesn't make much sense
					/*
					firstMediaWidth = (maxWidthForMedia - componentMargin) / 2;
					visibleMediaWidth = firstMediaWidth + componentMargin;
					firstMediaHeight = (int) (firstMediaWidth / visualPlayers[0].player.getAspectRatio());
					if (firstMediaHeight > visibleMediaHeight) {
						firstMediaHeight = visibleMediaHeight;
						firstMediaWidth = (int) (firstMediaHeight * visualPlayers[0].player.getAspectRatio());
					}
					
					secondMediaWidth = firstMediaWidth;
					secondMediaHeight = (int) (secondMediaWidth / secondAR);
					if (secondMediaHeight > visibleMediaHeight) {
						secondMediaHeight = visibleMediaHeight;
						secondMediaWidth = (int) (secondMediaHeight * secondAR);
					}
					*/
				}				
				
				if (mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth + secondMediaWidth))/2;
				}				
				visualPlayers[0].visualComponent.setBounds(containerMargin+visibleMediaX, visibleMediaY, firstMediaWidth,
						firstMediaHeight);
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth,
					visibleMediaY, secondMediaWidth, secondMediaHeight);
				visibleMediaWidth += (secondMediaWidth + componentMargin);
				//System.out.println("sec width: " + secondMediaWidth + " sec height: " + secondMediaHeight);
			}
			else if (numVisualPlayers == 3) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float secondAR = visualPlayers[1].player.getAspectRatio();
				Component thirdVisualComp = visualPlayers[2].visualComponent;
				float thirdAR = visualPlayers[2].player.getAspectRatio();
				int heightPerPlayer = (visibleMediaHeight - componentMargin) / 2;
				int secondWidth = (int)(secondAR * heightPerPlayer);
				int thirdWidth = (int) (thirdAR * heightPerPlayer);
				int widthPerPlayer = Math.max(secondWidth, thirdWidth);
				if(mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth+widthPerPlayer))/2;
				}
				visualPlayers[0].visualComponent.setBounds(visibleMediaX, visibleMediaY, firstMediaWidth,
							(int) (firstMediaWidth / visualPlayers[0].player.getAspectRatio()));						
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - secondWidth) / 2, visibleMediaY, 
					secondWidth, heightPerPlayer);
				thirdVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - thirdWidth) / 2, 
					visibleMediaY + heightPerPlayer + componentMargin, 
					thirdWidth, heightPerPlayer);
				visibleMediaWidth += widthPerPlayer + componentMargin;
			}
			else if (numVisualPlayers >= 4) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float secondAR = visualPlayers[1].player.getAspectRatio();
				Component thirdVisualComp = visualPlayers[2].visualComponent;
				float thirdAR = visualPlayers[2].player.getAspectRatio();
				Component fourthVisualComp = visualPlayers[3].visualComponent;
				float fourthAR = visualPlayers[3].player.getAspectRatio();
				int heightPerPlayer = (visibleMediaHeight - 2 * componentMargin) / 3;
				int secondWidth = (int)(secondAR * heightPerPlayer);
				int thirdWidth = (int) (thirdAR * heightPerPlayer);
				int fourthWidth = (int) (fourthAR * heightPerPlayer);
				int widthPerPlayer = Math.max(secondWidth, thirdWidth);
				widthPerPlayer = Math.max(widthPerPlayer, fourthWidth);
				if(mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth+widthPerPlayer))/2;
				}
				visualPlayers[0].visualComponent.setBounds(visibleMediaX, visibleMediaY, firstMediaWidth,
							(int) (firstMediaWidth / visualPlayers[0].player.getAspectRatio()));	
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - secondWidth) / 2, visibleMediaY, 
					secondWidth, heightPerPlayer);
				thirdVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - thirdWidth) / 2, 
					visibleMediaY + heightPerPlayer + componentMargin, 
					thirdWidth, heightPerPlayer);
				fourthVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - fourthWidth) / 2, 
					visibleMediaY + 2 * heightPerPlayer + 2 * componentMargin, 
					fourthWidth, heightPerPlayer);
				visibleMediaWidth += widthPerPlayer + componentMargin;
			}
	    }
        // layout the tab panel
		
		int tabPaneX = visibleMediaX + visibleMediaWidth;
        int tabPaneY = visibleMediaY;
        int tabPaneWidth = containerWidth - tabPaneX ;
        int tabPaneHeight = visibleMediaHeight;

        if (mediaInCentre){
        	if(numVisualPlayers > 0){
        		tabPaneWidth = visibleMediaX;
        	} else {
        		tabPaneWidth = tabPaneWidth/2;
        		tabPaneX = tabPaneWidth;
        	}    
        	tabPaneX = tabPaneX - containerMargin;
        	
        	getLeftTabPane().setBounds(containerMargin, containerMargin, tabPaneWidth, tabPaneHeight);
        } else {
        	destroyLeftPane();
        }
        
        if (tabPane != null) {
            tabPane.setBounds(tabPaneX, tabPaneY, tabPaneWidth, tabPaneHeight);

           if (mediaPlayerController != null && controlPanel != null) {
               controlPanel.setSize(tabPaneWidth, tabPaneHeight);
           }
        } 
        if (numOfPlayers != numVisualPlayers && numOfPlayers == 0){
        	preferenceChanged = true;
        }
        
        if (preferenceChanged || numVisualPlayers == 0){
        	reArrangeViewers(); 
        	preferenceChanged = false;
        }

        int timePanelX = 0;
        int timePanelY = visibleMediaY + visibleMediaHeight + 2;
        int timePanelWidth = 0;
        int timePanelHeight = 0;
        

        if (mediaPlayerController != null) {
            timePanelWidth = mediaPlayerController.getTimePanel()
                                                  .getPreferredSize().width;
            timePanelHeight = mediaPlayerController.getTimePanel()
                                                   .getPreferredSize().height;
			if (numVisualPlayers == 0) {
				timePanelX = containerMargin;
			} else {				
				if(mediaInCentre){		        	
		        	timePanelX = visibleMediaX;
		        } else{
		        	timePanelX = (containerMargin + (firstMediaWidth / 2)) -
		        			(timePanelWidth / 2);
		        }
			}
	        
            mediaPlayerController.getTimePanel().setBounds(timePanelX,
                timePanelY, timePanelWidth, timePanelHeight);
        }

        int playButtonsX = ElanLayoutManager.CONTAINER_MARGIN;
        int playButtonsY = timePanelY + timePanelHeight + 4;
        int playButtonsWidth = 0;
        int playButtonsHeight = 0;

        if (mediaPlayerController != null) {
            playButtonsWidth = mediaPlayerController.getPlayButtonsPanel()
                                                    .getPreferredSize().width;
            playButtonsHeight = mediaPlayerController.getPlayButtonsPanel()
                                                     .getPreferredSize().height;

			if (numVisualPlayers > 0) {
				if(mediaInCentre){	
					playButtonsX = (visibleMediaX );
				}else{
					playButtonsX = (containerMargin + (firstMediaWidth / 2)) -
						(playButtonsWidth / 2);				
					if (playButtonsX < ElanLayoutManager.CONTAINER_MARGIN) {
						playButtonsX = ElanLayoutManager.CONTAINER_MARGIN;
					}
				}
			}

            mediaPlayerController.getPlayButtonsPanel().setBounds(playButtonsX,
                playButtonsY, playButtonsWidth, playButtonsHeight);
        }

        int selectionPanelX = playButtonsX + playButtonsWidth + 20;
        int selectionPanelY = visibleMediaY + visibleMediaHeight + 2;
        int selectionPanelWidth = 0;
        int selectionPanelHeight = 0;

        if (mediaPlayerController != null) {
            selectionPanelWidth = 100 +
                mediaPlayerController.getSelectionPanel().getPreferredSize().width;
            selectionPanelHeight = mediaPlayerController.getSelectionPanel()
                                                        .getPreferredSize().height;
            mediaPlayerController.getSelectionPanel().setBounds(selectionPanelX,
                selectionPanelY, selectionPanelWidth, selectionPanelHeight);
        }

        int selectionButtonsX = selectionPanelX;
        int selectionButtonsY = selectionPanelY + selectionPanelHeight + 4;
        int selectionButtonsWidth = 0;
        int selectionButtonsHeight = 0;

        if (mediaPlayerController != null) {
            selectionButtonsWidth = mediaPlayerController.getSelectionButtonsPanel()
                                                         .getPreferredSize().width;
            selectionButtonsHeight = mediaPlayerController.getSelectionButtonsPanel()
                                                          .getPreferredSize().height;
            mediaPlayerController.getSelectionButtonsPanel().setBounds(selectionButtonsX,
                selectionButtonsY, selectionButtonsWidth, selectionButtonsHeight);
        }

        int annotationButtonsX = selectionButtonsX + selectionButtonsWidth + 15;
        int annotationButtonsY = selectionPanelY + selectionPanelHeight + 4;
        int annotationButtonsWidth = 0;
        int annotationButtonsHeight = 0;

        if (mediaPlayerController != null) {
            annotationButtonsWidth = mediaPlayerController.getAnnotationNavigationPanel()
                                                          .getPreferredSize().width;
            annotationButtonsHeight = mediaPlayerController.getAnnotationNavigationPanel()
                                                           .getPreferredSize().height;
            mediaPlayerController.getAnnotationNavigationPanel().setBounds(annotationButtonsX,
                annotationButtonsY, annotationButtonsWidth,
                annotationButtonsHeight);
        }

        int modePanelX = annotationButtonsX + annotationButtonsWidth + 10;
        int modePanelY = annotationButtonsY;
        int modePanelWidth = 0;
        int modePanelHeight = 0;

        if (mediaPlayerController != null) {
            //modePanelWidth = 300; //
        	modePanelWidth = mediaPlayerController.getModePanel().getPreferredSize().width;
            modePanelHeight = mediaPlayerController.getModePanel()
                                                   .getPreferredSize().height;
            if (modePanelHeight > annotationButtonsHeight && annotationButtonsHeight > 0) {
            	modePanelY -= (modePanelHeight - annotationButtonsHeight) / 2;
            }
            mediaPlayerController.getModePanel().setBounds(modePanelX,
                modePanelY, modePanelWidth, modePanelHeight);
        }
        
      //layout for the volume button & slider
		int butVolumeX = modePanelX + modePanelWidth + 10;
		int butVolumeY = annotationButtonsY;
		int butVolumeWidth = 0;
		int butVolumeHeight = 0;
    
		if (mediaPlayerController != null) {
			butVolumeWidth = mediaPlayerController.getButtonSize().width;
			butVolumeHeight =  mediaPlayerController.getButtonSize().height; 
			mediaPlayerController.getVolumeIconPanel().setBounds(butVolumeX, butVolumeY, butVolumeWidth, butVolumeHeight); 
		}
		
        if((butVolumeX+butVolumeWidth) > containerWidth){
        	if (mediaPlayerController != null) {
        		if (numVisualPlayers == 0) {
    				timePanelX = containerMargin;
    			} else {				
    				timePanelX = (containerMargin + (firstMediaWidth / 2)) -
    					(timePanelWidth / 2);
    		    }
        		
                mediaPlayerController.getTimePanel().setBounds(timePanelX,
                    timePanelY, timePanelWidth, timePanelHeight);
        		
        		
        		playButtonsX = (containerMargin + (firstMediaWidth / 2)) -
    						(playButtonsWidth / 2);				
        		if (playButtonsX < ElanLayoutManager.CONTAINER_MARGIN) {
        			playButtonsX = ElanLayoutManager.CONTAINER_MARGIN;
        		}
        		mediaPlayerController.getPlayButtonsPanel().setBounds(playButtonsX,
        				playButtonsY, playButtonsWidth, playButtonsHeight);
            	
        		selectionPanelX = playButtonsX + playButtonsWidth + 20;
                mediaPlayerController.getSelectionPanel().setBounds(selectionPanelX,
                    selectionPanelY, selectionPanelWidth, selectionPanelHeight);
                
                selectionButtonsX = selectionPanelX;
                mediaPlayerController.getSelectionButtonsPanel().setBounds(selectionButtonsX,
                    selectionButtonsY, selectionButtonsWidth, selectionButtonsHeight);
                
                annotationButtonsX = selectionButtonsX + selectionButtonsWidth + 15;
                mediaPlayerController.getAnnotationNavigationPanel().setBounds(annotationButtonsX,
                    annotationButtonsY, annotationButtonsWidth,
                    annotationButtonsHeight);
                
                modePanelX = annotationButtonsX + annotationButtonsWidth + 10;
                mediaPlayerController.getModePanel().setBounds(modePanelX,
                    modePanelY, modePanelWidth, modePanelHeight);
                
                butVolumeX = modePanelX+modePanelWidth;
        		mediaPlayerController.getVolumeIconPanel().setBounds(butVolumeX, butVolumeY, butVolumeWidth, butVolumeHeight); 
        	}
        } 
        
        // resize divider
        int divX = 0 ; 
        int divY = playButtonsY + playButtonsHeight +4; 
        int divHeight = vertMediaResizer.getPreferredSize().height;
        vertMediaResizer.setBounds(divX, divY, containerWidth, divHeight);

        
        int sliderPanelX = ElanLayoutManager.CONTAINER_MARGIN;
        int sliderPanelY = divY + divHeight +4;
        int sliderPanelWidth = 0;
        int sliderPanelHeight = 0;

        if (mediaPlayerController != null) {
            sliderPanelWidth = containerWidth -  (2 * ElanLayoutManager.CONTAINER_MARGIN);
            sliderPanelHeight = mediaPlayerController.getSliderPanel()
                                                     .getPreferredSize().height;
            mediaPlayerController.getSliderPanel().setBounds(sliderPanelX,
                sliderPanelY, sliderPanelWidth, sliderPanelHeight);
        }
        

        int densityPanelX = ElanLayoutManager.CONTAINER_MARGIN;
        int densityPanelY = sliderPanelY - 3;// - ElanLayoutManager.BELOW_BUTTONS_MARGIN; //sliderPanelHeight;
        int densityPanelWidth = sliderPanelWidth;
        int densityPanelHeight = 0;

        if (mediaPlayerController != null) {
            densityPanelHeight = mediaPlayerController.getAnnotationDensityViewer()
                                                      .getPreferredSize().height;
            mediaPlayerController.getAnnotationDensityViewer().setBounds(densityPanelX,
                densityPanelY, densityPanelWidth, densityPanelHeight);
        }

        // layout time line split pane
        int timeLineSplitPaneX = ElanLayoutManager.CONTAINER_MARGIN;
        int timeLineSplitPaneY = densityPanelY + densityPanelHeight + 4;
        int timeLineSplitPaneWidth = 0;
        int timeLineSplitPaneHeight = 0;

        if (timeLineSplitPane != null) {
            timeLineSplitPaneWidth = containerWidth - (2 * ElanLayoutManager.CONTAINER_MARGIN);
            timeLineSplitPaneHeight = containerHeight - timeLineSplitPaneY;
            timeLineSplitPane.setBounds(timeLineSplitPaneX, timeLineSplitPaneY,
                timeLineSplitPaneWidth, timeLineSplitPaneHeight);
        }

        // layout time line pane
        int multiTierControlX = 0;
        int multiTierControlY = 0;
        int multiTierControlWidth = 0;
        int multiTierControlHeight = 0;
        int timeLineX = 0;
        int timeLineY = 0;
        int timeLineWidth = 0;
        int timeLineHeight = 0;
        int interlinearX = 0;
        int interlinearY = 0;
        int interlinearWidth = 0;
        int interlinearHeight = 0;

        if (timeLineComponent != null) {
            int bottomHeight = timeLineSplitPane.getHeight() -
                timeLineSplitPane.getDividerLocation() -
                timeLineSplitPane.getDividerSize();
            Insets insets = timeLineSplitPane.getInsets();
            timeLineComponent.setSize(timeLineSplitPane.getWidth() - insets.left - insets.top, 
            		bottomHeight - insets.bottom);
            timeLineComponent.setPreferredSize(timeLineComponent.getSize());
            multiTierControlWidth = layoutManager.getMultiTierControlPanelWidth();
            multiTierControlHeight = bottomHeight; //timeLineComponent.getHeight();
            multiTierControlPanel.setSize(multiTierControlWidth, multiTierControlHeight);
            multiTierControlPanel.setBounds(multiTierControlX,
                multiTierControlY, multiTierControlWidth, multiTierControlHeight);

            if (showTimeLineViewer) {
                timeLineX = multiTierControlWidth;

                //timeLineWidth = timeLineComponent.getWidth() - multiTierControlWidth;
                timeLineWidth = timeLineSplitPane.getWidth() -
                    multiTierControlWidth;
                timeLineHeight = bottomHeight; //timeLineComponent.getHeight();
            } else {
                interlinearX = multiTierControlWidth;

                //interlinearWidth = timeLineComponent.getWidth() - multiTierControlWidth;
                interlinearWidth = timeLineSplitPane.getWidth() -
                    multiTierControlWidth;
                interlinearHeight = bottomHeight; //timeLineComponent.getHeight();
            }

            if (timeLineViewer != null) {
//                timeLineViewer.setBounds(timeLineX, timeLineY, timeLineWidth,
//                    timeLineHeight);
//				timeLineViewer.setPreferredSize(
//					new Dimension(timeLineWidth, timeLineHeight));
                timeLineScrollPane.setBounds(timeLineX, timeLineY, timeLineWidth,
                        timeLineHeight);
    			timeLineScrollPane.setPreferredSize(
    					new Dimension(timeLineWidth, timeLineHeight));
                // force a component event on the viewer, does not happen automatically apparently
                //timeLineViewer.componentResized(null);
            }

            if (interlinearViewer != null) {
                interlinearViewer.setBounds(interlinearX, interlinearY,
                    interlinearWidth, interlinearHeight);
					interlinearViewer.setPreferredSize(
						new Dimension(interlinearWidth, interlinearHeight));
                // force a component event on the viewer, does not happen automatically apparently
                interlinearViewer.componentResized(null);
            }
        }

        // layout signal pane
        int signalX = multiTierControlWidth;
        int signalY = 0;
        int signalWidth = 0;
        int signalHeight = 0;
        //int audioWidth = 0;
        int audioHeight = 0;

        if (wav_tsSplitPane != null && wavSpectSplitPane != null 
        		&& timeseriesComponent != null) { //overly checked
        	int topHeight = wav_tsSplitPane.getDividerLocation();
        	//layout the 2 viewers in the pane
            int rMargin = 0;

            if (timeLineViewer != null) {
                rMargin = timeLineViewer.getRightMargin();
            }
			signalWidth = timeLineSplitPane.getWidth() - multiTierControlWidth -
				rMargin;
			audioHeight = wav_tsSplitPane.getHeight() - wav_tsSplitPane.getDividerSize() -
					topHeight;// height of wav_spec_split

			if (signalViewer != null) {
				signalHeight = wavSpectSplitPane.getHeight() - wavSpectSplitPane.getDividerSize() - 
						wavSpectSplitPane.getDividerLocation();
				signalControlPanel.setBounds(0, 0, multiTierControlWidth, signalHeight);
				signalViewer.setBounds(signalX, signalY, signalWidth, signalHeight);
				signalComponent.setPreferredSize(new Dimension(wavSpectSplitPane.getWidth(), signalHeight));
			}			
			if (spectrogramViewer != null) {
				int specHeight = wavSpectSplitPane.getDividerLocation();
				spectrogramControlPanel.setBounds(0,  0, multiTierControlWidth, specHeight);
				spectrogramViewer.setBounds(signalX, 0, signalWidth, specHeight);
				spectrogramHostComponent.setPreferredSize(new Dimension(wavSpectSplitPane.getWidth(), specHeight));
			}			
			wavSpectSplitPane.setPreferredSize(new Dimension(timeLineSplitPaneWidth, audioHeight));
						
			int tsWidth = 0;
			timeseriesViewer.setRightMargin(rMargin);
			timeseriesViewer.setVerticalRulerWidth(multiTierControlWidth);
			tsWidth = timeLineSplitPane.getWidth();
			//signalHeight = timeseriesComponent.getHeight();
			//int tsHeight = wav_tsSplitPane.getHeight() - wav_tsSplitPane.getDividerLocation() - 
			//    wav_tsSplitPane.getDividerSize();
			timeseriesViewer.setBounds(0, signalY, tsWidth, topHeight);
			timeseriesComponent.setPreferredSize(new Dimension(tsWidth, topHeight));
			
        } else if (wavSpectSplitPane != null) {
            int rMargin = 0;

            if (timeLineViewer != null) {
                rMargin = timeLineViewer.getRightMargin();
            } 

			//		 signalWidth = signalComponent.getWidth() - multiTierControlWidth - rMargin;
			signalWidth = timeLineSplitPane.getWidth() - multiTierControlWidth -
				rMargin;// timeLineWidth - rMargin
			audioHeight = /*timeLineSplitPane.getHeight() -*/ timeLineSplitPane.getDividerLocation() /*- 
					timeLineSplitPane.getDividerSize()*/;
			signalHeight = wavSpectSplitPane.getHeight() - wavSpectSplitPane.getDividerSize() - 
					wavSpectSplitPane.getDividerLocation();
			if (signalViewer != null) {
				signalControlPanel.setBounds(0, 0, multiTierControlWidth, signalHeight);
				signalViewer.setBounds(signalX, signalY, signalWidth, signalHeight);
				signalComponent.setPreferredSize(new Dimension(timeLineSplitPaneWidth, signalHeight));	
			}
			if (spectrogramViewer != null) {
				int specHeight = wavSpectSplitPane.getDividerLocation();
				if (specHeight == 0 && signalViewer == null) {
					specHeight = signalHeight;
				}
				spectrogramControlPanel.setBounds(0,  0, multiTierControlWidth, specHeight);
				spectrogramViewer.setBounds(signalX, 0, signalWidth, specHeight);
				spectrogramHostComponent.setPreferredSize(new Dimension(wavSpectSplitPane.getWidth(), specHeight));
			}
			wavSpectSplitPane.setBounds(0,  0, timeLineSplitPane.getWidth(), audioHeight);
        } else if (timeseriesComponent != null && timeseriesViewer != null){
            int rMargin = 0;
            int tsWidth = 0;
            
            if (timeLineViewer != null) {
                rMargin = timeLineViewer.getRightMargin();
                timeseriesViewer.setRightMargin(rMargin);
            } 

			timeseriesViewer.setVerticalRulerWidth(multiTierControlWidth);
			tsWidth = timeLineSplitPane.getWidth();
			signalHeight = timeseriesComponent.getHeight();
			

			timeseriesViewer.setBounds(0, signalY, tsWidth, signalHeight);
			timeseriesComponent.setPreferredSize(new Dimension(tsWidth, signalHeight));
        }
        
        if (timeLineSplitPane != null) {
        	timeLineSplitPane.resetToPreferredSizes();
        }

        // layout control panel components
        /*
        if (mediaPlayerController != null) {
        	
            int vX = 10;
            int vY = 30;
            int vWidth = controlPanel.getWidth() - (2 * vX);
            int vHeight = mediaPlayerController.getVolumePanel()
                                               .getPreferredSize().height;
            mediaPlayerController.getVolumePanel().setBounds(vX, vY, vWidth,
                vHeight);
			

            int rX = 10;
            int rY = vY + vHeight + 10;
            int rWidth = controlPanel.getWidth() - (2 * vX); //mediaPlayerController.getRatePanel().getPreferredSize().width;
            int rHeight = mediaPlayerController.getRatePanel().getPreferredSize().height;
            mediaPlayerController.getRatePanel().setBounds(rX, rY, rWidth,
                rHeight);
                
        }
        */
        
        if(mediaInCentre){
        	Integer selIndex = Preferences.getInt(
					"LayoutManager.SelectedLeftTabIndex", viewerManager.getTranscription());
			if (selIndex != null) {
				int index = selIndex.intValue();
				if (index >= 0 && index < getLeftTabPane().getTabCount()) {
					getLeftTabPane().setSelectedIndex(index);
				}  else if(getLeftTabPane().getTabCount() > 0){
					getLeftTabPane().setSelectedIndex(0);
				}
			}
        }
        numOfPlayers = numVisualPlayers;
		container.validate();
		
	}

	/**
	 * Updates locale sensitive UI elements.
	 */
    @Override
	public void updateLocale() {
		 if (tabPane != null) {
	            int nTabs = tabPane.getTabCount();

	            for (int i = 0; i < nTabs; i++) {
	                Component component = tabPane.getComponentAt(i);

	                if (component == gridPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Grid"));
	                } else if (component == textPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Text"));
	                } else if (component == subtitlePanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Subtitles"));
	                } else if (component == controlPanelScrollPane ||
	                		   component == controlPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Controls"));
	                } else if (component == recognizerPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Recognizer"));
	                } else if (component == metadataPanel) {
	                	tabPane.setTitleAt(i, ElanLocale.getString("Tab.Metadata"));
	                } else if (component == lexiconPanel) {
	                	tabPane.setTitleAt(i, ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER));
	                } else if (component == commentPanel) {
	                	tabPane.setTitleAt(i, ElanLocale.getString(ELANCommandFactory.COMMENT_VIEWER));
	                }
	            }
	        }
	}
	
    /**
     * Removes all viewers and panels from the content pane.
     */
	@Override
	public void clearLayout() {	
		if(layoutManager.containsComponent(getTabPane())){
			container.remove(getTabPane());
		}
		
		if(layoutManager.containsComponent(getLeftTabPane())){
			container.remove(getLeftTabPane());
		}		
				
		if(layoutManager.containsComponent(timeLineSplitPane)){
			container.remove(timeLineSplitPane);
		}
		
		if(layoutManager.containsComponent(vertMediaResizer)){
			container.remove(vertMediaResizer);
		}	
		
		destroyAndRemoveViewer(ELANCommandFactory.INTERLINEAR_VIEWER);		
		destroyAndRemoveViewer(ELANCommandFactory.GRID_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.TEXT_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.SUBTITLE_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.LEXICON_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.COMMENT_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.RECOGNIZER);	
		destroyAndRemoveViewer(ELANCommandFactory.METADATA_VIEWER);
//		destroyAndRemoveViewer(ELANCommandFactory.SIGNAL_VIEWER);//??
		destroyAndRemoveViewer(ELANCommandFactory.SPECTROGRAM_VIEWER);
		
		removeMediaPlayerController();		
		
		if(signalViewer != null){
			container.remove(signalViewer);// not applicable in this manager?
		}		
		timeseriesViewer = null;
		
		if (timeLineComponent != null) {
			timeLineComponent.removeAll();
		}
		
		multiTierControlPanel = null;	
		
		timeLineViewer = null;
		viewerManager.destroyViewerByName(ELANCommandFactory.TIMELINE_VIEWER);	
		viewerManager.destroyMultiTierControlPanel();
		
		container.repaint();
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	public void cleanUpOnClose() {
	}
	
	/**
	 * Called before clearing the layout and switching to a new mode or called 
	 * before the file and/or ELAN is closed.
	 * The media player is stopped and preferences are stored.
	 */
    @Override
	public void isClosing() {
		mediaPlayerController.stopLoop();		
		
		layoutManager.setPreference("LayoutManager.SelectedTabIndex", getTabPane().getSelectedIndex(), viewerManager.getTranscription());
		
		if(mediaInCentre && getLeftTabPane().getSelectedIndex() >= 0){
			layoutManager.setPreference("LayoutManager.SelectedLeftTabIndex", getLeftTabPane().getSelectedIndex(), viewerManager.getTranscription());
		}
		
		Boolean val = Preferences.getBool("InlineEdit.DeselectCommits", null);
        if (val != null && val) {
        	if(gridPanel != null){
        		((GridViewer)gridPanel.getViewer()).isClosing();
        	}
        	
        	if(interlinearViewer != null){
        		interlinearViewer.isClosing();
        	}
        	
        	if(subtitlePanel != null){
        		Component panel[] = subtitlePanel.getComponents();
				for (Component element : panel) {
					if(element instanceof SingleTierViewerPanel){
						((SubtitleViewer)((SingleTierViewerPanel)element).getViewer()).isClosing();
					}
				}
        	}
        }
        
        if(timeLineViewer != null){
    		timeLineViewer.isClosing();
    	}
        
        if (commentPanel != null){
        	commentPanel.isClosing();
    	}
        
        if(recognizerPanel!= null &&  recognizerPanel instanceof AbstractRecognizerPanel){
        	((AbstractRecognizerPanel)recognizerPanel).isClosing();
        }
        
        if(timeLineSplitPane!= null){
			layoutManager.setPreference("LayoutManager.SplitPaneDividerLocation", 
					timeLineSplitPane.getDividerLocation() , viewerManager.getTranscription());
		}
		
        if(wav_tsSplitPane != null){
			layoutManager.setPreference("LayoutManager.TSWavSplitPaneDividerLocation", 
					wav_tsSplitPane.getDividerLocation(), viewerManager.getTranscription());
		}
        
        if (wavSpectSplitPane != null && wavSpectSplitPane.getTopComponent() != null && 
        		wavSpectSplitPane.getBottomComponent() != null) {
			layoutManager.setPreference("LayoutManager.AudioSplitPaneDividerLocation", 
					wavSpectSplitPane.getDividerLocation() , viewerManager.getTranscription());
        }
		
        
	}
	
	private void createSubtitleViewer(){
    	int numSubtitles = 4;
        Integer val = Preferences.getInt("NumberOfSubtitleViewers", null);
        if (val != null) {
        	numSubtitles = val.intValue();
        }
        
    	SubtitleViewer subtitleViewer;
    	for (int i = 0; i < numSubtitles; i++) {
    		subtitleViewer = viewerManager.createSubtitleViewer();
    		if(subtitleViewer != null){
    			subtitleViewer.setViewerIndex(i + 1);
    			subtitleViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
    			add(subtitleViewer);
    		}else {
    			break;
    		}
    	}
    }
	
	/**
	 * Creates, mostly via the viewer manager, and adds viewers and other
	 * UI components.
	 */
    @Override
	public void initComponents() {	
		
		controlPanel = new JPanel();
		controlPanel.setName(ElanLocale.getString("Tab.Controls"));
		//controlPanel.setLayout(new GridLayout(3, 1, 10, 10));
		controlPanel.setLayout(new GridBagLayout());
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));    
	
    	vertMediaResizer = new ResizeComponent(layoutManager, SwingConstants.VERTICAL);
    	vertMediaResizer.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    	vertMediaResizer.setPreferredSize(new Dimension(container.getWidth(), 7));
   	
    	Component n = vertMediaResizer.getComponent(0);
        vertMediaResizer.remove(n);
        vertMediaResizer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0; 
        vertMediaResizer.add(n, gbc);
        
        container.add(vertMediaResizer);
		        
	    Boolean val = Preferences.getBool("Media.VideosCentre", null);                
	    if (val != null) {
	    	mediaInCentre = val.booleanValue();	
	    } 
	    
	    ksNotToBeConsumed = new ArrayList<KeyStroke>();
	    loadKSNottoBeConsumed();
	    
	    viewerManager.getMultiTierControlPanel().preferencesChanged(); 
		
	    layoutManager.add(viewerManager.getMediaPlayerController());
	    viewerManager.getMediaPlayerController().preferencesChanged();
	    
		TimeLineViewer timeLineViewer = viewerManager.createTimeLineViewer();
		add(timeLineViewer); 
		this.timeLineViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
		createAndAddViewer(ELANCommandFactory.INTERLINEAR_VIEWER);	 
        
		// visible multitier viewer
	    String conViewerName = Preferences.getString("LayoutManager.VisibleMultiTierViewer",
	    		viewerManager.getTranscription());
	    if(conViewerName == null){
	    	showTimeLineViewer();	
	    	viewerManager.getMultiTierControlPanel().disableShowInterlinearViewer(true);
	    } else if (conViewerName.equals(TimeLineViewer.class.getName())) {
	    	if (timeLineViewer != null) {
	    		showTimeLineViewer();
	    		multiTierControlPanel.setViewer(timeLineViewer);
	    	}
	    } else if (conViewerName.equals(InterlinearViewer.class.getName())) {
	    	if (interlinearViewer != null) {
	    		showInterlinearViewer();
	    		getMultiTierControlPanel().setViewer(interlinearViewer);
	    	}
	    }   
	     
	    createAndAddViewer(ELANCommandFactory.GRID_VIEWER);
	    createAndAddViewer(ELANCommandFactory.TEXT_VIEWER);
	    createAndAddViewer(ELANCommandFactory.SUBTITLE_VIEWER);
	    createAndAddViewer(ELANCommandFactory.LEXICON_VIEWER);
	    createAndAddViewer(ELANCommandFactory.COMMENT_VIEWER);
	    createAndAddViewer(ELANCommandFactory.SIGNAL_VIEWER);
	    createAndAddViewer(ELANCommandFactory.SPECTROGRAM_VIEWER);
	    createAndAddViewer(ELANCommandFactory.RECOGNIZER);
	    createAndAddViewer(ELANCommandFactory.METADATA_VIEWER);
	    
	    layoutManager.add(layoutManager.getTimeSeriesViewer());
	    
	    if (timeLineViewer != null) {
	    	timeLineViewer.requestFocus();
	    }
	   	
	    preferencesChanged();
	}

    /**
     * Updates the layout based on the current user preferences.
     */
    @Override
	public void preferencesChanged() {
		Integer selIndex = Preferences.getInt(
				"LayoutManager.SelectedTabIndex", viewerManager.getTranscription());
		if (selIndex != null) {
			int index = selIndex.intValue();
			if (index >= 0 && index < tabPane.getTabCount()) {
				tabPane.setSelectedIndex(index);
			}  else {
				tabPane.setSelectedIndex(0);
			}
		}
		
		// visible multitier viewer
		String conViewerName = Preferences.getString("LayoutManager.VisibleMultiTierViewer",
				viewerManager.getTranscription());
		if (conViewerName != null && conViewerName.equals(TimeLineViewer.class.getName())) {
			if (timeLineViewer != null) {
				showTimeLineViewer();
				getMultiTierControlPanel().setViewer(timeLineViewer);
			}
		} else if (conViewerName != null && conViewerName.equals(InterlinearViewer.class.getName())) {
			if (interlinearViewer != null) {
				showInterlinearViewer();
				getMultiTierControlPanel().setViewer(interlinearViewer);
			}
		}
		
		Integer sigHeight = Preferences.getInt("LayoutManager.SplitPaneDividerLocation", 
				viewerManager.getTranscription());
		if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
			if ((signalViewer != null || timeseriesViewer != null) && timeLineSplitPane != null) {
				timeLineSplitPane.setDividerLocation(sigHeight.intValue());
			}
		}
		
		sigHeight = Preferences.getInt("LayoutManager.TSWavSplitPaneDividerLocation", 
				viewerManager.getTranscription());
		if (sigHeight != null && wav_tsSplitPane != null) {
			wav_tsSplitPane.setDividerLocation(sigHeight.intValue());
		}
		
		sigHeight = Preferences.getInt("LayoutManager.AudioSplitPaneDividerLocation", 
				viewerManager.getTranscription());
		if (sigHeight != null && wavSpectSplitPane != null) {
			wavSpectSplitPane.setDividerLocation(sigHeight.intValue());
		}
		
		// have to set the tier for single tier viewers here because the viewer and the tier 
		// selection box are separate objects
		if (tabPane != null) {
			for (int i = 0; i < tabPane.getTabCount(); i++) {
				JComponent comp = (JComponent) tabPane.getComponent(i);
				SingleTierViewerPanel panel;
				
				if (comp instanceof SingleTierViewerPanel) {
					panel = (SingleTierViewerPanel) comp;
					if (panel.getViewer() instanceof GridViewer) {	
						Boolean multiGrid = Preferences.getBool("GridViewer.MultiTierMode", 
								viewerManager.getTranscription());
						if (multiGrid != null && multiGrid) {
							Boolean mtmSubdivision = Preferences.getBool("GridViewer.MultiTierMode.Subdivision", 
									viewerManager.getTranscription());
							if (mtmSubdivision == null || ! mtmSubdivision) {
								panel.setTierMode(GridViewer.MULTI_TIER_ASSOCIATION_MODE);
								((GridViewer) panel.getViewer()).setModeFromPref(GridViewer.MULTI_TIER_ASSOCIATION_MODE);
							} else {
								panel.setTierMode(GridViewer.MULTI_TIER_SUBDIVISION_MODE);
								((GridViewer) panel.getViewer()).setModeFromPref(GridViewer.MULTI_TIER_SUBDIVISION_MODE);
							}
						} else {
							((GridViewer) panel.getViewer()).setModeFromPref(GridViewer.SINGLE_TIER_MODE);// maybe superfluous
						}
						
						String tierName = Preferences.getString("GridViewer.TierName",
								viewerManager.getTranscription());
						if (tierName != null) {
							panel.selectTier(tierName);
						}
					} else if (panel.getViewer() instanceof TextViewer){
						String tierName = Preferences.getString("TextViewer.TierName",
								viewerManager.getTranscription());
						if (tierName != null) {
							panel.selectTier(tierName);
						}	
					}
				} else if (comp == subtitlePanel) {
					Component[] subComps = comp.getComponents();
					String tierName;
					int index = 0;
					for (Component subComp : subComps) {
						if (subComp instanceof SingleTierViewerPanel) {
							panel = (SingleTierViewerPanel) subComp;							
							if (panel.getViewer() instanceof SubtitleViewer) {
								index = ((SubtitleViewer) panel.getViewer()).getViewerIndex();
								tierName = Preferences.getString(("SubTitleViewer.TierName-" + index),
										viewerManager.getTranscription());
								if (tierName != null) {
									panel.selectTier(tierName);
								}
							}							
						}
					}
				} 	
			}		
		}
		
		Boolean sameSize = Preferences.getBool("Media.VideosSameSize", null);
		
	    if (sameSize != null) {
	        oneRowForVisuals = sameSize.booleanValue();
	    }
	    
	    List<String> los = Preferences.getListOfString("PreferencesDialog.Viewer.SortOrder", null);
	    if (los != null) {
	    	viewerSortOrder = los;
	    }	        
	    Boolean boolPref = Preferences.getBool("Media.VideosCentre", null);                
	    if (boolPref != null) {
	    	mediaInCentre = boolPref.booleanValue();	
	    } 
	    
	    if(mediaInCentre){	    	
	    	boolPref = Preferences.getBool("PreferencesDialog.Viewer.Grid.Right", null);
	    	if (boolPref != null) {
	    		gridViewerLeft = !boolPref.booleanValue();        	
	    	} 
	    
	    	boolPref = Preferences.getBool("PreferencesDialog.Viewer.Text.Right", null);
	    	if (boolPref != null) {
	    		textViewerLeft = !boolPref.booleanValue();        	
	    	}   
	    
	    	boolPref = Preferences.getBool("PreferencesDialog.Viewer.Subtitle.Right", null);
	    	if (boolPref != null) {
	    		subtitleViewerLeft = !boolPref.booleanValue();        	
	    	} 
	    	
	    	boolPref = Preferences.getBool("PreferencesDialog.Viewer.Lexicon.Right", null);
	    	if (boolPref != null) {
	    		lexiconViewerLeft = !boolPref.booleanValue();        	
	    	} 
	    
	    	boolPref = Preferences.getBool("PreferencesDialog.Viewer.Comment.Right", null);
	    	if (boolPref != null) {
	    		commentViewerLeft = !boolPref.booleanValue();        	
	    	} 
	    
	    	boolPref = Preferences.getBool("PreferencesDialog.Viewer.Audio.Right", null);
	    	if (boolPref != null) {
	    		recognizerLeft = !boolPref.booleanValue();        	
	    	}
	    
	    	boolPref = Preferences.getBool("PreferencesDialog.Viewer.MetaData.Right", null);
	    	if (boolPref != null) {
	    		metaDataLeft = !boolPref.booleanValue();        	
	    	} 
	    }
	    
	    preferenceChanged = true;		
	}

    @Override
	public void enableOrDisableMenus(boolean enabled) {
	}	
	
	/**
	 * This methods loads the list of shortcuts for the command actions which are can be
	 * called from a inline editbox while editing. All the other command actions which can be triggered by
	 * a keystroke are ignored because few actions where not relevant while editing. 
	 * 
	 * This list has to be updated with all possible actions that can be called
	 * while editing.
	 * 
	 * @author aarsom
	 * @since April 2012, for version 4.3.0
	 * 
	 */
	private void loadKSNottoBeConsumed(){		
		ksNotToBeConsumed.clear();
    	ShortcutsUtil scu = ShortcutsUtil.getInstance();
    	KeyStroke ks = null;
    	String modeName = ELANCommandFactory.ANNOTATION_MODE;
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.PLAY_SELECTION, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}  	
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.PLAY_AROUND_SELECTION, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.SELECTION_BOUNDARY, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.SELECTION_CENTER, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.CENTER_SELECTION, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.LOOP_MODE, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
	}

	/**
	 * Updates the shortcuts of all supported actions while editing in 
	 * all Inline edit boxes.
	 * <br>
	 * Added by:  aarsom
	 * @since April 2012, added for version 4.3.0	
	 */
    @Override
	public void shortcutsChanged() {
		loadKSNottoBeConsumed();
		if(timeLineViewer != null){
			timeLineViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
		}
		
		if(viewerManager.getGridViewer() != null){
			viewerManager.getGridViewer().setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
		}
		
		List<SubtitleViewer> sViewers = viewerManager.getSubtitleViewers();
		for(SubtitleViewer v : sViewers){
			if(v != null){
				v.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
			}
		}
		
		if(interlinearViewer != null){
			interlinearViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
		}	
	}

    /**
     * Returns a list of zoomable viewers; timeline, signal, timeseries
     * and/or interlinear viewer.
     * 
     * @return a list of viewers that support zooming in and out
     */
	@Override
	public List<Zoomable> getZoomableViewers() {
		List<Zoomable> zoomList = new ArrayList<Zoomable>(6);
		if (timeLineViewer != null) {// also test if this is the active multitier viewer
			zoomList.add(timeLineViewer);
		}
		if (signalViewer != null) {
			zoomList.add(signalViewer);
		}
		if (timeseriesViewer != null) {
			zoomList.add(timeseriesViewer);
		}
		if (interlinearViewer != null) {
//			zoomList.add(interlinearViewer);
		}
		
		return zoomList;
	}
}
