package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.Pair;
import nl.mpi.lexan.analyzers.helpers.Position;
import nl.mpi.lexan.analyzers.helpers.Suggestion;
import nl.mpi.lexan.analyzers.helpers.SuggestionSet;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An undoable command that creates annotations from a set of suggestions.
 *
 * <p>If the annotations are created on a depending tier, first existing annotations are
 * deleted. Therefore it works best if all output tiers are (direct) children of the input tier, and preferably, the second
 * and subsequent outputs are children of the first output. This is because multiple outputs are modeled as children of each
 * other.
 *
 * <p>Child suggestions are processed.
 *
 * @author Han Sloetjes
 */
public class AnnotationsFromSuggestionSetCommand implements UndoableCommand {
    private final String name;
    private TranscriptionImpl transcription;
    private Object[] arguments;
    private List<DefaultMutableTreeNode> deletedForUndo;
    private List<DefaultMutableTreeNode> modifiedForUndo;
    private List<Annotation> createdForUndo;
    private List<Annotation> createdForRecursion;

    /**
     * Constructor accepting the name.
     *
     * @param name the name/id of the command
     */
    public AnnotationsFromSuggestionSetCommand(String name) {
        super();
        this.name = name;
    }

    /**
     * @param receiver the transcription object
     * @param arguments <ol>
     *     <li>arguments[0] = SuggestionSet: sequential new annotations and their children.
     *     Each level of suggestion is supposed to stick to a single tier.
     *     <li>arguments[1] (optional) = Boolean: make available a list of the created annotations.
     *     </ol>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        this.arguments = arguments;

        SuggestionSet sugSet = (SuggestionSet) arguments[0];
        boolean recursive = (arguments.length >= 2) ? (Boolean) arguments[1] : false;
        // Get the "top level" suggestions. They are supposed to be all on the same tier,
        // so we look only at the first one.
        List<Suggestion> topSugs = sugSet.getSuggestions();

        if (topSugs != null && !topSugs.isEmpty()) {
            Suggestion first = topSugs.get(0);
            Position pos = first.getPosition();

            TierImpl topChTier = transcription.getTierWithId(pos.getTierId());

            if (topChTier != null) {
                TierImpl topParent = topChTier.getParentTier();

                if (topParent != null) {
                    // The suggestions are for depending tiers.
                    AbstractAnnotation aaPar =
                        (AbstractAnnotation) topParent.getAnnotationAtTime((pos.getBeginTime() + pos.getEndTime()) / 2);

                    if (aaPar != null) {
                        List<Annotation> children = aaPar.getChildrenOnTier(topChTier);

                        if (!children.isEmpty()) {
                            // store for undo and remove
                            deletedForUndo = new ArrayList<>();

                            for (Annotation aa : children) {
                                // For undo:
                                deletedForUndo.add(AnnotationRecreator.createTreeForAnnotation((AbstractAnnotation) aa));
                                topChTier.removeAnnotation(aa);
                            }
                        }
                        // Could create annotation data records and use AnnotationRecreator to
                        // create the annotations...
                    }
                } else {
                    // The suggestions are for a top level tier.

                    // XXX Do we never remove annotations from it then?
                    // Since the GUI currently requires that target tiers are dependents of
                    // the source tier, this case cannot normally happen.
                    // Do we need to do something different for this case anyway? Yes, existing
                    // annotations can overlap partially, unlike in the other case.
                    List<Annotation> overlapAnnList =
                        topChTier.getOverlappingAnnotations(pos.getBeginTime(), pos.getEndTime());
                    // if not empty add to deletedForUndo, except maybe for the first and/or last annotation
                    if (!overlapAnnList.isEmpty()) {
                        // store for undo and remove
                        deletedForUndo = new ArrayList<>();
                        modifiedForUndo = new ArrayList<>();

                        for (Annotation annotation : overlapAnnList) {
                            AbstractAnnotation aa = (AbstractAnnotation) annotation;
                            // if partially overlaps, add to modified list, else add to deleted list
                            if (aa.getBeginTimeBoundary() < pos.getBeginTime()) {
                                //&& aa.getEndTimeBoundary() > pos.getBeginTime()) // this is inferred
                                modifiedForUndo.add(AnnotationRecreator.createTreeForAnnotation(aa));
                            } else if (aa.getEndTimeBoundary() > pos.getEndTime()) {
                                modifiedForUndo.add(AnnotationRecreator.createTreeForAnnotation(aa));
                            } else {
                                // add to deleted list
                                deletedForUndo.add(AnnotationRecreator.createTreeForAnnotation(aa));
                                topChTier.removeAnnotation(aa);
                            }
                        }
                    }
                }
                // Create a single Suggestion from the set so that the same method can be applied
                // for creating annotations.
                Suggestion topSug = new Suggestion(sugSet.getSuggestions());

                List<Pair<Suggestion, AbstractAnnotation>> list = new ArrayList<Pair<Suggestion, AbstractAnnotation>>(1);
                list.add(Pair.makePair(topSug, null));

                // For undo:
                createdForUndo = new ArrayList<>();
                if (recursive) {
                    createdForRecursion = new ArrayList<>();
                }

                createAnnotationsForSuggestions(list);
            }
        }
    }

    /**
     * Create child annotations for the child-suggestions.
     *
     * @param sugAnnList a list of already processed suggestions and the corresponding annotations. This is used so that
     *     the children of those suggestions can be processed next.
     */
    private void createAnnotationsForSuggestions(List<Pair<Suggestion, AbstractAnnotation>> sugAnnList) {
        if (sugAnnList.isEmpty()) {
            return;
        }

        for (Pair<Suggestion, AbstractAnnotation> entry : sugAnnList) {

            Suggestion nextSuggestion = entry.getFirst();
            final List<Suggestion> children = nextSuggestion.getChildren();
            if (children == null || children.isEmpty()) {
                continue;
            }

            Map<String, List<Suggestion>> groupedChildren = groupPerTier(children);
            if (groupedChildren == null) {
                continue;
            }
            AbstractAnnotation parAnn = entry.getSecond();

            // iterate over the grouped children and then process the children per group

            for (Map.Entry<String, List<Suggestion>> childEntry : groupedChildren.entrySet()) {
                String tierName = childEntry.getKey();
                List<Suggestion> sugsPerTier = childEntry.getValue();
                TierImpl t = transcription.getTierWithId(tierName);

                if (t == null) {
                    // If the tier doesn't exist, we can't create annotations, and then
                    // there won't be any children of them either.
                    continue;
                }

                LinguisticType lt = t.getLinguisticType();
                Constraint con = lt.getConstraints();

                int numSugs = sugsPerTier.size();

                long bt = -1;
                long et = -1;

                if (parAnn != null) {
                    bt = parAnn.getBeginTimeBoundary();
                    et = parAnn.getEndTimeBoundary();
                } else {
                    bt = sugsPerTier.get(0).getPosition().getBeginTime();
                    et = sugsPerTier.get(numSugs - 1).getPosition().getEndTime();
                }

                long perAnn = (et - bt) / numSugs;

                AbstractAnnotation prevAnn = null;
                List<Pair<Suggestion, AbstractAnnotation>> createdAnns = new ArrayList<>(3);

                for (int i = 0; i < numSugs; i++) {
                    Suggestion curSug = sugsPerTier.get(i);
                    AbstractAnnotation curAnn = null;
                    boolean rememberForUndo = parAnn == null; // Remember an annotation if we didn't create its parent

                    // check type of tier
                    if (con == null || con.getStereoType() == Constraint.INCLUDED_IN) {
                        curAnn = (AbstractAnnotation) t.createAnnotation(bt + (i * perAnn), bt + ((i + 1) * perAnn));
                    } else if (con.getStereoType() == Constraint.TIME_SUBDIVISION) {
                        if (prevAnn != null) {
                            curAnn = (AbstractAnnotation) t.createAnnotationAfter(prevAnn);
                        } else {
                            curAnn = (AbstractAnnotation) t.createAnnotation(bt + (i * perAnn), bt + ((i + 1) * perAnn));
                        }
                    } else if (con.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) {
                        if (prevAnn != null) {
                            curAnn = (AbstractAnnotation) t.createAnnotationAfter(prevAnn);
                        } else {
                            long mid = (bt + et) / 2;
                            curAnn = (AbstractAnnotation) t.createAnnotation(mid, mid);
                            if (curAnn == null) {
                                // This can happen if the suggestion has a child, but the tier of the suggestion
                                // is not a child tier of the parent suggestion.
                                // The order of creation is still left-to-right, because the List of Pairs
                                // maintains the order. In fact, due to the depth-first traversal of the
                                // suggestion tree, the new annotation should be to the right of the existing
                                // ones (if the annotation tree is nicely in time order...).
                                // XXX Remaining problem: the code to remove old annotations only cleans up
                                // annotations on the tier of the top-level suggestion, and their children,
                                // so it doesn't reach the sort of tier we're handling here...
                                // ---
                                // Instead of inserting after the still-existing annotations,
                                // we could also just remove them (and remember for undo)...
                                AbstractAnnotation existing = (AbstractAnnotation) t.getAnnotationAtTime(bt);
                                final boolean removeForUndo = true;
                                if (existing != null) {
                                    if (!removeForUndo) {
                                        while (true) {
                                            AbstractAnnotation after = (AbstractAnnotation) t.getAnnotationAfter(existing);
                                            if (after == null) { // if nothing follows, insert here
                                                break;
                                            }
                                            // The following check should not even be needed, since child annotations
                                            // are only linked together if they have the same parent.
                                            long afterBegin = after.getBeginTimeBoundary();
                                            if (afterBegin >= et) {
                                                // if what follows is outside out time period, it's unrelated
                                                break;
                                            }
                                            existing = after;
                                        }
                                        curAnn = (AbstractAnnotation) t.createAnnotationAfter(existing);
                                    } else {
                                        List<Annotation> overlap = t.getOverlappingAnnotations(bt, et);
                                        for (Annotation toRemove : overlap) {
                                            deletedForUndo.add(AnnotationRecreator.createTreeForAnnotation((AbstractAnnotation) toRemove));
                                            t.removeAnnotation(toRemove);
                                        }
                                        curAnn = (AbstractAnnotation) t.createAnnotation(mid, mid); // should be non-null now
                                    }
                                    rememberForUndo = curAnn != null;
                                }
                            }
                        }
                    } else if (con.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
                        // should we assume multiple sym. associated annotations/suggestions at the same level
                        if (prevAnn == null) {
                            long mid = (bt + et) / 2;
                            curAnn = (AbstractAnnotation) t.createAnnotation(mid, mid);
                        }
                    }

                    if (curAnn != null) {
                        curAnn.setValue(curSug.getContent());

                        if (curSug.getChildren() != null && !curSug.getChildren().isEmpty()) {
                            createdAnns.add(Pair.makePair(curSug, curAnn));
                        }

                        if (rememberForUndo) {
                            createdForUndo.add(curAnn);
                        }
                        if (createdForRecursion != null) {
                            createdForRecursion.add(curAnn);
                        }
                    }

                    prevAnn = curAnn;
                }
                // create children
                createAnnotationsForSuggestions(createdAnns);
            }
        }

    }

