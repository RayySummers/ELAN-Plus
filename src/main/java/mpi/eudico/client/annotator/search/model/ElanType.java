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
package mpi.eudico.client.annotator.search.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.BasicControlledVocabulary;
import mpi.eudico.util.CVEntry;
import mpi.search.SearchLocale;


/**
 * This class describes Transcription-specific Types and Relations of
 * tiers and possible units of distance between them. It is meant for the
 * (one) Transcription open in ELAN. 
 */
public class ElanType extends EAFType {
    private Map<String, Locale> langHash = new HashMap<String, Locale>();

    private final TranscriptionImpl transcription;

    /**
     * The constructor builds a tree with the tiers of the transcription as
     * nodes. 
     * The root node itself is an empty node.
     *
     * @param transcription the source transcription
     */
    public ElanType(TranscriptionImpl transcription) {
        this.transcription = transcription;

        List<TierImpl> tierVector = transcription.getTiers();
        tierNames = new String[tierVector.size()];
        Locale loc;
        for (int i = 0; i < tierVector.size(); i++) {
            TierImpl tier = tierVector.get(i);
            tierNames[i] = tier.getName();

            loc = tier.getDefaultLocale();
            if (loc != null) {
                langHash.put(tierNames[i],
                    loc);
            }
        }
    }

    /**
     * Returns a list of CV entries for a tier.
     *
     * @param tierName the tier name
     *
     * @return a list of {@code CVEntry} objects or {@code null}
     */
    @Override
	public List<CVEntry> getClosedVoc(String tierName) {
        TierImpl tier = transcription.getTierWithId(tierName);
        String cvName = tier.getLinguisticType().getControlledVocabularyName();
        BasicControlledVocabulary cv = transcription.getControlledVocabulary(cvName);

        return (cv != null) ? Arrays.asList(cv.getEntries()) : null;
    }

    /**
     * Returns whether a tier is connected to a CV.
     *
     * @param tierName the name of the tier
     *
     * @return {@code true} if the tier is in the transcription and is connected
     * to a Controlled Vocabulary
     */
    @Override
	public boolean isClosedVoc(String tierName) {
        TierImpl tier = transcription.getTierWithId(tierName);
        if(tier == null) {
			return false;
		}

        String cvName = tier.getLinguisticType().getControlledVocabularyName();
        return transcription.getControlledVocabulary(cvName) != null;
    }

    @Override
	public Locale getDefaultLocale(String tierName) {
        return langHash.get(tierName);
    }

    /**
     * Returns common ancestors of two tiers.
     *
     * @param tierName1 the first tier name 
     * @param tierName2 the second tier name
     *
     * @return an array of unit tier names, can be empty
     */
    @Override
	public String[] getPossibleUnitsFor(String tierName1, String tierName2) {
        List<String> commonAncestors = new ArrayList<String>();

        TierImpl tier1 = (transcription.getTierWithId(tierName1));
        TierImpl tier2 = (transcription.getTierWithId(tierName2));

        TierImpl loopTier = tier1;

        do {
            if (loopTier.equals(tier2) || tier2.hasAncestor(loopTier)) {
                commonAncestors.add(loopTier.getName() + " " +
                    SearchLocale.getString("Search.Annotation_PL"));
            }
        } while ((loopTier = loopTier.getParentTier()) != null);

        String[] possibleUnits = commonAncestors.toArray(new String[0]);
        standardUnit = (possibleUnits.length > 0)
            ? (String) commonAncestors.get(0) : null;

        return possibleUnits;
    }

    /**
     * Returns an array of names of related tiers.
     * This is the tier tree or tier hierarchy the specified tier is part of,
     * with the root tier at index 0.
     *
     * @param tierName the tier to find related tiers for
     *
     * @return an array of tier names in the same tier tree
     */
    @Override
	public String[] getRelatedTiers(String tierName) {
        String[] relatedTiers = new String[0];

        try {
            TierImpl tier = transcription.getTierWithId(tierName);
            TierImpl rootTier = tier.getRootTier();
            List<TierImpl> dependentTiers = rootTier.getDependentTiers();

            relatedTiers = new String[dependentTiers.size() + 1];
            relatedTiers[0] = rootTier.getName();

            for (int i = 0; i < dependentTiers.size(); i++) {
                relatedTiers[i + 1] = dependentTiers.get(i).getName();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return relatedTiers;
    }

    /**
     * Returns the transcription of this type object.
     * 
     * @return the transcription
     */
	public Transcription getTranscription() {
		return transcription;
	}
}
