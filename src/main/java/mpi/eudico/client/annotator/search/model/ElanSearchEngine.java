package mpi.eudico.client.annotator.search.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import mpi.eudico.client.annotator.search.result.model.ElanMatch;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeRelation;
import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.model.QueryFormulationException;
import mpi.search.content.query.model.RestrictedAnchorConstraint;
import mpi.search.content.query.model.Utilities;
import mpi.search.content.result.model.ContentMatch;
import mpi.search.model.SearchEngine;
import mpi.search.model.SearchListener;
import mpi.search.query.model.Query;


/**
 * The SearchEngine performs the actual search in ELAN.
 *
 * @author Alexander Klassmann
 * @version Aug 2005 Identity removed
 */
public class ElanSearchEngine implements SearchEngine {
    private static final Logger logger = Logger.getLogger(ElanSearchEngine.class.getName());
    private Map<String, List<? extends Annotation>> annotationHash = new HashMap<String, List<? extends Annotation>>();
    private Map<Constraint, Pattern> patternHash = new HashMap<Constraint, Pattern>();
    private Map<Constraint, Tier[]> relationshipHash = new HashMap<Constraint, Tier[]>();
    private Map<Constraint, TierImpl> unitTierHash = new HashMap<Constraint, TierImpl>();
    private Transcription transcription;

    /**
     * Creates a search engine for a specific transcription.
     *
     * @param searchTool the search listener
     * @param transcription the transcription to search in
     */
    public ElanSearchEngine(SearchListener searchTool,
        Transcription transcription) {
        this.transcription = transcription;
        logger.setLevel(Level.ALL);
    }

    /**
     * Performs the search.
     *
     * @param query the query to execute
     *
     * @throws PatternSyntaxException if the search (regular expression) pattern is malformed
     * @throws QueryFormulationException if the search query is malformed
     * @throws NullPointerException if a null pointer is encountered
     */
    public void executeThread(ContentQuery query)
        throws PatternSyntaxException, QueryFormulationException, 
            NullPointerException {
        //set unlimited size since search is done only within one transcription
        query.getResult().setPageSize(Integer.MAX_VALUE);
        initHashtables(query);

        AnchorConstraint anchorConstraint = query.getAnchorConstraint();

        String[] tierNames = anchorConstraint.getTierNames();

        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tierNames = annotationHash.keySet().toArray(new String[0]);
        }