    /**
     * Groups the suggestions in the list per tier and returns a map of tier name to suggestions mappings.
     *
     * @param suggestions a list of suggestions that might be for different tiers
     *
     * @return a map of tier name to suggestions mappings
     */
    private Map<String, List<Suggestion>> groupPerTier(List<Suggestion> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return null;
        }

        Map<String, List<Suggestion>> groupedSugs = new HashMap<>(6);

        for (Suggestion sug : suggestions) {
            final String tierId = sug.getPosition().getTierId();
            if (groupedSugs.containsKey(tierId)) {
                groupedSugs.get(tierId).add(sug);
            } else {
                List<Suggestion> sugGroup = new ArrayList<>(6);
                sugGroup.add(sug);
                groupedSugs.put(tierId, sugGroup);
            }
        }

        return groupedSugs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void undo() {
        // TODO remove the created annotations.
        // The list only needs to contain the most top-level annotations we create.
        // Their children will be removed automatically.
        if (createdForUndo != null) {
            for (Annotation ann : createdForUndo) {
                Tier tier = ann.getTier();
                tier.removeAnnotation(ann);
            }
        }

        // Restore the removed annotations
        int mode = transcription.getTimeChangePropagationMode();
        transcription.setTimeChangePropagationMode(Transcription.NORMAL);

        if (deletedForUndo != null) {
            if (!deletedForUndo.isEmpty()) {
                DefaultMutableTreeNode node = deletedForUndo.get(0);
                AnnotationDataRecord adr = (AnnotationDataRecord) node.getUserObject();
                TierImpl t = transcription.getTierWithId(adr.getTierName());
                LinguisticType lt = t.getLinguisticType();
                if (lt.getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) {
                    // recreate in reversed order
                    for (int i = deletedForUndo.size() - 1; i >= 0; i--) {
                        AnnotationRecreator.createAnnotationFromTree(transcription, deletedForUndo.get(i), true);
                    }
                } else {
                    for (DefaultMutableTreeNode tree : deletedForUndo) {
                        AnnotationRecreator.createAnnotationFromTree(transcription, tree, true);
                    }
                }
            }
            // this should do if the AnnotationRecreator would work correctly
            //            for (DefaultMutableTreeNode tree : deletedForUndo) {
            //                AnnotationRecreator.createAnnotationFromTree(transcription, tree, true);
            //            }
        }

        if (modifiedForUndo != null && !modifiedForUndo.isEmpty()) {
            // in principle just recreating the original, modified annotation should work
            // just as well but we could delete the remaining, modified annotation first
            for (DefaultMutableTreeNode tree : modifiedForUndo) {
                AnnotationRecreator.createAnnotationFromTree(transcription, tree, true);
            }
        }

        // finally reset time change propagation mode
        transcription.setTimeChangePropagationMode(mode);
    }

    @Override
    public void redo() {
        execute(transcription, arguments);
    }

    /**
     * If you want to perform recursive annotations, this method helps you to find out which annotations were created, and
     * therefore on which ones you need to recurse.
     *
     * @return a list of created annotations
     */
    public List<? extends Annotation> getCreatedAnnotations() {
        return createdForRecursion;
    }
}
