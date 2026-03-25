package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A class that extends AnnotationDataRecord with an index,  that denotes its position in the tier output order  and that
 * implements Comparable.<br> Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Han Sloetjes
 * @author Allan van Hulst
 * @version 13-01-2019 some simple modifications
 */
class IndexedExportRecord extends AnnotationDataRecord implements Comparable<IndexedExportRecord>,
                                                                  Serializable {

    @Serial
    private static final long serialVersionUID = 202310251036L;

    private final ExportTradTranscript exportTradTranscript;
    private final int index;
    private long silAfter = -1;

    /**
     * Constructor.
     *
     * @param annotation the annotation
     * @param index the index in the tier order
     */
    IndexedExportRecord(ExportTradTranscript exportTradTranscript, Annotation annotation, int index) {
        super(annotation);
        this.exportTradTranscript = exportTradTranscript;
        this.index = index;
    }

    /**
     * Returns the index in the tier order.
     *
     * @return the index in the tier order
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the duration of silence between this record and the next one. The default value is -1 (which stands for "no
     * silence indicated").
     *
     * @param value the duration between this and the next annotation
     */
    public void setSilenceAfter(long value) {
        silAfter = value;
    }

    /**
     * Returns the duration of silence between this record and the next one. -1 means "no silence indicated".
     *
     * @return the duration of silence between this record and the next one
     */
    public long getSilenceAfter() {
        return silAfter;
    }

    /**
     * Performs a multiple step comparison:
     *
     * <p>- compare the begin times
     * - when they are the same, compare the end times - when they are the same, compare the index - if end times not the
     * same, (OR if the index is the same, which is unlikely), compare parent/child relationship. Parent comes before child.
     * - when still not decided, look at end times again.
     *
     * @param other the IndexedExportRecord to compare with
     *
     * @return a negative integer, zero, or a positive integer as this record is less than,  equal to, or greater than the
     *     specified record
     */
    public int compareTo(IndexedExportRecord other) {
        int result = 0;

        if (this.getBeginTime() < other.getBeginTime()) {
            result = -1;
        } else if (this.getBeginTime() > other.getBeginTime()) {
            result = 1;
        }

        if (this.getEndTime() == other.getEndTime()) {
            if (this.index < other.getIndex()) {
                result = -1;
            } else if (this.index > other.getIndex()) {
                result = 1;
            }
        }

        /* if begin time is same child if it is a child annotation child ann */
        TierImpl thisTier = exportTradTranscript.transcription.getTierWithId(this.getTierName());
        TierImpl otherTier = exportTradTranscript.transcription.getTierWithId(other.getTierName());

        if (areParentAndChild(thisTier, otherTier)) {
            result = -1;
        } else if (areParentAndChild(otherTier, thisTier)) {
            result = 1;
        } else if (this.getEndTime() < other.getEndTime()) {
            result = -1;
        } else if (this.getEndTime() > other.getEndTime()) {
            result = 1;
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexedExportRecord that = (IndexedExportRecord) o;
        return getIndex() == that.getIndex() && silAfter == that.silAfter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), silAfter);
    }

    /**
     * Determine is the tiers are in a parent/(sub)child relation.
     *
     * @param parent the potential parent
     * @param child the potential child
     *
     * @return whether they are
     */
    private boolean areParentAndChild(Tier parent, Tier child) {
        while (child != null) {
            Tier childsParent = child.getParentTier();
            if (childsParent == parent) {
                return true;
            }

            child = childsParent;
        }
        return false;
    }
}
