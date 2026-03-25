/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.search.content.result.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import mpi.search.result.model.Result;

/**
 * Results of an annotation content search.
 * 
 * @author Alexander Klassmann
 * @version Jul 27, 2004
 */
@SuppressWarnings("serial")
public class ContentResult extends Result {
	/** list of tier names */
    private final List<String> tierNames = new ArrayList<String>();
    /** occurrence count */
    private int occurrenceCount = 0;
    private final ReentrantLock matchLock = new ReentrantLock();

    /**
     * Creates a new content result instance.
     */
    public ContentResult() {
		super();
	}

	/**
     * Adds a match to this content search result.
     * 
     * @param match the match to add
     */
    @Override
	public void addMatch(ContentMatch match) {
    	matchLock.lock();
    	try {
	        super.addMatch(match);
	
	        if (match != null) {
	            final ContentMatch contentMatch = match;
				if (!tierNames.contains(contentMatch.getTierName())) {
	                tierNames.add(contentMatch.getTierName());
	            }
	
	            if (contentMatch.getMatchedSubstringIndices() != null) {
	                occurrenceCount += contentMatch.getMatchedSubstringIndices().length;
	            }
	        }
    	} finally {
    		matchLock.unlock();
    	}
    }

    /**
     * Returns a list of all matches on the specified tier.
     * 
     * @param tierName the name of the tier
     * 
     * @return a list of matches
     */
    public List<ContentMatch> getMatches(String tierName) {
        if (tierName == null) {
            return null;
        }

    	matchLock.lock();
    	try {
	        List<ContentMatch> matchesInTier = new ArrayList<ContentMatch>();
	
	        for (int i = 1; i <= getRealSize(); i++) {
	            if (tierName.equals(((ContentMatch) getMatch(i)).getTierName())) {
	                matchesInTier.add(getMatch(i));
	            }
	        }
	
	        return matchesInTier;
    	} finally {
    		matchLock.unlock();
    	}
    }

    /**
     * Removes all matches from this result, clears lists and sets the occurrence
     * count to 0.
     */
    @Override
	public void reset() {
    	matchLock.lock();
    	try {
	        super.reset();
	        tierNames.clear();
	        occurrenceCount = 0;
    	} finally {
    		matchLock.unlock();
    	}
    }

    /**
     * Returns a list of all tierNames present in matches. Ordered along first
     * occurrence.
     * 
     * @return a list of tier names
     */
    public String[] getTierNames() {
    	matchLock.lock();
    	try {
    		return (String[]) tierNames.toArray(new String[0]);
    	} finally {
    		matchLock.unlock();
    	}
    }

    /**
     * Returns the number of occurrences, which can be greater than the number
     * of content matches, since each content match (Annotation) can contain
     * multiple {@code hits}.
     * 
     * @return the number of match occurrences
     */
    public int getOccurrenceCount() {
    	matchLock.lock();
    	try {
    		return occurrenceCount;
    	} finally {
    		matchLock.unlock();
    	} 	
    }
}
