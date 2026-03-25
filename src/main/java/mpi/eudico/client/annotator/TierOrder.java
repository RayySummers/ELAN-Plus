package mpi.eudico.client.annotator;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

/**
 * Class to store the current visualization order of tiers in the transcription.
 * 
 * @author aarsom
 */
public class TierOrder implements ACMEditListener{
    private List<TierOrderListener> listeners;
    
    // list of all tier names(List<String>)
    private List<String> tierOrder;
    
    private Transcription transcripton;

    /**
     * Creates a new TierOrder instance.
     * 
     * @param trans the transcription
     */
    public TierOrder(Transcription trans) {
        listeners = new ArrayList<TierOrderListener>();
        tierOrder = null;
        transcripton = trans;
    }

    /**
     * Sets the {@code TierOrder}.
     *
     * @param tierOrderList the ordered list of tiers 
     */
    public void setTierOrder(List<String> tierOrderList) {
        tierOrder = tierOrderList;
        
        // Tell all the interested TierOrder about the change
        notifyListeners();
    }

    /**
     * Gets the {@code TierOrder}.
     *
     * @return tierOrder the ordered list of all tier names (return type 
     * {@code List<String>})
     */
    public List<String> getTierOrder() {
        return tierOrder;
    }

    /**
     * Tell all TierOrderListeners about a change in the {@code TierOrder}.
     */
    public void notifyListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).updateTierOrder(tierOrder);
        }
    }

    /**
     * Add a listener for {@code TierOrder} events.
     *
     * @param listener the listener that wants to be notified of
     *        {@code TierOrder} events
     */
    public void addTierOrderListener(TierOrderListener listener) {
        listeners.add(listener);
        listener.updateTierOrder(tierOrder);
    }

    /**
     * Remove a listener for {@code TierOrder} events.
     *
     * @param listener the listener that no longer wants to be notified of
     *        {@code TierOrder} events
     */
    public void removeTierorderListener(
        TierOrderListener listener) {
        listeners.remove(listener);
    }

	
	@Override
	public void ACMEdited(ACMEditEvent e) {		
		switch(e.getOperation()){
		case ACMEditEvent.REMOVE_TIER:
			Object obj  = e.getModification();			
			if(obj instanceof TierImpl){
				String tierName = ((Tier) obj).getName();
				if(tierOrder.contains(tierName)){
					tierOrder.remove(tierName);
				}				
			}
			break;
		case ACMEditEvent.ADD_TIER:	
			obj  = e.getModification();			
			if(obj instanceof TierImpl){
				String tierName = ((Tier) obj).getName();
				if(!tierOrder.contains(tierName)){
					tierOrder.add(tierName);
				}				
			}
			break;
		case ACMEditEvent.CHANGE_TIER:
			obj  = e.getSource();
			if(obj instanceof TierImpl){
				String tierName = ((Tier) obj).getName();
				List<? extends Tier> tiers = transcripton.getTiers();
				List<String> tierNames = new ArrayList<String>();
				for(int i=0; i< tiers.size(); i++){
					String name = tiers.get(i).getName();
					tierNames.add(name);
				}
				
				for(int i=0; i < tierOrder.size(); i++){
					if(!tierNames.contains(tierOrder.get(i))){
						tierOrder.remove(i);
						tierOrder.add(i,tierName);
					}
				}
			}
			break;		
		}
	}
}
