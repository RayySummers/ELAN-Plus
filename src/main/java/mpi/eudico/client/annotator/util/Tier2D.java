package mpi.eudico.client.annotator.util;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Wraps or embeds a Tier object and maintains some additional display and
 * painting specific fields and properties.
 *
 * @author Han Sloetjes
 * @version 0.1 10/7/2003
 */
public class Tier2D {
    private TierImpl tier;
    private ArrayList<Tag2D> tags;
    private String name;
    private boolean isActiveTier;
    //private boolean isVisible; //may not be needed

    /**
     * Creates a new Tier2D instance.
     *
     * @param tier the tier to wrap
     */
    public Tier2D(TierImpl tier) {
        this.tier = tier;
        name = tier.getName();

        isActiveTier = false;
        //isVisible = true;
        tags = new ArrayList<Tag2D>(20);
    }

    /**
     * Returns the embedded tier.
     *
     * @return the wrapped tier
     */
    public TierImpl getTier() {
        return tier;
    }

    /**
     * Adds a {@code Tag2D} object, which wraps one annotation of the tier.
     *
     * @param tag the {@code Tag2D} to add
     */
    public void addTag(Tag2D tag) {
        tags.add(tag);
        tag.setTier2D(this);
    }

    /**
     * Insert the {@code Tag2D} into the list.
     * Determine the right index by means of the x position.
     *
     * @param tag the T{@code Tag2D} to insert
     */
    public void insertTag(Tag2D tag) {
        tag.setTier2D(this);

        Tag2D t1;
        Tag2D t2;

        for (int i = 0; i < tags.size(); i++) {
            t1 = tags.get(i);

            if (((i == 0) || (i == (tags.size() - 1))) &&
                    (tag.getX() < t1.getX())) {
                tags.add(i, tag);

                return;
            }

            if (i < (tags.size() - 1)) {
                t2 = tags.get(i + 1);

                if ((tag.getX() > t1.getX()) && (tag.getX() < t2.getX())) {
                    tags.add(i + 1, tag);

                    return;
                }
            }
        }

        tags.add(tag);
    }

    /**
     * Removes a {@code Tag2D} from this tier.
     *
     * @param tag the annotation, {@code Tag2D}, to remove
     */
    public void removeTag(Tag2D tag) {
        tags.remove(tag);
    }

    /**
     * Returns an iterator of the list of tags (annotations).
     *
     * @return an iterator of the tags
     */
    public Iterator<Tag2D> getTags() {
        return tags.iterator();
    }

    /**
     * Returns the list of {@code Tag2D} objects.
     *
     * @return the list of {@code Tag2D}s, not a copy
     */
    public List<Tag2D> getTagsList() {
        return tags;
    }

    /**
     * Returns the name of the tier.
     *
     * @return the name of the tier
     */
    public String getName() {
        return name;
    }

    /**
     * Update the name of this Tier2D object after a change in the name of the
     * Tier.
     */
    public void updateName() {
        String old = name;

        if (!old.equals(tier.getName())) {
            name = tier.getName();
        }
    }

    /**
     * Marks this {@code Tier2D} as the active or selected tier. 
     *
     * @param active if {@code true} this tier is the active tier,
     * otherwise it is (re)set to its normal, inactive state 
     */
    public void setActive(boolean active) {
        isActiveTier = active;
    }

    /**
     * Returns whether this tier is the active tier.
     *
     * @return {@code true} if this is the active tier, {@code false} otherwise
     * (the default state)
     */
    public boolean isActive() {
        return isActiveTier;
    }
}