        for (String tierName : tierNames) {
        	List<? extends Annotation> anchorAnnotations = annotationHash.get(tierName);
            List<ElanMatch> anchorMatches;

            if (!(anchorConstraint instanceof RestrictedAnchorConstraint)) {
                int[] range = getAnnotationIndicesInScope(anchorAnnotations,
                        anchorConstraint.getLowerBoundary(),
                        anchorConstraint.getUpperBoundary(),
                        anchorConstraint.getUnit());

                anchorMatches = getMatches(null,
                        patternHash.get(anchorConstraint),
                        anchorConstraint.getId(), anchorAnnotations, range);
            } else {
            	/*
                anchorMatches =
                		(List)	// FIXME TYPE This is an unsafe type conversion!
                		        // I am not even sure why it is supposed to be correct.
                		((RestrictedAnchorConstraint) anchorConstraint).getResult()
                                 .getMatches(tierName);*/
                
                anchorMatches = toElanMatches(((RestrictedAnchorConstraint) anchorConstraint)
                		.getResult().getMatches(tierName));
            }

            filterDependentConstraints(anchorMatches, anchorConstraint);

            for (int j = 0; j < anchorMatches.size(); j++) {
            	ElanMatch em = anchorMatches.get(j);
            	em.setFileName(((TranscriptionImpl) transcription).getPathName());
            	query.getResult().addMatch(em);
                //query.getResult().addMatch((ElanMatch) anchorMatches.get(j));
            }
        }
    }

    /**
     * Starts execution of a query.
     *
     * @param query the query to execute
     *
     * @throws Exception any exception that can occur
     */
    @Override
	public void performSearch(Query query) throws Exception {
        executeThread((ContentQuery) query);
    }

    /**
     * Returns the annotation indices without a specified scope.
     * This is the same as getAnnotationIndicesInScope(...) with distance 0.
     *
     * @param annotationList the list of annotations to search
     * @param intervalBegin start time of the interval, of the scope
     * @param intervalEnd the end time of the interval, of the scope
     * @param timeComparisonMode the compare method
     *
     * @return int[] an array of indices of annotation within scope (meeting the constraints)
     */
    private static int[] getAnnotationIndicesInScope(List<? extends Annotation> annotationList,
        long intervalBegin, long intervalEnd, String timeComparisonMode) {
        return getAnnotationIndicesInScope(annotationList, intervalBegin,
            intervalEnd, 0L, timeComparisonMode);
    }

    /**
     * Returns indices of annotations that fulfill the time constraints, 
     * determined by the parameter {@code distance} and the {@code timeComparisonMode}.
     * The {@code distance} is used only for particular comparison modes 
     *
     * @param annotationList the list of annotations to search
     * @param intervalBegin the interval begin time
     * @param intervalEnd the interval end time
     * @param distance the distance in milliseconds
     * @param timeComparisonMode the comparison mode, one of the 
     * {@link Constraint} time interval constants
     *
     * @return int[] an array of indices of annotation within scope
     */
    private static int[] getAnnotationIndicesInScope(List<? extends Annotation> annotationList,
        long intervalBegin, long intervalEnd, long distance,
        String timeComparisonMode) {
        int[] annotationsInInterval = new int[annotationList.size()];
        int index = 0;

        for (int i = 0; i < annotationList.size(); i++) {
            Annotation annotation = annotationList.get(i);
            boolean constraintFulfilled = false;

            if (Constraint.OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlaps(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.IS_INSIDE.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isInside(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.NO_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.doesNotOverlap(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.NOT_INSIDE.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isNotInside(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.LEFT_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlapsOnLeftSide(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.RIGHT_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlapsOnRightSide(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.WITHIN_OVERALL_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinDistance(annotation,
                        intervalBegin, intervalEnd, distance);
            } else if (Constraint.WITHIN_DISTANCE_TO_LEFT_BOUNDARY.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinLeftDistance(annotation,
                        intervalBegin, distance);
            } else if (Constraint.WITHIN_DISTANCE_TO_RIGHT_BOUNDARY.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinRightDistance(annotation,
                        intervalEnd, distance);
            } else if (Constraint.BEFORE_LEFT_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isBeforeLeftDistance(annotation,
                        intervalBegin, distance);
            } else if (Constraint.AFTER_RIGHT_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isAfterRightDistance(annotation,
                        intervalEnd, distance);
            }

            if (constraintFulfilled) {
                annotationsInInterval[index++] = i;
            }
        }

        int[] range = new int[index];
        System.arraycopy(annotationsInInterval, 0, range, 0, index);

        return range;
    }

    /**
     * Returns a list with the annotations (not their indices!) in constraint
     * tiers within specified range.
     *
     * @param lowerBoundary the start time boundary 
     * @param upperBoundary the end time boundary 
     * @param unitTier the unit tier
     * @param unitAnnotations the unit annotations
     * @param relationship the related tiers
     * @param centralAnnotation the central or reference annotation
     *
     * @return {@code List<Annotation>}
     *
     * @throws NullPointerException any null pointer that might occur
     */
    private static List<Annotation> getAnnotationsInScope(long lowerBoundary,
        long upperBoundary, TierImpl unitTier, List<? extends Annotation> unitAnnotations,
        Tier[] relationship, Annotation centralAnnotation)
        throws NullPointerException {
        List<Annotation> annotationsInScope = new ArrayList<Annotation>();
        Annotation centralUnitAnnotation = centralAnnotation;

        while ((centralUnitAnnotation != null) && (centralUnitAnnotation.getTier() != unitTier)) {
            centralUnitAnnotation = centralUnitAnnotation.getParentAnnotation();
        }

        if (centralUnitAnnotation == null) {
            throw new NullPointerException();
        }

        int unitAnnotationIndex = unitAnnotations.indexOf(centralUnitAnnotation);

        int[] unitAnnotationIndicesInScope = getRangeForTier(unitTier,
                lowerBoundary, upperBoundary, unitAnnotationIndex);

        Annotation rootOfCentralAnnotation = centralUnitAnnotation;

        while (rootOfCentralAnnotation.hasParentAnnotation()) {
            rootOfCentralAnnotation = rootOfCentralAnnotation.getParentAnnotation();
        }

        logger.log(Level.FINE,
            "Unit annotation " + centralUnitAnnotation.getValue());

        Annotation unitAnnotation;

        for (int element : unitAnnotationIndicesInScope) {
            unitAnnotation = unitAnnotations.get(element);

            boolean haveSameRoot = true;

            if (unitAnnotation.hasParentAnnotation()) {
                Annotation rootOfUnitAnnotation = unitAnnotation;

                while (rootOfUnitAnnotation.hasParentAnnotation()) {
                    rootOfUnitAnnotation = rootOfUnitAnnotation.getParentAnnotation();
                }

                haveSameRoot = rootOfUnitAnnotation == rootOfCentralAnnotation;
            }

            if (haveSameRoot) {
                annotationsInScope.addAll(getDescAnnotations(unitAnnotation,
                        relationship));
            }
        }

        return annotationsInScope;
    }

    /**
     * Returns all descendant annotations (e.g. children of children etc.).
     *
     * @param ancestorAnnotation the annotation for which to get the descendant annotations 
     * @param relationship the tiers to check
     *
     * @return a {@code List<Annotation>}
     */
    private static List<Annotation> getDescAnnotations(Annotation ancestorAnnotation,
        Tier[] relationship) {
        List<Annotation> childAnnotations = null;
        List<Annotation> parentAnnotations = new ArrayList<Annotation>();
        parentAnnotations.add(ancestorAnnotation);

        for (int r = relationship.length - 1; r >= 0; r--) {
            childAnnotations = new ArrayList<Annotation>();

            try {
                for (int i = 0; i < parentAnnotations.size(); i++) {
                    childAnnotations.addAll(parentAnnotations.get(
                            i).getChildrenOnTier(relationship[r]));
                }
            } catch (Exception re) {
                re.printStackTrace();

                return new ArrayList<Annotation>();
            }

            parentAnnotations = childAnnotations;
        }

        return parentAnnotations;
    }

    /**
     * Returns all (pattern) matches in a tier.
     *
     * @param parentMatch the parent match
     * @param pattern the search pattern
     * @param constraintId a constraint id
     * @param annotationList the annotations to query
     * @param range sub-indices of annotations
     *
     * @return a list of {@code ElanMatch}es
     */
    private static List<ElanMatch> getMatches(ElanMatch parentMatch, Pattern pattern,
        String constraintId, List<? extends Annotation> annotationList, int[] range) {
        List<ElanMatch> matchList = new ArrayList<ElanMatch>();

        for (int i = 0; i < range.length; i++) {
            Annotation annotation = annotationList.get(range[i]);
            Matcher matcher = pattern.matcher(annotation.getValue());

            if (matcher.find()) {
                List<int[]> substringIndices = new ArrayList<int[]>();

                do {
                    substringIndices.add(new int[] {
                            matcher.start(0), matcher.end(0)
                        });
                } while (matcher.find());

                ElanMatch match = new ElanMatch(parentMatch, annotation,
                        constraintId, range[i],
                        substringIndices.toArray(new int[0][0]));

                if (range[i] > 0) {
                    match.setLeftContext(annotationList.get(range[i] - 1));
                }

                if ((match.getIndex() + 1) < annotationList.size()) {
                    match.setRightContext(annotationList.get(range[i] + 1));
                }

                //add parent
                // TODO should it not be  if(annotation.hasParentAnnotation()){ //???
                // TODO because of range[i]; and in the next line too
                if(annotationList.get(i).hasParentAnnotation()){
                	match.setParentContext(annotationList.get(i).getParentAnnotation());
                }
                
                //add children
                // TODO should it not be  TierImpl tier=(TierImpl)annotation.getTier(); //???
                TierImpl tier=(TierImpl)annotationList.get(i).getTier();
                match.setChildrenContext(constructChildrenString(tier.getChildTiers(), annotationList.get(i)));
                
                matchList.add(match);
            }
        }

        return matchList;
    }
    
    /**
     * Converts a list of {@code ContentMatch} objects to a list of 
     * {@code ElanMatch} objects, by means of a safe cast per item in the list.
     *  
     * @param contentMatches a list of {@code ContentMatch} objects
     * @return a list of {@code ElanMatch} objects, not {@code null} but 
     * possibly empty
     */
    private static List<ElanMatch> toElanMatches(List<ContentMatch> contentMatches) {
    	List<ElanMatch> elanMatches = new ArrayList<ElanMatch>();
    	
    	if (contentMatches != null && !contentMatches.isEmpty()) {
    		for (ContentMatch cm : contentMatches) {
    			if (cm instanceof ElanMatch) {
    				elanMatches.add((ElanMatch) cm);
    			}
    		}
    	}
    	
    	return elanMatches;
    }

    /**
     * Method to construct a string with the children of a particular annotation.
     * Author: mod. Coralie Villes
     * 
     * @param tiers the tier list of children
     * @param annotation the current annotation
     * @return a String representation of the annotation's children
     */
    public static String constructChildrenString(List<? extends Tier> tiers, Annotation annotation){
    	StringBuilder childrenBuffer = new StringBuilder();
    	if (!tiers.isEmpty()) {
    		for (int j = 0; j < tiers.size(); j++){
    			List<Annotation> children =annotation.getChildrenOnTier(tiers.get(j));
    			Collections.sort(children);
    			childrenBuffer.append('[');
    			for (Annotation child : children) {
        			childrenBuffer.append(child.getValue());
        			childrenBuffer.append(' ');
    			}
    			childrenBuffer.append(']');
    		}
    	}
    	return childrenBuffer.toString();
    }
    
    /**
     * Computes the intersection of range and [0..tier.size] and returns an 
     * array of the integers in this intersection.
     *
     * @param tier the input tier
     * @param lowerBoundary the lower boundary
     * @param upperBoundary the upper boundary
     * @param center the center
     *
     * @return an {@code int[]} containing indices 
     */
    private static int[] getRangeForTier(TierImpl tier, long lowerBoundary,
        long upperBoundary, int center) {
        int newLowerBoundary = (lowerBoundary == Long.MIN_VALUE) ? 0
                                                                 : (int) Math.max(0,
                center + lowerBoundary);
        int newUpperBoundary = (upperBoundary == Long.MAX_VALUE)
            ? (tier.getNumberOfAnnotations() - 1)
            : (int) Math.min(tier.getNumberOfAnnotations() - 1,
                center + upperBoundary);

        int[] range = new int[-newLowerBoundary + newUpperBoundary + 1];

        for (int i = 0; i < range.length; i++) {
            range[i] = i + newLowerBoundary;
        }

        return range;
    }

    /**
     * Returns array of all Tiers between ancestor and descendant tier,
     * including descendTier, excluding ancestorTier; empty, if ancestorTier
     * == descendTier
     *
     * @param ancesterTier the ancestor tier
     * @param descendTier the descendant tier
     *
     * @return TierImpl[]
     */
    private static TierImpl[] getRelationship(TierImpl ancesterTier,
        TierImpl descendTier) {
        List<TierImpl> relationship = new ArrayList<TierImpl>();
        TierImpl parentTier = descendTier;

        try {
            if (descendTier.hasAncestor(ancesterTier)) {
                while (!ancesterTier.equals(parentTier)) {
                    relationship.add(parentTier);
                    parentTier = parentTier.getParentTier();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return relationship.toArray(new TierImpl[0]);
    }

    /**
     * Returns child matches.
     *
     * @param match the reference match
     * @param constraint the constraint
     *
     * @return a list of child matches
     *
     * @throws NullPointerException any null exception
     */
    private List<ElanMatch> getChildMatches(ElanMatch match, Constraint constraint)
        throws NullPointerException {
        TierImpl unitTier = null;
        List<? extends Annotation> unitAnnotations = null;
        List<? extends Annotation> constraintAnnotations = null;
        Tier[] relShip = null;

        long lowerBoundary = constraint.getLowerBoundary();
        long upperBoundary = constraint.getUpperBoundary();
        Pattern pattern = patternHash.get(constraint);
        // HS Nov 2011: added support for multiple "child" tiers
        String[] tierNames = constraint.getTierNames();
        
        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tierNames = annotationHash.keySet().toArray(new String[0]);
        }
        
        List<ElanMatch> allMatches = new ArrayList<ElanMatch>();
        
        for (String name : tierNames) {      
	        constraintAnnotations = annotationHash.get(name);
	
	        if (Constraint.STRUCTURAL.equals(constraint.getMode())) {
	            unitTier = unitTierHash.get(constraint);
	
	            unitAnnotations = annotationHash.get(unitTier.getName());
	
	            relShip = relationshipHash.get(constraint);
	        }
	
	        List<Annotation> annotationsInScope;
	        int[] annotationIndicesInScope;
	        Annotation annotation = match.getAnnotation();
	
	        if (Constraint.TEMPORAL.equals(constraint.getMode())) {
	            annotationIndicesInScope = getAnnotationIndicesInScope(constraintAnnotations,
	                    annotation.getBeginTimeBoundary(),
	                    annotation.getEndTimeBoundary(), upperBoundary,
	                    constraint.getUnit());
	        } else {
	            annotationsInScope = getAnnotationsInScope(lowerBoundary,
	                    upperBoundary, unitTier, unitAnnotations, relShip,
	                    annotation);
	
	            annotationIndicesInScope = new int[annotationsInScope.size()];
	
	            for (int j = 0; j < annotationsInScope.size(); j++) {
	                annotationIndicesInScope[j] = constraintAnnotations.indexOf(annotationsInScope.get(
	                            j));
	                logger.log(Level.FINE,
	                    "Constraint annotation: " +
	                    annotationsInScope.get(j).getValue());
	            }
	        }
	
	        List<ElanMatch> matches = getMatches(match, pattern, constraint.getId(),
	                constraintAnnotations, annotationIndicesInScope);
	
	        filterDependentConstraints(matches, constraint);
	        allMatches.addAll(matches);
        }
        
        return allMatches;
    }

    private void fillAnnotationHash(Constraint constraint)
        throws QueryFormulationException {
        String[] tierNames = constraint.getTierNames();
        TierImpl[] tiers;

        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tiers = transcription.getTiers().toArray(new TierImpl[0]);
        } else {
            tiers = new TierImpl[tierNames.length];

            for (int i = 0; i < tierNames.length; i++) {
                tiers[i] = (TierImpl) transcription.getTierWithId(tierNames[i]);

                if (tiers[i] == null) {
                    throw new QueryFormulationException(SearchLocale.getString(
                            "Search.Exception.CannotFindTier") + " '" +
                        tierNames[i] + "'");
                }
            }
        }

        for (TierImpl tier : tiers) {
            annotationHash.put(tier.getName(), tier.getAnnotations());
        }

        //find unit tiers for dependent constraints
        if (Constraint.STRUCTURAL.equals(constraint.getMode())) {
            String tierName = constraint.getUnit().substring(0,
                    constraint.getUnit().lastIndexOf(' '));

            TierImpl unitTier = (TierImpl) transcription.getTierWithId(tierName);

            if (unitTier == null) {
                throw new QueryFormulationException(SearchLocale.getString(
                        "Search.Exception.CannotFindTier") + " '" + tierName +
                    "'");
            }

            unitTierHash.put(constraint, unitTier);
            relationshipHash.put(constraint, getRelationship(unitTier, tiers[0]));

            if (!annotationHash.containsKey(tierName)) {
                List<? extends Annotation> annotations = unitTier.getAnnotations();
                annotationHash.put(tierName, annotations);
            }
        }
    }

    /*
     * traverse whole tree
     */
    private void fillHashes(CorpusType type, Constraint constraint)
        throws QueryFormulationException {
        for (Enumeration<Constraint> e = constraint.children(); e.hasMoreElements();) {
            fillHashes(type, e.nextElement());
        }

        fillAnnotationHash(constraint);
        patternHash.put(constraint, Utilities.getPattern(constraint, type));
    }

    private void filterDependentConstraints(List<ElanMatch> startingMatches,
        Constraint constraint) throws NullPointerException {
        for (Enumeration<Constraint> e = constraint.children(); e.hasMoreElements();) {
            int j = 0;

            Constraint childConstraint = e.nextElement();

            while (j < startingMatches.size()) {
                ElanMatch match = startingMatches.get(j);

                List<ElanMatch> childMatches = getChildMatches(match, childConstraint);
                /*
                if (((childConstraint.getQuantifier() == Constraint.ANY) &&
                        (childMatches.size() > 0)) ||
                        ((childConstraint.getQuantifier() == Constraint.NONE) &&
                        (childMatches.size() == 0))) {
                        */
                // HS 03-2008 replaced the "==" equality test by equals because e.g. when a query
                // has been read from file the constants are not always used. All other equality 
                // tests in this class are also performed using equals.
                if (((Constraint.ANY.equals(childConstraint.getQuantifier())) &&
                        (childMatches.size() > 0)) ||
                        ((Constraint.NONE.equals(childConstraint.getQuantifier())) &&
                        (childMatches.size() == 0))) {
                    j++;
                    match.addChildren(childMatches);
                } else {
                    startingMatches.remove(j);
                }
            }
        }
    }

    /**
     * Initializes several hashtables.
     *
     * @param query the content query
     *
     * @throws QueryFormulationException in case of a malformed query
     * @throws PatternSyntaxException in case of a malformed pattern
     */
    private void initHashtables(ContentQuery query)
        throws QueryFormulationException, PatternSyntaxException {
        patternHash.clear();
        annotationHash.clear();
        unitTierHash.clear();
        relationshipHash.clear();

        fillHashes(query.getType(), query.getAnchorConstraint());
    }
}
